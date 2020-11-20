package com.dev.clima.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.clima.Adapters.AdapterScannedItems
import com.dev.clima.DataClasses.ScannedPlasticsDataClass
import com.dev.clima.R
import com.dev.clima.Utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_my_scans.*
import java.util.ArrayList

class ActivityMyScans : AppCompatActivity() {

    var firebaseFirestore: FirebaseFirestore? = null
    var adapterScans: AdapterScannedItems? = null
    var linearLayoutManager: LinearLayoutManager? = null

    var currentUser: String? = null
    var preferenceManager: PreferenceManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_scans)
        setSupportActionBar(saveScanToolBar)
        saveScanToolBar.setNavigationIcon(R.drawable.ic_back)
        title = "Back"

        preferenceManager = PreferenceManager(applicationContext)

        firebaseFirestore = FirebaseFirestore.getInstance()
        linearLayoutManager = LinearLayoutManager(this@ActivityMyScans.applicationContext)
        scansRV.layoutManager = linearLayoutManager

        val scannedList: MutableList<ScannedPlasticsDataClass>  = ArrayList<ScannedPlasticsDataClass>()
        adapterScans = AdapterScannedItems(this, scannedList)
        scansRV.adapter = adapterScans

        btnNewScan.setOnClickListener {
            val intent: Intent = Intent(this, ActivityScanner::class.java)
            startActivity(intent)
            finish()
        }

        getAllScans(firebaseFirestore!!, adapterScans!!, scannedList)

    }

    private fun getAllScans(
        firebaseFirestore: FirebaseFirestore,
        adapterScans: AdapterScannedItems,
        scannedList: MutableList<ScannedPlasticsDataClass>
    ) {
        currentUser = preferenceManager?.getFullName()

        firebaseFirestore.collection("Scanned Plastics")
            .document(currentUser!!)
            .collection("Barcodes")
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful){
                    val itemNumber: String? = it.result!!.size().toString()
                    totalScans.text = itemNumber
                    val creditVal: Double? = 0.25
                    val prod = it.result!!.size() * creditVal!!
                    myCreditBalance.text = prod.toString()

                    for (documentSnapshot in it.result!!) {

                        val myList = ScannedPlasticsDataClass(
                            documentSnapshot.getString("barCode")
                        )
                        scannedList.add(myList)
                    }

                    adapterScans.notifyDataSetChanged()
                }
                else{

                    Toast.makeText(
                        this,
                        "Error Getting info:" + it.exception?.message.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("article_ERROR_TAG", it.exception?.message.toString())
                }
            }

    }
}