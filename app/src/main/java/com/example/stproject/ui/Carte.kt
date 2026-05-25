package com.example.stproject.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stproject.R
import com.example.stproject.models.POI
import com.example.stproject.models.Photo
import com.example.stproject.service.LocationRecovererService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class Carte : AppCompatActivity() {

    // Carte OpenStreetMap
    private lateinit var map: MapView

    // Overlay utilisé pour récupérer et afficher la position GPS
    private lateinit var locationOverlay: MyLocationNewOverlay

    // États possibles du voyage
    private enum class TripState { IDLE, RUNNING, PAUSED }

    // État actuel du voyage
    private var tripState = TripState.IDLE

    // Liste des points GPS du trajet
    private val trackingPoints = mutableListOf<GeoPoint>()

    // Liste des POI ajoutés par l'utilisateur
    private val poiList = mutableListOf<POI>()

    // Ligne affichant le trajet sur la carte
    private var routeLine: Polyline? = null

    // Dernier point enregistré avant une pause
    private var lastPointBeforePause: GeoPoint? = null

    // Handler utilisé pour répéter les mises à jour GPS
    private val handler = Handler(Looper.getMainLooper())

    private var tripId: String? = null
    private var tripName: String? = null

    private var isSelectingPoiLocation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Chargement de la configuration osmdroid
        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )

        setContentView(R.layout.activity_carte)

        tripId = intent.getStringExtra("tripId")
        tripName = intent.getStringExtra("trip_name")

        // Récupération de la carte dans le layout
        map = findViewById(R.id.map)

        // Activation du zoom et déplacement tactile
        map.setMultiTouchControls(true)

        // Désactive la répétition infinie de la carte
        map.isHorizontalMapRepetitionEnabled = false
        map.isVerticalMapRepetitionEnabled = false

        // Limites de zoom
        map.minZoomLevel = 3.0
        map.maxZoomLevel = 20.0

        // Initialisation du GPS
        setupGPS()
        setupMapClickForPoi()

        // Initialisation des boutons
        initButtons()
    }

    private fun setupGPS() {

        // Création de l'overlay GPS
        locationOverlay = MyLocationNewOverlay(map)

        // Active la récupération de la position utilisateur
        locationOverlay.enableMyLocation()

        // Ajoute l'overlay à la carte
        map.overlays.add(locationOverlay)

        // Attend que le GPS récupère une première position
        locationOverlay.runOnFirstFix {

            runOnUiThread {

                locationOverlay.myLocation?.let {

                    // Zoom sur la position utilisateur
                    map.controller.setZoom(18.0)

                    // Centre la carte sur l'utilisateur
                    map.controller.setCenter(it)
                }
            }
        }
    }

    private fun startTrip() {

        // Passage de l'état du voyage à RUNNING
        tripState = TripState.RUNNING

        // Réinitialisation du trajet
        trackingPoints.clear()

        // Démarrage du service Android de tracking GPS
        val serviceIntent = Intent(this, LocationRecovererService::class.java)
        serviceIntent.action = "ACTION_START"
        ContextCompat.startForegroundService(this, serviceIntent)

        // Lancement de la boucle de récupération GPS
        startTrackingLoop()

        Toast.makeText(this, "Voyage démarré", Toast.LENGTH_SHORT).show()

        updateTripStatus()
    }

    private fun pauseTrip() {

        // Passage de l'état du voyage à PAUSED
        tripState = TripState.PAUSED

        // Sauvegarde du dernier point avant pause
        lastPointBeforePause = trackingPoints.lastOrNull()

        // Mise en pause du service GPS
        val serviceIntent = Intent(this, LocationRecovererService::class.java)
        serviceIntent.action = "ACTION_PAUSE"
        startService(serviceIntent)

        Toast.makeText(this, "Voyage en pause", Toast.LENGTH_SHORT).show()

        updateTripStatus()
    }

    private fun resumeTrip() {

        // Retour à l'état RUNNING
        tripState = TripState.RUNNING

        locationOverlay.myLocation?.let { loc ->

            val newPoint = GeoPoint(loc.latitude, loc.longitude)

            // Trace une ligne entre le dernier point avant pause
            // et la nouvelle position
            lastPointBeforePause?.let { old ->
                drawDashedLine(old, newPoint)
            }

            // Ajout du nouveau point au trajet
            trackingPoints.add(newPoint)
        }

        // Redémarrage de la boucle GPS
        startTrackingLoop()

        Toast.makeText(this, "Voyage repris", Toast.LENGTH_SHORT).show()

        updateTripStatus()
    }

    private fun setupMapClickForPoi() {

        val receiver = object : MapEventsReceiver {

            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {

                if (isSelectingPoiLocation && p != null) {

                    isSelectingPoiLocation = false

                    val poi = POI(
                        "POI",
                        "",
                        0,
                        p.latitude,
                        p.longitude,
                        "autre"
                    )

                    PoiEditDialog(poi) { updatedPoi ->

                        poiList.add(updatedPoi)

                        afficherPoiSurCarte(updatedPoi)

                    }.show(supportFragmentManager, "PoiEditDialog")

                    return true
                }

                return false
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        }

        map.overlays.add(MapEventsOverlay(receiver))
    }

    private fun stopTrip() {

        // Retour à l'état IDLE
        tripState = TripState.IDLE

        // Arrêt du service GPS et suppression de la notification
        val serviceIntent = Intent(this, LocationRecovererService::class.java)
        serviceIntent.action = "ACTION_STOP"
        startService(serviceIntent)

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
                btnStart.setBackgroundColor(Color.TRANSPARENT)
            }

            TripState.RUNNING -> {
                statusText.text = "● Enregistrement en cours"
                btnStart.setBackgroundColor(Color.parseColor("#C8E6C9"))
            }

            TripState.PAUSED -> {
                statusText.text = "Pause"
                btnStart.setBackgroundColor(Color.parseColor("#FFE0B2"))
            }
        }
    }

    private fun startTrackingLoop() {

        // Boucle exécutée toutes les 3 secondes
        handler.post(object : Runnable {

            override fun run() {

                // Continue uniquement si le voyage est actif
                if (tripState == TripState.RUNNING) {

                    locationOverlay.myLocation?.let { loc ->

                        // Création d'un nouveau point GPS
                        val point = GeoPoint(loc.latitude, loc.longitude)

                        // Ajout du point à la liste du trajet
                        trackingPoints.add(point)

                        // Mise à jour de la route affichée
                        drawRoute()
                    }
                }

                // Répète la boucle toutes les 3 secondes
                handler.postDelayed(this, 3000)
            }
        })
    }

    private fun drawRoute() {

        // Supprime l'ancienne ligne du trajet
        routeLine?.let { map.overlays.remove(it) }

        // Création d'une nouvelle ligne
        routeLine = Polyline().apply {

            // Ajout de tous les points GPS
            setPoints(trackingPoints)

            // Épaisseur de la ligne
            outlinePaint.strokeWidth = 24f
            outlinePaint.color = Color.parseColor("#4CAF50")
            outlinePaint.strokeCap = Paint.Cap.ROUND
            outlinePaint.strokeJoin = Paint.Join.ROUND
            outlinePaint.isAntiAlias = true
        }

        // Ajout de la ligne à la carte
        map.overlays.add(routeLine)

        // Rafraîchissement de la carte
        map.invalidate()
    }

    private fun drawDashedLine(start: GeoPoint, end: GeoPoint) {

        // Ligne entre pause et reprise du voyage
        val dashedLine = Polyline().apply {

            setPoints(listOf(start, end))

            outlinePaint.strokeWidth = 10f
            outlinePaint.color = Color.GRAY
            outlinePaint.alpha = 180
            outlinePaint.strokeCap = Paint.Cap.ROUND
        }

        map.overlays.add(dashedLine)

        map.invalidate()
    }

    private fun afficherPoiSurCarte(poi: POI) {

        val marker = Marker(map)

        marker.position = GeoPoint(
            poi.latitude,
            poi.longitude
        )

        marker.title = poi.titre

        marker.icon = ContextCompat.getDrawable(
            this,
            R.drawable.ic_poi_marker
        )

        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        map.overlays.add(marker)

        map.invalidate()
    }

    private fun afficherPhotoSurCarte(photo: Photo) {

        val marker = Marker(map)

        marker.position = GeoPoint(
            photo.latitude,
            photo.longitude
        )

        marker.title = "Photo"

        map.overlays.add(marker)

        map.invalidate()
    }

    private fun addPOI() {

        locationOverlay.myLocation?.let { loc ->

            // Création d'un POI à la position actuelle
            val poi = POI(
                "POI",
                "",
                0,
                loc.latitude,
                loc.longitude,
                "autre"
            )

            PoiEditDialog(poi) { updatedPoi ->

                poiList.add(updatedPoi)

                afficherPoiSurCarte(updatedPoi)

            }.show(supportFragmentManager, "PoiEditDialog")
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
                addPOI()
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
            addPOI()
        }

        val controlPanel = findViewById<LinearLayout>(R.id.controlPanel)

        controlPanel.setOnTouchListener(object : View.OnTouchListener {

            private var dX = 0f
            private var dY = 0f

            override fun onTouch(view: View, event: MotionEvent): Boolean {

                when (event.action) {

                    MotionEvent.ACTION_DOWN -> {
                        dX = view.x - event.rawX
                        dY = view.y - event.rawY
                    }

                    MotionEvent.ACTION_MOVE -> {
                        view.animate()
                            .x(event.rawX + dX)
                            .y(event.rawY + dY)
                            .setDuration(0)
                            .start()
                    }
                }

                return true
            }
        })

        // Bouton d'ajout de POI ou photo
        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            showAddDialog()
        }

        // Bouton START
        findViewById<ImageButton>(R.id.btnStart).setOnClickListener {

            // Si le voyage est en pause, afficher la reprise
            if (tripState == TripState.PAUSED) {
                showResumeDialog()
            } else {
                startTrip()
            }
        }

        // Bouton PAUSE
        findViewById<ImageButton>(R.id.btnPause).setOnClickListener {
            pauseTrip()
        }

        // Bouton STOP
        findViewById<ImageButton>(R.id.btnStop).setOnClickListener {
            showStopDialog()
        }
    }

    private fun showAddDialog() {

        val options = arrayOf("Ajouter POI", "Ajouter Photo")

        AlertDialog.Builder(this)
            .setTitle("Ajouter")
            .setItems(options) { _, which ->

                when (which) {

                    // Ajout d'un POI
                    0 -> addPOI()

                    // Fonction photo non encore implémentée
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

        // Fenêtre de confirmation avant reprise du voyage
        AlertDialog.Builder(this)
            .setTitle("Reprendre le voyage")
            .setMessage("Voulez-vous reprendre votre voyage ?")
            .setPositiveButton("Oui") { _, _ ->
                resumeTrip()
            }
            .setNegativeButton("Non", null)
            .show()
    }

    private fun showStopDialog() {

        // Chargement du layout personnalisé
        val dialogView = layoutInflater.inflate(R.layout.stop_save_trip, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val btnFinish = dialogView.findViewById<Button>(R.id.btnFinish)
        val btnDelete = dialogView.findViewById<Button>(R.id.btnDelete)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        // Termine le voyage
        btnFinish.setOnClickListener {

            stopTrip()

            Toast.makeText(this, "Voyage terminé", Toast.LENGTH_SHORT).show()

            dialog.dismiss()

            goToHome()
        }

        // Supprime complètement le trajet
        btnDelete.setOnClickListener {

            stopTrip()

            trackingPoints.clear()

            map.overlays.clear()

            map.overlays.add(locationOverlay)

            map.invalidate()

            Toast.makeText(this, "Voyage supprimé", Toast.LENGTH_SHORT).show()

            dialog.dismiss()

            goToHome()
        }

        // Ferme simplement la fenêtre
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}