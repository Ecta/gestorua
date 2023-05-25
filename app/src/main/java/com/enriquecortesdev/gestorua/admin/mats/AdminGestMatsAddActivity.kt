package com.enriquecortesdev.gestorua.admin.mats

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
import com.enriquecortesdev.gestorua.R
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class AdminGestMatsAddActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    // Cloud Firestore instance declaration
    private val db = FirebaseFirestore.getInstance()

    // NFC Instance and alert declarations
    private var nfcAdapter: NfcAdapter? = null
    private var existsNFC: Boolean = false
    private var nfcAlertBuilder: AlertDialog.Builder? = null
    private var nfcAlert: AlertDialog? = null

    private var materialesList: Task<QuerySnapshot?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_gest_mats_add)

        // NFC Adapter Instance
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Checks the NFC function of the device
        checkNFC()

        materialesList = db.collection("materiales").get()

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

        // Behaviour for the "Añadir material" Button
        findViewById<Button>(R.id.id_addMatsConfButton).setOnClickListener {

            if(findViewById<EditText>(R.id.id_addMatsFormTipo).text.isBlank()){
                Toast.makeText(this, "El tipo es un campo obligatorio", Toast.LENGTH_LONG).show()
            }
            else{
                if(existsNFC){

                    // Sets up the NFC Callback and enables it
                    val readerCallback: NfcAdapter.ReaderCallback = this
                    val flags = NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
                    nfcAdapter!!.enableReaderMode(this, readerCallback, flags, Bundle())
                    nfcAlert!!.show()
                }
                else{
                    Toast.makeText(this, "No se puede registrar un material sin NFC", Toast.LENGTH_LONG).show()
                }
            }

        }

        // Behaviour for the "Volver" button
        findViewById<Button>(R.id.id_addMatsVolver).setOnClickListener {
            // Returns to the previous Activity
            onBackPressed()

        }

    }

    override fun onTagDiscovered(tag: Tag?) {

        // Register the mat to the NFC tag
        val nfcA = NfcA.get(tag)

        val matID = readNFC(nfcA)
        val newMat = db.collection("materiales").document()

        if(matID != null){
            if(checkMat(matID)){
                writeNFC(nfcA,  newMat.id)
                registerMat(newMat.id)
            }
            else{
                runOnUiThread {
                    Toast.makeText(this, "La etiqueta pertenece a un material activo", Toast.LENGTH_LONG).show()
                }
                // Closes the alert dialog
                nfcAlert!!.dismiss()
            }
        }
        else{
            writeNFC(nfcA,  newMat.id)
            registerMat(newMat.id)
        }

        //Disables NFC function
        if(existsNFC){
            nfcAdapter!!.disableReaderMode(this)
        }

        // Closes the alert dialog
        nfcAlert!!.dismiss()
    }

    private fun registerMat(id: String) {

        val aula = findViewById<EditText>(R.id.id_addMatsFormAula).text.toString()
        val tipo = findViewById<EditText>(R.id.id_addMatsFormTipo).text.toString()

        if(aula.isBlank()){
            db.collection("materiales").document(id)
                .set(mapOf(
                    "actual" to "",
                    "asignado" to false,
                    "aula" to "global",
                    "disponible" to true,
                    "tipo" to tipo
                ))
                .addOnSuccessListener {
                    Toast.makeText(this, "Material creado correctamente", Toast.LENGTH_LONG).show()
                    // Closes the alert dialog
                    nfcAlert!!.dismiss()
                    onBackPressed()
                }
                .addOnFailureListener{
                    Toast.makeText(this, "Ha pasado algo creando el material", Toast.LENGTH_LONG).show()
                    // Closes the alert dialog
                    nfcAlert!!.dismiss()
                }
        }
        else{
            db.collection("aulas").document(aula)
                .get()
                .addOnSuccessListener {
                    if(it.exists()){
                        db.collection("materiales").document(id)
                            .set(mapOf(
                                "actual" to "",
                                "asignado" to false,
                                "aula" to aula,
                                "disponible" to true,
                                "tipo" to tipo
                            ))
                            .addOnSuccessListener {
                                Toast.makeText(this, "Material creado correctamente", Toast.LENGTH_LONG).show()
                                // Closes the alert dialog
                                nfcAlert!!.dismiss()
                                onBackPressed()
                            }
                            .addOnFailureListener{
                                Toast.makeText(this, "Ha pasado algo creando el material", Toast.LENGTH_LONG).show()
                                // Closes the alert dialog
                                nfcAlert!!.dismiss()
                            }
                        }
                        else{
                        Toast.makeText(this, "El aula indicada no existe", Toast.LENGTH_LONG).show()
                        // Closes the alert dialog
                        nfcAlert!!.dismiss()
                        }
                    }
        }
    }

    private fun writeNFC(nfcA: NfcA, id: String) {

        nfcA.connect()

        // Convert the message to ByteArray (UTF-8)
        val messageBytes = id.toByteArray(Charsets.UTF_8)

        // Starting memory page (1 to 3 are reserved, writing starts in page 4)
        var currentPage = 4

        // Loop to write 4 bytes on every page
        for (offset in messageBytes.indices step 4) {

            // Get bytes for current page
            val pageData = messageBytes.sliceArray(offset until offset + 4)

            // NfcA Writing Command (0xA2 means write)
            val writeCommand = byteArrayOf(0xA2.toByte(), currentPage.toByte()) + pageData

            // Transceive the command to the NFC tag
            nfcA.transceive(writeCommand)

            // Step to next page
            currentPage++
        }

        nfcA.close()

    }

    private fun checkMat(matID: String): Boolean {

        val materialesListResult = materialesList?.result

        if(materialesListResult != null){

            if(materialesListResult.isEmpty){

                return true

            }
            else{

                materialesListResult.forEach{

                    if(it.id == matID)
                    {
                        return false
                    }

                }

                return true

            }

        }
        else{

            return true

        }

    }

    private fun readNFC(nfcA: NfcA): String? {

        nfcA.connect()

        // Initialize the total message variable
        val messageBytes = mutableListOf<Byte>()

        // Loop to read every page (we know the message is 20 char long, so there will be 5 pages with 4 bytes each: 4, 5, 6, 7, 8)
        for (page in 4..8) {

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
        val matID = String(messageBytes.toByteArray(), Charsets.UTF_8)

        if(matID.length != 20){
            return null
        }

        return matID

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
        super.onBackPressed()
        // Returns to the previous Activity
        val intent = Intent(this, AdminGestMatsActivity::class.java)
        startActivity(intent)
    }
}