package com.example.stproject.ui

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.stproject.R

// Activité principale de l'application
// Elle sert de point d'entrée : création d'un voyage ou consultation des voyages existants
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Active l'affichage edge to edge
        enableEdgeToEdge()

        // Charge le layout principal
        setContentView(R.layout.activity_main)

        // Ajuste automatiquement le padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Récupération des éléments de l'interface
        val btnStart = findViewById<Button>(R.id.btnStart)
        val editTripName = findViewById<EditText>(R.id.editTripName)
        val btnConsult = findViewById<Button>(R.id.consult)
        //val themeSwitch = findViewById<SwitchCompat>(R.id.themeSwitch)
        val traveler = findViewById<ImageView>(R.id.travelCharacter)

        // Animation du personnage qui traverse l'écran
        traveler.post {

            val screenWidth = resources.displayMetrics.widthPixels.toFloat()

            ObjectAnimator.ofFloat(
                traveler,
                "translationX",
                -200f,
                screenWidth
            ).apply {

                duration = 6000
                repeatCount = ObjectAnimator.INFINITE
                interpolator = LinearInterpolator()

                start()
            }
        }

        // Bouton permettant d'accéder à la page de consultation des voyages
        btnConsult.setOnClickListener {

            val intent = Intent(this, Consultation::class.java)

            startActivity(intent)
        }

        // Bouton de démarrage d'un nouveau voyage
        btnStart.setOnClickListener {

            // Récupération du nom du voyage saisi par l'utilisateur
            val tripName = editTripName.text.toString()

            // Lancement de l'activité carte avec le nom du voyage
            val intent = Intent(this, Carte::class.java)

            intent.putExtra("trip_name", tripName)

            startActivity(intent)
        }
    }
}