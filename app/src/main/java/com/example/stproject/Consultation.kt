// RecyclerView une liste complète à affciher de l'écran, ici nous allons affciher chaque nom du voyage.
//Consultation contrôle l'écran complet:
// récupération des voyages et activation de la carte
package com.example.stproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.stproject.ui.VoyageAdapter
import com.example.stproject.models.Voyage
import com.example.stproject.service.VoyageManager

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

                        // Donne les voyages au RecyclerView
                        recyclerView.adapter = VoyageAdapter(
                            voyages,

                            // Action quand on clique sur un voyage
                            object : VoyageAdapter.OnVoyageClickListener {

                                override fun onVoyageClick(voyage: Voyage) {

                                    val intent = Intent(
                                        this@Consultation,
                                        Carte::class.java
                                    )

                                    // Envoie l'id du voyage
                                    intent.putExtra("tripId", voyage.id)

                                    // Envoie le titre du voyage
                                    intent.putExtra("trip_name", voyage.titre)

                                    // Ouvre Carte
                                    startActivity(intent)
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