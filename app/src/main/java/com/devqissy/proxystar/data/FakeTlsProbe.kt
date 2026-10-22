package com.devqissy.proxystar.data

import android.util.Log
import java.io.ByteArrayOutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.Collections
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.net.ssl.SNIHostName
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLEngineResult
import javax.net.ssl.X509TrustManager

private const val TAG = "FakeTlsProbe"

object FakeTlsProbe {

    private const val READ_CAP = 8_192

    private const val IDLE_MS = 250

    private const val MAX_SCAN_OFFSET = 900

    private const val FAST_SCAN_OFFSET = 64

    private const val HELLO_FLOOR = 16_384
    private const val HELLO_CEILING = 65_536

    private val HEX_ONLY = Regex("^[0-9a-f]+$")
    private val DOMAIN = Regex(
        "^[a-z0-9]([a-z0-9-]*[a-z0-9])?(\\.[a-z0-9]([a-z0-9-]*[a-z0-9])?)+$"
    )
    private val ZEROS = ByteArray(32)
    private val EMPTY = ByteBuffer.allocate(0)

    private val sslContext: SSLContext by lazy {
        SSLContext.getInstance("TLS").apply {
            init(null, arrayOf(TRUST_ALL), SecureRandom())
        }
    }

    fun probe(proxy: Proxy, address: InetAddress, connectTimeoutMs: Int, readTimeoutMs: Int): Int? {
        val sni = sniOf(proxy.secret) ?: return null
        val key = keyOf(proxy.secret) ?: return null
        val hello = clientHello(sni, proxy.port) ?: return null
        val record = authenticate(hello, key)
        val random = record.copyOfRange(11, 43)

        var socket: Socket? = null
        return try {
            socket = Socket().apply { tcpNoDelay = true }
            socket.connect(InetSocketAddress(address, proxy.port), connectTimeoutMs)

            val sentAt = System.nanoTime()
            socket.getOutputStream().apply {
                write(record)
                flush()
            }

            val reply = readBurst(socket, readTimeoutMs)
            val firstByte = reply.firstByteAt
            if (firstByte == 0L || reply.bytes.size < 43) {
                null
            } else if (!provesProxy(reply.bytes, key, random)) {
                null
            } else {
                ((firstByte - sentAt) / 1_000_000L).toInt().coerceAtLeast(0)
            }
        } catch (e: Exception) {
            null
        } finally {
            runCatching { socket?.close() }
        }
    }

    private fun clientHello(sni: String, port: Int): ByteArray? {
        var engine: SSLEngine? = null
        return try {
            val e = sslContext.createSSLEngine(sni, port)
            engine = e
            e.useClientMode = true
            e.sslParameters = e.sslParameters.apply {
                serverNames = Collections.singletonList(SNIHostName(sni))
            }
            e.beginHandshake()

            var capacity = maxOf(e.session.packetBufferSize, HELLO_FLOOR) + 1024
            val acc = ByteArrayOutputStream()
            var guard = 0
            while (guard++ < 6) {
                if (e.handshakeStatus != SSLEngineResult.HandshakeStatus.NEED_WRAP) break
                val out = ByteBuffer.allocate(capacity)
                val r = e.wrap(EMPTY, out)
                out.flip()
                if (out.remaining() > 0) {
                    val chunk = ByteArray(out.remaining())
                    out.get(chunk)
                    acc.write(chunk)
                }
                when (r.status) {
                    SSLEngineResult.Status.BUFFER_OVERFLOW -> {
                        capacity *= 2
                        if (capacity > HELLO_CEILING) break
                    }
                    SSLEngineResult.Status.CLOSED -> break
                    else -> if (r.bytesProduced() > 0) break
                }
            }
            val bytes = acc.toByteArray()
            if (bytes.size < 43) null else bytes
        } catch (e: Exception) {
            Log.w(TAG, "client hello failed for $sni: ${e.message}")
            null
        } finally {
            runCatching { engine?.closeOutbound() }
        }
    }

