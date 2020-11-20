package com.dev.clima.Activities

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.dev.clima.DataClasses.ScannedPlasticsDataClass
import com.dev.clima.R
import com.dev.clima.Utilities.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_my_scans.*
import kotlinx.android.synthetic.main.activity_scanner.*
import kotlinx.android.synthetic.main.fragment_scanner.*
import kotlinx.android.synthetic.main.fragment_scanner.view.*

class ActivityScanner : AppCompatActivity() {

    private lateinit var codeScanner: CodeScanner
    val CAMERA_CODE: Int = 1001
    val IMAGE_CODE: Int = 101
    var capturedBarcode: String? = null

    var mAuth: FirebaseAuth? = null
    var myFirestore = FirebaseFirestore.getInstance()

    var currentUser: String? = null
    var preferenceManager: PreferenceManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)
        setSupportActionBar(scannerToolBar)
        scannerToolBar.setNavigationIcon(R.drawable.ic_back)
        title = "Back"

        setupPermissions()

        mAuth = FirebaseAuth.getInstance()
        preferenceManager = PreferenceManager(applicationContext)

        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)

        codeScanner = CodeScanner(applicationContext, scannerView)

        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            this.runOnUiThread {
                Toast.makeText(this, it.text, Toast.LENGTH_LONG).show()
                newScannedData.text = it.text
                capturedBarcode = it.text
            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            this?.runOnUiThread {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                // scannedData.text = it.message
            }
        }

        scannerView.setOnClickListener {
            newScannedData.text = ""
            codeScanner.startPreview()
        }

        buttonSaveScan.setOnClickListener {
            saveScannedItem(capturedBarcode)
        }
    }

    private fun saveScannedItem(capturedBarcode: String?) {
        if (capturedBarcode.isNullOrEmpty()){
            Toast.makeText(this, "Please Scan an item to proceed", Toast.LENGTH_LONG).show()
        }else{
            Toast.makeText(this, capturedBarcode, Toast.LENGTH_LONG).show()

            currentUser = preferenceManager?.getFullName()
            val modelClassScanned = ScannedPlasticsDataClass(capturedBarcode)

            myFirestore.collection("Scanned Plastics").document(currentUser!!).collection("Barcodes").document(capturedBarcode).set(modelClassScanned)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        updateUI()
                        Toast.makeText(
                            this,
                            "Scan saved.",
                            Toast.LENGTH_LONG
                        ).show()

                    } else {
                        Toast.makeText(
                            this,
                            "Failed to save the scan. Please try again Later. ",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }.addOnFailureListener {
                    var error: String? = it.toString()

                    Toast.makeText(
                        this,
                        error,
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    private fun updateUI() {
        val intent: Intent? = Intent(this, ActivityMyScans::class.java)
        this.startActivity(intent)

    }

    private fun setupPermissions() {
        val requestPermissionLauncher = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)

        if (requestPermissionLauncher != PackageManager.PERMISSION_GRANTED){
            makeRequest()
        }
    }
    private fun makeRequest() {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), CAMERA_CODE )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode){

            CAMERA_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    newScannedData.text = "You need the camera to scan the Barcodes. Restart the app to grant permission."
                }
                else{
                    //successful
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }
}