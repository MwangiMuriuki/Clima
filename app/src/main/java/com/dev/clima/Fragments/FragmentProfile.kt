package com.dev.clima.Fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dev.clima.Activities.MainActivity
import com.dev.clima.Adapters.AdapterArticles
import com.dev.clima.Adapters.AdapterVideoPic
import com.dev.clima.DataClasses.ArticlesDataClass
import com.dev.clima.DataClasses.UserDetailsDataClass
import com.dev.clima.DataClasses.VideoPicDataClass
import com.dev.clima.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.util.ArrayList

class FragmentProfile : Fragment() {

    private var mAuth = FirebaseAuth.getInstance()
    var firebaseFirestore = FirebaseFirestore.getInstance()
    var userLoggedIn = mAuth.currentUser

    val myList: MutableList<UserDetailsDataClass> = ArrayList<UserDetailsDataClass>()

    var adapterMedia: AdapterVideoPic? = null
    var adapterArticles: AdapterArticles? = null
    var gridLayoutManager: GridLayoutManager? = null
    var linearLayoutManager: LinearLayoutManager? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View =  inflater.inflate(R.layout.fragment_profile, container, false)
        mAuth = FirebaseAuth.getInstance()
        userLoggedIn = mAuth.currentUser

        val mediaRecyclerView: RecyclerView? = view.findViewById(R.id.myMediaRV)
        val articlesRecyclerView: RecyclerView = view.findViewById(R.id.myArticlesRV)

        val mediaPlaceholder: LinearLayout = view.findViewById(R.id.mediaPlaceholderLayout)
        val articlesPlaceholder: LinearLayout = view.findViewById(R.id.articlesPlaceholderLayout)

        firebaseFirestore = FirebaseFirestore.getInstance()
        gridLayoutManager = GridLayoutManager(requireActivity().applicationContext, 3)
        linearLayoutManager = LinearLayoutManager(requireActivity().applicationContext)
        mediaRecyclerView?.layoutManager = gridLayoutManager
        articlesRecyclerView.layoutManager = linearLayoutManager

        val mediaList: MutableList<VideoPicDataClass>  = ArrayList<VideoPicDataClass>()
        val articleList: MutableList<ArticlesDataClass>  = ArrayList<ArticlesDataClass>()

        adapterMedia = activity?.let { AdapterVideoPic(it, mediaList) }
        mediaRecyclerView?.adapter = adapterMedia

        adapterArticles = activity?.let { AdapterArticles(it, articleList) }
        articlesRecyclerView.adapter = adapterArticles

        //Fetch user details from FirebaseFirestore
        getUserdetails(userLoggedIn)
        getMyArticles(firebaseFirestore!!, adapterArticles, articleList, articlesRecyclerView, articlesPlaceholder)
        getMyMedia(firebaseFirestore!!, adapterMedia, mediaList, mediaRecyclerView, mediaPlaceholder)
        
        return view
    }

    private fun getUserdetails(userLoggedIn: FirebaseUser?) {

        if (userLoggedIn != null) {
            val userID: String = userLoggedIn!!.uid

            firebaseFirestore.collection("Users").document(userID).get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val documentSnapshot = task.result
                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                val userData = UserDetailsDataClass(
                                        documentSnapshot.getString("full_Name"),
                                        documentSnapshot.getString("email"),
                                        documentSnapshot.getString("user_id"),
                                        documentSnapshot.getString("display_picture")
                                )
                                myList.add(userData)

                                val fname = documentSnapshot.getString("full_Name")
                                val pic = documentSnapshot.getString("display_picture")
                                view?.userFullName?.text = fname

                                if (pic != null) {
                                    val imageUri = Uri.parse(documentSnapshot.getString("display_picture"))
                                    view?.displayPhoto?.let { Glide.with(requireActivity()).load(imageUri).into(it) }
                                }
                            }
                            else {
                                Log.e("SNAPSHOT_ERROR", "User Does not exist")
                            }
                        }
                        else {

                            Log.e("TASK_ERROR", task.exception?.message.toString())
                        }
                    }
                    .addOnFailureListener {
                        Log.e("TASK_ERROR", it.message.toString())

                    }
        }
    }

    private fun getMyMedia(firebaseFirestore: FirebaseFirestore, adapterMedia: AdapterVideoPic?, mediaList: MutableList<VideoPicDataClass>, mediaRecyclerView: RecyclerView?, mediaPlaceholder: LinearLayout) {

        firebaseFirestore.collection("videos and pictures")
                .whereEqualTo("featured", true)
                .limitToLast(6)
                .orderBy("title", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener {

                    if (it.isSuccessful){
                        for (documentSnapshot in it.result!!) {
                            if (documentSnapshot.data.isNotEmpty()){
                                mediaRecyclerView?.visibility = View.VISIBLE
                                mediaPlaceholder.visibility = View.GONE

                                val myMediaList = VideoPicDataClass(
                                        documentSnapshot.getString("title"),
                                        documentSnapshot.getString("thumbnail"),
                                        documentSnapshot.getBoolean("featured"),
                                        documentSnapshot.getBoolean("video"),
                                        documentSnapshot.getString("source")
                                )
                                mediaList.add(myMediaList)
                            }else{
                                mediaRecyclerView?.visibility = View.GONE
                                mediaPlaceholder.visibility = View.VISIBLE
                            }
                        }
                        adapterMedia!!.notifyDataSetChanged()
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

    private fun getMyArticles(firebaseFirestore: FirebaseFirestore, adapterArticles: AdapterArticles?, articleList: MutableList<ArticlesDataClass>, articlesRecyclerView: RecyclerView, articlesPlaceholder: LinearLayout) {
        firebaseFirestore.collection("articles")
                .orderBy("title", Query.Direction.ASCENDING)
                .limitToLast(10)
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        for (documentSnapshot in it.result!!) {
                            if (documentSnapshot.data.isNotEmpty()){
                                articlesRecyclerView.visibility = View.VISIBLE
                                articlesPlaceholder.visibility = View.GONE

                                val myArticleList = ArticlesDataClass(
                                        documentSnapshot.getString("title"),
                                        documentSnapshot.getString("image"),
                                        documentSnapshot.getString("source"),
                                        documentSnapshot.getString("content")
                                )
                                articleList.add(myArticleList)
                            }
                            else{
                                articlesRecyclerView.visibility = View.GONE
                                articlesPlaceholder.visibility = View.VISIBLE
                            }
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolBar()
    }

    private fun initToolBar() {
        MainActivity().toggle?.isDrawerIndicatorEnabled = true
        activity?.title = getString(R.string.profile)
    }

}