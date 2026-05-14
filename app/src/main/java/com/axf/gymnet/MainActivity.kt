package com.axf.gymnet

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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

    // ── Auto-refresh aforo ───────────────────────────────────────────────────
    private val aforoHandler  = Handler(Looper.getMainLooper())
    private val AFORO_INTERVAL = 30_000L  // 30 segundos

    private val aforoRunnable = object : Runnable {
        override fun run() {
            cargarAforo()
            aforoHandler.postDelayed(this, AFORO_INTERVAL)
        }
    }

    // ── Views de aforo ───────────────────────────────────────────────────────
    private lateinit var tvAforo:       TextView
    private lateinit var tvAforoPct:    TextView
    private lateinit var tvAforoHora:   TextView
    private lateinit var progressAforo: ProgressBar
    private lateinit var btnAforo:      Button

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
        val ivEstadoIcono   = findViewById<ImageView>(R.id.tvEstadoIcono)
        val tvVencimiento   = findViewById<TextView>(R.id.tvVencimiento)
        val tvTipoPlan      = findViewById<TextView>(R.id.tvTipoPlan)
        val barChart        = findViewById<BarChart>(R.id.barChart)
        tvChatBadge         = findViewById(R.id.tvChatBadge)

        // ── Referencias aforo ────────────────────────────────────────────────
        tvAforo       = findViewById(R.id.tvAforo)
        tvAforoPct    = findViewById(R.id.tvAforoPct)
        tvAforoHora   = findViewById(R.id.tvAforoHora)
        progressAforo = findViewById(R.id.progressAforo)
        btnAforo      = findViewById(R.id.btnActualizarAforo)

        // ── Nombre del usuario ───────────────────────────────────────────────
        val nombreGuardado   = prefs.getString("userName",    "") ?: ""
        val apellidoGuardado = prefs.getString("userApellido","") ?: ""
        if (nombreGuardado.isNotEmpty()) {
            tvNombreUsuario.text = "Hola, $nombreGuardado $apellidoGuardado".trim()
        }

        // ── Estado inicial desde prefs ───────────────────────────────────────
        val suscripcionActiva = prefs.getBoolean("suscripcionActiva", false)
        val fechaGuardada     = prefs.getString("fechaVencimiento",  "") ?: ""
        actualizarUIEstado(suscripcionActiva, fechaGuardada, ivEstadoIcono, tvEstado, tvVencimiento, tvDias)

        // ── Nav bar ──────────────────────────────────────────────────────────
        findViewById<View>(R.id.navEntreno).setOnClickListener  { startActivity(Intent(this, RutinasActivity::class.java)) }
        findViewById<View>(R.id.navDieta).setOnClickListener    { startActivity(Intent(this, DietasActivity::class.java)) }
        findViewById<View>(R.id.navReportar).setOnClickListener { startActivity(Intent(this, ReportarActivity::class.java)) }
        findViewById<View>(R.id.navChat).setOnClickListener {
            tvChatBadge.visibility = View.GONE
            startActivity(Intent(this, ChatListaActivity::class.java))
        }

        // ── Cargar suscripción REAL ──────────────────────────────────────────
        if (token.isNotEmpty()) {
            lifecycleScope.launch {
                try {
                    val resp = RetrofitClient.instance.getSuscripcion("Bearer $token")
                    if (resp.isSuccessful) {
                        val data = resp.body()!!
                        actualizarUIEstado(data.activa, data.vencimiento_final ?: "", ivEstadoIcono, tvEstado, tvVencimiento, tvDias)
                        val tipoPlan = data.nombre_plan ?: ""
                        tvTipoPlan.text      = tipoPlan
                        tvTipoPlan.visibility = if (tipoPlan.isNotEmpty()) View.VISIBLE else View.GONE
                        prefs.edit()
                            .putBoolean("suscripcionActiva", data.activa)
                            .putString("fechaVencimiento",  data.vencimiento_final)
                            .apply()
                    }
                } catch (_: Exception) {}
            }
        }

        setupBarChart(barChart)

        // ── Botón manual de aforo ────────────────────────────────────────────
        btnAforo.setOnClickListener { cargarAforo() }

        // ── Cerrar sesión ────────────────────────────────────────────────────
        findViewById<ImageView>(R.id.btnCerrarSesion).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que quieres cerrar sesión?")
                .setPositiveButton("Sí") { _, _ ->
                    prefs.edit().clear().apply()
                    ChatSocketService.stop(this)
                    val intent = Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Cargar aforo desde la API
    // ────────────────────────────────────────────────────────────────────────
    private fun cargarAforo() {
        if (token.isEmpty()) return
        btnAforo.isEnabled = false
        tvAforoHora.text   = "Actualizando..."

        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.instance.getAforo("Bearer $token")
                if (resp.isSuccessful) {
                    val a = resp.body()!!
                    runOnUiThread { actualizarUIAforo(a.personas_dentro, a.capacidad_maxima, a.porcentaje) }
                } else {
                    runOnUiThread { tvAforoHora.text = "Error al obtener aforo (${resp.code()})" }
                }
            } catch (e: Exception) {
                runOnUiThread { tvAforoHora.text = "Sin conexión" }
            } finally {
                runOnUiThread { btnAforo.isEnabled = true }
            }
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Actualizar UI del aforo
    // ────────────────────────────────────────────────────────────────────────
    private fun actualizarUIAforo(dentro: Int, max: Int, pct: Int) {
        tvAforo.text    = "$dentro / $max personas"
        tvAforoPct.text = "$pct%"

        // Color dinámico: verde < 60%, naranja 60-85%, rojo > 85%
        val color = when {
            pct < 60  -> getColor(android.R.color.holo_green_light)
            pct < 85  -> getColor(android.R.color.holo_orange_light)
            else      -> getColor(android.R.color.holo_red_light)
        }
        tvAforoPct.setTextColor(color)
        progressAforo.progressTintList = android.content.res.ColorStateList.valueOf(color)
        progressAforo.progress = pct

        // Hora de actualización
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        tvAforoHora.text = "Última actualización: ${sdf.format(Date())}"
    }

    // ────────────────────────────────────────────────────────────────────────
    // Lifecycle: iniciar/detener auto-refresh
    // ────────────────────────────────────────────────────────────────────────
    override fun onResume() {
        super.onResume()

        // Cargar aforo inmediatamente y programar refresh cada 30s
        aforoHandler.post(aforoRunnable)

        // Badge de chat
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

    override fun onPause() {
        super.onPause()
        aforoHandler.removeCallbacks(aforoRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        aforoHandler.removeCallbacks(aforoRunnable)
    }

    // ────────────────────────────────────────────────────────────────────────
    private fun actualizarUIEstado(
        activa: Boolean, fecha: String,
        icono: ImageView, tvEstado: TextView,
        tvVence: TextView, tvDias: TextView
    ) {
        if (activa) {
            tvEstado.text = "ACTIVA"
            tvEstado.setTextColor(getColor(android.R.color.holo_green_light))
            icono.setImageResource(R.drawable.ic_check_circle)
            icono.setColorFilter(getColor(android.R.color.holo_green_light))
            if (fecha.isNotEmpty()) {
                val fechaCorta = fecha.take(10)
                tvVence.text = "Vence: $fechaCorta"
                try {
                    val sdf   = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
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
            icono.setImageResource(R.drawable.ic_lock)
            icono.setColorFilter(getColor(android.R.color.holo_red_light))
            tvVence.text = "Sin suscripción activa"
            tvDias.text  = "0 Días"
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
            legend.isEnabled      = false
            setDrawGridBackground(false)
            setTouchEnabled(false)
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(horas)
                position       = XAxis.XAxisPosition.BOTTOM
                granularity    = 1f
                setDrawGridLines(false)
                textColor = android.graphics.Color.parseColor("#888888")
                textSize  = 10f
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