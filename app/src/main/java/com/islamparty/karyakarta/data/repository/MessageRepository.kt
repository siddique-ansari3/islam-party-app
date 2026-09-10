package com.islamparty.karyakarta.data.repository

import com.islamparty.karyakarta.data.model.SendMessageRequest
import com.islamparty.karyakarta.data.model.SendMessageResponse
import com.islamparty.karyakarta.data.remote.ApiService

class MessageRepository(private val api: ApiService) {
    suspend fun send(workerIds: List<String>?, allWorkers: Boolean, channel: String, message: String): SendMessageResponse =
        api.sendMessage(SendMessageRequest(workerIds = workerIds, allWorkers = allWorkers, channel = channel, message = message))
}
