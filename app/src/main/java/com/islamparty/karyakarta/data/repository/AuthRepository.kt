package com.islamparty.karyakarta.data.repository

import com.islamparty.karyakarta.data.local.TokenManager
import com.islamparty.karyakarta.data.model.LoginRequest
import com.islamparty.karyakarta.data.remote.ApiService

class AuthRepository(private val api: ApiService, private val tokenManager: TokenManager) {

    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        val response = api.login(LoginRequest(email.trim(), password))
        tokenManager.saveSession(
            token = response.token,
            role = response.user.role,
            city = response.user.city,
            name = response.user.name
        )
    }

    suspend fun logout() {
        tokenManager.clearSession()
    }

    suspend fun isLoggedIn(): Boolean = tokenManager.getToken() != null
    suspend fun currentRole(): String? = tokenManager.getRole()
    suspend fun currentCity(): String? = tokenManager.getCity()
    suspend fun currentName(): String? = tokenManager.getName()
}
