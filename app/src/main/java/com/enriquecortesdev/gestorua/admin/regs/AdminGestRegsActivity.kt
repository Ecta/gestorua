package com.enriquecortesdev.gestorua.admin.regs

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enriquecortesdev.gestorua.R
import com.enriquecortesdev.gestorua.admin.AdminMenuActivity
import com.enriquecortesdev.gestorua.admin.adapter.AulaAdapter
import com.enriquecortesdev.gestorua.admin.adapter.RegistroAdapter
import com.enriquecortesdev.gestorua.items.Aula
import com.enriquecortesdev.gestorua.items.Registro
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AdminGestRegsActivity : AppCompatActivity() {

    // Cloud Firestore instance declaration
    private val db = FirebaseFirestore.getInstance()

    private var registrosList = ArrayList<Registro>()

    // Empty adapter declaration
    private val registroAdapter = RegistroAdapter(registrosList)

    private var filtrosSwitcher: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_gest_regs)

        // Sets up the recyclerView and attaches the adapter (with an empty list)
        val manager = LinearLayoutManager(this)
        val decoration = DividerItemDecoration(this, manager.orientation)
        val recyclerView = findViewById<RecyclerView>(R.id.id_regsRecycler)
        recyclerView.layoutManager = manager
        recyclerView.adapter = registroAdapter
        recyclerView.addItemDecoration(decoration)

        // Sets the current day date to the calendar
        setTodaysDate()

        // Loads a full list of aulas and refreshes the adapter
        loadRegistros()

        // Behaviour for the "Date" Input Text
        findViewById<EditText>(R.id.id_regsDateFilter).setOnClickListener {
            datePicker(findViewById(R.id.id_regsDateFilter))
        }

        // Behaviour for the "Filtrar" button
        findViewById<Button>(R.id.id_regsFiltrarButton).setOnClickListener {
            findViewById<ProgressBar>(R.id.id_regsProgressBar).visibility = View.VISIBLE
            loadRegistros()
        }

        // Behaviour for the "Volver" button
        findViewById<Button>(R.id.id_regsVolverButton).setOnClickListener {
            // Returns to the AdminMenuActivity
            onBackPressed()
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadRegistros() {

        db.collection("profesores")
            .get()
            .addOnSuccessListener {profesores ->
                db.collection("registro").whereEqualTo("fecha", findViewById<EditText>(R.id.id_regsDateFilter).text.toString())
                    .get()
                    .addOnSuccessListener {registros ->

                        registrosList.clear()

                        if(registros.isEmpty){
                            registroAdapter.notifyDataSetChanged()
                            findViewById<TextView>(R.id.id_regsEmptyPlaceholder).visibility = View.VISIBLE
                        }
                        else{
                            if(profesores.isEmpty){
                                registroAdapter.notifyDataSetChanged()
                                findViewById<TextView>(R.id.id_regsEmptyPlaceholder).visibility = View.VISIBLE
                            }
                            else{
                                //SI HAY ALGUNA COINCIDENCIA
                                registros.forEach{registro ->
                                    profesores.forEach{profesor ->
                                        if(registro.get("profesor") == profesor.id){
                                            val nuevoRegistro = Registro(
                                                id = registro.id,
                                                activo = registro.get("activo") as Boolean,
                                                aula = registro.get("aula") as String,
                                                fecha = registro.get("fecha") as String,
                                                horaini = registro.get("horaini") as String,
                                                horafin = registro.get("horafin") as String,
                                                profesor = profesor.get("nombre") as String
                                            )
                                            registrosList.add(nuevoRegistro)
                                            registroAdapter.notifyDataSetChanged()
                                        }
                                    }
                                }
                                findViewById<ProgressBar>(R.id.id_regsProgressBar).visibility = View.GONE
                                findViewById<TextView>(R.id.id_regsEmptyPlaceholder).visibility = View.GONE
                            }
                        }

                    }
                    .addOnFailureListener {
                        findViewById<ProgressBar>(R.id.id_regsProgressBar).visibility = View.GONE
                        findViewById<TextView>(R.id.id_regsEmptyPlaceholder).visibility = View.GONE
                        Toast.makeText(this, "No se han podido consultar los registros", Toast.LENGTH_LONG).show()
                    }

            }
            .addOnFailureListener {
                findViewById<ProgressBar>(R.id.id_regsProgressBar).visibility = View.GONE
                findViewById<TextView>(R.id.id_regsEmptyPlaceholder).visibility = View.GONE
                Toast.makeText(this, "No se han podido consultar los profesores", Toast.LENGTH_LONG).show()
            }
        findViewById<ProgressBar>(R.id.id_regsProgressBar).visibility = View.GONE
        findViewById<TextView>(R.id.id_regsEmptyPlaceholder).visibility = View.GONE
    }

    private fun datePicker(dateText: EditText){
        val currentDate = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            R.style.PickerDialogStyle,
            { _, year, monthOfYear, dayOfMonth ->
                currentDate.set(Calendar.YEAR, year)
                currentDate.set(Calendar.MONTH, monthOfYear)
                currentDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(currentDate.time)
                dateText.setText(formattedDate)
            },
            currentDate.get(Calendar.YEAR),
            currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun setTodaysDate(){
        val currentTime = Calendar.getInstance()
        val yearInit = currentTime.get(Calendar.YEAR)
        val monthInit = currentTime.get(Calendar.MONTH) + 1
        val dayInit = currentTime.get(Calendar.DAY_OF_MONTH)
        findViewById<EditText>(R.id.id_regsDateFilter).setText(String.format(dayInit.toString() + "/" + (if (monthInit < 10) "0$monthInit" else monthInit) + "/" + yearInit.toString()))
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val intent = Intent(this, AdminMenuActivity::class.java)
        startActivity(intent)
    }

}