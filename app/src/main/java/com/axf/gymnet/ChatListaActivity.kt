package com.axf.gymnet

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.axf.gymnet.network.RetrofitClient
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Lista de conversaciones del suscriptor.
 *
 * YA NO crea su propio socket. Usa el socket centralizado del
 * ChatSocketService y se registra como listener para recibir
 * actualizaciones en tiempo real (nuevos mensajes, escribiendo, etc.)
 */
class ChatListaActivity : AppCompatActivity(), ChatSocketService.ChatSocketListener {

    private lateinit var rv:      RecyclerView
    private lateinit var adapter: ChatConversacionesAdapter
    private var tvSubtitulo: TextView? = null
    private var token  = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_lista)

        // Respetar status bar en el header
        val header = findViewById<View>(R.id.chatListaHeader)
        val headerPadTop = header?.paddingTop ?: 0
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            header?.updatePadding(top = headerPadTop + bars.top)
            insets
        }

        token = getSharedPreferences("axf_prefs", MODE_PRIVATE)
            .getString("token", "") ?: ""

        findViewById<View>(R.id.btnVolverChat).setOnClickListener { finish() }

        tvSubtitulo = findViewById(R.id.tvSubtituloChat)
        rv = findViewById(R.id.rvConversaciones)
        rv.layoutManager = LinearLayoutManager(this)

        adapter = ChatConversacionesAdapter(mutableListOf()) { conv ->
            // Guardar nombre en caché para que ChatSocketService lo use en notificaciones
            guardarNombrePersonal(conv.id_personal, conv.nombre_personal)

            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("id_personal",     conv.id_personal)
            intent.putExtra("nombre_personal", conv.nombre_personal)
            intent.putExtra("foto_personal",   conv.foto_url ?: "")
            startActivity(intent)
        }
        rv.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        cargarConversaciones()
        // Registrar como listener del socket centralizado
        ChatSocketService.addListener(this)
    }

    override fun onPause() {
        super.onPause()
        // Desregistrar listener pero NO desconectar el socket
        ChatSocketService.removeListener(this)
    }

    // ── ChatSocketService.ChatSocketListener ─────────────────────────────────

    override fun onSocketConnected() {
        // Al reconectar, recargar conversaciones para sincronizar estado
        cargarConversaciones()
    }

    override fun onMensajeNuevo(data: JSONObject) {
        val idPersonal = data.optInt("id_personal")
        val mensaje    = data.optJSONObject("mensaje")?.optString("contenido") ?: ""
        adapter.actualizarUltimo(idPersonal, mensaje, incrementarBadge = true)
    }

    override fun onMensajesLeidos(data: JSONObject) {
        val idPersonal = data.optInt("id_personal")
        adapter.limpiarBadge(idPersonal)
    }

    override fun onEscribiendo(data: JSONObject) {
        val idPersonal = data.optInt("id_personal")
        adapter.setEscribiendo(idPersonal, true)
    }

    override fun onPararEscribir(data: JSONObject) {
        val idPersonal = data.optInt("id_personal")
        adapter.setEscribiendo(idPersonal, false)
    }

    // ── REST ─────────────────────────────────────────────────────────────────

    private fun cargarConversaciones() {
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.instance.getConversaciones("Bearer $token")
                if (resp.isSuccessful) {
                    val lista = resp.body() ?: emptyList()

                    // Guardar todos los nombres en caché para las notificaciones
                    lista.forEach { guardarNombrePersonal(it.id_personal, it.nombre_personal) }

                    tvSubtitulo?.text = if (lista.any { it.ultimo_mensaje != null })
                        "Tus conversaciones" else "Tu entrenador / nutriólogo"

                    adapter.reemplazar(lista)
                } else {
                    Toast.makeText(
                        this@ChatListaActivity,
                        "Error ${resp.code()} al cargar.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@ChatListaActivity,
                    "Sin conexión: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Guarda el nombre del personal en SharedPreferences para que
     * ChatSocketService pueda mostrarlo en las notificaciones sin
     * necesitar hacer una llamada al backend.
     */
    private fun guardarNombrePersonal(idPersonal: Int, nombre: String) {
        getSharedPreferences("axf_personal_names", MODE_PRIVATE)
            .edit().putString("personal_$idPersonal", nombre).apply()
    }
}