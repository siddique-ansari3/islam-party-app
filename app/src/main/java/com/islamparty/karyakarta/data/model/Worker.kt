package com.islamparty.karyakarta.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WorkerSummary(
    val id: String,
    val fullName: String,
    val mobileNumber: String,
    val maskedAadhaar: String,
    val city: String,
    val designation: String?,
    val photoUrl: String?
)

@JsonClass(generateAdapter = true)
data class WorkerDetail(
    val id: String,
    val fullName: String,
    val mobileNumber: String,
    val aadhaarNumber: String,
    val address: String?,
    val city: String,
    val dateOfBirth: String?,
    val gender: String?,
    val bloodGroup: String?,
    val designation: String?,
    val email: String?,
    val notes: String?,
    val photoUrl: String?,
    val aadhaarPhotoUrl: String?,
    val createdAt: String?,
    val updatedAt: String?
)

@JsonClass(generateAdapter = true)
data class Pagination(val page: Int, val limit: Int, val total: Int, val totalPages: Int)

@JsonClass(generateAdapter = true)
data class WorkerListResponse(val data: List<WorkerSummary>, val pagination: Pagination)
