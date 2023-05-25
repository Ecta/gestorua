package com.enriquecortesdev.gestorua.admin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enriquecortesdev.gestorua.R
import com.enriquecortesdev.gestorua.items.Profesor

class ProfesorAdapter(private val profesoresList: ArrayList<Profesor>, private val editarProfesor:(String) -> Unit, private val borrarProfesor:(String) -> Unit) : RecyclerView.Adapter<ProfesorViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfesorViewHolder {

        // Context for the ViewHolder
        val layoutInflater = LayoutInflater.from(parent.context)

        return ProfesorViewHolder(layoutInflater.inflate(R.layout.item_profesor, parent, false))
    }

    override fun onBindViewHolder(holder: ProfesorViewHolder, position: Int) {
        val item = profesoresList[position]
        holder.render(item, editarProfesor, borrarProfesor)
    }

    override fun getItemCount(): Int {
        return profesoresList.size
    }
}