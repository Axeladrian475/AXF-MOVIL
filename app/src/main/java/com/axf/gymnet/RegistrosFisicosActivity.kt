package com.axf.gymnet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.axf.gymnet.data.RegistroFisico
import com.axf.gymnet.network.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class RegistrosFisicosActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var contentView: NestedScrollView
    
    // Top Card TextViews
    private lateinit var tvPesoAct: TextView
    private lateinit var tvAlturaAct: TextView
    private lateinit var tvEdadAct: TextView
    private lateinit var tvGrasaAct: TextView
    private lateinit var tvMusculoAct: TextView
    private lateinit var tvObjetivoAct: TextView
    private lateinit var tvTmbAct: TextView
    private lateinit var tvTdeeAct: TextView

    // Bottom section
    private lateinit var rvHistorial: RecyclerView
    private lateinit var tvEmpty: TextView

    private val formatIn = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val formatOut = SimpleDateFormat("dd MMM yyyy", Locale("es", "MX"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registros_fisicos)

        btnBack = findViewById(R.id.btnBack)
        progressBar = findViewById(R.id.progressBar)
        contentView = findViewById(R.id.contentView)

        tvPesoAct = findViewById(R.id.tvPesoAct)
        tvAlturaAct = findViewById(R.id.tvAlturaAct)
        tvEdadAct = findViewById(R.id.tvEdadAct)
        tvGrasaAct = findViewById(R.id.tvGrasaAct)
        tvMusculoAct = findViewById(R.id.tvMusculoAct)
        tvObjetivoAct = findViewById(R.id.tvObjetivoAct)
        tvTmbAct = findViewById(R.id.tvTmbAct)
        tvTdeeAct = findViewById(R.id.tvTdeeAct)

        rvHistorial = findViewById(R.id.rvHistorial)
        tvEmpty = findViewById(R.id.tvEmpty)

        rvHistorial.layoutManager = LinearLayoutManager(this)

        btnBack.setOnClickListener { finish() }

        cargarRegistros()
    }

    private fun cargarRegistros() {
        val prefs = getSharedPreferences("axf_prefs", MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token == null) {
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        progressBar.visibility = View.VISIBLE
        contentView.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getRegistrosFisicos("Bearer $token")
                if (response.isSuccessful) {
                    val registros = response.body() ?: emptyList()
                    if (registros.isNotEmpty()) {
                        // The backend returns them ORDER BY creado_en DESC, so index 0 is the latest
                        mostrarDatosActuales(registros[0])
                        
                        // History adapter with all records (or you can slice it from index 1)
                        rvHistorial.adapter = RegistrosFisicosAdapter(registros)
                        rvHistorial.visibility = View.VISIBLE
                        tvEmpty.visibility = View.GONE
                    } else {
                        mostrarDatosVacios()
                        rvHistorial.visibility = View.GONE
                        tvEmpty.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(this@RegistrosFisicosActivity, "Error al cargar datos", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegistrosFisicosActivity, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
                contentView.visibility = View.VISIBLE
            }
        }
    }

    private fun mostrarDatosActuales(registro: RegistroFisico) {
        tvPesoAct.text = if (registro.pesoKg != null) "${registro.pesoKg} kg" else "-- kg"
        tvAlturaAct.text = if (registro.alturaCm != null) "${registro.alturaCm} cm" else "-- cm"
        tvEdadAct.text = if (registro.edad != null) "${registro.edad} años" else "--"
        tvGrasaAct.text = if (registro.pctGrasa != null) "${registro.pctGrasa} %" else "-- %"
        tvMusculoAct.text = if (registro.pctMusculo != null) "${registro.pctMusculo} %" else "-- %"
        tvObjetivoAct.text = registro.objetivo ?: "--"
        tvTmbAct.text = if (registro.tmb != null) "${registro.tmb} kcal" else "-- kcal"
        tvTdeeAct.text = if (registro.tdee != null) "${registro.tdee} kcal" else "-- kcal"
    }

    private fun mostrarDatosVacios() {
        tvPesoAct.text = "-- kg"
        tvAlturaAct.text = "-- cm"
        tvEdadAct.text = "--"
        tvGrasaAct.text = "-- %"
        tvMusculoAct.text = "-- %"
        tvObjetivoAct.text = "--"
        tvTmbAct.text = "-- kcal"
        tvTdeeAct.text = "-- kcal"
    }

    inner class RegistrosFisicosAdapter(private val lista: List<RegistroFisico>) :
        RecyclerView.Adapter<RegistrosFisicosAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvFecha: TextView = view.findViewById(R.id.tvFecha)
            val tvNutriologo: TextView = view.findViewById(R.id.tvNutriologo)
            val tvPeso: TextView = view.findViewById(R.id.tvPeso)
            val tvGrasa: TextView = view.findViewById(R.id.tvGrasa)
            val tvMusculo: TextView = view.findViewById(R.id.tvMusculo)
            val tvNotas: TextView = view.findViewById(R.id.tvNotas)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_registro_fisico, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val r = lista[position]
            
            try {
                val d = formatIn.parse(r.creadoEn)
                holder.tvFecha.text = if (d != null) formatOut.format(d) else r.creadoEn.split("T")[0]
            } catch (e: Exception) {
                holder.tvFecha.text = r.creadoEn.split("T")[0]
            }

            holder.tvNutriologo.text = "Por: ${r.nutriologo ?: "Sistema"}"
            holder.tvPeso.text = if (r.pesoKg != null) "${r.pesoKg} kg" else "-- kg"
            holder.tvGrasa.text = if (r.pctGrasa != null) "${r.pctGrasa} %" else "-- %"
            holder.tvMusculo.text = if (r.pctMusculo != null) "${r.pctMusculo} %" else "-- %"
            
            if (r.notas.isNullOrBlank()) {
                holder.tvNotas.visibility = View.GONE
            } else {
                holder.tvNotas.visibility = View.VISIBLE
                holder.tvNotas.text = "Notas: ${r.notas}"
            }
        }

        override fun getItemCount() = lista.size
    }
}
