package com.islamparty.karyakarta.util

object Validators {
    private val MOBILE_REGEX = Regex("^[6-9]\\d{9}$")
    private val AADHAAR_REGEX = Regex("^\\d{12}$")

    fun isValidMobile(value: String) = MOBILE_REGEX.matches(value.trim())
    fun isValidAadhaar(value: String) = AADHAAR_REGEX.matches(value.trim())
    fun isNotBlank(value: String) = value.trim().isNotEmpty()
}
