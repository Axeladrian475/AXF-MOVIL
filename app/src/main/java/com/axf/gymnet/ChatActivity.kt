package com.axf.gymnet

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.axf.gymnet.data.ChatMensaje
import com.axf.gymnet.network.RetrofitClient
import io.socket.client.Ack
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Activity del chat individual.
 *
 * YA NO crea su propio socket. Usa el socket centralizado del
 * ChatSocketService y se registra como listener para recibir
 * todos los eventos en tiempo real.
 */
class ChatActivity : AppCompatActivity(), ChatSocketService.ChatSocketListener {

    private lateinit var adapter:         ChatMensajesAdapter
    private lateinit var rv:              RecyclerView
    private lateinit var etMensaje:       EditText
    private lateinit var tvEscribiendo:   TextView
    private lateinit var tvNombre:        TextView
    private lateinit var llReply:         View
    private lateinit var tvReplyContenido: TextView
    private lateinit var tvReplyDe:       TextView
    private lateinit var btnCancelarReply: View
    private lateinit var keyboardSpacer:  View

    private var idPersonal  = 0
    private var token       = ""
    private var fotoPersonal = ""
    private var replyMsg:    ChatMensaje? = null
    private var editandoMsg: ChatMensaje? = null

    private val handler = Handler(Looper.getMainLooper())
    private var escribiendoRunnable: Runnable? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        hideSystemBars()

        val rootLayout = findViewById<View>(R.id.rootChatLayout)
        keyboardSpacer = findViewById(R.id.keyboardSpacer)
        
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val isImeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            
            findViewById<View>(R.id.headerChat).setPadding(0, systemBars.top, 0, 0)
            
            val params = keyboardSpacer.layoutParams
            params.height = if (isImeVisible) ime.bottom else 0
            keyboardSpacer.layoutParams = params
            
