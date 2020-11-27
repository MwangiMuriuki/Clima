package com.dev.clima.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.clima.Adapters.AdapterArticles
import com.dev.clima.DataClasses.ArticlesDataClass
import com.dev.clima.DataClasses.VideoPicDataClass
import com.dev.clima.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_all_articles.*
import kotlinx.android.synthetic.main.activity_single_article.*
import java.util.ArrayList

class ActivityAllArticles : AppCompatActivity() {

    var firebaseFirestore: FirebaseFirestore? = null
    var adapterArticles: AdapterArticles? = null
    var linearLayoutManager: LinearLayoutManager? = null
    var searchView: SearchView? = null
    var articleList: MutableList<ArticlesDataClass>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_articles)
        setSupportActionBar(allArticlesToolBar)
        allArticlesToolBar.setNavigationIcon(R.drawable.ic_back)
        title = "Back"

        val articlesRecyclerView: RecyclerView = findViewById(R.id.allArticlesRecyclerView)

        val floatingActionButton: FloatingActionButton? = findViewById(R.id.fab)

        MobileAds.initialize(this@ActivityAllArticles)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        floatingActionButton?.setOnClickListener {
            val intent: Intent? = Intent(applicationContext, ActivityAddArticle::class.java)
            startActivity(intent)
        }

        firebaseFirestore = FirebaseFirestore.getInstance()
        linearLayoutManager = LinearLayoutManager(this@ActivityAllArticles.applicationContext)
        articlesRecyclerView.layoutManager = linearLayoutManager

        articleList = ArrayList<ArticlesDataClass>()
        adapterArticles =  AdapterArticles(this, articleList!!)
        articlesRecyclerView.adapter = adapterArticles

        getAllArticles(firebaseFirestore!!, adapterArticles!!, articleList!!)
    }

    private fun getAllArticles(
        firebaseFirestore: FirebaseFirestore,
        adapterArticles: AdapterArticles,
        articleList: MutableList<ArticlesDataClass>
    ) {

        firebaseFirestore.collection("articles")
            .orderBy("title", Query.Direction.ASCENDING)
            .get()
            .addOnCompleteListener {

                if (it.isSuccessful){
                    for (documentSnapshot in it.result!!) {

                        /*song_Title, song_Artist, */
                        val myArticleList = ArticlesDataClass(
                            documentSnapshot.getString("title"),
                            documentSnapshot.getString("image"),
                            documentSnapshot.getString("source"),
                            documentSnapshot.getString("content")
                        )
                        articleList.add(myArticleList)
                    }

                    adapterArticles!!.notifyDataSetChanged()
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
            val intent: Intent? = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        else if(item.itemId == R.id.action_search){

            searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(s: String): Boolean {
                    return false
                }

                override fun onQueryTextChange(s: String): Boolean {
                    val filteredList: MutableList<ArticlesDataClass> =
                        ArrayList<ArticlesDataClass>()
                    for (searchList in articleList!!) {
                        val searchArticleTitle: String = searchList.title!!
                        val searchArticleSource: String = searchList.source!!
                        if (searchArticleTitle.toLowerCase().contains(s.toLowerCase())
                            || searchArticleSource.toLowerCase().contains(s.toLowerCase())
                        ) {
                            filteredList.add(searchList)
                        }
                    }
                    adapterArticles?.filterList(filteredList)
                    return true
                }
            })
        }
        return super.onOptionsItemSelected(item)
    }
}