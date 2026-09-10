package com.islamparty.karyakarta.data.remote

import com.islamparty.karyakarta.data.model.LoginRequest
import com.islamparty.karyakarta.data.model.LoginResponse
import com.islamparty.karyakarta.data.model.SendMessageRequest
import com.islamparty.karyakarta.data.model.SendMessageResponse
import com.islamparty.karyakarta.data.model.WorkerDetail
import com.islamparty.karyakarta.data.model.WorkerListResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("api/workers")
    suspend fun listWorkers(
        @Query("search") search: String? = null,
        @Query("city") city: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): WorkerListResponse

    @GET("api/workers/{id}")
    suspend fun getWorker(@Path("id") id: String): WorkerDetail

    @Multipart
    @POST("api/workers")
    suspend fun createWorker(
        @Part parts: List<MultipartBody.Part>
    ): WorkerDetail

    @Multipart
    @PUT("api/workers/{id}")
    suspend fun updateWorker(
        @Path("id") id: String,
        @Part parts: List<MultipartBody.Part>
    ): WorkerDetail

    @DELETE("api/workers/{id}")
    suspend fun deleteWorker(@Path("id") id: String): Response<Unit>

    @POST("api/messages/send")
    suspend fun sendMessage(@Body request: SendMessageRequest): SendMessageResponse
}
