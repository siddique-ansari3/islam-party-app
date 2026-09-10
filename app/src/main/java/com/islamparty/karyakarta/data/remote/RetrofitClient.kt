package com.islamparty.karyakarta.data.remote

import com.islamparty.karyakarta.BuildConfig
import com.islamparty.karyakarta.data.local.TokenManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    @Volatile private var apiService: ApiService? = null

    fun getApi(tokenManager: TokenManager): ApiService {
        return apiService ?: synchronized(this) {
            apiService ?: build(tokenManager).also { apiService = it }
        }
    }

    private fun build(tokenManager: TokenManager): ApiService {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

        val logging = HttpLoggingInterceptor().apply {
            // Never log bodies in release builds - Aadhaar numbers and photos must not hit logcat.
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }
}
