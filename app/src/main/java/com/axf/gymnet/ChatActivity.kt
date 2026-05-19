package com.axf.gymnet

import android.Manifest
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.axf.gymnet.data.ChatMensaje
import com.axf.gymnet.data.EnviarMensajeRequest
import com.axf.gymnet.network.RetrofitClient
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*

class ChatActivity : AppCompatActivity() {

    private var socket: Socket? = null
    private lateinit var adapter:         ChatMensajesAdapter
    private lateinit var rv:              RecyclerView
    private lateinit var etMensaje:       EditText
    private lateinit var tvEscribiendo:   TextView
    private lateinit var tvNombre:        TextView
    private lateinit var llReply:         View
    private lateinit var tvReplyContenido: TextView
    private lateinit var tvReplyDe:       TextView
    private lateinit var btnCancelarReply: View

    private var idPersonal  = 0
    private var token       = ""
    private var fotoPersonal = ""
    private var replyMsg:    ChatMensaje? = null
    private var editandoMsg: ChatMensaje? = null
    private var hayMasAntiguos = false
    private var offset         = 0
    private var cargandoAntiguos = false

    private val handler = Handler(Looper.getMainLooper())
    private var escribiendoRunnable: Runnable? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Habilitar Edge-to-Edge (Necesario para Android 15)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }

        setContentView(R.layout.activity_chat)
        pedirPermisoNotificaciones()

        val rootLayout = findViewById<View>(R.id.rootChatLayout)
        rv             = findViewById(R.id.rvMensajes)
        etMensaje      = findViewById(R.id.etMensaje)
        tvEscribiendo  = findViewById(R.id.tvEscribiendo)
        tvNombre       = findViewById(R.id.tvNombreChat)
        llReply        = findViewById(R.id.llReply)
        tvReplyContenido = findViewById(R.id.tvReplyContenido)
        tvReplyDe      = findViewById(R.id.tvReplyDe)
        btnCancelarReply = findViewById(R.id.btnCancelarReply)

        // 2. LOGICA DE DESPLAZAMIENTO PARA ANDROID 15
        // Ajustamos los paddings del layout raíz para que el contenido suba con el teclado
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())

            v.updatePadding(
                top = systemBars.top,
                bottom = if (ime.bottom > 0) ime.bottom else systemBars.bottom
            )

            if (ime.bottom > 0) {
                rv.postDelayed({
                    if (adapter.itemCount > 0) rv.scrollToPosition(adapter.itemCount - 1)
                }, 100)
            }
            insets
        }

        // 3. ANIMACIÓN FLUIDA (El chat sigue al teclado mientras sube)
        ViewCompat.setWindowInsetsAnimationCallback(
            rootLayout,
            object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP) {
                override fun onProgress(
                    insets: WindowInsetsCompat,
                    runningAnimations: MutableList<WindowInsetsAnimationCompat>
                ): WindowInsetsCompat {
                    val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                    val barsHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
                    rootLayout.updatePadding(bottom = if (imeHeight > 0) imeHeight else barsHeight)
                    return insets
                }
            }
        )

        val prefs = getSharedPreferences("axf_prefs", MODE_PRIVATE)
        token = prefs.getString("token", "") ?: ""

        idPersonal = intent.getIntExtra("id_personal", 0)
        val nombre = intent.getStringExtra("nombre_personal") ?: "Chat"
        fotoPersonal = intent.getStringExtra("foto_personal") ?: ""

        val prefs2 = getSharedPreferences("axf_prefs", MODE_PRIVATE)
        val nombreSusc = "${prefs2.getString("userName", "") ?: ""} ${prefs2.getString("userApellido", "") ?: ""}".trim()
        val fotoSusc   = prefs2.getString("foto_suscriptor", "") ?: ""

        if (idPersonal > 0 && nombre != "Chat") {
            getSharedPreferences("axf_personal_names", MODE_PRIVATE).edit().putString("personal_$idPersonal", nombre).apply()
        }

        tvNombre.text = nombre
        llReply.visibility = View.GONE

        findViewById<View>(R.id.btnVolver).setOnClickListener { finish() }
        btnCancelarReply.setOnClickListener { cancelarReply() }

        val lm = LinearLayoutManager(this).apply { stackFromEnd = true }
        adapter = ChatMensajesAdapter(mutableListOf(), "suscriptor",
            fotoPersonalUrl = fotoPersonal.ifBlank { null },
            fotoSuscriptorUrl = fotoSusc.ifBlank { null },
            nombrePersonal = nombre,
            nombreSuscriptor = nombreSusc)

        adapter.onLongClick = { msg -> mostrarMenuMensaje(msg) }
        rv.layoutManager = lm
        rv.adapter = adapter

        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (lm.findFirstVisibleItemPosition() != 0 || cargandoAntiguos || !hayMasAntiguos) return
                if (dy < 0) cargarAntiguos()
            }
        })

        cargarMensajes()
        conectarSocket()
        marcarComoLeidoAPI()

        findViewById<View>(R.id.btnEnviar).setOnClickListener {
            val texto = etMensaje.text.toString().trim()
            if (texto.isEmpty()) return@setOnClickListener
            if (editandoMsg != null) enviarEdicion(texto) else enviarMensaje(texto)
        }

        etMensaje.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val sk = socket ?: return
                if (!sk.connected()) return
                sk.emit("chat:escribiendo", JSONObject().put("id_personal", idPersonal))
                escribiendoRunnable?.let { handler.removeCallbacks(it) }
                escribiendoRunnable = Runnable {
                    sk.emit("chat:parar_escribir", JSONObject().put("id_personal", idPersonal))
                }.also { handler.postDelayed(it, 2000) }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val nuevoIdPersonal = intent.getIntExtra("id_personal", 0)
        if (nuevoIdPersonal != 0 && nuevoIdPersonal != idPersonal) {
            idPersonal = nuevoIdPersonal
            val nombre = intent.getStringExtra("nombre_personal") ?: "Chat"
            tvNombre.text = nombre
            adapter.limpiar()
            cargarMensajes()
            marcarComoLeidoAPI()
        }
    }

    private fun pedirPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun marcarComoLeidoAPI() {
        lifecycleScope.launch {
            try { RetrofitClient.instance.marcarComoLeido("Bearer $token", idPersonal) } catch (_: Exception) {}
        }
    }

    private fun cargarMensajes() {
        offset = 0
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.instance.getMensajes("Bearer $token", idPersonal, 50, 0)
                if (resp.isSuccessful) {
                    val body = resp.body() ?: return@launch
                    body.mensajes.forEach { adapter.agregar(it) }
                    hayMasAntiguos = body.paginacion.hay_mas
                    offset = body.mensajes.size
                    if (adapter.itemCount > 0) rv.scrollToPosition(adapter.itemCount - 1)
                    socket?.emit("chat:leer", JSONObject().put("id_personal", idPersonal))
                }
            } catch (_: Exception) {
                Toast.makeText(this@ChatActivity, "Error al cargar mensajes", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarAntiguos() {
        if (cargandoAntiguos || !hayMasAntiguos) return
        cargandoAntiguos = true
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.instance.getMensajes("Bearer $token", idPersonal, 50, offset)
                if (resp.isSuccessful) {
                    val body = resp.body() ?: return@launch
                    val firstVisible = (rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    adapter.prepend(body.mensajes)
                    hayMasAntiguos = body.paginacion.hay_mas
                    offset += body.mensajes.size
                    rv.scrollToPosition(firstVisible + body.mensajes.size)
                }
            } catch (_: Exception) {}
            finally { cargandoAntiguos = false }
        }
    }

    private fun conectarSocket() {
        try {
            // Detener el ChatSocketService mientras el usuario está en el chat activo.
            // Así hay UN SOLO socket conectado (el de esta Activity), evitando la
            // condición de carrera donde ambos sockets compiten por el mismo evento.
            ChatSocketService.stop(this)

            val sk = IO.socket(
                RetrofitClient.BASE_URL.trimEnd('/'),
                IO.Options.builder()
                    .setAuth(mapOf("token" to token))
                    .setTransports(arrayOf("websocket", "polling")) // WebSocket primero
                    .setReconnection(true)
                    .setReconnectionAttempts(Int.MAX_VALUE)
                    .setReconnectionDelay(2000)
                    .build()
            )
            socket = sk

            sk.on(Socket.EVENT_CONNECT) {
                runOnUiThread {
                    sk.emit("chat:marcar_entregado", JSONObject())
                    sk.emit("chat:leer", JSONObject().put("id_personal", idPersonal))
                    adapter.marcarEntregados()
                }
            }

            sk.on("chat:mensaje_nuevo") { args ->
                val data   = args.getOrNull(0) as? JSONObject ?: return@on
                val msgObj = data.optJSONObject("mensaje") ?: return@on

                // Filtrar: solo procesar mensajes de ESTA conversación
                val msgIdPersonal = data.optInt("id_personal")
                if (msgIdPersonal != idPersonal) return@on

                val msg = parseMensaje(msgObj)
                runOnUiThread {
                    adapter.agregar(msg)
                    rv.scrollToPosition(adapter.itemCount - 1)
                    sk.emit("chat:leer", JSONObject().put("id_personal", idPersonal))
                    marcarComoLeidoAPI()
                }
            }

            sk.on("chat:mensajes_leidos") {
                runOnUiThread { adapter.marcarLeidos(idPersonal) }
            }

            sk.on("chat:entregado_bulk") {
                runOnUiThread { adapter.marcarEntregados() }
            }

            sk.on("chat:entregado") { args ->
                val data  = args.getOrNull(0) as? JSONObject ?: return@on
                val idMsg = data.optInt("id_mensaje")
                runOnUiThread { adapter.marcarEntregadoIndividual(idMsg) }
            }

            sk.on("chat:mensaje_editado") { args ->
                val data  = args.getOrNull(0) as? JSONObject ?: return@on
                val idMsg = data.optInt("id_mensaje")
                val nuevo = data.optString("nuevo_contenido")
                val edEn  = data.optString("editado_en")
                runOnUiThread { adapter.actualizarMensaje(idMsg, nuevo, edEn) }
            }

            sk.on("chat:mensaje_eliminado") { args ->
                val data  = args.getOrNull(0) as? JSONObject ?: return@on
                val idMsg = data.optInt("id_mensaje")
                runOnUiThread { adapter.eliminarMensaje(idMsg) }
            }

            sk.on("chat:escribiendo") { args ->
                val data = args.getOrNull(0) as? JSONObject ?: return@on
                if (data.optInt("id_personal") != idPersonal) return@on
                runOnUiThread { tvEscribiendo.visibility = View.VISIBLE }
            }

            sk.on("chat:parar_escribir") { args ->
                val data = args.getOrNull(0) as? JSONObject ?: return@on
                if (data.optInt("id_personal") != idPersonal) return@on
                runOnUiThread { tvEscribiendo.visibility = View.GONE }
            }

            sk.connect()

        } catch (_: Exception) { }
    }

    private fun enviarMensaje(texto: String) {
        etMensaje.setText("")
        val sk    = socket
        val reply = replyMsg
        val data  = JSONObject().put("id_personal", idPersonal).put("contenido", texto)
        reply?.let { data.put("id_respuesta", it.id_mensaje).put("respuesta_contenido", it.contenido.take(200)).put("respuesta_enviado_por", it.enviado_por) }
        cancelarReply()

        if (sk != null && sk.connected()) {
            sk.emit("chat:enviar", data, Ack { args ->
                val resp = args.getOrNull(0) as? JSONObject ?: return@Ack
                if (resp.optBoolean("ok")) {
                    val msg = parseMensaje(resp.optJSONObject("mensaje") ?: return@Ack)
                    runOnUiThread { adapter.agregar(msg); rv.scrollToPosition(adapter.itemCount - 1) }
                }
            })
        } else {
            lifecycleScope.launch {
                try {
                    val req = EnviarMensajeRequest(idPersonal, texto, reply?.id_mensaje, reply?.contenido?.take(200), reply?.enviado_por)
                    RetrofitClient.instance.enviarMensaje("Bearer $token", req)
                    cargarMensajes()
                } catch (_: Exception) { Toast.makeText(this@ChatActivity, "Error al enviar", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun cancelarReply() { replyMsg = null; llReply.visibility = View.GONE }

    private fun parseMensaje(obj: JSONObject): ChatMensaje {
        // leido y entregado vienen como Int (0/1) desde el backend, NO como Boolean
        val leidoVal = when {
            obj.isNull("leido") -> 0
            else -> try { obj.getInt("leido") } catch (_: Exception) { if (obj.optBoolean("leido")) 1 else 0 }
        }
        val entregadoVal = when {
            obj.isNull("entregado") -> 0
            else -> try { obj.getInt("entregado") } catch (_: Exception) { if (obj.optBoolean("entregado")) 1 else 0 }
        }
        return ChatMensaje(
            id_mensaje            = obj.optInt("id_mensaje"),
            enviado_por           = obj.optString("enviado_por"),
            contenido             = obj.optString("contenido"),
            leido                 = leidoVal,
            entregado             = entregadoVal,
            id_respuesta          = if (obj.isNull("id_respuesta")) null else obj.optInt("id_respuesta"),
            respuesta_contenido   = if (obj.isNull("respuesta_contenido")) null else obj.optString("respuesta_contenido"),
            respuesta_enviado_por = if (obj.isNull("respuesta_enviado_por")) null else obj.optString("respuesta_enviado_por"),
            editado_en            = if (obj.isNull("editado_en")) null else obj.optString("editado_en"),
            enviado_en            = obj.optString("enviado_en").ifBlank { obj.optString("fecha_envio") },
            borrado_para          = obj.optString("borrado_para", "nadie")
        )
    }

    private fun mostrarMenuMensaje(msg: ChatMensaje) {
        val options = if (msg.enviado_por == "suscriptor") arrayOf("Copiar", "Responder", "Editar", "Eliminar") else arrayOf("Copiar", "Responder")
        AlertDialog.Builder(this).setItems(options) { _, which ->
            when (options[which]) {
                "Copiar" -> { (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Mensaje", msg.contenido)) }
                "Responder" -> prepararReply(msg)
                "Editar"    -> prepararEdicion(msg)
                "Eliminar"  -> confirmarEliminacion(msg)
            }
        }.show()
    }

    private fun prepararReply(msg: ChatMensaje) {
        replyMsg = msg
        tvReplyDe.text        = if (msg.enviado_por == "suscriptor") "Tú" else tvNombre.text
        tvReplyContenido.text = msg.contenido
        llReply.visibility    = View.VISIBLE
        etMensaje.requestFocus()
    }

    private fun prepararEdicion(msg: ChatMensaje) {
        editandoMsg = msg
        etMensaje.setText(msg.contenido)
        etMensaje.requestFocus()
        etMensaje.setSelection(etMensaje.text.length)
    }

    private fun confirmarEliminacion(msg: ChatMensaje) {
        AlertDialog.Builder(this).setTitle("Eliminar mensaje").setMessage("¿Estás seguro?")
            .setPositiveButton("Eliminar") { _, _ -> socket?.emit("chat:eliminar", JSONObject().put("id_mensaje", msg.id_mensaje)) }
            .setNegativeButton("Cancelar", null).show()
    }

    private fun enviarEdicion(texto: String) {
        val msg = editandoMsg ?: return
        socket?.emit("chat:editar", JSONObject().put("id_mensaje", msg.id_mensaje).put("nuevo_contenido", texto))
        editandoMsg = null
        etMensaje.setText("")
    }

    override fun onPause() {
        super.onPause()
        escribiendoRunnable?.let { handler.removeCallbacks(it) }
        socket?.emit("chat:parar_escribir", JSONObject().put("id_personal", idPersonal))
        marcarComoLeidoAPI()
    }

    override fun onDestroy() {
        super.onDestroy()
        socket?.disconnect()
        socket = null
        // Reactivar el servicio de background para que las notificaciones
        // sigan llegando cuando el usuario sale del chat
        val prefs = getSharedPreferences("axf_prefs", MODE_PRIVATE)
        val savedToken = prefs.getString("token", "") ?: ""
        if (savedToken.isNotEmpty()) {
            ChatSocketService.start(this)
        }
    }
}