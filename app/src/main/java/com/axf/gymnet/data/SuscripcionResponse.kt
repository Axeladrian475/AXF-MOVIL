package com.axf.gymnet.data

// ✅ CORREGIDO: El endpoint /movil/suscripcion devuelve solo { activa, vencimiento_final }.
// El campo "totales" no existe en ese endpoint, se marcó como nullable para evitar crash.
data class SuscripcionResponse(
    val activa: Boolean,
    val vencimiento_final: String?,
    val totales: TotalesSesiones? = null   // nullable — solo lo devuelve /:id/suscripcion-activa
)

data class TotalesSesiones(
    val sesiones_nutriologo: Int,
    val sesiones_entrenador: Int
)