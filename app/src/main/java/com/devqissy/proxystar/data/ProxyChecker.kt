package com.devqissy.proxystar.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap

object ProxyChecker {

    const val FAST_TCP_TIMEOUT_MS = 1_000

    const val FULL_TCP_TIMEOUT_MS = 3_000

    const val READ_TIMEOUT_MS = 2_500

    private const val DNS_CACHE_LIMIT = 4_000

    private val NET: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(96)


    private val NXDOMAIN = Any()

    private val dns = ConcurrentHashMap<String, Any>()

    fun beginPass() {
        dns.clear()
    }

    private fun addressOf(host: String): InetAddress? {
        // A literal address needs no lookup at all.
        if (host[0].isDigit() || host[0] == '[') {
            return runCatching { InetAddress.getByName(host) }.getOrNull()
        }
        dns[host]?.let { return if (it === NXDOMAIN) null else it as InetAddress }

        val addr = runCatching { InetAddress.getByName(host) }.getOrNull()
        if (dns.size > DNS_CACHE_LIMIT) dns.clear()
        dns[host] = addr ?: NXDOMAIN
        return addr
    }

    suspend fun probeTcp(proxy: Proxy, timeoutMs: Int = FAST_TCP_TIMEOUT_MS): Int? =
        withContext(NET) {
            val addr = addressOf(proxy.server) ?: return@withContext null
            var socket: Socket? = null
            try {
                val start = System.nanoTime()
                socket = Socket().apply { tcpNoDelay = true }
                socket.connect(InetSocketAddress(addr, proxy.port), timeoutMs)
                ((System.nanoTime() - start) / 1_000_000L).toInt()
            } catch (_: Exception) {
                null
            } finally {
                runCatching { socket?.close() }
            }
        }

    suspend fun check(proxy: Proxy): Int? =
        withContext(NET) {
            val addr = addressOf(proxy.server) ?: return@withContext null
            FakeTlsProbe.probe(proxy, addr, FULL_TCP_TIMEOUT_MS, READ_TIMEOUT_MS)
        }
}
