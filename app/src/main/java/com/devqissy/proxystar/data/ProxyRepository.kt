package com.devqissy.proxystar.data

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class SourceResult(
    val url: String,
    val count: Int,
    val error: String? = null,
    val bodyPreview: String = ""
)

object ProxyRepository {

    private const val TAG = "ProxyRepository"

    private const val SOURCE_CONCURRENCY = 12

    private const val PER_SOURCE_LIMIT = 150

    private const val RACE_CONCURRENCY = 5

    private const val HOST_FAIL_LIMIT = 3

    private val client = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .callTimeout(7, TimeUnit.SECONDS)
        .build()

    private data class Candidate(val kind: String, val url: String, val accept: String? = null)

    @Volatile
    private var preferred: String? = null

    private val hostFails = ConcurrentHashMap<String, AtomicInteger>()

    private val raceGate = Semaphore(RACE_CONCURRENCY)

    fun beginPass() {
        hostFails.clear()
    }

    private fun noteFail(kind: String) {
        hostFails.computeIfAbsent(kind) { AtomicInteger() }.incrementAndGet()
    }

    private fun isDead(kind: String): Boolean =
        (hostFails[kind]?.get() ?: 0) >= HOST_FAIL_LIMIT

    private val rawGithubRegex = Regex(
        "https://raw\\.githubusercontent\\.com/([^/]+)/([^/]+)/([^/]+)/(.+)"
    )
    private val linkRegex = Regex(
        "(?:tg://proxy|https?://t\\.me/proxy)\\?[a-zA-Z0-9%._=&-]+",
        RegexOption.IGNORE_CASE
    )
    private val secretLikeRegex = Regex("^[0-9a-zA-Z_-]{16,}$")

    private val whitespaceRegex = Regex("\\s+")
    private val fieldSplitRegex = Regex("[\\s,|:]+")

    suspend fun streamSources(
        shouldStop: () -> Boolean = { false },
        onSource: suspend (List<Proxy>, SourceResult) -> Unit
    ): List<SourceResult> = coroutineScope {
        val sources = SourceDefaults.SOURCES

        val report = Collections.synchronizedList(mutableListOf<SourceResult>())
        val seen = ConcurrentHashMap.newKeySet<String>()
        val gate = Semaphore(SOURCE_CONCURRENCY)

        sources.map { url ->
            async {
                gate.withPermit {
                    if (shouldStop()) return@withPermit
                    val (list, sr) = fetchOne(url)
                    report += sr
                    // Cap before de-duplicating: the dropped tail stays available to
                    // another source instead of being marked as already seen.
                    val fresh = list.take(PER_SOURCE_LIMIT).filter { seen.add(it.id) }
                    if (fresh.isNotEmpty()) onSource(fresh, sr)
                }
            }
        }.awaitAll()

        report.toList()
    }

    suspend fun fetchAll(): Pair<List<Proxy>, List<SourceResult>> {
        val acc = Collections.synchronizedList(mutableListOf<Proxy>())
        val report = streamSources { list, _ -> acc.addAll(list) }
        return acc.toList().distinctBy { it.id } to report
    }

    private suspend fun fetchOne(url: String): Pair<List<Proxy>, SourceResult> =
        try {
            val f = fetchSource(url)
            f.proxies to SourceResult(url, f.proxies.size, bodyPreview = f.preview)
        } catch (e: Exception) {
            Log.e(TAG, "Source failed: $url → ${e.message}")
            emptyList<Proxy>() to SourceResult(url, 0, e.message ?: "unknown")
        }

    private class Fetched(val proxies: List<Proxy>, val preview: String)

    private suspend fun fetchSource(url: String): Fetched {
        val cands = candidatesOf(url)
        if (cands.isEmpty()) throw IOException("no candidate")

        val hit = preferred?.let { p -> cands.firstOrNull { it.kind == p } }
        if (hit != null) {
            try {
                return fetchUrl(hit.url, url, hit.accept)
            } catch (e: Exception) {
                Log.w(TAG, "preferred mirror ${hit.kind} failed: ${e.message}")
                noteFail(hit.kind)
            }
        }
        return raceFetch(cands, url)
    }

    private suspend fun raceFetch(cands: List<Candidate>, sourceTag: String): Fetched =
        // The permit covers the launches too — gating only the `await` would still
        // let every race open its connections at the same moment.
        raceGate.withPermit {
            coroutineScope {
                val win = CompletableDeferred<Fetched>()
                val remaining = AtomicInteger(cands.size)
                val jobs = cands.map { c ->
                    launch {
                        try {
                            val f = fetchUrl(c.url, sourceTag, c.accept)
                            if (!win.isCompleted) {
                                preferred = c.kind
                                win.complete(f)
                            }
                        } catch (e: Exception) {
                            noteFail(c.kind)
                            if (remaining.decrementAndGet() == 0 && !win.isCompleted) {
                                win.completeExceptionally(IOException("all mirrors failed"))
                            }
                        }
                    }
                }
                try {
                    win.await()
                } finally {
                    jobs.forEach { it.cancel() }
                }
            }
        }


