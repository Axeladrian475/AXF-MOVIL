package com.axf.gymnet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.axf.gymnet.data.PersonalItem
import com.axf.gymnet.network.RetrofitClient
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

/**
 * Adapter para la lista de personal en la pantalla "Reportar Personal".
 * Muestra foto circular (desde URL absoluta), nombre y puesto.
 * Permite seleccionar un elemento con indicador visual.
 */
class PersonalSeleccionAdapter(
    private val lista: List<PersonalItem>,
    private val onSeleccion: (PersonalItem, Int) -> Unit
) : RecyclerView.Adapter<PersonalSeleccionAdapter.VH>() {

    private var seleccionado: Int = 0

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar:    ImageView = view.findViewById(R.id.ivAvatarPersonal)
        val tvInicial:   TextView  = view.findViewById(R.id.tvInicialPersonal)
        val tvNombre:    TextView  = view.findViewById(R.id.tvNombrePersonal)
        val tvPuesto:    TextView  = view.findViewById(R.id.tvPuestoPersonal)
        val ivSeleccion: ImageView = view.findViewById(R.id.ivSeleccionado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_personal_seleccion, parent, false))

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val personal = lista[position]

        holder.tvNombre.text = "${personal.nombres} ${personal.apellido_paterno}"
        holder.tvPuesto.text = personal.puesto.replace('_', ' ')

        // Construir URL absoluta: el backend devuelve rutas relativas (/uploads/...)
        val baseUrl = RetrofitClient.BASE_URL.trimEnd('/')
        val fotoAbsoluta = personal.foto_url
            ?.takeIf { it.isNotBlank() }
            ?.let { if (it.startsWith("http")) it else "$baseUrl$it" }

        if (!fotoAbsoluta.isNullOrBlank()) {
            holder.ivAvatar.visibility  = View.VISIBLE
            holder.tvInicial.visibility = View.GONE
            Glide.with(holder.itemView.context)
                .load(fotoAbsoluta)
                .circleCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.bg_card_orange)
                .error(R.drawable.bg_card_orange)
                .into(holder.ivAvatar)
        } else {
            holder.ivAvatar.visibility  = View.GONE
            holder.tvInicial.visibility = View.VISIBLE
            holder.tvInicial.text = personal.nombres.firstOrNull()?.uppercase() ?: "?"
        }

        // Indicador de selección
        holder.ivSeleccion.visibility =
            if (position == seleccionado) View.VISIBLE else View.INVISIBLE
        holder.itemView.alpha = if (position == seleccionado) 1f else 0.75f

        holder.itemView.setOnClickListener {
            val prev = seleccionado
            seleccionado = position
            notifyItemChanged(prev)
            notifyItemChanged(position)
            onSeleccion(personal, position)
        }
    }

    fun getSeleccionado() = seleccionado
}