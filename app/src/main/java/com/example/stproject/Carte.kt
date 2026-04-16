package com.example.stproject

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class Carte : AppCompatActivity() {

    private val poiList = mutableListOf<POI>()
    private var routeLine: Polyline? = null

    private lateinit var map: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay
    private lateinit var placesClient: PlacesClient

    private val requestlocation = 1

    private var lastSearchedPoint: GeoPoint? = null
    private var lastMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("OSMDroidPrefs", MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = packageName

        setContentView(R.layout.activity_carte)

        val tripName = intent.getStringExtra("trip_name") ?: "Mon voyage"
        supportActionBar?.title = tripName

        val menuIcon = findViewById<ImageView>(R.id.imageMenu)

        menuIcon.setOnClickListener {
            val popup = PopupMenu(this, it)
            popup.menuInflater.inflate(R.menu.menu_main, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {

                    R.id.personalise -> {
                        Toast.makeText(this, "voyage personalisé", Toast.LENGTH_SHORT).show()
                        true
                    }

                    R.id.delete_trip -> {
                        poiList.clear()
                        map.overlays.clear()
                        map.overlays.add(locationOverlay) // garder GPS
                        Toast.makeText(this, "Trajet supprimé", Toast.LENGTH_SHORT).show()
                        true
                    }

                    else -> false
                }
            }

            popup.show()
        }

        val recycler = findViewById<RecyclerView>(R.id.recyclerSuggestions)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = SuggestionsAdapter(emptyList()) { selected ->
            searchLocation(selected)
        }

        map = findViewById(R.id.map)
        map.setMultiTouchControls(true)

        Places.initialize(applicationContext, "TAIzaSyBYhdnVYa3T85ngWsTZ6VfOYz_QXbEYBjE")
        placesClient = Places.createClient(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                requestlocation
            )
        } else {
            setupMap()
        }

        val searchView = findViewById<SearchView>(R.id.searchView)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) searchLocation(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) getSuggestions(newText)
                return true
            }
        })

        findViewById<Button>(R.id.btnAddPoint).setOnClickListener {
            lastSearchedPoint?.let { point ->

                val poi = POI(
                    name = lastMarker?.title ?: "Lieu",
                    geoPoint = point
                )

                poiList.add(poi)

                lastMarker?.title =
                    if (poiList.size == 1) "Départ"
                    else "Étape ${poiList.size}"

                drawRoute()

                lastSearchedPoint = null
                lastMarker = null
            }
        }
    }

    private fun setupMap() {

        locationOverlay = MyLocationNewOverlay(map)

        locationOverlay.enableMyLocation()
        locationOverlay.enableFollowLocation()
        locationOverlay.isDrawAccuracyEnabled = true

        map.overlays.add(locationOverlay)

        map.controller.setZoom(20.0)

        locationOverlay.runOnFirstFix {
            runOnUiThread {
                val userLocation = locationOverlay.myLocation
                if (userLocation != null) {
                    map.controller.setCenter(userLocation)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == requestlocation &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            setupMap()
        }
    }

    private fun openPoiEditor(poi: POI) {

        val dialog = PoiEditDialog(poi) { updatedPoi ->

            poi.name = updatedPoi.name
            poi.note = updatedPoi.note
            poi.photos = updatedPoi.photos

            // refresh map si besoin
            map.invalidate()
        }

        dialog.show(supportFragmentManager, "poi_editor")
    }

    @Suppress("DEPRECATION")
    private fun searchLocation(locationName: String) {

        val geocoder = Geocoder(this)

        try {
            val addresses = geocoder.getFromLocationName(locationName, 1)

            if (!addresses.isNullOrEmpty()) {

                val address = addresses[0]
                val geoPoint = GeoPoint(address.latitude, address.longitude)

                lastSearchedPoint = geoPoint

                lastMarker?.let { map.overlays.remove(it) }

                val marker = Marker(map)
                marker.position = geoPoint
                marker.title = locationName

                marker.setOnMarkerClickListener { m, _ ->

                    val poi = poiList.find { it.geoPoint == m.position }

                    if (poi != null) {
                        openPoiEditor(poi)
                    }

                    true
                }

                map.overlays.add(marker)

                lastMarker = marker

                map.controller.setZoom(20.0)
                map.controller.setCenter(geoPoint)

                map.invalidate()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun drawRoute() {

        routeLine?.let { map.overlays.remove(it) }

        if (poiList.size < 2) return

        routeLine = Polyline().apply {
            setPoints(poiList.map { it.geoPoint })
            outlinePaint.strokeWidth = 8f
        }

        map.overlays.add(routeLine)
    }

    private fun getSuggestions(query: String) {

        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->

                val newList = response.autocompletePredictions.map {
                    it.getFullText(null).toString()
                }

                val recycler = findViewById<RecyclerView>(R.id.recyclerSuggestions)

                recycler.adapter = SuggestionsAdapter(newList) { selected ->
                    searchLocation(selected)
                }

                recycler.visibility = View.VISIBLE
            }
    }
}

// locationOverlay.runOnFirstFix : attend que le Gps ait une position
// => execute le code quand le gps trouve position pour la premiere fois