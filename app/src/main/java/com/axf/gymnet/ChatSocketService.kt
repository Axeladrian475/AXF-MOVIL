package com.axf.gymnet

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
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
 * Servicio centralizado de Socket.IO.
 *
 * Mantiene UNA ÚNICA conexión Socket.IO para toda la app.
 * Las Activities (ChatActivity, ChatListaActivity) se registran como
 * listeners para recibir eventos en tiempo real, en vez de crear
 * su propia conexión.
 *
 * Beneficios:
 *  - No hay sockets duplicados/competidores
 *  - La conexión sobrevive cambios de Activity
 *  - Reconexión agresiva con heartbeat propio
 *  - Compatible con Android 14/15 (doze, restricciones de red)
 */
class ChatSocketService : Service() {

    companion object {
        private const val TAG = "ChatSocketService"
        private const val NOTIF_ID = 9001
        private const val CHANNEL_SERVICE = "chat_service"
        private const val HEARTBEAT_INTERVAL_MS = 20_000L // 20 segundos

        // ── Singleton state ──────────────────────────────────────────────
        @Volatile
        private var instance: ChatSocketService? = null

        @Volatile
        var socket: Socket? = null
            private set

        @Volatile
        var isConnected: Boolean = false
            private set

        // Activity que está mostrando el chat actualmente (para suprimir notificaciones)
        @Volatile
        var activeChatPersonalId: Int? = null

        // Listeners registrados
        private val listeners = mutableSetOf<ChatSocketListener>()

        /** Registrar un listener (Activity). Thread-safe. */
        fun addListener(listener: ChatSocketListener) {
            synchronized(listeners) { listeners.add(listener) }
        }

        /** Desregistrar un listener. Thread-safe. */
        fun removeListener(listener: ChatSocketListener) {
            synchronized(listeners) { listeners.remove(listener) }
        }

        /** Notificar a todos los listeners en el hilo principal. */
        private fun notifyListeners(action: (ChatSocketListener) -> Unit) {
            val handler = Handler(Looper.getMainLooper())
            synchronized(listeners) {
                listeners.toList().forEach { listener ->
                    handler.post { action(listener) }
                }
            }
        }

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

        /** Emitir evento a través del socket centralizado. */
        fun emit(event: String, data: JSONObject) {
            socket?.emit(event, data)
        }

        /** Emitir evento con Ack callback. */
        fun emit(event: String, data: JSONObject, ack: io.socket.client.Ack) {
            socket?.emit(event, data, ack)
        }
    }

    // ── Interface para listeners ──────────────────────────────────────────────

    interface ChatSocketListener {
        fun onSocketConnected() {}
        fun onSocketDisconnected() {}
        fun onMensajeNuevo(data: JSONObject) {}
        fun onMensajeEditado(data: JSONObject) {}
        fun onMensajeEliminado(data: JSONObject) {}
        fun onEntregado(data: JSONObject) {}
        fun onEntregadoBulk(data: JSONObject) {}
        fun onMensajesLeidos(data: JSONObject) {}
        fun onEscribiendo(data: JSONObject) {}
        fun onPararEscribir(data: JSONObject) {}
        fun onOnlineStatus(data: JSONObject) {}
        fun onNoLeidos(data: JSONObject) {}
    }

    // ── Instance state ───────────────────────────────────────────────────────

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var token = ""
    private var userId = 0
    private val heartbeatHandler = Handler(Looper.getMainLooper())
    private var heartbeatRunnable: Runnable? = null

    // ── Ciclo de vida ────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        crearCanalServicio()
        instance = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Iniciar como foreground inmediatamente para evitar ANR
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34+
            startForeground(
                NOTIF_ID,
                construirNotificacionServicio(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING
            )
        } else {
            startForeground(NOTIF_ID, construirNotificacionServicio())
        }

        val prefs = getSharedPreferences("axf_prefs", Context.MODE_PRIVATE)
        token  = prefs.getString("token", "") ?: ""
        userId = prefs.getInt("userId", 0)

        if (token.isEmpty() || userId == 0) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Actualizar FCM token en backend
        actualizarFcmToken()

