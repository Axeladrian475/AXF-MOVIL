package com.axf.gymnet

import android.content.Intent
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

        val prefs = getSharedPreferences("axf_prefs", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""
        val suscripcionActiva = prefs.getBoolean("suscripcionActiva", false)

        // Referencias UI
        val tvDias        = findViewById<TextView>(R.id.tvDiasRestantes)
        val tvEstado      = findViewById<TextView>(R.id.tvEstadoSuscripcion)
        val tvEstadoIcono = findViewById<TextView>(R.id.tvEstadoIcono)
        val tvVencimiento = findViewById<TextView>(R.id.tvVencimiento)
        val tvAforo       = findViewById<TextView>(R.id.tvAforo)
        val btnAforo      = findViewById<Button>(R.id.btnActualizarAforo)
        val barChart      = findViewById<BarChart>(R.id.barChart)

        // ── Nav bar ──────────────────────────────────────────────────────────
        val navEntreno  = findViewById<View>(R.id.navEntreno)
        val navDieta    = findViewById<View>(R.id.navDieta)
        val navReportar = findViewById<View>(R.id.navReportar)

        // ✅ Botón Entreno → abre lista de rutinas
        navEntreno.setOnClickListener {
            startActivity(Intent(this, RutinasActivity::class.java))
        }
        navDieta.setOnClickListener {
            android.widget.Toast.makeText(this, "Próximamente: Dieta", android.widget.Toast.LENGTH_SHORT).show()
        }
        navReportar.setOnClickListener {
            android.widget.Toast.makeText(this, "Próximamente: Reportar", android.widget.Toast.LENGTH_SHORT).show()
        }

        val navChat = findViewById<View>(R.id.navChat)
        navChat.setOnClickListener {
            startActivity(Intent(this, ChatListaActivity::class.java))
        }

        // ── Estado inicial desde prefs ───────────────────────────────────────
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

        // ── Cargar suscripción real ──────────────────────────────────────────
        if (token.isNotEmpty()) {
            lifecycleScope.launch {
                try {
                    val resp = RetrofitClient.instance.getSuscripcion("Bearer $token")
                    if (resp.isSuccessful) {
                        val data = resp.body()!!
                        if (data.activa && data.vencimiento_final != null) {
                            val fechaStr = data.vencimiento_final.take(10)
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val vence = sdf.parse(fechaStr)
                            if (vence != null) {
                                val dias = ((vence.time - Date().time) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
                                tvDias.text = "$dias Días"
                                tvVencimiento.text = "Vence: $fechaStr"
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
                } catch (_: Exception) { }
            }
        }

        setupBarChart(barChart)

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
            colors = valores.map { v ->
                if (v >= 60f) resources.getColor(android.R.color.holo_orange_dark, theme)
                else android.graphics.Color.parseColor("#CCCCCC")
            }
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
            axisLeft.apply { setDrawGridLines(false); setDrawLabels(false); axisMinimum = 0f }
            axisRight.isEnabled = false
            animateY(800)
            invalidate()
        }
    }
}