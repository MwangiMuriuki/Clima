package com.dev.clima.Activities

import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import kotlinx.android.synthetic.main.activity_add_article.*
import kotlinx.android.synthetic.main.activity_add_media.*


class ActivityAddMedia : AppCompatActivity() {

    val PICK_VIDEO_FILE = 1000

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

        storageReference = FirebaseStorage.getInstance().reference
        preferenceManager = PreferenceManager(applicationContext)

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

//        val videoUrl: String? = "https://firebasestorage.googleapis.com/v0/b/clima-959d3.appspot.com/o/media_thumbnails%2F---Peru_8K_HDR_60FPS_(FUHD)_4.mp4?alt=media&token=8097481a-bd4c-4c9c-89db-ac661193601c"
//        val uri: Uri? = Uri.parse(videoUrl)

//        viewView.setVideoURI(uri)
//
//        var mediaController: MediaController? = MediaController(this@ActivityAddMedia)
//        viewView.setMediaController(mediaController)
//        mediaController?.setAnchorView(viewView)
//
//        retrieveVideoFrameFromVideo(videoUrl)
//
//        val bm = retrieveVideoFrameFromVideo(videoUrl)
//        thumb.setImageBitmap(bm)

    }


    private fun mthdPickVideo() {

//      val intent = Intent(Intent.ACTION_GET_CONTENT).apply{
//              addCategory(Intent.CATEGORY_OPENABLE)
//              type = "video/*"
//      }
//        startActivityForResult(intent, PICK_VIDEO_FILE)

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

                //  val videoUrl = resultUri?.path.toString()
                // val selectedVideoPath = getRealPathFromURI(resultUri!!)
                //val selectedVideoPath = getPath(resultUri)

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
                                updateUI()
                                Toast.makeText(
                                        this@ActivityAddMedia,
                                        "Posted added Successfully!!!",
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

    private fun retrieveVideoFrameFromVideo(videoUrl: String?): Bitmap? {
        var bitmap: Bitmap? = null
        var mediaMetadataRetriever: MediaMetadataRetriever? = null

        try {
            mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(videoUrl, HashMap<String, String>())
            bitmap = mediaMetadataRetriever.frameAtTime
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaMetadataRetriever?.release()
        }
        return bitmap
    }

    private fun getRealPathFromURI(uri: Uri) : String? {
         if (uri == null){
             return null
         }
        val projection: Array<String>? = arrayOf(MediaStore.Images.Media.DATA)

        val cursor: Cursor? = applicationContext.contentResolver.query(uri, projection, null, null, null)

        if (cursor != null) {
            val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(column_index)
        }
        val realPath: String? = uri.path

        return realPath
    }

    fun getPath(uri: Uri?): String? {
        var cursor: Cursor? = null
        return try {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            cursor = contentResolver.query(uri!!, projection, null, null, null)
            val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(column_index)
        } finally {
            cursor?.close()
        }
    }
}