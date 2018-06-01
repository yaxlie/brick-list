package com.mlmg.bricklist

class Inventory(name: String, id: Int = 0, active: Boolean = true) {
    var id: Int? = id
    var name: String? = name
    var active: Boolean? = active
    var lastMod: String? = null
    var inventoriesParts: ArrayList<InventoriesPart>? = null

    var quantityInStore = 0
    var quantityInSet = 0
}