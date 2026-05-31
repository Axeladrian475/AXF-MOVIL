package com.axf.gymnet.data

import com.google.gson.annotations.SerializedName

data class CatalogoTiendaResponse(
    val suscripciones: List<PlanTienda>,
    val promociones: List<PromocionTienda>,
    @SerializedName("tiene_suscripcion_activa") val tieneSuscripcionActiva: Boolean = false,
    @SerializedName("vencimiento_final") val vencimientoFinal: String? = null
)

data class PlanTienda(
    @SerializedName("id_tipo") val idTipo: Int,
    val nombre: String,
    @SerializedName("duracion_dias") val duracionDias: Int,
    val precio: Double,
    @SerializedName("limite_sesiones_nutriologo") val sesionesNutriologo: Int = 0,
    @SerializedName("limite_sesiones_entrenador") val sesionesEntrenador: Int = 0
)

data class PromocionTienda(
    @SerializedName("id_promocion") val idPromocion: Int,
    val nombre: String,
    val descripcion: String? = null,
    @SerializedName("duracion_dias") val duracionDias: Int = 0,
    val precio: Double,
    @SerializedName("sesiones_nutriologo") val sesionesNutriologo: Int = 0,
    @SerializedName("sesiones_entrenador") val sesionesEntrenador: Int = 0
)

data class PayPalConfigResponse(
    @SerializedName("client_id") val clientId: String,
    val currency: String = "MXN"
)

data class CrearOrdenTiendaRequest(
    @SerializedName("id_tipo") val idTipo: Int? = null,
    @SerializedName("id_promocion") val idPromocion: Int? = null
)

data class CrearOrdenTiendaResponse(
    @SerializedName("order_id") val orderId: String,
    @SerializedName("plan_nombre") val planNombre: String? = null,
    val precio: Double? = null
)

data class CapturarOrdenTiendaRequest(
    @SerializedName("order_id") val orderId: String,
    @SerializedName("id_tipo") val idTipo: Int? = null,
    @SerializedName("id_promocion") val idPromocion: Int? = null
)

data class CapturarOrdenTiendaResponse(
    val ok: Boolean,
    val message: String? = null,
    val suscripcion: SuscripcionComprada? = null
)

data class SuscripcionComprada(
    @SerializedName("id_suscripcion") val idSuscripcion: Int? = null,
    @SerializedName("fecha_inicio") val fechaInicio: String? = null,
    @SerializedName("fecha_fin") val fechaFin: String? = null,
    val estado: String? = null,
    @SerializedName("plan_nombre") val planNombre: String? = null,
    @SerializedName("solo_sesiones") val soloSesiones: Boolean? = null
)
