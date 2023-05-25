package com.enriquecortesdev.gestorua.admin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enriquecortesdev.gestorua.R
import com.enriquecortesdev.gestorua.items.Aula

class AulaAdapter(private val aulasList: ArrayList<Aula>,
                  private val editarAula:(String) -> Unit,
                  private val borrarAula:(String) -> Unit) : RecyclerView.Adapter<AulaViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AulaViewHolder {

        // Context for the ViewHolder
        val layoutInflater = LayoutInflater.from(parent.context)

        return AulaViewHolder(layoutInflater.inflate(R.layout.item_aula, parent, false))
    }

    override fun onBindViewHolder(holder: AulaViewHolder, position: Int) {
        val item = aulasList[position]
        holder.render(item, editarAula, borrarAula)
    }

    override fun getItemCount(): Int {
        return aulasList.size
    }
}