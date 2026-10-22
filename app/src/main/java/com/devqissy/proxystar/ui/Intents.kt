package com.devqissy.proxystar.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.devqissy.proxystar.R
import com.devqissy.proxystar.data.Proxy


internal fun shareLink(context: Context, proxy: Proxy) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, proxy.toTgLink())
    }
    context.startActivity(Intent.createChooser(send, context.getString(R.string.share_title)))
}

internal fun openInTelegram(context: Context, proxy: Proxy) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(proxy.toTgLink())))
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(
            context,
            context.getString(R.string.telegram_missing),
            Toast.LENGTH_SHORT
        ).show()
    }
}

internal fun openInBrowser(context: Context, url: String) {
    if (url.isBlank()) return
    val base = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    val candidates = context.packageManager
        .queryIntentActivities(base, 0)
        .filterNot {
            val pkg = it.activityInfo.packageName
            pkg.startsWith("org.telegram") || pkg.startsWith("org.thunderdog")
        }
    val candidate = candidates.firstOrNull()
    if (candidate != null) {
        try {
            context.startActivity(
                base.setClassName(
                    candidate.activityInfo.packageName,
                    candidate.activityInfo.name
                )
            )
            return
        } catch (_: Exception) {
        }
    }
    try {
        context.startActivity(base)
    } catch (_: Exception) {
        Toast.makeText(
            context,
            context.getString(R.string.telegram_missing),
            Toast.LENGTH_SHORT
        ).show()
    }
}

internal fun openRepository(context: Context) {
    openInBrowser(context, context.getString(R.string.github_url))
}
