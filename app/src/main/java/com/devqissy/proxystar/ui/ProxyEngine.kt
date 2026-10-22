package com.devqissy.proxystar.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.devqissy.proxystar.data.Proxy
import com.devqissy.proxystar.data.ProxyChecker
import com.devqissy.proxystar.data.ProxyRepository
import com.devqissy.proxystar.data.SourceResult
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.util.Collections
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

private const val TAG = "ProxyEngine"

private const val SHOW_LIMIT = 30

private const val TARGET_ALIVE = 30

private const val FAST_WORKERS = 80

private const val FULL_WORKERS = 14

private const val PUBLISH_INTERVAL_MS = 320L


private const val COLD_DEADLINE_MS = 30_000L

private const val DRY_LIMIT = 1_200

private const val FETCH_DEADLINE_MS = 20_000L

private const val LEGACY_SNAPSHOT = "proxy_snapshot_v1.tsv"

data class ProxyUiState(
    val proxies: List<Proxy> = emptyList(),
    val aliveCount: Int = 0,
    val totalCount: Int = 0,
    val checkedCount: Int = 0,
    val selectedIndex: Int = 0,
    val isLoading: Boolean = false,
    val isChecking: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,

    val diag: String = "",
    val sourceResults: List<SourceResult> = emptyList(),
    val bootResolved: Boolean = false,
    val targetAlive: Int = TARGET_ALIVE
)

object ProxyEngine {

    private val crashGuard = CoroutineExceptionHandler { _, e ->
        Log.e(TAG, "unhandled coroutine failure: ${e.message}", e)
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default + crashGuard)

    private val _state = MutableStateFlow(ProxyUiState())
    val state = _state.asStateFlow()

    private val lock = Any()
    private val pool = LinkedHashMap<String, Proxy>()

    @Volatile private var appContext: Context? = null
    @Volatile private var lastPassAt = 0L
    @Volatile private var lastPublishAt = 0L

    private var started = false
    private val busy = AtomicBoolean(false)

    @Volatile private var booting = false
    private var cycleStart = 0L

    private val connectivityManager: ConnectivityManager?
        get() = appContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    @Synchronized
    fun start(context: Context) {
        appContext = context.applicationContext
        if (started) return
        started = true
        booting = true

        _state.update { it.copy(bootResolved = true) }

        scope.launch { dropLegacySnapshot() }
        scope.launch { runPass(userInitiated = false) }
    }


    fun refresh() {
        _state.update { it.copy(isRefreshing = true) }
        scope.launch { runPass(userInitiated = true) }
    }

    fun onForeground() {
        if (booting) return
        val s = _state.value
        if (s.isLoading || s.isChecking) return
        scope.launch { runPass(userInitiated = false) }
    }

    fun switchNext() {
        val alive = _state.value.proxies.filter { it.alive == true }
        if (alive.size < 2) return
        _state.update { s -> s.copy(selectedIndex = (s.selectedIndex + 1) % alive.size) }
    }

    private fun dropLegacySnapshot() {
        val ctx = appContext ?: return
        runCatching {
            val f = File(ctx.filesDir, LEGACY_SNAPSHOT)
            if (f.exists()) {
                Log.d(TAG, "removing legacy snapshot (${f.length()} bytes)")
                f.delete()
            }
        }
    }

