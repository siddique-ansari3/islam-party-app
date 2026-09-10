package com.islamparty.karyakarta.ui.screens.workerform

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil.compose.AsyncImage
import com.islamparty.karyakarta.data.repository.AuthRepository
import com.islamparty.karyakarta.data.repository.WorkerRepository
import com.islamparty.karyakarta.util.UiState
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerFormScreen(
    repository: WorkerRepository,
    authRepository: AuthRepository,
    editingWorkerId: String?,
    onSaved: (String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: WorkerFormViewModel = viewModel(
        factory = viewModelFactory {
            initializer { WorkerFormViewModel(repository, authRepository, editingWorkerId) }
        }
    )
    val fields by viewModel.fields.collectAsState()
    val submitState by viewModel.submitState.collectAsState()
    val lockedCity by viewModel.lockedCity.collectAsState()

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) viewModel.update { it.copy(photoUri = uri) }
    }
    val aadhaarPhotoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) viewModel.update { it.copy(aadhaarPhotoUri = uri) }
    }

    LaunchedEffect(submitState) {
        if (submitState is UiState.Success) {
            onSaved((submitState as UiState.Success).data.id)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (viewModel.isEditing) "Edit Worker" else "Add Worker") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            LabeledField("Full Name *", fields.fullName) { viewModel.update { f -> f.copy(fullName = it) } }
            LabeledField("Mobile Number *", fields.mobileNumber) { viewModel.update { f -> f.copy(mobileNumber = it) } }
            LabeledField(
                if (viewModel.isEditing) "Aadhaar Number (leave blank to keep unchanged)" else "Aadhaar Number *",
                fields.aadhaarNumber
            ) { viewModel.update { f -> f.copy(aadhaarNumber = it) } }
            LabeledField("Address", fields.address) { viewModel.update { f -> f.copy(address = it) } }
            LabeledField("City *", if (lockedCity != null) lockedCity!! else fields.city, enabled = lockedCity == null) {
                viewModel.update { f -> f.copy(city = it) }
            }
            LabeledField("Date of Birth (yyyy-MM-dd)", fields.dateOfBirth) { viewModel.update { f -> f.copy(dateOfBirth = it) } }
            LabeledField("Gender (male/female/other)", fields.gender) { viewModel.update { f -> f.copy(gender = it) } }
            LabeledField("Blood Group", fields.bloodGroup) { viewModel.update { f -> f.copy(bloodGroup = it) } }
            LabeledField("Designation", fields.designation) { viewModel.update { f -> f.copy(designation = it) } }
            LabeledField("Email", fields.email) { viewModel.update { f -> f.copy(email = it) } }
            LabeledField("Notes", fields.notes) { viewModel.update { f -> f.copy(notes = it) } }

            Text("Worker Photo", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            fields.photoUri?.let {
                AsyncImage(model = it, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(120.dp))
            }
            OutlinedButton(onClick = { photoPicker.launch("image/*") }, modifier = Modifier.padding(top = 8.dp)) {
                Text("Choose Photo")
            }

            Text("Aadhaar Card Photo", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            fields.aadhaarPhotoUri?.let {
                AsyncImage(model = it, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(120.dp))
            }
            OutlinedButton(onClick = { aadhaarPhotoPicker.launch("image/*") }, modifier = Modifier.padding(top = 8.dp)) {
                Text("Choose Aadhaar Photo")
            }

            if (submitState is UiState.Error) {
                Text(
                    (submitState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Row(modifier = Modifier.padding(top = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onCancel) { Text("Cancel") }
                Button(onClick = { viewModel.submit(context) }, enabled = submitState !is UiState.Loading) {
                    if (submitState is UiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun LabeledField(label: String, value: String, enabled: Boolean = true, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    )
}
