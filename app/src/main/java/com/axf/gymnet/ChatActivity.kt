package com.axf.gymnet

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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

class ChatActivity : AppCompatActivity() {

    private var socket: Socket? = null
    private lateinit var adapter: ChatMensajesAdapter
    private lateinit var rv: RecyclerView
    private lateinit var etMensaje: EditText
    private lateinit var tvEscribiendo: TextView
    private lateinit var tvNombre: TextView
    private lateinit var llReply: View
    private lateinit var tvReplyContenido: TextView
    private lateinit var tvReplyDe: TextView
    private lateinit var btnCancelarReply: View

    private var idPersonal = 0
    private var token = ""
    private var replyMsg: ChatMensaje? = null
    private var editandoMsg: ChatMensaje? = null
    private var hayMasAntiguos = false
    private var offset = 0
    private var cargandoAntiguos = false

    private val handler = Handler(Looper.getMainLooper())
    private var escribiendoRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val prefs  = getSharedPreferences("axf_prefs", MODE_PRIVATE)
        token      = prefs.getString("token", "") ?: ""
        idPersonal = intent.getIntExtra("id_personal", 0)
        val nombre = intent.getStringExtra("nombre_personal") ?: "Chat"

        rv             = findViewById(R.id.rvMensajes)
        etMensaje      = findViewById(R.id.etMensaje)
        tvEscribiendo  = findViewById(R.id.tvEscribiendo)
        tvNombre       = findViewById(R.id.tvNombreChat)
        llReply        = findViewById(R.id.llReply)
        tvReplyContenido = findViewById(R.id.tvReplyContenido)
        tvReplyDe      = findViewById(R.id.tvReplyDe)
        btnCancelarReply = findViewById(R.id.btnCancelarReply)

        tvNombre.text = nombre
        llReply.visibility = View.GONE

        findViewById<View>(R.id.btnVolver).setOnClickListener { finish() }
        btnCancelarReply.setOnClickListener { cancelarReply() }

        val lm = LinearLayoutManager(this).apply { stackFromEnd = true }
        adapter = ChatMensajesAdapter(mutableListOf(), "suscriptor")
        adapter.onLongClick = { msg -> mostrarMenuMensaje(msg) }
        rv.layoutManager = lm
        rv.adapter = adapter

