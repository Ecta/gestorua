package com.enriquecortesdev.gestorua.entry

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.NfcA
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enriquecortesdev.gestorua.R
import com.enriquecortesdev.gestorua.admin.adapter.MaterialAdapter
import com.enriquecortesdev.gestorua.items.Material
import com.enriquecortesdev.gestorua.main.MainActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.*

class NewEntryActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    // Cloud Firestore instance declaration
    private val db = FirebaseFirestore.getInstance()

    // NFC Instance and alert declarations
    private var nfcAdapter: NfcAdapter? = null
    private var existsNFC: Boolean = false
    private var nfcAlertBuilder: AlertDialog.Builder? = null
    private var nfcAlert: AlertDialog? = null

    // Intent vars initialized on null
    private var idProfesor: String? = null
    private var idAula: String? = null
    private var idKey: String? = null

    // Material related vars
    private var isMaterial: Boolean = false
    private var materialesList = ArrayList<Material>()
    private val materialAdapter = MaterialAdapter(materialesList, {borrarMaterial(it)})

    private var registerList: Task<QuerySnapshot?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_entry)

        /*val material1 = Material("1", "hola", "adios", true, false, "")
        val material2 = Material("2", "hola", "adios", true, false, "")
        val material3 = Material("3", "hola", "adios", true, false, "")
        val material4 = Material("4", "hola", "adios", true, false, "")
        val material5 = Material("5", "hola", "adios", true, false, "")

        materialesList.add(material1)
        materialesList.add(material2)
        materialesList.add(material3)
        materialesList.add(material4)
        materialesList.add(material5)*/

        // NFC Adapter Instance
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Checks the NFC function of the device
        checkNFC()

        // Sets up the initial texts (Prof and Aula from Intent, Date and Time from Calendar)
        initialSetup()

        // Gets the register list from DB (will use it later)
        registerList = db.collection("registro").orderBy("horaini").get()

        // Sets up the recyclerView and attaches the adapter (with an empty list)
        val manager = LinearLayoutManager(this)
        val decoration = DividerItemDecoration(this, manager.orientation)
        val recyclerView = findViewById<RecyclerView>(R.id.id_ExtraMatsRecycler)
        recyclerView.layoutManager = manager
        recyclerView.adapter = materialAdapter
        recyclerView.addItemDecoration(decoration)

        // Create NFC tag alert to scan a key (will use it later)
        nfcAlertBuilder = AlertDialog.Builder(this)
        nfcAlertBuilder!!.setMessage("Acerca una etiqueta NFC para continuar")
            .setCancelable(false)
            .setNegativeButton("Cancelar") { dialog, _ ->

                // What to do when clicking "Cancelar"
                // Pauses the listening only if nfcAdapter exists
                if(existsNFC){
                    nfcAdapter!!.disableReaderMode(this)
                }
                dialog.dismiss()
            }
        nfcAlert = nfcAlertBuilder!!.create()

        idProfesor = intent.extras?.getString("idProfesor").toString()
        idAula = intent.extras?.getString("idAula").toString()

        // Behaviour for the "Change Aula" Button
        findViewById<Button>(R.id.id_addEntryAulaChangeButton).setOnClickListener {

            if(existsNFC){
                val readerCallback: NfcAdapter.ReaderCallback = this
                val flags = NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
                nfcAdapter!!.enableReaderMode(this, readerCallback, flags, Bundle())
                nfcAlert!!.show()
            }

        }

        findViewById<Button>(R.id.id_addEntryNewMats).setOnClickListener {
            if(findViewById<EditText>(R.id.id_addEntryFormAula).text.isEmpty()){
                Toast.makeText(this, "Debes especificar un aula para agregar materiales", Toast.LENGTH_LONG).show()
            }
            else{
                if(existsNFC){
                    val readerCallback: NfcAdapter.ReaderCallback = this
                    val flags = NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
                    nfcAdapter!!.enableReaderMode(this, readerCallback, flags, Bundle())
                    isMaterial = true
                    nfcAlert!!.show()
                }
            }

        }

        // Behaviour for the "Time Init" Input Text
        findViewById<Button>(R.id.id_addEntryDateChangeButton).setOnClickListener {
            datePicker(findViewById(R.id.id_addEntryFormDate))
        }

        // Behaviour for the "Time Init" Input Text
        findViewById<EditText>(R.id.id_addEntryFormTimeInit).setOnClickListener {
            timePicker(findViewById(R.id.id_addEntryFormTimeInit))
        }

        // Behaviour for the "Time End" Input Text
        findViewById<EditText>(R.id.id_addEntryFormTimeEnd).setOnClickListener {
            timePicker(findViewById(R.id.id_addEntryFormTimeEnd))
        }

        // Behaviour for the "Confirmar" Button
        findViewById<Button>(R.id.id_addEntryConfButton).setOnClickListener {
            if(checkCampos()){
                if(checkHoras()){
                    confirmEntry(
                        findViewById<EditText>(R.id.id_addEntryFormProf).text.toString(),
                        findViewById<EditText>(R.id.id_addEntryFormAula).text.toString(),
                        findViewById<EditText>(R.id.id_addEntryFormDate).text.toString(),
                        findViewById<EditText>(R.id.id_addEntryFormTimeInit).text.toString(),
                        findViewById<EditText>(R.id.id_addEntryFormTimeEnd).text.toString()
                    )
                }
            }
        }

        // Behaviour for the "Cancelar" button
        findViewById<Button>(R.id.id_addEntryVolver).setOnClickListener {
            // Returns to the previous Activity
            onBackPressed()
        }

    }

    private fun confirmEntry(textProf: String, textAula: String, textDate: String, textTimeInit: String, textTimeEnd: String) {

        //AQUI SABEMOS QUE TODOS LOS CAMPOS ESTAN RELLENOS, LAS HORAS ESTAN BIEN PUESTAS Y NO COINCIDE CON OTRA CLASE
        db.collection("profesores").document(textProf)
            .get()
            .addOnSuccessListener {
                if(it.exists()){

                    db.collection("registro").document()
                        .set(mapOf(
                            "aula" to textAula,
                            "fecha" to textDate,
                            "horaini" to textTimeInit,
                            "horafin" to textTimeEnd,
                            "profesor" to textProf,
                            "activo" to true
                        ))
                        .addOnSuccessListener {
                            db.collection("llaves").document(idKey!!)
                                .update(mapOf(
                                    "asignada" to true,
                                    "profesor" to textProf
                                ))
                                .addOnSuccessListener {
                                    db.runBatch { batch ->
                                        // Batch for updating every material added
                                        for (material in materialesList){
                                            batch.update(
                                                db.collection("materiales").document(material.id), mapOf(
                                                    "asignado" to true,
                                                    "actual" to findViewById<EditText>(R.id.id_addEntryFormAula).text.toString()
                                                ))
                                        }

                                    }
                                    Toast.makeText(this, "Registro creado correctamente", Toast.LENGTH_LONG).show()
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Ha habido un problema asignando la llave", Toast.LENGTH_LONG).show()
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Ha habido un problema creando el registro", Toast.LENGTH_LONG).show()
                        }

                }
                else{
                    Toast.makeText(this, "El profesor no existe", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Ha habido un problema buscando al profesor", Toast.LENGTH_LONG).show()
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onTagDiscovered(tag: Tag?) {

        val nfcA = NfcA.get(tag)
        nfcA.connect()

        // Initialize the total message variable
        val messageBytes = mutableListOf<Byte>()

        // Loop to read every page (we know the message is 20 char long, so there will be 5 pages with 4 bytes each: 4, 5, 6, 7, 8)
        for (page in 4 .. 8) {

            // NfcA Reading Command (0x30 means read)
            val readCommand = byteArrayOf(0x30.toByte(), page.toByte())

            // Transceive the command to the NFC tag
            val readResult = nfcA.transceive(readCommand)

            // Copy the desired result range (NFC answer is larger than desired)
            val pageData = readResult.copyOfRange(0, 4)

            // Add the page to the total message variable
            messageBytes.addAll(pageData.toList())
        }

        nfcA.close()

        // Convert the message from MutableList to ByteArray and then to String (UTF-8)
        val text = String(messageBytes.toByteArray(), Charsets.UTF_8)

        if(isMaterial){
            db.collection("materiales").document(text).get()
                .addOnSuccessListener {
                    if(it.exists()){

                        if(it.get("aula").toString() == findViewById<EditText>(R.id.id_addEntryFormAula).text.toString() || it.get("aula").toString() == "global"){

                            if(!(it.get("asignado") as Boolean)){
                                // Create a new Material to add to the materialesList ArrayList
                                val nuevoMaterial = Material(
                                    id = it.id,
                                    aula = it.get("aula") as String,
                                    tipo = it.get("tipo") as String,
                                    disponible = it.get("disponible") as Boolean,
                                    asignado = it.get("asignado") as Boolean,
                                    actual = it.get("actual") as String
                                )

                                // Add the created Material to the materialesList ArrayList
                                materialesList.add(nuevoMaterial)
                                // Sends materialesList to the adapter to fill the recyclerView
                                materialAdapter.notifyDataSetChanged()
                                Toast.makeText(this, "Material añadido correctamente", Toast.LENGTH_LONG).show()
                                nfcAlert!!.dismiss()

                            }
                            else{
                                Toast.makeText(this, "Este material ya está asignado", Toast.LENGTH_LONG).show()
                                nfcAlert!!.dismiss()
                            }

                        }
                        else{
                            Toast.makeText(this, "Este material pertenece a otro aula", Toast.LENGTH_LONG).show()
                        }

                    }
                    else{
                        Toast.makeText(this, "El material no está registrado", Toast.LENGTH_LONG).show()
                        nfcAlert!!.dismiss()
                    }

                }
                .addOnFailureListener {
                    Toast.makeText(this, "No se ha podido comprobar el material", Toast.LENGTH_LONG).show()
                    nfcAlert!!.dismiss()
                }
        }
        else{
            db.collection("llaves").document(text).get()
                .addOnSuccessListener {
                    if(it.exists()){

                        if(it.get("asignada") != true){

                            idKey = it.id
                            findViewById<EditText>(R.id.id_addEntryFormAula).setText(it.get("aula").toString())
                            Toast.makeText(this, "Llave escaneada correctamente", Toast.LENGTH_LONG).show()
                            nfcAlert!!.dismiss()

                        }
                        else{

                            Toast.makeText(this, "Esta llave ya está asignada", Toast.LENGTH_LONG).show()
                            nfcAlert!!.dismiss()

                        }

                    }
                    else{
                        Toast.makeText(this, "La llave no está registrada", Toast.LENGTH_LONG).show()
                        nfcAlert!!.dismiss()
                    }

                }
                .addOnFailureListener {
                    Toast.makeText(this, "No se ha podido comprobar la llave", Toast.LENGTH_LONG).show()
                    nfcAlert!!.dismiss()
                }
        }
        // Pauses the listening only if nfcAdapter exists
        if(existsNFC){
            nfcAdapter!!.disableReaderMode(this)
        }
        isMaterial = false
    }

    private fun initialSetup(){

        if(idProfesor != null){
            findViewById<EditText>(R.id.id_addEntryFormProf).setText(idProfesor)
        }

        if(idAula != null){
            findViewById<EditText>(R.id.id_addEntryFormAula).setText(idAula)
        }

        val currentTime = Calendar.getInstance()

        val hourInit = currentTime.get(Calendar.HOUR_OF_DAY) + 1
        val hourEnd = currentTime.get(Calendar.HOUR_OF_DAY) + 3
        findViewById<EditText>(R.id.id_addEntryFormTimeInit).setText(String.format("$hourInit:00"))
        findViewById<EditText>(R.id.id_addEntryFormTimeEnd).setText(String.format("$hourEnd:00"))

        val yearInit = currentTime.get(Calendar.YEAR)
        val monthInit = currentTime.get(Calendar.MONTH) + 1
        val dayInit = currentTime.get(Calendar.DAY_OF_MONTH)
        findViewById<EditText>(R.id.id_addEntryFormDate).setText(String.format(dayInit.toString() + "/" + (if (monthInit < 10) "0$monthInit" else monthInit) + "/" + yearInit.toString()))
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

    private fun timePicker(timeText: EditText) {
        val currentTime = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            this,
            R.style.PickerDialogStyle,
            { _, selectedHour, selectedMinute ->
                currentTime.set(Calendar.HOUR_OF_DAY, selectedHour)
                currentTime.set(Calendar.MINUTE, selectedMinute)

                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val formattedTime = timeFormat.format(currentTime.time)
                timeText.setText(formattedTime)
            },
            currentTime.get(Calendar.HOUR_OF_DAY),
            currentTime.get(Calendar.MINUTE),
            true
        )

        timePickerDialog.show()

    }

    private fun checkHoras(): Boolean {

        val registerListResult = registerList?.result

        if(registerListResult != null){

            if(!registerListResult.isEmpty){

                registerListResult.forEach{
                    if(it.get("fecha").toString() == findViewById<EditText>(R.id.id_addEntryFormDate).text.toString()){
                        if(!((it.get("horaini").toString() < findViewById<EditText>(R.id.id_addEntryFormTimeInit).text.toString() && it.get("horafin").toString() <= findViewById<EditText>(R.id.id_addEntryFormTimeInit).text.toString()) || (it.get("horaini").toString() >= findViewById<EditText>(R.id.id_addEntryFormTimeEnd).text.toString() && it.get("horafin").toString() > findViewById<EditText>(R.id.id_addEntryFormTimeEnd).text.toString()))){
                            Toast.makeText(this, "Ya hay un registro de " + it.get("horaini").toString() + " a " + it.get("horafin").toString(), Toast.LENGTH_LONG).show()
                            return false
                        }
                    }

                }

            }
            return true
        }
        return true
    }

    private fun checkCampos(): Boolean {

        if(findViewById<EditText>(R.id.id_addEntryFormProf).text.isBlank() || findViewById<EditText>(R.id.id_addEntryFormAula).text.isBlank() || findViewById<EditText>(R.id.id_addEntryFormDate).text.isBlank() || findViewById<EditText>(R.id.id_addEntryFormTimeInit).text.isBlank() || findViewById<EditText>(R.id.id_addEntryFormTimeEnd).text.isBlank()){
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_LONG).show()
            return false
        }

        if(findViewById<EditText>(R.id.id_addEntryFormTimeInit).text.toString() >= findViewById<EditText>(R.id.id_addEntryFormTimeEnd).text.toString()){
            Toast.makeText(this, "La hora de fin debe ser mayor que la de inicio", Toast.LENGTH_LONG).show()
            return false
        }

        return true

    }


    @SuppressLint("NotifyDataSetChanged")
    private fun borrarMaterial(id: String) {

        // Alert builder to show the entered text
        val builder = AlertDialog.Builder(this)

        builder.setMessage("¿Quitar material de la lista?")
            .setCancelable(false)
            .setPositiveButton("Aceptar") { dialog, _ ->

                // What to do when clicking "Aceptar"
                dialog.dismiss()

                // Removes the specified material from the materialesList
                materialesList.removeIf{
                    it.id == id
                }
                // Sends materialesList to the adapter to fill the recyclerView
                materialAdapter.notifyDataSetChanged()

            }
            .setNegativeButton("Cancelar") { dialog, _ ->

                // What to do when clicking "Cancelar"
                dialog.dismiss()

            }

        val alert = builder.create()
        alert.show()

    }

    // This function is used to check the NFC capability of the device
    private fun checkNFC(): Boolean {

        // Checks if the device has NFC chip
        if(nfcAdapter != null){
            if(!nfcAdapter?.isEnabled!!){
                Toast.makeText(this, "La antena NFC está desactivada", Toast.LENGTH_LONG).show()
                existsNFC = false
                return false
            }
        }
        else{
            Toast.makeText(this, "El dispositivo no tiene tecnología NFC", Toast.LENGTH_LONG).show()
            existsNFC = false
            return false
        }
        existsNFC = true
        return true
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {

        // Alert builder to show the entered text
        val builder = AlertDialog.Builder(this)

        builder.setMessage("¿Salir sin registrar préstamo?")
            .setCancelable(false)
            .setPositiveButton("Aceptar") { dialog, _ ->

                // What to do when clicking "Aceptar"
                dialog.dismiss()

                val intent = Intent(this, MainActivity::class.java)
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