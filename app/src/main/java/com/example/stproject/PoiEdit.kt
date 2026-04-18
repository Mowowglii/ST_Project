package com.example.stproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
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
        val imagephoto = view.findViewById<Button>(R.id.imagephoto)

        nameInput.setText(poi.name)
        ratingBar.rating = poi.note

        saveBtn.setOnClickListener {

            poi.name = nameInput.text.toString()
            poi.note = ratingBar.rating

            onSave(poi)
            dismiss()
        }

        return view
    }
}