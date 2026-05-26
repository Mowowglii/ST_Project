package com.example.stproject.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Spinner
import android.widget.Toast
import com.example.stproject.R
import com.example.stproject.models.POI
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

// Fenêtre permettant de créer ou modifier un POI
class PoiEditDialog(
    private val poi: POI,
    private val onSave: (POI) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Charge le layout du formulaire POI
        val view = inflater.inflate(R.layout.edit_poi, container, false)

        // Récupération des éléments de l'interface
        val nameInput = view.findViewById<EditText>(R.id.poiName)
        val reviewInput = view.findViewById<EditText>(R.id.poiReview)
        val ratingBar = view.findViewById<RatingBar>(R.id.poiRating)
        val saveBtn = view.findViewById<Button>(R.id.saveBtn)
        val imagephoto = view.findViewById<ImageView>(R.id.imagephoto)
        val spinner = view.findViewById<Spinner>(R.id.spinnerTypeLieu)

        // Liste des catégories disponibles pour un POI
        val types = listOf(
            "Choisir une catégorie",
            "Monument",
            "Restaurant",
            "Bar",
            "Parc",
            "autre"
        )

        // Adaptateur utilisé pour remplir le spinner des catégories
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            types
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter

        // Pré-remplit le formulaire si le POI existe déjà
        nameInput.setText(poi.titre)
        reviewInput.setText(poi.description)
        ratingBar.rating = poi.note.toFloat()

        // Sélectionne automatiquement la catégorie actuelle du POI
        val typeIndex = types.indexOf(poi.type)

        if (typeIndex >= 0) {
            spinner.setSelection(typeIndex)
        } else {
            spinner.setSelection(0)
        }

        // Sauvegarde les informations saisies
        saveBtn.setOnClickListener {
            val choix = spinner.selectedItem.toString()

            if (choix == "Choisir une catégorie") {
                Toast.makeText(
                    requireContext(),
                    "Veuillez choisir une catégorie",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            poi.titre = nameInput.text.toString()
            poi.description = reviewInput.text.toString()
            poi.note = ratingBar.rating.toInt()
            poi.type = choix

            onSave(poi)

            dismiss()
        }

        return view
    }
}