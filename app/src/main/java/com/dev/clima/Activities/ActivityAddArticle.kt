package com.dev.clima.Activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import com.dev.clima.DataClasses.ArticlesDataClass
import com.dev.clima.DataClasses.UserDetailsDataClass
import com.dev.clima.R
import com.dev.clima.Utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_add_article.*
import kotlinx.android.synthetic.main.activity_register.*

class ActivityAddArticle : AppCompatActivity() {
    var resultUri: Uri? = null
    var myFirestore = FirebaseFirestore.getInstance()
    var storageReference: StorageReference? = null
    var myUploadTask: UploadTask? = null

    var display_picture: String? = null
    var downloadUri: String? = null
    var currentUser: String? = null

    var preferenceManager: PreferenceManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_article)
        setSupportActionBar(addArticlesToolBar)
        addArticlesToolBar.setNavigationIcon(R.drawable.ic_back)
        title = "Back"

        storageReference = FirebaseStorage.getInstance().reference

        preferenceManager = PreferenceManager(applicationContext)

        btnPost.setOnClickListener {
            validateFields()
        }

        articleImageView.setOnClickListener {
            CropImage.activity()
                .setAspectRatio(16, 9)
                .start(this@ActivityAddArticle)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                resultUri = result.uri
                articleImageView.setImageURI(resultUri)
                downloadUri = resultUri.toString()
                Toast.makeText(this@ActivityAddArticle, downloadUri, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun validateFields(){
        val articleTitle: String? = articleTitleTV.text.toString()
        val articleContent: String? = articleContentTV.text.toString()
        val articleFeaturedImage: String? = downloadUri
        currentUser = preferenceManager?.getFullName()

        if(downloadUri.isNullOrEmpty()){
            Toast.makeText(
                this@ActivityAddArticle,
                " Please select a feture image for your article ",
                Toast.LENGTH_LONG
            ).show()
        }
        else if(articleTitle.isNullOrEmpty()){
            Toast.makeText(
                this@ActivityAddArticle,
                " Please enter a title for your article",
                Toast.LENGTH_LONG
            ).show()
        }
        else if (articleContent.isNullOrEmpty()){
            Toast.makeText(
                this@ActivityAddArticle,
                " Please enter content for your article",
                Toast.LENGTH_LONG
            ).show()
        }
        else{
            createNewArticle(articleTitle, articleContent, articleFeaturedImage, currentUser)
        }
    }

    private fun createNewArticle(
        articleTitle: String,
        articleContent: String,
        articleFeaturedImage: String?,
        currentUser: String?
    ) {

        var source: String? = currentUser
        val mStorageRef = storageReference!!.child("my_articles_images").child(articleTitle)
        myUploadTask = mStorageRef.putFile(resultUri!!)

        val urlTask = myUploadTask!!.continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception!!
                }
            // Continue with the task to get the download URL
            mStorageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result!!
                display_picture = downloadUri.toString()
                val dataClassArticles = ArticlesDataClass(
                    articleTitle,
                    display_picture,
                    source,
                    articleContent
                )
                myFirestore.collection("articles").document(articleTitle).set(dataClassArticles)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            updateUI()
                            Toast.makeText(
                                this@ActivityAddArticle,
                                "Article Posted Successfully!!!",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Error when posting article. Please try again Later. ",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }.addOnFailureListener {
                        var error: String? = task.exception.toString()

                        Toast.makeText(
                            applicationContext,
                            error,
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            else {
                Toast.makeText(
                    applicationContext,
                    "Error Posting Article. Please try again Later. ",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun updateUI() {
        val intent: Intent? = Intent(applicationContext, ActivityAllArticles::class.java)
        startActivity(intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}