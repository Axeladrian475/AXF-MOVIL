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
import com.axf.gymnet.network.RetrofitClient
import kotlinx.coroutines.launch

class ChatListaActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private var tvSubtitulo: TextView? = null
    private var token = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_lista)

        token = getSharedPreferences("axf_prefs", MODE_PRIVATE)
            .getString("token", "") ?: ""

        findViewById<View>(R.id.btnVolverChat).setOnClickListener { finish() }

        tvSubtitulo = findViewById<TextView?>(R.id.tvSubtituloChat)
        rv = findViewById(R.id.rvConversaciones)
        rv.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        cargarConversaciones()
    }

    private fun cargarConversaciones() {
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.instance.getConversaciones("Bearer $token")
                if (resp.isSuccessful) {
                    val lista = resp.body() ?: emptyList()

                    // Mostrar subtítulo según si hay chats o solo personal disponible
                    tvSubtitulo?.text = if (lista.any { it.ultimo_mensaje != null })
                        "Tus conversaciones" else "Tu entrenador / nutriólogo"

                    rv.adapter = ChatConversacionesAdapter(lista) { conv ->
                        val intent = Intent(this@ChatListaActivity, ChatActivity::class.java)
                        intent.putExtra("id_personal", conv.id_personal)
                        intent.putExtra("nombre_personal", conv.nombre_personal)
                        startActivity(intent)
                    }
                } else {
                    val code = resp.code()
                    Toast.makeText(
                        this@ChatListaActivity,
                        "Error $code al cargar. Revisa tu sesión.",
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
}