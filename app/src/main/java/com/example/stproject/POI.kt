package com.example.stproject
import org.osmdroid.util.GeoPoint
data class POI(
    var name: String,
    val geoPoint: GeoPoint,
    var note: Float = 0f,
    var photos: MutableList<String> = mutableListOf(),
    var type: String = "autre"
)