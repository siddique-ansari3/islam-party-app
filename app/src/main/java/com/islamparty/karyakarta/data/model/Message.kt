package com.islamparty.karyakarta.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SendMessageRequest(
    val workerIds: List<String>?,
    val allWorkers: Boolean,
    val channel: String, // "sms" | "whatsapp" | "both"
    val message: String
)

@JsonClass(generateAdapter = true)
data class MessageTarget(val id: String, val fullName: String, val error: String? = null)

@JsonClass(generateAdapter = true)
data class SendMessageResponse(
    val totalTargeted: Int,
    val succeeded: List<MessageTarget>,
    val failed: List<MessageTarget>
)
