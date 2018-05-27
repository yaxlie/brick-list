package com.mlmg.bricklist

class Inventory(name: String) {
    var id: Int? = null
    var name: String? = name
    var active: Boolean? = true
    var lastMod: String? = null
    var inventoriesParts: ArrayList<InventoriesPart>? = null
}