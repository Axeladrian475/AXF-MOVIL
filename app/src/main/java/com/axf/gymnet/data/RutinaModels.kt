package com.axf.gymnet.data

// ─── Respuesta del endpoint GET /api/suscriptores/movil/rutinas ─────────────

data class RutinaResponse(
    val id_rutina: Int = 0,
    val nombre: String? = null,
    val notas_pdf: String? = null,
    val creado_en: String = "",
    val entrenador: String = "",
    val ejercicios: List<EjercicioRutina>? = null,   // nullable — Gson puede mandarlo null
    val bloques: List<BloqueRutina>? = null           // nullable — puede no venir del backend
)

data class BloqueRutina(
    val bloque_idx: Int = 0,
    val nombre: String? = null
)

data class EjercicioRutina(
    val id_rutina_ejercicio: Int = 0,
    val orden: Int = 0,
    val nombre: String = "",
    val imagen_url: String? = null,
    val series: Int = 0,
    val repeticiones: Int = 0,
    val descanso_seg: Int? = null,
    val peso_kg: Double? = null,
    val descripcion_tecnica: String? = null,
    val grupo_muscular: String? = null,
    val bloque_idx: Int? = null    // nullable — si no viene, se calcula desde orden
) {
    // Bloque calculado: si el backend no lo manda, se infiere de FLOOR(orden/100)
    fun getBloqueIdx(): Int = bloque_idx ?: (orden / 100)
}

// ─── Request para guardar series completadas ─────────────────────────────────

data class GuardarSerieRequest(
    val id_rutina_ejercicio: Int,
    val num_serie: Int,
    val peso_levantado: Double?,
    val reps_realizadas: Int?
)