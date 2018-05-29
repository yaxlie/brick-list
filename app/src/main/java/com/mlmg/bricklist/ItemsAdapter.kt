package com.mlmg.bricklist

import android.R.attr.name

import android.R.attr.name
import android.content.Context
import android.view.*
import android.widget.ArrayAdapter
import android.widget.TextView

class ItemsAdapter(context: Context, parts: ArrayList<InventoriesPart>) : ArrayAdapter<InventoriesPart>(context, 0, parts) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        // Get the data item for this position
        val part = getItem(position)
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.part_list_object, parent, false)
        }
        // Lookup view for data population
        val title = convertView!!.findViewById<TextView>(R.id.titleText) as TextView
        // Populate the data into the template view using the data object
        title.setText(part!!.itemId.toString())
        // Return the completed view to render on screen
        return convertView
    }
}