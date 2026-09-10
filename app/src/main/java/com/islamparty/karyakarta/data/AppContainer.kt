package com.islamparty.karyakarta.data

import android.content.Context
import com.islamparty.karyakarta.data.local.TokenManager
import com.islamparty.karyakarta.data.remote.RetrofitClient
import com.islamparty.karyakarta.data.repository.AuthRepository
import com.islamparty.karyakarta.data.repository.MessageRepository
import com.islamparty.karyakarta.data.repository.WorkerRepository

/** Simple manual dependency container - avoids pulling in a DI framework for this scope. */
class AppContainer(context: Context) {
    val tokenManager = TokenManager(context)
    private val api = RetrofitClient.getApi(tokenManager)

    val authRepository = AuthRepository(api, tokenManager)
    val workerRepository = WorkerRepository(api)
    val messageRepository = MessageRepository(api)
}
