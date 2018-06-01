package com.mlmg.bricklist

import android.content.ContentValues
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.content.Intent
import android.graphics.Color
import kotlinx.android.synthetic.main.activity_package.*


class PackageActivity : AppCompatActivity() {

    val TAG = "PartsList"
    val EXTRA_ID = "project_id" //todo pobrac to z Main Activity
    var myDbHelper: DataBaseHelper? = null
    var inventory: Inventory? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_package)

        val projectId = intent.getIntExtra(EXTRA_ID, 0)

        myDbHelper = MyDbHelper.openDb(this)
        inventory = myDbHelper?.getInventory(projectId)

        // Construct the data source
        val arrayOfUsers = myDbHelper!!.getAllInvParts(projectId)
        Log.i(TAG,"${arrayOfUsers.size.toString()} $projectId")
        // Create the adapter to convert the array to views
        val adapter = ItemsAdapter(this, arrayOfUsers)
        // Attach the adapter to a ListView
        val listView = findViewById<ListView>(R.id.listView) as ListView
        listView.setAdapter(adapter)
        setArchiveColor()

        buttonDelete.setOnClickListener {
            myDbHelper!!.deleteInventory(projectId.toString())
            finish()
        }

        buttonArchive.setOnClickListener {
            val values = ContentValues()
            val active = inventory?.active!!
            values.put("Active", !active)
            myDbHelper!!.updateInventory(values, projectId)
            inventory!!.active = !active
            setArchiveColor()
        }
    }

    fun setArchiveColor(){
        buttonArchive.background = (if (inventory!!.active!!) resources.getDrawable(R.drawable.ic_archive_white_24dp)
            else resources.getDrawable(R.drawable.ic_archive_black_24dp))
    }
}
