package com.axf.gymnet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.axf.gymnet.data.ChatConversacion

class ChatConversacionesAdapter(
    private val lista: MutableList<ChatConversacion>,
    private val onClick: (ChatConversacion) -> Unit
) : RecyclerView.Adapter<ChatConversacionesAdapter.VH>() {

    // id_personal → está escribiendo
    private val escribiendo = mutableSetOf<Int>()

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvAvatar : TextView = v.findViewById(R.id.tvAvatar)
        val tvNombre : TextView = v.findViewById(R.id.tvNombrePersonal)
        val tvPuesto : TextView = v.findViewById(R.id.tvPuesto)
        val tvUltimo : TextView = v.findViewById(R.id.tvUltimoMensaje)
        val tvBadge  : TextView = v.findViewById(R.id.tvNoLeidos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conversacion, parent, false))

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val c = lista[position]
        holder.tvAvatar.text = c.nombre_personal.firstOrNull()?.uppercase() ?: "?"
        holder.tvNombre.text = c.nombre_personal
        holder.tvPuesto.text = c.puesto ?: ""

        if (escribiendo.contains(c.id_personal)) {
            holder.tvUltimo.text = "Escribiendo..."
        } else {
            holder.tvUltimo.text = c.ultimo_mensaje ?: "Sin mensajes"
        }

        if (c.no_leidos > 0) {
            holder.tvBadge.visibility = View.VISIBLE
            holder.tvBadge.text = c.no_leidos.toString()
        } else {
            holder.tvBadge.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onClick(c) }
    }

    fun reemplazar(nuevaLista: List<ChatConversacion>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    fun actualizarUltimo(idPersonal: Int, mensaje: String, incrementarBadge: Boolean) {
        val idx = lista.indexOfFirst { it.id_personal == idPersonal }
        if (idx == -1) return
        val c = lista[idx]
        lista[idx] = c.copy(
            ultimo_mensaje = mensaje,
            no_leidos = if (incrementarBadge) c.no_leidos + 1 else c.no_leidos
        )
        notifyItemChanged(idx)
    }

    fun limpiarBadge(idPersonal: Int) {
        val idx = lista.indexOfFirst { it.id_personal == idPersonal }
        if (idx == -1) return
        lista[idx] = lista[idx].copy(no_leidos = 0)
        notifyItemChanged(idx)
    }

    fun setEscribiendo(idPersonal: Int, activo: Boolean) {
        if (activo) escribiendo.add(idPersonal) else escribiendo.remove(idPersonal)
        val idx = lista.indexOfFirst { it.id_personal == idPersonal }
        if (idx != -1) notifyItemChanged(idx)
    }
}