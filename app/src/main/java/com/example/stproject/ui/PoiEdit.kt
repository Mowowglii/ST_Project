package com.example.stproject.ui

import android.net.Uri
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stproject.R
import com.example.stproject.models.POI
import com.example.stproject.models.Photo
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PoiEditDialog(
    private val poi: POI,
    private val onSave: (POI, List<Photo>) -> Unit
) : BottomSheetDialogFragment() {

    private val selectedPhotos = mutableListOf<Photo>()
    private var photoAdapter: PoiPhotoAdapter? = null

    private val photoPickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->

            if (uris.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Aucune photo sélectionnée",
                    Toast.LENGTH_SHORT
                ).show()
                return@registerForActivityResult
            }

            for (uri in uris) {
                val photo = Photo()
                photo.imageURI = uri
                photo.associatedPOI = poi
                selectedPhotos.add(photo)
            }

            photoAdapter?.notifyDataSetChanged()
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.edit_poi, container, false)

        val nameInput = view.findViewById<EditText>(R.id.poiName)
        val reviewInput = view.findViewById<EditText>(R.id.poiReview)
        val ratingBar = view.findViewById<RatingBar>(R.id.poiRating)
        val saveBtn = view.findViewById<Button>(R.id.saveBtn)
        val imagephoto = view.findViewById<ImageView>(R.id.imagephoto)
        val spinner = view.findViewById<Spinner>(R.id.spinnerTypeLieu)
        val photoGrid = view.findViewById<RecyclerView>(R.id.photoGrid)

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

        nameInput.setText(poi.titre)
        reviewInput.setText(poi.description)
        ratingBar.rating = poi.note.toFloat()

        val typeIndex = types.indexOf(poi.type)

        if (typeIndex >= 0) {
            spinner.setSelection(typeIndex)
        } else {
            spinner.setSelection(0)
        }

        photoGrid.layoutManager = GridLayoutManager(requireContext(), 3)

        photoAdapter = PoiPhotoAdapter(selectedPhotos) { _ ->
            Toast.makeText(
                requireContext(),
                "Photo sélectionnée",
                Toast.LENGTH_SHORT
            ).show()
        }

        photoGrid.adapter = photoAdapter

        imagephoto.setOnClickListener {
            photoPickerLauncher.launch("image/*")
        }

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

            onSave(poi, selectedPhotos)

            dismiss()
        }

        return view
    }
}