package com.enriquecortesdev.gestorua.admin.adapter

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.enriquecortesdev.gestorua.R
import com.enriquecortesdev.gestorua.items.Departamento

class DepartamentoViewHolder (view: View) : RecyclerView.ViewHolder(view){

    private val nombreDepartamento: TextView = view.findViewById<TextView>(R.id.id_nombreDepartamento)
    private val siglasDepartamento: TextView = view.findViewById<TextView>(R.id.id_siglasDepartamento)
    private val editarDepartamentoButton: Button = view.findViewById<Button>(R.id.id_editarDepartamento)
    private val borrarDepartamentoButton: Button = view.findViewById<Button>(R.id.id_borrarDepartamento)

    fun render(
        departamentoModel: Departamento,
        editarDepartamento: (String) -> Unit,
        borrarDepartamento: (String) -> Unit
    ){

        nombreDepartamento.text = departamentoModel.nombre
        siglasDepartamento.text = departamentoModel.siglas

        editarDepartamentoButton.setOnClickListener { editarDepartamento(departamentoModel.id) }
        borrarDepartamentoButton.setOnClickListener { borrarDepartamento(departamentoModel.id) }

    }

}