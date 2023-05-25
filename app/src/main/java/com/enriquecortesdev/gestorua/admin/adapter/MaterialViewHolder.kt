package com.enriquecortesdev.gestorua.admin.adapter

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.enriquecortesdev.gestorua.R
import com.enriquecortesdev.gestorua.items.Material

class MaterialViewHolder (view: View) : ViewHolder(view){

    private val tipoMaterial: TextView = view.findViewById<TextView>(R.id.id_tipoMaterial)
    private val aulaMaterial: TextView = view.findViewById<TextView>(R.id.id_aulaMaterial)
    private val borrarMaterialButton: Button = view.findViewById<Button>(R.id.id_borrarMaterial)

    fun render(
        materialModel: Material,
        borrarMaterial: (String) -> Unit
    ){

        tipoMaterial.text = materialModel.tipo
        aulaMaterial.text = materialModel.aula

        borrarMaterialButton.setOnClickListener { borrarMaterial(materialModel.id) }

    }

}