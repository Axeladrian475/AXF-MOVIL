package com.axf.gymnet.data

data class ChatMensaje(
    val id_mensaje:            Int,
    val enviado_por:           String,   // "personal" o "suscriptor"
    val contenido:             String,
    val leido:                 Int,
    val entregado:             Int       = 0,
    val editado_en:            String?   = null,
    val borrado_para:          String    = "nadie",  // "nadie", "emisor", "todos"
    val id_respuesta:          Int?      = null,
    val respuesta_contenido:   String?   = null,
    val respuesta_enviado_por: String?   = null,
    val enviado_en:            String
)

data class ChatConversacion(
    val id_personal:       Int,
    val nombre_personal:   String,
    val puesto:            String?,
    val foto_url:          String?,
    val ultimo_mensaje:    String?,
    val ultimo_mensaje_en: String?,
    val no_leidos:         Int
)

data class EnviarMensajeRequest(
    val id_personal:           Int,
    val contenido:             String,
    val id_respuesta:          Int?    = null,
    val respuesta_contenido:   String? = null,
    val respuesta_enviado_por: String? = null
)

data class EditarMensajeRequest(
    val id_mensaje:      Int,
    val nuevo_contenido: String
)

data class MensajesResponse(
    val mensajes:   List<ChatMensaje>,
    val paginacion: Paginacion
)

data class Paginacion(
    val total:   Int,
    val limite:  Int,
    val offset:  Int,
    val hay_mas: Boolean
)