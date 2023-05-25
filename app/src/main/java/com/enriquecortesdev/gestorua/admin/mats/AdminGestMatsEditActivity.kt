package com.enriquecortesdev.gestorua.admin.mats

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import com.enriquecortesdev.gestorua.R
import com.enriquecortesdev.gestorua.items.Material
import com.google.firebase.firestore.FirebaseFirestore

class AdminGestMatsEditActivity : AppCompatActivity() {

    // Cloud Firestore instance declaration
    private val db = FirebaseFirestore.getInstance()

    private lateinit var oldMaterial: Material

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_gest_mats_edit)

        // Gets material information from DB
        db.collection("materiales").document(intent.extras!!.getString("id").toString())
            .get()
            .addOnSuccessListener {
                if(it.exists()){

                    oldMaterial = Material(
                        id = it.id,
                        tipo = it.get("tipo") as String,
                        aula = it.get("aula") as String,
                        disponible = it.get("disponible") as Boolean,
                        asignado = it.get("asignado") as Boolean,
                        actual = it.get("actual") as String
                    )

                    // Setting up the hints for the form
                    if(it.get("aula") != "global"){
                        findViewById<EditText>(R.id.id_editMatsFormAula).setText(it.get("aula") as String)
                    }
                    findViewById<EditText>(R.id.id_editMatsFormTipo).setText(it.get("tipo") as String)
                    findViewById<SwitchCompat>(R.id.id_editMatsDispSwitch).isChecked = it.get("disponible") as Boolean
                    findViewById<SwitchCompat>(R.id.id_editMatsAsignSwitch).isChecked = it.get("asignado") as Boolean
                }
            }
            .addOnFailureListener {

                // What to do when something wrong happens
                Toast.makeText(this, "No se ha podido cargar el material", Toast.LENGTH_LONG).show()
                val intent = Intent(this, AdminGestMatsActivity::class.java)
                startActivity(intent)

            }

        // Behaviour for the "Confirmar" button
        findViewById<Button>(R.id.id_editMatsConfButton).setOnClickListener {

            if(findViewById<EditText>(R.id.id_editMatsFormTipo).text.isNotBlank()) {
                if (checkChanges()) {
                    confirmEdit()

                } else {
                    Toast.makeText(this, "No se han detectado cambios", Toast.LENGTH_LONG).show()
                }
            }
            else{
                Toast.makeText(this, "El tipo es obligatorio", Toast.LENGTH_LONG).show()
            }
        }

        // Behaviour for the "Volver" button
        findViewById<Button>(R.id.id_editMatsVolver).setOnClickListener {

            onBackPressed()

        }

    }

    private fun registerChanges() {

        if(!findViewById<SwitchCompat>(R.id.id_editMatsAsignSwitch).isChecked){
            db.collection("materiales").document(oldMaterial.id)
                .update(mapOf(
                    "aula" to findViewById<EditText>(R.id.id_editMatsFormAula).text.toString(),
                    "tipo" to findViewById<EditText>(R.id.id_editMatsFormTipo).text.toString(),
                    "disponible" to findViewById<SwitchCompat>(R.id.id_editMatsDispSwitch).isChecked,
                    "asignado" to findViewById<SwitchCompat>(R.id.id_editMatsAsignSwitch).isChecked,
                    "actual" to ""
                ))
                .addOnSuccessListener {

                    // What to do when prof is updated
                    Toast.makeText(this, "Material editado correctamente", Toast.LENGTH_LONG).show()

                    val intent = Intent(this, AdminGestMatsActivity::class.java)
                    startActivity(intent)

                }
                .addOnFailureListener {

                    // What to do when something wrong happens
                    Toast.makeText(this, "No se ha podido editar el material", Toast.LENGTH_LONG).show()

                }
        }
        else{
            db.collection("materiales").document(oldMaterial.id)
                .update(mapOf(
                    "aula" to findViewById<EditText>(R.id.id_editMatsFormAula).text.toString(),
                    "tipo" to findViewById<EditText>(R.id.id_editMatsFormTipo).text.toString(),
                    "disponible" to findViewById<SwitchCompat>(R.id.id_editMatsDispSwitch).isChecked,
                    "asignado" to findViewById<SwitchCompat>(R.id.id_editMatsAsignSwitch).isChecked
                ))
                .addOnSuccessListener {

                    // What to do when prof is updated
                    Toast.makeText(this, "Material editado correctamente", Toast.LENGTH_LONG).show()

                    val intent = Intent(this, AdminGestMatsActivity::class.java)
                    startActivity(intent)

                }
                .addOnFailureListener {

                    // What to do when something wrong happens
                    Toast.makeText(this, "No se ha podido editar el material", Toast.LENGTH_LONG).show()

                }
        }
    }

    private fun confirmEdit() {

        // Alert builder to show the entered text
        val builder = AlertDialog.Builder(this)

        builder.setMessage("¿Confirmar los cambios?")
            .setCancelable(false)
            .setPositiveButton("Aceptar") { dialog, _ ->

                dialog.dismiss()

                if(oldMaterial.actual.isNotBlank() && !findViewById<SwitchCompat>(R.id.id_editMatsAsignSwitch).isChecked){
                    // Alert builder to show the entered text
                    val builder2 = AlertDialog.Builder(this)

                    builder2.setMessage("Este material está actualmente asignado, si desactivas el switch de asignación se liberará. ¿Seguro que quieres continuar?")
                        .setCancelable(false)
                        .setPositiveButton("Aceptar") { dialog2, _ ->
                            // What to do when clicking "Aceptar"
                            dialog2.dismiss()
                            registerChanges()
                        }
                        .setNegativeButton("Cancelar") { dialog2, _ ->
                            // What to do when clicking "Cancelar"
                            dialog2.dismiss()
                        }
                    val alert2 = builder2.create()
                    alert2.show()
                }
                else{
                    registerChanges()
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

        if(findViewById<EditText>(R.id.id_editMatsFormAula).text.toString() != oldMaterial.aula || findViewById<SwitchCompat>(R.id.id_editMatsDispSwitch).isChecked != oldMaterial.disponible || findViewById<SwitchCompat>(R.id.id_editMatsAsignSwitch).isChecked != oldMaterial.asignado){
            return true
        }

        if(findViewById<EditText>(R.id.id_editMatsFormTipo).text.isBlank()){
            if(oldMaterial.tipo != "global"){
                return true
            }
        }
        else{
            if(findViewById<EditText>(R.id.id_editMatsFormTipo).text.toString() != oldMaterial.tipo){
                return true
            }
        }

        return false

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {

        // Alert builder to show the entered text
        val builder = AlertDialog.Builder(this)

        builder.setMessage("¿Salir sin editar el material?")
            .setCancelable(false)
            .setPositiveButton("Aceptar") { dialog, _ ->

                // What to do when clicking "Aceptar"
                dialog.dismiss()

                val intent = Intent(this, AdminGestMatsActivity::class.java)
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