package com.enriquecortesdev.gestorua.admin.aulas

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enriquecortesdev.gestorua.R
import com.enriquecortesdev.gestorua.admin.AdminMenuActivity
import com.enriquecortesdev.gestorua.admin.adapter.AulaAdapter
import com.enriquecortesdev.gestorua.items.Aula
import com.google.firebase.firestore.FirebaseFirestore

class AdminGestAulasActivity : AppCompatActivity() {

    // Cloud Firestore instance declaration
    private val db = FirebaseFirestore.getInstance()

    private var aulasList = ArrayList<Aula>()

    // Empty adapter declaration
    private val aulaAdapter = AulaAdapter(aulasList, {editarAula(it)}, {borrarAula(it)})

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_gest_aulas)

        // Sets up the recyclerView and attaches the adapter (with an empty list)
        val manager = LinearLayoutManager(this)
        val decoration = DividerItemDecoration(this, manager.orientation)
        val recyclerView = findViewById<RecyclerView>(R.id.id_aulasRecycler)
        recyclerView.layoutManager = manager
        recyclerView.adapter = aulaAdapter
        recyclerView.addItemDecoration(decoration)

        // Loads a full list of aulas and refreshes the adapter
        loadAulas()

        // Behaviour for the "Añadir" button
        findViewById<Button>(R.id.id_aulasAddButton).setOnClickListener {

            val intent = Intent(this, AdminGestAulasAddActivity::class.java)
            startActivity(intent)

        }

        // Behaviour for the "Volver" button
        findViewById<Button>(R.id.id_aulasVolverButton).setOnClickListener {

            // Returns to the AdminMenuActivity
            onBackPressed()

        }
    }

    // Function to initialize the teachers list
    @SuppressLint("NotifyDataSetChanged")
    private fun loadAulas() {

        db.collection("aulas").orderBy("edificio").get()
            .addOnCompleteListener {

                if(it.result != null) {

                    //val profesoresList = ArrayList<Profesor>()
                    aulasList.clear()

                    // For each professor in database
                    for (document in it.result) {

                        // Create a new Aula to add to the aulasList ArrayList
                        val nuevoAula = Aula(
                            id = document.id,
                            tipo = document.data["tipo"] as String,
                            edificio = document.data["edificio"] as String,
                            planta = document.data["planta"] as String,
                            puerta = document.data["puerta"] as String
                        )

                        // Add the created Aula to the aulasList ArrayList
                        aulasList.add(nuevoAula)

                    }

                    // Sends aulasList to the adapter to fill the recyclerView
                    aulaAdapter.notifyDataSetChanged()

                    // Disables the progressBar
                    findViewById<ProgressBar>(R.id.id_aulasProgress).visibility = View.GONE

                    // In case database is empty
                    if(aulasList.isEmpty()){

                        Toast.makeText(this, "No hay aulas registradas", Toast.LENGTH_LONG).show()

                    }

                }
                else{

                    // In case the query returned null
                    Toast.makeText(this, "La base de datos no ha respondido", Toast.LENGTH_LONG).show()

                }

            }
            .addOnFailureListener {

                // In case something goes wrong
                Toast.makeText(this, "Algo ha ido mal:" + it.message, Toast.LENGTH_LONG).show()

            }

    }

    private fun borrarAula(id: String){

        // Alert builder to ask deleting the item
        val builder = AlertDialog.Builder(this)

        builder.setMessage("¿Seguro que quieres eliminar este aula?")
            .setCancelable(false)
            .setPositiveButton("Aceptar") { dialog, _ ->

                // What to do when clicking "Aceptar"
                dialog.dismiss()

                db.collection("llaves").whereEqualTo("aula", id).limit(1)
                    .get()
                    .addOnSuccessListener { llaves ->
                        if(!llaves.isEmpty){
                            // If there is a related key and is not assigned, delete it
                            if(llaves.documents[0].get("asignada") as Boolean){
                                Toast.makeText(this, "No se puede borrar un aula asignada", Toast.LENGTH_LONG).show()
                            }
                            else{
                                db.collection("llaves").document(llaves.documents[0].id)
                                    .delete()
                                    .addOnSuccessListener {
                                        // If success deleting the key, delete the aula
                                        db.collection("aulas").document(id)
                                            .delete()
                                            .addOnSuccessListener {
                                                Toast.makeText(this, "Aula borrada: $id", Toast.LENGTH_LONG).show()
                                                loadAulas()
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(this, "Ha habido un problema borrando el aula", Toast.LENGTH_LONG).show()
                                            }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "Ha habido un problema borrando la llave", Toast.LENGTH_LONG).show()
                                    }
                            }
                        }
                        else{
                            Toast.makeText(this, "No se ha encontrado la llave", Toast.LENGTH_LONG).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Ha habido un problema buscando la llave", Toast.LENGTH_LONG).show()
                    }

            }
            .setNegativeButton("Cancelar") { dialog, _ ->

                // What to do when clicking "Cancelar"
                dialog.dismiss()

            }

        val alert = builder.create()
        alert.show()

    }

    private fun editarAula(id: String){

        val intent = Intent(this, AdminGestAulasEditActivity::class.java)
        intent.putExtra("id", id)
        startActivity(intent)

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val intent = Intent(this, AdminMenuActivity::class.java)
        startActivity(intent)
    }

}