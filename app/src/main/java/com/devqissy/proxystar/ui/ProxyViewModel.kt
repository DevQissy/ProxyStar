package com.devqissy.proxystar.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class ProxyViewModel(app: Application) : AndroidViewModel(app) {

    val uiState = ProxyEngine.state

    init {
        ProxyEngine.start(app)
    }

    fun refresh() = ProxyEngine.refresh()

    fun switchNext() = ProxyEngine.switchNext()

    fun onForeground() = ProxyEngine.onForeground()
}
