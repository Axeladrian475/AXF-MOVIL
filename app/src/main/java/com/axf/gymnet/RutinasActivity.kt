package com.axf.gymnet

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.axf.gymnet.data.RutinaResponse
import com.axf.gymnet.network.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class RutinasActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rutinas)

        val prefs = getSharedPreferences("axf_prefs", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        val progressBar   = findViewById<ProgressBar>(R.id.progressRutinas)
        val containerList = findViewById<LinearLayout>(R.id.containerRutinas)
        val tvVacio       = findViewById<TextView>(R.id.tvVacio)
        val btnBack       = findViewById<View>(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        if (token.isEmpty()) {
            tvVacio.text = "Sesión no válida"
            tvVacio.visibility = View.VISIBLE
            return
        }

        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.instance.getRutinas("Bearer $token")
                progressBar.visibility = View.GONE

                if (resp.isSuccessful) {
                    val rutinas = resp.body() ?: emptyList()
                    if (rutinas.isEmpty()) {
                        tvVacio.visibility = View.VISIBLE
                    } else {
                        containerList.removeAllViews()
                        rutinas.forEach { rutina ->
                            containerList.addView(crearTarjetaRutina(rutina))
                        }
                    }
                } else {
                    tvVacio.text = "Error al cargar rutinas"
                    tvVacio.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                tvVacio.text = "Sin conexión al servidor"
                tvVacio.visibility = View.VISIBLE
            }
        }
    }

    private fun crearTarjetaRutina(rutina: RutinaResponse): View {
        val inflater = layoutInflater
        val card = inflater.inflate(R.layout.item_rutina, null) as CardView

        val tvFecha      = card.findViewById<TextView>(R.id.tvRutinaFecha)
        val tvNombre     = card.findViewById<TextView>(R.id.tvRutinaNombre)
        val tvEntrenador = card.findViewById<TextView>(R.id.tvRutinaEntrenador)
        val tvEjercicios = card.findViewById<TextView>(R.id.tvRutinaEjercicios)
        val btnEmpezar   = card.findViewById<View>(R.id.btnEmpezarRutina)

        // Nombre de la rutina (Grupo Muscular) - Formato: "Rutina de [Nombre]"
        tvNombre.text = if (!rutina.nombre.isNullOrBlank()) {
            "Rutina de ${rutina.nombre}"
        } else {
            "Rutina de Entrenamiento"
        }

        // Formatear fecha
        val sdfOut = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formatos = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",     Locale.US),
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss",           Locale.US),
            SimpleDateFormat("yyyy-MM-dd",                    Locale.US)
        )
        tvFecha.text = formatos.firstNotNullOfOrNull { fmt ->
            try { sdfOut.format(fmt.parse(rutina.creado_en)!!) } catch (_: Exception) { null }
        } ?: rutina.creado_en.take(10)

        tvEntrenador.text = "Entrenador: ${rutina.entrenador}"

        // Listar ejercicios resumidos
        val nombresEjercicios = rutina.ejercicios.take(3).joinToString(", ") { it.nombre }
        val extras = if (rutina.ejercicios.size > 3) " +" + (rutina.ejercicios.size - 3) + " más" else ""
        tvEjercicios.text = nombresEjercicios + extras

        btnEmpezar.setOnClickListener {
            val intent = Intent(this, EntrenamientoActivity::class.java)
            intent.putExtra("rutina_id", rutina.id_rutina)
            intent.putExtra("rutina_json", Gson().toJson(rutina))
            startActivity(intent)
        }

        return card
    }
}
