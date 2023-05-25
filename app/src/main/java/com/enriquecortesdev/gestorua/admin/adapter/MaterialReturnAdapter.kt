package com.enriquecortesdev.gestorua.admin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enriquecortesdev.gestorua.R
import com.enriquecortesdev.gestorua.items.Material

class MaterialReturnAdapter(private val materialesList: ArrayList<Material>) : RecyclerView.Adapter<MaterialReturnViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterialReturnViewHolder {

        // Context for the ViewHolder
        val layoutInflater = LayoutInflater.from(parent.context)

        return MaterialReturnViewHolder(layoutInflater.inflate(R.layout.item_material_return, parent, false))
    }

    override fun onBindViewHolder(holder: MaterialReturnViewHolder, position: Int) {
        val item = materialesList[position]
        holder.render(item)
    }

    override fun getItemCount(): Int {
        return materialesList.size
    }
}