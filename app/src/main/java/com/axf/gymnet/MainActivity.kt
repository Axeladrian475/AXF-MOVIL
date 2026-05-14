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
import com.axf.gymnet.data.DescansoRequest
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

    // Auto-refresh aforo
    private val aforoHandler   = Handler(Looper.getMainLooper())
    private val AFORO_INTERVAL = 30_000L

    private val aforoRunnable = object : Runnable {
        override fun run() {
            cargarAforo()
            aforoHandler.postDelayed(this, AFORO_INTERVAL)
        }
    }

    // Views de racha
    private lateinit var tvRachaDias:        TextView
    private lateinit var tvRachaEstado:      TextView
    private lateinit var tvDiasDescanso:     TextView
    private lateinit var tvRachaProgresoMes: TextView
    private lateinit var btnDescansoMenos:   Button
    private lateinit var btnDescansoMas:     Button
    private var diasDescanso: Int = 0

    // Views de aforo
    private lateinit var tvAforo:       TextView
    private lateinit var tvAforoPct:    TextView
    private lateinit var tvAforoHora:   TextView
    private lateinit var progressAforo: ProgressBar
    private lateinit var btnAforo:      Button
    private lateinit var tvPuntos:      TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MyFirebaseMessagingService.crearCanales(this)
        ChatSocketService.start(this)

        val prefs = getSharedPreferences("axf_prefs", MODE_PRIVATE)
        token = prefs.getString("token", "") ?: ""

        // Referencias UI
        val tvNombreUsuario = findViewById<TextView>(R.id.tvNombreUsuario)
        val tvDias          = findViewById<TextView>(R.id.tvDiasRestantes)
        val tvEstado        = findViewById<TextView>(R.id.tvEstadoSuscripcion)
        val ivEstadoIcono   = findViewById<ImageView>(R.id.tvEstadoIcono)
        val tvVencimiento   = findViewById<TextView>(R.id.tvVencimiento)
        val tvTipoPlan      = findViewById<TextView>(R.id.tvTipoPlan)
        val barChart        = findViewById<BarChart>(R.id.barChart)
        tvChatBadge         = findViewById(R.id.tvChatBadge)

        // Referencias racha
        tvRachaDias        = findViewById(R.id.tvRachaDias)
        tvRachaEstado      = findViewById(R.id.tvRachaEstado)
        tvDiasDescanso     = findViewById(R.id.tvDiasDescanso)
        tvRachaProgresoMes = findViewById(R.id.tvRachaProgresoMes)
        btnDescansoMenos = findViewById(R.id.btnDescansoMenos)
        btnDescansoMas   = findViewById(R.id.btnDescansoMas)
        diasDescanso     = prefs.getInt("diasDescanso", 0)
        val rachaSaved   = prefs.getInt("rachaDias", 0)
        tvRachaDias.text    = rachaSaved.toString()
        tvDiasDescanso.text = diasDescanso.toString()
        actualizarEstadoRacha(rachaSaved)
        btnDescansoMenos.setOnClickListener {
            if (diasDescanso > 0) { diasDescanso--; tvDiasDescanso.text = diasDescanso.toString(); guardarDiasDescanso(diasDescanso) }
        }
        btnDescansoMas.setOnClickListener {
            if (diasDescanso < 6) { diasDescanso++; tvDiasDescanso.text = diasDescanso.toString(); guardarDiasDescanso(diasDescanso) }
        }

        // Referencias aforo
        tvAforo       = findViewById(R.id.tvAforo)
        tvAforoPct    = findViewById(R.id.tvAforoPct)
        tvAforoHora   = findViewById(R.id.tvAforoHora)
        progressAforo = findViewById(R.id.progressAforo)
        btnAforo      = findViewById(R.id.btnActualizarAforo)
        tvPuntos      = findViewById(R.id.tvPuntos)

        // Nombre del usuario
        val nombreGuardado   = prefs.getString("userName",    "") ?: ""
        val apellidoGuardado = prefs.getString("userApellido","") ?: ""
        if (nombreGuardado.isNotEmpty()) {
            tvNombreUsuario.text = "Hola, $nombreGuardado $apellidoGuardado".trim()
        }

        // Estado inicial desde prefs
        val suscripcionActiva  = prefs.getBoolean("suscripcionActiva", false)
        val fechaGuardada      = prefs.getString("fechaVencimiento",  "") ?: ""
        tvDias.text = calcularDiasRestantes(fechaGuardada)
        actualizarUIEstado(suscripcionActiva, fechaGuardada, ivEstadoIcono, tvEstado, tvVencimiento)

        // Nav bar
        findViewById<View>(R.id.navEntreno).setOnClickListener  { startActivity(Intent(this, RutinasActivity::class.java)) }
        findViewById<View>(R.id.navDieta).setOnClickListener    { startActivity(Intent(this, DietasActivity::class.java)) }
        findViewById<View>(R.id.navReportar).setOnClickListener { startActivity(Intent(this, ReportarActivity::class.java)) }
        findViewById<View>(R.id.navChat).setOnClickListener {
            tvChatBadge.visibility = View.GONE
            startActivity(Intent(this, ChatListaActivity::class.java))
        }

        // Cargar suscripción REAL
        if (token.isNotEmpty()) {
            lifecycleScope.launch {
                try {
                    val resp = RetrofitClient.instance.getSuscripcion("Bearer $token")
                    if (resp.isSuccessful) {
                        val data = resp.body()!!
                        val fechaVence = data.vencimiento_final ?: ""
                        actualizarUIEstado(data.activa, fechaVence, ivEstadoIcono, tvEstado, tvVencimiento)
                        val tipoPlan = data.nombre_plan ?: ""
                        tvTipoPlan.text       = tipoPlan
                        tvTipoPlan.visibility = if (tipoPlan.isNotEmpty()) View.VISIBLE else View.GONE
                        // Calcular días restantes en el cliente a partir de la fecha
                        val diasText = calcularDiasRestantes(fechaVence)
                        tvDias.text = diasText
                        // Racha
                        val rachaDias = data.racha_dias
                        diasDescanso  = data.dias_descanso_semana
                        tvRachaDias.text    = rachaDias.toString()
                        tvDiasDescanso.text = diasDescanso.toString()
                        actualizarEstadoRacha(rachaDias)
                        // Puntos del suscriptor
                        tvPuntos.text = "${data.puntos} Pts"
                        prefs.edit()
                            .putBoolean("suscripcionActiva", data.activa)
                            .putString("fechaVencimiento",  fechaVence)
                            .putInt("rachaDias",    rachaDias)
                            .putInt("diasDescanso", diasDescanso)
                            .apply()
                    }
                } catch (_: Exception) {}
            }
        }

        setupBarChart(barChart, listOf(0f, 0f, 0f, 0f, 0f, 0f))

        // Botón manual de aforo
        btnAforo.setOnClickListener { cargarAforo() }

        // Cerrar sesión
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

    // Calcula días restantes a partir de "YYYY-MM-DD" en el dispositivo
    private fun calcularDiasRestantes(fecha: String): String {
        if (fecha.isEmpty()) return "-- Días"
        return try {
            val sdf   = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.isLenient = false
            val vence = sdf.parse(fecha.take(10)) ?: return "-- Días"
            val hoy   = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
            }.time
            val dias  = ((vence.time - hoy.time) / (1000L * 60 * 60 * 24)).toInt()
            if (dias >= 0) "$dias Días" else "0 Días"
        } catch (_: Exception) { "-- Días" }
    }

    // Badge dinámico de estado de racha
    private fun actualizarEstadoRacha(dias: Int) {
        val (texto, color) = when {
            dias >= 30 -> Pair("🏆 ¡Leyenda!",   getColor(android.R.color.holo_orange_light))
            dias >= 14 -> Pair("🔥 ¡En racha!",  getColor(android.R.color.holo_orange_light))
            dias >= 7  -> Pair("💪 ¡Constante!", getColor(android.R.color.holo_green_light))
            dias >= 1  -> Pair("⚡ Iniciando",   getColor(android.R.color.holo_blue_light))
            else       -> Pair("Sin racha",      getColor(R.color.axf_text_secondary))
        }
        tvRachaEstado.text = texto
        tvRachaEstado.setTextColor(color)

        // Progreso hacia el bonus mensual
        val diasEnMes = dias % 30
        val restantesMes = 30 - diasEnMes
        tvRachaProgresoMes.text = if (diasEnMes == 0 && dias > 0) {
            "🏆 ¡Bonus de +30 pts desbloqueado!"
        } else {
            "${diasEnMes}/30 días — faltan $restantesMes para bonus +30 pts"
        }
    }

    // Sincroniza días de descanso con el servidor
    private fun guardarDiasDescanso(dias: Int) {
        getSharedPreferences("axf_prefs", MODE_PRIVATE).edit().putInt("diasDescanso", dias).apply()
        if (token.isEmpty()) return
        lifecycleScope.launch {
            try { RetrofitClient.instance.actualizarDescanso("Bearer $token", DescansoRequest(dias)) }
            catch (_: Exception) {}
        }
    }

    // Cargar aforo desde la API
    private fun cargarAforo() {
        if (token.isEmpty()) return
        btnAforo.isEnabled = false
        tvAforoHora.text   = "Actualizando..."

        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.instance.getAforo("Bearer $token")
                if (resp.isSuccessful) {
                    val a = resp.body()!!
                    val graficaFloats = a.grafica?.map { it.toFloat() } ?: listOf(0f, 0f, 0f, 0f, 0f, 0f)
                    runOnUiThread { 
                        actualizarUIAforo(a.personas_dentro, a.capacidad_maxima, a.porcentaje) 
                        setupBarChart(findViewById(R.id.barChart), graficaFloats)
                    }
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

    // Actualizar UI del aforo
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

        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        tvAforoHora.text = "Última actualización: ${sdf.format(Date())}"
    }

    // Lifecycle: iniciar/detener auto-refresh
    override fun onResume() {
        super.onResume()
        aforoHandler.post(aforoRunnable)
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

    private fun actualizarUIEstado(
        activa: Boolean, fecha: String,
        icono: ImageView, tvEstado: TextView,
        tvVence: TextView
    ) {
        if (activa) {
            tvEstado.text = "ACTIVA"
            tvEstado.setTextColor(getColor(android.R.color.holo_green_light))
            icono.setImageResource(R.drawable.ic_check_circle)
            icono.setColorFilter(getColor(android.R.color.holo_green_light))
            if (fecha.isNotEmpty()) tvVence.text = "Vence: ${fecha.take(10)}"
        } else {
            tvEstado.text = "INACTIVA"
            tvEstado.setTextColor(getColor(android.R.color.holo_red_light))
            icono.setImageResource(R.drawable.ic_lock)
            icono.setColorFilter(getColor(android.R.color.holo_red_light))
            tvVence.text = "Sin suscripción activa"
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

    private fun setupBarChart(chart: BarChart, valores: List<Float>) {
        val horas   = listOf("6am", "9am", "12pm", "6pm", "8pm", "10pm")
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