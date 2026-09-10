package com.islamparty.karyakarta.ui.screens.workerlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil.compose.AsyncImage
import com.islamparty.karyakarta.data.model.WorkerSummary
import com.islamparty.karyakarta.data.repository.WorkerRepository
import com.islamparty.karyakarta.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerListScreen(
    repository: WorkerRepository,
    authBaseUrl: String,
    onAddWorker: () -> Unit,
    onOpenWorker: (String) -> Unit,
    onSendMessage: (List<String>) -> Unit
) {
    val viewModel: WorkerListViewModel = viewModel(
        factory = viewModelFactory { initializer { WorkerListViewModel(repository) } }
    )
    val state by viewModel.state.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Party Karyakarta") })
        },
        floatingActionButton = {
            if (selectedIds.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { onSendMessage(selectedIds.toList()) },
                    icon = { Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null) },
                    text = { Text("Message (${selectedIds.size})") }
                )
            } else {
                FloatingActionButton(onClick = onAddWorker) {
                    Icon(Icons.Filled.Add, contentDescription = "Add worker")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onSearchChanged,
                label = { Text("Search by name, mobile or address") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            when (val s = state) {
                is UiState.Loading, UiState.Idle -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                is UiState.Error -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { Text(s.message, color = MaterialTheme.colorScheme.error) }

                is UiState.Success -> WorkerList(
                    workers = s.data,
                    selectedIds = selectedIds,
                    baseUrl = authBaseUrl,
                    onClick = { onOpenWorker(it.id) },
                    onLongClick = { viewModel.toggleSelection(it.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WorkerList(
    workers: List<WorkerSummary>,
    selectedIds: Set<String>,
    baseUrl: String,
    onClick: (WorkerSummary) -> Unit,
    onLongClick: (WorkerSummary) -> Unit
) {
    if (workers.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No party workers found")
        }
        return
    }
    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
        items(workers, key = { it.id }) { worker ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .combinedClickable(
                        onClick = { onClick(worker) },
                        onLongClick = { onLongClick(worker) }
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = selectedIds.contains(worker.id), onCheckedChange = { onLongClick(worker) })

                    if (worker.photoUrl != null) {
                        AsyncImage(
                            model = baseUrl.trimEnd('/') + worker.photoUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(48.dp))
                    }

                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(worker.fullName, style = MaterialTheme.typography.titleMedium)
                        Text(worker.mobileNumber, style = MaterialTheme.typography.bodyMedium)
                        Text("${worker.city} · ${worker.maskedAadhaar}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
