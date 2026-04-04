package com.axf.gymnet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.axf.gymnet.data.ChatMensaje

class ChatMensajesAdapter(
    private val mensajes: MutableList<ChatMensaje>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TIPO_ENVIADO   = 1
        private const val TIPO_RECIBIDO  = 2
    }

    fun agregar(m: ChatMensaje) {
        mensajes.add(m)
        notifyItemInserted(mensajes.size - 1)
    }

    override fun getItemViewType(position: Int) =
        if (mensajes[position].enviado_por == "suscriptor") TIPO_ENVIADO else TIPO_RECIBIDO

    override fun getItemCount() = mensajes.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout = if (viewType == TIPO_ENVIADO)
            R.layout.item_mensaje_enviado else R.layout.item_mensaje_recibido
        val v = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return MsgVH(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val m  = mensajes[position]
        val vh = holder as MsgVH
        vh.tvContenido.text = m.contenido
        // Mostrar hora HH:mm
        vh.tvHora.text = if (m.enviado_en.length >= 16)
            m.enviado_en.substring(11, 16) else ""
    }

    inner class MsgVH(v: View) : RecyclerView.ViewHolder(v) {
        val tvContenido: TextView = v.findViewById(R.id.tvContenido)
        val tvHora: TextView      = v.findViewById(R.id.tvHora)
    }
}