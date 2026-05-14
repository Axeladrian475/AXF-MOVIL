package com.axf.gymnet.data

// ─── Respuesta del endpoint GET /api/movil/entrenamiento/historial/:id ───────

data class HistorialEjercicioResponse(
    val ejercicio:      EjercicioInfo,
    val pr_peso_kg:     Double,
    val pr_volumen:     Double,
    val total_sesiones: Int,
    val sesiones:       List<SesionHistorial>
)

data class EjercicioInfo(
    val nombre_ejercicio: String,
    val grupo_muscular:   String?,
    val series_objetivo:  Int,
    val reps_objetivo:    Int,
    val peso_objetivo:    Double?
)

data class SesionHistorial(
    val fecha:   String,          // "2026-05-14"
    val series:  List<SerieHistorial>
)

data class SerieHistorial(
    val num_serie:       Int,
    val peso_levantado:  Double?,
    val reps_realizadas: Int?
)
