package com.dev.clima.Activities

import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
        }

        val videoUrl: String? = "https://firebasestorage.googleapis.com/v0/b/clima-959d3.appspot.com/o/media_thumbnails%2F---Peru_8K_HDR_60FPS_(FUHD)_4.mp4?alt=media&token=8097481a-bd4c-4c9c-89db-ac661193601c"
        val uri: Uri? = Uri.parse(videoUrl)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                resultUri = result.uri
                thumbnailImageView.setImageURI(resultUri)
                downloadUri = resultUri.toString()
                Toast.makeText(this@ActivityAddMedia, downloadUri, Toast.LENGTH_LONG).show()
            }
        }
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
}