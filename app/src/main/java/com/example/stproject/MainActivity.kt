package com.example.stproject


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// Activité principale de l'application
// Elle sert de point d'entrée : création d'un voyage ou consultation des voyages existants
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Active l'affichage edge to edge (interface derrière les barres système)
        enableEdgeToEdge()

        // Charge le layout principal
        setContentView(R.layout.activity_main)

        // Ajuste automatiquement le padding pour éviter que le contenu passe sous la barre système
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Récupération des éléments de l'interface
        val btnStart = findViewById<Button>(R.id.btnStart)
        val editTripName = findViewById<EditText>(R.id.editTripName)
        val btnConsult = findViewById<Button>(R.id.consult)

        // Bouton permettant d'accéder à la page de consultation des voyages
        btnConsult.setOnClickListener {
            val intent = Intent(this, Consultation::class.java)
            startActivity(intent)
        }

        // Bouton de démarrage d'un nouveau voyage
        btnStart.setOnClickListener {

            // Récupération du nom du voyage saisi par l'utilisateur
            val tripName = editTripName.text.toString()

            // Lancement de l'activité carte avec le nom du voyage en paramètre
            val intent = Intent(this, Carte::class.java)
            intent.putExtra("trip_name", tripName)

            startActivity(intent)
        }
    }
}