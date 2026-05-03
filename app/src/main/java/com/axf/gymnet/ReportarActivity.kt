package com.axf.gymnet

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.axf.gymnet.data.PersonalItem
import com.axf.gymnet.data.SucursalItem
import com.axf.gymnet.network.RetrofitClient
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

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

    // Foto
    private lateinit var btnSeleccionarFoto: View
    private lateinit var ivFotoPreview: ImageView
    private lateinit var tvFotoStatus: TextView
    private var fotoUri: Uri? = null

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

    // Launcher para seleccionar fotos (Photo Picker moderno)
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            fotoUri = uri
            ivFotoPreview.setImageURI(uri)
            ivFotoPreview.alpha = 1.0f
            ivFotoPreview.setPadding(0, 0, 0, 0)
            tvFotoStatus.text = "Foto seleccionada"
            tvFotoStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reportar)

        token = getSharedPreferences("axf_prefs", MODE_PRIVATE).getString("token", "") ?: ""

        bindViews()
        setupTabs()
        setupBtnVolver()
        setupCategoriasSpinner()
        setupFotoPicker()
        setupEnviarReporte()

        cargarSucursales()
        cargarMisReportes()
    }

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

        btnSeleccionarFoto   = findViewById(R.id.btnSeleccionarFoto)
        ivFotoPreview        = findViewById(R.id.ivFotoPreview)
        tvFotoStatus         = findViewById(R.id.tvFotoStatus)

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

    private fun setupFotoPicker() {
        btnSeleccionarFoto.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun setupTabs() {
        mostrarTab(0)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                tabActual = tab.position
                mostrarTab(tab.position)
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

    private fun cargarSucursales() {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.instance.getSucursales("Bearer $token")
                if (res.isSuccessful) {
                    sucursales = res.body() ?: emptyList()
                    if (sucursales.isNotEmpty()) {
                        poblarSpinnerSucursal()
                        poblarSpinnerSucursalPublicos()
                        cargarReportesPublicos(sucursales[0].id_sucursal)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun poblarSpinnerSucursal() {
        val nombres = sucursales.map { it.nombre }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombres)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSucursal.adapter = adapter

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
        if (sucursales.isNotEmpty()) {
            sucursalSeleccionadaId = sucursales[if (indexUsuario >= 0) indexUsuario else 0].id_sucursal
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
        val nombres = personalLista.map { "${it.nombres} ${it.apellido_paterno} (${it.puesto.replace('_', ' ')})" }
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

    private fun setupEnviarReporte() {
        btnEnviarReporte.setOnClickListener {
            val descripcion = etDescripcion.text?.toString()?.trim() ?: ""
            if (sucursalSeleccionadaId == -1 || descripcion.isEmpty()) {
                tvEstadoEnvio.text = "⚠ Completa los campos obligatorios"
                tvEstadoEnvio.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark))
                tvEstadoEnvio.visibility = View.VISIBLE
                return@setOnClickListener
            }

            val catPos = spinnerCategoria.selectedItemPosition
            val categoriaApi = categoriasApi[catPos]
            val esPersonal = (catPos == 3)

            // Preparar partes Multipart
            val idSucursalBody = sucursalSeleccionadaId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val categoriaBody = categoriaApi.toRequestBody("text/plain".toMediaTypeOrNull())
            val descripcionBody = descripcion.toRequestBody("text/plain".toMediaTypeOrNull())
            val esPrivadoBody = cbPrivado.isChecked.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val idPersonalBody = if (esPersonal && personalSeleccionadoId != -1)
                personalSeleccionadoId.toString().toRequestBody("text/plain".toMediaTypeOrNull()) else null
            val sobreAtencionBody = if (esPersonal && tuvoAtencionPrevia)
                cbSobreAtencion.isChecked.toString().toRequestBody("text/plain".toMediaTypeOrNull()) else null

            btnEnviarReporte.isEnabled = false
            tvEstadoEnvio.text = "Enviando reporte..."
            tvEstadoEnvio.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
            tvEstadoEnvio.visibility = View.VISIBLE

            // Capturar fotoUri antes de entrar a la coroutine
            val fotoUriSnapshot = fotoUri

            lifecycleScope.launch {
                // Procesar la imagen DENTRO de la coroutine (Dispatchers.IO implícito por lifecycleScope)
                var fotoPart: MultipartBody.Part? = null
                fotoUriSnapshot?.let { uri ->
                    try {
                        val file = uriToFile(uri)
                        if (file.exists() && file.length() > 0) {
                            // Detectar tipo MIME real a partir de la extensión
                            val ext = file.name.substringAfterLast('.', "jpg").lowercase()
                            val mimeType = when (ext) {
                                "png"  -> "image/png"
                                "gif"  -> "image/gif"
                                "webp" -> "image/webp"
                                else   -> "image/jpeg"
                            }
                            val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                            fotoPart = MultipartBody.Part.createFormData("foto", file.name, requestFile)
                        }
                    } catch (e: Exception) {
                        // No bloquear el envío del reporte si falla la foto
                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                            Toast.makeText(this@ReportarActivity,
                                "No se pudo adjuntar la foto: ${e.message}",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                try {
                    val res = RetrofitClient.instance.crearReporteMultipart(
                        "Bearer $token", idSucursalBody, categoriaBody, descripcionBody,
                        esPrivadoBody, idPersonalBody, sobreAtencionBody, fotoPart
                    )
                    if (res.isSuccessful && res.body()?.success == true) {
                        tvEstadoEnvio.text = "✅ Reporte enviado correctamente"
                        tvEstadoEnvio.setTextColor(ContextCompat.getColor(this@ReportarActivity, android.R.color.holo_green_dark))
                        resetForm()
                        cargarMisReportes()
                    } else {
                        val msg = res.body()?.message ?: "Error al enviar"
                        tvEstadoEnvio.text = "❌ $msg"
                        tvEstadoEnvio.setTextColor(ContextCompat.getColor(this@ReportarActivity, android.R.color.holo_red_dark))
                    }
                } catch (e: Exception) {
                    tvEstadoEnvio.text = "❌ Sin conexión: ${e.message}"
                    tvEstadoEnvio.setTextColor(ContextCompat.getColor(this@ReportarActivity, android.R.color.holo_red_dark))
                } finally {
                    btnEnviarReporte.isEnabled = true
                }
            }
        }
    }

    private fun resetForm() {
        etDescripcion.setText("")
        cbPrivado.isChecked = false
        cbSobreAtencion.isChecked = false
        fotoUri = null
        ivFotoPreview.setImageResource(R.drawable.ic_fire)
        ivFotoPreview.alpha = 0.5f
        ivFotoPreview.setPadding(40, 40, 40, 40)
        tvFotoStatus.text = "Toca para subir una foto"
        tvFotoStatus.setTextColor(ContextCompat.getColor(this, R.color.axf_text_secondary))
    }

    private fun uriToFile(uri: Uri): File {
        val fileName = getFileName(uri)
        val tempFile = File(cacheDir, fileName)
        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("No se pudo abrir la imagen: URI inaccesible")
        tempFile.outputStream().use { output ->
            inputStream.use { input ->
                input.copyTo(output)
            }
        }
        if (tempFile.length() == 0L) throw IllegalStateException("La imagen está vacía")
        return tempFile
    }

    private fun getFileName(uri: Uri): String {
        var name = "temp_image.jpg"
        contentResolver.query(uri, null, null, null, null)?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) name = it.getString(index)
            }
        }
        return name
    }

    private fun cargarReportesPublicos(idSucursal: Int) {
        pbPublicos.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.instance.getReportesPublicos("Bearer $token", idSucursal)
                pbPublicos.visibility = View.GONE
                if (res.isSuccessful) {
                    val lista = res.body()?.reportes ?: emptyList()
                    tvSinReportes.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
                    rvReportesPublicos.visibility = if (lista.isEmpty()) View.GONE else View.VISIBLE
                    rvReportesPublicos.adapter = ReportesPublicosAdapter(lista) { id -> sumarseReporte(id) }
                }
            } catch (_: Exception) { pbPublicos.visibility = View.GONE }
        }
    }

    private fun sumarseReporte(idReporte: Int) {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.instance.sumarseReporte("Bearer $token", idReporte)
                Toast.makeText(this@ReportarActivity, res.body()?.message ?: "OK", Toast.LENGTH_SHORT).show()
                cargarReportesPublicos(sucursales[spinnerSucursalPublicos.selectedItemPosition].id_sucursal)
            } catch (_: Exception) {}
        }
    }

    private fun cargarMisReportes() {
        pbMisReportes.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.instance.getMisReportes("Bearer $token")
                pbMisReportes.visibility = View.GONE
                if (res.isSuccessful) {
                    val lista = res.body()?.reportes ?: emptyList()
                    tvSinMisReportes.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
                    rvMisReportes.visibility = if (lista.isEmpty()) View.GONE else View.VISIBLE
                    rvMisReportes.adapter = MisReportesAdapter(lista)
                }
            } catch (_: Exception) { pbMisReportes.visibility = View.GONE }
        }
    }
}