package com.enriquecortesdev.gestorua.admin.adapter

import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.enriquecortesdev.gestorua.R
import com.enriquecortesdev.gestorua.items.Profesor

class ProfesorViewHolder (view: View) : ViewHolder(view){

    private val nombreProfesor: TextView = view.findViewById<TextView>(R.id.id_nombreProfesor)
    private val departamentoProfesor: TextView = view.findViewById<TextView>(R.id.id_departamentoProfesor)
    private val editarProfesorButton: Button = view.findViewById<Button>(R.id.id_editarProfesor)
    private val borrarProfesorButton: Button = view.findViewById<Button>(R.id.id_borrarProfesor)

    fun render(
        profesorModel: Profesor,
        editarProfesor: (String) -> Unit,
        borrarProfesor: (String) -> Unit
    ){

        nombreProfesor.text = profesorModel.nombre
        departamentoProfesor.text = profesorModel.departamento

        editarProfesorButton.setOnClickListener { editarProfesor(profesorModel.documento) }
        borrarProfesorButton.setOnClickListener { borrarProfesor(profesorModel.documento) }

    }

}