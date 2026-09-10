package com.islamparty.karyakarta.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(val email: String, val password: String)

@JsonClass(generateAdapter = true)
data class LoginResponse(val token: String, val user: UserInfo)

@JsonClass(generateAdapter = true)
data class UserInfo(
    val id: String,
    val name: String,
    val email: String,
    val role: String, // "super_admin" | "city_admin"
    val city: String?
)

@JsonClass(generateAdapter = true)
data class CreateUserRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String,
    val city: String?
)
