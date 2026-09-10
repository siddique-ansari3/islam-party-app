package com.islamparty.karyakarta.ui.screens.workerdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil.compose.AsyncImage
import com.islamparty.karyakarta.data.model.WorkerDetail
import com.islamparty.karyakarta.data.repository.WorkerRepository
import com.islamparty.karyakarta.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDetailScreen(
    workerId: String,
    repository: WorkerRepository,
    baseUrl: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onGenerateCard: (String) -> Unit
) {
    val viewModel: WorkerDetailViewModel = viewModel(
        factory = viewModelFactory { initializer { WorkerDetailViewModel(repository, workerId) } }
    )
    val state by viewModel.state.collectAsState()
    val deleted by viewModel.deleted.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (deleted) {
        onBack()
        return
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete worker?") },
            text = { Text("This will permanently remove this worker's record and photos.") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; viewModel.delete() }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Worker Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = { onEdit(workerId) }) { Icon(Icons.Filled.Edit, contentDescription = "Edit") }
                    IconButton(onClick = { onGenerateCard(workerId) }) { Icon(Icons.Filled.Badge, contentDescription = "Business card") }
                    IconButton(onClick = { showDeleteConfirm = true }) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                is UiState.Loading, UiState.Idle -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is UiState.Error -> Text(
                    s.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
                is UiState.Success -> WorkerDetailContent(s.data, baseUrl)
            }
        }
    }
}

@Composable
private fun WorkerDetailContent(worker: WorkerDetail, baseUrl: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row {
            if (worker.photoUrl != null) {
                AsyncImage(
                    model = baseUrl.trimEnd('/') + worker.photoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(96.dp).clip(CircleShape)
                )
            }
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(worker.fullName, style = MaterialTheme.typography.titleLarge)
                Text(worker.designation ?: "", style = MaterialTheme.typography.bodyMedium)
            }
        }

        DetailRow("Mobile", worker.mobileNumber)
        DetailRow("Aadhaar Number", worker.aadhaarNumber)
        DetailRow("City", worker.city)
        DetailRow("Address", worker.address ?: "-")
        DetailRow("Date of Birth", worker.dateOfBirth ?: "-")
        DetailRow("Gender", worker.gender ?: "-")
        DetailRow("Blood Group", worker.bloodGroup ?: "-")
        DetailRow("Email", worker.email ?: "-")
        DetailRow("Notes", worker.notes ?: "-")

        if (worker.aadhaarPhotoUrl != null) {
            Text("Aadhaar Photo", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            AsyncImage(
                model = baseUrl.trimEnd('/') + worker.aadhaarPhotoUrl,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.size(width = 140.dp, height = 24.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
