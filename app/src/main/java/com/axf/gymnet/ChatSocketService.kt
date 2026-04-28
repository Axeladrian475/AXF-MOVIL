package com.axf.gymnet

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.axf.gymnet.data.FcmTokenRequest
import com.axf.gymnet.network.RetrofitClient
import com.google.firebase.messaging.FirebaseMessaging
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Servicio en primer plano (Foreground Service) que mantiene la conexión
 * Socket.io activa aunque el usuario haya salido de la app.
 *
 * Comportamiento:
 *  - Al conectarse, marca como entregados los mensajes pendientes.
 *  - Escucha "chat:mensaje_nuevo" y muestra notificación push local.
 *  - Reintenta la conexión automáticamente si se pierde.
 *  - Se detiene cuando el usuario cierra sesión.
 *
 * IMPORTANTE: FCM ya envía notificaciones cuando el servidor detecta al
 * destinatario offline. Este servicio es la capa complementaria: cuando
 * el teléfono tiene internet y el servicio está vivo, los mensajes llegan
 * instantáneamente SIN esperar al push de FCM.
 */
class ChatSocketService : Service() {

    companion object {
        private const val TAG = "ChatSocketService"
        private const val NOTIF_ID = 9001
        private const val CHANNEL_SERVICE = "chat_service"

        /** Inicia el servicio. Llamar desde LoginActivity o MainActivity. */
        fun start(context: Context) {
            val intent = Intent(context, ChatSocketService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /** Detiene el servicio. Llamar al cerrar sesión. */
        fun stop(context: Context) {
            context.stopService(Intent(context, ChatSocketService::class.java))
        }
    }

    private var socket: Socket? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var token = ""
    private var userId = 0

    // ─── Ciclo de vida ────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        crearCanalServicio()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Iniciar como foreground inmediatamente para evitar ANR
        startForeground(NOTIF_ID, construirNotificacionServicio())

        val prefs = getSharedPreferences("axf_prefs", Context.MODE_PRIVATE)
        token  = prefs.getString("token", "") ?: ""
        userId = prefs.getInt("userId", 0)

        if (token.isEmpty() || userId == 0) {
            // Sin sesión → no tiene sentido mantener el servicio
            stopSelf()
            return START_NOT_STICKY
        }

        // Actualizar FCM token en backend al arrancar el servicio
        actualizarFcmToken()

        conectarSocket()

        // START_STICKY → Android reinicia el servicio si lo mata por memoria
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        socket?.disconnect()
        socket = null
        scope.cancel()
        Log.d(TAG, "Servicio detenido")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ─── Socket ───────────────────────────────────────────────────────────────

    private fun conectarSocket() {
        try {
            val sk = IO.socket(
                RetrofitClient.BASE_URL.trimEnd('/'),
                IO.Options.builder()
                    .setAuth(mapOf("token" to token))
                    .setReconnection(true)
                    .setReconnectionAttempts(Int.MAX_VALUE)
                    .setReconnectionDelay(3000)
                    .setReconnectionDelayMax(30_000)
                    .build()
            )
            socket = sk

            sk.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Socket conectado en background")
                // Marcar mensajes pendientes como entregados
                sk.emit("chat:marcar_entregado", JSONObject())
            }

            sk.on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "Socket desconectado, reintentando...")
            }

            sk.on("chat:mensaje_nuevo") { args ->
                val data   = args.getOrNull(0) as? JSONObject ?: return@on
                val msgObj = data.optJSONObject("mensaje") ?: return@on
                val idPersonal     = data.optInt("id_personal")
                val contenido      = msgObj.optString("contenido")
                val enviadoPor     = msgObj.optString("enviado_por")

                // Solo notificar mensajes de personal (entrenadores/nutriólogos)
                // Los mensajes propios no se notifican
                if (enviadoPor == "suscriptor") return@on

                // Obtener nombre del emisor desde las conversaciones locales
                // (usamos idPersonal como fallback de nombre)
                val nombre = obtenerNombrePersonal(idPersonal)

                mostrarNotificacionMensaje(
                    idPersonal     = idPersonal,
                    nombre         = nombre,
                    contenido      = contenido
                )

                // Marcar como entregado vía socket
                sk.emit("chat:marcar_entregado", JSONObject())
            }

            sk.connect()
            Log.d(TAG, "Intentando conectar socket en background...")

        } catch (e: Exception) {
            Log.e(TAG, "Error al conectar socket: ${e.message}")
        }
    }

    // ─── Notificación de mensaje nuevo ────────────────────────────────────────

    private fun mostrarNotificacionMensaje(
        idPersonal: Int,
        nombre:     String,
        contenido:  String
    ) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val abrirChat = Intent(this, ChatActivity::class.java).apply {
            putExtra("id_personal",     idPersonal)
            putExtra("nombre_personal", nombre)
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

        val notif = NotificationCompat.Builder(this, MyFirebaseMessagingService.CHANNEL_CHAT)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setContentTitle(nombre)
            .setContentText(contenido)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contenido))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setNumber(1)
            .build()

        // ID única por conversación para agrupar mensajes del mismo entrenador
        manager.notify(MyFirebaseMessagingService.CHANNEL_CHAT.hashCode() + idPersonal, notif)
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun obtenerNombrePersonal(idPersonal: Int): String {
        // Intentar recuperar de SharedPreferences (caché simple)
        val prefs = getSharedPreferences("axf_personal_names", Context.MODE_PRIVATE)
        return prefs.getString("personal_$idPersonal", "Entrenador") ?: "Entrenador"
    }

    private fun actualizarFcmToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { fcmToken ->
            val prefs = getSharedPreferences("axf_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("fcm_token", fcmToken).apply()
            scope.launch {
                try {
                    RetrofitClient.instance.registrarFcmToken(
                        "Bearer $token",
                        FcmTokenRequest(fcmToken)
                    )
                } catch (_: Exception) {}
            }
        }
    }

    // ─── Canal y notificación del servicio en primer plano ───────────────────

    private fun crearCanalServicio() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val canal = NotificationChannel(
            CHANNEL_SERVICE,
            "Conexión AXF",
            NotificationManager.IMPORTANCE_MIN  // Silencioso, sin sonido
        ).apply {
            description = "Mantiene la conexión para recibir mensajes"
            setShowBadge(false)
        }
        manager.createNotificationChannel(canal)
    }

    private fun construirNotificacionServicio(): Notification {
        val abrirApp = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, abrirApp,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_SERVICE)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setContentTitle("AXF GymNet")
            .setContentText("Conectado para recibir mensajes")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)   // No se puede deslizar para eliminar
            .setSilent(true)
            .build()
    }
}