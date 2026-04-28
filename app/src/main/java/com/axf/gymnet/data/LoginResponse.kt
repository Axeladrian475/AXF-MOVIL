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
    // ✅ CORREGIDO: MySQL puede devolver la fecha como ISO 8601 ("2025-12-31T06:00:00.000Z")
    // Se mantiene como String nullable y se normaliza en MainActivity antes de parsear.
    val fechaVencimiento: String?
)