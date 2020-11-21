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
import com.dev.clima.R

class AdapterMyArticles (val context: Context, val vidPicList: List<ArticlesDataClass>) :
        RecyclerView.Adapter<MyArticlesViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyArticlesViewHolder {
        val inflater = LayoutInflater.from(context)
        return MyArticlesViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int {
        return vidPicList.size
    }

    override fun onBindViewHolder(holder: MyArticlesViewHolder, position: Int) {

        val articleList: ArticlesDataClass = vidPicList[position]

        val articleTitle: String? = articleList.title
        val articleContent: String? = articleList.content
        val articleThumbnail: String? = articleList.image

        val imageThumbnail: Uri? = Uri.parse(articleThumbnail)
        Glide.with(context).load(imageThumbnail).into(holder.articleImage!!)

        holder.articleTitle?.text = articleTitle
        holder.mainLayout!!.setOnClickListener {

            val intent: Intent = Intent(context, ActivitySingleArticle::class.java)
            intent.putExtra("articleTitle", articleTitle)
            intent.putExtra("articleContent", articleContent)
            intent.putExtra("articleImage", articleThumbnail)
            context.startActivity(intent)
        }
    }
}

class MyArticlesViewHolder(inflater: LayoutInflater, parent: ViewGroup) : RecyclerView.ViewHolder(
        inflater.inflate(R.layout.single_item_my_articles, parent, false)
) {
    var articleTitle: TextView? = null
    var articleImage: ImageView? = null
    var mainLayout: LinearLayout? = null

    init{
        articleTitle = itemView.findViewById(R.id.articleTitle)
        articleImage = itemView.findViewById(R.id.articleImage)
        mainLayout = itemView.findViewById(R.id.mainLayoutArticles)
    }
}
