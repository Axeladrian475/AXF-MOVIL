package com.axf.gymnet

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.axf.gymnet.network.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DietaDetalleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dieta_detalle)

        val prefs   = getSharedPreferences("axf_prefs", MODE_PRIVATE)
        val token   = prefs.getString("token", "") ?: ""
        val idDieta = intent.getIntExtra("id_dieta", -1)
        val baseUrl = RetrofitClient.BASE_URL

        val tvBack      = findViewById<TextView>(R.id.tvDetalleBack)
        val tvTitulo    = findViewById<TextView>(R.id.tvDetalleTitulo)
        val tvNutri     = findViewById<TextView>(R.id.tvDetalleNutriologo)
        val tvFecha     = findViewById<TextView>(R.id.tvDetalleFecha)
        val btnPdf      = findViewById<TextView>(R.id.btnDetallePdf)
        val llDias      = findViewById<LinearLayout>(R.id.llDetalleDias)
        val loading     = findViewById<View>(R.id.progressDetalle)

        tvBack.setOnClickListener { finish() }

        if (idDieta == -1) { finish(); return }

        loading.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.instance.getDietaDetalle("Bearer $token", idDieta)
                loading.visibility = View.GONE

                if (!resp.isSuccessful) {
                    Toast.makeText(this@DietaDetalleActivity, "Error al cargar dieta", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val dieta = resp.body()!!

                // Info superior
                tvTitulo.text = "Plan Nutricional #$idDieta"
                tvNutri.text  = "Nutriólogo: ${dieta.nutriologo}"
                try {
                    val sdfIn  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    val sdfOut = SimpleDateFormat("dd 'de' MMMM yyyy", Locale("es", "MX"))
                    val f = sdfIn.parse(dieta.creado_en)
                    tvFecha.text = if (f != null) "Fecha: ${sdfOut.format(f)}" else "Fecha: ${dieta.creado_en.take(10)}"
                } catch (_: Exception) {
                    tvFecha.text = "Fecha: ${dieta.creado_en.take(10)}"
                }

                // Botón PDF → pasa el token como query param porque se abre en el navegador
                // y no puede enviar headers de Authorization
                btnPdf.setOnClickListener {
                    val url = "${baseUrl}api/movil/nutricion/dietas/$idDieta/pdf?token=$token"
                    try {
                        val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(i)
                    } catch (_: Exception) {
                        Toast.makeText(this@DietaDetalleActivity, "No se pudo abrir el PDF", Toast.LENGTH_SHORT).show()
                    }
                }

                // Construir días
                llDias.removeAllViews()
                val inflater = LayoutInflater.from(this@DietaDetalleActivity)

                for (dia in dieta.dias) {
                    // Encabezado de día
                    val tvDia = TextView(this@DietaDetalleActivity).apply {
                        text = "📅  ${dia.dia}"
                        textSize = 15f
                        setTextColor(android.graphics.Color.WHITE)
                        setBackgroundColor(android.graphics.Color.parseColor("#1A2E45"))
                        setPadding(32, 18, 32, 18)
                        val lp = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        lp.topMargin = 24
                        layoutParams = lp
                    }
                    llDias.addView(tvDia)

                    // Comidas del día
                    for (comida in dia.comidas) {
                        val vComida = inflater.inflate(R.layout.item_dieta_comida, llDias, false)

                        vComida.findViewById<TextView>(R.id.tvComidaTitulo).text =
                            "${comida.orden_comida}. ${comida.descripcion ?: comida.receta_nombre ?: "Comida"}"

                        val tvReceta = vComida.findViewById<TextView>(R.id.tvComidaReceta)
                        if (!comida.receta_nombre.isNullOrBlank() && comida.receta_nombre != comida.descripcion) {
                            tvReceta.visibility = View.VISIBLE
                            tvReceta.text = "Receta: ${comida.receta_nombre}"
                        } else {
                            tvReceta.visibility = View.GONE
                        }

                        // Macros
                        val macros = buildString {
                            comida.calorias?.let    { append("🔥 ${it.toInt()} kcal  ") }
                            comida.proteinas_g?.let { append("💪 ${it.toInt()}g prot  ") }
                            comida.grasas_g?.let    { append("🥑 ${it.toInt()}g gras") }
                        }
                        val tvMacros = vComida.findViewById<TextView>(R.id.tvComidaMacros)
                        if (macros.isNotBlank()) {
                            tvMacros.visibility = View.VISIBLE
                            tvMacros.text = macros.trim()
                        } else {
                            tvMacros.visibility = View.GONE
                        }

                        // Ingredientes
                        val tvIngs = vComida.findViewById<TextView>(R.id.tvComidaIngredientes)
                        val ings = comida.ingredientes
                        if (!ings.isNullOrEmpty()) {
                            tvIngs.visibility = View.VISIBLE
                            tvIngs.text = ings.joinToString(" · ") { "${it.nombre} ${it.cantidad} ${it.unidad_medicion}" }
                        } else {
                            tvIngs.visibility = View.GONE
                        }

                        // Notas
                        val tvNotas = vComida.findViewById<TextView>(R.id.tvComidaNotas)
                        if (!comida.notas.isNullOrBlank()) {
                            tvNotas.visibility = View.VISIBLE
                            tvNotas.text = "📝 ${comida.notas}"
                        } else {
                            tvNotas.visibility = View.GONE
                        }

                        llDias.addView(vComida)
                    }
                }

            } catch (e: Exception) {
                loading.visibility = View.GONE
                Toast.makeText(this@DietaDetalleActivity, "Sin conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }
}