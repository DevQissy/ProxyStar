package com.devqissy.proxystar.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "app_prefs")

object Prefs {

    private val KEY_ONBOARDED = booleanPreferencesKey("onboarded")

    private val KEY_SKIP_TELEGRAM = booleanPreferencesKey("confirm_skip_telegram")
    private val KEY_SKIP_GITHUB = booleanPreferencesKey("confirm_skip_github")

    suspend fun isOnboarded(context: Context): Boolean =
        context.dataStore.data.first()[KEY_ONBOARDED] ?: false

    suspend fun setOnboarded(context: Context, value: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDED] = value }
    }

    suspend fun skipTelegramConfirm(context: Context): Boolean =
        context.dataStore.data.first()[KEY_SKIP_TELEGRAM] ?: false

    suspend fun setSkipTelegramConfirm(context: Context, value: Boolean) {
        context.dataStore.edit { it[KEY_SKIP_TELEGRAM] = value }
    }

    suspend fun skipGithubConfirm(context: Context): Boolean =
        context.dataStore.data.first()[KEY_SKIP_GITHUB] ?: false

    suspend fun setSkipGithubConfirm(context: Context, value: Boolean) {
        context.dataStore.edit { it[KEY_SKIP_GITHUB] = value }
    }
}