    private fun candidatesOf(url: String): List<Candidate> {
        // `/refs/heads/main/` and `/main/` are the same file.
        val clean = url.replace("/refs/heads/", "/")
        val m = rawGithubRegex.find(clean) ?: return listOf(Candidate("direct", clean))
        val (user, repo, branch, path) = m.destructured

        return buildList {
            if (!isDead("jsdelivr")) add(
                Candidate("jsdelivr", "https://cdn.jsdelivr.net/gh/$user/$repo@$branch/$path")
            )
            if (!isDead("jsdelivr-cf")) add(
                Candidate("jsdelivr-cf", "https://testingcf.jsdelivr.net/gh/$user/$repo@$branch/$path")
            )
            if (!isDead("raw")) add(Candidate("raw", clean))
            if (!isDead("ghproxy")) add(Candidate("ghproxy", "https://gh-proxy.com/$clean"))
            if (!isDead("jsdelivr-fastly")) add(
                Candidate("jsdelivr-fastly", "https://fastly.jsdelivr.net/gh/$user/$repo@$branch/$path")
            )
            if (!isDead("github-api")) add(
                Candidate(
                    "github-api",
                    "https://api.github.com/repos/$user/$repo/contents/$path",
                    "application/vnd.github.raw"
                )
            )
            if (!isDead("jsdelivr-gcore")) add(
                Candidate("jsdelivr-gcore", "https://gcore.jsdelivr.net/gh/$user/$repo@$branch/$path")
            )
        }.ifEmpty {
            // Everything is marked dead — better one more attempt than none.
            listOf(Candidate("raw", clean))
        }
    }

    private suspend fun fetchUrl(target: String, sourceTag: String, accept: String?): Fetched {
        val builder = Request.Builder()
            .url(target)
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120.0 Mobile Safari/537.36"
            )
        if (accept != null) builder.header("Accept", accept)

        client.executeSuspend(builder.build()).use { resp ->
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code}")
            val body = resp.body?.string().orEmpty()
            if (body.isBlank()) throw Exception("empty body")
            val proxies = parseAll(body, sourceTag)
            Log.i(TAG, "$target → HTTP ${resp.code}, ${body.length} chars, ${proxies.size} proxies")
            return Fetched(proxies, body.take(300).replace(whitespaceRegex, " "))
        }
    }

    private fun parseAll(text: String, source: String): List<Proxy> {
        // Avoid copying the whole body when there is nothing to unescape.
        val clean = if (text.contains("&amp;")) text.replace("&amp;", "&") else text
        return parseRawLinks(clean, source)
            .ifEmpty { parseJsonFlexible(clean, source) }
            .ifEmpty { parsePlainLines(clean, source) }
    }

    private fun parseRawLinks(text: String, source: String): List<Proxy> =
        linkRegex.findAll(text).mapNotNull { m ->
            val uri = Uri.parse(m.value)
            val server = uri.getQueryParameter("server")
            val port = uri.getQueryParameter("port")?.toIntOrNull()
            val secret = uri.getQueryParameter("secret")
            if (server.isNullOrBlank() || secret.isNullOrBlank() ||
                port == null || port !in 1..65535
            ) null else Proxy(server, port, secret, source)
        }.toList()

    private fun parseJsonFlexible(text: String, source: String): List<Proxy> = try {
        val trimmed = text.trim()
        val arr: JSONArray = when {
            trimmed.startsWith("[") -> JSONArray(trimmed)
            trimmed.startsWith("{") -> {
                val obj = JSONObject(trimmed)
                val key = listOf("proxies", "data", "results", "items")
                    .firstOrNull { obj.optJSONArray(it) != null }
                key?.let { obj.getJSONArray(it) } ?: JSONArray().also { it.put(obj) }
            }
            trimmed.lines().count { it.trim().startsWith("{") } > 1 -> {
                JSONArray().also { a ->
                    trimmed.lines().forEach { l ->
                        val t = l.trim()
                        if (t.startsWith("{")) runCatching { a.put(JSONObject(t)) }
                    }
                }
            }
            else -> return emptyList()
        }
        (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            val server = o.optString("host").ifBlank { o.optString("server") }
                .ifBlank { o.optString("ip") }.ifBlank { o.optString("address") }
            val port = if (o.has("port")) o.optInt("port") else -1
            val secret = o.optString("secret")
            if (server.isNotBlank() && port in 1..65535 && secret.isNotBlank())
                Proxy(server, port, secret, source) else null
        }
    } catch (_: Exception) {
        emptyList()
    }

    private fun parsePlainLines(text: String, source: String): List<Proxy> =
        text.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.contains("://") && !it.startsWith("#") }
            .mapNotNull { line ->
                val parts = line.split(fieldSplitRegex).filter { it.isNotBlank() }
                if (parts.size < 3) return@mapNotNull null
                val server = parts[0]
                val port = parts[1].toIntOrNull()
                val secret = parts[2]
                if (port != null && port in 1..65535 &&
                    server.contains('.') && secretLikeRegex.matches(secret)
                ) Proxy(server, port, secret, source) else null
            }
            .toList()

    private suspend fun OkHttpClient.executeSuspend(request: Request): Response =
        suspendCancellableCoroutine { cont ->
            val call = newCall(request)
            cont.invokeOnCancellation { runCatching { call.cancel() } }
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (cont.isActive) cont.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (cont.isActive) cont.resume(response) else response.close()
                }
            })
        }
}
