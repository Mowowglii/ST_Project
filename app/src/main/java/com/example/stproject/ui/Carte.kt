package com.example.stproject.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stproject.Manager.POIManager
import com.example.stproject.R
import com.example.stproject.data.CloudFirestoreCommunicator
import com.example.stproject.models.POI
import com.example.stproject.service.LocationRecovererService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.stproject.data.LocationRepository
import kotlinx.coroutines.launch
import android.location.Location
import com.example.stproject.utils.ReductionListPoint
import com.google.firebase.firestore.FirebaseFirestore
import androidx.activity.result.contract.ActivityResultContracts
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay
import com.example.stproject.Manager.PhotoManager

class Carte : AppCompatActivity() {

    // Carte OpenStreetMap
    private lateinit var map: MapView

    // Overlay utilisé pour récupérer et afficher la position GPS
    private lateinit var locationOverlay: MyLocationNewOverlay

    // États possibles du voyage
    private enum class TripState { IDLE, RUNNING, PAUSED }

    // État actuel du voyage
    private var tripState = TripState.IDLE

    // placement libre POI
    private var isSelectingPoiLocation = false

    // Liste des POI ajoutés ou récupérés pour le voyage actuel
    private val poiList = mutableListOf<POI>()

    // Identifiants du voyage transmis depuis MainActivity ou Consultation
    private var tripId: String? = null
    private var tripName: String? = null

    // Ligne utilisée pour afficher le tracé du voyage
    private var routeLine: Polyline? = null

