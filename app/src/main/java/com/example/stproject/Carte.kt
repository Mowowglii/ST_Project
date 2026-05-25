package com.example.stproject

import android.os.Bundle
import android.content.Intent
//import android.os.Handler
//import android.os.Looper
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
//import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import com.example.stproject.service.LocationRecovererService
import com.example.stproject.service.POIManager
import com.example.stproject.models.POI

class Carte : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay

    // État du voyage : arrêté, en cours ou en pause
    private enum class TripState { IDLE, RUNNING, PAUSED }
    private var tripState = TripState.IDLE

    // Liste des points GPS enregistrés pendant le trajet
//    private val trackingPoints = mutableListOf<GeoPoint>()

    // Liste des points d'intérêt (POI) ajoutés par l'utilisateur
    private val poiList = mutableListOf<POI>()

    // Ligne représentant le trajet sur la carte
//    private var routeLine: Polyline? = null

    // Dernier point enregistré avant une pause du trajet
//    private var lastPointBeforePause: GeoPoint? = null

    // Handler utilisé pour exécuter un suivi GPS périodique
//    private val handler = Handler(Looper.getMainLooper())

    // Mathushan :
    private var tripId : String? = null
    private var tripName : String? = null

    private lateinit var  poiManager: POIManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Chargement de la configuration osmdroid
        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )

        setContentView(R.layout.activity_carte)

        // /Mathushan :
        tripId = intent.getStringExtra("tripId")
        tripName = intent.getStringExtra("trip_name")
        poiManager = POIManager()
        // ? c'est une condition qui dit si trip id est différent de null alors faire ça
        tripId?.let {
            poiManager.definirVoyageActif(tripId)
        }

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
        chargerPOIDuVoyage()
        initButtons()
    }

    private fun sendLocationServiceAction(action : String) {
        val intent = Intent(this, LocationRecovererService::class.java)
        intent.action = action
        startService(intent)
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
//        trackingPoints.clear()

//        startTrackingLoop()
        Toast.makeText(this, "Voyage démarré", Toast.LENGTH_SHORT).show()
    }

    // Mise en pause du trajet
    private fun pauseTrip() {
        tripState = TripState.PAUSED
        //lastPointBeforePause = trackingPoints.lastOrNull()
        Toast.makeText(this, "Voyage en pause", Toast.LENGTH_SHORT).show()
    }

    // Reprise du trajet après une pause
    private fun resumeTrip() {
        tripState = TripState.RUNNING

//        locationOverlay.myLocation?.let { loc ->
//            val newPoint = GeoPoint(loc.latitude, loc.longitude)
//
//            // Dessine une ligne entre la dernière position et la reprise
//            lastPointBeforePause?.let { old ->
//                drawDashedLine(old, newPoint)
//            }
//
//            trackingPoints.add(newPoint)
//        }
//
//        startTrackingLoop()
        Toast.makeText(this, "Voyage repris", Toast.LENGTH_SHORT).show()
    }

    // Arrêt du trajet
    private fun stopTrip() {
        tripState = TripState.IDLE
        stopService(Intent(this, LocationRecovererService::class.java))
    }

    // Boucle de suivi GPS toutes les 3 secondes
//    private fun startTrackingLoop() {
//        handler.post(object : Runnable {
//            override fun run() {
//
//                if (tripState == TripState.RUNNING) {
//
//                    locationOverlay.myLocation?.let { loc ->
//                        val point = GeoPoint(loc.latitude, loc.longitude)
//
//                        trackingPoints.add(point)
//                        drawRoute()
//                    }
//                }
//
//                handler.postDelayed(this, 3000)
//            }
//        })
//    }

    // Dessine le trajet complet sur la carte
//    private fun drawRoute() {
//        routeLine?.let { map.overlays.remove(it) }
//
//        routeLine = Polyline().apply {
//            setPoints(trackingPoints)
//            outlinePaint.strokeWidth = 6f
//        }
//
//        map.overlays.add(routeLine)
//        map.invalidate()
//    }

    // Dessine une ligne en pointillés entre deux positions
