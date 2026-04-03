package com.axf.gymnet.network

import com.axf.gymnet.data.GuardarSerieRequest
import com.axf.gymnet.data.LoginRequest
import com.axf.gymnet.data.LoginResponse
import com.axf.gymnet.data.RutinaResponse
import com.axf.gymnet.data.SuscripcionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @POST("api/suscriptores/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/suscriptores/movil/suscripcion")
    suspend fun getSuscripcion(
        @Header("Authorization") token: String
    ): Response<SuscripcionResponse>

    // Obtener todas las rutinas asignadas al suscriptor
    @GET("api/suscriptores/movil/rutinas")
    suspend fun getRutinas(
        @Header("Authorization") token: String
    ): Response<List<RutinaResponse>>

    // Guardar una serie completada durante el entrenamiento
    @POST("api/suscriptores/movil/entrenamiento/serie")
    suspend fun guardarSerie(
        @Header("Authorization") token: String,
        @Body request: GuardarSerieRequest
    ): Response<Unit>
}