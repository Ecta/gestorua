package com.enriquecortesdev.gestorua.admin

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.enriquecortesdev.gestorua.R
import com.google.android.material.internal.ViewUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AdminLoginActivity : AppCompatActivity() {

    // Firebase Auth instance declaration
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Checks if there is a previous session started and ends it
        if(auth.currentUser != null){
            auth.signOut()
        }

        // Behaviour for the "Acceder" button
        findViewById<Button>(R.id.send_button).setOnClickListener {

            // Email and Password declarations
            val emailForm = findViewById<EditText>(R.id.formulario_tiu2)
            val emailText = emailForm.text.toString()
            val pwdForm = findViewById<EditText>(R.id.formulario_tiu)
            val pwdText = pwdForm.text.toString()

            if(checkForm(emailText, pwdText)){

                // Call function to sign into the system
                auth.signInWithEmailAndPassword(emailText, pwdText)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {

                            // Sign in success
                            Toast.makeText(this, "Credenciales correctas", Toast.LENGTH_LONG).show()

                            // Intent call to AdminMenuActivity
                            val intent = Intent(this, AdminMenuActivity::class.java)
                            startActivity(intent)

                        } else {

                            // If sign in fails, manage Exceptions

                            if(task.exception?.message.toString() == "The email address is badly formatted."){

                                Toast.makeText(this, "Introduce un correo electrónico válido", Toast.LENGTH_LONG).show()

                            }

                            // Alert builder to show some critical exceptions
                            val builder = AlertDialog.Builder(this)

                            if(task.exception?.message.toString() == "There is no user record corresponding to this identifier. The user may have been deleted."){

                                builder.setMessage("El correo introducido no existe en el sistema")
                                    .setCancelable(true)
                                    .setPositiveButton("Aceptar") { dialog, _ ->
                                        // What to do when clicking "Aceptar"
                                        dialog.dismiss()
                                    }

                                val alert = builder.create()
                                alert.show()

                            }

                            if(task.exception?.message.toString() == "The password is invalid or the user does not have a password."){

                                builder.setMessage("La contraseña es incorrecta")
                                    .setCancelable(true)
                                    .setPositiveButton("Aceptar") { dialog, _ ->
                                        // What to do when clicking "Aceptar"
                                        dialog.dismiss()
                                    }

                                val alert = builder.create()
                                alert.show()

                            }

                        }

                    }

            }

        }

        // Behaviour for the "Cancelar" button
        findViewById<Button>(R.id.cancel_button).setOnClickListener {

            // Returns to the previous Activity
            onBackPressedDispatcher.onBackPressed()
        }

    }

    // Function for checking empty data in login form
    private fun checkForm(email: String, pwd: String): Boolean {

        if(email.isBlank() && pwd.isBlank()){

            // In case both "email" and "pwd" are blank (whitespaces, empty or null)
            Toast.makeText(this, "Ambos campos son obligatorios", Toast.LENGTH_LONG).show()
            return false

        }

        if(email.isBlank()){

            // In case "email" is blank (whitespaces, empty or null)
            Toast.makeText(this, "El correo electrónico es obligatorio", Toast.LENGTH_LONG).show()
            return false

        }

        if(pwd.isBlank()){

            // In case "pwd" is blank (whitespaces, empty or null)
            Toast.makeText(this, "La contraseña es obligatoria", Toast.LENGTH_LONG).show()
            return false

        }

        // In case both "email" and "pwd" are not blank (whitespaces, empty or null)
        return true

    }

}