package com.example.stproject

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.content.SharedPreferences


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


        btnStart.setOnClickListener {
            val tripName = editTripName.text.toString()  // récupère le nom du voyage


            // Stocke le nom du voyage dans SharedPreferences
            val sharedPref = getSharedPreferences("SmartTripPrefs", MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("trip_name", tripName)
                apply()
            }


            // Ouvre la Carte
            val intent = Intent(this, Carte::class.java)
            startActivity(intent)
        }
    }
}
