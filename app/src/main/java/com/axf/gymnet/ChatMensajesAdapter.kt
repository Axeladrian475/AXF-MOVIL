package com.axf.gymnet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.axf.gymnet.data.ChatMensaje
import com.axf.gymnet.network.RetrofitClient
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

/**
 * Adapter para la lista de mensajes dentro de una conversación.
 * Soporta mensajes enviados y recibidos, estados de entrega/lectura,
 * respuestas anidadas, edición/eliminación y avatares de perfil.
 */
class ChatMensajesAdapter(
    private val mensajes: MutableList<ChatMensaje>,
    private val miRol: String,                         // "suscriptor" en la app móvil
    private val fotoPersonalUrl: String?   = null,     // foto del personal (burbujas recibidas)
    private val fotoSuscriptorUrl: String? = null,     // foto del suscriptor (burbujas enviadas)
    private val nombrePersonal: String     = "",       // inicial fallback personal
    private val nombreSuscriptor: String   = ""        // inicial fallback suscriptor
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onLongClick: ((ChatMensaje) -> Unit)? = null

    companion object {
        private const val TIPO_ENVIADO  = 1
        private const val TIPO_RECIBIDO = 2

        /** Convierte una ruta relativa del backend en URL absoluta para Glide. */
        fun urlAbsoluta(ruta: String?): String? {
            if (ruta.isNullOrBlank()) return null
            return if (ruta.startsWith("http")) ruta
            else "${RetrofitClient.BASE_URL.trimEnd('/')}$ruta"
        }
    }

    // ─── ViewHolders ──────────────────────────────────────────────────────────

    inner class EnviadoVH(view: View) : RecyclerView.ViewHolder(view) {
        val tvContenido:      TextView  = view.findViewById(R.id.tvContenido)
        val tvHora:           TextView  = view.findViewById(R.id.tvHora)
        val tvTicks:          TextView  = view.findViewById(R.id.tvTicks)
        val tvReplyContenido: TextView? = view.findViewById(R.id.tvReplyContenido)
        val tvEditado:        TextView? = view.findViewById(R.id.tvEditado)
        val ivAvatar:         ImageView = view.findViewById(R.id.ivAvatarSuscMsg)
        val tvAvatar:         TextView  = view.findViewById(R.id.tvAvatarSuscMsg)
    }

    inner class RecibidoVH(view: View) : RecyclerView.ViewHolder(view) {
        val tvContenido:      TextView  = view.findViewById(R.id.tvContenido)
        val tvHora:           TextView  = view.findViewById(R.id.tvHora)
        val tvReplyContenido: TextView? = view.findViewById(R.id.tvReplyContenido)
        val tvEditado:        TextView? = view.findViewById(R.id.tvEditado)
        val ivAvatar:         ImageView = view.findViewById(R.id.ivAvatarMsg)
        val tvAvatar:         TextView  = view.findViewById(R.id.tvAvatarMsg)
    }

    // ─── RecyclerView overrides ───────────────────────────────────────────────

    override fun getItemViewType(position: Int): Int =
        if (mensajes[position].enviado_por == miRol) TIPO_ENVIADO else TIPO_RECIBIDO

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TIPO_ENVIADO)
            EnviadoVH(inflater.inflate(R.layout.item_mensaje_enviado,   parent, false))
        else
            RecibidoVH(inflater.inflate(R.layout.item_mensaje_recibido, parent, false))
    }

    override fun getItemCount() = mensajes.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = mensajes[position]

        // Mensajes eliminados para todos
        if (msg.borrado_para == "todos") {
            val tv = when (holder) {
                is EnviadoVH  -> holder.tvContenido
                is RecibidoVH -> holder.tvContenido
                else          -> return
            }
            tv.text  = "🚫 Mensaje eliminado"
            tv.alpha = 0.5f
            when (holder) {
                is EnviadoVH  -> holder.tvReplyContenido?.visibility = View.GONE
                is RecibidoVH -> holder.tvReplyContenido?.visibility = View.GONE
            }
            return
        }

        val hora = try {
            val sdf   = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            val parse = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
            parse.timeZone = java.util.TimeZone.getTimeZone("UTC")
            sdf.format(parse.parse(msg.enviado_en) ?: return)
        } catch (_: Exception) { msg.enviado_en.take(5) }

        when (holder) {
            is EnviadoVH -> {
                holder.tvContenido.text  = msg.contenido
                holder.tvContenido.alpha = 1f
                holder.tvHora.text       = hora
                holder.tvTicks.text = when {
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
                    } else visibility = View.GONE
                }
                holder.tvEditado?.visibility =
                    if (msg.editado_en != null) View.VISIBLE else View.GONE

                // Avatar del suscriptor — URL absoluta
                cargarAvatar(holder.ivAvatar, holder.tvAvatar,
                    urlAbsoluta(fotoSuscriptorUrl), nombreSuscriptor)

                holder.itemView.setOnLongClickListener {
                    onLongClick?.invoke(msg); true
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
                    } else visibility = View.GONE
                }
                holder.tvEditado?.visibility =
                    if (msg.editado_en != null) View.VISIBLE else View.GONE

                // Avatar del personal — URL absoluta
                cargarAvatar(holder.ivAvatar, holder.tvAvatar,
                    urlAbsoluta(fotoPersonalUrl), nombrePersonal)

                holder.itemView.setOnLongClickListener {
                    onLongClick?.invoke(msg); true
                }
            }
        }
    }

    // ─── Métodos públicos ─────────────────────────────────────────────────────

    fun agregar(msg: ChatMensaje) {
        if (mensajes.any { it.id_mensaje == msg.id_mensaje }) return
        mensajes.add(msg)
        notifyItemInserted(mensajes.size - 1)
    }

    fun prepend(lista: List<ChatMensaje>) {
        val filtrada = lista.filter { nuevo ->
            mensajes.none { it.id_mensaje == nuevo.id_mensaje }
        }
        mensajes.addAll(0, filtrada)
        notifyItemRangeInserted(0, filtrada.size)
    }

    fun limpiar() {
        val size = mensajes.size
        mensajes.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun marcarEntregados() {
        mensajes.forEachIndexed { i, msg ->
            if (msg.entregado == 0) {
                mensajes[i] = msg.copy(entregado = 1)
                notifyItemChanged(i)
            }
        }
    }

    fun marcarEntregadoIndividual(idMensaje: Int) {
        val idx = mensajes.indexOfFirst { it.id_mensaje == idMensaje }
        if (idx == -1) return
        if (mensajes[idx].entregado == 0) {
            mensajes[idx] = mensajes[idx].copy(entregado = 1)
            notifyItemChanged(idx)
        }
    }

    fun marcarLeidos(idPersonal: Int) {
        mensajes.forEachIndexed { i, msg ->
            if (msg.enviado_por == miRol && msg.leido == 0) {
                mensajes[i] = msg.copy(leido = 1, entregado = 1)
                notifyItemChanged(i)
            }
        }
    }

    fun actualizarMensaje(idMensaje: Int, nuevoContenido: String, editadoEn: String?) {
        val idx = mensajes.indexOfFirst { it.id_mensaje == idMensaje }
        if (idx == -1) return
        mensajes[idx] = mensajes[idx].copy(contenido = nuevoContenido, editado_en = editadoEn)
        notifyItemChanged(idx)
    }

    fun eliminarMensaje(idMensaje: Int) {
        val idx = mensajes.indexOfFirst { it.id_mensaje == idMensaje }
        if (idx == -1) return
        mensajes[idx] = mensajes[idx].copy(borrado_para = "todos")
        notifyItemChanged(idx)
    }

    // ─── Helpers privados ─────────────────────────────────────────────────────

    private fun cargarAvatar(
        ivFoto:    ImageView,
        tvInicial: TextView,
        fotoUrl:   String?,
        nombre:    String
    ) {
        if (!fotoUrl.isNullOrBlank()) {
            ivFoto.visibility    = View.VISIBLE
            tvInicial.visibility = View.GONE
            Glide.with(ivFoto.context)
                .load(fotoUrl)
                .circleCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.bg_card_orange)
                .error(R.drawable.bg_card_orange)
                .into(ivFoto)
        } else {
            ivFoto.visibility    = View.GONE
            tvInicial.visibility = View.VISIBLE
            tvInicial.text       = nombre.firstOrNull()?.uppercase() ?: "?"
        }
    }
}