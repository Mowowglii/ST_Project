package com.example.stproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.stproject.service.DetecteurClicCarte
import com.example.stproject.service.EcouteurClicCarte
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.MapEventsOverlay

class Carte : AppCompatActivity(), EcouteurClicCarte {
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


        // Mathushan qui ajouté ici
        // detecteur contient une instance de la classe DetecteurClicCarte qui gère les événements de clic
        val detecteur = DetecteurClicCarte(this)

        // Overlay permet de capter les interactions sur la carte et les transmettre au detecteur (single tap et long press)
        val overlay = MapEventsOverlay(detecteur)

        // On ajoute cet overlay à la map pour détecter les clics
        map.overlays.add(overlay)
    }
    // Avant j'affichais le coordonnées dans la classe detecteurcliccarte, ce qui faisait que je ne pouvais pas réellement utiliser.
    // Maintenant avec cette méthode ça affiche toujours dans le terminal mais je vais enfin pouvoir utiliser pour les comparer...
    override fun recevoirClic(latitude: Double, longitude: Double) {
        println("Latitude: $latitude Longitude: $longitude")
    }
}