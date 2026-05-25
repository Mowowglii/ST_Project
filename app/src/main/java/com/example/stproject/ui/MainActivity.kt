package com.example.stproject.ui

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.stproject.Manager.VoyageManager
import com.example.stproject.R
import com.example.stproject.models.Voyage

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        val voyageManager = VoyageManager()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnStart = findViewById<Button>(R.id.btnStart)
        val editTripName = findViewById<EditText>(R.id.editTripName)
        val btnConsult = findViewById<Button>(R.id.consult)
        val traveler = findViewById<ImageView>(R.id.travelCharacter)

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

        btnConsult.setOnClickListener {
            val intent = Intent(this, Consultation::class.java)
            startActivity(intent)
        }

        btnStart.setOnClickListener {
            val tripName = editTripName.text.toString()

            voyageManager.creerNouveauVoyage(
                tripName,
                object : VoyageManager.CreationVoyageCallback {

                    override fun onSuccess(voyageId: String, voyage: Voyage) {
                        val intent = Intent(this@MainActivity, Carte::class.java)

                        intent.putExtra("trip_name", voyage.titre)
                        intent.putExtra("tripId", voyageId)

                        startActivity(intent)
                    }

                    override fun onError(message: String) {
                        Toast.makeText(
                            this@MainActivity,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }
    }
}