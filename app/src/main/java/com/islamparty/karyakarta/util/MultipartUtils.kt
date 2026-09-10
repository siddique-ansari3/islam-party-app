package com.islamparty.karyakarta.util

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/** Small helpers for building multipart/form-data requests from plain fields and image Uris. */
object MultipartUtils {

    fun textPart(name: String, value: String?): MultipartBody.Part? {
        if (value.isNullOrBlank()) return null
        return MultipartBody.Part.createFormData(name, value)
    }

    /** Copies the content behind [uri] into the app cache dir, then wraps it as a multipart file part. */
    fun filePart(context: Context, fieldName: String, uri: Uri?): MultipartBody.Part? {
        if (uri == null) return null
        val cacheFile = File(context.cacheDir, "upload_${fieldName}_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            cacheFile.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        val requestBody = cacheFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(fieldName, cacheFile.name, requestBody)
    }
}
