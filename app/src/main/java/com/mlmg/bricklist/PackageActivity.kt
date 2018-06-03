package com.mlmg.bricklist

import android.content.ContentValues
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AlertDialog
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_package.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


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
        val arrayOfParts = myDbHelper!!.getAllInvParts(projectId)
        Log.i(TAG,"${arrayOfParts.size.toString()} $projectId")
        // Create the adapter to convert the array to views
        val adapter = ItemsAdapter(this, arrayOfParts)
        // Attach the adapter to a ListView
        val listView = findViewById<ListView>(R.id.listView) as ListView
        listView.setAdapter(adapter)
        setArchiveColor()

        buttonDelete.setOnClickListener {

            val builder = AlertDialog.Builder(this@PackageActivity)
            builder.setTitle("Usuwanie zestawu.")
            builder.setMessage("Czy chcesz usunąć ten zestaw?")
            builder.setPositiveButton("YES"){dialog, which ->
                dialog.dismiss()
                myDbHelper!!.deleteInventory(projectId.toString())
                finish()
            }
            builder.setNeutralButton("Cancel"){_,_ ->
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        buttonArchive.setOnClickListener {
            val values = ContentValues()
            val active = inventory?.active!!
            values.put("Active", !active)
            myDbHelper!!.updateInventory(values, projectId)
            inventory!!.active = !active
            setArchiveColor()
        }

        buttonExport.setOnClickListener {
            writeXml(arrayOfParts)
        }
    }

    fun setArchiveColor(){
        buttonArchive.background = (if (inventory!!.active!!) resources.getDrawable(R.drawable.ic_archive_white_24dp)
            else resources.getDrawable(R.drawable.ic_archive_black_24dp))
    }

    fun writeXml(parts : ArrayList<InventoriesPart>){
        val docBuilder: DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc: Document = docBuilder.newDocument()

        val rootElement: Element = doc.createElement("INVENTORY")

        for(part in parts) {
            val itemtElement: Element = doc.createElement("ITEM")

            if((part.quantityInSet!! - part.quantityInStore!!)>0) {
                val itemType: Element = doc.createElement("ITEMTYPE")
                itemType.appendChild(doc.createTextNode(myDbHelper!!.getTypeModel(part.part!!.typeId.toString())!!.code))
                itemtElement.appendChild(itemType)
                val itemId: Element = doc.createElement("ITEMID")
                itemId.appendChild(doc.createTextNode(part.itemId.toString()))
                itemtElement.appendChild(itemId)
                val color: Element = doc.createElement("COLOR")
                color.appendChild(doc.createTextNode(part.colorId.toString()))
                itemtElement.appendChild(color)
                val qtyFilled: Element = doc.createElement("QTYFILLED")
                qtyFilled.appendChild(doc.createTextNode((part.quantityInSet!! - part.quantityInStore!!).toString()))
                itemtElement.appendChild(qtyFilled)
                rootElement.appendChild(itemtElement)
            }
        }

        doc.appendChild(rootElement)
        val transformer: Transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2")

        val path = this.getExternalFilesDir(null)
        val outDir = File(path, "Output")
        outDir.mkdir()

        val file=File(outDir,"${inventory!!.name}.txt")
        transformer.transform(DOMSource(doc), StreamResult(file))
        Toast.makeText(this@PackageActivity, "Zapisano w $outDir", Toast.LENGTH_SHORT).show()
    }
}
