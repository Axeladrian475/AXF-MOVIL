package com.axf.gymnet.data

data class SuscripcionResponse(
    val activa: Boolean,
    val vencimiento_final: String?,
    val totales: TotalesSesiones?
)

data class TotalesSesiones(
    val sesiones_nutriologo: Int,
    val sesiones_entrenador: Int
)