    private suspend fun runPass(userInitiated: Boolean) {
        if (!busy.compareAndSet(false, true)) return
        try {
            appContext ?: return
            cycleStart = System.currentTimeMillis()

            val hasList = _state.value.proxies.isNotEmpty()
            _state.update {
                it.copy(
                    isLoading = !hasList,
                    isRefreshing = userInitiated,
                    error = null
                )
            }

            if (!hasNetwork()) {
                Log.w(TAG, "no active network — skipping this pass")
                _state.update {
                    it.copy(
                        isLoading = false,
                        isChecking = false,
                        error = "offline"
                    )
                }
                return
            }

            ProxyChecker.beginPass()
            ProxyRepository.beginPass()
            val known: List<Proxy>
            synchronized(lock) {
                known = pool.values.filter { it.alive == true }
                pool.keys.removeAll(pool.filterValues { it.alive == false }.keys)
            }
            val knownIds = known.mapTo(HashSet()) { it.id }

            val fastQueue = Channel<Proxy>(Channel.UNLIMITED)
            val fullQueue = Channel<Proxy>(Channel.UNLIMITED)

            val confirmed = AtomicInteger(0)
            val checkedNow = AtomicInteger(0)
            val lastConfirm = AtomicInteger(0)
            val stop = AtomicBoolean(false)

            val fastWorkers = List(FAST_WORKERS) {
                scope.launch {
                    for (p in fastQueue) {
                        if (stop.get()) continue
                        val t = ProxyChecker.probeTcp(p)
                        val done = checkedNow.incrementAndGet()
                        if (t == null) {
                            record(p, null)
                            if (done - lastConfirm.get() >= DRY_LIMIT) stop.set(true)
                        } else {
                            fullQueue.send(p)
                        }
                    }
                }
            }

            val fullWorkers = List(FULL_WORKERS) {
                scope.launch {
                    for (p in fullQueue) {
                        if (stop.get()) continue
                        val t = ProxyChecker.check(p)
                        record(p, t)
                        if (t != null) {
                            lastConfirm.set(checkedNow.get())
                            if (confirmed.incrementAndGet() >= TARGET_ALIVE) stop.set(true)
                        }
                    }
                }
            }

            _state.update { it.copy(isChecking = true) }
            val watchdog = scope.launch {
                delay(COLD_DEADLINE_MS)
                stop.set(true)
                Log.w(TAG, "pass deadline hit (${COLD_DEADLINE_MS}ms) — stopping")
            }

            var results: List<SourceResult> = emptyList()
            var timedOut = false
            val queued = AtomicInteger(0)
            val rejected = AtomicInteger(0)
            val collected = Collections.synchronizedList(mutableListOf<SourceResult>())
            try {
                known.forEach {
                    queued.incrementAndGet()
                    fastQueue.send(it)
                }

                val fetchDeadline = minOf(FETCH_DEADLINE_MS, COLD_DEADLINE_MS)
                withTimeoutOrNull(fetchDeadline) {
                    ProxyRepository.streamSources(shouldStop = { stop.get() }) { fresh, sr ->
                        collected += sr
                        for (p in fresh) {
                            if (stop.get()) break
                            if (p.id in knownIds) continue
                            if (!p.isVerifiable) {
                                rejected.incrementAndGet()
                                continue
                            }
                            queued.incrementAndGet()
                            fastQueue.send(p)
                        }
                    }
                } ?: run {
                    timedOut = true
                }
                if (timedOut) Log.w(TAG, "fetch deadline hit (${fetchDeadline}ms) — using partial results")
            } finally {
                watchdog.cancel()
                fastQueue.close()
                fastWorkers.forEach { it.join() }
                fullQueue.close()
                fullWorkers.forEach { it.join() }
            }
            results = collected.toList()

            _state.update { it.copy(sourceResults = results, isLoading = false) }

            val total = countAll()
            val alive = countAlive()
            val parsed = results.sumOf { it.count }
            val okSources = results.count { it.error == null }
            val diag = "sources $okSources/${results.size} · parsed $parsed · " +
                    "checked ${checkedNow.get()} · alive $alive"
            _state.update { it.copy(diag = diag) }
            Log.d(
                TAG,
                "pass done in ${System.currentTimeMillis() - cycleStart}ms — " +
                        "sources=$okSources/${results.size}failed " +
                        "parsed=$parsed queued=${queued.get()} notFakeTls=${rejected.get()} " +
                        "checked=${checkedNow.get()} pool=$total alive=$alive"
            )
            if (results.isNotEmpty()) {
                Log.d(TAG, "sources: " + results.joinToString { "${it.count}${if (it.error != null) "!" else ""}" })
            }

            if (total == 0) {
                val reason = when {
                    results.isEmpty() -> "no_sources"
                    parsed == 0 -> "no_sources"
                    queued.get() == 0 -> "no_fake_tls"
                    else -> "no_alive"
                }
                Log.w(TAG, "nothing to show: reason=$reason (sources=$okSources, parsed=$parsed)")
                _state.update {
                    it.copy(
                        isLoading = false,
                        isChecking = false,
                        error = reason
                    )
                }
                return
            }

            finishPass()
        } finally {
            busy.set(false)
            booting = false
            if (_state.value.isLoading || _state.value.isChecking || _state.value.isRefreshing) {
                _state.update { it.copy(isLoading = false, isChecking = false, isRefreshing = false) }
            }
        }
    }

    private fun finishPass() {
        lastPassAt = System.currentTimeMillis()
        val alive = countAlive()
        _state.update {
            it.copy(
                isChecking = false,
                isLoading = false,
                isRefreshing = false,
                selectedIndex = 0,
                error = if (alive == 0) "no_alive" else null
            )
        }

        reorderPool()
        publish()
    }


    private fun record(p: Proxy, latency: Int?) {
        val updated = p.copy(latencyMs = latency, alive = latency != null)
        synchronized(lock) { pool[p.id] = updated }
        maybePublish()
    }

    private fun countAll(): Int = synchronized(lock) { pool.size }

    private fun countAlive(): Int = synchronized(lock) { pool.values.count { it.alive == true } }

    private fun maybePublish() {
        val now = System.currentTimeMillis()
        if (now - lastPublishAt < PUBLISH_INTERVAL_MS) return
        lastPublishAt = now
        publish()
    }

    private fun reorderPool() {
        synchronized(lock) {
            val ordered = LinkedHashMap<String, Proxy>(pool.size * 2)
            pool.values.filter { it.alive == true }
                .sortedBy { it.latencyMs ?: Int.MAX_VALUE }
                .forEach { ordered[it.id] = it }
            pool.forEach { (id, p) -> if (p.alive != true) ordered[id] = p }
            pool.clear()
            pool.putAll(ordered)
        }
    }

    private fun publish() {
        val snapshot = synchronized(lock) { pool.values.toList() }
        val checked = snapshot.count { it.alive != null }
        val aliveList = snapshot.filter { it.alive == true }

        val display = aliveList.take(SHOW_LIMIT)

        _state.update { s ->
            s.copy(
                proxies = display,
                aliveCount = aliveList.size.coerceAtMost(TARGET_ALIVE),
                totalCount = snapshot.size,
                checkedCount = checked
            )
        }
    }

    private fun hasNetwork(): Boolean = try {
        val cm = connectivityManager ?: return true
        val net = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(net) ?: return false
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } catch (e: Exception) {
        Log.w(TAG, "network check failed: ${e.message}")
        true
    }
}
