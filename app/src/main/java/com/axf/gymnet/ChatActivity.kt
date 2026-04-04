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
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.launch
import org.json.JSONObject

class ChatActivity : AppCompatActivity() {

    private var socket: Socket? = null          // ✅ FIX: var nullable en lugar de lateinit
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

        val prefs  = getSharedPreferences("axf_prefs", MODE_PRIVATE)
        token      = prefs.getString("token", "") ?: ""
        idPersonal = intent.getIntExtra("id_personal", 0)
        val nombre = intent.getStringExtra("nombre_personal") ?: "Chat"

        rv            = findViewById(R.id.rvMensajes)
        etMensaje     = findViewById(R.id.etMensaje)
        tvEscribiendo = findViewById(R.id.tvEscribiendo)
        val tvNombre  = findViewById<TextView>(R.id.tvNombreChat)
        val btnVolver = findViewById<View>(R.id.btnVolver)
        val btnEnviar = findViewById<View>(R.id.btnEnviar)

        tvNombre.text = nombre
        btnVolver.setOnClickListener { finish() }

        adapter = ChatMensajesAdapter(mutableListOf())
        rv.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        rv.adapter = adapter

        cargarMensajes()
        conectarSocket()

        btnEnviar.setOnClickListener {
            val texto = etMensaje.text.toString().trim()
            if (texto.isEmpty()) return@setOnClickListener
            enviarMensaje(texto)
        }

        // ✅ FIX: verificar socket != null antes de emitir
        etMensaje.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val sk = socket ?: return
                if (!sk.connected()) return
                val data = JSONObject().put("id_personal", idPersonal)
                sk.emit("chat:escribiendo", data)
                escribiendoRunnable?.let { handler.removeCallbacks(it) }
                escribiendoRunnable = Runnable {
                    sk.emit("chat:parar_escribir", data)
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
                    if (adapter.itemCount > 0)
                        rv.scrollToPosition(adapter.itemCount - 1)
                }
            } catch (e: Exception) {
                Toast.makeText(this@ChatActivity, "Error al cargar mensajes", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun conectarSocket() {
        try {
            val opts = IO.Options.builder()
                .setAuth(mapOf("token" to token))
                .build()
            val sk = IO.socket("http://10.0.2.2:3001", opts)  // ✅ emulador; cambia a tu IP si usas celular físico
            socket = sk

            sk.on(Socket.EVENT_CONNECT) {
                runOnUiThread {
                    val data = JSONObject().put("id_personal", idPersonal)
                    sk.emit("chat:leer", data)
                }
            }

            // ✅ FIX: manejar error de conexión sin crashear
            sk.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val err = args.getOrNull(0)?.toString() ?: "desconocido"
                android.util.Log.w("ChatSocket", "Error conexión: $err")
            }

            sk.on("chat:mensaje_nuevo") { args ->
                val data = args.getOrNull(0) as? JSONObject ?: return@on
                val msgJson = data.optJSONObject("mensaje") ?: return@on
                val msg = ChatMensaje(
                    id_mensaje  = msgJson.optInt("id_mensaje"),
                    enviado_por = msgJson.optString("enviado_por"),
                    contenido   = msgJson.optString("contenido"),
                    leido       = msgJson.optInt("leido"),
                    enviado_en  = msgJson.optString("enviado_en")
                )
                runOnUiThread {
                    adapter.agregar(msg)
                    rv.scrollToPosition(adapter.itemCount - 1)
                    val leerData = JSONObject().put("id_personal", idPersonal)
                    sk.emit("chat:leer", leerData)
                }
            }

            sk.on("chat:escribiendo") {
                runOnUiThread { tvEscribiendo.visibility = View.VISIBLE }
            }

            sk.on("chat:parar_escribir") {
                runOnUiThread { tvEscribiendo.visibility = View.GONE }
            }

            sk.connect()
        } catch (e: Exception) {
            android.util.Log.w("ChatSocket", "Socket no disponible: ${e.message}")
        }
    }

    private fun enviarMensaje(texto: String) {
        etMensaje.setText("")
        val sk = socket

        if (sk != null && sk.connected()) {
            val data = JSONObject()
                .put("id_personal", idPersonal)
                .put("contenido", texto)
            sk.emit("chat:enviar", data, Ack { args ->
                val resp = args.getOrNull(0) as? JSONObject ?: return@Ack
                if (resp.optBoolean("ok")) {
                    val msgJson = resp.optJSONObject("mensaje") ?: return@Ack
                    val msg = ChatMensaje(
                        id_mensaje  = msgJson.optInt("id_mensaje"),
                        enviado_por = msgJson.optString("enviado_por"),
                        contenido   = msgJson.optString("contenido"),
                        leido       = msgJson.optInt("leido"),
                        enviado_en  = msgJson.optString("enviado_en")
                    )
                    runOnUiThread {
                        adapter.agregar(msg)
                        rv.scrollToPosition(adapter.itemCount - 1)
                    }
                }
            })
        } else {
            // Fallback REST cuando no hay WebSocket
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
        socket?.disconnect()   // ✅ FIX: safe call en lugar de isInitialized
        socket = null
    }
}