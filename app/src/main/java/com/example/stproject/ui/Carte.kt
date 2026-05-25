package com.example.stproject.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stproject.Manager.POIManager
import com.example.stproject.R
import com.example.stproject.models.POI
import com.example.stproject.service.LocationRecovererService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class Carte : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay

    private enum class TripState { IDLE, RUNNING, PAUSED }
    private var tripState = TripState.IDLE

    private val poiList = mutableListOf<POI>()

    private var tripId: String? = null
    private var tripName: String? = null

    private lateinit var poiManager: POIManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )

        setContentView(R.layout.activity_carte)

        tripId = intent.getStringExtra("tripId")
        tripName = intent.getStringExtra("trip_name")

        poiManager = POIManager()

        tripId?.let {
            poiManager.definirVoyageActif(tripId)
        }

        map = findViewById(R.id.map)
        map.setMultiTouchControls(true)

        map.isHorizontalMapRepetitionEnabled = false
        map.isVerticalMapRepetitionEnabled = false

        map.minZoomLevel = 3.0
        map.maxZoomLevel = 20.0

        setupGPS()
        chargerPOIDuVoyage()
        initButtons()
        updateTripStatus()
    }

    private fun sendLocationServiceAction(action: String) {
        val intent = Intent(this, LocationRecovererService::class.java)
        intent.action = action
        startService(intent)
    }

    private fun setupGPS() {
        locationOverlay = MyLocationNewOverlay(map)
        locationOverlay.enableMyLocation()

        map.overlays.add(locationOverlay)

        locationOverlay.runOnFirstFix {
            runOnUiThread {
                locationOverlay.myLocation?.let {
                    map.controller.setZoom(18.0)
                    map.controller.setCenter(it)
                }
            }
        }
    }

    private fun startTrip() {
        tripState = TripState.RUNNING
        Toast.makeText(this, "Voyage démarré", Toast.LENGTH_SHORT).show()
        updateTripStatus()
    }

    private fun pauseTrip() {
        tripState = TripState.PAUSED
        Toast.makeText(this, "Voyage en pause", Toast.LENGTH_SHORT).show()
        updateTripStatus()
    }

    private fun resumeTrip() {
        tripState = TripState.RUNNING
        Toast.makeText(this, "Voyage repris", Toast.LENGTH_SHORT).show()
        updateTripStatus()
    }

    private fun stopTrip() {
        tripState = TripState.IDLE
        stopService(Intent(this, LocationRecovererService::class.java))
        updateTripStatus()
    }

    private fun goToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun updateTripStatus() {
        val statusText = findViewById<TextView>(R.id.tripStatusText)
        val btnStart = findViewById<ImageButton>(R.id.btnStart)

        when (tripState) {
            TripState.IDLE -> {
                statusText.text = "Prêt"
                btnStart.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }

            TripState.RUNNING -> {
                statusText.text = "● Enregistrement en cours"
                btnStart.setBackgroundColor(android.graphics.Color.parseColor("#C8E6C9"))
            }

            TripState.PAUSED -> {
                statusText.text = "Pause"
                btnStart.setBackgroundColor(android.graphics.Color.parseColor("#FFE0B2"))
            }
        }
    }

    private fun afficherMarkerPOI(poi: POI) {
        val marker = Marker(map)

        marker.position = GeoPoint(
            poi.latitude,
            poi.longitude
        )

        marker.title = poi.titre
        marker.relatedObject = poi

        configurerClickMarker(marker)

        map.overlays.add(marker)
        map.invalidate()
    }

    private fun addPOIWithEditDialog() {
        locationOverlay.myLocation?.let { loc ->

            val poi = POI(
                "POI",
                "",
                0,
                loc.latitude,
                loc.longitude,
                "autre"
            )

            PoiEditDialog(poi) { updatedPoi ->

                poiManager.ajouterPOI(
                    updatedPoi.titre,
                    updatedPoi.description,
                    updatedPoi.note,
                    updatedPoi.latitude,
                    updatedPoi.longitude,
                    updatedPoi.type,
                    object : POIManager.AjoutPOICallback {

                        override fun onSuccess(poi: POI) {
                            poiList.add(poi)
                            afficherMarkerPOI(poi)

                            Toast.makeText(
                                this@Carte,
                                "POI ajouté",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onError(message: String) {
                            Toast.makeText(
                                this@Carte,
                                message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }.show(supportFragmentManager, "PoiEditDialog")

        } ?: Toast.makeText(
            this,
            "Position GPS indisponible",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun configurerClickMarker(marker: Marker) {
        marker.setOnMarkerClickListener { clickedMarker, _ ->
            val poiClique = clickedMarker.relatedObject as? POI

            if (poiClique != null) {
                showDetailsPOIDialog(poiClique, clickedMarker)
            }

            true
        }
    }

    private fun showModifierPOIDialog(poi: POI, marker: Marker) {
        PoiEditDialog(poi) { updatedPoi ->

            poiManager.modifierPOI(
                poi,
                updatedPoi.titre,
                updatedPoi.description,
                updatedPoi.type,
                updatedPoi.note,
                object : POIManager.AjoutPOICallback {

                    override fun onSuccess(poiModifie: POI) {
                        marker.title = poiModifie.titre
                        marker.relatedObject = poiModifie

                        map.invalidate()

                        Toast.makeText(
                            this@Carte,
                            "POI modifié",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onError(message: String) {
                        Toast.makeText(
                            this@Carte,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }.show(supportFragmentManager, "PoiEditDialog")
    }

    private fun supprimerPOI(poi: POI, marker: Marker) {
        poiManager.supprimerPOI(
            poi,
            object : POIManager.AjoutPOICallback {

                override fun onSuccess(poi: POI) {
                    poiList.remove(poi)
                    map.overlays.remove(marker)
                    map.invalidate()

                    Toast.makeText(
                        this@Carte,
                        "POI supprimé",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onError(message: String) {
                    Toast.makeText(
                        this@Carte,
                        message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun showDetailsPOIDialog(poi: POI, marker: Marker) {
        AlertDialog.Builder(this)
            .setTitle(poi.titre)
            .setMessage(
                "Description : ${poi.description}\n" +
                        "Note : ${poi.note}\n" +
                        "Type : ${poi.type}"
            )
            .setPositiveButton("Modifier") { _, _ ->
                showModifierPOIDialog(poi, marker)
            }
            .setNegativeButton("Supprimer") { _, _ ->
                supprimerPOI(poi, marker)
            }
            .setNeutralButton("Fermer", null)
            .show()
    }

    private fun chargerPOIDuVoyage() {
        tripId?.let { voyageId ->

            poiManager.recupererPOIDuVoyage(
                voyageId,
                object : POIManager.ListePOICallback {

                    override fun onSuccess(pois: List<POI>) {
                        poiList.clear()
                        poiList.addAll(pois)

                        for (poi in pois) {
                            afficherMarkerPOI(poi)
                        }
                    }

                    override fun onError(message: String) {
                        Toast.makeText(
                            this@Carte,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }
    }

    private fun initButtons() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val menuRecyclerView = findViewById<RecyclerView>(R.id.menuRecyclerView)
        val menuTitle = findViewById<TextView>(R.id.menuTitle)
        val menuAddButton = findViewById<Button>(R.id.menuAddButton)

        menuRecyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<ImageView>(R.id.imageMenu).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        findViewById<TextView>(R.id.tabPoi).setOnClickListener {
            menuTitle.text = "Mes POI"
            menuAddButton.text = "+ Ajouter un POI"

            menuRecyclerView.adapter = SuggestionsAdapter(
                poiList.map { it.titre }
            ) { selectedPoi ->
                Toast.makeText(this, selectedPoi, Toast.LENGTH_SHORT).show()
            }

            menuAddButton.setOnClickListener {
                addPOIWithEditDialog()
            }
        }

        findViewById<TextView>(R.id.tabPhotos).setOnClickListener {
            menuTitle.text = "Mes Photos"
            menuAddButton.text = "+ Ajouter une photo"

            menuRecyclerView.adapter = SuggestionsAdapter(
                listOf("Photos à charger depuis le backend")
            ) { selectedPhoto ->
                Toast.makeText(this, selectedPhoto, Toast.LENGTH_SHORT).show()
            }

            menuAddButton.setOnClickListener {
                Toast.makeText(this, "Ajout photo à implémenter", Toast.LENGTH_SHORT).show()
            }
        }

        menuAddButton.setOnClickListener {
            addPOIWithEditDialog()
        }

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            showAddDialog()
        }

        findViewById<ImageButton>(R.id.btnStart).setOnClickListener {
            sendLocationServiceAction("ACTION_START")

            if (tripState == TripState.PAUSED) {
                showResumeDialog()
            } else {
                startTrip()
            }
        }

        findViewById<ImageButton>(R.id.btnPause).setOnClickListener {
            sendLocationServiceAction("ACTION_PAUSE")
            pauseTrip()
        }

        findViewById<ImageButton>(R.id.btnStop).setOnClickListener {
            stopTrip()
            showStopDialog()
        }
    }

    private fun showAddDialog() {
        val options = arrayOf("Ajouter POI", "Ajouter Photo")

        AlertDialog.Builder(this)
            .setTitle("Ajouter")
            .setItems(options) { _, which ->

                when (which) {
                    0 -> addPOIWithEditDialog()

                    1 -> Toast.makeText(
                        this,
                        "Photo à implémenter",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .show()
    }

    private fun showResumeDialog() {
        AlertDialog.Builder(this)
            .setTitle("Reprendre le voyage")
            .setMessage("Voulez-vous reprendre votre voyage ?")
            .setPositiveButton("Oui") { _, _ -> resumeTrip() }
            .setNegativeButton("Non", null)
            .show()
    }

    private fun showStopDialog() {
        val dialogView = layoutInflater.inflate(R.layout.stop_save_trip, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val btnFinish = dialogView.findViewById<Button>(R.id.btnFinish)
        val btnDelete = dialogView.findViewById<Button>(R.id.btnDelete)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        btnFinish.setOnClickListener {
            stopTrip()
            Toast.makeText(this, "Voyage terminé", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            goToHome()
        }

        btnDelete.setOnClickListener {
            stopTrip()
            map.overlays.clear()
            map.overlays.add(locationOverlay)
            map.invalidate()

            Toast.makeText(this, "Voyage supprimé", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            goToHome()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}