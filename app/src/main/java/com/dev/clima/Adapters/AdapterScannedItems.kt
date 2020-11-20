package com.dev.clima.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dev.clima.DataClasses.ArticlesDataClass
import com.dev.clima.DataClasses.ScannedPlasticsDataClass
import com.dev.clima.R

class AdapterScannedItems(val context: Context, val scannedItemList: List<ScannedPlasticsDataClass>) :
    RecyclerView.Adapter<ScannedViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScannedViewHolder {
        val inflater = LayoutInflater.from(context)
        return ScannedViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int {
       return scannedItemList.size
    }

    override fun onBindViewHolder(holder: ScannedViewHolder, position: Int) {
        val scanList: ScannedPlasticsDataClass = scannedItemList[position]

        holder.barcodeText?.text = scanList.barCode
    }
}

class ScannedViewHolder(inflater: LayoutInflater, parent: ViewGroup) : RecyclerView.ViewHolder(
    inflater.inflate(R.layout.single_scanned_item, parent, false)
) {
    var barcodeText: TextView? = null

    init{
        barcodeText = itemView.findViewById(R.id.barcodeText)
    }

}
