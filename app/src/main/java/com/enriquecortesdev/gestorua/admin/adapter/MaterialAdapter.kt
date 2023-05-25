package com.enriquecortesdev.gestorua.admin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enriquecortesdev.gestorua.R
import com.enriquecortesdev.gestorua.items.Material

class MaterialAdapter(private val materialesList: ArrayList<Material>, private val borrarMaterial:(String) -> Unit) : RecyclerView.Adapter<MaterialViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterialViewHolder {

        // Context for the ViewHolder
        val layoutInflater = LayoutInflater.from(parent.context)

        return MaterialViewHolder(layoutInflater.inflate(R.layout.item_material, parent, false))
    }

    override fun onBindViewHolder(holder: MaterialViewHolder, position: Int) {
        val item = materialesList[position]
        holder.render(item, borrarMaterial)
    }

    override fun getItemCount(): Int {
        return materialesList.size
    }
}