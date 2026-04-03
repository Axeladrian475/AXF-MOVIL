package com.axf.gymnet.data

// ─── Respuesta del endpoint GET /api/suscriptores/movil/rutinas ─────────────

data class RutinaResponse(
    val id_rutina: Int,
    val notas_pdf: String?,
    val creado_en: String,
    val entrenador: String,
    val ejercicios: List<EjercicioRutina>
)

data class EjercicioRutina(
    val id_rutina_ejercicio: Int,   // necesario para guardar registro
    val orden: Int,
    val nombre: String,
    val imagen_url: String?,
    val series: Int,
    val repeticiones: Int,
    val descanso_seg: Int?,         // null = sin descanso
    val peso_kg: Double?,
    val descripcion_tecnica: String?
)

// ─── Request para guardar series completadas ─────────────────────────────────

data class GuardarSerieRequest(
    val id_rutina_ejercicio: Int,
    val num_serie: Int,
    val peso_levantado: Double?,
    val reps_realizadas: Int?
)