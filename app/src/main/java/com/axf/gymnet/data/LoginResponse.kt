package com.axf.gymnet.data
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String?,
    val suscriptor: SuscriptorData?
)

data class SuscriptorData(
    val id: Int,
    val nombres: String,
    val apellidoPaterno: String,
    val correo: String,
    val sucursalId: Int,
    val suscripcionActiva: Boolean,
    val fechaVencimiento: String?
)