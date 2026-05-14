package com.axf.gymnet.data

// ─── Respuesta del endpoint GET /api/suscriptores/movil/rutinas ─────────────

data class RutinaResponse(
    val id_rutina: Int,
    val nombre: String?,            // Nombre general de la rutina (puede ser null)
    val notas_pdf: String?,
    val creado_en: String,
    val entrenador: String,
    val ejercicios: List<EjercicioRutina>,
    val bloques: List<BloqueRutina>  // Lista de bloques ordenados (Pecho, Espalda, Pierna...)
)

data class BloqueRutina(
    val bloque_idx: Int,            // 0 = primer bloque, 1 = segundo, etc.
    val nombre: String?             // Nombre del bloque ej: "Pecho", "Espalda". null si no tiene nombre
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
    val descripcion_tecnica: String?,
    val grupo_muscular: String?,    // nombre del bloque al que pertenece (ej: "Pecho")
    val bloque_idx: Int             // índice del bloque: 0, 1, 2...
)

// ─── Request para guardar series completadas ─────────────────────────────────

data class GuardarSerieRequest(
    val id_rutina_ejercicio: Int,
    val num_serie: Int,
    val peso_levantado: Double?,
    val reps_realizadas: Int?
)