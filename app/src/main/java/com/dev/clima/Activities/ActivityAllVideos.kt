package com.dev.clima.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import android.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.clima.Adapters.AdapterArticles
import com.dev.clima.Adapters.AdapterVideoPic
import com.dev.clima.DataClasses.ArticlesDataClass
import com.dev.clima.DataClasses.VideoPicDataClass
import com.dev.clima.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_all_videos.*
import kotlinx.android.synthetic.main.activity_single_article.*
import java.util.ArrayList

class ActivityAllVideos : AppCompatActivity() {

    var firebaseFirestore: FirebaseFirestore? = null
    var adapterVideos: AdapterVideoPic? = null
    var gridLayoutManager: GridLayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_videos)
        setSupportActionBar(allVideosToolBar)
        allVideosToolBar.setNavigationIcon(R.drawable.ic_back)
        title = "Back"

        val vidPicRecyclerView: RecyclerView = findViewById(R.id.allVideosRecyclerView)

        val floatingActionButton: FloatingActionButton? = findViewById(R.id.fab)

        floatingActionButton?.setOnClickListener {
            val intent: Intent? = Intent(applicationContext, ActivityAddMedia::class.java)
            startActivity(intent)
        }

        firebaseFirestore = FirebaseFirestore.getInstance()
        gridLayoutManager = GridLayoutManager(applicationContext, 2)
        vidPicRecyclerView.layoutManager = gridLayoutManager

        val mediaList: MutableList<VideoPicDataClass>  = ArrayList<VideoPicDataClass>()
        adapterVideos =  AdapterVideoPic(this, mediaList)
        vidPicRecyclerView.adapter = adapterVideos

        getAllVideos(firebaseFirestore!!, adapterVideos!!, mediaList)
    }

    private fun getAllVideos(
        firebaseFirestore: FirebaseFirestore,
        adapterVideos: AdapterVideoPic,
        mediaList: MutableList<VideoPicDataClass>
    ) {
        firebaseFirestore.collection("videos and pictures")
            .whereEqualTo("featured", true)
            .orderBy("title", Query.Direction.ASCENDING)
            .get()
            .addOnCompleteListener {

                if (it.isSuccessful){
                    for (documentSnapshot in it.result!!) {

                        /*song_Title, song_Artist, */
                        val myMediaList = VideoPicDataClass(
                            documentSnapshot.getString("title"),
                            documentSnapshot.getString("thumbnail"),
                            documentSnapshot.getBoolean("featured")
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}