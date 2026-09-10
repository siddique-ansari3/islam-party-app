package com.islamparty.karyakarta.data.remote

import com.islamparty.karyakarta.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/** Attaches the cached JWT (if any) as a Bearer token to every outgoing request. */
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenManager.cachedToken.value
        val request = chain.request().newBuilder().apply {
            if (!token.isNullOrBlank()) {
                addHeader("Authorization", "Bearer $token")
            }
        }.build()
        return chain.proceed(request)
    }
}
