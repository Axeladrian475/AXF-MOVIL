package com.axf.gymnet

import androidx.appcompat.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.axf.gymnet.data.AgregarConsumoRequest
import com.axf.gymnet.data.ConsumoDiario
import com.axf.gymnet.network.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ConsumoDiarioActivity : AppCompatActivity() {

    private val TAG = "ConsumoActivity"
    private var token: String = ""
    private var currentDate: Calendar = Calendar.getInstance()
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayDateFormat = SimpleDateFormat("EEE, d MMM yyyy", Locale.forLanguageTag("es-MX"))

    private lateinit var tvFechaActual: TextView
    private lateinit var tvTotalCalorias: TextView
    private lateinit var rvConsumo: RecyclerView
    private lateinit var tvVacio: TextView
    private lateinit var pbConsumo: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consumo_diario)

        token = getSharedPreferences("axf_prefs", MODE_PRIVATE).getString("token", "") ?: ""
        if (token.isEmpty()) {
            Toast.makeText(this, "Sesión inválida", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvFechaActual = findViewById(R.id.tvFechaActual)
        tvTotalCalorias = findViewById(R.id.tvTotalCalorias)
        rvConsumo = findViewById(R.id.rvConsumo)
        tvVacio = findViewById(R.id.tvVacioConsumo)
        pbConsumo = findViewById(R.id.pbConsumo)

        rvConsumo.layoutManager = LinearLayoutManager(this)

        findViewById<View>(R.id.tvConsumoBack).setOnClickListener { finish() }

        findViewById<View>(R.id.btnFechaPrev).setOnClickListener {
            currentDate.add(Calendar.DAY_OF_YEAR, -1)
            actualizarFechaYDatos()
        }

        findViewById<View>(R.id.btnFechaNext).setOnClickListener {
            currentDate.add(Calendar.DAY_OF_YEAR, 1)
            actualizarFechaYDatos()
        }

        findViewById<View>(R.id.fabAgregarConsumo).setOnClickListener {
            mostrarDialogoAgregar()
        }

        actualizarFechaYDatos()
    }

    private fun actualizarFechaYDatos() {
        val dateObj = currentDate.time
        
        // Si es hoy, mostrar "Hoy, fecha"
        val today = Calendar.getInstance()
        if (currentDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            currentDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            tvFechaActual.text = "Hoy, " + displayDateFormat.format(dateObj)
        } else {
            tvFechaActual.text = displayDateFormat.format(dateObj).replaceFirstChar { it.uppercase() }
        }

        cargarConsumo(apiDateFormat.format(dateObj))
    }

    private fun cargarConsumo(fechaStr: String) {
        pbConsumo.visibility = View.VISIBLE
        rvConsumo.visibility = View.GONE
        tvVacio.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.instance.getConsumoDiario("Bearer $token", fechaStr)
                pbConsumo.visibility = View.GONE
                if (resp.isSuccessful) {
                    val lista = resp.body() ?: emptyList()
                    if (lista.isEmpty()) {
                        tvVacio.visibility = View.VISIBLE
                        tvTotalCalorias.text = "Total: 0 kcal"
                    } else {
                        rvConsumo.visibility = View.VISIBLE
                        val totalCal = lista.sumOf { it.calorias ?: 0.0 }
                        tvTotalCalorias.text = "Total: ${totalCal.toInt()} kcal"
                        rvConsumo.adapter = ConsumoDiarioAdapter(lista.toMutableList()) { item, position ->
                            eliminarConsumo(item, position)
                        }
                    }
                } else {
                    val errorBody = resp.errorBody()?.string()
                    var errorMessage = "Error al cargar datos (${resp.code()})"
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
                    tvVacio.visibility = View.VISIBLE
                    Toast.makeText(this@ConsumoDiarioActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                pbConsumo.visibility = View.GONE
                Toast.makeText(this@ConsumoDiarioActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDialogoAgregar() {
        val view = layoutInflater.inflate(R.layout.dialog_agregar_consumo, null)
        val etDesc = view.findViewById<EditText>(R.id.etConsumoDesc)
        val etCal = view.findViewById<EditText>(R.id.etConsumoCalorias)
        
        // Updated to use AppCompat AlertDialog and use the default theme
        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .create()

        view.findViewById<Button>(R.id.btnCancelarConsumo).setOnClickListener { dialog.dismiss() }
        
        view.findViewById<Button>(R.id.btnGuardarConsumo).setOnClickListener {
            val desc = etDesc.text.toString().trim()
            val calStr = etCal.text.toString().trim()
            val calorias = if (calStr.isNotEmpty()) calStr.toDoubleOrNull() else null

            if (desc.isEmpty()) {
                Toast.makeText(this, "Por favor, escribe qué comiste", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val req = AgregarConsumoRequest(
                fecha = apiDateFormat.format(currentDate.time),
                id_dieta_comida = null,
                descripcion = desc,
                calorias = calorias
            )

            guardarConsumo(req, dialog)
        }
        
        dialog.show()
    }

    private fun guardarConsumo(req: AgregarConsumoRequest, dialog: AlertDialog) {
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.instance.agregarConsumoDiario("Bearer $token", req)
                if (resp.isSuccessful) {
                    dialog.dismiss()
                    Toast.makeText(this@ConsumoDiarioActivity, "Consumo registrado", Toast.LENGTH_SHORT).show()
                    actualizarFechaYDatos()
                } else {
                    val errorBody = resp.errorBody()?.string()
                    var errorMessage = "Error al guardar (${resp.code()})"
                    if (!errorBody.isNullOrEmpty()) {
                        try {
                            val json = org.json.JSONObject(errorBody)
                            if (json.has("message")) {
                                errorMessage = json.getString("message")
                            }
                        } catch (e: Exception) {
                            // Ignorar error
                        }
                    }
                    Toast.makeText(this@ConsumoDiarioActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ConsumoDiarioActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun eliminarConsumo(item: ConsumoDiario, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar registro")
            .setMessage("¿Estás seguro de que quieres eliminar '${item.descripcion}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val resp = RetrofitClient.instance.eliminarConsumoDiario("Bearer $token", item.id_consumo)
                        if (resp.isSuccessful) {
                            Toast.makeText(this@ConsumoDiarioActivity, "Registro eliminado", Toast.LENGTH_SHORT).show()
                            actualizarFechaYDatos()
                        } else {
                            Toast.makeText(this@ConsumoDiarioActivity, "Error al eliminar", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@ConsumoDiarioActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}

// ── Adapter ───────────────────────────────────────────────────────────────────

class ConsumoDiarioAdapter(
    private val items: MutableList<ConsumoDiario>,
    private val onDeleteClick: (ConsumoDiario, Int) -> Unit
) : RecyclerView.Adapter<ConsumoDiarioAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvDesc: TextView = v.findViewById(R.id.tvConsumoDescripcion)
        val tvMacros: TextView = v.findViewById(R.id.tvConsumoMacros)
        val btnEliminar: ImageView = v.findViewById(R.id.btnEliminarConsumo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_consumo_diario, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvDesc.text = item.descripcion

        var macros = ""
        if (item.calorias != null) macros += "${item.calorias.toInt()} kcal "
        if (item.proteinas != null) macros += "• ${item.proteinas.toInt()}g Prot "
        if (item.grasas != null) macros += "• ${item.grasas.toInt()}g Gras "
        if (item.carbohidratos != null) macros += "• ${item.carbohidratos.toInt()}g Carbs"

        holder.tvMacros.text = if (macros.isEmpty()) "Sin macros registrados" else macros.trim()
        
        holder.btnEliminar.setOnClickListener { onDeleteClick(item, position) }
    }
}
