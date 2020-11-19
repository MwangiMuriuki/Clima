package com.dev.clima.Activities

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dev.clima.R
import com.google.android.material.appbar.CollapsingToolbarLayout
import kotlinx.android.synthetic.main.activity_single_article.*
import kotlinx.android.synthetic.main.content_scrolling.*

class ActivitySingleArticle : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_article)
        setSupportActionBar(findViewById(R.id.toolbar))
        toolbar.setNavigationIcon(R.drawable.ic_back)
        findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout).title = "Back"
        findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout).setExpandedTitleColor(
            Color.parseColor(
                "#00FFFFFF"
            )
        ) /*Hide Title when the collapsing toolbar is expanded*/

        val articleImg: ImageView = findViewById(R.id.articleImage)

        val passedArticleImage: String? = intent.getStringExtra("articleImage")
        val passedArticleTitle: String? = intent.getStringExtra("articleTitle")
        val passedArticleContent: String? = intent.getStringExtra("articleContent")
        val passedArticleSource: String? = intent.getStringExtra("articleSource")

        val imgUri: Uri? = Uri.parse(passedArticleImage)

        Glide.with(applicationContext).load(imgUri).into(articleImage)
        articleTitleTV.text = passedArticleTitle
        articleSourceTV.text = passedArticleSource

        articleContentView.text = Html.fromHtml(passedArticleContent)
        articleContentView.movementMethod = LinkMovementMethod.getInstance()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}