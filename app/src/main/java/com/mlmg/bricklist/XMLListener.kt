package com.mlmg.bricklist

interface XMLListener {
    fun onSuccess(xml:String)
    fun onFailure(er: String)
}