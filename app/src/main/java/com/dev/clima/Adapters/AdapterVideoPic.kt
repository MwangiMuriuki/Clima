package com.dev.clima.Adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dev.clima.Activities.ActivityViewMedia
import com.dev.clima.DataClasses.VideoPicDataClass
import com.dev.clima.R

class AdapterVideoPic(val context: Context, var vidPicList: List<VideoPicDataClass>) :
    RecyclerView.Adapter<VideopicViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideopicViewHolder {
        val inflater = LayoutInflater.from(context)
        return VideopicViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: VideopicViewHolder, position: Int) {
        val mediaList: VideoPicDataClass = vidPicList[position]

        val postTitle: String? = mediaList.title
        val postSource: String? = mediaList.source
        val postImage: String? = mediaList.thumbnail
        val imageThumbnail: Uri? = Uri.parse(postImage)
        val postIsVideo: Boolean? = mediaList.video

        Glide.with(context).load(imageThumbnail).into(holder.thumbnail!!)

        if (postIsVideo!!){
            holder.iconLayout?.visibility = View.VISIBLE
        }else{
            holder.iconLayout?.visibility = View.GONE
        }

        holder.title?.text = mediaList.title
        holder.mainLayout!!.setOnClickListener {
            val intent: Intent = Intent(context, ActivityViewMedia::class.java)
            intent.putExtra("postTitle", postTitle)
            intent.putExtra("postSource", postSource)
            intent.putExtra("postThumbnail", postImage)
            intent.putExtra("postIsVideo", postIsVideo)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return vidPicList.size
    }

    fun filterList(filteredList: MutableList<VideoPicDataClass>) {
        vidPicList = filteredList
        notifyDataSetChanged()
    }
}

class VideopicViewHolder(inflater: LayoutInflater, parent: ViewGroup) : RecyclerView.ViewHolder(
    inflater.inflate(R.layout.single_media_list_item, parent, false)
){

    var title: TextView? = null
    var thumbnail: ImageView? = null
    var mainLayout: LinearLayout? = null
    var iconLayout: LinearLayout? = null

    init {
        title = itemView.findViewById(R.id.mediaTitle)
        thumbnail = itemView.findViewById(R.id.thumbnail)
        mainLayout = itemView.findViewById(R.id.mainLayout)
        iconLayout = itemView.findViewById(R.id.playVideoIcon)
    }
}
