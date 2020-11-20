package com.dev.clima.Activities

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dev.clima.R
import kotlinx.android.synthetic.main.activity_view_media.*


abstract class ActivityViewMedia : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_media)
        setSupportActionBar(viewMediaToolBar)
        viewMediaToolBar.setNavigationIcon(R.drawable.ic_back)
        title = "Back"

        val passedPostThumbnail: String? = intent.getStringExtra("postThumbnail")
        val passedPostTitle: String? = intent.getStringExtra("postTitle")
        val passedPostIsVideo: Boolean? = intent.getBooleanExtra("postIsVideo", true)
        val passedPostSource: String? = intent.getStringExtra("postSource")

        val thumbnailUri: Uri? = Uri.parse(passedPostThumbnail)
        sourceTextView.text = passedPostSource

        if (passedPostIsVideo == true){
            showImage.visibility = View.GONE
            imageTitle.visibility = View.GONE
            showVideo.visibility = View.VISIBLE
            videoTitle.visibility = View.VISIBLE

            videoTitle.text = passedPostTitle
        }
        else{
            showImage.visibility = View.VISIBLE
            imageTitle.visibility = View.VISIBLE
            showVideo.visibility = View.GONE
            videoTitle.visibility = View.GONE

            imageTitle.text = passedPostTitle
        }

        Glide.with(applicationContext).load(thumbnailUri).into(showImage)

        showVideo.setVideoURI(thumbnailUri)
        showVideo.requestFocus()
        showVideo.start()

        var mediaController: MediaController? = MediaController(this@ActivityViewMedia)
        showVideo.setMediaController(mediaController)
        mediaController?.setAnchorView(showVideo)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}