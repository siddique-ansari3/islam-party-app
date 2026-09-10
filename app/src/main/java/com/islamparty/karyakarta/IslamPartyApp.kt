package com.islamparty.karyakarta

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.islamparty.karyakarta.data.AppContainer
import com.islamparty.karyakarta.data.remote.AuthInterceptor
import okhttp3.OkHttpClient

class IslamPartyApp : Application(), ImageLoaderFactory {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }

    // Worker/Aadhaar photos are served from a protected endpoint - Coil needs the same Bearer token.
    override fun newImageLoader(): ImageLoader {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(container.tokenManager))
            .build()
        return ImageLoader.Builder(this).okHttpClient(client).build()
    }
}
