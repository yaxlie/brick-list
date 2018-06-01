package com.mlmg.bricklist

import android.R.attr.name

import android.R.attr.name
import android.content.Context
import android.view.*
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import android.app.Activity
import android.content.ContentValues
import android.graphics.Color
import android.widget.Button
import kotlinx.android.synthetic.main.part_list_object.view.*


class ItemsAdapter(context: Context, parts: ArrayList<InventoriesPart>) : ArrayAdapter<InventoriesPart>(context, 0, parts) {

    val NAME_SIZE = 80
    var myDbHelper: DataBaseHelper? = null

    internal class ViewHolder {
        var itemName: TextView? = null
        var itemColor: TextView? = null
        var itemIMG: ImageView? = null
        var itemInSet: TextView? = null
        var itemInStore: TextView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        myDbHelper = MyDbHelper.openDb(context)
        val viewHolder: ViewHolder
        var convertView = convertView
        // Get the data item for this position
        val part = getItem(position)
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.part_list_object, parent, false)
        }
        // Lookup view for data population
        val minusButton = convertView!!.findViewById<Button>(R.id.button_minus)
        val plusButton = convertView.findViewById<Button>(R.id.button_plus)

        val title = convertView.findViewById<TextView>(R.id.titleText) as TextView
        val color = convertView.findViewById<TextView>(R.id.colorText) as TextView
        val quantity = convertView.findViewById<TextView>(R.id.quantityText) as TextView
        val img = convertView.findViewById<ImageView>(R.id.imageView) as ImageView
        // Populate the data into the template view using the data object

        var _quantity = "${part.quantityInStore} / ${part.quantityInSet}"
        val _color = "kolor : ${part.color!!.name}"
        var _name =  part!!.part!!.name
        if(_name.length>NAME_SIZE)
            _name = "${_name.substring(0, NAME_SIZE)}..."

        title.text = _name
        color.text = _color
        quantity.text = _quantity
        // Return the completed view to render on screen

        fun refreshQuantity(inStore: Int){
            if(inStore>=0 && inStore<=part.quantityInSet!!) {
                part.quantityInStore = inStore
                _quantity = "${part.quantityInStore!!} / ${part.quantityInSet}"
                quantity.text = _quantity

                if(inStore == part.quantityInSet!!){
                    if(!part.listMoved) {
                        part.listMoved = true
                        this.remove(part)
                        this.add(part)
                    }
                    else {
                        convertView!!.setBackgroundColor(context.resources.getColor(R.color.colorTrue))
                    }
                }
                else
                    convertView!!.setBackgroundColor(Color.WHITE)
            }
        }

        refreshQuantity(part.quantityInStore!!)

        minusButton.setOnClickListener {
            updateInStore(part.quantityInStore!!-1, part.id!!)
            refreshQuantity(part.quantityInStore!!-1)
        }

        plusButton.setOnClickListener {
            updateInStore(part.quantityInStore!!+1, part.id!!)
            refreshQuantity(part.quantityInStore!!+1)
        }


        if(part.color!!.code == 0)
            Picasso.get().load("https://www.bricklink.com/PL/${part.part!!.code}.jpg").into(img)
        else
            Picasso.get().load("http://img.bricklink.com/P/${part.color!!.code}/${part.part!!.code}.gif").into(img)
        return convertView
    }


    fun updateInStore(value: Int, id: Int){
        val values = ContentValues()
        values.put("QuantityInStore", value)
        myDbHelper?.updateInStore(values,id)
    }
}