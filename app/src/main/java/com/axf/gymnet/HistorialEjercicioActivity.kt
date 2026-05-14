package com.axf.gymnet

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.axf.gymnet.network.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class HistorialEjercicioActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_ejercicio)

        val token             = getSharedPreferences("axf_prefs", MODE_PRIVATE)
            .getString("token", "") ?: ""
        val idRutinaEjercicio = intent.getIntExtra("id_rutina_ejercicio", -1)

        val tvBack        = findViewById<TextView>(R.id.btnHistorialBack)
        val tvNombre      = findViewById<TextView>(R.id.tvHistorialNombreEjercicio)
        val tvMusculo     = findViewById<TextView>(R.id.tvHistorialGrupoMuscular)
        val tvPrPeso      = findViewById<TextView>(R.id.tvPrPeso)
        val tvSesiones    = findViewById<TextView>(R.id.tvTotalSesiones)
        val tvPrVolumen   = findViewById<TextView>(R.id.tvPrVolumen)
        val progress      = findViewById<ProgressBar>(R.id.progressHistorial)
        val container     = findViewById<LinearLayout>(R.id.containerSesiones)

        tvBack.setOnClickListener { finish() }

        if (idRutinaEjercicio == -1 || token.isEmpty()) { finish(); return }

        progress.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.instance.getHistorialEjercicio(
                    "Bearer $token", idRutinaEjercicio
                )
                progress.visibility = View.GONE

                if (!resp.isSuccessful) {
                    Toast.makeText(this@HistorialEjercicioActivity,
                        "Error al cargar historial (${resp.code()})", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val data = resp.body()!!

                // Header
                tvNombre.text  = data.ejercicio.nombre_ejercicio
                tvMusculo.text = data.ejercicio.grupo_muscular?.replaceFirstChar { it.uppercase() } ?: ""

                // PRs
                tvPrPeso.text    = if (data.pr_peso_kg > 0) "${data.pr_peso_kg.toInt()} kg" else "-- kg"
                tvSesiones.text  = data.total_sesiones.toString()
                tvPrVolumen.text = if (data.pr_volumen > 0) "${data.pr_volumen.toInt()} kg" else "-- kg"

                // Sesiones
                container.removeAllViews()

                if (data.sesiones.isEmpty()) {
                    val tvVacio = TextView(this@HistorialEjercicioActivity).apply {
                        text = "Aún no tienes sesiones registradas para este ejercicio.\n\n¡Completa una serie en tu entrenamiento y aparecerá aquí! 💪"
                        textSize = 14f
                        gravity = Gravity.CENTER
                        setTextColor(Color.parseColor("#99AABB"))
                        setPadding(32, 64, 32, 32)
                    }
                    container.addView(tvVacio)
                    return@launch
                }

                val sdfIn  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val sdfOut = SimpleDateFormat("d 'de' MMMM yyyy", Locale("es", "MX"))

                for (sesion in data.sesiones) {
                    // ── Tarjeta de sesión ─────────────────────────────────────
                    val card = LinearLayout(this@HistorialEjercicioActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        setBackgroundColor(Color.parseColor("#1A2535"))
                        val lp = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { bottomMargin = 16 }
                        layoutParams = lp
                        setPadding(0, 0, 0, 16)
                    }

                    // Fecha de sesión
                    val tvFecha = TextView(this@HistorialEjercicioActivity).apply {
                        text = try {
                            "📅  ${sdfOut.format(sdfIn.parse(sesion.fecha)!!)}"
                        } catch (_: Exception) { sesion.fecha }
                        textSize  = 13f
                        setTextColor(Color.WHITE)
                        setBackgroundColor(Color.parseColor("#E87722"))
                        setPadding(24, 14, 24, 14)
                        setTypeface(null, android.graphics.Typeface.BOLD)
                    }
                    card.addView(tvFecha)

                    // Calcular volumen de la sesión
                    val volSesion = sesion.series.sumOf {
                        (it.peso_levantado ?: 0.0) * (it.reps_realizadas ?: 0)
                    }

                    // Series de la sesión
                    for (serie in sesion.series) {
                        val fila = LinearLayout(this@HistorialEjercicioActivity).apply {
                            orientation = LinearLayout.HORIZONTAL
                            gravity = android.view.Gravity.CENTER_VERTICAL
                            setPadding(24, 10, 24, 4)
                        }

                        // Número de serie
                        val tvNum = TextView(this@HistorialEjercicioActivity).apply {
                            text  = "Serie ${serie.num_serie}"
                            textSize = 12f
                            setTextColor(Color.parseColor("#E87722"))
                            setTypeface(null, android.graphics.Typeface.BOLD)
                            layoutParams = LinearLayout.LayoutParams(0,
                                LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        }

                        // Peso × Reps
                        val peso = if ((serie.peso_levantado ?: 0.0) > 0)
                            "${serie.peso_levantado!!.toInt()} kg" else "--"
                        val reps = serie.reps_realizadas?.toString() ?: "--"

                        val tvDatos = TextView(this@HistorialEjercicioActivity).apply {
                            text     = "$peso  ×  $reps reps"
                            textSize = 13f
                            setTextColor(Color.WHITE)
                            setTypeface(null, android.graphics.Typeface.BOLD)
                        }

                        fila.addView(tvNum)
                        fila.addView(tvDatos)
                        card.addView(fila)
                    }

                    // Volumen total de la sesión
                    if (volSesion > 0) {
                        val tvVol = TextView(this@HistorialEjercicioActivity).apply {
                            text     = "Volumen total: ${volSesion.toInt()} kg"
                            textSize = 11f
                            setTextColor(Color.parseColor("#99AABB"))
                            setPadding(24, 6, 24, 0)
                        }
                        card.addView(tvVol)
                    }

                    container.addView(card)
                }

            } catch (e: Exception) {
                progress.visibility = View.GONE
                Toast.makeText(this@HistorialEjercicioActivity,
                    "Sin conexión: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
