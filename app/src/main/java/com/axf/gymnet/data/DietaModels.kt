package com.axf.gymnet.data

// ─── Lista de dietas (GET /api/movil/nutricion/dietas) ───────────────────────

data class DietaResumen(
    val id_dieta: Int,
    val creado_en: String,
    val nutriologo: String,
    val total_comidas: Int
)

// ─── Detalle de una dieta (GET /api/movil/nutricion/dietas/:id) ───────────────

data class DietaDetalle(
    val id_dieta: Int,
    val creado_en: String,
    val nutriologo: String,
    val dias: List<DietaDia>
    // NOTA: Se eliminó el campo `comidas` flat porque Retrofit lanzaba
    // un error de parseo al recibir la respuesta. Los datos ya vienen
    // correctamente agrupados dentro de cada DietaDia.
)

data class DietaDia(
    val dia: String,          // "Lunes", "Martes", etc.
    val comidas: List<DietaComida>
)

data class DietaComida(
    val id_comida: Int,
    val dia: Int,
    val orden_comida: Int,
    val descripcion: String?,
    val calorias: Double?,
    val notas: String?,
    // campos de receta (pueden ser null si no hay receta asociada)
    val id_receta: Int?,
    val receta_nombre: String?,
    val receta_imagen: String?,
    val proteinas_g: Double?,
    val grasas_g: Double?,
    val ingredientes: List<IngredienteInfo>?
)

data class IngredienteInfo(
    val nombre: String,
    val cantidad: Double,
    val unidad_medicion: String
)