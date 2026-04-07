package com.axf.gymnet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.axf.gymnet.data.ChatMensaje
import java.text.SimpleDateFormat
import java.util.*

class ChatMensajesAdapter(
    private val mensajes: MutableList<ChatMensaje>,
    private val miRol: String = "suscriptor"  // quien usa la app móvil es suscriptor
) : RecyclerView.Adapter<ChatMensajesAdapter.VH>() {

    var onLongClick: ((ChatMensaje) -> Unit)? = null

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val llBurbuja:    LinearLayout = view.findViewById(R.id.llBurbuja)
        val tvContenido:  TextView     = view.findViewById(R.id.tvContenido)
        val tvHora:       TextView     = view.findViewById(R.id.tvHora)
        val tvTicks:      TextView     = view.findViewById(R.id.tvTicks)
        val tvEditado:    TextView     = view.findViewById(R.id.tvEditado)
        val llReply:      View         = view.findViewById(R.id.llReply)
        val tvReplyCont:  TextView     = view.findViewById(R.id.tvReplyContenido)
        val tvReplyDe:    TextView     = view.findViewById(R.id.tvReplyDe)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val layout = if (viewType == 1) R.layout.item_mensaje_enviado else R.layout.item_mensaje_recibido
        val v = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return VH(v)
    }

    override fun getItemViewType(position: Int): Int {
        return if (mensajes[position].enviado_por == miRol) 1 else 0
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val msg = mensajes[position]

        if (msg.borrado_para == "todos") {
            holder.tvContenido.text = "🚫 Mensaje eliminado"
            holder.tvContenido.alpha = 0.5f
            holder.tvTicks.visibility = View.GONE
            holder.tvEditado.visibility = View.GONE
            holder.llReply.visibility = View.GONE
            holder.tvHora.text = formatHora(msg.enviado_en)
            holder.itemView.setOnLongClickListener(null)
            return
        }

        holder.tvContenido.alpha = 1f
        holder.tvContenido.text = msg.contenido
        holder.tvHora.text = formatHora(msg.enviado_en)

        // Ticks (solo mensajes enviados por este usuario)
        if (msg.enviado_por == miRol) {
            holder.tvTicks.visibility = View.VISIBLE
            holder.tvTicks.text = when {
                msg.leido     == 1 -> "✓✓"
                msg.entregado == 1 -> "✓✓"
                else               -> "✓"
            }
            // Azul = leído, gris = entregado o enviado
            holder.tvTicks.setTextColor(
                if (msg.leido == 1)
                    holder.itemView.context.getColor(android.R.color.holo_blue_light)
                else
                    holder.itemView.context.getColor(android.R.color.darker_gray)
            )
        } else {
            holder.tvTicks.visibility = View.GONE
        }

        // Editado
        holder.tvEditado.visibility = if (msg.editado_en != null) View.VISIBLE else View.GONE

        // Reply/Cita
        if (msg.id_respuesta != null && !msg.respuesta_contenido.isNullOrBlank()) {
            holder.llReply.visibility = View.VISIBLE
            holder.tvReplyDe.text = if (msg.respuesta_enviado_por == miRol) "Tú" else "Entrenador"
            holder.tvReplyCont.text = msg.respuesta_contenido
        } else {
            holder.llReply.visibility = View.GONE
        }

        // Long press para opciones
        holder.itemView.setOnLongClickListener {
            onLongClick?.invoke(msg)
            true
        }
    }

    override fun getItemCount() = mensajes.size

    fun agregar(msg: ChatMensaje) {
        // Evitar duplicados
        if (mensajes.none { it.id_mensaje == msg.id_mensaje }) {
            mensajes.add(msg)
            notifyItemInserted(mensajes.size - 1)
        }
    }

    fun prepend(msgs: List<ChatMensaje>) {
        val nuevos = msgs.filter { n -> mensajes.none { it.id_mensaje == n.id_mensaje } }
        mensajes.addAll(0, nuevos)
        notifyItemRangeInserted(0, nuevos.size)
    }

    fun actualizarMensaje(idMensaje: Int, nuevoContenido: String, editadoEn: String) {
        val idx = mensajes.indexOfFirst { it.id_mensaje == idMensaje }
        if (idx >= 0) {
            mensajes[idx] = mensajes[idx].copy(contenido = nuevoContenido, editado_en = editadoEn)
            notifyItemChanged(idx)
        }
    }

    fun eliminarMensaje(idMensaje: Int) {
        val idx = mensajes.indexOfFirst { it.id_mensaje == idMensaje }
        if (idx >= 0) {
            mensajes[idx] = mensajes[idx].copy(borrado_para = "todos")
            notifyItemChanged(idx)
        }
    }

    fun marcarLeidos(idPersonal: Int) {
        var changed = false
        mensajes.forEachIndexed { i, m ->
            if (m.enviado_por != miRol && m.leido == 0) {
                mensajes[i] = m.copy(leido = 1, entregado = 1)
                changed = true
            }
        }
        if (changed) notifyDataSetChanged()
    }

    fun marcarEntregados() {
        var changed = false
        mensajes.forEachIndexed { i, m ->
            if (m.enviado_por == miRol && m.entregado == 0) {
                mensajes[i] = m.copy(entregado = 1)
                changed = true
            }
        }
        if (changed) notifyDataSetChanged()
    }

    private fun formatHora(iso: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(iso.take(19)) ?: return iso
            val out = SimpleDateFormat("HH:mm", Locale.getDefault())
            out.format(date)
        } catch (e: Exception) {
            iso.take(5)
        }
    }
}