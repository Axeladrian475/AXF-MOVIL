package com.axf.gymnet

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.axf.gymnet.data.EjercicioRutina
import com.axf.gymnet.data.RutinaResponse
import com.axf.gymnet.network.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.launch

/**
 * RutinasActivity — agrupa los ejercicios por su PROPIO grupo muscular
 * (campo `grupo_muscular` de cada EjercicioRutina), no por el nombre de la rutina.
 *
 * Ejemplo:
 *   Rutina "Pecho y Espalda" tiene 4 ejercicios:
 *     - Press banca          → grupo_muscular = "Pecho"
 *     - Aperturas            → grupo_muscular = "Pecho"
 *     - Jalón al pecho       → grupo_muscular = "Espalda"
 *     - Remo con barra       → grupo_muscular = "Espalda"
 *
 *   Resultado visible:
 *     ── PECHO ──────────────────
 *     • Press banca  (De: Pecho y Espalda)
 *     • Aperturas    (De: Pecho y Espalda)
 *     ── ESPALDA ─────────────────
 *     • Jalón al pecho  (De: Pecho y Espalda)
 *     • Remo con barra  (De: Pecho y Espalda)
 *
 * Si un ejercicio no tiene `grupo_muscular` en el API, usa el nombre
 * de la rutina como categoría de respaldo.
 */
class RutinasActivity : AppCompatActivity() {

    /** Ejercicio + contexto de la rutina de la que proviene */
    private data class EjercicioConContexto(
        val ejercicio: EjercicioRutina,
        val rutina: RutinaResponse
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rutinas)

        val prefs = getSharedPreferences("axf_prefs", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        val progressBar   = findViewById<ProgressBar>(R.id.progressRutinas)
        val containerList = findViewById<LinearLayout>(R.id.containerRutinas)
        val tvVacio       = findViewById<TextView>(R.id.tvVacio)
        val layoutVacio   = findViewById<LinearLayout>(R.id.layoutVacio)
        val btnBack       = findViewById<View>(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

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
                        val grupos = agruparPorCategoria(rutinas)
                        if (grupos.isEmpty()) {
                            layoutVacio.visibility = View.VISIBLE
                        } else {
                            containerList.removeAllViews()
                            renderizarGrupos(containerList, grupos)
                        }
                    }
                } else {
                    tvVacio.text = "Error al cargar rutinas"
                    layoutVacio.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                tvVacio.text = "Sin conexión al servidor"
                layoutVacio.visibility = View.VISIBLE
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Agrupación por grupo_muscular PROPIO del ejercicio
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Recorre todas las rutinas y todos sus ejercicios.
     * Cada ejercicio se clasifica bajo su [EjercicioRutina.grupo_muscular].
     *
     * Si el campo viene null/vacío, se usa el nombre de la rutina como
     * categoría de respaldo para no perder el ejercicio.
     *
     * El mapa resultante está ordenado: mayor cantidad de ejercicios primero;
     * en caso de empate, orden alfabético.
     */
    private fun agruparPorCategoria(
        rutinas: List<RutinaResponse>
    ): LinkedHashMap<String, MutableList<EjercicioConContexto>> {

        val mapa = mutableMapOf<String, MutableList<EjercicioConContexto>>()

        for (rutina in rutinas) {
            for (ejercicio in rutina.ejercicios) {
                // Categoría = grupo_muscular del ejercicio; fallback = nombre de la rutina
                val categoria = resolverCategoria(ejercicio, rutina)
                mapa.getOrPut(categoria) { mutableListOf() }
                    .add(EjercicioConContexto(ejercicio, rutina))
            }
        }

        // Ordenar por cantidad de ejercicios desc, luego alfabético
        val ordenadas = mapa.entries.sortedWith(
            compareByDescending<Map.Entry<String, MutableList<EjercicioConContexto>>> {
                it.value.size
            }.thenBy { it.key }
        )

        return LinkedHashMap<String, MutableList<EjercicioConContexto>>().also { lhm ->
            ordenadas.forEach { lhm[it.key] = it.value }
        }
    }

    /**
     * Determina la categoría de un ejercicio:
     *   1. Usa [EjercicioRutina.grupo_muscular] si viene del API.
     *   2. Si no, usa el nombre de la rutina (ej: "Pecho y Espalda").
     *   3. Si tampoco hay nombre, retorna "General".
     */
    private fun resolverCategoria(ej: EjercicioRutina, rutina: RutinaResponse): String {
        if (!ej.grupo_muscular.isNullOrBlank()) {
            return ej.grupo_muscular.trim().capitalizar()
        }
        if (!rutina.nombre.isNullOrBlank()) {
            return rutina.nombre.trim().capitalizar()
        }
        return "General"
    }

    private fun String.capitalizar(): String =
        this.lowercase().replaceFirstChar { it.uppercase() }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Renderizar grupos en pantalla
    // ─────────────────────────────────────────────────────────────────────────

    private fun renderizarGrupos(
        container: LinearLayout,
        grupos: LinkedHashMap<String, MutableList<EjercicioConContexto>>
    ) {
        val inflater = layoutInflater

        for ((categoria, items) in grupos) {

            // ── Header de sección ──────────────────────────────────────────
            val header = inflater.inflate(R.layout.item_categoria_header, container, false)
            header.findViewById<TextView>(R.id.tvCategoriaNombre).text =
                categoria.uppercase()
            header.findViewById<TextView>(R.id.tvCategoriaCount).text =
                items.size.toString()
            container.addView(header)

            // ── Tarjeta de cada ejercicio ──────────────────────────────────
            for (item in items) {
                val card = inflater.inflate(
                    R.layout.item_ejercicio_categoria, container, false
                ) as CardView

                val ej     = item.ejercicio
                val rutina = item.rutina

                // Nombre del ejercicio
                card.findViewById<TextView>(R.id.tvEjNombre).text = ej.nombre

                // Series × Repeticiones
                card.findViewById<TextView>(R.id.tvEjSeriesReps).text =
                    "${ej.series} × ${ej.repeticiones}"

                // Peso sugerido
                val tvPeso = card.findViewById<TextView>(R.id.tvEjPeso)
                tvPeso.text = if ((ej.peso_kg ?: 0.0) > 0.0) {
                    "· ${ej.peso_kg} kg sugerido"
                } else {
                    "· Peso libre"
                }

                // Origen — rutina + entrenador
                val nombreRutina = rutina.nombre.takeIf { !it.isNullOrBlank() } ?: "Rutina"
                card.findViewById<TextView>(R.id.tvEjRutinaOrigen).text =
                    "De: $nombreRutina · ${rutina.entrenador}"

                // Botón Empezar — lanza la rutina COMPLETA de origen
                card.findViewById<Button>(R.id.btnEmpezarEjercicio).setOnClickListener {
                    val intent = Intent(this, EntrenamientoActivity::class.java)
                    intent.putExtra("rutina_id", rutina.id_rutina)
                    intent.putExtra("rutina_json", Gson().toJson(rutina))
                    startActivity(intent)
                }

                container.addView(card)
            }
        }
    }
}
