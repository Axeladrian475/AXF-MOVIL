package com.axf.gymnet.network

import com.axf.gymnet.data.LoginRequest
import com.axf.gymnet.data.LoginResponse
import com.axf.gymnet.data.SuscripcionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @POST("api/suscriptores/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // ✅ CORREGIDO: Antes usaba /{id}/suscripcion-activa con token de personal.
    // Ahora usa el endpoint móvil correcto que acepta tokens de suscriptor.
    @GET("api/suscriptores/movil/suscripcion")
    suspend fun getSuscripcion(
        @Header("Authorization") token: String
    ): Response<SuscripcionResponse>
}