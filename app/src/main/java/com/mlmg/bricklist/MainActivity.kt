package com.mlmg.bricklist

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutCompat
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import com.mlmg.bricklist.R.id.projectsList
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.project_list_object.view.*
import java.io.IOException
import java.sql.SQLException
import java.util.ArrayList
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource

import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory


class MainActivity : AppCompatActivity() {

    val EXTRA_ID = "project_id"
    val BASE_URL = "http://fcds.cs.put.poznan.pl/MyWeb/BL/"

    var myDbHelper: DataBaseHelper? = null
    var inventories: List<Inventory>? = null
    var package_nr: String = "615"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i("Main", "Start")
//        myDbHelper = openDb()
        myDbHelper = MyDbHelper.openDb(this)

//        myDbHelper?.insertInventory(Inventory("Projekt 1"))
        inventories = myDbHelper?.readAllInventory()

        loadProjects()

        Log.i("Main", inventories?.size.toString())

//        getXml(package_nr, object:XMLListener{
//            override fun onSuccess(xml:String) {
//                var items = xmlParser(xml)
//                Log.i("Main", "XML sprasowany")
//            }
//
//            override fun onFailure(er : String) {
//                Log.i("Main", er)
//            }
//        })

        new_projButton.setOnClickListener {
            addProject()
//             myDbHelper?.getTypeModel("M")
        }

    }


    fun loadProjects(){
        val layout = findViewById<LinearLayout>(R.id.projectsList)
        layout.removeAllViews()
        for (i in inventories!!){
            val item = addProjListItem(i,layout)
            item.setOnClickListener {
                val intent = Intent(this, PackageActivity::class.java)
                val prjId = i.id
                intent.putExtra(EXTRA_ID, prjId)
                startActivity(intent)
            }
        }
    }
    fun addProjListItem(inventory: Inventory, tempLayout: LinearLayout): LinearLayoutCompat {
        val item = layoutInflater.inflate(R.layout.project_list_object,null) as LinearLayoutCompat
        item.titleText.text = (inventory.name)

        tempLayout.addView(item)
        return item
    }

    fun openDb(): DataBaseHelper{
        var myDbHelper = DataBaseHelper(this)
        myDbHelper = DataBaseHelper(this)
        try {
            myDbHelper.createDataBase()
        } catch (ioe: IOException) {
            throw Error("Unable to create database")
        }
        try {
            myDbHelper.openDataBase()
        } catch (sqle: SQLException) {
            throw sqle
        }
        return myDbHelper
    }

    private fun getXml(name:String, listener: XMLListener){
        val thread = Thread(Runnable {
            try {
                val url = URL(BASE_URL + name + ".xml")
                val conn = url.openConnection() as HttpURLConnection
                conn.setRequestMethod("GET")
                conn.setDoInput(true)
                conn.setReadTimeout(10000)
                conn.setConnectTimeout(15000)

                Log.v("Start Query", "Stream")
                conn.connect()
                Log.v("End Query", "Stream")
                //read the result from the server
                val rdr = BufferedReader(InputStreamReader(conn.getInputStream()))
                val sbr = StringBuilder()

                var line = rdr.readLine()
                while (line != null) {
                    sbr.append(line + '\n')
                    line = rdr.readLine()
                }

                Log.i("Main", sbr.toString())
                listener.onSuccess(sbr.toString())

            } catch (e: Exception) {
                e.printStackTrace()
                listener.onFailure(e.toString())
            }
        })
        thread.start()
    }

    fun xmlParser(string:String):ArrayList<Item>{
        val items = ArrayList<Item>()
//        val partNames = ArrayList<String>()
//        for (s:String in string.split("<ITEM>"))
//            partNames.add(s.split("</ITEMID>")[0])
//        partNames.removeAt(0)

        val charset = Charsets.UTF_8
        val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                InputSource(ByteArrayInputStream(string.toByteArray(charset))))

        xmlDoc.documentElement.normalize()

        println("Root Node:" + xmlDoc.documentElement.nodeName)

        val itemList: NodeList = xmlDoc.getElementsByTagName("ITEM")

        for(i in 0..itemList.length - 1)
        {
            var itemNode: Node = itemList.item(i)
            if (itemNode.getNodeType() === Node.ELEMENT_NODE) {
                val elem = itemNode as Element
                val mMap = mutableMapOf<String, String>()
                for(j in 0..elem.attributes.length - 1)
                {
                    if(mMap.get(elem.attributes.item(j).nodeName)==null)
                        mMap.put(elem.attributes.item(j).nodeName, elem.attributes.item(j).nodeValue)
//                    mMap.putIfAbsent(elem.attributes.item(j).nodeName, elem.attributes.item(j).nodeValue)
                }
                items.add(Item(elem.getElementsByTagName("ITEMTYPE").item(0).textContent,
                        elem.getElementsByTagName("ITEMID").item(0).textContent,
                        elem.getElementsByTagName("QTY").item(0).textContent,
                        elem.getElementsByTagName("COLOR").item(0).textContent,
                        elem.getElementsByTagName("EXTRA").item(0).textContent,
                        elem.getElementsByTagName("ALTERNATE").item(0).textContent,
                        elem.getElementsByTagName("MATCHID").item(0).textContent,
                        elem.getElementsByTagName("COUNTERPART").item(0).textContent))
            }
        }
        return items
    }

    fun addProject() {
        Log.i("ADD_PROJ", "Start")
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.custom_dialog, null)
        dialogBuilder.setView(dialogView)

        val editText = dialogView.findViewById<View>(R.id.idText) as EditText

        dialogBuilder.setTitle("Nowy projekt")
        dialogBuilder.setMessage("Wprowadź id projektu")
        dialogBuilder.setPositiveButton("Dodaj", DialogInterface.OnClickListener { dialog, whichButton ->

            getXml(editText.text.toString(), object:XMLListener{
                override fun onSuccess(xml:String) {

                        Log.i("ADD_PROJ", "Sukces")
                        var items = xmlParser(xml)
                        val p_id = myDbHelper?.insertInventory(Inventory(editText.text.toString()))

                        for (item in items) {
                            Log.i("ADD_PROJ", "Przetwarzanie ${item.itemId}")
                            val e = myDbHelper?.insertInventoryPart(item, p_id)
                            if (e!! >-1){
                                Log.i("ADD_PROJ", "Dodano część ${item.itemId} do projektu $p_id")
                            }else{
                                Log.e("ADD_PROJ", "Brak klocka ${item.itemId}:" +e.toString())
                            }
                    }
                    this@MainActivity.runOnUiThread(java.lang.Runnable {
                        loadProjects()
                    })
                }

                override fun onFailure(er : String) {
                    Log.i("ADD_PROJ", "Failure:" + er)
                }
            })

        })
        dialogBuilder.setNegativeButton("Anuluj", DialogInterface.OnClickListener { dialog, whichButton ->
            //pass
        })
        val b = dialogBuilder.create()
        b.show()
    }
}
