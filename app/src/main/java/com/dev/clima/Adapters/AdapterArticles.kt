package com.dev.clima.Adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dev.clima.Activities.ActivitySingleArticle
import com.dev.clima.DataClasses.ArticlesDataClass
import com.dev.clima.DataClasses.VideoPicDataClass
import com.dev.clima.R

class AdapterArticles(val context: Context, val vidPicList: List<ArticlesDataClass>) :
    RecyclerView.Adapter<ArticlesViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticlesViewHolder {
        val inflater = LayoutInflater.from(context)
        return ArticlesViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: ArticlesViewHolder, position: Int) {

        val articleList: ArticlesDataClass = vidPicList[position]

        val articleTitle: String? = articleList.title
        val articleSource: String? = articleList.source
        val articleContent: String? = articleList.content
        val articleThumbnail: String? = articleList.image

        val imageThumbnail: Uri? = Uri.parse(articleThumbnail)
        Glide.with(context).load(imageThumbnail).into(holder.articleImage!!)

        holder.articleTitle?.text = articleTitle
        holder.articleSource?.text = articleSource
        holder.mainLayout!!.setOnClickListener {

            val intent: Intent = Intent(context, ActivitySingleArticle::class.java)
            intent.putExtra("articleTitle", articleTitle)
            intent.putExtra("articleContent", articleContent)
            intent.putExtra("articleSource", articleSource)
            intent.putExtra("articleImage", articleThumbnail)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
       return vidPicList.size
    }
}

class ArticlesViewHolder(inflater: LayoutInflater, parent: ViewGroup) : RecyclerView.ViewHolder(
    inflater.inflate(R.layout.single_article_list_item, parent, false)
) {
    var articleTitle: TextView? = null
    var articleSource: TextView? = null
    var articleImage: ImageView? = null
    var mainLayout: LinearLayout? = null

    init{
        articleTitle = itemView.findViewById(R.id.articleTitle)
        articleSource = itemView.findViewById(R.id.articleSource)
        articleImage = itemView.findViewById(R.id.articleImage)
        mainLayout = itemView.findViewById(R.id.mainLayoutArticles)
    }

}
