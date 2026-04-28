package com.axf.gymnet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.axf.gymnet.data.ReportePublico

class ReportesPublicosAdapter(
    private val items: List<ReportePublico>,
    private val onSumar: (Int) -> Unit
) : RecyclerView.Adapter<ReportesPublicosAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvCategoria: TextView = v.findViewById(R.id.tvCategoriaReporte)
        val tvDescripcion: TextView = v.findViewById(R.id.tvDescripcionReporte)
        val tvEstado: TextView = v.findViewById(R.id.tvEstadoReporte)
        val tvSumados: TextView = v.findViewById(R.id.tvSumados)
        val tvStrikes: TextView = v.findViewById(R.id.tvStrikes)
        val btnSumar: Button = v.findViewById(R.id.btnSumarReporte)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reporte_publico, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val r = items[pos]
        h.tvCategoria.text = formatCategoria(r.categoria)
        h.tvDescripcion.text = r.descripcion
        h.tvEstado.text = formatEstado(r.estado)
        h.tvSumados.text = "👥 ${r.sumados} personas reportaron esto"
        h.tvStrikes.text = if (r.num_strikes > 0) "⚠ ${r.num_strikes} strike(s)" else ""
        h.tvStrikes.visibility = if (r.num_strikes > 0) View.VISIBLE else View.GONE

        if (r.ya_sumado != 0) {
            h.btnSumar.text = "✔ Ya te sumaste"
            h.btnSumar.isEnabled = false
            h.btnSumar.alpha = 0.5f
        } else {
            h.btnSumar.text = "Sumarme al reporte"
            h.btnSumar.isEnabled = true
            h.btnSumar.alpha = 1f
            h.btnSumar.setOnClickListener { onSumar(r.id_reporte) }
        }
    }

    private fun formatCategoria(cat: String) = when (cat) {
        "Maquina_Dañada"     -> "🔧 Máquina Dañada"
        "Baño_Tapado"        -> "🚽 Baño Tapado"
        "Problema_Limpieza"  -> "🧹 Problema de Limpieza"
        "Reporte_Personal"   -> "👤 Reporte de Personal"
        else                 -> "📋 Otro"
    }

    private fun formatEstado(estado: String) = when (estado) {
        "Abierto"    -> "🔴 Abierto"
        "En_Proceso" -> "🟡 En proceso"
        "Resuelto"   -> "🟢 Resuelto"
        else         -> estado
    }
}