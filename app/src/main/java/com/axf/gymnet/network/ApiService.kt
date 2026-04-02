package com.axf.gymnet.network

import com.axf.gymnet.data.LoginRequest
import com.axf.gymnet.data.LoginResponse
import com.axf.gymnet.data.SuscripcionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("api/suscriptores/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/suscriptores/{id}/suscripcion-activa")
    suspend fun getSuscripcion(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<SuscripcionResponse>
}