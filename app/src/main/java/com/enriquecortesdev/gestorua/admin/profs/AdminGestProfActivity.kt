package com.enriquecortesdev.gestorua.admin.profs

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
import com.enriquecortesdev.gestorua.admin.adapter.ProfesorAdapter
import com.enriquecortesdev.gestorua.items.Profesor
import com.google.firebase.firestore.FirebaseFirestore

class AdminGestProfActivity : AppCompatActivity() {

    // Cloud Firestore instance declaration
    private val db = FirebaseFirestore.getInstance()

    private var profesoresList = ArrayList<Profesor>()

    // Empty adapter declaration
    private val profesorAdapter = ProfesorAdapter(profesoresList, {editarProfesor(it)}, {borrarProfesor(it)})

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_gest_prof)

        // Sets up the recyclerView and attaches the adapter (with an empty list)
        val manager = LinearLayoutManager(this)
        val decoration = DividerItemDecoration(this, manager.orientation)
        val recyclerView = findViewById<RecyclerView>(R.id.id_profesoresRecycler)
        recyclerView.layoutManager = manager
        recyclerView.adapter = profesorAdapter
        recyclerView.addItemDecoration(decoration)

        // Loads a full list of professors and refreshes the adapter
        loadProfesores()

        // Behaviour for the "Añadir" button
        findViewById<Button>(R.id.id_profAddButton).setOnClickListener {

            val intent = Intent(this, AdminGestProfAddActivity::class.java)
            startActivity(intent)

        }

        // Behaviour for the "Volver" button
        findViewById<Button>(R.id.id_profVolverButton).setOnClickListener {

            // Returns to the AdminMenuActivity
            onBackPressed()

        }
    }

    // Function to initialize the teachers list
    @SuppressLint("NotifyDataSetChanged")
    private fun loadProfesores() {

        db.collection("profesores").orderBy("nombre").get()
            .addOnCompleteListener {

                if(it.result != null) {

                    //val profesoresList = ArrayList<Profesor>()
                    profesoresList.clear()

                    // For each professor in database
                    for (document in it.result) {

                        // Create a new Profesor to add to the profesoresList ArrayList
                        val nuevoProfesor = Profesor(
                            documento = document.id,
                            nombre = document.data["nombre"] as String,
                            departamento = document.data["departamento"] as String

                        )

                        // Add the created Profesor to the profesoresList ArrayList
                        profesoresList.add(nuevoProfesor)

                    }

                    // Sends profesoresList to the adapter to fill the recyclerView
                    profesorAdapter.notifyDataSetChanged()

                    // Disables the progressBar
                    findViewById<ProgressBar>(R.id.id_profesoresProgress).visibility = View.GONE

                    // In case database is empty
                    if(profesoresList.isEmpty()){

                        Toast.makeText(this, "No hay profesores registrados", Toast.LENGTH_LONG).show()

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

    private fun borrarProfesor(id: String){

        // Alert builder to ask deleting the item
        val builder = AlertDialog.Builder(this)

        builder.setMessage("¿Seguro que quieres eliminar este profesor?")
            .setCancelable(false)
            .setPositiveButton("Aceptar") { dialog, _ ->

                // What to do when clicking "Aceptar"
                dialog.dismiss()

                db.collection("profesores").document(id)
                    .delete()
                    .addOnSuccessListener {

                        Toast.makeText(this, "Profesor borrado: $id", Toast.LENGTH_LONG).show()
                        loadProfesores()

                    }
                    .addOnFailureListener {

                        // In case something goes wrong
                        Toast.makeText(this, "Algo ha ido mal:" + it.message, Toast.LENGTH_LONG).show()

                    }

            }
            .setNegativeButton("Cancelar") { dialog, _ ->

                // What to do when clicking "Cancelar"
                dialog.dismiss()

            }

        val alert = builder.create()
        alert.show()

    }

    private fun editarProfesor(id: String){

        val intent = Intent(this, AdminGestProfEditActivity::class.java)
        intent.putExtra("id", id)
        startActivity(intent)

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val intent = Intent(this, AdminMenuActivity::class.java)
        startActivity(intent)
    }

}