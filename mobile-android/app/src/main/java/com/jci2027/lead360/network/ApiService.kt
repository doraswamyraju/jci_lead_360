package com.jci2027.lead360.network

import com.jci2027.lead360.model.TemplatesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface ApiService {
    @GET("/api/templates")
    suspend fun getTemplates(
        @Header("Authorization") token: String
    ): Response<TemplatesResponse>
}
