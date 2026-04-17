package com.axf.gymnet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.axf.gymnet.data.ChatMensaje

/**
 * Adapter para la lista de mensajes dentro de una conversación.
 * Soporta mensajes enviados y recibidos, estados de entrega/lectura,
 * respuestas anidadas y edición/eliminación.
 */
class ChatMensajesAdapter(
    private val mensajes: MutableList<ChatMensaje>,
    private val miRol: String   // "suscriptor" en la app móvil
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onLongClick: ((ChatMensaje) -> Unit)? = null

    companion object {
        private const val TIPO_ENVIADO   = 1
        private const val TIPO_RECIBIDO  = 2
    }

    // ─── ViewHolders ──────────────────────────────────────────────────────────

    inner class EnviadoVH(view: View) : RecyclerView.ViewHolder(view) {
        val tvContenido:        TextView = view.findViewById(R.id.tvContenido)
        val tvHora:             TextView = view.findViewById(R.id.tvHora)
        val tvTicks:            TextView = view.findViewById(R.id.tvTicks)
        val tvReplyContenido:   TextView? = view.findViewById(R.id.tvReplyContenido)
        val tvEditado:          TextView? = view.findViewById(R.id.tvEditado)
    }

    inner class RecibidoVH(view: View) : RecyclerView.ViewHolder(view) {
        val tvContenido:        TextView = view.findViewById(R.id.tvContenido)
        val tvHora:             TextView = view.findViewById(R.id.tvHora)
        val tvReplyContenido:   TextView? = view.findViewById(R.id.tvReplyContenido)
        val tvEditado:          TextView? = view.findViewById(R.id.tvEditado)
    }

    // ─── RecyclerView overrides ───────────────────────────────────────────────

    override fun getItemViewType(position: Int): Int {
        return if (mensajes[position].enviado_por == miRol) TIPO_ENVIADO else TIPO_RECIBIDO
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TIPO_ENVIADO) {
            EnviadoVH(inflater.inflate(R.layout.item_mensaje_enviado, parent, false))
        } else {
            RecibidoVH(inflater.inflate(R.layout.item_mensaje_recibido, parent, false))
        }
    }

    override fun getItemCount() = mensajes.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = mensajes[position]

        // Ignorar mensajes eliminados para todos
        if (msg.borrado_para == "todos") {
            if (holder is EnviadoVH) {
                holder.tvContenido.text = "🚫 Mensaje eliminado"
                holder.tvContenido.alpha = 0.5f
                holder.tvReplyContenido?.visibility = View.GONE
            } else if (holder is RecibidoVH) {
                holder.tvContenido.text = "🚫 Mensaje eliminado"
                holder.tvContenido.alpha = 0.5f
                holder.tvReplyContenido?.visibility = View.GONE
            }
            return
        }

        val hora = try {
            val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            val parse = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
            parse.timeZone = java.util.TimeZone.getTimeZone("UTC")
            sdf.format(parse.parse(msg.enviado_en) ?: return)
        } catch (_: Exception) { msg.enviado_en.take(5) }

        when (holder) {
            is EnviadoVH -> {
                holder.tvContenido.text  = msg.contenido
                holder.tvContenido.alpha = 1f
                holder.tvHora.text       = hora
                holder.tvTicks.text      = when {
                    msg.leido     == 1 -> "✓✓"
                    msg.entregado == 1 -> "✓✓"
                    else               -> "✓"
                }
                holder.tvTicks.setTextColor(
                    if (msg.leido == 1)
                        holder.itemView.context.getColor(android.R.color.holo_blue_light)
                    else
                        android.graphics.Color.parseColor("#AAAAAA")
                )
                holder.tvReplyContenido?.apply {
                    if (!msg.respuesta_contenido.isNullOrEmpty()) {
                        visibility = View.VISIBLE
                        text = "↩ ${msg.respuesta_contenido.take(60)}"
                    } else {
                        visibility = View.GONE
                    }
                }
                holder.tvEditado?.visibility =
                    if (msg.editado_en != null) View.VISIBLE else View.GONE

                holder.itemView.setOnLongClickListener {
                    onLongClick?.invoke(msg)
                    true
                }
            }

            is RecibidoVH -> {
                holder.tvContenido.text  = msg.contenido
                holder.tvContenido.alpha = 1f
                holder.tvHora.text       = hora
                holder.tvReplyContenido?.apply {
                    if (!msg.respuesta_contenido.isNullOrEmpty()) {
                        visibility = View.VISIBLE
                        text = "↩ ${msg.respuesta_contenido.take(60)}"
                    } else {
                        visibility = View.GONE
                    }
                }
                holder.tvEditado?.visibility =
                    if (msg.editado_en != null) View.VISIBLE else View.GONE

                holder.itemView.setOnLongClickListener {
                    onLongClick?.invoke(msg)
                    true
                }
            }
        }
    }

    // ─── Métodos públicos ─────────────────────────────────────────────────────

    /** Añade un mensaje al final. */
    fun agregar(msg: ChatMensaje) {
        // Evitar duplicados
        if (mensajes.any { it.id_mensaje == msg.id_mensaje }) return
        mensajes.add(msg)
        notifyItemInserted(mensajes.size - 1)
    }

    /** Añade mensajes más antiguos al inicio. */
    fun prepend(lista: List<ChatMensaje>) {
        val filtrada = lista.filter { nuevo ->
            mensajes.none { it.id_mensaje == nuevo.id_mensaje }
        }
        mensajes.addAll(0, filtrada)
        notifyItemRangeInserted(0, filtrada.size)
    }

    /** Limpia todos los mensajes (útil al cambiar de conversación). */
    fun limpiar() {
        val size = mensajes.size
        mensajes.clear()
        notifyItemRangeRemoved(0, size)
    }

    /** Marca todos los mensajes como entregados (doble palomita gris). */
    fun marcarEntregados() {
        mensajes.forEachIndexed { i, msg ->
            if (msg.entregado == 0) {
                mensajes[i] = msg.copy(entregado = 1)
                notifyItemChanged(i)
            }
        }
    }

    /** Marca todos los mensajes enviados por mí como leídos (palomitas azules). */
    fun marcarLeidos(idPersonal: Int) {
        mensajes.forEachIndexed { i, msg ->
            if (msg.enviado_por == miRol && msg.leido == 0) {
                mensajes[i] = msg.copy(leido = 1, entregado = 1)
                notifyItemChanged(i)
            }
        }
    }

    /** Actualiza el contenido de un mensaje editado. */
    fun actualizarMensaje(idMensaje: Int, nuevoContenido: String, editadoEn: String?) {
        val idx = mensajes.indexOfFirst { it.id_mensaje == idMensaje }
        if (idx == -1) return
        mensajes[idx] = mensajes[idx].copy(
            contenido  = nuevoContenido,
            editado_en = editadoEn
        )
        notifyItemChanged(idx)
    }

    /** Marca un mensaje como eliminado para todos. */
    fun eliminarMensaje(idMensaje: Int) {
        val idx = mensajes.indexOfFirst { it.id_mensaje == idMensaje }
        if (idx == -1) return
        mensajes[idx] = mensajes[idx].copy(borrado_para = "todos")
        notifyItemChanged(idx)
    }
}