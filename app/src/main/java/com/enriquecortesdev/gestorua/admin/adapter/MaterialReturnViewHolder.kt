package com.enriquecortesdev.gestorua.admin.adapter

import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.enriquecortesdev.gestorua.R
import com.enriquecortesdev.gestorua.items.Material

class MaterialReturnViewHolder (view: View) : ViewHolder(view){

    private val tipoMaterial: TextView = view.findViewById<TextView>(R.id.id_tipoMaterialReturn)
    private val aulaMaterial: TextView = view.findViewById<TextView>(R.id.id_aulaMaterialReturn)
    private val returnMaterialCheck: CheckBox = view.findViewById<CheckBox>(R.id.id_returnMaterialCheck)

    fun render(
        materialModel: Material
    ){

        tipoMaterial.text = materialModel.tipo
        aulaMaterial.text = materialModel.aula
        returnMaterialCheck.isChecked = !materialModel.asignado
    }

}