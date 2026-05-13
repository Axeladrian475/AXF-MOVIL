package com.axf.gymnet

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.axf.gymnet.data.EjercicioRutina
import com.axf.gymnet.data.RutinaResponse
import com.axf.gymnet.network.RetrofitClient
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.gson.Gson
import kotlinx.coroutines.launch

/**
 * RutinasActivity — Lista de rutinas agrupadas por nombre de día/bloque.
 *
 * Cambios respecto a versión anterior:
 * - El header muestra el NOMBRE de la rutina (ej: "Pecho", "Piernas") NO "RUTINA #16"
 * - El sub-label del header dice "RUTINA DEL DÍA" en lugar de "GRUPO MUSCULAR"
 * - El botón "▶ Empezar Rutina Completa" está UNA VEZ en el header de cada bloque,
 *   NO en cada ejercicio individual
 * - Los ejercicios solo muestran info (imagen, nombre, series×reps, descanso, peso)
 *   sin botón propio
 */
class RutinasActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rutinas)

        val token = getSharedPreferences("axf_prefs", MODE_PRIVATE)
            .getString("token", "") ?: ""

        val progressBar   = findViewById<ProgressBar>(R.id.progressRutinas)
        val containerList = findViewById<LinearLayout>(R.id.containerRutinas)
        val tvVacio       = findViewById<TextView>(R.id.tvVacio)
        val layoutVacio   = findViewById<LinearLayout>(R.id.layoutVacio)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        if (token.isEmpty()) {
            tvVacio.text = "Sesión no válida"
            layoutVacio.visibility = View.VISIBLE
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
                        layoutVacio.visibility = View.VISIBLE
                    } else {
                        containerList.removeAllViews()
                        renderizarRutinas(containerList, rutinas)
                    }
                } else {
                    tvVacio.text = "Error al cargar rutinas (${resp.code()})"
                    layoutVacio.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                tvVacio.text = "Sin conexión: ${e.message}"
                layoutVacio.visibility = View.VISIBLE
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Renderizado: una sección por rutina (un día/bloque)
    // Header con nombre del día + botón ÚNICO de empezar
    // Ejercicios solo informativos, sin botón individual
    // ─────────────────────────────────────────────────────────────────────────
    private fun renderizarRutinas(
        container: LinearLayout,
        rutinas: List<RutinaResponse>
    ) {
        val inflater = layoutInflater
        val baseUrl  = RetrofitClient.BASE_URL.trimEnd('/')

        for (rutina in rutinas) {
            if (rutina.ejercicios.isEmpty()) continue

            // ── Header del bloque: nombre del día + botón empezar ────────────
            val header = inflater.inflate(R.layout.item_rutina_header, container, false)

            // Nombre de la rutina (ej: "Pecho", "Piernas", "Brazos") — nunca "RUTINA #16"
            val nombreRutina = rutina.nombre
                ?.takeIf { it.isNotBlank() }
                ?.uppercase()
                ?: "RUTINA ${rutina.id_rutina}"

            header.findViewById<TextView>(R.id.tvRutinaNombre).text  = nombreRutina
            header.findViewById<TextView>(R.id.tvRutinaCount).text   =
                "${rutina.ejercicios.size} ejercicios"
            header.findViewById<TextView>(R.id.tvRutinaEntrenador).text =
                "Asignada por: ${rutina.entrenador}"

            // Botón ÚNICO por rutina — lanza EntrenamientoActivity con la rutina completa
            header.findViewById<Button>(R.id.btnEmpezarRutina).setOnClickListener {
                val intent = Intent(this, EntrenamientoActivity::class.java)
                intent.putExtra("rutina_id",   rutina.id_rutina)
                intent.putExtra("rutina_json", Gson().toJson(rutina))
                startActivity(intent)
            }

            container.addView(header)

            // ── Tarjetas de ejercicios (solo informativas, sin botón) ────────
            for (ej in rutina.ejercicios.sortedBy { it.orden }) {
                val card = inflater.inflate(
                    R.layout.item_ejercicio_info, container, false
                ) as CardView

                // Número de orden
                card.findViewById<TextView>(R.id.tvEjOrden).text =
                    "${ej.orden}"

                // Nombre
                card.findViewById<TextView>(R.id.tvEjNombre).text = ej.nombre

                // Series × Reps
                card.findViewById<TextView>(R.id.tvEjSeriesReps).text =
                    "${ej.series} × ${ej.repeticiones}"

                // Descanso
                val tvDescanso = card.findViewById<TextView>(R.id.tvEjDescanso)
                tvDescanso.text = when {
                    ej.descanso_seg == null || ej.descanso_seg == 0 -> "Sin descanso"
                    ej.descanso_seg < 60 -> "${ej.descanso_seg}s descanso"
                    else -> {
                        val min = ej.descanso_seg / 60
                        val seg = ej.descanso_seg % 60
                        if (seg == 0) "${min}min descanso" else "${min}m ${seg}s descanso"
                    }
                }

                // Peso sugerido
                card.findViewById<TextView>(R.id.tvEjPeso).text =
                    if ((ej.peso_kg ?: 0.0) > 0.0) "· ${ej.peso_kg} kg" else "· Peso libre"

                // Notas técnicas
                val tvNotas = card.findViewById<TextView>(R.id.tvEjNotas)
                if (!ej.descripcion_tecnica.isNullOrBlank()) {
                    tvNotas.text      = "📋 ${ej.descripcion_tecnica}"
                    tvNotas.visibility = View.VISIBLE
                } else {
                    tvNotas.visibility = View.GONE
                }

                // Imagen del ejercicio
                val ivImg      = card.findViewById<ImageView>(R.id.ivEjImagen)
                val tvFallback = card.findViewById<TextView>(R.id.tvEjImagenFallback)
                val imgUrl = ej.imagen_url
                    ?.takeIf { it.isNotBlank() }
                    ?.let { if (it.startsWith("http")) it else "$baseUrl$it" }

                if (!imgUrl.isNullOrBlank()) {
                    ivImg.visibility      = View.VISIBLE
                    tvFallback.visibility = View.GONE
                    Glide.with(this)
                        .load(imgUrl)
                        .centerCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .placeholder(R.drawable.bg_card_orange)
                        .error(R.drawable.bg_card_orange)
                        .into(ivImg)
                } else {
                    ivImg.visibility      = View.GONE
                    tvFallback.visibility = View.VISIBLE
                }

                container.addView(card)
            }
        }
    }
}