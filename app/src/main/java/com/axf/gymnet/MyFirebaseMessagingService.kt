package com.axf.gymnet

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.axf.gymnet.network.RetrofitClient
import com.axf.gymnet.data.FcmTokenRequest

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Guardar token localmente
        getSharedPreferences("axf_prefs", Context.MODE_PRIVATE)
            .edit().putString("fcm_token", token).apply()

        // Registrar en backend si ya hay sesión activa
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

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val titulo  = message.notification?.title ?: message.data["titulo"] ?: "Nuevo mensaje"
        val cuerpo  = message.notification?.body  ?: message.data["cuerpo"] ?: ""
        val idPersonal    = message.data["id_personal"] ?: return
        val nombrePersonal = message.data["nombre_personal"] ?: ""

        // Intent que abre ChatActivity directamente
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("id_personal",     idPersonal.toIntOrNull() ?: return)
            putExtra("nombre_personal", nombrePersonal)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, idPersonal.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "chat_mensajes"
        val manager   = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal (Android 8+)
        val channel = NotificationChannel(
            channelId,
            "Mensajes del chat",
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Notificaciones de nuevos mensajes" }
        manager.createNotificationChannel(channel)

        val notif = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(idPersonal.toInt(), notif)
    }
}