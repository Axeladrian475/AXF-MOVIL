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

    private lateinit var tvChatBadge: TextView
    private var token: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MyFirebaseMessagingService.crearCanales(this)
        ChatSocketService.start(this)

        val prefs = getSharedPreferences("axf_prefs", MODE_PRIVATE)
        token = prefs.getString("token", "") ?: ""

        // ── Referencias UI ───────────────────────────────────────────────────
        val tvNombreUsuario = findViewById<TextView>(R.id.tvNombreUsuario)
        val tvDias          = findViewById<TextView>(R.id.tvDiasRestantes)
        val tvEstado        = findViewById<TextView>(R.id.tvEstadoSuscripcion)
        val tvEstadoIcono   = findViewById<TextView>(R.id.tvEstadoIcono)
        val tvVencimiento   = findViewById<TextView>(R.id.tvVencimiento)
        val tvTipoPlan      = findViewById<TextView>(R.id.tvTipoPlan)
        val tvAforo         = findViewById<TextView>(R.id.tvAforo)
        val btnAforo        = findViewById<Button>(R.id.btnActualizarAforo)
        val barChart        = findViewById<BarChart>(R.id.barChart)
        tvChatBadge         = findViewById(R.id.tvChatBadge)

        // ── Mostrar nombre guardado en prefs inmediatamente ──────────────────
        val nombreGuardado = prefs.getString("userName", "") ?: ""
        val apellidoGuardado = prefs.getString("userApellido", "") ?: ""
        if (nombreGuardado.isNotEmpty()) {
            tvNombreUsuario.text = "Hola, $nombreGuardado $apellidoGuardado".trim()
        }

        // ── Estado inicial desde prefs ───────────────────────────────────────
        val suscripcionActiva = prefs.getBoolean("suscripcionActiva", false)
        val fechaGuardada = prefs.getString("fechaVencimiento", "") ?: ""

        if (suscripcionActiva) {
            tvEstado.text = "ACTIVA"
            tvEstado.setTextColor(getColor(android.R.color.holo_green_light))
            tvEstadoIcono.text = "✅"
            if (fechaGuardada.isNotEmpty()) {
                val fechaCorta = fechaGuardada.take(10)
                tvVencimiento.text = "Vence: $fechaCorta"
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val vence = sdf.parse(fechaCorta)
                    if (vence != null) {
                        val dias = ((vence.time - Date().time) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
                        tvDias.text = "$dias Días"
                    }
                } catch (_: Exception) {}
            }
        } else {
            tvEstado.text = "INACTIVA"
            tvEstado.setTextColor(getColor(android.R.color.holo_red_light))
            tvEstadoIcono.text = "❌"
            tvVencimiento.text = "Sin suscripción activa"
            tvDias.text = "0 Días"
        }

        // ── Nav bar ──────────────────────────────────────────────────────────
        val navEntreno  = findViewById<View>(R.id.navEntreno)
        val navDieta    = findViewById<View>(R.id.navDieta)
        val navReportar = findViewById<View>(R.id.navReportar)
        val navChat     = findViewById<View>(R.id.navChat)

        navEntreno.setOnClickListener  { startActivity(Intent(this, RutinasActivity::class.java)) }
        navDieta.setOnClickListener    { startActivity(Intent(this, DietasActivity::class.java)) }
        navReportar.setOnClickListener { startActivity(Intent(this, ReportarActivity::class.java)) }
        navChat.setOnClickListener {
            tvChatBadge.visibility = View.GONE
            startActivity(Intent(this, ChatListaActivity::class.java))
        }

        // ── Cargar suscripción REAL desde la API ─────────────────────────────
        if (token.isNotEmpty()) {
            lifecycleScope.launch {
                try {
                    val resp = RetrofitClient.instance.getSuscripcion("Bearer $token")
                    if (resp.isSuccessful) {
                        val data = resp.body()!!
                        if (data.activa && data.vencimiento_final != null) {
                            val fechaStr = data.vencimiento_final.take(10)
                            try {
                                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val vence = sdf.parse(fechaStr)
                                if (vence != null) {
                                    val dias = ((vence.time - Date().time) / (1000 * 60 * 60 * 24))
                                        .toInt().coerceAtLeast(0)
                                    tvDias.text = "$dias Días"
                                }
                            } catch (_: Exception) {}
                            tvVencimiento.text = "Vence: $fechaStr"
                            tvEstado.text = "ACTIVA"
                            tvEstado.setTextColor(getColor(android.R.color.holo_green_light))
                            tvEstadoIcono.text = "✅"

                            // Mostrar tipo de plan si viene en la respuesta
                            val tipoPlan = data.nombre_plan ?: ""
                            tvTipoPlan.text = tipoPlan
                            tvTipoPlan.visibility = if (tipoPlan.isNotEmpty()) View.VISIBLE else View.GONE

                            // Actualizar prefs con datos frescos
                            getSharedPreferences("axf_prefs", MODE_PRIVATE).edit()
                                .putBoolean("suscripcionActiva", true)
                                .putString("fechaVencimiento", data.vencimiento_final)
                                .apply()
                        } else {
                            tvEstado.text = "INACTIVA"
                            tvEstado.setTextColor(getColor(android.R.color.holo_red_light))
                            tvEstadoIcono.text = "❌"
                            tvVencimiento.text = "Sin suscripción activa"
                            tvDias.text = "0 Días"
                            tvTipoPlan.visibility = View.GONE
                        }
                    }
                } catch (_: Exception) {}
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

    override fun onResume() {
        super.onResume()
        if (token.isNotEmpty()) {
            lifecycleScope.launch {
                try {
                    val resp = RetrofitClient.instance.getNoLeidos("Bearer $token")
                    if (resp.isSuccessful) {
                        actualizarBadge(resp.body()?.no_leidos ?: 0)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    private fun actualizarBadge(count: Int) {
        runOnUiThread {
            if (count > 0) {
                tvChatBadge.visibility = View.VISIBLE
                tvChatBadge.text = if (count > 99) "99+" else count.toString()
            } else {
                tvChatBadge.visibility = View.GONE
            }
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