    // Manager utilisé pour gérer les POI côté backend
    private lateinit var poiManager: POIManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Chargement de la configuration osmdroid
        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )

        setContentView(R.layout.activity_carte)

        // Récupération des informations du voyage
        tripId = intent.getStringExtra("tripId")
        tripName = intent.getStringExtra("trip_name")

        // Initialisation du manager POI
        poiManager = POIManager()

        // Définit le voyage actif pour les ajouts, modifications et suppressions de POI
        tripId?.let {
            poiManager.definirVoyageActif(it)
        }

        // Initialisation de la carte
        map = findViewById(R.id.map)
        map.setMultiTouchControls(true)

        // Désactive la répétition infinie de la carte
        map.isHorizontalMapRepetitionEnabled = false
        map.isVerticalMapRepetitionEnabled = false

        // Limites de zoom
        map.minZoomLevel = 3.0
        map.maxZoomLevel = 20.0

        setupGPS()
        setupMapClickForPoi()
        chargerPOIDuVoyage()
        chargerTraceVoyage()
        observerTraceTempsReel()
        initButtons()
        updateTripStatus()
    }

    // Envoie une action au service de localisation
    private fun sendLocationServiceAction(action: String) {
        val intent = Intent(this, LocationRecovererService::class.java)
        intent.action = action
        startService(intent)
    }

    private fun marquerVoyageCommeTermine() {
        tripId?.let { voyageId ->
            FirebaseFirestore.getInstance()
                .collection("voyages")
                .document(voyageId)
                .update("termine", true)
        }
    }

    // Initialisation de la localisation GPS utilisateur
    private fun setupGPS() {
        locationOverlay = MyLocationNewOverlay(map)
        locationOverlay.enableMyLocation()
        map.overlays.add(locationOverlay)

        // Centre la carte sur la première position GPS disponible
        locationOverlay.runOnFirstFix {
            runOnUiThread {
                locationOverlay.myLocation?.let { position ->
                    map.controller.setZoom(18.0)
                    map.controller.setCenter(position)
                }
            }
        }
    }

    // pour mettre un poi

    private fun setupMapClickForPoi() {
        val receiver = object : MapEventsReceiver {

            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                if (isSelectingPoiLocation && p != null) {
                    isSelectingPoiLocation = false
                    ouvrirFormulairePoi(p.latitude, p.longitude)
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

    // Démarrage du voyage
    private fun startTrip() {
        tripState = TripState.RUNNING
        Toast.makeText(this, "Voyage démarré", Toast.LENGTH_SHORT).show()
        updateTripStatus()
    }

    // Charge le tracé déjà enregistré du voyage depuis Firestore
    private fun chargerTraceVoyage() {
        tripId?.let { voyageId ->

            val communicator = CloudFirestoreCommunicator()

            communicator.recuperer_path_voyage(
                voyageId,
                object : CloudFirestoreCommunicator.PathCallback {

                    override fun onComplete(path: List<Map<String, Any>>) {
                        val locations = mutableListOf<Location>()

                        for (point in path) {
                            val lat = point["latitude"] as? Double
                            val lon = point["longitude"] as? Double

                            if (lat != null && lon != null) {
                                val location = Location("firestore")
                                location.latitude = lat
                                location.longitude = lon
                                locations.add(location)
                            }
                        }

                        val locationsReduites =
                            ReductionListPoint.douglasPeucker(locations)

                        val points = locationsReduites.map { location ->
                            GeoPoint(location.latitude, location.longitude)
                        }

                        if (points.isNotEmpty()) {
                            routeLine?.let {
                                map.overlays.remove(it)
                            }

                            routeLine = Polyline().apply {
                                setPoints(points)
                                styliserTrace(this)
                            }

                            map.overlays.add(routeLine)
                            map.invalidate()
                        }
                    }

                    override fun onError(error: String) {
                        Toast.makeText(
                            this@Carte,
                            error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }
    }

    private fun observerTraceTempsReel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                LocationRepository.currentPath.collect { locations ->

                    if (locations.isEmpty()) return@collect

                    val points = locations.map { location ->
                        GeoPoint(location.latitude, location.longitude)
                    }

                    if (routeLine == null) {
                        routeLine = Polyline().apply {
                            styliserTrace(this)
                        }

                        map.overlays.add(routeLine)
                    }

                    routeLine?.setPoints(points)

                    val lastLocation = locations.last()
                    val lastPoint = GeoPoint(
                        lastLocation.latitude,
                        lastLocation.longitude
                    )

                    map.controller.animateTo(lastPoint)

                    map.invalidate()
                }
            }
        }
    }

    // Mise en pause du voyage
    private fun pauseTrip() {
        tripState = TripState.PAUSED
        Toast.makeText(this, "Voyage en pause", Toast.LENGTH_SHORT).show()
        updateTripStatus()
    }

    // Reprise du voyage après une pause
    private fun resumeTrip() {
        tripState = TripState.RUNNING
        Toast.makeText(this, "Voyage repris", Toast.LENGTH_SHORT).show()
        updateTripStatus()
    }

    // Arrêt du voyage
    private fun stopTrip() {
        tripState = TripState.IDLE
        stopService(Intent(this, LocationRecovererService::class.java))
        updateTripStatus()
    }

    // Retour vers la page d'accueil
    private fun goToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    // Met à jour le texte et la couleur du bouton selon l'état du voyage
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

    // Applique le style visuel du tracé du voyage
    private fun styliserTrace(trace: Polyline) {
        trace.outlinePaint.strokeWidth = 24f
        trace.outlinePaint.color = Color.parseColor("#4CAF50")
        trace.outlinePaint.strokeCap = Paint.Cap.ROUND
        trace.outlinePaint.strokeJoin = Paint.Join.ROUND
        trace.outlinePaint.isAntiAlias = true
    }

    // Affiche un marqueur POI sur la carte
    private fun afficherMarkerPOI(poi: POI) {
        val marker = Marker(map)

        marker.position = GeoPoint(
            poi.latitude,
            poi.longitude
        )

        marker.title = poi.titre
        marker.relatedObject = poi

        marker.icon = ContextCompat.getDrawable(
            this,
            R.drawable.ic_poi_marker
        )

        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        configurerClickMarker(marker)

        map.overlays.add(marker)
        map.invalidate()
    }

    // Recharge la liste des POI affichée dans le menu latéral
    private fun refreshPoiMenu() {
        val menuRecyclerView = findViewById<RecyclerView>(R.id.menuRecyclerView)
        val menuTitle = findViewById<TextView>(R.id.menuTitle)
        val menuAddButton = findViewById<Button>(R.id.menuAddButton)

        menuTitle.text = "Mes POI"
        menuAddButton.text = "+ Ajouter un POI"

        menuRecyclerView.adapter = PoiAdapter(poiList) { poi ->
            Toast.makeText(this, poi.titre, Toast.LENGTH_SHORT).show()
        }
    }

    // Ouvre le formulaire de création d'un POI
    private fun addPOIWithEditDialog() {
        isSelectingPoiLocation = true

        Toast.makeText(
            this,
            "Touchez la carte pour placer le POI",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun ouvrirFormulairePoi(latitude: Double, longitude: Double) {
        val poi = POI(
            "POI",
            "",
            0,
            latitude,
            longitude,
            "autre"
        )

        PoiEditDialog(poi) { updatedPoi ->

            tripId?.let {
                poiManager.definirVoyageActif(it)
            }

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
                        refreshPoiMenu()

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
    }

    // Configure le clic sur un marqueur POI
    private fun configurerClickMarker(marker: Marker) {
        marker.setOnMarkerClickListener { clickedMarker, _ ->
            val poiClique = clickedMarker.relatedObject as? POI

            if (poiClique != null) {
                showDetailsPOIDialog(poiClique, clickedMarker)
            }

            true
        }
    }

    // Ouvre le formulaire de modification d'un POI
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
                        refreshPoiMenu()

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

    // Supprime un POI du backend et de la carte
    private fun supprimerPOI(poi: POI, marker: Marker) {
        poiManager.supprimerPOI(
            poi,
            object : POIManager.AjoutPOICallback {

                override fun onSuccess(poi: POI) {
                    poiList.remove(poi)
                    map.overlays.remove(marker)
                    map.invalidate()
                    refreshPoiMenu()

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

    // Affiche les détails d'un POI sélectionné
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

    // Charge les POI du voyage actuel depuis Firestore
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

                        refreshPoiMenu()
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

    // Initialisation des boutons de l'interface
    private fun initButtons() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val menuRecyclerView = findViewById<RecyclerView>(R.id.menuRecyclerView)
        val menuTitle = findViewById<TextView>(R.id.menuTitle)
        val menuAddButton = findViewById<Button>(R.id.menuAddButton)

        menuRecyclerView.layoutManager = LinearLayoutManager(this)

        // Ouvre le menu latéral
        findViewById<ImageView>(R.id.imageMenu).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Recentrage sur la position actuelle
        findViewById<ImageButton>(R.id.btnCenterLocation).setOnClickListener {
            locationOverlay.myLocation?.let {
                map.controller.animateTo(it)
                map.controller.setZoom(18.0)
            } ?: Toast.makeText(
                this,
                "Position indisponible",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Onglet POI du menu latéral
        findViewById<TextView>(R.id.tabPoi).setOnClickListener {
            menuTitle.text = "Mes POI"
            menuAddButton.text = "+ Ajouter un POI"

            menuRecyclerView.adapter = PoiAdapter(poiList) { poi ->
                Toast.makeText(this, poi.titre, Toast.LENGTH_SHORT).show()
            }

            menuAddButton.setOnClickListener {
                addPOIWithEditDialog()
            }
        }

        // Onglet Photos du menu latéral
        findViewById<TextView>(R.id.tabPhotos).setOnClickListener {
            menuTitle.text = "Mes Photos"
            menuAddButton.text = "+ Ajouter une photo"

            menuRecyclerView.adapter = SuggestionsAdapter(
                listOf("Photos à charger depuis le backend")
            ) { selectedPhoto ->
                Toast.makeText(this, selectedPhoto, Toast.LENGTH_SHORT).show()
            }

            menuAddButton.setOnClickListener {
                photoPickerLauncher.launch("image/*")
            }
        }

        // Bouton par défaut du menu latéral
        menuAddButton.setOnClickListener {
            addPOIWithEditDialog()
        }

        // Bouton d'ajout flottant
        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            showAddDialog()
        }

        // Bouton Start
        findViewById<ImageButton>(R.id.btnStart).setOnClickListener {
            sendLocationServiceAction("ACTION_START")

            if (tripState == TripState.PAUSED) {
                showResumeDialog()
            } else {
                startTrip()
            }
        }

        // Bouton Pause
        findViewById<ImageButton>(R.id.btnPause).setOnClickListener {
            sendLocationServiceAction("ACTION_PAUSE")
            pauseTrip()
        }

        // Bouton Stop
        findViewById<ImageButton>(R.id.btnStop).setOnClickListener {
            stopTrip()
            showStopDialog()
        }
    }

    // Boîte de dialogue pour ajouter un POI ou une photo
    private fun showAddDialog() {
        val options = arrayOf("Ajouter POI", "Ajouter Photo")

        AlertDialog.Builder(this)
            .setTitle("Ajouter")
            .setItems(options) { _, which ->

                when (which) {

                    0 -> {
                        addPOIWithEditDialog()
                    }

                    1 -> {
                        photoPickerLauncher.launch("image/*")
                    }
                }
            }
            .show()
    }

    // Demande confirmation pour reprendre le voyage
    private fun showResumeDialog() {
        AlertDialog.Builder(this)
            .setTitle("Reprendre le voyage")
            .setMessage("Voulez-vous reprendre votre voyage ?")
            .setPositiveButton("Oui") { _, _ -> resumeTrip() }
            .setNegativeButton("Non", null)
            .show()
    }

    // Boîte de dialogue de fin de trajet
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

            marquerVoyageCommeTermine()

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

    private val photoPickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->

            if (uris.isEmpty()) {
                Toast.makeText(this, "Aucune photo sélectionnée", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            val photoManager = PhotoManager(this)

            val result = photoManager.analyzeSelectedPhotos(
                uris,
                LocationRepository.currentPath.value
            )

            Toast.makeText(
                this,
                "Acceptées : ${result.acceptedPhotos.size} | " +
                        "Hors trajet : ${result.outsideTripPhotos.size} | " +
                        "À valider : ${result.manualValidationPhotos.size}",
                Toast.LENGTH_LONG
            ).show()
        }
}