package com.axf.gymnet.data

data class SuscripcionResponse(
    val activa: Boolean,
    val vencimiento_final: String?,
    val nombre_plan: String? = null,
    val totales: TotalesSesiones? = null,
    val racha_dias: Int = 0,
    val dias_descanso_semana: Int = 0
)

data class TotalesSesiones(
    val sesiones_nutriologo: Int,
    val sesiones_entrenador: Int
)
