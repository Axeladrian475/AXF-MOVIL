package com.axf.gymnet

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.axf.gymnet.data.BloqueRutina
import com.axf.gymnet.data.EjercicioRutina
import com.axf.gymnet.data.RutinaResponse
import com.axf.gymnet.network.RetrofitClient
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
            // Si hay múltiples bloques, mostrar cuántos bloques tiene. Si no, cantidad de ejercicios
            val numBloques = rutina.bloques.size
            header.findViewById<TextView>(R.id.tvRutinaCount).text   =
                if (numBloques > 1) "$numBloques bloques" else "${rutina.ejercicios.size} ejercicios"
            header.findViewById<TextView>(R.id.tvRutinaEntrenador).text =
                "Asignada por: ${rutina.entrenador}"

            // Botón ÚNICO por rutina — abre selector de grupo si hay varios, si no arranca directo
            header.findViewById<Button>(R.id.btnEmpezarRutina).setOnClickListener {
                lanzarRutinaOSeleccionarGrupo(rutina)
            }

            container.addView(header)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Si la rutina tiene múltiples bloques → diálogo para elegir cuál entrenar.
    // Los bloques vienen del backend ya ordenados con nombre y bloque_idx.
    // Si solo hay un bloque → arranca directo sin diálogo.
    // ─────────────────────────────────────────────────────────────────────────
    private fun lanzarRutinaOSeleccionarGrupo(rutina: RutinaResponse) {

        // Usar los bloques del backend si existen, sino inferir por bloque_idx
        val bloques = if (rutina.bloques.isNotEmpty()) {
            rutina.bloques
        } else {
            // Fallback: agrupar por bloque_idx (FLOOR(orden/100))
            rutina.ejercicios
                .map { it.bloque_idx }
                .distinct()
                .sorted()
                .map { idx -> BloqueRutina(bloque_idx = idx, nombre = null) }
        }

        if (bloques.size <= 1) {
            // Un solo bloque → lanzar directamente con todos los ejercicios
            val nombreBloque = bloques.firstOrNull()?.nombre
                ?: rutina.nombre
                ?: "Entrenamiento"
            abrirEntrenamiento(rutina, rutina.ejercicios, nombreBloque)
            return
        }

        // Varios bloques → mostrar diálogo de selección
        val dialogView = layoutInflater.inflate(R.layout.dialog_seleccion_grupo, null)
        val containerGrupos = dialogView.findViewById<LinearLayout>(R.id.containerGrupos)
        val tvRutinaNombreDialog = dialogView.findViewById<TextView>(R.id.tvRutinaNombreDialog)

        tvRutinaNombreDialog.text = rutina.nombre?.uppercase()
            ?: "RUTINA ${rutina.id_rutina}"

        val dialog = AlertDialog.Builder(this, R.style.DialogDescanso)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        bloques.forEachIndexed { i, bloque ->
            val ejerciciosBloque = rutina.ejercicios.filter { it.bloque_idx == bloque.bloque_idx }
            val nombreBloque = bloque.nombre
                ?: "Bloque ${i + 1}"

            val btn = Button(this).apply {
                text = "$nombreBloque  (${ejerciciosBloque.size} ejercicios)"
                setBackgroundResource(R.drawable.bg_btn_orange_rounded)
                setTextColor(resources.getColor(R.color.axf_text_primary, null))
                textSize = 14f
                val dp12 = (12 * resources.displayMetrics.density).toInt()
                val dp8  = (8  * resources.displayMetrics.density).toInt()
                setPadding(dp12, dp8, dp12, dp8)
                stateListAnimator = null
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.bottomMargin = dp8
                layoutParams = params
                setOnClickListener {
                    dialog.dismiss()
                    abrirEntrenamiento(rutina, ejerciciosBloque, nombreBloque)
                }
            }
            containerGrupos.addView(btn)
        }

        dialog.show()
    }

    private fun abrirEntrenamiento(
        rutina: RutinaResponse,
        ejercicios: List<EjercicioRutina>,
        grupoNombre: String
    ) {
        // Crear una copia de la rutina solo con los ejercicios del bloque elegido
        val rutinaFiltrada = rutina.copy(
            nombre     = grupoNombre,
            ejercicios = ejercicios,
            bloques    = emptyList()
        )
        val intent = Intent(this, EntrenamientoActivity::class.java)
        intent.putExtra("rutina_id",    rutina.id_rutina)
        intent.putExtra("rutina_json",  Gson().toJson(rutinaFiltrada))
        intent.putExtra("grupo_nombre", grupoNombre)
        startActivity(intent)
    }
}