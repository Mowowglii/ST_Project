package com.example.stproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class Carte : AppCompatActivity() {
    private lateinit var map: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Config OSMDroid
        Configuration.getInstance().load(applicationContext,
            getSharedPreferences("OSMDroidPrefs", MODE_PRIVATE))

        setContentView(R.layout.activity_carte)

        map = findViewById(R.id.map)
        map.setMultiTouchControls(true)

        // Exemple : récupérer le nom du voyage depuis SharedPreferences
        val sharedPref = getSharedPreferences("SmartTripPrefs", MODE_PRIVATE)
        val tripName = sharedPref.getString("trip_name", "Mon voyage")

        // Centrer la carte sur Paris (ou autre)
        val startPoint = GeoPoint(48.8566, 2.3522)
        map.controller.setZoom(12.0)
        map.controller.setCenter(startPoint)

        // Ajouter un marqueur avec le nom du voyage
        val marker = Marker(map)
        marker.position = startPoint
        marker.title = tripName
        map.overlays.add(marker)
    }
}