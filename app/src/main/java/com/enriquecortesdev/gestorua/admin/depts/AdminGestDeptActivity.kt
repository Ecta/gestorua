package com.enriquecortesdev.gestorua.admin.depts

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
import com.enriquecortesdev.gestorua.admin.adapter.DepartamentoAdapter
import com.enriquecortesdev.gestorua.items.Departamento
import com.google.firebase.firestore.FirebaseFirestore

class AdminGestDeptActivity : AppCompatActivity() {

    // Cloud Firestore instance declaration
    private val db = FirebaseFirestore.getInstance()

    private var departamentosList = ArrayList<Departamento>()

    // Empty adapter declaration
    private val departamentoAdapter = DepartamentoAdapter(departamentosList, {editarDepartamento(it)}, {borrarDepartamento(it)})

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_gest_dept)

        // Sets up the recyclerView and attaches the adapter (with an empty list)
        val manager = LinearLayoutManager(this)
        val decoration = DividerItemDecoration(this, manager.orientation)
        val recyclerView = findViewById<RecyclerView>(R.id.id_dptRecycler)
        recyclerView.layoutManager = manager
        recyclerView.adapter = departamentoAdapter
        recyclerView.addItemDecoration(decoration)

        // Loads a full list of professors and refreshes the adapter
        loadDepartamentos()

        // Behaviour for the "Añadir" button
        findViewById<Button>(R.id.id_dptAddButton).setOnClickListener {

            val intent = Intent(this, AdminGestDeptAddActivity::class.java)
            startActivity(intent)

        }

        // Behaviour for the "Volver" button
        findViewById<Button>(R.id.id_dptVolverButton).setOnClickListener {

            // Returns to the AdminMenuActivity
            onBackPressed()

        }

    }

    // Function to initialize the teachers list
    @SuppressLint("NotifyDataSetChanged")
    private fun loadDepartamentos() {

        db.collection("departamentos").orderBy("nombre").get()
            .addOnCompleteListener {

                if(it.result != null) {

                    departamentosList.clear()

                    // For each professor in database
                    for (document in it.result) {

                        // Create a new Departamento to add to the departamentosList ArrayList
                        val nuevoDepartamento = Departamento(
                            id = document.id,
                            nombre = document.data["nombre"] as String,
                            siglas = document.data["siglas"] as String
                        )

                        // Add the created Departamento to the departamentosList ArrayList
                        departamentosList.add(nuevoDepartamento)

                    }

                    // Sends departamentosList to the adapter to fill the recyclerView
                    departamentoAdapter.notifyDataSetChanged()

                    // Disables the progressBar
                    findViewById<ProgressBar>(R.id.id_dptProgress).visibility = View.GONE

                    // In case database is empty
                    if(departamentosList.isEmpty()){

                        Toast.makeText(this, "No hay departamentos registrados", Toast.LENGTH_LONG).show()

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

    private fun borrarDepartamento(id: String){

        // Alert builder to ask deleting the item
        val builder = AlertDialog.Builder(this)

        builder.setMessage("¿Seguro que quieres eliminar este departamento?")
            .setCancelable(false)
            .setPositiveButton("Aceptar") { dialog, _ ->

                // What to do when clicking "Aceptar"
                dialog.dismiss()

                db.collection("departamentos").document(id)
                    .delete()
                    .addOnSuccessListener {

                        Toast.makeText(this, "Departamento borrado", Toast.LENGTH_LONG).show()
                        loadDepartamentos()

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

    private fun editarDepartamento(id: String){

        val intent = Intent(this, AdminGestDeptEditActivity::class.java)
        intent.putExtra("id", id)
        startActivity(intent)

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val intent = Intent(this, AdminMenuActivity::class.java)
        startActivity(intent)
    }

}