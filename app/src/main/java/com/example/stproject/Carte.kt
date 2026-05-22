package com.example.stproject

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.stproject.models.POI
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

// Imports utilisés pour lancer et contrôler le service de tracking
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.stproject.service.LocationRecovererService

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Chargement de la configuration osmdroid
        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )

        setContentView(R.layout.activity_carte)

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
    }

    private fun stopTrip() {

        // Retour à l'état IDLE
        tripState = TripState.IDLE

        // Arrêt du service GPS et suppression de la notification
        val serviceIntent = Intent(this, LocationRecovererService::class.java)
        serviceIntent.action = "ACTION_STOP"
        startService(serviceIntent)
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
            outlinePaint.strokeWidth = 6f
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

            outlinePaint.strokeWidth = 6f
        }

        map.overlays.add(dashedLine)

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

            // Ajout du POI dans la liste
            poiList.add(poi)

            // Création du marqueur sur la carte
            val marker = Marker(map)

            // Position du marqueur
            marker.position = GeoPoint(
                poi.latitude,
                poi.longitude
            )

            // Nom affiché lors du clic sur le marqueur
            marker.title = poi.titre

            // Ajout du marqueur sur la carte
            map.overlays.add(marker)

            // Rafraîchissement de la carte
            map.invalidate()
        }
    }

    private fun initButtons() {

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

        androidx.appcompat.app.AlertDialog.Builder(this)
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
        androidx.appcompat.app.AlertDialog.Builder(this)
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

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
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
        }

        // Ferme simplement la fenêtre
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
// => execute le code quand le gps trouve position pour la premiere fois