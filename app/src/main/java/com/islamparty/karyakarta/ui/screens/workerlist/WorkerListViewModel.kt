package com.islamparty.karyakarta.ui.screens.workerlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamparty.karyakarta.data.model.WorkerSummary
import com.islamparty.karyakarta.data.repository.WorkerRepository
import com.islamparty.karyakarta.util.UiState
import com.islamparty.karyakarta.util.toUserMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkerListViewModel(private val repository: WorkerRepository) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<WorkerSummary>>>(UiState.Idle)
    val state: StateFlow<UiState<List<WorkerSummary>>> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadWorkers()
    }

    fun onSearchChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350) // debounce keystrokes before hitting the network
            loadWorkers()
        }
    }

    fun loadWorkers() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val response = repository.listWorkers(search = _searchQuery.value, city = null)
                _state.value = UiState.Success(response.data)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.toUserMessage())
            }
        }
    }

    fun toggleSelection(id: String) {
        _selectedIds.value = _selectedIds.value.toMutableSet().apply {
            if (contains(id)) remove(id) else add(id)
        }
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }
}
