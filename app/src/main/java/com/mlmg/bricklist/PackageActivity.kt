package com.mlmg.bricklist

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.content.Intent



class PackageActivity : AppCompatActivity() {

    val TAG = "PartsList"
    val EXTRA_ID = "project_id" //todo pobrac to z Main Activity
    var myDbHelper: DataBaseHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_package)

        val projectId = intent.getIntExtra(EXTRA_ID, 0)

        myDbHelper = MyDbHelper.openDb(this)

        // Construct the data source
        val arrayOfUsers = myDbHelper!!.getAllInvParts(projectId)
        Log.i(TAG,"${arrayOfUsers.size.toString()} $projectId")
        // Create the adapter to convert the array to views
        val adapter = ItemsAdapter(this, arrayOfUsers)
        // Attach the adapter to a ListView
        val listView = findViewById<ListView>(R.id.listView) as ListView
        listView.setAdapter(adapter)
    }
}
