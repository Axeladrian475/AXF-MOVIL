package com.axf.gymnet

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.axf.gymnet.data.ChatConversacion
import com.axf.gymnet.network.RetrofitClient
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.launch
import org.json.JSONObject

class ChatListaActivity : AppCompatActivity() {

    private lateinit var rv:      RecyclerView
    private lateinit var adapter: ChatConversacionesAdapter
    private var tvSubtitulo: TextView? = null
    private var token  = ""
    private var socket: Socket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_lista)

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
            startActivity(intent)
        }
        rv.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        cargarConversaciones()
        conectarSocket()
    }

    override fun onPause() {
        super.onPause()
        socket?.disconnect()
        socket = null
    }

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

    private fun conectarSocket() {
        try {
            val sk = IO.socket(
                RetrofitClient.BASE_URL.trimEnd('/'),
                IO.Options.builder()
                    .setAuth(mapOf("token" to token))
                    .setReconnection(true)
                    .setReconnectionAttempts(Int.MAX_VALUE)
                    .setReconnectionDelay(2000)
                    .build()
            )
            socket = sk

            // Nuevo mensaje → actualizar preview y badge
            sk.on("chat:mensaje_nuevo") { args ->
                val data       = args.getOrNull(0) as? JSONObject ?: return@on
                val idPersonal = data.optInt("id_personal")
                val mensaje    = data.optJSONObject("mensaje")?.optString("contenido") ?: ""
                runOnUiThread {
                    adapter.actualizarUltimo(idPersonal, mensaje, incrementarBadge = true)
                }
            }

            // Mensajes leídos → limpiar badge
            sk.on("chat:mensajes_leidos") { args ->
                val data       = args.getOrNull(0) as? JSONObject ?: return@on
                val idPersonal = data.optInt("id_personal")
                runOnUiThread { adapter.limpiarBadge(idPersonal) }
            }

            // Está escribiendo
            sk.on("chat:escribiendo") { args ->
                val data       = args.getOrNull(0) as? JSONObject ?: return@on
                val idPersonal = data.optInt("id_personal")
                runOnUiThread { adapter.setEscribiendo(idPersonal, true) }
            }

            sk.on("chat:parar_escribir") { args ->
                val data       = args.getOrNull(0) as? JSONObject ?: return@on
                val idPersonal = data.optInt("id_personal")
                runOnUiThread { adapter.setEscribiendo(idPersonal, false) }
            }

            sk.connect()
        } catch (e: Exception) {
            android.util.Log.w("ChatLista", "Socket error: ${e.message}")
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