package com.enriquecortesdev.gestorua.admin.profs

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.enriquecortesdev.gestorua.R
import com.google.firebase.firestore.FirebaseFirestore

class AdminGestProfAddActivity : AppCompatActivity() {

    // Cloud Firestore instance declaration
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_gest_prof_add)

        // Behaviour for the "Confirmar" button
        findViewById<Button>(R.id.id_addProfesoresConfButton).setOnClickListener {

            // Checks that every EditText is filled
            if(checkCampos()){

                // If they are all filled, tries to ADD the professor
                confirmAdd(
                    findViewById<EditText>(R.id.id_addProfesoresFormTIU).text.toString(),
                    findViewById<EditText>(R.id.id_addProfesoresFormName).text.toString(),
                    findViewById<EditText>(R.id.id_addProfesoresFormDpt).text.toString()
                )

            }

        }

        // Behaviour for the "Volver" button
        findViewById<Button>(R.id.id_addProfesoresVolver).setOnClickListener {

            onBackPressed()

        }

    }

    private fun confirmAdd(id: String, name: String, dpt: String){

        // Alert builder to show the entered text
        val builder = AlertDialog.Builder(this)

        builder.setMessage("¿Crear con estos datos?")
            .setCancelable(false)
            .setPositiveButton("Aceptar") { dialog, _ ->

                // What to do when clicking "Aceptar"
                dialog.dismiss()

                db.collection("profesores").document(id)
                    .get()
                    .addOnSuccessListener {

                        if(it.exists()){

                            // What to do when prof already exists
                            Toast.makeText(this, "El profesor $id ya existe", Toast.LENGTH_LONG).show()

                        }
                        else{

                            // When prof does not exist, create it
                            db.collection("profesores").document(id)
                                .set(mapOf(
                                    "nombre" to name,
                                    "departamento" to dpt
                                ))
                                .addOnSuccessListener {

                                    // What to do when prof is created
                                    Toast.makeText(this, "Profesor creado", Toast.LENGTH_LONG).show()

                                    val intent = Intent(this, AdminGestProfActivity::class.java)
                                    startActivity(intent)

                                }
                                .addOnFailureListener {

                                    // What to do when something wrong happens
                                    Toast.makeText(this, "No se ha podido crear al profesor", Toast.LENGTH_LONG).show()

                                }

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
        if(findViewById<EditText>(R.id.id_addProfesoresFormTIU).text.isBlank()){

            if(findViewById<EditText>(R.id.id_addProfesoresFormName).text.isBlank() || findViewById<EditText>(R.id.id_addProfesoresFormDpt).text.isBlank()){

                // If some EditText is empty, set a message and return false
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_LONG).show()

            }
            else{

                // If some EditText is empty, set a message and return false
                Toast.makeText(this, "El código TIU es obligatorio", Toast.LENGTH_LONG).show()

            }

            return false

        }
        else{

            if(findViewById<EditText>(R.id.id_addProfesoresFormName).text.isBlank()){

                if(findViewById<EditText>(R.id.id_addProfesoresFormTIU).text.isBlank() || findViewById<EditText>(R.id.id_addProfesoresFormDpt).text.isBlank()){

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

                if(findViewById<EditText>(R.id.id_addProfesoresFormDpt).text.isBlank()){

                    if(findViewById<EditText>(R.id.id_addProfesoresFormTIU).text.isBlank() || findViewById<EditText>(R.id.id_addProfesoresFormName).text.isBlank()){

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

                    // If they are all filled, returns true
                    return true

                }

            }

        }

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {

        // Alert builder to show the entered text
        val builder = AlertDialog.Builder(this)

        builder.setMessage("¿Salir sin crear al profesor?")
            .setCancelable(false)
            .setPositiveButton("Aceptar") { dialog, _ ->

                // What to do when clicking "Aceptar"
                dialog.dismiss()

                val intent = Intent(this, AdminGestProfActivity::class.java)
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