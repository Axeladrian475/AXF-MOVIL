package com.axf.gymnet.data

data class ConsumoDiario(
    val id_consumo: Int,
    val id_suscriptor: Int,
    val fecha: String,
    val id_dieta_comida: Int?,
    val descripcion: String,
    val calorias: Double?,
    val proteinas: Double?,
    val grasas: Double?,
    val carbohidratos: Double?,
    val creado_en: String,
    val comida_planificada_desc: String?,
    val orden_comida: Int?
)

data class AgregarConsumoRequest(
    val fecha: String,
    val id_dieta_comida: Int?,
    val descripcion: String,
    val calorias: Double?,
    val proteinas: Double? = null,
    val grasas: Double? = null,
    val carbohidratos: Double? = null
)

data class AgregarConsumoResponse(
    val message: String,
    val id_consumo: Int
)

data class GenericResponse(
    val message: String
)
