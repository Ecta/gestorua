package com.enriquecortesdev.gestorua.entry

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.NfcA
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enriquecortesdev.gestorua.R
import com.enriquecortesdev.gestorua.admin.adapter.MaterialAdapter
import com.enriquecortesdev.gestorua.admin.adapter.MaterialReturnAdapter
import com.enriquecortesdev.gestorua.items.Material
import com.enriquecortesdev.gestorua.items.Registro
import com.enriquecortesdev.gestorua.main.MainActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.util.ArrayList

class ReturnEntryActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    // Cloud Firestore instance declaration
    private val db = FirebaseFirestore.getInstance()

    // NFC Instance and alert declarations
    private var nfcAdapter: NfcAdapter? = null
    private var existsNFC: Boolean = false

    // Intent vars initialized on null
    private var idKey: String? = null
    private var idRegistro: String? = null

    // Materiales ArrayList declaration
    private var materialesList = ArrayList<Material>()
    private val materialAdapter = MaterialReturnAdapter(materialesList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_return_entry)

        // NFC Adapter Instance
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Checks the NFC function of the device
        checkNFC()

        // Get IDs from intent
        idRegistro = intent.extras?.getString("idRegistro").toString()
        idKey = intent.extras?.getString("idKey").toString()

        // Sets up the recyclerView and attaches the adapter (with an empty list)
        val manager = LinearLayoutManager(this)
        val decoration = DividerItemDecoration(this, manager.orientation)
        val recyclerView = findViewById<RecyclerView>(R.id.id_returnExtraMatsRecycler)
        recyclerView.layoutManager = manager
        recyclerView.adapter = materialAdapter
        recyclerView.addItemDecoration(decoration)

        // Sets up the initial values
        initialSetup()

        // Behaviour for the "Cancelar" button
        findViewById<Button>(R.id.id_returnEntryConfButton).setOnClickListener {
            confirmReturn()
        }

        // Behaviour for the "Cancelar" button
        findViewById<Button>(R.id.id_returnEntryVolver).setOnClickListener {
            // Returns to the previous Activity
            onBackPressed()
        }

    }

    private fun confirmReturn() {
        // Check if every mat has been scanned
        if(checkLista()){
            // We have all the mats, run a db batch to set "asignado" to false on each one
            db.runBatch { batch ->
                materialesList.forEach{ material ->
                    batch.update(
                        db.collection("materiales").document(material.id), mapOf(
                            "actual" to "",
                            "asignado" to false
                        )
                    )
                }
            }
                .addOnSuccessListener {
                    // All mats are edited, then liberate the key
                    db.collection("llaves").document(idKey!!)
                        .update(mapOf(
                            "asignada" to false,
                            "profesor" to ""
                        ))
                        .addOnSuccessListener {
                            db.collection("registro").document(idRegistro!!)
                                .update("activo", false)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Se ha completado la devolución", Toast.LENGTH_LONG).show()
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Ha ocurrido algo actualizando el registro", Toast.LENGTH_LONG).show()
                                }

                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Ha ocurrido algo editando la llave", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Ha ocurrido algo editando los materiales", Toast.LENGTH_LONG).show()
                }
        }
        else{
            Toast.makeText(this, "Faltan materiales por escanear", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkLista():Boolean {
        materialesList.forEach{
            if(it.asignado){
                return false
            }
        }
        return true
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

        db.collection("materiales").document(text)
            .get()
            .addOnSuccessListener {
                if(it.exists()){
                    // Changes the specified material from the materialesList from assigned to unassigned
                    materialesList.forEach{material ->
                        if(material.id == it.id){
                            material.asignado = false
                        }
                    }
                    // Sends materialesList to the adapter to refresh the recyclerView
                    materialAdapter.notifyDataSetChanged()
                }
                else{
                    Toast.makeText(this, "El material no está registrado", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "No se ha podido identificar el material", Toast.LENGTH_LONG).show()
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initialSetup(){

        if(idRegistro != null){

            db.collection("registro").document(idRegistro.toString())
                .get()
                .addOnSuccessListener {
                    if(it.exists()){

                        // Set up the view values
                        findViewById<EditText>(R.id.id_returnEntryFormAula).setText(it.get("aula").toString())
                        findViewById<EditText>(R.id.id_returnEntryFormDate).setText(it.get("fecha").toString())
                        findViewById<EditText>(R.id.id_returnEntryFormTimeInit).setText(it.get("horaini").toString())
                        findViewById<EditText>(R.id.id_returnEntryFormTimeEnd).setText(it.get("horafin").toString())
                        findViewById<EditText>(R.id.id_returnEntryFormProf).setText(it.get("profesor").toString())

                        // Look for related mats
                        db.collection("materiales").whereEqualTo("asignado", true).whereEqualTo("actual", it.get("aula").toString())
                            .get()
                            .addOnSuccessListener {materiales ->

                                if(!materiales.isEmpty){

                                    for(material in materiales.documents){

                                        val nuevoMaterial = Material(
                                            id = material.id,
                                            aula = material.get("aula") as String,
                                            tipo = material.get("tipo") as String,
                                            disponible = material.get("disponible") as Boolean,
                                            actual = material.get("actual") as String,
                                            asignado = material.get("asignado") as Boolean
                                        )
                                        // Add the created Material to the materialesList ArrayList
                                        materialesList.add(nuevoMaterial)

                                    }

                                    // Sends materialesList to the adapter to fill the recyclerView
                                    materialAdapter.notifyDataSetChanged()

                                }

                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "No se han podido recuperar los materiales", Toast.LENGTH_LONG).show()
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                            }
                    }
                    else{
                        Toast.makeText(this, "El registro no existe", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "No se ha podido recuperar el registro", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }

        }
        else{
            Toast.makeText(this, "No se ha podido recuperar la id del registro", Toast.LENGTH_LONG).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
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

        builder.setMessage("¿Salir sin registrar devolución?")
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

    // Overrides onResume function to enable the NFC Adapter when resuming the app
    override fun onResume() {
        super.onResume()

        // Checks the NFC function of the device and sets up the listening
        checkNFC()

        if(existsNFC){
            val readerCallback: NfcAdapter.ReaderCallback = this
            val flags = NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
            nfcAdapter!!.enableReaderMode(this, readerCallback, flags, Bundle())
        }
    }

    // Overrides onPause function to disable the NFC Adapter when pausing the app
    override fun onPause() {
        super.onPause()
        // Pauses the listening only if nfcAdapter exists
        if(existsNFC){
            nfcAdapter!!.disableReaderMode(this)
        }
    }

}