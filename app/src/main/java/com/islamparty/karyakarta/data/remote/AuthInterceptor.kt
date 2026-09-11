package com.islamparty.karyakarta.data.remote

import com.islamparty.karyakarta.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/** Attaches the cached JWT (if any) as a Bearer token to every outgoing request. */
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val isLogin = original.url.encodedPath.endsWith("/api/auth/login")
        val token = tokenManager.cachedToken.value

        val request = if (!isLogin && !token.isNullOrBlank()) {
            original.newBuilder().addHeader("Authorization", "Bearer $token").build()
        } else {
            original
        }

        val response = chain.proceed(request)
        if (response.code == 401 && !isLogin && !token.isNullOrBlank()) {
            tokenManager.onUnauthorized()
        }
        return response
    }
}
