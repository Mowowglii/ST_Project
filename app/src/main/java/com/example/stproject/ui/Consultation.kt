package com.example.stproject.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
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

    private val tripManager = TripManager()
    private val voyages = mutableListOf<Voyage>()
    private val voyagesAffiches = mutableListOf<Voyage>()

    private var afficherTermines = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consultation)

        recyclerView = findViewById(R.id.tripRecyclerView)
        emptyText = findViewById(R.id.emptyText)
        tabEnCours = findViewById(R.id.tabEnCours)
        tabTermines = findViewById(R.id.tabTermines)

        recyclerView.layoutManager = LinearLayoutManager(this)

        tabEnCours.setOnClickListener {
            afficherTermines = false
            applyTripFilter()
        }

        tabTermines.setOnClickListener {
            afficherTermines = true
            applyTripFilter()
        }

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
                    Toast.makeText(this@Consultation, error, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun applyTripFilter() {
        voyagesAffiches.clear()

        for (voyage in voyages) {
            val isFinished = voyage.isTermine

            if (afficherTermines && isFinished) {
                voyagesAffiches.add(voyage)
            }

            if (!afficherTermines && !isFinished) {
                voyagesAffiches.add(voyage)
            }
        }

        tabEnCours.setTypeface(null, if (!afficherTermines) Typeface.BOLD else Typeface.NORMAL)
        tabTermines.setTypeface(null, if (afficherTermines) Typeface.BOLD else Typeface.NORMAL)

        tabEnCours.setTextColor(if (!afficherTermines) Color.BLACK else Color.GRAY)
        tabTermines.setTextColor(if (afficherTermines) Color.BLACK else Color.GRAY)

        if (voyagesAffiches.isEmpty()) {
            emptyText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            return
        }

        emptyText.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE

        recyclerView.adapter = VoyageAdapter(
            voyagesAffiches,
            object : VoyageAdapter.OnVoyageClickListener {

                override fun onVoyageClick(voyage: Voyage) {
                    val intent = Intent(this@Consultation, Carte::class.java)
                    intent.putExtra("tripId", voyage.id)
                    intent.putExtra("trip_name", voyage.titre)
                    startActivity(intent)
                }

                override fun onVoyageEdit(voyage: Voyage) {
                    showEditTripDialog(voyage)
                }

                override fun onVoyageDelete(voyage: Voyage) {
                    showDeleteTripDialog(voyage)
                }
            }
        )
    }

    private fun showDeleteTripDialog(voyage: Voyage) {
        AlertDialog.Builder(this)
            .setTitle("Supprimer le voyage")
            .setMessage("Voulez-vous vraiment supprimer ce voyage ?")
            .setPositiveButton("Supprimer") { _, _ ->

                tripManager.deleteTrip(
                    voyage,
                    object : TripManager.TripCallback {

                        override fun onSuccess() {
                            voyages.remove(voyage)
                            voyagesAffiches.remove(voyage)

                            Toast.makeText(
                                this@Consultation,
                                "Voyage supprimé",
                                Toast.LENGTH_SHORT
                            ).show()

                            applyTripFilter()
                        }

                        override fun onError(error: String) {
                            Toast.makeText(this@Consultation, error, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showEditTripDialog(voyage: Voyage) {
        val dialogView = layoutInflater.inflate(R.layout.edit_trip, null)

        val editTitle = dialogView.findViewById<EditText>(R.id.editTripTitle)
        val editDescription = dialogView.findViewById<EditText>(R.id.editTripDescription)
        val editRating = dialogView.findViewById<RatingBar>(R.id.editTripRating)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveTripEdit)

        editTitle.setText(voyage.titre)
        editDescription.setText(voyage.description ?: "")
        editRating.rating = voyage.note?.toFloat() ?: 0f

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnSave.setOnClickListener {
            voyage.titre = editTitle.text.toString().trim()
            voyage.description = editDescription.text.toString().trim()
            voyage.note = editRating.rating.toInt()

            tripManager.updateTrip(
                voyage,
                object : TripManager.TripCallback {

                    override fun onSuccess() {
                        Toast.makeText(
                            this@Consultation,
                            "Voyage modifié",
                            Toast.LENGTH_SHORT
                        ).show()

                        dialog.dismiss()
                        applyTripFilter()
                    }

                    override fun onError(error: String) {
                        Toast.makeText(this@Consultation, error, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        dialog.show()
    }
}