    private fun authenticate(hello: ByteArray, key: ByteArray): ByteArray {
        val record = hello.copyOf()
        record.fill(0, 11, 43)
        val digest = hmac(key, record, 0, record.size)
        System.arraycopy(digest, 0, record, 11, 32)
        return record
    }

    private fun provesProxy(reply: ByteArray, key: ByteArray, random: ByteArray): Boolean {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))

        val last = minOf(reply.size - 43, MAX_SCAN_OFFSET)
        for (start in 0..last) {
            val type = reply[start].toInt()
            // 0x14 change_cipher_spec, 0x15 alert, 0x16 handshake, 0x17 app data
            if (type != 0x14 && type != 0x15 && type != 0x16 && type != 0x17) continue

            val recordEnd = recordEnd(reply, start)
            if (recordEnd > 0 && recordEnd - start >= 43 &&
                check(mac, reply, start, recordEnd, random)
            ) return true

            // Fallback for a server that hashes the bare payload instead.
            if (start < FAST_SCAN_OFFSET && check(mac, reply, start, reply.size, random)) return true
        }
        return false
    }

    private fun recordEnd(buf: ByteArray, start: Int): Int {
        if (start + 5 > buf.size) return -1
        val len = ((buf[start + 3].toInt() and 0xFF) shl 8) or (buf[start + 4].toInt() and 0xFF)
        val end = start + 5 + len
        return if (end <= buf.size) end else -1
    }

    private fun check(
        mac: Mac,
        buf: ByteArray,
        start: Int,
        end: Int,
        random: ByteArray
    ): Boolean {
        if (end - start < 43) return false
        mac.reset()
        mac.update(random)
        mac.update(buf, start, 11)
        mac.update(ZEROS)
        val tail = end - (start + 43)
        if (tail > 0) mac.update(buf, start + 43, tail)
        return matches(mac.doFinal(), buf, start + 11)
    }

    private fun matches(digest: ByteArray, buf: ByteArray, at: Int): Boolean {
        for (i in 0 until 32) {
            if (digest[i] != buf[at + i]) return false
        }
        return true
    }

    private fun hmac(key: ByteArray, data: ByteArray, from: Int, len: Int): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        mac.update(data, from, len)
        return mac.doFinal()
    }

    private class Burst(val bytes: ByteArray, val firstByteAt: Long)

    private fun readBurst(socket: Socket, readTimeoutMs: Int): Burst {
        val out = ByteArrayOutputStream()
        val chunk = ByteArray(2048)
        val input = socket.getInputStream()
        var first = true
        var firstByteAt = 0L
        while (out.size() < READ_CAP) {
            socket.soTimeout = if (first) readTimeoutMs else IDLE_MS
            val n = try {
                input.read(chunk)
            } catch (_: SocketTimeoutException) {
                break
            } catch (_: Exception) {
                break
            }
            if (n <= 0) break
            if (first) firstByteAt = System.nanoTime()
            out.write(chunk, 0, n)
            first = false
        }
        return Burst(out.toByteArray(), firstByteAt)
    }

    private fun keyOf(secret: String): ByteArray? {
        if (secret.length < 34) return null
        return try {
            ByteArray(16) { i -> secret.substring(2 + i * 2, 4 + i * 2).toInt(16).toByte() }
        } catch (_: Exception) {
            null
        }
    }

    fun sniOf(secret: String): String? {
        val s = secret.lowercase()
        if (!s.startsWith("ee") || s.length < 35) return null

        // "ee" + 16-byte key = 34 chars, the rest is the domain (hex or ascii).
        val tail = s.substring(34)

        if (HEX_ONLY.matches(tail)) {
            val decoded = runCatching {
                tail.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
                    .toString(Charsets.US_ASCII).trimEnd('\u0000')
            }.getOrNull()
            if (decoded != null && decoded.contains('.') && decoded.lowercase().matches(DOMAIN)) {
                return decoded.lowercase()
            }
        }

        val plain = tail.trimEnd('0')
        if (plain.contains('.') && plain.matches(DOMAIN)) return plain
        return null
    }

    private val TRUST_ALL = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }
}
