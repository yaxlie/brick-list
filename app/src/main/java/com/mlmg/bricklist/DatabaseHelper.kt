package com.mlmg.bricklist

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import java.io.FileOutputStream
import java.io.IOException
import java.sql.SQLException


class DataBaseHelper
/**
 * Constructor
 * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
 * @param context
 */
(private val myContext: Context) : SQLiteOpenHelper(myContext, DB_NAME, null, 1) {

    private var myDataBase: SQLiteDatabase? = null

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     */
    @Throws(IOException::class)
    fun createDataBase() {

        val dbExist = checkDataBase()

        if (dbExist) {
            //do nothing - database already exist
        } else {

            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.readableDatabase

            try {

                copyDataBase()

            } catch (e: IOException) {

                throw Error("Error copying database")

            }

        }

    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private fun checkDataBase(): Boolean {

        var checkDB: SQLiteDatabase? = null

        try {
            val myPath = DB_PATH + DB_NAME
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY)

        } catch (e: SQLiteException) {

            //database does't exist yet.

        }

        if (checkDB != null) {

            checkDB.close()

        }

        return if (checkDB != null) true else false
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     */
    @Throws(IOException::class)
    private fun copyDataBase() {

        //Open your local db as the input stream
        val myInput = myContext.getAssets().open(DB_NAME)

        // Path to the just created empty db
        val outFileName = DB_PATH + DB_NAME

        //Open the empty db as the output stream
        val myOutput = FileOutputStream(outFileName)

        //transfer bytes from the inputfile to the outputfile
        val buffer = ByteArray(1024)
        var length: Int
        length = myInput.read(buffer)
        while (length > 0) {
            myOutput.write(buffer, 0, length)
            length = myInput.read(buffer)
        }

        //Close the streams
        myOutput.flush()
        myOutput.close()
        myInput.close()

    }

    @Throws(SQLException::class)
    fun openDataBase() {

        //Open the database
        val myPath = DB_PATH + DB_NAME
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE)

    }

    @Synchronized override fun close() {

        if (myDataBase != null)
            myDataBase!!.close()

        super.close()

    }

    override fun onCreate(db: SQLiteDatabase) {

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    companion object {

        //The Android's default system path of your application database.
        private val DB_PATH = "/data/data/com.mlmg.bricklist/databases/"

        private val DB_NAME = "BrickList.db"
    }

    @Throws(SQLiteConstraintException::class)
    fun insertInventory(inventory: Inventory): Long? {
        // Create a new map of values, where column names are the keys
        val values = ContentValues()
        values.put("Name", inventory.name)
        values.put("Active", true)
        values.put("LastAccessed", 0)

        // Insert the new row, returning the primary key value of the new row
        val newRowId = myDataBase?.insert("Inventories", null, values)
        return newRowId
    }

    @Throws(SQLiteConstraintException::class)
    fun insertInventoryPart(item: Item, invId: Long?): Long? {
        // Create a new map of values, where column names are the keys

        val itemType = getTypeModel(item.itemType)
        val part = getPart(item.itemId)
        val color = getColorModel(item.color)

        val values = ContentValues()
        values.put("InventoryID", invId)
        values.put("TypeID", itemType?.id)
        values.put("ItemID", part?.id)
        values.put("QuantityInSet", item.qty.toInt())
        values.put("QuantityInStore", 0)
        values.put("ColorID", color?.id)

        // Insert the new row, returning the primary key value of the new row
        if (part?.id != null){
            return myDataBase?.insert("InventoriesParts", null, values)
        }else

        return -1
    }

    fun readAllInventory(): ArrayList<Inventory> {
        val inventories = ArrayList<Inventory>()
        val db = myDataBase
        var cursor: Cursor? = null
        try {
            cursor = db?.rawQuery("select * from Inventories", null)
        } catch (e: SQLiteException) {
            return ArrayList()
        }

        var id: Int
        var name: String
        var active: Int
        var lastAccess: Int

        if (cursor!!.moveToFirst()) {
            while (cursor.isAfterLast == false) {
                name = cursor.getString(cursor.getColumnIndex("Name"))
                id = cursor.getInt(cursor.getColumnIndex("id"))
                active = cursor.getInt(cursor.getColumnIndex("Active"))

                inventories.add(Inventory(name, id, active>0))
                cursor.moveToNext()
            }
        }
        cursor.close()
        return inventories
    }

    fun getInventory(id: Int): Inventory? {
        var inventory: Inventory? = null
        val db = myDataBase
        var cursor: Cursor? = null
        try {
            cursor = db?.rawQuery("select * from Inventories where id = $id", null)
        } catch (e: SQLiteException) {
            return null
        }

        var id: Int
        var name: String
        var active: Int
        var lastAccess: Int

        if (cursor!!.moveToFirst()) {
            while (cursor.isAfterLast == false) {
                name = cursor.getString(cursor.getColumnIndex("Name"))
                id = cursor.getInt(cursor.getColumnIndex("id"))
                active = cursor.getInt(cursor.getColumnIndex("Active"))

                inventory = Inventory(name, id, active>0)
                cursor.moveToNext()
            }
        }
        cursor.close()
        return inventory
    }

    fun getTypeModel(code: String): ItemTypeModel? {
        val db = myDataBase
        var cursor: Cursor? = null
        try {
            cursor = db?.rawQuery("select * from ItemTypes WHERE Code like '$code'", null)
        } catch (e: SQLiteException) {
            // if table not yet present, create it
            return null
        }
        var id = 0
        var code = ""
        var name = ""
        var namePl: String? = ""
        if (cursor!!.moveToFirst()) {
            while (cursor.isAfterLast == false) {
                id = cursor.getInt(cursor.getColumnIndex("id"))
                code = cursor.getString(cursor.getColumnIndex("Code"))
                name = cursor.getString(cursor.getColumnIndex("Name"))
                namePl = cursor.getString(cursor.getColumnIndex("NamePL"))
                cursor.moveToNext()
            }
        }
        cursor.close()
        return ItemTypeModel(id, code, name, namePl)
    }

    fun getColorModel(code: String, colorId: Int = 0): ColorModel? {
        val db = myDataBase
        var cursor: Cursor? = null
        try {
            if(colorId>0)
                cursor = db?.rawQuery("select * from Colors WHERE id = $colorId", null)
            else
                cursor = db?.rawQuery("select * from Colors WHERE Code = "  + code, null)
        } catch (e: SQLiteException) {
            // if table not yet present, create it
            return null
        }
        var id = 0
        var code = 0
        var name = ""
        var namePl: String? = ""
        if (cursor!!.moveToFirst()) {
            while (cursor.isAfterLast == false) {
                id = cursor.getInt(cursor.getColumnIndex("id"))
                code = cursor.getInt(cursor.getColumnIndex("Code"))
                name = cursor.getString(cursor.getColumnIndex("Name"))
                namePl = cursor.getString(cursor.getColumnIndex("NamePL"))
                cursor.moveToNext()
            }
        }
        cursor.close()
        return ColorModel(id, code, name, namePl)
    }

    fun getPart(code: String, itemId: Int = 0): Part? {
        val db = myDataBase
        var part: Part? = null
        var cursor: Cursor? = null
        try {
            if(itemId == 0)
                cursor = db?.rawQuery("select * from Parts WHERE Code like '$code'", null)
            else
                cursor = db?.rawQuery("select * from Parts WHERE id =  '$itemId'", null)
        } catch (e: SQLiteException) {
            // if table not yet present, create it
            return null
        }

        var id = 0
        var typeId = 0
        var code = ""
        var name = ""
        var namePl: String? = ""
        var categoryId = 0

        if (cursor!!.moveToFirst()) {
            while (cursor.isAfterLast == false) {
                id = cursor.getInt(cursor.getColumnIndex("id"))
                typeId = cursor.getInt(cursor.getColumnIndex("TypeID"))
                code = cursor.getString(cursor.getColumnIndex("Code"))
                name = cursor.getString(cursor.getColumnIndex("Name"))
                namePl = cursor.getString(cursor.getColumnIndex("NamePL"))
                categoryId = cursor.getInt(cursor.getColumnIndex("CategoryID"))
                part = Part(id,typeId,code,name,namePl, categoryId)
                cursor.moveToNext()
            }
        }
        cursor.close()
        return part
    }

    fun getAllInvParts(inventory_id : Int): ArrayList<InventoriesPart> {
        val parts = ArrayList<InventoriesPart>()
        val db = myDataBase
        var cursor: Cursor? = null
        try {
            cursor = db?.rawQuery("select * from InventoriesParts where InventoryID = $inventory_id", null)
        } catch (e: SQLiteException) {
            return ArrayList()
        }

        var id = 0
        var inventoryId = inventory_id
        var typeId = 0
        var itemId = 0
        var quantityInSet = 0
        var quantityInStore = 0
        var colorId = 0
        var extra : Int? = 0

        var part: Part? = null
        var color: ColorModel? = null

        if (cursor!!.moveToFirst()) {
            while (cursor.isAfterLast == false) {
                id = cursor.getInt(cursor.getColumnIndex("id"))
                typeId = cursor.getInt(cursor.getColumnIndex("TypeID"))
                itemId = cursor.getInt(cursor.getColumnIndex("ItemID"))
                quantityInSet = cursor.getInt(cursor.getColumnIndex("QuantityInSet"))
                quantityInStore = cursor.getInt(cursor.getColumnIndex("QuantityInStore"))
                colorId = cursor.getInt(cursor.getColumnIndex("ColorID"))

                part = getPart("", itemId)
                color = getColorModel("", colorId)

                parts.add(InventoriesPart(id, inventoryId, typeId, itemId, quantityInSet, quantityInStore, colorId, extra,
                        part,color))
                cursor.moveToNext()
            }
        }
        cursor.close()
        return parts
    }

    @Throws(SQLiteConstraintException::class)
    fun deleteInventory(id: String): Boolean {
        // Gets the data repository in write mode
        val db = myDataBase
        // Define 'where' part of query.
        val selection = "id" + " = ?"
        // Specify arguments in placeholder order.
        val selectionArgs = arrayOf(id)
        // Issue SQL statement.
        db!!.delete("Inventories", selection, selectionArgs)
        return true
    }

    fun updateInStore(values: ContentValues, id: Int): String {
        var selectionArs = arrayOf(id.toString())
        val i = myDataBase!!.update("InventoriesParts", values, "id=?", selectionArs)
        if (i > 0) {
            return "ok";
        } else {
            return "error";
        }
    }


    fun updateInventory(values: ContentValues, id: Int): String {
        var selectionArs = arrayOf(id.toString())
        val i = myDataBase!!.update("Inventories", values, "id=?", selectionArs)
        if (i > 0) {
            return "ok";
        } else {
            return "error";
        }
    }

    fun getInvQuantity(inventory_id : Int): IntArray {
        val q: IntArray = intArrayOf(0,0)
        val db = myDataBase
        var cursor: Cursor? = null
        try {
            cursor = db?.rawQuery("select * from InventoriesParts where InventoryID = $inventory_id", null)
        } catch (e: SQLiteException) {
            return q
        }

        var quantityInSet = 0
        var quantityInStore = 0


        if (cursor!!.moveToFirst()) {
            while (cursor.isAfterLast == false) {
                quantityInSet = cursor.getInt(cursor.getColumnIndex("QuantityInSet"))
                quantityInStore = cursor.getInt(cursor.getColumnIndex("QuantityInStore"))

                q[0] += quantityInStore
                q[1] += quantityInSet
                cursor.moveToNext()
            }
        }
        cursor.close()
        return q
    }

    // Add your public helper methods to access and get content from the database.
    // You could return cursors by doing "return myDataBase.query(....)" so it'd be easy
    // to you to create adapters for your views.

}