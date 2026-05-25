// RecyclerView une liste complète à affciher de l'écran, ici nous allons affciher chaque nom du voyage.
//Consultation contrôle l'écran complet:
// récupération des voyages et activation de la carte
package com.example.stproject.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.stproject.R
import com.example.stproject.models.Voyage
import com.example.stproject.Manager.VoyageManager

class Consultation : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView

    private val voyageManager = VoyageManager()

    private val voyages = mutableListOf<Voyage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Affiche l'écran consultation
        setContentView(R.layout.activity_consultation)

        // Récupère la liste et le texte "aucun voyage"
        recyclerView = findViewById(R.id.tripRecyclerView)
        emptyText = findViewById(R.id.emptyText)

        // Affichage vertical des voyages
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Charge les voyages
        chargerVoyages()
    }
    private fun chargerVoyages() {

        voyageManager.recupererTousLesVoyages(

            object : VoyageManager.ListeVoyagesCallback {

                override fun onSuccess(voyagesRecuperes: List<Voyage>) {

                    voyages.clear()
                    voyages.addAll(voyagesRecuperes)

                    // Aucun voyage trouvé
                    if (voyages.isEmpty()) {

                        emptyText.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE

                    } else {

                        emptyText.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE

                        recyclerView.adapter = VoyageAdapter(

                            voyages,

                            object : VoyageAdapter.OnVoyageClickListener {

                                // Ouvre la carte du voyage
                                override fun onVoyageClick(voyage: Voyage) {

                                    val intent = Intent(
                                        this@Consultation,
                                        Carte::class.java
                                    )

                                    intent.putExtra("tripId", voyage.id)
                                    intent.putExtra("trip_name", voyage.titre)

                                    startActivity(intent)
                                }

                                // Supprime le voyage
                                override fun onVoyageDelete(voyage: Voyage) {

                                    voyageManager.supprimerVoyage(

                                        voyage,

                                        object : VoyageManager.SuppressionVoyageCallback {

                                            override fun onSuccess() {

                                                voyages.remove(voyage)

                                                recyclerView.adapter?.notifyDataSetChanged()

                                                Toast.makeText(
                                                    this@Consultation,
                                                    "Voyage supprimé",
                                                    Toast.LENGTH_SHORT
                                                ).show()
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
                            }
                        )
                    }
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
}