package com.axf.gymnet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.axf.gymnet.data.MiReporte

class MisReportesAdapter(
    private val items: List<MiReporte>
) : RecyclerView.Adapter<MisReportesAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvCategoria: TextView = v.findViewById(R.id.tvMiCategoriaReporte)
        val tvSucursal: TextView = v.findViewById(R.id.tvMiSucursalReporte)
        val tvDescripcion: TextView = v.findViewById(R.id.tvMiDescripcionReporte)
        val tvEstado: TextView = v.findViewById(R.id.tvMiEstadoReporte)
        val tvFecha: TextView = v.findViewById(R.id.tvMiFechaReporte)
        val tvPersonal: TextView = v.findViewById(R.id.tvMiPersonalReporte)
        val tvStrikes: TextView = v.findViewById(R.id.tvMiStrikes)
        val tvPrivado: TextView = v.findViewById(R.id.tvMiPrivado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mi_reporte, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val r = items[pos]
        h.tvCategoria.text = formatCategoria(r.categoria)
        h.tvSucursal.text = "📍 ${r.nombre_sucursal}"
        h.tvDescripcion.text = r.descripcion
        h.tvEstado.text = formatEstado(r.estado)
        h.tvFecha.text = "📅 ${r.creado_en.take(10)}"
        h.tvPrivado.text = if (r.es_privado != 0) "🔒 Privado" else "🌐 Público"

        if (!r.nombre_personal_reportado.isNullOrBlank()) {
            h.tvPersonal.text = "👤 Personal: ${r.nombre_personal_reportado}"
            h.tvPersonal.visibility = View.VISIBLE
        } else {
            h.tvPersonal.visibility = View.GONE
        }

        h.tvStrikes.text = when (r.num_strikes) {
            0    -> ""
            1    -> "⚠ Strike 1 — Personal notificado"
            2    -> "⚠⚠ Strike 2 — Gerente notificado"
            else -> "🚨 Strike 3 — Escalada máxima"
        }
        h.tvStrikes.visibility = if (r.num_strikes > 0) View.VISIBLE else View.GONE
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