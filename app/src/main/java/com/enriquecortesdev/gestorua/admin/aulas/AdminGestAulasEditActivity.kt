package com.enriquecortesdev.gestorua.admin.aulas

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.enriquecortesdev.gestorua.R
import com.enriquecortesdev.gestorua.items.Aula
import com.google.firebase.firestore.FirebaseFirestore

class AdminGestAulasEditActivity : AppCompatActivity() {

    // Cloud Firestore instance declaration
    private val db = FirebaseFirestore.getInstance()

    private lateinit var oldAula: Aula

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_gest_aulas_edit)

        // Gets aulas information from DB
        db.collection("aulas").document(intent.extras!!.getString("id").toString())
            .get()
            .addOnSuccessListener {
                if(it.exists()){

                    oldAula = Aula(
                        id = it.id,
                        edificio = it.get("edificio") as String,
                        planta = it.get("planta") as String,
                        puerta = it.get("puerta") as String,
                        tipo = it.get("tipo") as String
                    )

                    // Setting up the hints for the form
                    findViewById<EditText>(R.id.id_editAulaFormCodigo).setText(it.id)
                    findViewById<EditText>(R.id.id_editAulaFormEdificio).setText(it.get("edificio") as String)
                    findViewById<EditText>(R.id.id_editAulaFormPlanta).setText(it.get("planta") as String)
                    findViewById<EditText>(R.id.id_editAulaFormPuerta).setText(it.get("puerta") as String)
                    findViewById<EditText>(R.id.id_editAulaFormTipo).setText(it.get("tipo") as String)

                }
            }
            .addOnFailureListener {

                // What to do when something wrong happens
                Toast.makeText(this, "No se ha podido cargar el aula", Toast.LENGTH_LONG).show()
                val intent = Intent(this, AdminGestAulasActivity::class.java)
                startActivity(intent)

            }

        // Behaviour for the "Confirmar" button
        findViewById<Button>(R.id.id_editAulaConfButton).setOnClickListener {

            if(checkCampos()) {

                if (checkChanges()) {

                    confirmEdit(
                        findViewById<EditText>(R.id.id_editAulaFormCodigo).text.toString(),
                        findViewById<EditText>(R.id.id_editAulaFormEdificio).text.toString(),
                        findViewById<EditText>(R.id.id_editAulaFormPlanta).text.toString(),
                        findViewById<EditText>(R.id.id_editAulaFormPuerta).text.toString(),
                        findViewById<EditText>(R.id.id_editAulaFormTipo).text.toString()
                    )

                } else {

                    Toast.makeText(this, "No se han detectado cambios", Toast.LENGTH_LONG).show()

                }
            }

        }

        // Behaviour for the "Volver" button
        findViewById<Button>(R.id.id_editAulaVolver).setOnClickListener {

            onBackPressed()

        }

    }

    private fun confirmEdit(id: String, edificio: String, planta: String, puerta: String, tipo: String){

        // Alert builder to show the entered text
        val builder = AlertDialog.Builder(this)

        builder.setMessage("¿Confirmar los cambios?")
            .setCancelable(false)
            .setPositiveButton("Aceptar") { dialog, _ ->

                // What to do when clicking "Aceptar"
                dialog.dismiss()

                if(id != oldAula.id){

                    db.collection("aulas").document(oldAula.id)
                        .delete()
                        .addOnSuccessListener {
                            db.collection("aulas").document(id)
                                .set(mapOf(
                                    "edificio" to edificio,
                                    "planta" to planta,
                                    "puerta" to puerta,
                                    "tipo" to tipo
                                    ))
                                .addOnSuccessListener {

                                    db.collection("llaves").whereEqualTo("aula", oldAula.id)
                                        .get()
                                        .addOnSuccessListener {keys ->

                                            if(!keys.isEmpty){

                                                db.collection("aulas").document(keys.documents[0].id)
                                                    .update(mapOf(
                                                        "aula" to id
                                                    ))
                                                    .addOnSuccessListener {

                                                        // What to do when the aula is updated
                                                        Toast.makeText(this, "Aula editada correctamente", Toast.LENGTH_LONG).show()
                                                        val intent = Intent(this, AdminGestAulasActivity::class.java)
                                                        startActivity(intent)

                                                    }
                                                    .addOnFailureListener {

                                                        // What to do when something wrong happens
                                                        Toast.makeText(this, "Ha ocurrido un error editando la llave", Toast.LENGTH_LONG).show()

                                                    }

                                            }
                                            else{

                                                // Intent to Aulas menu when there is no key (it can't happen but nvm)
                                                Toast.makeText(this, "Aula editada correctamente", Toast.LENGTH_LONG).show()
                                                val intent = Intent(this, AdminGestAulasActivity::class.java)
                                                startActivity(intent)

                                            }

                                        }
                                        .addOnFailureListener {
                                            // What to do when something wrong happens
                                            Toast.makeText(this, "Ha ocurrido un error buscando la llave", Toast.LENGTH_LONG).show()
                                        }
                                }
                                .addOnFailureListener {
                                    // What to do when something wrong happens
                                    Toast.makeText(this, "Ha ocurrido un error creando el nuevo aula", Toast.LENGTH_LONG).show()
                                }
                        }
                        .addOnFailureListener {
                            // What to do when something wrong happens
                            Toast.makeText(this, "Ha ocurrido un error borrando el viejo aula", Toast.LENGTH_LONG).show()
                        }
                }
                else{

                    db.collection("aulas").document(id)
                        .update(mapOf(
                            "edificio" to edificio,
                            "planta" to planta,
                            "puerta" to puerta,
                            "tipo" to tipo
                        ))
                        .addOnSuccessListener {

                            // What to do when prof is updated
                            Toast.makeText(this, "Aula editada correctamente", Toast.LENGTH_LONG).show()

                            val intent = Intent(this, AdminGestAulasActivity::class.java)
                            startActivity(intent)

                        }
                        .addOnFailureListener {

                            // What to do when something wrong happens
                            Toast.makeText(this, "No se ha podido editar el aula", Toast.LENGTH_LONG).show()

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

    private fun checkChanges(): Boolean {

        if(oldAula.id != findViewById<EditText>(R.id.id_editAulaFormCodigo).text.toString() || oldAula.edificio != findViewById<EditText>(R.id.id_editAulaFormEdificio).text.toString() || oldAula.planta != findViewById<EditText>(R.id.id_editAulaFormPlanta).text.toString() || oldAula.puerta != findViewById<EditText>(R.id.id_editAulaFormPuerta).text.toString() || oldAula.tipo !=         findViewById<EditText>(R.id.id_editAulaFormTipo).text.toString()){

            return true

        }

        return false

    }

    private fun checkCampos(): Boolean {

        // Checks that every EditText is filled
        if (findViewById<EditText>(R.id.id_editAulaFormCodigo).text.isBlank()) {

            if (findViewById<EditText>(R.id.id_editAulaFormEdificio).text.isBlank() || findViewById<EditText>(R.id.id_editAulaFormPlanta).text.isBlank() || findViewById<EditText>(R.id.id_editAulaFormPuerta).text.isBlank() || findViewById<EditText>(R.id.id_editAulaFormTipo).text.isBlank()) {

                // If some EditText is empty, set a message and return false
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_LONG).show()

            } else {

                // If some EditText is empty, set a message and return false
                Toast.makeText(this, "El código del aula es obligatorio", Toast.LENGTH_LONG).show()

            }

            return false

        } else {

            if (findViewById<EditText>(R.id.id_editAulaFormEdificio).text.isBlank()) {

                if (findViewById<EditText>(R.id.id_editAulaFormPlanta).text.isBlank() || findViewById<EditText>(R.id.id_editAulaFormPuerta).text.isBlank() || findViewById<EditText>(R.id.id_editAulaFormTipo).text.isBlank()) {

                    // If some EditText is empty, set a message and return false
                    Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_LONG).show()

                } else {

                    // If some EditText is empty, set a message and return false
                    Toast.makeText(this, "El edificio es obligatorio", Toast.LENGTH_LONG).show()

                }

                return false

            } else {

                if (findViewById<EditText>(R.id.id_editAulaFormPlanta).text.isBlank()) {

                    if (findViewById<EditText>(R.id.id_editAulaFormPuerta).text.isBlank() || findViewById<EditText>(R.id.id_editAulaFormTipo).text.isBlank()) {

                        // If some EditText is empty, set a message and return false
                        Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_LONG).show()

                    } else {

                        // If some EditText is empty, set a message and return false
                        Toast.makeText(this, "La planta es obligatoria", Toast.LENGTH_LONG).show()

                    }

                    return false

                } else {

                    if (findViewById<EditText>(R.id.id_editAulaFormPuerta).text.isBlank()) {

                        if (findViewById<EditText>(R.id.id_editAulaFormTipo).text.isBlank()) {

                            // If some EditText is empty, set a message and return false
                            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_LONG).show()

                        } else {

                            // If some EditText is empty, set a message and return false
                            Toast.makeText(this, "La puerta es obligatoria", Toast.LENGTH_LONG).show()

                        }

                        return false

                    } else {

                        if (findViewById<EditText>(R.id.id_editAulaFormTipo).text.isBlank()) {

                            // If some EditText is empty, set a message and return false
                            Toast.makeText(this, "El tipo es obligatorio", Toast.LENGTH_LONG).show()

                            return false

                        } else {

                            // If they are all filled, returns true
                            return true

                        }

                    }

                }

            }

        }

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {

        // Alert builder to show the entered text
        val builder = AlertDialog.Builder(this)

        builder.setMessage("¿Salir sin editar el aula?")
            .setCancelable(false)
            .setPositiveButton("Aceptar") { dialog, _ ->

                // What to do when clicking "Aceptar"
                dialog.dismiss()

                val intent = Intent(this, AdminGestAulasActivity::class.java)
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