package com.mlmg.bricklist

class InventoriesPart(id: Int?, inventoryId:Int?, typeId:Int?, itemId:Int?, quantityInSet:Int?, quantityInStore:Int?,
                      colorId:Int?, extra:Int?, part: Part? = null, color: ColorModel? = null) {
    var id = id
    var inventoryId = inventoryId
    var typeId = typeId
    var itemId = itemId
    var quantityInSet = quantityInSet
    var quantityInStore = quantityInStore
    var colorId = colorId
    var extra = extra

    var part = part
    var color = color

    var listMoved = false
}