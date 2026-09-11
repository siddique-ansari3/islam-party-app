package com.islamparty.karyakarta.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

/**
 * Persists the JWT + logged-in user's role/city using Jetpack DataStore.
 * Also keeps an in-memory cache so the OkHttp auth interceptor (which runs
 * synchronously on a background thread) can read the token without suspending.
 */
class TokenManager(private val context: Context) {

    private object Keys {
        val TOKEN = stringPreferencesKey("token")
        val ROLE = stringPreferencesKey("role")
        val CITY = stringPreferencesKey("city")
        val NAME = stringPreferencesKey("name")
    }

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val cachedToken = MutableStateFlow<String?>(null)

    private val _sessionExpired = MutableStateFlow(false)
    val sessionExpired: StateFlow<Boolean> = _sessionExpired.asStateFlow()

    init {
        cachedToken.value = runBlocking { context.dataStore.data.first()[Keys.TOKEN] }
    }

    suspend fun saveSession(token: String, role: String, city: String?, name: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TOKEN] = token
            prefs[Keys.ROLE] = role
            prefs[Keys.CITY] = city ?: ""
            prefs[Keys.NAME] = name
        }
        cachedToken.value = token
        _sessionExpired.value = false
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
        cachedToken.value = null
        _sessionExpired.value = false
    }

    /** Called from OkHttp when a request that sent a Bearer token receives HTTP 401. */
    fun onUnauthorized() {
        if (cachedToken.value.isNullOrBlank() && _sessionExpired.value) return
        cachedToken.value = null
        _sessionExpired.value = true
        ioScope.launch { context.dataStore.edit { it.clear() } }
    }

    fun consumeSessionExpired() {
        _sessionExpired.value = false
    }

    suspend fun getRole(): String? = context.dataStore.data.first()[Keys.ROLE]
    suspend fun getCity(): String? = context.dataStore.data.first()[Keys.CITY]?.takeIf { it.isNotBlank() }
    suspend fun getName(): String? = context.dataStore.data.first()[Keys.NAME]
    suspend fun getToken(): String? = context.dataStore.data.first()[Keys.TOKEN]
}
