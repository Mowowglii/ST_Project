package com.example.stproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

// Activité permettant d’afficher l’écran de consultation
class Consultation : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Liaison de l’activité avec son interface graphique XML
        // Le fichier activity_consultation.xml définit l’UI affichée à l’écran
        setContentView(R.layout.activity_consultation)
    }
}