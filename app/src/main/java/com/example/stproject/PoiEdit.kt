package com.example.stproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

// Fenêtre d’édition d’un POI affichée sous forme de Bottom Sheet
// Permet de modifier les informations d’un point d’intérêt existant
class PoiEditDialog(
    private val poi: POI,
    private val onSave: (POI) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Chargement de l’interface XML du dialog
        val view = inflater.inflate(R.layout.edit_poi, container, false)

        // Récupération des éléments de l’interface
        val nameInput = view.findViewById<EditText>(R.id.poiName)
        val ratingBar = view.findViewById<RatingBar>(R.id.poiRating)
        val saveBtn = view.findViewById<Button>(R.id.saveBtn)
        val imagephoto = view.findViewById<Button>(R.id.imagephoto)
        val spinner = view.findViewById<Spinner>(R.id.spinnerTypeLieu)

        // Liste des catégories possibles pour un POI
        val types = listOf(
            "Choisir une catégorie",
            "Monument",
            "Restaurant",
            "Bar",
            "Parc",
            "autre"
        )

        // Adaptateur permettant d’afficher la liste dans le Spinner
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            types
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter

        // Position initiale du spinner
        spinner.setSelection(0)

        // Pré-remplissage des champs avec les données existantes du POI
        nameInput.setText(poi.name)
        ratingBar.rating = poi.note

        // Action lors du clic sur le bouton de sauvegarde
        saveBtn.setOnClickListener {

            // Récupération de la catégorie choisie
            val choix = spinner.selectedItem.toString()

            // Vérification : une catégorie doit être sélectionnée
            if (choix == "Choisir une catégorie") {
                Toast.makeText(
                    requireContext(),
                    "Veuillez choisir une catégorie",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Mise à jour des données du POI avec les nouvelles valeurs
            poi.name = nameInput.text.toString()
            poi.note = ratingBar.rating
            poi.type = choix

            // Retour des données modifiées à l’activité appelante
            onSave(poi)

            // Fermeture du Bottom Sheet
            dismiss()
        }

        return view
    }
}