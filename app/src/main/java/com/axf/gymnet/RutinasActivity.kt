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
 * RutinasActivity — muestra las rutinas agrupadas por DÍA/BLOQUE (nombre de rutina).
 *
 * Cada sección = un nombre de rutina (ej: "Pecho y Espalda", "Piernas", "Brazos").
 * Dentro de cada sección, los ejercicios se muestran con imagen, series × reps,
 * descanso y notas técnicas.
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
    // Renderizado: una sección por rutina (día/bloque)
    // ─────────────────────────────────────────────────────────────────────────

    private fun renderizarRutinas(
        container: LinearLayout,
        rutinas: List<RutinaResponse>
    ) {
        val inflater  = layoutInflater
        val baseUrl   = RetrofitClient.BASE_URL.trimEnd('/')

        for (rutina in rutinas) {
            if (rutina.ejercicios.isEmpty()) continue

            // ── Header de sección (nombre del día / bloque) ────────────────
            val header = inflater.inflate(R.layout.item_categoria_header, container, false)
            val nombreDia = rutina.nombre
                ?.takeIf { it.isNotBlank() }
                ?.uppercase()
                ?: "RUTINA #${rutina.id_rutina}"
            header.findViewById<TextView>(R.id.tvCategoriaNombre).text = nombreDia
            header.findViewById<TextView>(R.id.tvCategoriaCount).text =
                rutina.ejercicios.size.toString()
            container.addView(header)

            // ── Tarjeta de cada ejercicio ──────────────────────────────────
            for (ej in rutina.ejercicios.sortedBy { it.orden }) {
                val card = inflater.inflate(
                    R.layout.item_ejercicio_categoria, container, false
                ) as CardView

                // Nombre
                card.findViewById<TextView>(R.id.tvEjNombre).text = ej.nombre

                // Series × Reps
                card.findViewById<TextView>(R.id.tvEjSeriesReps).text =
                    "${ej.series} × ${ej.repeticiones}"

                // Descanso
                val tvDescanso = card.findViewById<TextView>(R.id.tvEjDescanso)
                tvDescanso.text = if ((ej.descanso_seg ?: 0) > 0)
                    "${ej.descanso_seg}s descanso"
                else
                    "Sin descanso fijo"

                // Peso sugerido
                card.findViewById<TextView>(R.id.tvEjPeso).text =
                    if ((ej.peso_kg ?: 0.0) > 0.0) "· ${ej.peso_kg} kg sugerido"
                    else "· Peso libre"

                // Origen
                card.findViewById<TextView>(R.id.tvEjRutinaOrigen).text =
                    "De: Rutina · ${rutina.entrenador}"

                // Notas técnicas
                val tvNotas = card.findViewById<TextView>(R.id.tvEjNotas)
                if (!ej.descripcion_tecnica.isNullOrBlank()) {
                    tvNotas.text = "📋 ${ej.descripcion_tecnica}"
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

                // Botón Empezar — lanza el entrenamiento completo de esa rutina
                card.findViewById<Button>(R.id.btnEmpezarEjercicio).setOnClickListener {
                    val intent = Intent(this, EntrenamientoActivity::class.java)
                    intent.putExtra("rutina_id",   rutina.id_rutina)
                    intent.putExtra("rutina_json", Gson().toJson(rutina))
                    startActivity(intent)
                }

                container.addView(card)
            }
        }
    }
}