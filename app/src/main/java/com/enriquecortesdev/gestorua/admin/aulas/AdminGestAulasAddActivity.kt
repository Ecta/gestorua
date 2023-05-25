package com.enriquecortesdev.gestorua.admin.aulas

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

class AdminGestAulasAddActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    // Cloud Firestore instance declaration
    private val db = FirebaseFirestore.getInstance()

    private var nfcAdapter: NfcAdapter? = null
    private var existsNFC: Boolean = false
    private var nfcAlertBuilder: AlertDialog.Builder? = null
    private var nfcAlert: AlertDialog? = null
    private var keyID: String? = null

    private lateinit var aulaCodigo: String
    private lateinit var aulaEdificio: String
    private lateinit var aulaPlanta: String
    private lateinit var aulaPuerta: String
    private lateinit var aulaTipo: String

    private var keyList: Task<QuerySnapshot?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_gest_aulas_add)

        // Get NFC instance
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Checks NFC function of the device
        checkNFC()

        keyList = db.collection("llaves").get()

        // Create NFC tag alert to register a key (will use it later)
        nfcAlertBuilder = AlertDialog.Builder(this)
        nfcAlertBuilder!!.setMessage("Acerca un llavero NFC para crear la llave")
            .setCancelable(false)
            .setNegativeButton("Cancelar") { dialog, _ ->

                // What to do when clicking "Cancelar"
                dialog.dismiss()

            }
        nfcAlert = nfcAlertBuilder!!.create()

        // Behaviour for the "Confirmar" button
        findViewById<Button>(R.id.id_addAulaConfButton).setOnClickListener {

            // Checks that every EditText is filled
            if(checkCampos()){

                // If they are all filled, tries to ADD the professor
                confirmAdd(
                    findViewById<EditText>(R.id.id_addAulaFormCodigo).text.toString(),
                    findViewById<EditText>(R.id.id_addAulaFormEdificio).text.toString(),
                    findViewById<EditText>(R.id.id_addAulaFormPlanta).text.toString(),
                    findViewById<EditText>(R.id.id_addAulaFormPuerta).text.toString(),
                    findViewById<EditText>(R.id.id_addAulaFormTipo).text.toString()
                    )

            }

        }

        // Behaviour for the "Volver" button
        findViewById<Button>(R.id.id_addAulaVolver).setOnClickListener {

            onBackPressed()

        }

    }

    private fun confirmAdd(id: String, edificio: String, planta: String, puerta: String, tipo: String){

        // Alert builder to show the entered text
        val builder = AlertDialog.Builder(this)

        builder.setMessage("¿Crear con estos datos?")
            .setCancelable(false)
            .setPositiveButton("Aceptar") { dialog, _ ->

                // What to do when clicking "Aceptar"
                dialog.dismiss()

                db.collection("aulas").document(id)
                    .get()
                    .addOnSuccessListener {

                        if(it.exists()){

                            // What to do when prof already exists
                            Toast.makeText(this, "El aula $id ya existe", Toast.LENGTH_LONG).show()

                        }
                        else{

                            if(existsNFC){

                                // Asign values to the lateinits
                                aulaCodigo = id
                                aulaEdificio = edificio
                                aulaPlanta = planta
                                aulaPuerta = puerta
                                aulaTipo = tipo

                                // Sets up the NFC Callback and enables it
                                val readerCallback: NfcAdapter.ReaderCallback = this
                                val flags = NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
                                nfcAdapter!!.enableReaderMode(this, readerCallback, flags, Bundle())
                                nfcAlert!!.show()
                            }
                            else{
                                Toast.makeText(this, "No se puede registrar un aula sin NFC", Toast.LENGTH_LONG).show()
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

    override fun onTagDiscovered(tag: Tag?) {

        // Register the key to the NFC tag
        val nfcA = NfcA.get(tag)
        //nfcA.connect()
        val keyID = readNFC(nfcA)
        val newKey = db.collection("llaves").document()

        if(keyID != null){
            if(checkKey(keyID)){
                writeNFC(nfcA,  newKey.id)
                generateKey(newKey.id)
            }
            else{
                runOnUiThread {
                    Toast.makeText(this, "La llave pertenece a un aula activa", Toast.LENGTH_LONG).show()
                }
                // Closes the alert dialog
                nfcAlert!!.dismiss()
            }
        }
        else{
            writeNFC(nfcA,  newKey.id)
            generateKey(newKey.id)
        }

        //Disables NFC function
        if(existsNFC){
            nfcAdapter!!.disableReaderMode(this)
        }

        // Closes the alert dialog
        nfcAlert!!.dismiss()

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
        val keyID = String(messageBytes.toByteArray(), Charsets.UTF_8)

        if(keyID.length != 20){
            return null
        }

        return keyID

    }

    private fun checkKey(keyID: String): Boolean {

        val keyListResult = keyList?.result

        if(keyListResult != null){

            if(keyListResult.isEmpty){

                return true

            }
            else{

                keyListResult.forEach{

                    if(it.id == keyID)
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

    private fun generateKey(newKey: String) {

        db.collection("llaves").document(newKey)
            .set(mapOf(
                "asignada" to false,
                "aula" to aulaCodigo,
                "profesor" to ""
            ))
            .addOnSuccessListener {
                Toast.makeText(this, "Llave creada correctamente", Toast.LENGTH_LONG).show()
                generateAula()
                // Closes the alert dialog
                nfcAlert!!.dismiss()

            }
            .addOnFailureListener{
                Toast.makeText(this, "Ha pasado algo creando la llave", Toast.LENGTH_LONG).show()
                // Closes the alert dialog
                nfcAlert!!.dismiss()
            }
    }

    private fun generateAula() {

        // Create the aula
        db.collection("aulas").document(aulaCodigo)
            .set(mapOf(
                "edificio" to aulaEdificio,
                "planta" to aulaPlanta,
                "puerta" to aulaPuerta,
                "tipo" to aulaTipo
            ))
            .addOnSuccessListener {

                // What to do when prof is created
                Toast.makeText(this, "Aula creada correctamente", Toast.LENGTH_LONG).show()

                val intent = Intent(this, AdminGestAulasActivity::class.java)
                startActivity(intent)

            }
            .addOnFailureListener {

                // What to do when something wrong happens
                Toast.makeText(this, "Ha pasado algo con el aula", Toast.LENGTH_LONG).show()
                // Closes the alert dialog
                nfcAlert!!.dismiss()
            }
    }

    private fun writeNFC(nfcA: NfcA, keyID: String) {

        nfcA.connect()

        // Convert the message to ByteArray (UTF-8)
        val messageBytes = keyID.toByteArray(Charsets.UTF_8)

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

    private fun checkCampos(): Boolean {

        // Checks that every EditText is filled
        if (findViewById<EditText>(R.id.id_addAulaFormCodigo).text.isBlank()) {

            if (findViewById<EditText>(R.id.id_addAulaFormEdificio).text.isBlank() || findViewById<EditText>(
                    R.id.id_addAulaFormPlanta).text.isBlank() || findViewById<EditText>(R.id.id_addAulaFormPuerta).text.isBlank() || findViewById<EditText>(R.id.id_addAulaFormTipo).text.isBlank()) {

                // If some EditText is empty, set a message and return false
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_LONG).show()

            } else {

                // If some EditText is empty, set a message and return false
                Toast.makeText(this, "El código del aula es obligatorio", Toast.LENGTH_LONG).show()

            }

            return false

        } else {

            if (findViewById<EditText>(R.id.id_addAulaFormEdificio).text.isBlank()) {

                if (findViewById<EditText>(R.id.id_addAulaFormPlanta).text.isBlank() || findViewById<EditText>(R.id.id_addAulaFormPuerta).text.isBlank() || findViewById<EditText>(R.id.id_addAulaFormTipo).text.isBlank()) {

                    // If some EditText is empty, set a message and return false
                    Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_LONG).show()

                } else {

                    // If some EditText is empty, set a message and return false
                    Toast.makeText(this, "El edificio es obligatorio", Toast.LENGTH_LONG).show()

                }

                return false

            } else {

                if (findViewById<EditText>(R.id.id_addAulaFormPlanta).text.isBlank()) {

                    if (findViewById<EditText>(R.id.id_addAulaFormPuerta).text.isBlank() || findViewById<EditText>(R.id.id_addAulaFormTipo).text.isBlank()) {

                        // If some EditText is empty, set a message and return false
                        Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_LONG).show()

                    } else {

                        // If some EditText is empty, set a message and return false
                        Toast.makeText(this, "La planta es obligatoria", Toast.LENGTH_LONG).show()

                    }

                    return false

                } else {

                    if (findViewById<EditText>(R.id.id_addAulaFormPuerta).text.isBlank()) {

                        if (findViewById<EditText>(R.id.id_addAulaFormTipo).text.isBlank()) {

                            // If some EditText is empty, set a message and return false
                            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_LONG).show()

                        } else {

                            // If some EditText is empty, set a message and return false
                            Toast.makeText(this, "La puerta es obligatoria", Toast.LENGTH_LONG).show()

                        }

                        return false

                    } else {

                        if (findViewById<EditText>(R.id.id_addAulaFormTipo).text.isBlank()) {

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

        builder.setMessage("¿Salir sin crear el aula?")
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