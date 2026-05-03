package com.axf.gymnet.network

import com.axf.gymnet.data.AtencionPreviaResponse
import com.axf.gymnet.data.ChatConversacion
import com.axf.gymnet.data.ChatMensaje
import com.axf.gymnet.data.CrearReporteRequest
import com.axf.gymnet.data.CrearReporteResponse
import com.axf.gymnet.data.DietaDetalle
import com.axf.gymnet.data.DietaResumen
import com.axf.gymnet.data.EnviarMensajeRequest
import com.axf.gymnet.data.FcmTokenRequest
import com.axf.gymnet.data.GuardarSerieRequest
import com.axf.gymnet.data.LoginRequest
import com.axf.gymnet.data.LoginResponse
import com.axf.gymnet.data.MensajesResponse
import com.axf.gymnet.data.MisReportesResponse
import com.axf.gymnet.data.NoLeidosResponse
import com.axf.gymnet.data.PersonalItem
import com.axf.gymnet.data.ReportesPublicosResponse
import com.axf.gymnet.data.RutinaResponse
import com.axf.gymnet.data.SucursalItem
import com.axf.gymnet.data.SumarseReporteResponse
import com.axf.gymnet.data.SuscripcionResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
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

    @POST("api/chat/leer/personal/{id_personal}")
    suspend fun marcarComoLeido(
        @Header("Authorization") token: String,
        @Path("id_personal") idPersonal: Int
    ): Response<Unit>

    @GET("api/chat/no-leidos")
    suspend fun getNoLeidos(
        @Header("Authorization") token: String
    ): Response<NoLeidosResponse>

    // ── FCM ───────────────────────────────────────────────────────────────────
    @POST("api/chat/fcm-token")
    suspend fun registrarFcmToken(
        @Header("Authorization") token: String,
        @Body request: FcmTokenRequest
    ): Response<Unit>

    // ── DIETAS ────────────────────────────────────────────────────────────────
    @GET("api/movil/nutricion/dietas")
    suspend fun getDietas(
        @Header("Authorization") token: String
    ): Response<List<DietaResumen>>

    @GET("api/movil/nutricion/dietas/{id}")
    suspend fun getDietaDetalle(
        @Header("Authorization") token: String,
        @Path("id") idDieta: Int
    ): Response<DietaDetalle>

    // ── REPORTES ──────────────────────────────────────────────────────────────

    /** Lista de todas las sucursales activas (para el selector al reportar) */
    @GET("api/reportes/sucursales")
    suspend fun getSucursales(
        @Header("Authorization") token: String
    ): Response<List<SucursalItem>>

    /** Personal de una sucursal (para Reporte de Personal) */
    @GET("api/reportes/personal/{id_sucursal}")
    suspend fun getPersonalSucursal(
        @Header("Authorization") token: String,
        @Path("id_sucursal") idSucursal: Int
    ): Response<List<PersonalItem>>

    /**
     * Verifica si el suscriptor recibió atención previa de ese personal.
     * Usado al seleccionar "Reporte de Personal".
     */
    @GET("api/reportes/atencion-previa/{id_personal}")
    suspend fun verificarAtencionPrevia(
        @Header("Authorization") token: String,
        @Path("id_personal") idPersonal: Int
    ): Response<AtencionPreviaResponse>

    /** Crear un nuevo reporte (incidencia o de personal) con soporte para foto */
    @Multipart
    @POST("api/reportes/crear")
    suspend fun crearReporteMultipart(
        @Header("Authorization") token: String,
        @Part("id_sucursal") id_sucursal: RequestBody,
        @Part("categoria") categoria: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("es_privado") es_privado: RequestBody,
        @Part("id_personal_reportado") id_personal_reportado: RequestBody?,
        @Part("sobre_atencion_previa") sobre_atencion_previa: RequestBody?,
        @Part foto: MultipartBody.Part?
    ): Response<CrearReporteResponse>

    /** Crear un nuevo reporte (incidencia o de personal) - Legacy JSON */
    @POST("api/reportes/crear")
    suspend fun crearReporte(
        @Header("Authorization") token: String,
        @Body request: CrearReporteRequest
    ): Response<CrearReporteResponse>

    /** Reportes públicos activos de una sucursal (para que otros se sumen) */
    @GET("api/reportes/publicos/{id_sucursal}")
    suspend fun getReportesPublicos(
        @Header("Authorization") token: String,
        @Path("id_sucursal") idSucursal: Int
    ): Response<ReportesPublicosResponse>

    /** Sumarse a un reporte existente */
    @POST("api/reportes/sumar/{id_reporte}")
    suspend fun sumarseReporte(
        @Header("Authorization") token: String,
        @Path("id_reporte") idReporte: Int
    ): Response<SumarseReporteResponse>

    /** Mis propios reportes (historial del suscriptor) */
    @GET("api/reportes/mis-reportes")
    suspend fun getMisReportes(
        @Header("Authorization") token: String
    ): Response<MisReportesResponse>
}
