package com.axf.gymnet.network

import com.axf.gymnet.data.ChatConversacion
import com.axf.gymnet.data.ChatMensaje
import com.axf.gymnet.data.EnviarMensajeRequest
import com.axf.gymnet.data.GuardarSerieRequest
import com.axf.gymnet.data.LoginRequest
import com.axf.gymnet.data.LoginResponse
import com.axf.gymnet.data.MensajesResponse
import com.axf.gymnet.data.RutinaResponse
import com.axf.gymnet.data.SuscripcionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("api/suscriptores/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/suscriptores/movil/suscripcion")
    suspend fun getSuscripcion(
        @Header("Authorization") token: String
    ): Response<SuscripcionResponse>

    @GET("api/suscriptores/movil/rutinas")
    suspend fun getRutinas(
        @Header("Authorization") token: String
    ): Response<List<RutinaResponse>>

    @POST("api/suscriptores/movil/entrenamiento/serie")
    suspend fun guardarSerie(
        @Header("Authorization") token: String,
        @Body request: GuardarSerieRequest
    ): Response<Unit>

    // ── CHAT ──────────────────────────────────────────────────────────────────
    // CORRECCIÓN: se agrega el prefijo "api/" que faltaba en las 3 rutas de chat.
    // Sin él, la app llamaba a /chat/... en vez de /api/chat/... → error 404.

    @GET("api/chat/conversaciones")
    suspend fun getConversaciones(
        @Header("Authorization") token: String
    ): Response<List<ChatConversacion>>

    @GET("api/chat/mensajes/personal/{id_personal}")
    suspend fun getMensajes(
        @Header("Authorization") token: String,
        @Path("id_personal")     idPersonal: Int,
        @Query("limite")         limite: Int = 50,
        @Query("offset")         offset: Int = 0
    ): Response<MensajesResponse>

    @POST("api/chat/mensajes")
    suspend fun enviarMensaje(
        @Header("Authorization") token: String,
        @Body request: EnviarMensajeRequest
    ): Response<Any>
}
