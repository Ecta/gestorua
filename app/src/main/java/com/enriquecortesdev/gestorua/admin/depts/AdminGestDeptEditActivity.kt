package com.enriquecortesdev.gestorua.admin.depts

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.enriquecortesdev.gestorua.R
import com.enriquecortesdev.gestorua.items.Departamento
import com.google.firebase.firestore.FirebaseFirestore

class AdminGestDeptEditActivity : AppCompatActivity() {

    // Cloud Firestore instance declaration
    private val db = FirebaseFirestore.getInstance()

    private lateinit var oldDept: Departamento

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_gest_dept_edit)

        // Gets departamento information from DB
        db.collection("departamentos").document(intent.extras!!.getString("id").toString())
            .get()
            .addOnSuccessListener {
                if(it.exists()){

                    // Sets the old data from the Dept
                    oldDept = Departamento(
                        id = it.id,
                        nombre = it.get("nombre") as String,
                        siglas = it.get("siglas") as String
                    )

                    // Setting up the hints for the form
                    findViewById<EditText>(R.id.id_editDeptFormName).setText(it.get("nombre") as String)
                    findViewById<EditText>(R.id.id_editDeptFormSiglas).setText(it.get("siglas") as String)

                }
            }
            .addOnFailureListener {

                // What to do when something wrong happens
                Toast.makeText(this, "No se ha podido cargar el departamento", Toast.LENGTH_LONG).show()

            }

        // Behaviour for the "Confirmar" button
        findViewById<Button>(R.id.id_editDeptConfButton).setOnClickListener {

            if(checkCampos()){

                confirmEdit(
                    intent.extras!!.getString("id").toString(),
                    findViewById<EditText>(R.id.id_editDeptFormName).text.toString(),
                    findViewById<EditText>(R.id.id_editDeptFormSiglas).text.toString()
                )

            }

        }

        // Behaviour for the "Volver" button
        findViewById<Button>(R.id.id_editDeptVolver).setOnClickListener {

            onBackPressed()

        }

    }

    private fun confirmEdit(id: String, name: String, siglas: String){

        // Alert builder to show the entered text
        val builder = AlertDialog.Builder(this)

        builder.setMessage("¿Confirmar los cambios?")
            .setCancelable(false)
            .setPositiveButton("Aceptar") { dialog, _ ->

                // What to do when clicking "Aceptar"
                dialog.dismiss()

                // If there are any changes
                if(checkChanges(oldDept.nombre, name, oldDept.siglas, siglas)){

                    db.collection("departamentos").document(id)
                        .update(mapOf(
                            "nombre" to name,
                            "siglas" to siglas
                        ))
                        .addOnSuccessListener {

                            // What to do when prof is updated
                            extendChanges(oldDept.siglas, siglas)
                            Toast.makeText(this, "Departamento editado", Toast.LENGTH_LONG).show()

                            val intent = Intent(this, AdminGestDeptActivity::class.java)
                            startActivity(intent)

                        }
                        .addOnFailureListener {

                            // What to do when something wrong happens
                            Toast.makeText(this, "No se ha podido editar el departamento", Toast.LENGTH_LONG).show()

                        }
                }
                else{

                    Toast.makeText(this, "No se han detectado cambios", Toast.LENGTH_LONG).show()

                }
            }
            .setNegativeButton("Cancelar") { dialog, _ ->

                // What to do when clicking "Cancelar"
                dialog.dismiss()

            }

        val alert = builder.create()
        alert.show()

    }

    private fun extendChanges(oldSiglas: String, newSiglas: String) {

        db.collection("profesores").whereEqualTo("departamento", oldSiglas)
            .get()
            .addOnSuccessListener {

                db.runBatch { batch ->

                    for (profesor in it){

                        batch.update(
                            db.collection("profesores").document(profesor.id),
                            "departamento",
                            newSiglas)

                    }

                }
                    .addOnFailureListener {

                        Toast.makeText(this, "HA PASAO ALGO EN EL BATCH", Toast.LENGTH_LONG).show()

                    }

            }
            .addOnFailureListener {

                Toast.makeText(this, "HA PASAO ALGO ANTES", Toast.LENGTH_LONG).show()

            }

    }

    private fun checkChanges(oldName: String, newName: String, oldSiglas: String, newSiglas: String): Boolean {

        if(oldName != newName || oldSiglas != newSiglas){

            return true

        }

        return false

    }

    private fun checkCampos(): Boolean {

        // Checks that every EditText is filled
        if(findViewById<EditText>(R.id.id_editDeptFormName).text.isBlank()){

            if(findViewById<EditText>(R.id.id_editDeptFormSiglas).text.isBlank()){

                // If some EditText is empty, set a message and return false
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_LONG).show()

            }
            else{

                // If some EditText is empty, set a message and return false
                Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_LONG).show()

            }

            return false

        }
        else{

            if(findViewById<EditText>(R.id.id_editDeptFormSiglas).text.isBlank()){

                // If some EditText is empty, set a message and return false
                Toast.makeText(this, "Las siglas son obligatorias", Toast.LENGTH_LONG).show()

                return false

            }
            else{

                // If they are all filled, returns true
                return true

            }

        }

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {

        // Alert builder to show the entered text
        val builder = AlertDialog.Builder(this)

        builder.setMessage("¿Salir sin editar el departamento?")
            .setCancelable(false)
            .setPositiveButton("Aceptar") { dialog, _ ->

                // What to do when clicking "Aceptar"
                dialog.dismiss()

                val intent = Intent(this, AdminGestDeptActivity::class.java)
                startActivity(intent)

            }
            .setNegativeButton("Cancelar") { dialog, _ ->

                // What to do when clicking "Cancelar"
                dialog.dismiss()

            }

        val alert = builder.create()
        alert.show()

    }

}