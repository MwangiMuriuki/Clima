package com.dev.clima.Fragments

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.dev.clima.Activities.ActivityAllArticles
import com.dev.clima.Activities.ActivityMyScans
import com.dev.clima.Activities.MainActivity
import com.dev.clima.DataClasses.ScannedPlasticsDataClass
import com.dev.clima.DataClasses.UserDetailsDataClass
import com.dev.clima.R
import com.dev.clima.Utilities.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_scanner.*
import kotlinx.android.synthetic.main.fragment_scanner.view.*

class FragmentScanner : Fragment() {

    private lateinit var codeScanner: CodeScanner
    val CAMERA_CODE: Int = 1001
    val IMAGE_CODE: Int = 101
    var capturedBarcode: String? = null

    var mAuth: FirebaseAuth? = null
    var myFirestore = FirebaseFirestore.getInstance()

    var currentUser: String? = null
    var preferenceManager: PreferenceManager? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val vw: View =  inflater.inflate(R.layout.fragment_scanner, container, false)

        setupPermissions()

        mAuth = FirebaseAuth.getInstance()
        preferenceManager = PreferenceManager(requireContext())

        val scannerView = vw.findViewById<CodeScannerView>(R.id.scanner_view)

        codeScanner = CodeScanner(requireContext(), scannerView)

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
            activity?.runOnUiThread {
                Toast.makeText(activity, it.text, Toast.LENGTH_LONG).show()
                scannedData.text = it.text
                capturedBarcode = it.text
            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            activity?.runOnUiThread {
                Toast.makeText(activity, it.message, Toast.LENGTH_LONG).show()
               // scannedData.text = it.message
            }
        }

        scannerView.setOnClickListener {
            scannedData.text = ""
            codeScanner.startPreview()
        }

        vw.btnSaveScan.setOnClickListener {
            saveScannedItem(capturedBarcode)
        }

        return vw
    }

    private fun saveScannedItem(capturedBarcode: String?) {
        if (capturedBarcode.isNullOrEmpty()){
            Toast.makeText(activity, "Please Scan an item to proceed", Toast.LENGTH_LONG).show()
        }else{
            Toast.makeText(activity, capturedBarcode, Toast.LENGTH_LONG).show()

            currentUser = preferenceManager?.getFullName()
            val modelClassScanned = ScannedPlasticsDataClass(capturedBarcode)

            myFirestore.collection("Scanned Plastics").document(currentUser!!).collection("Barcodes").document(capturedBarcode).set(modelClassScanned)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        updateUI()
                        Toast.makeText(
                            context,
                            "Scan saved.",
                            Toast.LENGTH_LONG
                        ).show()

                    } else {
                        Toast.makeText(
                            context,
                            "Failed to save the scan. Please try again Later. ",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }.addOnFailureListener {
                    var error: String? = it.toString()

                    Toast.makeText(
                        context,
                        error,
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    private fun updateUI() {
        val intent: Intent? = Intent(context, ActivityMyScans::class.java)
        context?.startActivity(intent)

    }

    private fun setupPermissions() {
        val requestPermissionLauncher = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)

        if (requestPermissionLauncher != PackageManager.PERMISSION_GRANTED){
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.CAMERA), CAMERA_CODE )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode){

            CAMERA_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    scannedData.text = "You need the camera to scan the Barcodes. Restart the app to grant permission."
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolBar()
    }

    private fun initToolBar() {
        MainActivity().toggle?.isDrawerIndicatorEnabled = true
        (activity as MainActivity?)!!.setActionBarTitle(getString(R.string.scanner))
        (activity as MainActivity?)!!.supportActionBar!!.setDisplayShowCustomEnabled(true)
    }
}