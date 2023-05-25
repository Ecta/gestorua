package com.enriquecortesdev.gestorua.admin.mats

import android.app.AlertDialog
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.NfcA
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.enriquecortesdev.gestorua.R
import com.enriquecortesdev.gestorua.admin.AdminMenuActivity
import com.google.firebase.firestore.FirebaseFirestore

class AdminGestMatsActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    // Cloud Firestore instance declaration
    private val db = FirebaseFirestore.getInstance()

    // NFC Instance and alert declarations
    private var nfcAdapter: NfcAdapter? = null
    private var existsNFC: Boolean = false
    private var nfcAlertBuilder: AlertDialog.Builder? = null
    private var nfcAlert: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_gest_mats)

        // NFC Adapter Instance
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Checks the NFC function of the device
        checkNFC()

        // Create NFC tag alert to scan a key (will use it later)
        nfcAlertBuilder = AlertDialog.Builder(this)
        nfcAlertBuilder!!.setMessage("Acerca la etiqueta NFC del material")
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
        findViewById<Button>(R.id.id_matsAddButton).setOnClickListener {
            val intent = Intent(this, AdminGestMatsAddActivity::class.java)
            startActivity(intent)
        }

        // Behaviour for the "Editar material" Button
        findViewById<Button>(R.id.id_matsEditButton).setOnClickListener {

            if(existsNFC){
                val readerCallback: NfcAdapter.ReaderCallback = this
                val flags = NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
                nfcAdapter!!.enableReaderMode(this, readerCallback, flags, Bundle())
                nfcAlert!!.show()
            }

        }

        // Behaviour for the "Volver" button
        findViewById<Button>(R.id.id_matsVolverButton).setOnClickListener {
            // Returns to the previous Activity
            onBackPressed()
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

        db.collection("materiales").document(text)
            .get()
            .addOnSuccessListener {
                if(it.exists()){
                    Toast.makeText(this, "Material escaneado correctamente", Toast.LENGTH_LONG).show()
                    nfcAlert!!.dismiss()

                    val intent = Intent(this, AdminGestMatsEditActivity::class.java)
                    intent.putExtra("id", text)
                    startActivity(intent)
                }
                else{
                    Toast.makeText(this, "No se ha encontrado el material", Toast.LENGTH_LONG).show()
                    nfcAlert!!.dismiss()
                }
            }
            .addOnFailureListener {
                nfcAlert!!.dismiss()
                Toast.makeText(this, "No se ha reconocido la etiqueta NFC", Toast.LENGTH_LONG).show()
            }

        // Pauses the listening only if nfcAdapter exists
        if(existsNFC){
            nfcAdapter!!.disableReaderMode(this)
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

    override fun onPause() {
        super.onPause()
        // Pauses the listening only if nfcAdapter exists
        if(existsNFC){
            nfcAdapter!!.disableReaderMode(this)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        // Returns to the previous Activity
        val intent = Intent(this, AdminMenuActivity::class.java)
        startActivity(intent)
    }

}