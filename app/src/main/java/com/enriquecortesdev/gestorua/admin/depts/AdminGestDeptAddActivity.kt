package com.enriquecortesdev.gestorua.admin.depts

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.enriquecortesdev.gestorua.R
import com.google.firebase.firestore.FirebaseFirestore

class AdminGestDeptAddActivity : AppCompatActivity() {

    // Cloud Firestore instance declaration
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_gest_dept_add)

        // Behaviour for the "Confirmar" button
        findViewById<Button>(R.id.id_addDeptConfButton).setOnClickListener {

            // Checks that every EditText is filled
            if(checkCampos()){

                // If they are all filled, tries to ADD the professor
                confirmAdd(
                    findViewById<EditText>(R.id.id_addDeptFormName).text.toString(),
                    findViewById<EditText>(R.id.id_addDeptFormSiglas).text.toString()
                )

            }

        }

        // Behaviour for the "Volver" button
        findViewById<Button>(R.id.id_addDeptVolver).setOnClickListener {

            onBackPressed()

        }

    }

    private fun confirmAdd(name: String, siglas: String){

        // Alert builder to show the entered text
        val builder = AlertDialog.Builder(this)

        builder.setMessage("¿Crear con estos datos?")
            .setCancelable(false)
            .setPositiveButton("Aceptar") { dialog, _ ->

                // What to do when clicking "Aceptar"
                dialog.dismiss()

                db.collection("departamentos").whereEqualTo("siglas", siglas)
                    .get()
                    .addOnSuccessListener {

                        if(it.isEmpty){

                            // When dept does not exist, create it
                            db.collection("departamentos")
                                .add(mapOf(
                                    "nombre" to name,
                                    "siglas" to siglas
                                ))
                                .addOnSuccessListener {

                                    // What to do when prof is created
                                    Toast.makeText(this, "Profesor creado", Toast.LENGTH_LONG).show()

                                    val intent = Intent(this, AdminGestDeptActivity::class.java)
                                    startActivity(intent)

                                }
                                .addOnFailureListener {

                                    // What to do when something wrong happens
                                    Toast.makeText(this, "No se ha podido crear el departamento", Toast.LENGTH_LONG).show()

                                }

                        }
                        else{

                            // What to do when prof already exists
                            Toast.makeText(this, "El departamento $siglas ya existe", Toast.LENGTH_LONG).show()

                        }

                    }
                    .addOnFailureListener {

                        // What to do when something wrong happens
                        Toast.makeText(this, "Ha habido un problema con la conexión", Toast.LENGTH_LONG).show()


                    }

            }
            .setNegativeButton("Cancelar") { dialog, _ ->

                // What to do when clicking "Cancelar"
                dialog.dismiss()

            }

        val alert = builder.create()
        alert.show()

    }

    private fun checkCampos(): Boolean {

        // Checks that every EditText is filled
        if(findViewById<EditText>(R.id.id_addDeptFormName).text.isBlank()){

            if(findViewById<EditText>(R.id.id_addDeptFormSiglas).text.isBlank()){

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

            if(findViewById<EditText>(R.id.id_addDeptFormSiglas).text.isBlank()){

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

        builder.setMessage("¿Salir sin crear el departamento?")
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