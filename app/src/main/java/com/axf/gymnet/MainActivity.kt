package com.axf.gymnet

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("axf_prefs", MODE_PRIVATE)
        val userName = prefs.getString("userName", "Usuario")
        val suscripcionActiva = prefs.getBoolean("suscripcionActiva", false)

        val tv = findViewById<TextView>(R.id.tvBienvenida)
        tv.text = "Bienvenido, $userName\n" +
                "Suscripción: ${if (suscripcionActiva) "✅ Activa" else "❌ Inactiva"}"
    }
}