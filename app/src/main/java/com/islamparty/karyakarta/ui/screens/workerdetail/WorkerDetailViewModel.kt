package com.islamparty.karyakarta.ui.screens.workerdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamparty.karyakarta.data.model.WorkerDetail
import com.islamparty.karyakarta.data.repository.WorkerRepository
import com.islamparty.karyakarta.util.UiState
import com.islamparty.karyakarta.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkerDetailViewModel(private val repository: WorkerRepository, private val workerId: String) : ViewModel() {

    private val _state = MutableStateFlow<UiState<WorkerDetail>>(UiState.Idle)
    val state: StateFlow<UiState<WorkerDetail>> = _state.asStateFlow()

    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                _state.value = UiState.Success(repository.getWorker(workerId))
            } catch (e: Exception) {
                _state.value = UiState.Error(e.toUserMessage())
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            try {
                repository.deleteWorker(workerId)
                _deleted.value = true
            } catch (e: Exception) {
                _state.value = UiState.Error(e.toUserMessage())
            }
        }
    }
}
