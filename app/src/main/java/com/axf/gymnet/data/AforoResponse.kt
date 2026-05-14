package com.axf.gymnet.data

data class AforoResponse(
    val personas_dentro:  Int,
    val capacidad_maxima: Int,
    val nombre_sucursal:  String,
    val actualizado_en:   String?,
    val porcentaje:       Int,
)
