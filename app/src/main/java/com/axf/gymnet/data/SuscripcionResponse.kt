package com.axf.gymnet.data

data class SuscripcionResponse(
    val activa: Boolean,
    val vencimiento_final: String?,
    val nombre_plan: String? = null,          // nombre del tipo de suscripción
    val totales: TotalesSesiones? = null
)

data class TotalesSesiones(
    val sesiones_nutriologo: Int,
    val sesiones_entrenador: Int
)
