package com.mlmg.bricklist

import android.content.Context
import java.io.IOException
import java.sql.SQLException

object MyDbHelper {

    fun openDb(context: Context): DataBaseHelper{
        val dbHelper = DataBaseHelper(context)
        try {
            dbHelper.createDataBase()
        } catch (ioe: IOException) {
            throw Error("Unable to create database")
        }
        try {
            dbHelper.openDataBase()
        } catch (sqle: SQLException) {
            throw sqle
        }
        return dbHelper
    }
}