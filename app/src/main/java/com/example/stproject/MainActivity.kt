package com.example.stproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        // Ajuste le padding pour les barres système
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val btnStart = findViewById<Button>(R.id.btnStart)
        val editTripName = findViewById<EditText>(R.id.editTripName)
        val btnConsult = findViewById<Button>(R.id.consult)

        btnConsult.setOnClickListener {
            val cst = Intent(this, Consultation::class.java)
            startActivity(cst)

        }

        btnStart.setOnClickListener {
            val tripName = editTripName.text.toString()  // récupère le nom du voyage

            // Ouvre la Carte
            val intent = Intent(this, Carte::class.java)
            intent.putExtra("trip_name", tripName)  // <-- ici on passe le nom du voyage
            startActivity(intent)

        }
    }
}