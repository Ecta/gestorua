package com.enriquecortesdev.gestorua.admin.adapter

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.enriquecortesdev.gestorua.R
import com.enriquecortesdev.gestorua.items.Aula

class AulaViewHolder (view: View) : RecyclerView.ViewHolder(view) {

    private val codigoAula: TextView = view.findViewById<TextView>(R.id.id_codigoAula)
    private val tipoAula: TextView = view.findViewById<TextView>(R.id.id_tipoAula)
    private val edificioAula: TextView = view.findViewById<TextView>(R.id.id_edificioAula)
    private val plantaAula: TextView = view.findViewById<TextView>(R.id.id_plantaAula)
    private val puertaAula: TextView = view.findViewById<TextView>(R.id.id_puertaAula)
    private val editarAulaButton: Button = view.findViewById<Button>(R.id.id_editarAula)
    private val borrarAulaButton: Button = view.findViewById<Button>(R.id.id_borrarAula)

    fun render(
        aulaModel: Aula,
        editarAula: (String) -> Unit,
        borrarAula: (String) -> Unit
    ){

        codigoAula.text = aulaModel.id
        tipoAula.text = aulaModel.tipo
        edificioAula.text = aulaModel.edificio
        plantaAula.text = aulaModel.planta
        puertaAula.text = aulaModel.puerta

        editarAulaButton.setOnClickListener { editarAula(aulaModel.id) }
        borrarAulaButton.setOnClickListener { borrarAula(aulaModel.id) }

    }

}