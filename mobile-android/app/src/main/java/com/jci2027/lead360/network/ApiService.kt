package com.jci2027.lead360.network

import com.jci2027.lead360.model.TemplatesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String?,
    val token: String?,
    val user: UserDto?
)

data class UserDto(
    val id: String?,
    val name: String?,
    val email: String?,
    val role: String?,
    val chapterName: String?
)

interface ApiService {
    @POST("/api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("/api/templates")
    suspend fun getTemplates(
        @Header("Authorization") token: String
    ): Response<TemplatesResponse>
}
