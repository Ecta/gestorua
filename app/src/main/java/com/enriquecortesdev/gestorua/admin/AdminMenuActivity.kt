package com.enriquecortesdev.gestorua.admin

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.enriquecortesdev.gestorua.R
import com.enriquecortesdev.gestorua.admin.aulas.AdminGestAulasActivity
import com.enriquecortesdev.gestorua.admin.depts.AdminGestDeptActivity
import com.enriquecortesdev.gestorua.admin.mats.AdminGestMatsActivity
import com.enriquecortesdev.gestorua.admin.profs.AdminGestProfActivity
import com.enriquecortesdev.gestorua.admin.regs.AdminGestRegsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AdminMenuActivity : AppCompatActivity() {

    // Firebase Auth instance declaration
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_menu)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Checks if there is a logged admin and returns to home screen if there is not
        checkAdmin()

        // Behaviour for the "Gestionar profesores" button
        findViewById<Button>(R.id.profesores_button).setOnClickListener {

            val intent = Intent(this, AdminGestProfActivity::class.java)
            startActivity(intent)

        }

        // Behaviour for the "Gestionar departamentos" button
        findViewById<Button>(R.id.departamentos_button).setOnClickListener {

            val intent = Intent(this, AdminGestDeptActivity::class.java)
            startActivity(intent)

        }

        // Behaviour for the "Gestionar aulas" button
        findViewById<Button>(R.id.aulas_button).setOnClickListener {

            val intent = Intent(this, AdminGestAulasActivity::class.java)
            startActivity(intent)

        }

        // Behaviour for the "Gestionar materiales" button
        findViewById<Button>(R.id.materiales_button).setOnClickListener {

            val intent = Intent(this, AdminGestMatsActivity::class.java)
            startActivity(intent)

        }

        // Behaviour for the "Gestionar materiales" button
        findViewById<Button>(R.id.asignaciones_button).setOnClickListener {

            val intent = Intent(this, AdminGestRegsActivity::class.java)
            startActivity(intent)

        }

        // Behaviour for the "Salir" button
        findViewById<Button>(R.id.exit_button).setOnClickListener {

            // Call to the exitButton() function
            exitButton()

        }

    }

    // Function to set the behaviour of the exit button
    private fun exitButton() {

        // Alert builder to show the entered text
        val builder = AlertDialog.Builder(this)

        builder.setMessage("¿Salir y volver al menú principal?")
            .setCancelable(false)
            .setPositiveButton("Aceptar") { dialog, _ ->

                // What to do when clicking "Aceptar"
                dialog.dismiss()

                // Checks if there is a previous session started and ends it
                if(auth.currentUser != null){
                    auth.signOut()
                }

                // Return to the previous activity (MainActivity)
                onBackPressedDispatcher.onBackPressed()

            }
            .setNegativeButton("Cancelar") { dialog, _ ->

                // What to do when clicking "Cancelar"
                dialog.dismiss()

            }

        val alert = builder.create()
        alert.show()

    }

    // Function to check if there is a logged admin
    private fun checkAdmin(){

        // Returns to home screen if there is not logged admin
        if(auth.currentUser == null){

            // Displays a message to the user
            Toast.makeText(this, "Menú exclusivo para administradores", Toast.LENGTH_LONG).show()

            // Returns to the previous Activity
            onBackPressedDispatcher.onBackPressed()

        }

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        exitButton()
    }

}