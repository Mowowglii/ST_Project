package com.example.stproject

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
import com.example.stproject.models.POI
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PoiEditDialog(
    private val poi: POI,
    private val onSave: (POI) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.edit_poi, container, false)

        val nameInput = view.findViewById<EditText>(R.id.poiName)

        val ratingBar = view.findViewById<RatingBar>(R.id.poiRating)

        val saveBtn = view.findViewById<Button>(R.id.saveBtn)

        val imagephoto = view.findViewById<ImageView>(R.id.imagephoto)

        val spinner = view.findViewById<Spinner>(R.id.spinnerTypeLieu)

        val types = listOf(
            "Choisir une catégorie",
            "Monument",
            "Restaurant",
            "Bar",
            "Parc",
            "autre"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            types
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter

        spinner.setSelection(0)

        nameInput.setText(poi.getTitre())

        ratingBar.rating = poi.getNote().toFloat()

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

            poi.setTitre(nameInput.text.toString())

            poi.setNote(ratingBar.rating.toInt())

            poi.setType(choix)

            onSave(poi)

            dismiss()
        }

        return view
    }
}