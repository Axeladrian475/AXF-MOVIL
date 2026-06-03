package com.axf.gymnet

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.axf.gymnet.data.BloqueRutina
import com.axf.gymnet.data.EjercicioRutina
import com.axf.gymnet.data.RutinaResponse
import com.axf.gymnet.network.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class RutinasActivity : AppCompatActivity() {
    private var token: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rutinas)

        // Respetar status bar en el header
        val header = findViewById<View>(R.id.rutinasHeader)
        val headerPadTop = header?.paddingTop ?: 0
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            header?.updatePadding(top = headerPadTop + bars.top)
            insets
        }

        token = getSharedPreferences("axf_prefs", MODE_PRIVATE)
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
                        renderizarRutinas(containerList, rutinas, token)
                    }
                } else {
                    val errorBody = resp.errorBody()?.string()
                    var errorMessage = "Error al cargar rutinas (${resp.code()})"
                    if (resp.code() == 403 && !errorBody.isNullOrEmpty()) {
                        try {
                            val json = org.json.JSONObject(errorBody)
                            if (json.has("message")) {
                                errorMessage = json.getString("message")
                            }
                        } catch (e: Exception) {
                            // Ignorar error
                        }
                    }
                    tvVacio.text = errorMessage
                    layoutVacio.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                tvVacio.text = "Sin conexión: ${e.message}"
                layoutVacio.visibility = View.VISIBLE
            }
        }
    }

    // ── Formatea "2026-05-13T02:58:47.000Z" → "13 may 2026" ─────────────────
    private fun formatearFecha(creado_en: String): String {
        return try {
            val entrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale("es", "MX"))
            val salida  = SimpleDateFormat("d MMM yyyy", Locale("es", "MX"))
            salida.format(entrada.parse(creado_en)!!)
        } catch (_: Exception) { creado_en.take(10) }
    }

    // ── Calcula los bloques desde orden (FLOOR(orden/100)) ───────────────────
    private fun calcularBloques(rutina: RutinaResponse): List<BloqueRutina> {
        val ejercicios = rutina.ejercicios ?: emptyList()

        // Si el backend ya devuelve bloques con nombre, usarlos directamente
        val bloquesBackend = rutina.bloques
        if (!bloquesBackend.isNullOrEmpty() && bloquesBackend.any { it.nombre != null }) return bloquesBackend

        // Agrupar por bloque_idx y tomar el nombre_bloque del primer ejercicio de cada bloque
        return ejercicios
            .map { it.getBloqueIdx() }
            .distinct()
            .sorted()
            .map { bloqueIdx ->
                val primerEj = ejercicios.first { it.getBloqueIdx() == bloqueIdx }
                val nombre = primerEj.getNombreBloque()
                BloqueRutina(bloque_idx = bloqueIdx, nombre = nombre)
            }
    }

    // ── Genera el título: "13 may 2026 · Pecho / Espalda" ───────────────────
    private fun generarTituloRutina(rutina: RutinaResponse, bloques: List<BloqueRutina>): String {
        val fecha = formatearFecha(rutina.creado_en)
        val musculos = bloques
            .mapNotNull { it.nombre?.replaceFirstChar { c -> c.uppercase() } }
            .joinToString(" / ")
        return if (musculos.isNotBlank()) "$fecha  ·  $musculos" else fecha
    }

    private fun renderizarRutinas(container: LinearLayout, rutinas: List<RutinaResponse>, token: String) {
        val inflater = layoutInflater

        for (rutina in rutinas) {
            val ejercicios = rutina.ejercicios ?: emptyList()
            if (ejercicios.isEmpty()) continue

            val header = inflater.inflate(R.layout.item_rutina_header, container, false)
            val bloques = calcularBloques(rutina)

            // Título: "13 may 2026  ·  Pecho / Espalda"
            val titulo = generarTituloRutina(rutina, bloques)
            header.findViewById<TextView>(R.id.tvRutinaNombre).apply {
                text = titulo
                textSize = 15f   // un poco menor para que quepa la fecha + músculos
            }

            header.findViewById<TextView>(R.id.tvRutinaCount).text =
                if (bloques.size > 1) "${bloques.size} bloques" else "${ejercicios.size} ejercicios"

            header.findViewById<TextView>(R.id.tvRutinaEntrenador).text =
                "Asignada por: ${rutina.entrenador}"

            // Mostrar nombres de ejercicios debajo del entrenador
            val tvEjercicios = header.findViewById<TextView>(R.id.tvRutinaEjercicios)
            if (ejercicios.isNotEmpty()) {
                val maxMostrar = 4
                val nombres = ejercicios.take(maxMostrar).joinToString("\n") { "• ${it.nombre}" }
                val extra = if (ejercicios.size > maxMostrar) "\n  +${ejercicios.size - maxMostrar} más..." else ""
                tvEjercicios.text = nombres + extra
                tvEjercicios.visibility = android.view.View.VISIBLE
            }

            header.findViewById<android.widget.Button>(R.id.btnEmpezarRutina)
                .setOnClickListener { lanzarRutinaOSeleccionarGrupo(rutina) }

            // ── Botón Descargar PDF ───────────────────────────────────────
            header.findViewById<TextView>(R.id.btnDescargarPdfRutina)
                .setOnClickListener {
                    val url = "${RetrofitClient.BASE_URL}api/suscriptores/movil/rutinas/${rutina.id_rutina}/pdf?token=$token"
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (_: Exception) {
                        android.widget.Toast.makeText(this, "No se pudo abrir el PDF", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }

            // ── Botón Eliminar Rutina ─────────────────────────────────────
            header.findViewById<TextView>(R.id.btnEliminarRutina)
                .setOnClickListener {
                    AlertDialog.Builder(this)
                        .setTitle("Eliminar rutina")
                        .setMessage("¿Estás seguro de que quieres eliminar esta rutina? Esta acción no se puede deshacer.")
                        .setPositiveButton("Eliminar") { _, _ ->
                            lifecycleScope.launch {
                                try {
                                    val response = RetrofitClient.instance.eliminarRutina("Bearer $token", rutina.id_rutina)
                                    if (response.isSuccessful) {
                                        android.widget.Toast.makeText(this@RutinasActivity, "Rutina eliminada", android.widget.Toast.LENGTH_SHORT).show()
                                        container.removeView(header)
                                        if (container.childCount == 0) {
                                            findViewById<LinearLayout>(R.id.layoutVacio).visibility = android.view.View.VISIBLE
                                            container.visibility = android.view.View.GONE
                                        }
                                    } else {
                                        android.widget.Toast.makeText(this@RutinasActivity, "Error al eliminar rutina", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(this@RutinasActivity, "Sin conexión", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }

            container.addView(header)
        }
    }

    private fun lanzarRutinaOSeleccionarGrupo(rutina: RutinaResponse) {
        val ejercicios = rutina.ejercicios ?: emptyList()
        val bloques    = calcularBloques(rutina)

        if (bloques.size <= 1) {
            val nombre = bloques.firstOrNull()?.nombre ?: rutina.nombre ?: "Entrenamiento"
            abrirEntrenamiento(rutina, ejercicios, nombre)
            return
        }

        // ── Diálogo rediseñado ────────────────────────────────────────────────
        val dialogView      = layoutInflater.inflate(R.layout.dialog_seleccion_grupo, null)
        val containerGrupos = dialogView.findViewById<LinearLayout>(R.id.containerGrupos)
        val tvNombre        = dialogView.findViewById<TextView>(R.id.tvRutinaNombreDialog)
        val tvFecha         = dialogView.findViewById<TextView>(R.id.tvRutinaFechaDialog)

        // Título: músculos combinados
        val musculos = bloques.mapNotNull { it.nombre?.replaceFirstChar { c -> c.uppercase() } }
            .joinToString(" + ")
        tvNombre.text = musculos.ifBlank { rutina.nombre?.uppercase() ?: "HOY ENTRENAS" }
        tvFecha.text  = formatearFecha(rutina.creado_en)

        val dialog = AlertDialog.Builder(this, R.style.DialogDescanso)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        bloques.forEachIndexed { i, bloque ->
            val ejerciciosBloque = ejercicios.filter { it.getBloqueIdx() == bloque.bloque_idx }
            // Usar nombre real del músculo; si no hay, "Bloque N"
            val nombreBloque = bloque.nombre?.replaceFirstChar { it.uppercase() } ?: "Bloque ${i + 1}"

            // Inflar la tarjeta de bloque
            val card = layoutInflater.inflate(R.layout.item_bloque_btn, containerGrupos, false)
            card.findViewById<TextView>(R.id.tvBloqueNombre).text     = nombreBloque.uppercase()
            card.findViewById<TextView>(R.id.tvBloqueEjercicios).text =
                "${ejerciciosBloque.size} ejercicios"

            card.setOnClickListener {
                dialog.dismiss()
                abrirEntrenamiento(rutina, ejerciciosBloque, nombreBloque)
            }
            containerGrupos.addView(card)
        }

        dialog.show()

        // Limitar la altura del diálogo al 85% de la pantalla para que el ScrollView funcione
        dialog.window?.let { win ->
            val displayMetrics = resources.displayMetrics
            val maxHeight = (displayMetrics.heightPixels * 0.85).toInt()
            win.setLayout(
                android.view.WindowManager.LayoutParams.MATCH_PARENT,
                maxHeight
            )
        }
    }

    private fun abrirEntrenamiento(
        rutina: RutinaResponse,
        ejercicios: List<EjercicioRutina>,
        grupoNombre: String
    ) {
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