        // Solo conectar si no hay socket activo
        if (socket == null || socket?.connected() != true) {
            conectarSocket()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        detenerHeartbeat()
        socket?.disconnect()
        socket = null
        isConnected = false
        instance = null
        scope.cancel()
        Log.d(TAG, "Servicio detenido, socket desconectado")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ── Socket ───────────────────────────────────────────────────────────────

    private fun conectarSocket() {
        try {
            // Desconectar socket anterior si existe
            socket?.disconnect()
            socket = null

            val sk = IO.socket(
                RetrofitClient.BASE_URL.trimEnd('/'),
                IO.Options.builder()
                    .setAuth(mapOf("token" to token))
                    .setReconnection(true)
                    .setReconnectionAttempts(Int.MAX_VALUE)
                    .setReconnectionDelay(3000)
                    .setReconnectionDelayMax(30_000)
                    .setTransports(arrayOf("websocket", "polling"))
                    .build()
            )
            socket = sk

            // ── CONNECT ──────────────────────────────────────────────────
            sk.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "✅ Socket conectado")
                isConnected = true
                sk.emit("chat:marcar_entregado", JSONObject())
                iniciarHeartbeat()
                notifyListeners { it.onSocketConnected() }
            }

            // ── DISCONNECT ───────────────────────────────────────────────
            sk.on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "❌ Socket desconectado, reintentando...")
                isConnected = false
                detenerHeartbeat()
                notifyListeners { it.onSocketDisconnected() }
            }

            sk.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.w(TAG, "Socket error: ${args.getOrNull(0)}")
                isConnected = false
            }

            // ── MENSAJE NUEVO ────────────────────────────────────────────
            sk.on("chat:mensaje_nuevo") { args ->
                val data   = args.getOrNull(0) as? JSONObject ?: return@on
                val msgObj = data.optJSONObject("mensaje") ?: return@on
                val idPersonal     = data.optInt("id_personal")
                val contenido      = msgObj.optString("contenido")
                val enviadoPor     = msgObj.optString("enviado_por")

                // Notificar a listeners (Activities)
                notifyListeners { it.onMensajeNuevo(data) }

                // Solo notificar si es de personal y NO estamos viendo ese chat
                if (enviadoPor == "suscriptor") return@on
                if (activeChatPersonalId == idPersonal) return@on

                val nombre = obtenerNombrePersonal(idPersonal)
                mostrarNotificacionMensaje(
                    idPersonal = idPersonal,
                    nombre     = nombre,
                    contenido  = contenido
                )

                // Marcar como entregado
                sk.emit("chat:marcar_entregado", JSONObject())
            }

            // ── MENSAJE EDITADO ──────────────────────────────────────────
            sk.on("chat:mensaje_editado") { args ->
                val data = args.getOrNull(0) as? JSONObject ?: return@on
                notifyListeners { it.onMensajeEditado(data) }
            }

            // ── MENSAJE ELIMINADO ────────────────────────────────────────
            sk.on("chat:mensaje_eliminado") { args ->
                val data = args.getOrNull(0) as? JSONObject ?: return@on
                notifyListeners { it.onMensajeEliminado(data) }
            }

            // ── ENTREGADO INDIVIDUAL ─────────────────────────────────────
            sk.on("chat:entregado") { args ->
                val data = args.getOrNull(0) as? JSONObject ?: return@on
                notifyListeners { it.onEntregado(data) }
            }

            // ── ENTREGADO BULK ───────────────────────────────────────────
            sk.on("chat:entregado_bulk") { args ->
                val data = args.getOrNull(0) as? JSONObject ?: return@on
                notifyListeners { it.onEntregadoBulk(data) }
            }

            // ── MENSAJES LEÍDOS ──────────────────────────────────────────
            sk.on("chat:mensajes_leidos") { args ->
                val data = args.getOrNull(0) as? JSONObject ?: return@on
                notifyListeners { it.onMensajesLeidos(data) }
            }

            // ── ESCRIBIENDO ──────────────────────────────────────────────
            sk.on("chat:escribiendo") { args ->
                val data = args.getOrNull(0) as? JSONObject ?: return@on
                notifyListeners { it.onEscribiendo(data) }
            }

            sk.on("chat:parar_escribir") { args ->
                val data = args.getOrNull(0) as? JSONObject ?: return@on
                notifyListeners { it.onPararEscribir(data) }
            }

            // ── PRESENCIA ────────────────────────────────────────────────
            sk.on("chat:online") { args ->
                val data = args.getOrNull(0) as? JSONObject ?: return@on
                notifyListeners { it.onOnlineStatus(data) }
            }

            // ── NO LEÍDOS ────────────────────────────────────────────────
            sk.on("chat:no_leidos") { args ->
                val data = args.getOrNull(0) as? JSONObject ?: return@on
                notifyListeners { it.onNoLeidos(data) }
            }

            sk.connect()
            Log.d(TAG, "Intentando conectar socket...")

        } catch (e: Exception) {
            Log.e(TAG, "Error al conectar socket: ${e.message}")
        }
    }

    // ── Heartbeat ────────────────────────────────────────────────────────────

    private fun iniciarHeartbeat() {
        detenerHeartbeat()
        heartbeatRunnable = object : Runnable {
            override fun run() {
                val sk = socket
                if (sk != null && sk.connected()) {
                    sk.emit("chat:ping", JSONObject(), io.socket.client.Ack { args ->
                        val resp = args.getOrNull(0) as? JSONObject
                        if (resp?.optBoolean("ok") != true) {
                            Log.w(TAG, "Heartbeat sin respuesta, forzando reconexión")
                            sk.disconnect()
                            // Socket.IO reconectará automáticamente
                        }
                    })
                } else if (sk != null && !sk.connected()) {
                    Log.w(TAG, "Heartbeat: socket desconectado, forzando connect()")
                    sk.connect()
                }
                heartbeatHandler.postDelayed(this, HEARTBEAT_INTERVAL_MS)
            }
        }
        heartbeatHandler.postDelayed(heartbeatRunnable!!, HEARTBEAT_INTERVAL_MS)
    }

    private fun detenerHeartbeat() {
        heartbeatRunnable?.let { heartbeatHandler.removeCallbacks(it) }
        heartbeatRunnable = null
    }

    // ── Notificación de mensaje nuevo ────────────────────────────────────────

    private fun mostrarNotificacionMensaje(
        idPersonal: Int,
        nombre:     String,
        contenido:  String
    ) {
        // Verificar si ChatActivity está mostrando este chat
        if (activeChatPersonalId == idPersonal) return

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

        manager.notify(MyFirebaseMessagingService.CHANNEL_CHAT.hashCode() + idPersonal, notif)
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun obtenerNombrePersonal(idPersonal: Int): String {
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

    // ── Canal y notificación del servicio en primer plano ────────────────────

    private fun crearCanalServicio() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val canal = NotificationChannel(
            CHANNEL_SERVICE,
            "Conexión AXF",
            NotificationManager.IMPORTANCE_MIN
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
            .setOngoing(true)
            .setSilent(true)
            .build()
    }
}