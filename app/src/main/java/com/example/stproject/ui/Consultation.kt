// RecyclerView une liste complète à afficher de l'écran, ici nous allons afficher chaque nom du voyage.
// Consultation contrôle l'écran complet :
// récupération des voyages et activation de la carte

package com.example.stproject.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stproject.Manager.TripManager
import com.example.stproject.R
import com.example.stproject.models.Voyage

class Consultation : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var tabEnCours: TextView
    private lateinit var tabTermines: TextView

    // Manager utilisé pour récupérer et supprimer les voyages
    private val tripManager = TripManager()

    // Liste complète des voyages récupérés
    private val voyages = mutableListOf<Voyage>()

    // Liste affichée selon l'onglet choisi
    private val voyagesAffiches = mutableListOf<Voyage>()

    // Indique si l'onglet "Terminés" est sélectionné
    private var afficherTermines = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Affiche l'écran consultation
        setContentView(R.layout.activity_consultation)

        // Récupère la liste, le texte vide et les onglets
        recyclerView = findViewById(R.id.tripRecyclerView)
        emptyText = findViewById(R.id.emptyText)
        tabEnCours = findViewById(R.id.tabEnCours)
        tabTermines = findViewById(R.id.tabTermines)

        // Affichage vertical des voyages
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Onglet des voyages en cours
        tabEnCours.setOnClickListener {
            afficherTermines = false
            applyTripFilter()
        }

        // Onglet des voyages terminés
        tabTermines.setOnClickListener {
            afficherTermines = true
            applyTripFilter()
        }

        // Charge les voyages
        loadTrips()
    }

    private fun loadTrips() {

        tripManager.getAllTrips(

            object : TripManager.TripsCallback {

                override fun onSuccess(trips: List<Voyage>) {

                    voyages.clear()
                    voyages.addAll(trips)

                    applyTripFilter()
                }

                override fun onError(error: String) {

                    Toast.makeText(
                        this@Consultation,
                        error,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun applyTripFilter() {
        voyagesAffiches.clear()

        for (voyage in voyages) {
            val estTermine = voyage.isTermine

            if (afficherTermines && estTermine) {
                voyagesAffiches.add(voyage)
            }

            if (!afficherTermines && !estTermine) {
                voyagesAffiches.add(voyage)
            }
        }

        // Met en évidence l'onglet sélectionné
        tabEnCours.setTypeface(
            null,
            if (!afficherTermines) Typeface.BOLD else Typeface.NORMAL
        )

        tabTermines.setTypeface(
            null,
            if (afficherTermines) Typeface.BOLD else Typeface.NORMAL
        )

        tabEnCours.setTextColor(
            if (!afficherTermines) Color.BLACK else Color.GRAY
        )

        tabTermines.setTextColor(
            if (afficherTermines) Color.BLACK else Color.GRAY
        )

        if (voyagesAffiches.isEmpty()) {
            emptyText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

            // Donne les voyages filtrés au RecyclerView
            recyclerView.adapter = VoyageAdapter(
                voyagesAffiches,
                object : VoyageAdapter.OnVoyageClickListener {

                    // Ouvre la carte du voyage sélectionné
                    override fun onVoyageClick(voyage: Voyage) {
                        val intent = Intent(
                            this@Consultation,
                            Carte::class.java
                        )

                        // Envoie l'id du voyage
                        intent.putExtra("tripId", voyage.id)

                        // Envoie le titre du voyage
                        intent.putExtra("trip_name", voyage.titre)

                        // Ouvre la carte du voyage
                        startActivity(intent)
                    }

                    // Demande confirmation avant suppression
                    override fun onVoyageDelete(voyage: Voyage) {
                        AlertDialog.Builder(this@Consultation)
                            .setTitle("Supprimer le voyage")
                            .setMessage("Voulez-vous vraiment supprimer ce voyage ?")
                            .setPositiveButton("Supprimer") { _, _ ->

                                tripManager.deleteTrip(
                                    voyage,
                                    object : TripManager.TripCallback {

                                        override fun onSuccess() {
                                            // Retire le voyage de la liste complète et de la liste affichée
                                            voyages.remove(voyage)
                                            voyagesAffiches.remove(voyage)

                                            recyclerView.adapter?.notifyDataSetChanged()

                                            Toast.makeText(
                                                this@Consultation,
                                                "Voyage supprimé",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            applyTripFilter()
                                        }

                                        override fun onError(message: String) {
                                            Toast.makeText(
                                                this@Consultation,
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
                }
            )
        }
    }
}