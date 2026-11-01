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

data class SaveContactRequest(
    val id: String,
    val name: String,
    val company: String,
    val designation: String,
    val phone: String,
    val email: String,
    val website: String,
    val address: String,
    val eventTag: String,
    val locationName: String,
    val city: String,
    val notes: String,
    val followUpDate: String
)

data class SaveContactResponse(
    val success: Boolean,
    val message: String?,
    val contact: SaveContactRequest?
)

data class GetContactsResponse(
    val success: Boolean,
    val contacts: List<SaveContactRequest>?
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

    @POST("/api/contacts")
    suspend fun saveContact(
        @Header("Authorization") token: String,
        @Body contact: SaveContactRequest
    ): Response<SaveContactResponse>

    @GET("/api/contacts")
    suspend fun getContacts(
        @Header("Authorization") token: String
    ): Response<GetContactsResponse>
}
