package com.axf.gymnet

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.axf.gymnet.network.RetrofitClient
import com.axf.gymnet.data.FcmTokenRequest

/**
 * Servicio de Firebase Cloud Messaging.
 *
 * Maneja mensajes DATA-ONLY (solo "data" payload) → Siempre llegan a
 * onMessageReceived, incluso con la app cerrada.
 *
 * CORRECCIÓN para tiempo real:
 *  - Si ChatSocketService está activo y conectado, FCM se usa como backup.
 *  - Si ChatActivity está mostrando el chat del remitente, NO mostrar notificación
 *    (el mensaje ya apareció vía socket).
 *  - Si la app tiene el socket activo, refrescar la conexión al recibir FCM
 *    (indica que el servidor tiene mensajes para nosotros).
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    // ── Canales de notificación ──────────────────────────────────────────────
    companion object {
        const val CHANNEL_CHAT     = "chat_mensajes"
        const val CHANNEL_GENERAL  = "general"

        /**
         * Crea los canales de notificación. Llamar desde Application.onCreate()
         * o desde cualquier Activity antes de mostrar notificaciones.
         */
        fun crearCanales(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

            // Canal de mensajes de chat (alta prioridad → sonido + vibración)
            val canalChat = NotificationChannel(
                CHANNEL_CHAT,
                "Mensajes del chat",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de nuevos mensajes de tu entrenador"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }

            // Canal general (avisos, recordatorios)
            val canalGeneral = NotificationChannel(
                CHANNEL_GENERAL,
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Avisos y recordatorios de AXF GymNet"
            }

            manager.createNotificationChannel(canalChat)
            manager.createNotificationChannel(canalGeneral)
        }
    }

    // ── Nuevo token FCM ─────────────────────────────────────────────────────
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Guardar localmente
        getSharedPreferences("axf_prefs", Context.MODE_PRIVATE)
            .edit().putString("fcm_token", token).apply()

        // Enviar al backend si ya hay sesión activa
        val authToken = getSharedPreferences("axf_prefs", Context.MODE_PRIVATE)
            .getString("token", null) ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitClient.instance.registrarFcmToken(
                    "Bearer $authToken",
                    FcmTokenRequest(token)
                )
            } catch (_: Exception) {}
        }
    }

    // ── Mensaje recibido ─────────────────────────────────────────────────────
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Asegurarnos de que los canales existan
        crearCanales(this)

        val data           = message.data
        Log.d("FCM_SERVICE", "Mensaje recibido: $data")
        val tipo           = data["tipo"] ?: "general"
        val titulo         = data["titulo"] ?: data["nombre_personal"] ?: "AXF GymNet"
        val cuerpo         = data["cuerpo"] ?: ""
        val idPersonalStr  = data["id_personal"]
        val nombrePersonal = data["nombre_personal"] ?: titulo

        // Guardar nombre en caché para notificaciones locales del ChatSocketService
        idPersonalStr?.toIntOrNull()?.let { idP ->
            if (nombrePersonal.isNotBlank()) {
                getSharedPreferences("axf_personal_names", MODE_PRIVATE)
                    .edit().putString("personal_$idP", nombrePersonal).apply()
            }
        }

        when (tipo) {
            "chat" -> {
                val idPersonal = idPersonalStr?.toIntOrNull() ?: return

                // ── PREVENCIÓN DE DUPLICADOS ────────────────────────────
                // Si ChatActivity está mostrando exactamente este chat,
                // el mensaje ya llegó por socket → NO mostrar notificación
                if (ChatSocketService.activeChatPersonalId == idPersonal) {
                    Log.d("FCM_SERVICE", "Chat activo para personal $idPersonal, suprimiendo notificación")
                    return
                }

                // Si el socket está conectado, el ChatSocketService ya manejó
                // el mensaje y mostró la notificación → evitar duplicado
                if (ChatSocketService.isConnected) {
                    Log.d("FCM_SERVICE", "Socket activo, suprimiendo notificación FCM (ya notificado por socket)")
                    return
                }

                // Socket desconectado → mostrar notificación FCM
                mostrarNotificacionChat(
                    titulo         = nombrePersonal,
                    cuerpo         = cuerpo,
                    idPersonal     = idPersonal,
                    nombrePersonal = nombrePersonal
                )
            }
            else -> mostrarNotificacionGeneral(titulo, cuerpo)
        }
    }

    // ── Mensajes eliminados en el servidor ───────────────────────────────────
    override fun onDeletedMessages() {
        super.onDeletedMessages()
        getSharedPreferences("axf_prefs", MODE_PRIVATE)
            .edit().putBoolean("pendientes_sinc", true).apply()
        mostrarNotificacionGeneral(
            titulo = "AXF GymNet",
            cuerpo = "Tienes mensajes nuevos esperando"
        )
    }

    // ── Notificación de chat ─────────────────────────────────────────────────
    private fun mostrarNotificacionChat(
        titulo:         String,
        cuerpo:         String,
        idPersonal:     Int,
        nombrePersonal: String
    ) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val abrirChat = Intent(this, ChatActivity::class.java).apply {
            putExtra("id_personal",      idPersonal)
            putExtra("nombre_personal",  nombrePersonal)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            idPersonal,
            abrirChat,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(this, CHANNEL_CHAT)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setStyle(NotificationCompat.BigTextStyle().bigText(cuerpo))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setNumber(1)
            .build()

        manager.notify(CHANNEL_CHAT.hashCode() + idPersonal, notif)
    }

    // ── Notificación general ─────────────────────────────────────────────────
    private fun mostrarNotificacionGeneral(titulo: String, cuerpo: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val abrirApp = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, abrirApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(this, CHANNEL_GENERAL)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notif)
    }
}