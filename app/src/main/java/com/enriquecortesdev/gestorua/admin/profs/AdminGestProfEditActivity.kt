package com.enriquecortesdev.gestorua.admin.profs

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.enriquecortesdev.gestorua.R
import com.enriquecortesdev.gestorua.items.Profesor
import com.google.firebase.firestore.FirebaseFirestore

class AdminGestProfEditActivity : AppCompatActivity() {

    // Cloud Firestore instance declaration
    private val db = FirebaseFirestore.getInstance()

    private lateinit var oldProfesor: Profesor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_gest_prof_edit)

        // Gets professor information from DB
        db.collection("profesores").document(intent.extras!!.getString("id").toString())
            .get()
            .addOnSuccessListener {
                if(it.exists()){

                    // Setting up the hints for the form
                    findViewById<EditText>(R.id.id_editProfesoresFormName).setText(it.get("nombre") as String)
                    findViewById<EditText>(R.id.id_editProfesoresFormDpt).setText(it.get("departamento") as String)
                    oldProfesor = Profesor(
                        documento = it.id,
                        nombre = it.get("nombre") as String,
                        departamento = it.get("departamento") as String
                    )

                }
            }
            .addOnFailureListener {

                // What to do when something wrong happens
                Toast.makeText(this, "No se ha podido cargar al profesor", Toast.LENGTH_LONG).show()
                val intent = Intent(this, AdminGestProfActivity::class.java)
                startActivity(intent)

            }

        // Behaviour for the "Confirmar" button
        findViewById<Button>(R.id.id_editProfesoresConfButton).setOnClickListener {

            if(checkCampos()){

                confirmEdit(
                    intent.extras!!.getString("id").toString(),
                    findViewById<EditText>(R.id.id_editProfesoresFormName).text.toString(),
                    findViewById<EditText>(R.id.id_editProfesoresFormDpt).text.toString()
                )

            }

        }

        // Behaviour for the "Volver" button
        findViewById<Button>(R.id.id_editProfesoresVolver).setOnClickListener {

            onBackPressed()

        }
    }

    private fun confirmEdit(id: String, name: String, dpt: String){

        // Alert builder to show the entered text
        val builder = AlertDialog.Builder(this)

        builder.setMessage("¿Confirmar los cambios?")
            .setCancelable(false)
            .setPositiveButton("Aceptar") { dialog, _ ->

                // What to do when clicking "Aceptar"
                dialog.dismiss()

                //TODO VER COMO EDITAR LA ID DEL DOCUMENTO

                if(id != oldProfesor.documento){

                    db.collection("profesores").document(oldProfesor.documento)
                        .delete()
                        .addOnSuccessListener {
                            db.collection("profesores").document(id)
                                .set(mapOf(
                                    "nombre" to name,
                                    "departamento" to dpt
                                ))
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Profesor editado", Toast.LENGTH_LONG).show()
                                    val intent = Intent(this, AdminGestProfActivity::class.java)
                                    startActivity(intent)
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "No se ha podido editar al profesor", Toast.LENGTH_LONG).show()
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Ha ocurrido un error editando al profesor", Toast.LENGTH_LONG).show()
                        }
                }
                else{

                    db.collection("profesores").document(id)
                        .update(mapOf(
                            "nombre" to name,
                            "departamento" to dpt
                        ))
                        .addOnSuccessListener {

                            // What to do when prof is updated
                            Toast.makeText(this, "Profesor editado", Toast.LENGTH_LONG).show()

                            val intent = Intent(this, AdminGestProfActivity::class.java)
                            startActivity(intent)

                        }
                        .addOnFailureListener {

                            // What to do when something wrong happens
                            Toast.makeText(this, "No se ha podido editar al profesor", Toast.LENGTH_LONG).show()

                        }

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
        if(findViewById<EditText>(R.id.id_editProfesoresFormName).text.isBlank()){

            if(findViewById<EditText>(R.id.id_editProfesoresFormDpt).text.isBlank()){

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

            if(findViewById<EditText>(R.id.id_editProfesoresFormDpt).text.isBlank()){

                // If some EditText is empty, set a message and return false
                Toast.makeText(this, "El departamento es obligatorio", Toast.LENGTH_LONG).show()

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

        builder.setMessage("¿Salir sin editar al profesor?")
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