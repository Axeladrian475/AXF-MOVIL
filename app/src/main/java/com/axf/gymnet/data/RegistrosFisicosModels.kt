package com.axf.gymnet.data

import com.google.gson.annotations.SerializedName

data class RegistroFisico(
    @SerializedName("id_registro") val idRegistro: Int,
    @SerializedName("peso_kg") val pesoKg: Double?,
    @SerializedName("altura_cm") val alturaCm: Double?,
    @SerializedName("edad") val edad: Int?,
    @SerializedName("pct_grasa") val pctGrasa: Double?,
    @SerializedName("pct_musculo") val pctMusculo: Double?,
    @SerializedName("actividad") val actividad: String?,
    @SerializedName("objetivo") val objetivo: String?,
    @SerializedName("notas") val notas: String?,
    @SerializedName("tmb") val tmb: Double?,
    @SerializedName("tdee") val tdee: Double?,
    @SerializedName("proteinas_min") val proteinasMin: Double?,
    @SerializedName("proteinas_max") val proteinasMax: Double?,
    @SerializedName("grasas_min") val grasasMin: Double?,
    @SerializedName("grasas_max") val grasasMax: Double?,
    @SerializedName("carbs_min") val carbsMin: Double?,
    @SerializedName("carbs_max") val carbsMax: Double?,
    @SerializedName("creado_en") val creadoEn: String,
    @SerializedName("nutriologo") val nutriologo: String?
)
