package com.enriquecortesdev.gestorua.main

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.NfcA
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.enriquecortesdev.gestorua.R
import com.enriquecortesdev.gestorua.admin.AdminLoginActivity
import com.enriquecortesdev.gestorua.entry.NewEntryActivity
import com.enriquecortesdev.gestorua.entry.ReturnEntryActivity
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    // Cloud Firestore instance declaration
    private val db = FirebaseFirestore.getInstance()

    private var nfcAdapter: NfcAdapter? = null
    private var existsNFC: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Behaviour for the "Acceder mediante TIU" button
        /*findViewById<Button>(R.id.tiu_button).setOnClickListener {
            Toast.makeText(this, "Acceso mediante cámara", Toast.LENGTH_LONG).show()
        }*/

        // Behaviour for the "Acceder de forma manual" button
        findViewById<Button>(R.id.manual_button).setOnClickListener {
            val intent = Intent(this, NewEntryActivity::class.java)
            startActivity(intent)
        }

        // Behaviour for the "Panel de administrador" button
        findViewById<Button>(R.id.admin_panel_button).setOnClickListener {

            val intent = Intent(this, AdminLoginActivity::class.java)
            startActivity(intent)

        }

    }

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

        if(text.length != 20){
            runOnUiThread {
                Toast.makeText(this, "No se ha reconocido la etiqueta", Toast.LENGTH_LONG).show()
            }
        }
        else{
            db.collection("llaves").document(text)
                .get()
                .addOnSuccessListener {
                    if(it.exists()){
                        db.collection("registro").whereEqualTo("activo", true).whereEqualTo("aula", it.get("aula").toString()).limit(1)
                            .get()
                            .addOnSuccessListener {  llaves ->
                                if(!llaves.isEmpty){
                                    val intent = Intent(this, ReturnEntryActivity::class.java)
                                    intent.putExtra("idKey", it.id)
                                    intent.putExtra("idRegistro", llaves.documents[0].id)
                                    startActivity(intent)
                                }
                                else{
                                    val intent = Intent(this, NewEntryActivity::class.java)
                                    intent.putExtra("idAula", it.get("aula").toString())
                                    startActivity(intent)
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "No se pueden buscar registros", Toast.LENGTH_LONG).show()
                            }
                    }
                    else{
                        Toast.makeText(this, "La llave no está registrada", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "No se ha podido consultar la llave", Toast.LENGTH_LONG).show()
                }
        }
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

    // Overrides onPause function to disable the NFC Adapter when pausing the app
    override fun onPause() {
        super.onPause()
        // Pauses the listening only if nfcAdapter exists
        if(existsNFC){
            nfcAdapter!!.disableReaderMode(this)
        }
    }

}