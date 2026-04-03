package com.axf.gymnet

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.axf.gymnet.network.RetrofitClient
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Recuperar sesión
        val prefs = getSharedPreferences("axf_prefs", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""
        val suscripcionActiva = prefs.getBoolean("suscripcionActiva", false)

        // Referencias UI
        val tvDias      = findViewById<TextView>(R.id.tvDiasRestantes)
        val tvEstado    = findViewById<TextView>(R.id.tvEstadoSuscripcion)
        val tvEstadoIcono = findViewById<TextView>(R.id.tvEstadoIcono)
        val tvVencimiento = findViewById<TextView>(R.id.tvVencimiento)
        val tvPuntos    = findViewById<TextView>(R.id.tvPuntos)
        val tvAforo     = findViewById<TextView>(R.id.tvAforo)
        val btnAforo    = findViewById<Button>(R.id.btnActualizarAforo)
        val barChart    = findViewById<BarChart>(R.id.barChart)

        // Mostrar estado inicial rápido desde prefs
        if (suscripcionActiva) {
            tvEstado.text = "ACTIVA"
            tvEstado.setTextColor(getColor(android.R.color.holo_green_light))
            tvEstadoIcono.text = "✅"
        } else {
            tvEstado.text = "INACTIVA"
            tvEstado.setTextColor(getColor(android.R.color.holo_red_light))
            tvEstadoIcono.text = "❌"
            tvVencimiento.text = "Sin suscripción activa"
        }

        // ✅ CORREGIDO: El endpoint móvil no requiere id en la URL.
        // Usa el token para identificar al suscriptor en el backend.
        if (token.isNotEmpty()) {
            lifecycleScope.launch {
                try {
                    val resp = RetrofitClient.instance.getSuscripcion("Bearer $token")
                    if (resp.isSuccessful) {
                        val data = resp.body()!!
                        if (data.activa && data.vencimiento_final != null) {
                            // ✅ CORREGIDO: Parseo de fecha robusto.
                            // El backend puede devolver "2025-12-31" o "2025-12-31T06:00:00.000Z".
                            // Se normaliza tomando solo los primeros 10 caracteres (yyyy-MM-dd).
                            val fechaStr = data.vencimiento_final.take(10)
                            val sdf  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val hoy  = Date()
                            val vence = sdf.parse(fechaStr)

                            if (vence != null) {
                                val diff = vence.time - hoy.time
                                val dias = (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
                                tvDias.text = "$dias Días"
                                tvVencimiento.text = "Vence: $fechaStr"
                            } else {
                                tvDias.text = "-- Días"
                                tvVencimiento.text = "Vence: ${data.vencimiento_final}"
                            }

                            tvEstado.text = "ACTIVA"
                            tvEstado.setTextColor(getColor(android.R.color.holo_green_light))
                            tvEstadoIcono.text = "✅"

                        } else {
                            tvEstado.text = "INACTIVA"
                            tvEstado.setTextColor(getColor(android.R.color.holo_red_light))
                            tvEstadoIcono.text = "❌"
                            tvVencimiento.text = "Sin suscripción activa"
                            tvDias.text = "0 Días"
                        }
                    }
                } catch (e: Exception) {
                    // Si falla la red, los datos de prefs ya están mostrados
                }
            }
        }

        // Gráfica placeholder de horarios pico
        setupBarChart(barChart)

        // Botón aforo (placeholder hasta tener backend)
        btnAforo.setOnClickListener {
            btnAforo.isEnabled = false
            tvAforo.visibility = View.VISIBLE
            tvAforo.text = "Cargando aforo..."
            android.os.Handler(mainLooper).postDelayed({
                tvAforo.text = "Aforo actual: próximamente disponible"
                btnAforo.isEnabled = true
            }, 1000)
        }
    }

    private fun setupBarChart(chart: BarChart) {
        val horas   = listOf("6am", "9am", "12pm", "6pm", "8pm", "10pm")
        val valores = listOf(40f, 25f, 30f, 55f, 80f, 20f)

        val entries = valores.mapIndexed { i, v -> BarEntry(i.toFloat(), v) }
        val dataSet = BarDataSet(entries, "").apply {
            val colores = valores.map { v ->
                if (v >= 60f)
                    resources.getColor(android.R.color.holo_orange_dark, theme)
                else
                    android.graphics.Color.parseColor("#CCCCCC")
            }
            colors = colores
            setDrawValues(false)
        }

        chart.apply {
            data = BarData(dataSet).apply { barWidth = 0.6f }
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setTouchEnabled(false)

            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(horas)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                textColor = android.graphics.Color.parseColor("#888888")
                textSize = 10f
            }
            axisLeft.apply {
                setDrawGridLines(false)
                setDrawLabels(false)
                axisMinimum = 0f
            }
            axisRight.isEnabled = false
            animateY(800)
            invalidate()
        }
    }
}