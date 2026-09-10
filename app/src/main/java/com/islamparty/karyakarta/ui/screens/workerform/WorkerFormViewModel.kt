package com.islamparty.karyakarta.ui.screens.workerform

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamparty.karyakarta.data.model.WorkerDetail
import com.islamparty.karyakarta.data.repository.AuthRepository
import com.islamparty.karyakarta.data.repository.WorkerFormData
import com.islamparty.karyakarta.data.repository.WorkerRepository
import com.islamparty.karyakarta.util.UiState
import com.islamparty.karyakarta.util.Validators
import com.islamparty.karyakarta.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WorkerFormFields(
    val fullName: String = "",
    val mobileNumber: String = "",
    val aadhaarNumber: String = "",
    val address: String = "",
    val city: String = "",
    val dateOfBirth: String = "",
    val gender: String = "",
    val bloodGroup: String = "",
    val designation: String = "",
    val email: String = "",
    val notes: String = "",
    val photoUri: Uri? = null,
    val aadhaarPhotoUri: Uri? = null
)

class WorkerFormViewModel(
    private val repository: WorkerRepository,
    private val authRepository: AuthRepository,
    private val editingWorkerId: String?
) : ViewModel() {

    val isEditing = editingWorkerId != null

    private val _fields = MutableStateFlow(WorkerFormFields())
    val fields: StateFlow<WorkerFormFields> = _fields.asStateFlow()

    // city_admins may only create/keep workers in their own city; null means unrestricted (super_admin).
    private val _lockedCity = MutableStateFlow<String?>(null)
    val lockedCity: StateFlow<String?> = _lockedCity.asStateFlow()

    private val _loadState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val loadState: StateFlow<UiState<Unit>> = _loadState.asStateFlow()

    private val _submitState = MutableStateFlow<UiState<WorkerDetail>>(UiState.Idle)
    val submitState: StateFlow<UiState<WorkerDetail>> = _submitState.asStateFlow()

    init {
        viewModelScope.launch {
            val role = authRepository.currentRole()
            val city = authRepository.currentCity()
            if (role == "city_admin" && city != null) {
                _lockedCity.value = city
                if (editingWorkerId == null) {
                    _fields.value = _fields.value.copy(city = city)
                }
            }
            if (editingWorkerId != null) loadExisting(editingWorkerId)
        }
    }

    private suspend fun loadExisting(id: String) {
        _loadState.value = UiState.Loading
        try {
            val w = repository.getWorker(id)
            _fields.value = WorkerFormFields(
                fullName = w.fullName,
                mobileNumber = w.mobileNumber,
                aadhaarNumber = "", // left blank on edit; only sent to server if the admin re-enters it
                address = w.address ?: "",
                city = w.city,
                dateOfBirth = w.dateOfBirth ?: "",
                gender = w.gender ?: "",
                bloodGroup = w.bloodGroup ?: "",
                designation = w.designation ?: "",
                email = w.email ?: "",
                notes = w.notes ?: ""
            )
            _loadState.value = UiState.Success(Unit)
        } catch (e: Exception) {
            _loadState.value = UiState.Error(e.toUserMessage())
        }
    }

    fun update(transform: (WorkerFormFields) -> WorkerFormFields) {
        _fields.value = transform(_fields.value)
    }

    fun submit(context: Context) {
        val f = _fields.value
        if (!Validators.isNotBlank(f.fullName)) {
            _submitState.value = UiState.Error("Full name is required"); return
        }
        if (!Validators.isValidMobile(f.mobileNumber)) {
            _submitState.value = UiState.Error("Enter a valid 10-digit mobile number"); return
        }
        if (!isEditing && !Validators.isValidAadhaar(f.aadhaarNumber)) {
            _submitState.value = UiState.Error("Aadhaar number must be exactly 12 digits"); return
        }
        if (isEditing && f.aadhaarNumber.isNotBlank() && !Validators.isValidAadhaar(f.aadhaarNumber)) {
            _submitState.value = UiState.Error("Aadhaar number must be exactly 12 digits"); return
        }
        if (!Validators.isNotBlank(f.city)) {
            _submitState.value = UiState.Error("City is required"); return
        }

        val form = WorkerFormData(
            fullName = f.fullName.trim(),
            mobileNumber = f.mobileNumber.trim(),
            aadhaarNumber = f.aadhaarNumber.ifBlank { null },
            address = f.address.ifBlank { null },
            city = f.city.trim(),
            dateOfBirth = f.dateOfBirth.ifBlank { null },
            gender = f.gender.ifBlank { null },
            bloodGroup = f.bloodGroup.ifBlank { null },
            designation = f.designation.ifBlank { null },
            email = f.email.ifBlank { null },
            notes = f.notes.ifBlank { null },
            photoUri = f.photoUri,
            aadhaarPhotoUri = f.aadhaarPhotoUri
        )

        viewModelScope.launch {
            _submitState.value = UiState.Loading
            try {
                val result = if (isEditing) {
                    repository.updateWorker(context, editingWorkerId!!, form)
                } else {
                    repository.createWorker(context, form)
                }
                _submitState.value = UiState.Success(result)
            } catch (e: Exception) {
                _submitState.value = UiState.Error(e.toUserMessage())
            }
        }
    }
}
