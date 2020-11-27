package com.dev.clima.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.clima.Adapters.AdapterVideoPic
import com.dev.clima.DataClasses.VideoPicDataClass
import com.dev.clima.R
import com.google.android.gms.ads.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_all_articles.*
import kotlinx.android.synthetic.main.activity_all_videos.*
import kotlinx.android.synthetic.main.activity_single_article.*
import java.util.*


class ActivityAllVideos : AppCompatActivity() {

    var firebaseFirestore: FirebaseFirestore? = null
    var adapterVideos: AdapterVideoPic? = null
    var gridLayoutManager: GridLayoutManager? = null
    var searchView: SearchView? = null
    var mediaList: MutableList<VideoPicDataClass>? = null

    private lateinit var mInterstitialAd: InterstitialAd


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_videos)
        setSupportActionBar(allVideosToolBar)
        allVideosToolBar.setNavigationIcon(R.drawable.ic_back)
        title = "Back"

        val vidPicRecyclerView: RecyclerView = findViewById(R.id.allVideosRecyclerView)

        val floatingActionButton: FloatingActionButton? = findViewById(R.id.fab)

        MobileAds.initialize(this@ActivityAllVideos)
        val adRequest = AdRequest.Builder().build()
        adViewMedia.loadAd(adRequest)

        mInterstitialAd = InterstitialAd(this@ActivityAllVideos)
        mInterstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"
        mInterstitialAd.loadAd(AdRequest.Builder().build())

        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                mInterstitialAd.loadAd(AdRequest.Builder().build())
                val intent: Intent = Intent(this@ActivityAllVideos, MainActivity::class.java)
                startActivity(intent)
            }
        }

        floatingActionButton?.setOnClickListener {
            val intent: Intent? = Intent(applicationContext, ActivityAddMedia::class.java)
            startActivity(intent)
        }

        firebaseFirestore = FirebaseFirestore.getInstance()
        gridLayoutManager = GridLayoutManager(applicationContext, 2)
        vidPicRecyclerView.layoutManager = gridLayoutManager

        mediaList = ArrayList<VideoPicDataClass>()
        adapterVideos =  AdapterVideoPic(this, mediaList!!)
        vidPicRecyclerView.adapter = adapterVideos

        getAllVideos(firebaseFirestore!!, adapterVideos!!, mediaList!!)
    }

    private fun getAllVideos(
        firebaseFirestore: FirebaseFirestore,
        adapterVideos: AdapterVideoPic,
        mediaList: MutableList<VideoPicDataClass>
    ) {
        firebaseFirestore.collection("videos and pictures")
            .orderBy("title", Query.Direction.ASCENDING)
            .get()
            .addOnCompleteListener {

                if (it.isSuccessful){
                    for (documentSnapshot in it.result!!) {

                        val myMediaList = VideoPicDataClass(
                            documentSnapshot.getString("title"),
                            documentSnapshot.getString("thumbnail"),
                            documentSnapshot.getBoolean("featured"),
                            documentSnapshot.getBoolean("video"),
                            documentSnapshot.getString("source")

                        )
                        mediaList.add(myMediaList)
                    }

                    adapterVideos.notifyDataSetChanged()
                }
                else{

                    Toast.makeText(
                        this,
                        "Error Getting info:" + it.exception?.message.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("INDEX_ERROR_TAG", it.exception?.message.toString())

                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main, menu)

        val item = menu.findItem(R.id.action_search)
        searchView = SearchView((this).supportActionBar!!.themedContext)
        val searchPlate: EditText = searchView!!.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchPlate.hint = "Search Videos & Pictures..."
        searchPlate.setHintTextColor(ContextCompat.getColor(applicationContext, R.color.colorGrey))
        searchPlate.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.colorWhite))
        searchPlate.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimaryDark))
        MenuItemCompat.setShowAsAction(
            item,
            MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW or MenuItemCompat.SHOW_AS_ACTION_IF_ROOM
        )
        MenuItemCompat.setActionView(item, searchView)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {

            if (mInterstitialAd.isLoaded) {
                mInterstitialAd.show()
                Log.d("AD_TAG", "The interstitial Ad has loaded")

            } else {
                Log.d("AD_TAG", "The interstitial wasn't loaded yet.")
                val intent: Intent? = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        else if(item.itemId == R.id.action_search){

            searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(s: String): Boolean {
                    return false
                }

                override fun onQueryTextChange(s: String): Boolean {
                    val filteredList: MutableList<VideoPicDataClass> =
                            ArrayList<VideoPicDataClass>()
                    for (searchList in mediaList!!) {
                        val searchMediaTitle: String = searchList.title!!
                        val searchMediaSource: String = searchList.source!!
                        if (searchMediaTitle.toLowerCase().contains(s.toLowerCase())
                                || searchMediaSource.toLowerCase().contains(s.toLowerCase())
                        ) {
                            filteredList.add(searchList)
                        }
                    }
                    adapterVideos?.filterList(filteredList)
                    return true
                }
            })

        }

        return super.onOptionsItemSelected(item)
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle item selection
//        return when (item.itemId) {
//            R.id.home -> {
//                val intent: Intent = Intent(this, MainActivity::class.java)
//                startActivity(intent)
//                finish()
//                true
//            }
//            R.id.action_search -> {
//
//                searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//                    override fun onQueryTextSubmit(s: String): Boolean {
//                        return false
//                    }
//
//                    override fun onQueryTextChange(s: String): Boolean {
//                        val filteredList: MutableList<VideoPicDataClass> =
//                            ArrayList<VideoPicDataClass>()
//                        for (searchList in mediaList!!) {
//                            val searchMediaTitle: String = searchList.title!!
//                            val searchMediaSource: String = searchList.source!!
//                            if (searchMediaTitle.toLowerCase().contains(s.toLowerCase())
//                                || searchMediaSource.toLowerCase().contains(s.toLowerCase())
//                            ) {
//                                filteredList.add(searchList)
//                            }
//                        }
//                        adapterVideos?.filterList(filteredList)
//                        return true
//                    }
//                })
//
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
}