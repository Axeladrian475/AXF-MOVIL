package com.axf.gymnet

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.axf.gymnet.data.ChatMensaje
import com.axf.gymnet.data.EnviarMensajeRequest
import com.axf.gymnet.network.RetrofitClient
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.launch
import org.json.JSONObject

class ChatActivity : AppCompatActivity() {

    private lateinit var socket: Socket
    private lateinit var adapter: ChatMensajesAdapter
    private lateinit var rv: RecyclerView
    private lateinit var etMensaje: EditText
    private lateinit var tvEscribiendo: TextView

    private var idPersonal = 0
    private var token = ""
    private val handler = Handler(Looper.getMainLooper())
    private var escribiendoRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val prefs    = getSharedPreferences("axf_prefs", MODE_PRIVATE)
        token        = prefs.getString("token", "") ?: ""
        idPersonal   = intent.getIntExtra("id_personal", 0)
        val nombre   = intent.getStringExtra("nombre_personal") ?: "Chat"

        // Referencias UI
        rv              = findViewById(R.id.rvMensajes)
        etMensaje       = findViewById(R.id.etMensaje)
        tvEscribiendo   = findViewById(R.id.tvEscribiendo)
        val tvNombre    = findViewById<TextView>(R.id.tvNombreChat)
        val btnVolver   = findViewById<View>(R.id.btnVolver)
        val btnEnviar   = findViewById<View>(R.id.btnEnviar)

        tvNombre.text = nombre
        btnVolver.setOnClickListener { finish() }

        // RecyclerView
        adapter = ChatMensajesAdapter(mutableListOf())
        rv.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        rv.adapter = adapter

        // Cargar historial
        cargarMensajes()

        // Conectar socket
        conectarSocket()

        // Enviar mensaje
        btnEnviar.setOnClickListener {
            val texto = etMensaje.text.toString().trim()
            if (texto.isEmpty()) return@setOnClickListener
            enviarMensaje(texto)
        }

        // Indicador "escribiendo"
        etMensaje.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val data = JSONObject().put("id_personal", idPersonal)
                socket.emit("chat:escribiendo", data)
                escribiendoRunnable?.let { handler.removeCallbacks(it) }
                escribiendoRunnable = Runnable {
                    socket.emit("chat:parar_escribir", data)
                }.also { handler.postDelayed(it, 2000) }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun cargarMensajes() {
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.instance.getMensajes("Bearer $token", idPersonal)
                if (resp.isSuccessful) {
                    val body = resp.body() ?: return@launch
                    body.mensajes.forEach { adapter.agregar(it) }
                    rv.scrollToPosition(adapter.itemCount - 1)
                }
            } catch (e: Exception) {
                Toast.makeText(this@ChatActivity, "Error al cargar mensajes", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun conectarSocket() {
        try {
            // ⚠️ Cambia esta IP: emulador → 10.0.2.2 | celular físico → IP de tu PC
            val opts = IO.Options.builder()
                .setAuth(mapOf("token" to token))
                .build()
            socket = IO.socket("http://10.0.2.2:3001", opts)

            socket.on(Socket.EVENT_CONNECT) {
                runOnUiThread {
                    // Marcar como leídos al abrir
                    val data = JSONObject().put("id_personal", idPersonal)
                    socket.emit("chat:leer", data)
                }
            }

            socket.on("chat:mensaje_nuevo") { args ->
                val data = args[0] as JSONObject
                val msgJson = data.getJSONObject("mensaje")
                val msg = ChatMensaje(
                    id_mensaje   = msgJson.getInt("id_mensaje"),
                    enviado_por  = msgJson.getString("enviado_por"),
                    contenido    = msgJson.getString("contenido"),
                    leido        = msgJson.getInt("leido"),
                    enviado_en   = msgJson.getString("enviado_en")
                )
                runOnUiThread {
                    adapter.agregar(msg)
                    rv.scrollToPosition(adapter.itemCount - 1)
                    // Marcar como leído
                    val leerData = JSONObject().put("id_personal", idPersonal)
                    socket.emit("chat:leer", leerData)
                }
            }

            socket.on("chat:escribiendo") {
                runOnUiThread { tvEscribiendo.visibility = View.VISIBLE }
            }

            socket.on("chat:parar_escribir") {
                runOnUiThread { tvEscribiendo.visibility = View.GONE }
            }

            socket.connect()
        } catch (e: Exception) {
            Toast.makeText(this, "Socket no disponible, usando REST", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enviarMensaje(texto: String) {
        etMensaje.setText("")

        // Intentar por WebSocket primero
        if (::socket.isInitialized && socket.connected()) {
            val data = JSONObject()
                .put("id_personal", idPersonal)
                .put("contenido", texto)
            socket.emit("chat:enviar", data) { args ->
                val resp = args[0] as JSONObject
                if (resp.getBoolean("ok")) {
                    val msgJson = resp.getJSONObject("mensaje")
                    val msg = ChatMensaje(
                        id_mensaje   = msgJson.getInt("id_mensaje"),
                        enviado_por  = msgJson.getString("enviado_por"),
                        contenido    = msgJson.getString("contenido"),
                        leido        = msgJson.getInt("leido"),
                        enviado_en   = msgJson.getString("enviado_en")
                    )
                    runOnUiThread {
                        adapter.agregar(msg)
                        rv.scrollToPosition(adapter.itemCount - 1)
                    }
                }
            }
        } else {
            // Fallback REST
            lifecycleScope.launch {
                try {
                    RetrofitClient.instance.enviarMensaje(
                        "Bearer $token",
                        EnviarMensajeRequest(idPersonal, texto)
                    )
                    cargarMensajes()
                } catch (e: Exception) {
                    Toast.makeText(this@ChatActivity, "Error al enviar", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::socket.isInitialized) socket.disconnect()
    }
}