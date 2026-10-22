package com.devqissy.proxystar.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit


object GeoGate {

    private val RESTRICTED = setOf("IR")

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .callTimeout(8, TimeUnit.SECONDS)
        .build()

    private const val ENDPOINT = "https://ipapi.co/json/"

    @Suppress("UNUSED_PARAMETER")
    suspend fun isRestrictedRegion(context: Context): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val req = Request.Builder()
                    .url(ENDPOINT)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 13) Chrome/120.0")
                    .build()
                client.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) return@withContext false
                    val cc = JSONObject(resp.body?.string() ?: "{}")
                        .optString("country_code", "")
                    cc.uppercase() in RESTRICTED
                }
            } catch (_: Exception) {
                false
            }
        }
}