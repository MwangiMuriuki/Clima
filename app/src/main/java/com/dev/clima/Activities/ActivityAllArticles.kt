package com.dev.clima.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import android.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.clima.Adapters.AdapterArticles
import com.dev.clima.DataClasses.ArticlesDataClass
import com.dev.clima.R
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_articles)
        setSupportActionBar(allArticlesToolBar)
        allArticlesToolBar.setNavigationIcon(R.drawable.ic_back)
        title = "Back"

        val articlesRecyclerView: RecyclerView = findViewById(R.id.allArticlesRecyclerView)

        val floatingActionButton: FloatingActionButton? = findViewById(R.id.fab)

        floatingActionButton?.setOnClickListener {
            val intent: Intent? = Intent(applicationContext, ActivityAddArticle::class.java)
            startActivity(intent)
        }

        firebaseFirestore = FirebaseFirestore.getInstance()
        linearLayoutManager = LinearLayoutManager(this@ActivityAllArticles.applicationContext)
        articlesRecyclerView.layoutManager = linearLayoutManager

        val articleList: MutableList<ArticlesDataClass>  = ArrayList<ArticlesDataClass>()
        adapterArticles =  AdapterArticles(this, articleList)
        articlesRecyclerView.adapter = adapterArticles

        getAllArticles(firebaseFirestore!!, adapterArticles!!, articleList)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}