        // Scroll al tope → cargar más antiguos
        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (!lm.findFirstVisibleItemPosition().let { it == 0 } || cargandoAntiguos || !hayMasAntiguos) return
                if (dy < 0) cargarAntiguos()
            }
        })

        cargarMensajes()
        conectarSocket()

        findViewById<View>(R.id.btnEnviar).setOnClickListener {
            val texto = etMensaje.text.toString().trim()
            if (texto.isEmpty()) return@setOnClickListener
            if (editandoMsg != null) {
                enviarEdicion(texto)
            } else {
                enviarMensaje(texto)
            }
        }

        etMensaje.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val sk = socket ?: return
                if (!sk.connected()) return
                // CORRECCIÓN: incluir id_personal e id_suscriptor para que
                // el receptor pueda filtrar por conversación activa.
                sk.emit("chat:escribiendo", JSONObject()
                    .put("id_personal", idPersonal))
                escribiendoRunnable?.let { handler.removeCallbacks(it) }
                escribiendoRunnable = Runnable {
                    sk.emit("chat:parar_escribir", JSONObject()
                        .put("id_personal", idPersonal))
                }.also { handler.postDelayed(it, 2000) }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
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
                    if (adapter.itemCount > 0)
                        rv.scrollToPosition(adapter.itemCount - 1)
                    // Marcar como leídos
                    socket?.emit("chat:leer", JSONObject().put("id_personal", idPersonal))
                }
            } catch (e: Exception) {
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
            val sk = IO.socket(
                "http://10.0.2.2:3001",  // ← cambia a tu IP de producción o usa BuildConfig
                IO.Options.builder().setAuth(mapOf("token" to token))
                    .setReconnection(true).setReconnectionAttempts(Int.MAX_VALUE)
                    .setReconnectionDelay(2000).build()
            )
            socket = sk

            sk.on(Socket.EVENT_CONNECT) {
                runOnUiThread {
                    // Al conectar, marcar mensajes pendientes como entregados
                    sk.emit("chat:marcar_entregado", JSONObject())
                    sk.emit("chat:leer", JSONObject().put("id_personal", idPersonal))
                    adapter.marcarEntregados()
                }
            }

            sk.on(Socket.EVENT_CONNECT_ERROR) { args ->
                android.util.Log.w("ChatSocket", "Error: ${args.getOrNull(0)}")
            }

            sk.on("chat:mensaje_nuevo") { args ->
                val data   = args.getOrNull(0) as? JSONObject ?: return@on
                val msgObj = data.optJSONObject("mensaje") ?: return@on
                val msg    = parseMensaje(msgObj)
                runOnUiThread {
                    adapter.agregar(msg)
                    rv.scrollToPosition(adapter.itemCount - 1)
                    sk.emit("chat:leer", JSONObject().put("id_personal", idPersonal))
                }
            }

            sk.on("chat:mensajes_leidos") {
                runOnUiThread { adapter.marcarLeidos(idPersonal) }
            }

            sk.on("chat:entregado") { args ->
                val data = args.getOrNull(0) as? JSONObject ?: return@on
                val idMsg = data.optInt("id_mensaje")
                runOnUiThread {
                    // Actualizar ese mensaje específico como entregado
                    // (usa notifyDataSetChanged o un método del adapter)
                    adapter.notifyDataSetChanged()
                }
            }

            sk.on("chat:entregado_bulk") {
                runOnUiThread { adapter.marcarEntregados() }
            }

            sk.on("chat:mensaje_editado") { args ->
                val data = args.getOrNull(0) as? JSONObject ?: return@on
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

            sk.on("chat:escribiendo") {
                runOnUiThread { tvEscribiendo.visibility = View.VISIBLE }
            }
            sk.on("chat:parar_escribir") {
                runOnUiThread { tvEscribiendo.visibility = View.GONE }
            }

            sk.connect()
        } catch (e: Exception) {
            android.util.Log.w("ChatSocket", "Error al conectar: ${e.message}")
        }
    }

    private fun enviarMensaje(texto: String) {
        etMensaje.setText("")
        val sk = socket
        val reply = replyMsg

        val data = JSONObject()
            .put("id_personal", idPersonal)
            .put("contenido", texto)
        reply?.let {
            data.put("id_respuesta", it.id_mensaje)
            data.put("respuesta_contenido", it.contenido.take(200))
            data.put("respuesta_enviado_por", it.enviado_por)
        }
        cancelarReply()

        if (sk != null && sk.connected()) {
            sk.emit("chat:enviar", data, Ack { args ->
                val resp = args.getOrNull(0) as? JSONObject ?: return@Ack
                if (resp.optBoolean("ok")) {
                    val msg = parseMensaje(resp.optJSONObject("mensaje") ?: return@Ack)
                    runOnUiThread {
                        adapter.agregar(msg)
                        rv.scrollToPosition(adapter.itemCount - 1)
                    }
                }
            })
        } else {
            lifecycleScope.launch {
                try {
                    val req = EnviarMensajeRequest(
                        id_personal           = idPersonal,
                        contenido             = texto,
                        id_respuesta          = reply?.id_mensaje,
                        respuesta_contenido   = reply?.contenido?.take(200),
                        respuesta_enviado_por = reply?.enviado_por
                    )
                    RetrofitClient.instance.enviarMensaje("Bearer $token", req)
                    cargarMensajes()
                } catch (_: Exception) {
                    Toast.makeText(this@ChatActivity, "Error al enviar", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun enviarEdicion(nuevoTexto: String) {
        val msg = editandoMsg ?: return
        editandoMsg = null
        etMensaje.setText("")
        etMensaje.hint = "Escribe un mensaje..."
        llReply.visibility = View.GONE

        val sk = socket
        if (sk != null && sk.connected()) {
            val data = JSONObject()
                .put("id_mensaje", msg.id_mensaje)
                .put("nuevo_contenido", nuevoTexto)
            sk.emit("chat:editar", data, Ack { args ->
                val resp = args.getOrNull(0) as? JSONObject ?: return@Ack
                if (resp.optBoolean("ok")) {
                    runOnUiThread {
                        adapter.actualizarMensaje(msg.id_mensaje, nuevoTexto, "")
                    }
                }
            })
        }
    }

    private fun mostrarMenuMensaje(msg: ChatMensaje) {
        if (msg.borrado_para == "todos") return
        val esMio = msg.enviado_por == "suscriptor"
        val opciones = buildList {
            add("↩️ Responder")
            add("📋 Copiar texto")
            if (esMio) add("✏️ Editar")
            add("🗑️ Eliminar para mí")
            if (esMio) add("🚫 Eliminar para todos")
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setItems(opciones) { _, idx ->
                when (opciones[idx]) {
                    "↩️ Responder"          -> iniciarReply(msg)
                    "📋 Copiar texto"       -> copiarTexto(msg.contenido)
                    "✏️ Editar"             -> iniciarEdicion(msg)
                    "🗑️ Eliminar para mí"  -> eliminarMensaje(msg, false)
                    "🚫 Eliminar para todos"-> eliminarMensaje(msg, true)
                }
            }.show()
    }

    private fun iniciarReply(msg: ChatMensaje) {
        replyMsg = msg
        editandoMsg = null
        llReply.visibility = View.VISIBLE
        tvReplyDe.text = if (msg.enviado_por == "suscriptor") "Tú" else tvNombre.text
        tvReplyContenido.text = msg.contenido
        etMensaje.requestFocus()
    }

    private fun iniciarEdicion(msg: ChatMensaje) {
        editandoMsg = msg
        replyMsg = null
        etMensaje.setText(msg.contenido)
        etMensaje.setSelection(msg.contenido.length)
        llReply.visibility = View.VISIBLE
        tvReplyDe.text = "✏️ Editando mensaje"
        tvReplyContenido.text = msg.contenido
        etMensaje.requestFocus()
    }

    private fun cancelarReply() {
        replyMsg = null
        editandoMsg = null
        llReply.visibility = View.GONE
        etMensaje.hint = "Escribe un mensaje..."
    }

    private fun eliminarMensaje(msg: ChatMensaje, paraTodos: Boolean) {
        val sk = socket ?: return
        val data = JSONObject()
            .put("id_mensaje", msg.id_mensaje)
            .put("para_todos", paraTodos)
        sk.emit("chat:eliminar", data, Ack { args ->
            val resp = args.getOrNull(0) as? JSONObject ?: return@Ack
            if (resp.optBoolean("ok")) {
                runOnUiThread {
                    if (paraTodos) {
                        adapter.eliminarMensaje(msg.id_mensaje)
                    } else {
                        // Para mí: quitar de la lista local (no en la BD)
                        // Aquí puedes usar un método removeLocal en el adapter
                        adapter.eliminarMensaje(msg.id_mensaje)
                    }
                }
            }
        })
    }

    private fun copiarTexto(texto: String) {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("mensaje", texto))
        Toast.makeText(this, "Copiado al portapapeles", Toast.LENGTH_SHORT).show()
    }

    private fun parseMensaje(obj: JSONObject) = ChatMensaje(
        id_mensaje            = obj.optInt("id_mensaje"),
        enviado_por           = obj.optString("enviado_por"),
        contenido             = obj.optString("contenido"),
        leido                 = obj.optInt("leido"),
        entregado             = obj.optInt("entregado"),
        editado_en            = obj.optString("editado_en").takeIf { it.isNotBlank() && it != "null" },
        borrado_para          = obj.optString("borrado_para", "nadie"),
        id_respuesta          = obj.optInt("id_respuesta").takeIf { it != 0 },
        respuesta_contenido   = obj.optString("respuesta_contenido").takeIf { it.isNotBlank() && it != "null" },
        respuesta_enviado_por = obj.optString("respuesta_enviado_por").takeIf { it.isNotBlank() && it != "null" },
        enviado_en            = obj.optString("enviado_en")
    )

    override fun onDestroy() {
        super.onDestroy()
        socket?.disconnect()
        socket = null
    }
}