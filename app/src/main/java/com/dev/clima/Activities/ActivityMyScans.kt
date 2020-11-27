package com.dev.clima.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
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
    var currentUserID: String? = null
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

    fun getAllScans(
        firebaseFirestore: FirebaseFirestore,
        adapterScans: AdapterScannedItems,
        scannedList: MutableList<ScannedPlasticsDataClass>
    ) {
        currentUser = preferenceManager?.getFullName()
        currentUserID = preferenceManager?.getUserId()

        firebaseFirestore.collection("Scanned Plastics")
            .document(currentUserID!!)
            .collection("Barcodes")
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful){
                    val itemNumber: String? = it.result!!.size().toString()
                    totalScans.text = itemNumber
                    val creditVal: Double? = 0.25
                    val prod = it.result!!.size() * creditVal!!
                    myCreditBalance.text = prod.toString()

                    preferenceManager?.setCreditBalance(prod.toString())
                    preferenceManager?.setTotalScans(itemNumber)

                    if (it.result!!.size() > 0){
                        placeholder.visibility = View.GONE
                        scansRV.visibility = View.VISIBLE
                    }else{
                        placeholder.visibility = View.VISIBLE
                        scansRV.visibility = View.GONE
                    }

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            val intent: Intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}