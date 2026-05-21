package com.example.stproject

//import com.example.stproject.models.POI
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.stproject.VoyageAdapter
import com.example.stproject.data.CloudFirestoreCommunicator
import com.example.stproject.models.Voyage

class Consultation : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView

    private val communicator = CloudFirestoreCommunicator()

    private val voyages = mutableListOf<Voyage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_consultation)

        recyclerView = findViewById(R.id.tripRecyclerView)
        emptyText = findViewById(R.id.emptyText)

        recyclerView.layoutManager = LinearLayoutManager(this)

        chargerVoyages()
    }

    private fun chargerVoyages() {

        communicator.tous_les_voyages(object : CloudFirestoreCommunicator.VoyageCallback {

            override fun onComplete(voyagesRecuperes: List<Voyage>) {

                voyages.clear()
                voyages.addAll(voyagesRecuperes)

                if (voyages.isEmpty()) {

                    emptyText.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE

                } else {

                    emptyText.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE

                    val adapter = VoyageAdapter(voyages) { voyage ->

                        val intent = Intent(this@Consultation, Carte::class.java)

                        intent.putExtra("voyage_id", voyage.id)
                        intent.putExtra("trip_name", voyage.titre)

                        startActivity(intent)
                    }

                    recyclerView.adapter = adapter
                }
            }

            override fun onError(error: String) {

                Toast.makeText(
                    this@Consultation,
                    "Erreur : $error",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}