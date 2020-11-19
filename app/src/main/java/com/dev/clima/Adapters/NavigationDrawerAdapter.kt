package com.dev.clima.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dev.clima.DataClasses.NavigationDrawerDataClass
import com.dev.clima.R

class NavigationDrawerAdapter(
    val context: Context,
    val drawerList: List<NavigationDrawerDataClass>,
    var listener: NavigationDrawerListener?

) : RecyclerView.Adapter<MyViewHolder>() {
    interface NavigationDrawerListener {
        fun onNavMenuItemSelected(navDrawerDataClass: NavigationDrawerDataClass?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(context)
        return MyViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val navDrawer: NavigationDrawerDataClass = drawerList[position]

        holder.itemLabel?.text = navDrawer.item_name
        holder.itemIcon!!.setImageResource(navDrawer.image_resource)
        holder.itemLayout!!.setOnClickListener {
            listener?.onNavMenuItemSelected(navDrawer)
        }
    }

    override fun getItemCount(): Int = drawerList.size
}

class MyViewHolder(inflater: LayoutInflater, parent: ViewGroup) : RecyclerView.ViewHolder(
    inflater.inflate(R.layout.navigation_drawer_single_item, parent, false)
){

    var itemLabel: TextView? = null
    var itemIcon: ImageView? = null
    var itemLayout: LinearLayout? = null

    init{
        itemLabel = itemView.findViewById(R.id.navItemLabel)
        itemIcon = itemView.findViewById(R.id.navItemIcon)
        itemLayout = itemView.findViewById(R.id.navLinearLayout)
    }

}

