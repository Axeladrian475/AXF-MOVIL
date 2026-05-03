package com.axf.gymnet

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.axf.gymnet.data.CrearReporteRequest
import com.axf.gymnet.data.PersonalItem
import com.axf.gymnet.data.SucursalItem
import com.axf.gymnet.network.RetrofitClient
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class ReportarActivity : AppCompatActivity() {

    private var token = ""

    // Tabs
    private lateinit var tabLayout: TabLayout

    // TAB 0 — Nuevo reporte
    private lateinit var layoutNuevoReporte: LinearLayout
    private lateinit var spinnerSucursal: Spinner
    private lateinit var spinnerCategoria: Spinner
    private lateinit var etDescripcion: TextInputEditText
    private lateinit var cbPrivado: CheckBox
    private lateinit var layoutPersonal: LinearLayout
    private lateinit var spinnerPersonal: Spinner
    private lateinit var layoutAtencionPrevia: LinearLayout
    private lateinit var cbSobreAtencion: CheckBox
    private lateinit var btnEnviarReporte: Button
    private lateinit var tvEstadoEnvio: TextView

    // TAB 1 — Públicos
    private lateinit var layoutReportesPublicos: LinearLayout
    private lateinit var spinnerSucursalPublicos: Spinner
    private lateinit var rvReportesPublicos: RecyclerView
    private lateinit var tvSinReportes: TextView
    private lateinit var pbPublicos: ProgressBar

    // TAB 2 — Mis reportes
    private lateinit var layoutMisReportes: LinearLayout
    private lateinit var rvMisReportes: RecyclerView
    private lateinit var tvSinMisReportes: TextView
    private lateinit var pbMisReportes: ProgressBar

    // Estado
    private var sucursales: List<SucursalItem> = emptyList()
    private var personalLista: List<PersonalItem> = emptyList()
    private var sucursalSeleccionadaId: Int = -1
    private var personalSeleccionadoId: Int = -1
    private var tuvoAtencionPrevia: Boolean = false
    private var tabActual: Int = 0

    private val categorias = listOf(
        "Máquina Dañada", "Baño Tapado",
        "Problema de Limpieza", "Reporte de Personal", "Otro"
    )
    private val categoriasApi = listOf(
        "Maquina_Dañada", "Baño_Tapado",
        "Problema_Limpieza", "Reporte_Personal", "Otro"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reportar)

        token = getSharedPreferences("axf_prefs", MODE_PRIVATE).getString("token", "") ?: ""

        bindViews()
        setupTabs()
        setupBtnVolver()
        setupCategoriasSpinner()
        setupEnviarReporte()

        // Cargar sucursales → cuando terminen, puebla ambos spinners y carga datos
        cargarSucursales()
        cargarMisReportes()
    }

    // ── Bind ──────────────────────────────────────────────────────────────────

    private fun bindViews() {
        tabLayout = findViewById(R.id.tabLayoutReportes)

        layoutNuevoReporte   = findViewById(R.id.layoutNuevoReporte)
        spinnerSucursal      = findViewById(R.id.spinnerSucursal)
        spinnerCategoria     = findViewById(R.id.spinnerCategoria)
        etDescripcion        = findViewById(R.id.etDescripcion)
        cbPrivado            = findViewById(R.id.cbPrivado)
        layoutPersonal       = findViewById(R.id.layoutPersonal)
        spinnerPersonal      = findViewById(R.id.spinnerPersonal)
        layoutAtencionPrevia = findViewById(R.id.layoutAtencionPrevia)
        cbSobreAtencion      = findViewById(R.id.cbSobreAtencion)
        btnEnviarReporte     = findViewById(R.id.btnEnviarReporte)
        tvEstadoEnvio        = findViewById(R.id.tvEstadoEnvio)

        layoutReportesPublicos  = findViewById(R.id.layoutReportesPublicos)
        spinnerSucursalPublicos = findViewById(R.id.spinnerSucursalPublicos)
        rvReportesPublicos      = findViewById(R.id.rvReportesPublicos)
        tvSinReportes           = findViewById(R.id.tvSinReportes)
        pbPublicos              = findViewById(R.id.pbPublicos)

        layoutMisReportes  = findViewById(R.id.layoutMisReportes)
        rvMisReportes      = findViewById(R.id.rvMisReportes)
        tvSinMisReportes   = findViewById(R.id.tvSinMisReportes)
        pbMisReportes      = findViewById(R.id.pbMisReportes)

        rvReportesPublicos.layoutManager = LinearLayoutManager(this)
        rvMisReportes.layoutManager = LinearLayoutManager(this)
    }

    private fun setupBtnVolver() {
        findViewById<View>(R.id.btnVolverReportar).setOnClickListener { finish() }
    }

    // ── Tabs ──────────────────────────────────────────────────────────────────

    private fun setupTabs() {
        mostrarTab(0)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                tabActual = tab.position
                mostrarTab(tab.position)
                // Al entrar al tab Públicos con sucursal ya cargada, recargar
                if (tab.position == 1 && sucursales.isNotEmpty()) {
                    cargarReportesPublicos(sucursales[spinnerSucursalPublicos.selectedItemPosition].id_sucursal)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun mostrarTab(pos: Int) {
        layoutNuevoReporte.visibility     = if (pos == 0) View.VISIBLE else View.GONE
        layoutReportesPublicos.visibility = if (pos == 1) View.VISIBLE else View.GONE
        layoutMisReportes.visibility      = if (pos == 2) View.VISIBLE else View.GONE
    }

    // ── Sucursales ────────────────────────────────────────────────────────────

    private fun cargarSucursales() {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.instance.getSucursales("Bearer $token")
                if (res.isSuccessful) {
                    sucursales = res.body() ?: emptyList()
                    if (sucursales.isNotEmpty()) {
                        poblarSpinnerSucursal()
                        poblarSpinnerSucursalPublicos()
                        // Auto-cargar reportes públicos de la primera sucursal
                        cargarReportesPublicos(sucursales[0].id_sucursal)
                    }
                } else {
                    Toast.makeText(this@ReportarActivity, "No se pudieron cargar las sucursales", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ReportarActivity, "Sin conexión al servidor", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun poblarSpinnerSucursal() {
        val nombres = sucursales.map { it.nombre }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombres)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSucursal.adapter = adapter

        // Seleccionar la sucursal del suscriptor por defecto si está en la lista
        val prefs = getSharedPreferences("axf_prefs", MODE_PRIVATE)
        val sucursalIdUsuario = prefs.getInt("sucursalId", -1)
        val indexUsuario = sucursales.indexOfFirst { it.id_sucursal == sucursalIdUsuario }
        if (indexUsuario >= 0) spinnerSucursal.setSelection(indexUsuario)

        spinnerSucursal.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>, v: View?, pos: Int, id: Long) {
                sucursalSeleccionadaId = sucursales[pos].id_sucursal
                if (spinnerCategoria.selectedItemPosition == 3) cargarPersonal(sucursalSeleccionadaId)
            }
            override fun onNothingSelected(p: AdapterView<*>) {}
        }
        // Forzar el ID inicial
        if (sucursales.isNotEmpty()) {
            sucursalSeleccionadaId = sucursales[
                if (indexUsuario >= 0) indexUsuario else 0
            ].id_sucursal
        }
    }

    private fun poblarSpinnerSucursalPublicos() {
        val nombres = sucursales.map { it.nombre }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombres)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSucursalPublicos.adapter = adapter

        spinnerSucursalPublicos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>, v: View?, pos: Int, id: Long) {
                cargarReportesPublicos(sucursales[pos].id_sucursal)
            }
            override fun onNothingSelected(p: AdapterView<*>) {}
        }
    }

    // ── Categorías ────────────────────────────────────────────────────────────

    private fun setupCategoriasSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = adapter
        spinnerCategoria.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>, v: View?, pos: Int, id: Long) {
                val esPersonal = (pos == 3)
                layoutPersonal.visibility = if (esPersonal) View.VISIBLE else View.GONE
                if (!esPersonal) {
                    layoutAtencionPrevia.visibility = View.GONE
                    cbSobreAtencion.isChecked = false
                } else if (sucursalSeleccionadaId != -1) {
                    cargarPersonal(sucursalSeleccionadaId)
                }
            }
            override fun onNothingSelected(p: AdapterView<*>) {}
        }
    }

    // ── Personal ──────────────────────────────────────────────────────────────

    private fun cargarPersonal(idSucursal: Int) {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.instance.getPersonalSucursal("Bearer $token", idSucursal)
                if (res.isSuccessful) {
                    personalLista = res.body() ?: emptyList()
                    poblarSpinnerPersonal()
                }
            } catch (_: Exception) {}
        }
    }

    private fun poblarSpinnerPersonal() {
        val nombres = personalLista.map {
            "${it.nombres} ${it.apellido_paterno} (${it.puesto.replace('_', ' ')})"
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombres)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPersonal.adapter = adapter
        spinnerPersonal.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>, v: View?, pos: Int, id: Long) {
                if (pos in personalLista.indices) {
                    personalSeleccionadoId = personalLista[pos].id_personal
                    verificarAtencionPrevia(personalSeleccionadoId)
                }
            }
            override fun onNothingSelected(p: AdapterView<*>) {}
        }
    }

    private fun verificarAtencionPrevia(idPersonal: Int) {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.instance.verificarAtencionPrevia("Bearer $token", idPersonal)
                if (res.isSuccessful) {
                    tuvoAtencionPrevia = (res.body()?.tuvo_atencion ?: 0) != 0
                    layoutAtencionPrevia.visibility = if (tuvoAtencionPrevia) View.VISIBLE else View.GONE
                }
            } catch (_: Exception) {}
        }
    }

    // ── Enviar reporte ────────────────────────────────────────────────────────

    private fun setupEnviarReporte() {
        btnEnviarReporte.setOnClickListener {
            val descripcion = etDescripcion.text?.toString()?.trim() ?: ""
            tvEstadoEnvio.visibility = View.GONE

            if (sucursalSeleccionadaId == -1) {
                tvEstadoEnvio.text = "⚠ Selecciona una sucursal"
                tvEstadoEnvio.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark))
                tvEstadoEnvio.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (descripcion.isEmpty()) {
                tvEstadoEnvio.text = "⚠ Escribe una descripción del problema"
                tvEstadoEnvio.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark))
                tvEstadoEnvio.visibility = View.VISIBLE
                return@setOnClickListener
            }

            val catPos = spinnerCategoria.selectedItemPosition
            val categoriaApi = categoriasApi[catPos]
            val esPersonal = (catPos == 3)

            val request = CrearReporteRequest(
                id_sucursal           = sucursalSeleccionadaId,
                categoria             = categoriaApi,
                descripcion           = descripcion,
                es_privado            = cbPrivado.isChecked,
                id_personal_reportado = if (esPersonal && personalSeleccionadoId != -1) personalSeleccionadoId else null,
                sobre_atencion_previa = if (esPersonal && tuvoAtencionPrevia) cbSobreAtencion.isChecked else null
            )

            btnEnviarReporte.isEnabled = false

            lifecycleScope.launch {
                try {
                    val res = RetrofitClient.instance.crearReporte("Bearer $token", request)
                    if (res.isSuccessful && res.body()?.success == true) {
                        tvEstadoEnvio.text = "✅ Reporte enviado correctamente"
                        tvEstadoEnvio.setTextColor(ContextCompat.getColor(this@ReportarActivity, android.R.color.holo_green_dark))
                        etDescripcion.setText("")
                        cbPrivado.isChecked = false
                        cbSobreAtencion.isChecked = false
                        cargarMisReportes()
                    } else {
                        val msg = res.body()?.message ?: "Error al enviar el reporte"
                        tvEstadoEnvio.text = "❌ $msg"
                        tvEstadoEnvio.setTextColor(ContextCompat.getColor(this@ReportarActivity, android.R.color.holo_red_dark))
                    }
                } catch (e: Exception) {
                    tvEstadoEnvio.text = "❌ Sin conexión: ${e.message}"
                    tvEstadoEnvio.setTextColor(ContextCompat.getColor(this@ReportarActivity, android.R.color.holo_red_dark))
                } finally {
                    tvEstadoEnvio.visibility = View.VISIBLE
                    btnEnviarReporte.isEnabled = true
                }
            }
        }
    }

    // ── Reportes públicos ─────────────────────────────────────────────────────

    private fun cargarReportesPublicos(idSucursal: Int) {
        pbPublicos.visibility = View.VISIBLE
        tvSinReportes.visibility = View.GONE
        rvReportesPublicos.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val res = RetrofitClient.instance.getReportesPublicos("Bearer $token", idSucursal)
                pbPublicos.visibility = View.GONE
                if (res.isSuccessful) {
                    val lista = res.body()?.reportes ?: emptyList()
                    if (lista.isEmpty()) {
                        tvSinReportes.text = "No hay reportes públicos activos en esta sucursal."
                        tvSinReportes.visibility = View.VISIBLE
                    } else {
                        rvReportesPublicos.visibility = View.VISIBLE
                        rvReportesPublicos.adapter = ReportesPublicosAdapter(lista) { idReporte ->
                            sumarseReporte(idReporte)
                        }
                    }
                } else {
                    tvSinReportes.text = "Error al cargar reportes (${res.code()})"
                    tvSinReportes.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                pbPublicos.visibility = View.GONE
                tvSinReportes.text = "Sin conexión: ${e.message}"
                tvSinReportes.visibility = View.VISIBLE
            }
        }
    }

    private fun sumarseReporte(idReporte: Int) {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.instance.sumarseReporte("Bearer $token", idReporte)
                Toast.makeText(this@ReportarActivity, res.body()?.message ?: "OK", Toast.LENGTH_SHORT).show()
                if (sucursales.isNotEmpty()) {
                    cargarReportesPublicos(sucursales[spinnerSucursalPublicos.selectedItemPosition].id_sucursal)
                }
            } catch (_: Exception) {
                Toast.makeText(this@ReportarActivity, "Error de red", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ── Mis reportes ──────────────────────────────────────────────────────────

    private fun cargarMisReportes() {
        pbMisReportes.visibility = View.VISIBLE
        tvSinMisReportes.visibility = View.GONE
        rvMisReportes.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val res = RetrofitClient.instance.getMisReportes("Bearer $token")
                pbMisReportes.visibility = View.GONE
                if (res.isSuccessful) {
                    val lista = res.body()?.reportes ?: emptyList()
                    if (lista.isEmpty()) {
                        tvSinMisReportes.text = "Aún no has enviado ningún reporte."
                        tvSinMisReportes.visibility = View.VISIBLE
                    } else {
                        rvMisReportes.visibility = View.VISIBLE
                        rvMisReportes.adapter = MisReportesAdapter(lista)
                    }
                } else {
                    tvSinMisReportes.text = "Error al cargar reportes (${res.code()})"
                    tvSinMisReportes.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                pbMisReportes.visibility = View.GONE
                tvSinMisReportes.text = "Sin conexión: ${e.message}"
                tvSinMisReportes.visibility = View.VISIBLE
            }
        }
    }
}