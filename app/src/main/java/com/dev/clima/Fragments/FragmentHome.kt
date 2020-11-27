package com.dev.clima.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.clima.Activities.*
import com.dev.clima.Adapters.AdapterArticles
import com.dev.clima.Adapters.AdapterVideoPic
import com.dev.clima.DataClasses.ArticlesDataClass
import com.dev.clima.DataClasses.VideoPicDataClass
import com.dev.clima.R
import com.dev.clima.Utilities.MyAnimations
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_home.view.*
import java.util.*

class FragmentHome : Fragment() {

    var firebaseFirestore: FirebaseFirestore? = null

    var isRotate: Boolean? = false

    var adapter: AdapterVideoPic? = null
    var adapterArticles: AdapterArticles? = null
    var gridLayoutManager: GridLayoutManager? = null
    var linearLayoutManager: LinearLayoutManager? = null

    private lateinit var mInterstitialAd: InterstitialAd

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View =  inflater.inflate(R.layout.fragment_home, container, false)

        MyAnimations.init(view.newArticleLayout)
        MyAnimations.init(view.newMediaLayout)

        mInterstitialAd = InterstitialAd(context)
        mInterstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"
        mInterstitialAd.loadAd(AdRequest.Builder().build())

        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                mInterstitialAd.loadAd(AdRequest.Builder().build())
                val intent: Intent = Intent(context, ActivityAllVideos::class.java)
                context?.startActivity(intent)
            }
        }

        var recyclerView: RecyclerView? = view.findViewById(R.id.mediaRecyclerView)
        var articlesRecyclerView: RecyclerView = view.findViewById(R.id.articlesRecyclerView)
        firebaseFirestore = FirebaseFirestore.getInstance()
        gridLayoutManager = GridLayoutManager(requireActivity().applicationContext, 2)
        linearLayoutManager = LinearLayoutManager(requireActivity().applicationContext)
        recyclerView?.layoutManager = gridLayoutManager
        articlesRecyclerView.layoutManager = linearLayoutManager

        val mediaList: MutableList<VideoPicDataClass>  = ArrayList<VideoPicDataClass>()
        val articleList: MutableList<ArticlesDataClass>  = ArrayList<ArticlesDataClass>()

        val showAllMedia: LinearLayout = view.findViewById(R.id.showMoreMedia)
        val showAllArticles: LinearLayout = view.findViewById(R.id.showMoreArticles)

        adapter = activity?.let { AdapterVideoPic(it, mediaList) }
        recyclerView?.adapter = adapter

        adapterArticles = activity?.let { AdapterArticles(it, articleList) }
        articlesRecyclerView.adapter = adapterArticles

        getVidPicdata(firebaseFirestore!!, adapter, mediaList)
        getArticles(firebaseFirestore!!, adapterArticles, articleList)

        showAllMedia.setOnClickListener {

//            if (mInterstitialAd.isLoaded) {
//                mInterstitialAd.show()
//                Log.d("AD_TAG", "The interstitial Ad has loaded")
//
//            } else {
//                Log.d("AD_TAG", "The interstitial wasn't loaded yet.")
//
//
//            }

            val intent: Intent = Intent(context, ActivityAllVideos::class.java)
            context?.startActivity(intent)
        }

        showAllArticles.setOnClickListener {
            val intent: Intent = Intent(context, ActivityAllArticles::class.java)
            context?.startActivity(intent)
        }

        view.fab.setOnClickListener {
            isRotate = MyAnimations.rotateFab(it, !isRotate!!)
            if(isRotate!!){
                MyAnimations.showIn(view.newArticleLayout)
                MyAnimations.showIn(view.newMediaLayout)
            }else{
                MyAnimations.showOut(view.newArticleLayout)
                MyAnimations.showOut(view.newMediaLayout)
            }
        }

        view.fabArticle.setOnClickListener {
            var intent: Intent = Intent(context, ActivityAddArticle::class.java)
            context?.startActivity(intent)
        }
        view.newArticleLayout.setOnClickListener {
            var intent: Intent = Intent(context, ActivityAddArticle::class.java)
            context?.startActivity(intent)
        }

        view.fabMedia.setOnClickListener {
            var intent: Intent = Intent(context, ActivityAddMedia::class.java)
            context?.startActivity(intent)
        }
        view.newMediaLayout.setOnClickListener {
            var intent: Intent = Intent(context, ActivityAddMedia::class.java)
            context?.startActivity(intent)
        }

        return view
    }

    private fun getArticles(
        firebaseFirestore: FirebaseFirestore,
        adapterArticles: AdapterArticles?,
        articleList: MutableList<ArticlesDataClass>
    ) {
        firebaseFirestore.collection("articles")
            .orderBy("title", Query.Direction.ASCENDING)
            .limitToLast(10)
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
                        context,
                        "Error Getting info:" + it.exception?.message.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("article_ERROR_TAG", it.exception?.message.toString())

                }
            }
    }

    private fun getVidPicdata(
        firebaseFirestore: FirebaseFirestore,
        adapter: AdapterVideoPic?,
        mediaList: MutableList<VideoPicDataClass>
    ) {

            firebaseFirestore.collection("videos and pictures")
                .whereEqualTo("featured", true)
                .limitToLast(4)
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

                        adapter!!.notifyDataSetChanged()
                    }
                    else{

                        Toast.makeText(
                            context,
                            "Error Getting info:" + it.exception?.message.toString(),
                            Toast.LENGTH_LONG
                        ).show()
                        Log.e("INDEX_ERROR_TAG", it.exception?.message.toString())

                    }
            }

    }

    //******************************************  END ********************************************//
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolBar()
    }

    private fun initToolBar() {
        MainActivity().toggle?.isDrawerIndicatorEnabled = true
        (activity as MainActivity?)!!.setActionBarTitle(getString(R.string.dashboard))
        (activity as MainActivity?)!!.supportActionBar!!.setDisplayShowCustomEnabled(true)
    }
}