package com.axf.gymnet

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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
 * Maneja dos tipos de mensajes FCM:
 *  1. Mensajes con "notification" payload  → Android los muestra automáticamente
 *     cuando la app está en background/cerrada. Aquí los interceptamos para
 *     añadir extras (deep link a ChatActivity).
 *  2. Mensajes DATA-ONLY (solo "data" payload) → Siempre llegan a onMessageReceived,
 *     incluso con la app cerrada. El backend envía este tipo para máximo control.
 *
 * El backend (socket.js) ya envía ambos en el mismo mensaje FCM ("notification" + "data"),
 * lo que garantiza entrega en todos los estados de la app.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    // ─── Canales de notificación ──────────────────────────────────────────────
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

    // ─── Nuevo token FCM ─────────────────────────────────────────────────────
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

    // ─── Mensaje recibido ────────────────────────────────────────────────────
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Extraer datos: primero del payload "data", luego del "notification"
        val data            = message.data
        val tipo            = data["tipo"] ?: "general"
        val titulo          = data["titulo"]
            ?: message.notification?.title
            ?: "AXF GymNet"
        val cuerpo          = data["cuerpo"]
            ?: message.notification?.body
            ?: ""
        val idPersonalStr   = data["id_personal"]
        val idSuscriptorStr = data["id_suscriptor"]
        val nombrePersonal  = data["nombre_personal"] ?: titulo

        when (tipo) {
            "chat" -> mostrarNotificacionChat(
                titulo          = nombrePersonal,
                cuerpo          = cuerpo,
                idPersonal      = idPersonalStr?.toIntOrNull() ?: return,
                nombrePersonal  = nombrePersonal
            )
            else -> mostrarNotificacionGeneral(titulo, cuerpo)
        }
    }

    // ─── Notificación de chat ─────────────────────────────────────────────────
    private fun mostrarNotificacionChat(
        titulo:         String,
        cuerpo:         String,
        idPersonal:     Int,
        nombrePersonal: String
    ) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent que abre ChatActivity directamente
        // Si la app está cerrada, Android la lanza desde LoginActivity → MainActivity
        // pero con launchMode=singleTop en ChatActivity se apila correctamente.
        val abrirChat = Intent(this, ChatActivity::class.java).apply {
            putExtra("id_personal",      idPersonal)
            putExtra("nombre_personal",  nombrePersonal)
            // Estas flags garantizan que si la app está en background,
            // ChatActivity se pone al frente en vez de crear una nueva instancia
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            idPersonal,   // requestCode único por conversación
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
            // Badge en el ícono de la app
            .setNumber(1)
            .build()

        // ID única por conversación → una notificación por entrenador, no una por mensaje
        manager.notify(CHANNEL_CHAT.hashCode() + idPersonal, notif)
    }

    // ─── Notificación general ─────────────────────────────────────────────────
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