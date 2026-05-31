package com.axf.gymnet.data

data class SuscripcionResponse(
    val activa: Boolean,
    val vencimiento_final: String?,
    val nombre_plan: String? = null,
    val totales: TotalesSesiones? = null,
    val sesiones_nutriologo_restantes: Int? = null,
    val sesiones_entrenador_restantes: Int? = null,
    val racha_dias: Int = 0,
    val dias_descanso_semana: Int = 0,
    val dias_restantes: Int = 0,
    val puntos: Int = 0,
    val asistencias_semana: Int = 0,
    val faltas_restantes: Int = 0,
    val faltas_permitidas_semana: Int = 0,
    val faltas_usadas: Int = 0,
    val dias_obligatorios: Int = 7,
    val visitas_pendientes: Int = 0,
    val dias_restantes_semana: Int = 0,
    val dias_hasta_reset: Int = 0,
    val proximo_reset: String? = null
) {
    val nutriologoCount: Int
        get() = totales?.sesiones_nutriologo ?: sesiones_nutriologo_restantes ?: 0

    val entrenadorCount: Int
        get() = totales?.sesiones_entrenador ?: sesiones_entrenador_restantes ?: 0
}

data class TotalesSesiones(
    val sesiones_nutriologo: Int,
    val sesiones_entrenador: Int
)
