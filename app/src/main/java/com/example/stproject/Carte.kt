package com.example.stproject

import com.example.stproject.models.POI
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class Carte : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay

    // État du voyage : arrêté, en cours ou en pause
    private enum class TripState { IDLE, RUNNING, PAUSED }
    private var tripState = TripState.IDLE

    // Liste des points GPS enregistrés pendant le trajet
    private val trackingPoints = mutableListOf<GeoPoint>()

    // Liste des points d'intérêt (POI) ajoutés par l'utilisateur
    private val poiList = mutableListOf<POI>()

    // Ligne représentant le trajet sur la carte
    private var routeLine: Polyline? = null

    // Dernier point enregistré avant une pause du trajet
    private var lastPointBeforePause: GeoPoint? = null

    // Handler utilisé pour exécuter un suivi GPS périodique
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Chargement de la configuration osmdroid
        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )

        setContentView(R.layout.activity_carte)

        // Initialisation de la carte
        map = findViewById(R.id.map)
        map.setMultiTouchControls(true)

        // Désactivation de la répétition de la carte pour éviter les duplications du monde
        map.isHorizontalMapRepetitionEnabled = false
        map.isVerticalMapRepetitionEnabled = false

        // Limitation du niveau de zoom pour éviter un dézoom excessif
        map.minZoomLevel = 3.0
        map.maxZoomLevel = 20.0

        setupGPS()
        initButtons()
    }

    // Initialisation de la localisation GPS utilisateur
    private fun setupGPS() {
        locationOverlay = MyLocationNewOverlay(map)
        locationOverlay.enableMyLocation()

        map.overlays.add(locationOverlay)

        // Centrage automatique sur la position de l'utilisateur lors du premier fix GPS
        locationOverlay.runOnFirstFix {
            runOnUiThread {
                locationOverlay.myLocation?.let {
                    map.controller.setZoom(18.0)
                    map.controller.setCenter(it)
                }
            }
        }
    }

    // Démarrage d’un nouveau trajet
    private fun startTrip() {
        tripState = TripState.RUNNING
        trackingPoints.clear()

        startTrackingLoop()
        Toast.makeText(this, "Voyage démarré", Toast.LENGTH_SHORT).show()
    }

    // Mise en pause du trajet
    private fun pauseTrip() {
        tripState = TripState.PAUSED
        lastPointBeforePause = trackingPoints.lastOrNull()
        Toast.makeText(this, "Voyage en pause", Toast.LENGTH_SHORT).show()
    }

    // Reprise du trajet après une pause
    private fun resumeTrip() {
        tripState = TripState.RUNNING

        locationOverlay.myLocation?.let { loc ->
            val newPoint = GeoPoint(loc.latitude, loc.longitude)

            // Dessine une ligne entre la dernière position et la reprise
            lastPointBeforePause?.let { old ->
                drawDashedLine(old, newPoint)
            }

            trackingPoints.add(newPoint)
        }

        startTrackingLoop()
        Toast.makeText(this, "Voyage repris", Toast.LENGTH_SHORT).show()
    }

    // Arrêt du trajet
    private fun stopTrip() {
        tripState = TripState.IDLE
    }

    // Boucle de suivi GPS toutes les 3 secondes
    private fun startTrackingLoop() {
        handler.post(object : Runnable {
            override fun run() {

                if (tripState == TripState.RUNNING) {

                    locationOverlay.myLocation?.let { loc ->
                        val point = GeoPoint(loc.latitude, loc.longitude)

                        trackingPoints.add(point)
                        drawRoute()
                    }
                }

                handler.postDelayed(this, 3000)
            }
        })
    }

    // Dessine le trajet complet sur la carte
    private fun drawRoute() {
        routeLine?.let { map.overlays.remove(it) }

        routeLine = Polyline().apply {
            setPoints(trackingPoints)
            outlinePaint.strokeWidth = 6f
        }

        map.overlays.add(routeLine)
        map.invalidate()
    }

    // Dessine une ligne en pointillés entre deux positions
    private fun drawDashedLine(start: GeoPoint, end: GeoPoint) {
        val dashedLine = Polyline().apply {
            setPoints(listOf(start, end))
            outlinePaint.strokeWidth = 6f
        }

        map.overlays.add(dashedLine)
        map.invalidate()
    }

    // Ajout d’un point d’intérêt sur la carte
    private fun addPOI() {
        locationOverlay.myLocation?.let { loc ->
            val poi = POI("POI", "Description", 0, loc.latitude, loc.longitude, tripId
            )
            poiList.add(poi)

            val marker = Marker(map)
            marker.position = GeoPoint(
                poi.getLatitude(),
                poi.getLongitude()
            )
            marker.title = poi.name

            map.overlays.add(marker)
            map.invalidate()
        }
    }

    // Initialisation des boutons de l’interface
    private fun initButtons() {

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            showAddDialog()
        }

        findViewById<ImageButton>(R.id.btnStart).setOnClickListener {
            if (tripState == TripState.PAUSED) {
                showResumeDialog()
            } else {
                startTrip()
            }
        }

        findViewById<ImageButton>(R.id.btnPause).setOnClickListener {
            pauseTrip()
        }

        findViewById<ImageButton>(R.id.btnStop).setOnClickListener {
            showStopDialog()
        }
    }

    // Boîte de dialogue pour ajouter un élément (POI ou photo)
    private fun showAddDialog() {
        val options = arrayOf("Ajouter POI", "Ajouter Photo")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Ajouter")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> addPOI()
                    1 -> Toast.makeText(this, "Photo à implémenter", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    // Demande confirmation pour reprendre le voyage
    private fun showResumeDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Reprendre le voyage")
            .setMessage("Voulez-vous reprendre votre voyage ?")
            .setPositiveButton("Oui") { _, _ -> resumeTrip() }
            .setNegativeButton("Non", null)
            .show()
    }

    // Boîte de dialogue de fin de trajet (sauvegarde ou suppression)
    private fun showStopDialog() {

        val dialogView = layoutInflater.inflate(R.layout.stop_save_trip, null)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val btnFinish = dialogView.findViewById<Button>(R.id.btnFinish)
        val btnDelete = dialogView.findViewById<Button>(R.id.btnDelete)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        btnFinish.setOnClickListener {
            stopTrip()
            Toast.makeText(this, "Voyage terminé", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnDelete.setOnClickListener {
            stopTrip()
            trackingPoints.clear()
            map.overlays.clear()
            map.overlays.add(locationOverlay)
            map.invalidate()

            Toast.makeText(this, "Voyage supprimé", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
// locationOverlay.runOnFirstFix : attend que le Gps ait une position
// => execute le code quand le gps trouve position pour la premiere fois