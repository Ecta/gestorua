package com.enriquecortesdev.gestorua.admin.adapter

import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.enriquecortesdev.gestorua.R
import com.enriquecortesdev.gestorua.items.Registro

class RegistroViewHolder (view: View) : RecyclerView.ViewHolder(view) {

    private val nombreProfesor: TextView = view.findViewById<TextView>(R.id.id_regsItemProfesor)
    private val nombreAula: TextView = view.findViewById<TextView>(R.id.id_regsItemAula)
    private val fecha: TextView = view.findViewById<TextView>(R.id.id_regsItemFecha)
    private val horaInicio: TextView = view.findViewById<TextView>(R.id.id_regsItemHoraIni)
    private val horaFin: TextView = view.findViewById<TextView>(R.id.id_regsItemHoraFin)
    private val activoCheck: CheckBox = view.findViewById<CheckBox>(R.id.id_regsItemCheck)

    fun render(registroModel: Registro){

        nombreProfesor.text = registroModel.profesor
        nombreAula.text = registroModel.aula
        fecha.text = registroModel.fecha
        horaInicio.text = registroModel.horaini
        horaFin.text = registroModel.horafin
        activoCheck.isChecked = registroModel.activo

    }

}