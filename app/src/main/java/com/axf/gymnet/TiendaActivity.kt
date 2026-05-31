package com.axf.gymnet

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.axf.gymnet.data.PlanTienda
import com.axf.gymnet.network.RetrofitClient
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class TiendaActivity : AppCompatActivity() {

    private data class TiendaItem(
        val plan: PlanTienda,
        val esPromo: Boolean,
        val descripcion: String? = null
    )

    private var token = ""
    private var tieneActiva = false
    private var vencimientoFinal: String? = null
    private val fmtPrecio = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tienda)

        token = getSharedPreferences("axf_prefs", MODE_PRIVATE).getString("token", "") ?: ""
        if (token.isEmpty()) {
            Toast.makeText(this, "Sesión no válida.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        findViewById<TextView>(R.id.tvTiendaBack).setOnClickListener { finish() }
        cargarCatalogo()
    }

    private fun cargarCatalogo() {
        val pb     = findViewById<ProgressBar>(R.id.pbTienda)
        val scroll = findViewById<NestedScrollView>(R.id.scrollTienda)

        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.instance.getCatalogoTienda("Bearer $token")
                if (!resp.isSuccessful) {
                    Toast.makeText(this@TiendaActivity, "Error al cargar catálogo (${resp.code()})", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val data = resp.body() ?: return@launch
                tieneActiva     = data.tieneSuscripcionActiva
                vencimientoFinal = data.vencimientoFinal

                pb.visibility = View.GONE
                scroll.visibility = View.VISIBLE

                val tvInfo = findViewById<TextView>(R.id.tvTiendaInfo)
                if (tieneActiva && !vencimientoFinal.isNullOrBlank()) {
                    tvInfo.visibility = View.VISIBLE
                    tvInfo.text = "Tienes suscripción activa. Los planes con días se acumulan al vencimiento (${fmtFecha(vencimientoFinal!!)})."
                }

                setupLista(
                    findViewById(R.id.rvSuscripciones),
                    findViewById(R.id.tvSinSuscripciones),
                    data.suscripciones.map { TiendaItem(it, false) }
                )
                setupLista(
                    findViewById(R.id.rvPromociones),
                    findViewById(R.id.tvSinPromociones),
                    data.promociones.map { promo ->
                        TiendaItem(
                            plan = PlanTienda(
                                idTipo = promo.idPromocion,
                                nombre = promo.nombre,
                                duracionDias = promo.duracionDias,
                                precio = promo.precio,
                                sesionesNutriologo = promo.sesionesNutriologo,
                                sesionesEntrenador = promo.sesionesEntrenador
                            ),
                            esPromo = true,
                            descripcion = promo.descripcion
                        )
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(this@TiendaActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupLista(
        rv: RecyclerView,
        tvVacio: TextView,
        items: List<TiendaItem>
    ) {
        if (items.isEmpty()) {
            tvVacio.visibility = View.VISIBLE
            rv.visibility = View.GONE
            return
        }
        tvVacio.visibility = View.GONE
        rv.visibility = View.VISIBLE
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = TiendaAdapter(items) { item ->
            val plan = item.plan
            if (item.esPromo && plan.duracionDias <= 0 && !tieneActiva) {
                Toast.makeText(
                    this,
                    "Esta promoción solo agrega sesiones y requiere una suscripción activa.",
                    Toast.LENGTH_LONG
                ).show()
                return@TiendaAdapter
            }
            val intent = Intent(this, PagoTarjetaActivity::class.java).apply {
                putExtra("nombre", plan.nombre)
                putExtra("precio", plan.precio)
                putExtra("es_promocion", item.esPromo)
                if (item.esPromo) putExtra("id_promocion", plan.idTipo)
                else putExtra("id_tipo", plan.idTipo)
            }
            startActivity(intent)
        }
    }

    private fun fmtFecha(iso: String): String = try {
        val inFmt  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        outFmt.format(inFmt.parse(iso)!!)
    } catch (_: Exception) { iso }

    private class TiendaAdapter(
        private val items: List<TiendaItem>,
        private val onComprar: (TiendaItem) -> Unit
    ) : RecyclerView.Adapter<TiendaAdapter.VH>() {

        private val fmtPrecio = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvNombre: TextView = v.findViewById(R.id.tvItemNombre)
            val tvPrecio: TextView = v.findViewById(R.id.tvItemPrecio)
            val tvDesc: TextView   = v.findViewById(R.id.tvItemDescripcion)
            val tvDetalle: TextView = v.findViewById(R.id.tvItemDetalle)
            val btnComprar: Button = v.findViewById(R.id.btnComprar)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_tienda_plan, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            val plan = item.plan
            holder.tvNombre.text = plan.nombre
            holder.tvPrecio.text = fmtPrecio.format(plan.precio)

            if (item.esPromo) {
                holder.tvDesc.text = item.descripcion?.takeIf { it.isNotBlank() } ?: "Promoción especial"
                holder.tvDesc.visibility = View.VISIBLE
            } else {
                holder.tvDesc.visibility = View.GONE
            }

            val detalle = buildString {
                if (plan.duracionDias > 0) append("${plan.duracionDias} días")
                else if (item.esPromo) append("Solo sesiones")
                if (plan.sesionesEntrenador > 0) {
                    if (isNotEmpty()) append(" · ")
                    append("${plan.sesionesEntrenador} ses. entrenador")
                }
                if (plan.sesionesNutriologo > 0) {
                    if (isNotEmpty()) append(" · ")
                    append("${plan.sesionesNutriologo} ses. nutriólogo")
                }
            }
            holder.tvDetalle.text = detalle.ifBlank { "Acceso al gimnasio" }

            holder.btnComprar.setOnClickListener { onComprar(item) }
        }

        override fun getItemCount() = items.size
    }
}
