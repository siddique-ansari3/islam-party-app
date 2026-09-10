package com.islamparty.karyakarta.util

sealed class UiState<out T> {
    data object Idle : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

fun Throwable.toUserMessage(): String = when (this) {
    is java.net.UnknownHostException, is java.net.ConnectException ->
        "Cannot reach the server. Check your internet connection or server address."
    is retrofit2.HttpException -> when (code()) {
        401 -> "Session expired. Please log in again."
        403 -> "You don't have permission to do this."
        404 -> "Not found."
        409 -> "This mobile number or Aadhaar is already registered."
        else -> "Server error (${code()}). Please try again."
    }
    else -> message ?: "Something went wrong. Please try again."
}
