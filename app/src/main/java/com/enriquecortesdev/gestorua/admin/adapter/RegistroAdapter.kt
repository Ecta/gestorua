package com.enriquecortesdev.gestorua.admin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enriquecortesdev.gestorua.R
import com.enriquecortesdev.gestorua.items.Registro

class RegistroAdapter(private val registrosList: ArrayList<Registro>) : RecyclerView.Adapter<RegistroViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegistroViewHolder {

        // Context for the ViewHolder
        val layoutInflater = LayoutInflater.from(parent.context)

        return RegistroViewHolder(layoutInflater.inflate(R.layout.item_registro, parent, false))
    }

    override fun onBindViewHolder(holder: RegistroViewHolder, position: Int) {
        val item = registrosList[position]
        holder.render(item)
    }

    override fun getItemCount(): Int {
        return registrosList.size
    }
}