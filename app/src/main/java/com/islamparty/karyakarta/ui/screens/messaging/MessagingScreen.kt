package com.islamparty.karyakarta.ui.screens.messaging

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.islamparty.karyakarta.data.repository.MessageRepository
import com.islamparty.karyakarta.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingScreen(
    repository: MessageRepository,
    workerIds: List<String>,
    onBack: () -> Unit
) {
    val viewModel: MessagingViewModel = viewModel(
        factory = viewModelFactory { initializer { MessagingViewModel(repository, workerIds) } }
    )
    val state by viewModel.state.collectAsState()
    var message by remember { mutableStateOf("") }
    var channel by remember { mutableStateOf("sms") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Send Meeting Notice") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text(
                if (workerIds.isEmpty()) "Sending to all workers in your scope"
                else "Sending to ${workerIds.size} selected worker(s)",
                style = MaterialTheme.typography.bodyMedium
            )

            Text("Channel", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            listOf("sms" to "SMS", "whatsapp" to "WhatsApp", "both" to "Both").forEach { (value, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(selected = channel == value, onClick = { channel = value })
                ) {
                    RadioButton(selected = channel == value, onClick = { channel = value })
                    Text(label)
                }
            }

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Message") },
                minLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            if (state is UiState.Error) {
                Text(
                    (state as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (state is UiState.Success) {
                val result = (state as UiState.Success).data
                Text(
                    "Sent to ${result.succeeded.size}/${result.totalTargeted}. Failed: ${result.failed.size}",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Button(
                onClick = { viewModel.send(channel, message) },
                enabled = state !is UiState.Loading,
                modifier = Modifier.padding(top = 20.dp)
            ) {
                if (state is UiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.padding(2.dp), strokeWidth = 2.dp)
                } else {
                    Text("Send")
                }
            }
        }
    }
}
