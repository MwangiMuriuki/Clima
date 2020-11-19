package com.dev.clima.Adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dev.clima.DataClasses.NavigationDrawerDataClass
import com.dev.clima.DataClasses.VideoPicDataClass
import com.dev.clima.R
import com.google.common.reflect.Reflection.getPackageName

class AdapterVideoPic(val context: Context, val vidPicList: List<VideoPicDataClass>) :
    RecyclerView.Adapter<VideopicViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideopicViewHolder {
        val inflater = LayoutInflater.from(context)
        return VideopicViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: VideopicViewHolder, position: Int) {

        val mediaList: VideoPicDataClass = vidPicList[position]

        val imageThumbnail: Uri? = Uri.parse(mediaList.thumbnail)
        Glide.with(context).load(imageThumbnail).into(holder.thumbnail!!)

        holder.title?.text = mediaList.title
        holder.mainLayout!!.setOnClickListener {

        }

    }

    override fun getItemCount(): Int {
        return vidPicList.size
    }
}

class VideopicViewHolder(inflater: LayoutInflater, parent: ViewGroup) : RecyclerView.ViewHolder(
    inflater.inflate(R.layout.single_media_list_item, parent, false)){

    var title: TextView? = null
    var thumbnail: ImageView? = null
    var mainLayout: LinearLayout? = null

    init {
        title = itemView.findViewById(R.id.mediaTitle)
        thumbnail = itemView.findViewById(R.id.thumbnail)
        mainLayout = itemView.findViewById(R.id.mainLayout)
    }
}
