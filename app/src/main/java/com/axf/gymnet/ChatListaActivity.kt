package com.axf.gymnet

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.axf.gymnet.network.RetrofitClient
import kotlinx.coroutines.launch

class ChatListaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_lista)

        val token = getSharedPreferences("axf_prefs", MODE_PRIVATE)
            .getString("token", "") ?: ""

        findViewById<android.view.View>(R.id.btnVolverChat).setOnClickListener { finish() }

        val rv = findViewById<RecyclerView>(R.id.rvConversaciones)
        rv.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.instance.getConversaciones("Bearer $token")
                if (resp.isSuccessful) {
                    val lista = resp.body() ?: emptyList()
                    if (lista.isEmpty()) {
                        Toast.makeText(this@ChatListaActivity,
                            "No tienes conversaciones aún", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    rv.adapter = ChatConversacionesAdapter(lista) { conv ->
                        val intent = Intent(this@ChatListaActivity, ChatActivity::class.java)
                        intent.putExtra("id_personal", conv.id_personal)
                        intent.putExtra("nombre_personal", conv.nombre_personal)
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(this@ChatListaActivity,
                        "Error al cargar conversaciones", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ChatListaActivity,
                    "Sin conexión: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}