            if (isImeVisible) {
                rv.postDelayed({
                    if (adapter.itemCount > 0) rv.scrollToPosition(adapter.itemCount - 1)
                }, 100)
            }
            insets
        }

        pedirPermisoNotificaciones()
        bindViews()
        setupLogic()
    }

    private fun bindViews() {
        rv             = findViewById(R.id.rvMensajes)
        etMensaje      = findViewById(R.id.etMensaje)
        tvEscribiendo  = findViewById(R.id.tvEscribiendo)
        tvNombre       = findViewById(R.id.tvNombreChat)
        llReply        = findViewById(R.id.llReply)
        tvReplyContenido = findViewById(R.id.tvReplyContenido)
        tvReplyDe      = findViewById(R.id.tvReplyDe)
        btnCancelarReply = findViewById(R.id.btnCancelarReply)
    }

    private fun setupLogic() {
        val prefs = getSharedPreferences("axf_prefs", MODE_PRIVATE)
        token = prefs.getString("token", "") ?: ""
        idPersonal = intent.getIntExtra("id_personal", 0)
        val nombre = intent.getStringExtra("nombre_personal") ?: "Chat"
        fotoPersonal = intent.getStringExtra("foto_personal") ?: ""
        
        tvNombre.text = nombre
        findViewById<View>(R.id.btnVolver).setOnClickListener { finish() }
        btnCancelarReply.setOnClickListener { cancelarReply() }

        val lm = LinearLayoutManager(this).apply { stackFromEnd = true }
        adapter = ChatMensajesAdapter(mutableListOf(), "suscriptor",
            fotoPersonalUrl = fotoPersonal.ifBlank { null },
            nombrePersonal = nombre,
            nombreSuscriptor = "Tú")
        
        rv.layoutManager = lm
        rv.adapter = adapter

        cargarMensajes()
        marcarComoLeidoAPI()

        // Registrar como listener del socket centralizado
        ChatSocketService.addListener(this)
        // Marcar que estamos viendo este chat (suprime notificaciones)
        ChatSocketService.activeChatPersonalId = idPersonal

        // Si el socket está conectado, marcar leídos vía socket
        if (ChatSocketService.isConnected) {
            ChatSocketService.emit("chat:leer", JSONObject().put("id_personal", idPersonal))
        }

        // Guardar nombre en caché para notificaciones
        getSharedPreferences("axf_personal_names", MODE_PRIVATE)
            .edit().putString("personal_$idPersonal", nombre).apply()

        findViewById<View>(R.id.btnEnviar).setOnClickListener {
            val texto = etMensaje.text.toString().trim()
            if (texto.isNotEmpty()) enviarMensaje(texto)
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Si llegamos aquí por una notificación de otro chat, reconfigurar
        val nuevoIdPersonal = intent.getIntExtra("id_personal", 0)
        if (nuevoIdPersonal > 0 && nuevoIdPersonal != idPersonal) {
            idPersonal = nuevoIdPersonal
            val nombre = intent.getStringExtra("nombre_personal") ?: "Chat"
            fotoPersonal = intent.getStringExtra("foto_personal") ?: ""
            tvNombre.text = nombre
            ChatSocketService.activeChatPersonalId = idPersonal
            adapter.limpiar()
            cargarMensajes()
            marcarComoLeidoAPI()
            if (ChatSocketService.isConnected) {
                ChatSocketService.emit("chat:leer", JSONObject().put("id_personal", idPersonal))
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    private fun hideSystemBars() {
        val windowInsetsController = ViewCompat.getWindowInsetsController(window.decorView) ?: return
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun pedirPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // ── Enviar Mensaje (vía socket centralizado) ─────────────────────────────

    private fun enviarMensaje(texto: String) {
        etMensaje.setText("")
        val data = JSONObject()
            .put("id_personal", idPersonal)
            .put("contenido", texto)

        if (replyMsg != null) {
            data.put("id_respuesta", replyMsg!!.id_mensaje)
        }
        cancelarReply()

        ChatSocketService.emit("chat:enviar", data, Ack { args ->
            val resp = args.getOrNull(0) as? JSONObject ?: return@Ack
            if (resp.optBoolean("ok")) {
                val msgObj = resp.optJSONObject("mensaje") ?: return@Ack
                runOnUiThread {
                    adapter.agregar(parseMensaje(msgObj))
                    rv.scrollToPosition(adapter.itemCount - 1)
                }
            }
        })

        // Indicador de dejar de escribir
        ChatSocketService.emit("chat:parar_escribir", JSONObject().put("id_personal", idPersonal))
    }

    // ── Parsear mensaje JSON ─────────────────────────────────────────────────

    private fun parseMensaje(obj: JSONObject): ChatMensaje {
        return ChatMensaje(
            id_mensaje = obj.optInt("id_mensaje"),
            enviado_por = obj.optString("enviado_por"),
            contenido = obj.optString("contenido"),
            leido = if (obj.optInt("leido", 0) == 1 || obj.optBoolean("leido")) 1 else 0,
            entregado = if (obj.optInt("entregado", 0) == 1 || obj.optBoolean("entregado")) 1 else 0,
            enviado_en = obj.optString("enviado_en", ""),
            borrado_para = obj.optString("borrado_para", "nadie"),
            editado_en = if (obj.isNull("editado_en")) null else obj.optString("editado_en"),
            id_respuesta = if (obj.isNull("id_respuesta")) null else obj.optInt("id_respuesta"),
            respuesta_contenido = if (obj.isNull("respuesta_contenido")) null else obj.optString("respuesta_contenido"),
            respuesta_enviado_por = if (obj.isNull("respuesta_enviado_por")) null else obj.optString("respuesta_enviado_por")
        )
    }

    // ── REST fallbacks ───────────────────────────────────────────────────────

    private fun marcarComoLeidoAPI() {
        lifecycleScope.launch {
            try { RetrofitClient.instance.marcarComoLeido("Bearer $token", idPersonal) } catch (_: Exception) {}
        }
    }

    private fun cargarMensajes() {
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.instance.getMensajes("Bearer $token", idPersonal, 50, 0)
                if (resp.isSuccessful) {
                    resp.body()?.mensajes?.forEach { adapter.agregar(it) }
                    if (adapter.itemCount > 0) rv.scrollToPosition(adapter.itemCount - 1)
                }
            } catch (_: Exception) {}
        }
    }

    // ── ChatSocketService.ChatSocketListener ─────────────────────────────────

    override fun onSocketConnected() {
        // Al reconectar, marcar leídos y recargar mensajes
        ChatSocketService.emit("chat:leer", JSONObject().put("id_personal", idPersonal))
    }

    override fun onMensajeNuevo(data: JSONObject) {
        if (data.optInt("id_personal") == idPersonal) {
            val msgObj = data.optJSONObject("mensaje") ?: return
            val msg = parseMensaje(msgObj)
            adapter.agregar(msg)
            rv.scrollToPosition(adapter.itemCount - 1)
            // Marcar como leído inmediatamente
            ChatSocketService.emit("chat:leer", JSONObject().put("id_personal", idPersonal))
        }
    }

    override fun onMensajeEditado(data: JSONObject) {
        val idMensaje = data.optInt("id_mensaje")
        val nuevoContenido = data.optString("nuevo_contenido")
        val editadoEn = data.optString("editado_en")
        if (idMensaje > 0) {
            adapter.actualizarMensaje(idMensaje, nuevoContenido, editadoEn)
        }
    }

    override fun onMensajeEliminado(data: JSONObject) {
        val idMensaje = data.optInt("id_mensaje")
        if (idMensaje > 0) {
            adapter.eliminarMensaje(idMensaje)
        }
    }

    override fun onEntregado(data: JSONObject) {
        val idMensaje = data.optInt("id_mensaje")
        if (idMensaje > 0) {
            adapter.marcarEntregadoIndividual(idMensaje)
        }
    }

    override fun onEntregadoBulk(data: JSONObject) {
        val idP = data.optInt("id_personal")
        if (idP == idPersonal) {
            adapter.marcarEntregados()
        }
    }

    override fun onMensajesLeidos(data: JSONObject) {
        val idP = data.optInt("id_personal")
        if (idP == idPersonal) {
            adapter.marcarLeidos(idPersonal)
        }
    }

    override fun onEscribiendo(data: JSONObject) {
        val idP = data.optInt("id_personal")
        if (idP == idPersonal) {
            tvEscribiendo.visibility = View.VISIBLE
            tvEscribiendo.text = "escribiendo..."
        }
    }

    override fun onPararEscribir(data: JSONObject) {
        val idP = data.optInt("id_personal")
        if (idP == idPersonal) {
            tvEscribiendo.visibility = View.GONE
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun cancelarReply() { replyMsg = null; llReply.visibility = View.GONE }

    override fun onResume() {
        super.onResume()
        ChatSocketService.activeChatPersonalId = idPersonal
    }

    override fun onPause() {
        super.onPause()
        ChatSocketService.activeChatPersonalId = null
    }

    override fun onDestroy() {
        super.onDestroy()
        ChatSocketService.removeListener(this)
        ChatSocketService.activeChatPersonalId = null
    }
}
