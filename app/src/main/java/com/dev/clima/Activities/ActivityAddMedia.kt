package com.dev.clima.Activities

import android.app.AlertDialog
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dev.clima.DataClasses.ArticlesDataClass
import com.dev.clima.DataClasses.VideoPicDataClass
import com.dev.clima.R
import com.dev.clima.Utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_add_article.*
import kotlinx.android.synthetic.main.activity_add_media.*


class ActivityAddMedia : AppCompatActivity() {

    val PICK_VIDEO_FILE = 1000

    var alertDialog: AlertDialog? = null

    var resultUri: Uri? = null
    var videoUrl: Uri? = null
    var myFirestore = FirebaseFirestore.getInstance()
    var storageReference: StorageReference? = null
    var myUploadTask: UploadTask? = null

    var display_picture: String? = null
    var downloadUri: String? = null
    var currentUser: String? = null
    var isVideo: Boolean? = false

    var preferenceManager: PreferenceManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_media)
        setSupportActionBar(addMediaToolBar)
        addMediaToolBar.setNavigationIcon(R.drawable.ic_back)
        title = "Back"

        storageReference = FirebaseStorage.getInstance().reference
        preferenceManager = PreferenceManager(applicationContext)

        alertDialog = SpotsDialog(this, R.style.addMediaAlert)

        selectImage.setOnClickListener {
            CropImage.activity()
                .setAspectRatio(16, 9)
                .start(this@ActivityAddMedia)
        }

        selectVideo.setOnClickListener {
            //Start Here
            mthdPickVideo()
        }

        btnUpload.setOnClickListener {
            validateFields()
        }
    }

    private fun mthdPickVideo() {

        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_VIDEO_FILE)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(resultData)
            if (resultCode == RESULT_OK) {
                resultUri = result.uri
                thumbnailImageView.setImageURI(resultUri)
                downloadUri = resultUri.toString()
                isVideo = false
                Toast.makeText(this@ActivityAddMedia, downloadUri, Toast.LENGTH_LONG).show()
            }
        }
        else if (requestCode == PICK_VIDEO_FILE){
            if (resultCode == RESULT_OK){
                resultUri = resultData?.data

                Glide.with(applicationContext)
                        .load(resultUri)
                        .into(thumbnailImageView)

                downloadUri = resultUri.toString()
                isVideo = true

            }
        }
    }

    private fun validateFields() {
        val postTitle: String? = postTitleTV.text.toString()
        val postThumbnail: String? = downloadUri
        currentUser = preferenceManager?.getFullName()

        if(postThumbnail.isNullOrEmpty()){
            Toast.makeText(
                    this@ActivityAddMedia,
                    " Please select a feture image for your article ",
                    Toast.LENGTH_LONG
            ).show()
        }
        else if(postTitle.isNullOrEmpty()){
            Toast.makeText(
                    this@ActivityAddMedia,
                    " Please enter a title for your article",
                    Toast.LENGTH_LONG
            ).show()
        }
        else{
            alertDialog!!.setCancelable(false)
            alertDialog!!.show()
            createNewPost(postTitle, postThumbnail, currentUser)
        }
    }

    private fun createNewPost(postTitle: String, postThumbnail: String, currentUser: String?) {
        var source: String? = currentUser
        var isFeatured: Boolean? = false
        val mStorageRef = storageReference!!.child("media_thumbnails").child(postTitle)
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
                val dataClassPosts = VideoPicDataClass(
                        postTitle,
                        display_picture,
                        isFeatured,
                        isVideo,
                        source
                )
                myFirestore.collection("videos and pictures").document(postTitle).set(dataClassPosts)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                alertDialog!!.cancel()
                                updateUI()
                                Toast.makeText(
                                        this@ActivityAddMedia,
                                        "Posted added Successfully!!!",
                                        Toast.LENGTH_LONG
                                ).show()
                            } else {
                                alertDialog!!.cancel()
                                Toast.makeText(
                                        applicationContext,
                                        "Error when posting article. Please try again Later. ",
                                        Toast.LENGTH_LONG
                                ).show()
                            }
                        }.addOnFailureListener {
                            var error: String? = task.exception.toString()
                        alertDialog!!.cancel()

                            Toast.makeText(
                                    applicationContext,
                                    error,
                                    Toast.LENGTH_LONG
                            ).show()
                        }
            }
            else {
                alertDialog!!.cancel()
                Toast.makeText(
                        applicationContext,
                        "Error saving your post. Please try again Later. ",
                        Toast.LENGTH_LONG
                ).show()
            }
        }

    }

    private fun updateUI() {
        val intent: Intent? = Intent(applicationContext, ActivityAllVideos::class.java)
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