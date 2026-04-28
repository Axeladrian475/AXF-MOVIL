package com.axf.gymnet

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.axf.gymnet.data.EjercicioRutina
import com.axf.gymnet.data.GuardarSerieRequest
import com.axf.gymnet.data.RutinaResponse
import com.axf.gymnet.network.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.util.*

class EntrenamientoActivity : AppCompatActivity() {

    // ── Estado del entrenamiento ──────────────────────────────────────────────
    private lateinit var rutina: RutinaResponse
    private var timerEntrenamiento: CountDownTimer? = null
    private var timerDescanso: CountDownTimer? = null
    private var segundosTotales = 0L
    private var token = ""

    // Mapa: id_rutina_ejercicio -> List<SerieState>
    data class SerieState(
        var pesoKg: Double,
        var reps: Int,
        var completada: Boolean = false,
        var guardada: Boolean = false
    )
    private val seriesState = mutableMapOf<Int, MutableList<SerieState>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entrenamiento)

        val prefs = getSharedPreferences("axf_prefs", MODE_PRIVATE)
        token = prefs.getString("token", "") ?: ""

        // Deserializar rutina
        val rutinaJson = intent.getStringExtra("rutina_json") ?: run { finish(); return }
        rutina = Gson().fromJson(rutinaJson, RutinaResponse::class.java)

        // Inicializar estado de series con los valores predefinidos
        rutina.ejercicios.forEach { ej ->
            val series = (1..ej.series).map {
                SerieState(
                    pesoKg = ej.peso_kg ?: 0.0,
                    reps   = ej.repeticiones
                )
            }.toMutableList()
            seriesState[ej.id_rutina_ejercicio] = series
        }

        iniciarTimerEntrenamiento()
        construirUI()

        // Botón terminar
        findViewById<Button>(R.id.btnTerminar).setOnClickListener { confirmarTerminar() }

        // Botón descartar
        findViewById<Button>(R.id.btnDescartar).setOnClickListener { confirmarDescartar() }
    }

    // ── Timer de duración total ───────────────────────────────────────────────
    private fun iniciarTimerEntrenamiento() {
        val tvDuracion = findViewById<TextView>(R.id.tvDuracion)
        timerEntrenamiento = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                segundosTotales++
                val min = segundosTotales / 60
                val seg = segundosTotales % 60
                tvDuracion.text = if (min > 0) "${min}m ${seg}s" else "${seg}s"
            }
            override fun onFinish() {}
        }.start()
    }

    // ── Construir la lista de ejercicios ─────────────────────────────────────
    private fun construirUI() {
        val container = findViewById<LinearLayout>(R.id.containerEjercicios)
        container.removeAllViews()

        rutina.ejercicios.sortedBy { it.orden }.forEach { ejercicio ->
            container.addView(crearTarjetaEjercicio(ejercicio))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun crearTarjetaEjercicio(ej: EjercicioRutina): View {
        val v = LayoutInflater.from(this).inflate(R.layout.item_ejercicio_entreno, null)

        v.findViewById<TextView>(R.id.tvNombreEjercicio).text = ej.nombre

        // Notas técnicas
        val tvNotas = v.findViewById<TextView>(R.id.tvNotasTecnicas)
        if (!ej.descripcion_tecnica.isNullOrBlank()) {
            tvNotas.text = ej.descripcion_tecnica
            tvNotas.visibility = View.VISIBLE
        }

        // Descanso
        val tvDescanso = v.findViewById<TextView>(R.id.tvDescanso)
        tvDescanso.text = when {
            ej.descanso_seg == null || ej.descanso_seg == 0 -> "Descanso: APAGADO"
            ej.descanso_seg < 60 -> "Descanso: ${ej.descanso_seg}s"
            else -> {
                val min = ej.descanso_seg / 60
                val seg = ej.descanso_seg % 60
                if (seg == 0) "Descanso: ${min}min" else "Descanso: ${min}min ${seg}s"
            }
        }

        // Tabla de series
        val tablaSeries = v.findViewById<LinearLayout>(R.id.tablaSeries)
        val series = seriesState[ej.id_rutina_ejercicio] ?: return v

        series.forEachIndexed { idx, serie ->
            tablaSeries.addView(crearFilaSerie(ej, idx, serie, tablaSeries))
        }

        // Botón agregar serie
        v.findViewById<Button>(R.id.btnAgregarSerie).setOnClickListener {
            val ultima = series.last()
            series.add(SerieState(pesoKg = ultima.pesoKg, reps = ultima.reps))
            tablaSeries.removeAllViews()
            series.forEachIndexed { idx, s ->
                tablaSeries.addView(crearFilaSerie(ej, idx, s, tablaSeries))
            }
            actualizarTotales()
        }

        return v
    }

    @SuppressLint("SetTextI18n")
    private fun crearFilaSerie(
        ej: EjercicioRutina,
        idx: Int,
        serie: SerieState,
        tabla: LinearLayout
    ): View {
        val fila = LayoutInflater.from(this).inflate(R.layout.item_serie_fila, null)

        val tvNumero  = fila.findViewById<TextView>(R.id.tvSerieNum)
        val tvAnterior = fila.findViewById<TextView>(R.id.tvAnterior)
        val etPeso    = fila.findViewById<EditText>(R.id.etPesoSerie)
        val etReps    = fila.findViewById<EditText>(R.id.etRepsSerie)
        val btnCheck  = fila.findViewById<ImageButton>(R.id.btnCheckSerie)

        tvNumero.text = "${idx + 1}"

        // "Anterior": el valor predefinido de la rutina
        val pesoBase = if (ej.peso_kg != null && ej.peso_kg > 0) "${ej.peso_kg.toInt()}kg" else "--"
        tvAnterior.text = "$pesoBase x ${ej.repeticiones}"

        etPeso.setText(if (serie.pesoKg > 0) serie.pesoKg.toInt().toString() else "")
        etReps.setText(serie.reps.toString())

        // Estado visual
        actualizarEstadoFila(fila, serie.completada)

        // Guardar cambios en el estado mientras se edita
        etPeso.setOnFocusChangeListener { _, _ ->
            serie.pesoKg = etPeso.text.toString().toDoubleOrNull() ?: 0.0
        }
        etReps.setOnFocusChangeListener { _, _ ->
            serie.reps = etReps.text.toString().toIntOrNull() ?: 0
        }

        btnCheck.setOnClickListener {
            serie.pesoKg = etPeso.text.toString().toDoubleOrNull() ?: 0.0
            serie.reps   = etReps.text.toString().toIntOrNull() ?: 0
            serie.completada = !serie.completada

            actualizarEstadoFila(fila, serie.completada)
            actualizarTotales()

            // Guardar en backend
            if (serie.completada && !serie.guardada) {
                guardarSerieBackend(ej.id_rutina_ejercicio, idx + 1, serie)
                serie.guardada = true
            }

            // Iniciar timer de descanso si aplica
            if (serie.completada && ej.descanso_seg != null && ej.descanso_seg > 0) {
                iniciarDescanso(ej.descanso_seg)
            }
        }

        return fila
    }

    private fun actualizarEstadoFila(fila: View, completada: Boolean) {
        val btnCheck = fila.findViewById<ImageButton>(R.id.btnCheckSerie)
        val etPeso   = fila.findViewById<EditText>(R.id.etPesoSerie)
        val etReps   = fila.findViewById<EditText>(R.id.etRepsSerie)

        if (completada) {
            fila.alpha = 0.6f
            btnCheck.setImageResource(R.drawable.ic_check_filled)
            etPeso.isEnabled  = false
            etReps.isEnabled  = false
        } else {
            fila.alpha = 1f
            btnCheck.setImageResource(R.drawable.ic_check_empty)
            etPeso.isEnabled  = true
            etReps.isEnabled  = true
        }
    }

    // ── Totales en el header ──────────────────────────────────────────────────
    private fun actualizarTotales() {
        var volumenTotal = 0.0
        var seriesCompletadas = 0

        seriesState.forEach { (_, series) ->
            series.forEach { s ->
                if (s.completada) {
                    volumenTotal += s.pesoKg * s.reps
                    seriesCompletadas++
                }
            }
        }

        findViewById<TextView>(R.id.tvVolumen).text = "${volumenTotal.toInt()} kg"
        findViewById<TextView>(R.id.tvSeriesTotal).text = "$seriesCompletadas"
    }

    // ── Guardar serie en backend ──────────────────────────────────────────────
    private fun guardarSerieBackend(idEjercicio: Int, numSerie: Int, serie: SerieState) {
        lifecycleScope.launch {
            try {
                RetrofitClient.instance.guardarSerie(
                    "Bearer $token",
                    GuardarSerieRequest(
                        id_rutina_ejercicio = idEjercicio,
                        num_serie           = numSerie,
                        peso_levantado      = serie.pesoKg,
                        reps_realizadas     = serie.reps
                    )
                )
            } catch (_: Exception) { /* silencioso — no interrumpir el flujo */ }
        }
    }

    // ── Timer de descanso ─────────────────────────────────────────────────────
    private fun iniciarDescanso(segundos: Int) {
        timerDescanso?.cancel()

        val dialogo = AlertDialog.Builder(this, R.style.DialogDescanso)
            .setView(R.layout.dialog_descanso)
            .setCancelable(true)
            .create()

        dialogo.show()

        val tvCuenta   = dialogo.findViewById<TextView>(R.id.tvCuentaAtras)
        val btnSaltar  = dialogo.findViewById<Button>(R.id.btnSaltarDescanso)
        val progressBar = dialogo.findViewById<ProgressBar>(R.id.progressDescanso)

        progressBar?.max = segundos

        timerDescanso = object : CountDownTimer(segundos * 1000L, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millis: Long) {
                val restantes = (millis / 1000).toInt()
                val min = restantes / 60
                val seg = restantes % 60
                tvCuenta?.text = if (min > 0) "${min}:${seg.toString().padStart(2,'0')}" else "$seg"
                progressBar?.progress = restantes
            }
            override fun onFinish() {
                if (dialogo.isShowing) dialogo.dismiss()
            }
        }.start()

        btnSaltar?.setOnClickListener {
            timerDescanso?.cancel()
            dialogo.dismiss()
        }
    }

    // ── Confirmar terminar ────────────────────────────────────────────────────
    private fun confirmarTerminar() {
        val seriesCompletadas = seriesState.values.flatten().count { it.completada }
        AlertDialog.Builder(this)
            .setTitle("Terminar Entrenamiento")
            .setMessage("Has completado $seriesCompletadas series.\n¿Deseas terminar el entrenamiento?")
            .setPositiveButton("Terminar") { _, _ ->
                timerEntrenamiento?.cancel()
                timerDescanso?.cancel()
                Toast.makeText(this, "¡Entrenamiento completado! 💪", Toast.LENGTH_LONG).show()
                finish()
            }
            .setNegativeButton("Continuar", null)
            .show()
    }

    private fun confirmarDescartar() {
        AlertDialog.Builder(this)
            .setTitle("Descartar Entrenamiento")
            .setMessage("¿Estás seguro? Se perderá el progreso no guardado.")
            .setPositiveButton("Descartar") { _, _ ->
                timerEntrenamiento?.cancel()
                timerDescanso?.cancel()
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerEntrenamiento?.cancel()
        timerDescanso?.cancel()
    }
}