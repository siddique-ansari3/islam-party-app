package com.islamparty.karyakarta.ui.screens.messaging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamparty.karyakarta.data.model.SendMessageResponse
import com.islamparty.karyakarta.data.repository.MessageRepository
import com.islamparty.karyakarta.util.UiState
import com.islamparty.karyakarta.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MessagingViewModel(
    private val repository: MessageRepository,
    val workerIds: List<String>, // empty means "all workers in scope"
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<SendMessageResponse>>(UiState.Idle)
    val state: StateFlow<UiState<SendMessageResponse>> = _state.asStateFlow()

    fun send(channel: String, message: String) {
        if (message.isBlank()) {
            _state.value = UiState.Error("Message cannot be empty")
            return
        }
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val response = repository.send(
                    workerIds = workerIds.ifEmpty { null },
                    allWorkers = workerIds.isEmpty(),
                    channel = channel,
                    message = message
                )
                _state.value = UiState.Success(response)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.toUserMessage())
            }
        }
    }
}
