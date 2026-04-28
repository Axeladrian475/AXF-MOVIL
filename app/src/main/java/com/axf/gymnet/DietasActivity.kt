package com.axf.gymnet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.axf.gymnet.data.DietaResumen
import com.axf.gymnet.network.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DietasActivity : AppCompatActivity() {

    private val TAG = "DietasActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dietas)

        val prefs = getSharedPreferences("axf_prefs", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        val rv        = findViewById<RecyclerView>(R.id.rvDietas)
        val tvVacio   = findViewById<TextView>(R.id.tvDietasVacio)
        val tvBack    = findViewById<TextView>(R.id.tvDietasBack)

        rv.layoutManager = LinearLayoutManager(this)
        tvBack.setOnClickListener { finish() }

        if (token.isEmpty()) {
            Toast.makeText(this, "Sesión no válida. Inicia sesión de nuevo.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        cargarDietas(token, rv, tvVacio)
    }

    private fun cargarDietas(token: String, rv: RecyclerView, tvVacio: TextView) {
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.instance.getDietas("Bearer $token")
                if (resp.isSuccessful) {
                    val lista = resp.body() ?: emptyList()
                    Log.d(TAG, "Dietas cargadas: ${lista.size}")
                    
                    if (lista.isEmpty()) {
                        tvVacio.visibility = View.VISIBLE
                        rv.visibility = View.GONE
                    } else {
                        tvVacio.visibility = View.GONE
                        rv.visibility = View.VISIBLE
                        rv.adapter = DietasAdapter(lista) { dieta ->
                            val intent = Intent(this@DietasActivity, DietaDetalleActivity::class.java)
                            intent.putExtra("id_dieta", dieta.id_dieta)
                            startActivity(intent)
                        }
                    }
                } else {
                    val errorBody = resp.errorBody()?.string()
                    Log.e(TAG, "Error API ${resp.code()}: $errorBody")
                    Toast.makeText(this@DietasActivity, "Error del servidor (${resp.code()})", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error de conexión/parseo", e)
                Toast.makeText(this@DietasActivity, "Error de conexión o datos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// ── Adapter ───────────────────────────────────────────────────────────────────

class DietasAdapter(
    private val items: List<DietaResumen>,
    private val onClick: (DietaResumen) -> Unit
) : RecyclerView.Adapter<DietasAdapter.VH>() {

    // Formatos de entrada comunes de SQL/Node
    private val formats = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    )
    private val sdfOut = SimpleDateFormat("dd 'de' MMMM yyyy", Locale("es", "MX"))

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvFecha    : TextView = v.findViewById(R.id.tvDietaFecha)
        val tvNutri    : TextView = v.findViewById(R.id.tvDietaNutriologo)
        val tvComidas  : TextView = v.findViewById(R.id.tvDietaComidas)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_dieta, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val d = items[position]
        holder.tvNutri.text = "Nutriólogo: ${d.nutriologo}"
        holder.tvComidas.text = "${d.total_comidas} comida(s) registradas"
        
        var fechaFormateada = d.creado_en
        for (format in formats) {
            try {
                format.timeZone = TimeZone.getTimeZone("UTC")
                val date = format.parse(d.creado_en)
                if (date != null) {
                    fechaFormateada = sdfOut.format(date)
                    break
                }
            } catch (_: Exception) {}
        }
        holder.tvFecha.text = fechaFormateada

        holder.itemView.setOnClickListener { onClick(d) }
    }
}
