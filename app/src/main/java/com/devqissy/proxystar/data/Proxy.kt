package com.devqissy.proxystar.data

import android.net.Uri

data class Proxy(
    val server: String,
    val port: Int,
    val secret: String,
    val source: String,
    val latencyMs: Int? = null,
    val alive: Boolean? = null
) {
    val id: String get() = "$server:$port:$secret"

    val isVerifiable: Boolean
        get() = secret.length >= 35 &&
                secret.startsWith("ee", ignoreCase = true) &&
                FakeTlsProbe.sniOf(secret) != null

    fun toTgLink(): String =
        "tg://proxy?server=${Uri.encode(server)}&port=$port&secret=${Uri.encode(secret)}"
}