//    private fun drawDashedLine(start: GeoPoint, end: GeoPoint) {
//        val dashedLine = Polyline().apply {
//            setPoints(listOf(start, end))
//            outlinePaint.strokeWidth = 6f
//        }
//
//        map.overlays.add(dashedLine)
//        map.invalidate()
//    }

    // Ajout d’un point d’intérêt sur la carte
    // Mathushan :

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

    private fun addPOI(titre: String) {
        // vérifie si la position existe
        locationOverlay.myLocation?.let { loc ->

            poiManager.ajouterPOI(
                titre,
                "",
                0,
                loc.latitude,
                loc.longitude,
                "default",
                object : POIManager.AjoutPOICallback {

                    override fun onSuccess(poi: POI) {
                        afficherMarkerPOI(poi)
//                        val marker = Marker(map)
//
//                        marker.position = GeoPoint(
//                            poi.latitude,
//                            poi.longitude
//                        )
//
//                        marker.title = poi.titre
//                        // avant le poi ne contenait que le titre mais maintenant ça contient l'objet POI.
//                        marker.relatedObject = poi
//                        configurerClickMarker(marker)
//
//                        map.overlays.add(marker)
//                        map.invalidate()

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

        val input = android.widget.EditText(this)

        input.hint = "Nouveau titre"
        input.setText(poi.titre)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Modifier le POI")
            .setView(input)

            .setPositiveButton("Modifier") { _, _ ->

                val nouveauTitre = input.text.toString().trim()

                poiManager.modifierPOI(
                    poi,
                    nouveauTitre,
                    null,
                    null,
                    null,

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
            }

            .setNegativeButton("Annuler", null)

            .show()
    }

    private fun supprimerPOI(poi : POI, marker: Marker) {
        poiManager.supprimerPOI(
            poi,
            object : POIManager.AjoutPOICallback {
                override fun onSuccess(poi: POI) {
                    map.overlays.remove(marker)
                    map.invalidate()

                    Toast.makeText(this@Carte,
                        "POI supprimé",
                        Toast.LENGTH_SHORT).show()
                }

                override fun onError(message: String) {
                    Toast.makeText(this@Carte,
                        message,
                        Toast.LENGTH_SHORT).show()

                }
            }
        )
    }
    private fun showDetailsPOIDialog(poi: POI, marker: Marker) {

        androidx.appcompat.app.AlertDialog.Builder(this)
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
//    private fun addPOI() {
//        locationOverlay.myLocation?.let { loc ->
//
//            val poi = POI("POI", GeoPoint(loc.latitude, loc.longitude))
//            poiList.add(poi)
//
//            val marker = Marker(map)
//            marker.position = poi.geoPoint
//            marker.title = poi.name
//
//            map.overlays.add(marker)
//            map.invalidate()
//        }
//    }
//    private fun addPOI() {
//
//        locationOverlay.myLocation?.let { loc ->
//
//            // Création du marqueur sur la carte
//            val marker = Marker(map)
//
//            marker.position = GeoPoint(
//                loc.latitude,
//                loc.longitude
//            )
//
//            marker.title = "POI"
//
//            // Ajout du marqueur à la carte
//            map.overlays.add(marker)
//
//            // Rafraîchit la carte
//            map.invalidate()
//        }
//    }

    // Initialisation des boutons de l’interface
    private fun initButtons() {

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            showAddPOIDialog()
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

    // Boîte de dialogue pour ajouter un élément (POI ou photo)
//    private fun showAddDialog() {
//        val options = arrayOf("Ajouter POI", "Ajouter Photo")
//
//        androidx.appcompat.app.AlertDialog.Builder(this)
//            .setTitle("Ajouter")
//            .setItems(options) { _, which ->
//                when (which) {
//                    0 -> addPOI()
//                    1 -> Toast.makeText(this, "Photo à implémenter", Toast.LENGTH_SHORT).show()
//                }
//            }
//            .show()
//    }

    private fun showAddPOIDialog() {
        val input = android.widget.EditText(this)
        input.hint = "Nom du POI"

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Ajouter un POI")
            .setView(input)
            .setPositiveButton("Ajouter") { _, _ ->
                val titre = input.text.toString().trim()
                addPOI(titre)
            }
            .setNegativeButton("Annuler", null)
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
//            trackingPoints.clear()
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