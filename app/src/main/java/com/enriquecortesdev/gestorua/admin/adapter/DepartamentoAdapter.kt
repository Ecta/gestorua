package com.enriquecortesdev.gestorua.admin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enriquecortesdev.gestorua.R
import com.enriquecortesdev.gestorua.items.Departamento

class DepartamentoAdapter(private val departamentosList: ArrayList<Departamento>, private val editarDepartamento:(String) -> Unit, private val borrarDepartamento:(String) -> Unit) : RecyclerView.Adapter<DepartamentoViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepartamentoViewHolder {

        // Context for the ViewHolder
        val layoutInflater = LayoutInflater.from(parent.context)

        return DepartamentoViewHolder(layoutInflater.inflate(R.layout.item_departamento, parent, false))

    }

    override fun onBindViewHolder(holder: DepartamentoViewHolder, position: Int) {
        val item = departamentosList[position]
        holder.render(item, editarDepartamento, borrarDepartamento)
    }

    override fun getItemCount(): Int {
        return departamentosList.size
    }

}