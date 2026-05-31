package com.axf.gymnet

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.axf.gymnet.data.CapturarOrdenTiendaRequest
import com.axf.gymnet.data.CrearOrdenTiendaRequest
import com.axf.gymnet.network.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

class PagoTarjetaActivity : AppCompatActivity() {

    private var token = ""
    private val gson = Gson()
    private lateinit var webView: WebView

    private val nombre: String by lazy { intent.getStringExtra("nombre") ?: "Plan" }
    private val precio: Double by lazy { intent.getDoubleExtra("precio", 0.0) }
    private val esPromocion: Boolean by lazy { intent.getBooleanExtra("es_promocion", false) }
    private val idTipo: Int by lazy { intent.getIntExtra("id_tipo", 0) }
    private val idPromocion: Int by lazy { intent.getIntExtra("id_promocion", 0) }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pago_tarjeta)

        token = getSharedPreferences("axf_prefs", MODE_PRIVATE).getString("token", "") ?: ""
        if (token.isEmpty()) {
            Toast.makeText(this, "Sesión no válida.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val fmt = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        findViewById<TextView>(R.id.tvPagoPlan).text = "$nombre — ${fmt.format(precio)}"
        findViewById<TextView>(R.id.tvPagoBack).setOnClickListener { finish() }

        webView = findViewById(R.id.webPayPal)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.addJavascriptInterface(PayPalBridge(), "AndroidBridge")
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                cargarPayPal()
            }
        }

        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.instance.getPayPalConfig("Bearer $token")
                if (!resp.isSuccessful || resp.body()?.clientId.isNullOrBlank()) {
                    Toast.makeText(this@PagoTarjetaActivity, "PayPal no configurado en el servidor.", Toast.LENGTH_LONG).show()
                    finish()
                    return@launch
                }
                val clientId = resp.body()!!.clientId
                findViewById<ProgressBar>(R.id.pbPago).visibility = View.GONE
                webView.visibility = View.VISIBLE
                webView.loadUrl("file:///android_asset/paypal_card.html")
                webView.tag = clientId
            } catch (e: Exception) {
                Toast.makeText(this@PagoTarjetaActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun cargarPayPal() {
        val clientId = webView.tag as? String ?: return
        val config = mapOf(
            "clientId" to clientId,
            "nombre" to nombre,
            "precio" to precio,
            "idTipo" to (if (esPromocion) 0 else idTipo),
            "idPromocion" to (if (esPromocion) idPromocion else 0)
        )
        val json = gson.toJson(config).replace("\\", "\\\\").replace("'", "\\'")
        webView.evaluateJavascript("start('$json');", null)
    }

    inner class PayPalBridge {

        @JavascriptInterface
        fun crearOrden(idTipoJs: Int, idPromoJs: Int): String = runBlocking {
            try {
                val req = CrearOrdenTiendaRequest(
                    idTipo = if (idTipoJs > 0) idTipoJs else null,
                    idPromocion = if (idPromoJs > 0) idPromoJs else null
                )
                val resp = RetrofitClient.instance.crearOrdenTienda("Bearer $token", req)
                if (resp.isSuccessful && resp.body()?.orderId != null) {
                    gson.toJson(mapOf("order_id" to resp.body()!!.orderId))
                } else {
                    val msg = resp.errorBody()?.string()?.let {
                        try { JSONObject(it).optString("message", "Error al crear orden") } catch (_: Exception) { "Error al crear orden" }
                    } ?: "Error al crear orden"
                    gson.toJson(mapOf("message" to msg))
                }
            } catch (e: Exception) {
                gson.toJson(mapOf("message" to (e.message ?: "Error de red")))
            }
        }

        @JavascriptInterface
        fun capturarOrden(orderId: String, idTipoJs: Int, idPromoJs: Int): String = runBlocking {
            try {
                val req = CapturarOrdenTiendaRequest(
                    orderId = orderId,
                    idTipo = if (idTipoJs > 0) idTipoJs else null,
                    idPromocion = if (idPromoJs > 0) idPromoJs else null
                )
                val resp = RetrofitClient.instance.capturarOrdenTienda("Bearer $token", req)
                if (resp.isSuccessful) {
                    val body = resp.body()
                    gson.toJson(mapOf(
                        "ok" to (body?.ok == true),
                        "message" to body?.message,
                        "suscripcion" to body?.suscripcion
                    ))
                } else {
                    gson.toJson(mapOf("ok" to false, "message" to "Error del servidor (${resp.code()})"))
                }
            } catch (e: Exception) {
                gson.toJson(mapOf("ok" to false, "message" to (e.message ?: "Error de red")))
            }
        }

        @JavascriptInterface
        fun onSuccess(jsonStr: String) {
            runOnUiThread {
                try {
                    val obj = JSONObject(jsonStr)
                    val sub = obj.optJSONObject("suscripcion")
                    val planNombre = sub?.optString("plan_nombre") ?: nombre
                    val fechaFin   = sub?.optString("fecha_fin") ?: ""

                    getSharedPreferences("axf_prefs", MODE_PRIVATE).edit()
                        .putBoolean("suscripcionActiva", true)
                        .putString("fechaVencimiento", fechaFin)
                        .apply()

                    AlertDialog.Builder(this@PagoTarjetaActivity)
                        .setTitle("¡Pago exitoso!")
                        .setMessage("Se activó \"$planNombre\" correctamente.")
                        .setPositiveButton("Aceptar") { _, _ ->
                            setResult(RESULT_OK)
                            finish()
                        }
                        .setCancelable(false)
                        .show()
                } catch (_: Exception) {
                    Toast.makeText(this@PagoTarjetaActivity, "Pago completado.", Toast.LENGTH_LONG).show()
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
    }
}
