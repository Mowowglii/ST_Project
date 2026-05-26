// RecyclerView une liste complète à afficher de l'écran, ici nous allons afficher chaque nom du voyage.
// Consultation contrôle l'écran complet :
// récupération des voyages et activation de la carte

package com.example.stproject.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stproject.Manager.VoyageManager
import com.example.stproject.R
import com.example.stproject.models.Voyage

class Consultation : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView

    // Manager utilisé pour récupérer et supprimer les voyages
    private val voyageManager = VoyageManager()

    // Liste des voyages affichés dans le RecyclerView
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

                                            voyageManager.supprimerVoyage(

                                                voyage,


                                                object : VoyageManager.SuppressionVoyageCallback {

                                                    override fun onSuccess() {

                                                        // Retire le voyage de la liste affichée
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

                                        .setNegativeButton("Annuler", null)

                                        .show()
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