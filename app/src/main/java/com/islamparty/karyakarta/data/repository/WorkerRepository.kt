package com.islamparty.karyakarta.data.repository

import android.content.Context
import android.net.Uri
import com.islamparty.karyakarta.data.model.WorkerDetail
import com.islamparty.karyakarta.data.model.WorkerListResponse
import com.islamparty.karyakarta.data.remote.ApiService
import com.islamparty.karyakarta.util.MultipartUtils

/** Plain-field form state used when creating or editing a worker. */
data class WorkerFormData(
    val fullName: String,
    val mobileNumber: String,
    val aadhaarNumber: String? = null, // omit on update if unchanged
    val address: String? = null,
    val city: String,
    val dateOfBirth: String? = null, // yyyy-MM-dd
    val gender: String? = null,
    val bloodGroup: String? = null,
    val designation: String? = null,
    val email: String? = null,
    val notes: String? = null,
    val photoUri: Uri? = null,
    val aadhaarPhotoUri: Uri? = null
)

class WorkerRepository(private val api: ApiService) {

    suspend fun listWorkers(search: String?, city: String?, page: Int = 1, limit: Int = 20): WorkerListResponse =
        api.listWorkers(search = search?.ifBlank { null }, city = city, page = page, limit = limit)

    suspend fun getWorker(id: String): WorkerDetail = api.getWorker(id)

    suspend fun createWorker(context: Context, form: WorkerFormData): WorkerDetail {
        val parts = buildParts(context, form)
        return api.createWorker(parts)
    }

    suspend fun updateWorker(context: Context, id: String, form: WorkerFormData): WorkerDetail {
        val parts = buildParts(context, form)
        return api.updateWorker(id, parts)
    }

    suspend fun deleteWorker(id: String) {
        api.deleteWorker(id)
    }

    private fun buildParts(context: Context, form: WorkerFormData) = listOfNotNull(
        MultipartUtils.textPart("fullName", form.fullName),
        MultipartUtils.textPart("mobileNumber", form.mobileNumber),
        MultipartUtils.textPart("aadhaarNumber", form.aadhaarNumber),
        MultipartUtils.textPart("address", form.address),
        MultipartUtils.textPart("city", form.city),
        MultipartUtils.textPart("dateOfBirth", form.dateOfBirth),
        MultipartUtils.textPart("gender", form.gender),
        MultipartUtils.textPart("bloodGroup", form.bloodGroup),
        MultipartUtils.textPart("designation", form.designation),
        MultipartUtils.textPart("email", form.email),
        MultipartUtils.textPart("notes", form.notes),
        MultipartUtils.filePart(context, "photo", form.photoUri),
        MultipartUtils.filePart(context, "aadhaarPhoto", form.aadhaarPhotoUri)
    )
}
