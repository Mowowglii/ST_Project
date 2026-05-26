package com.example.stproject.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stproject.R
import com.example.stproject.models.POI

// Adapter utilisé pour afficher les POI dans le menu latéral
class PoiAdapter(

    // Liste des POI à afficher
    private val pois: List<POI>,

    // Action déclenchée lorsqu'un POI est sélectionné
    private val onClick: (POI) -> Unit

) : RecyclerView.Adapter<PoiAdapter.ViewHolder>() {

    // Représente une ligne du RecyclerView
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        // Informations affichées pour chaque POI
        val title: TextView = view.findViewById(R.id.poiTitle)
        val type: TextView = view.findViewById(R.id.poiType)
        val rating: RatingBar = view.findViewById(R.id.poiRating)
        val description: TextView = view.findViewById(R.id.poiDescription)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        // Relie item_poi.xml au RecyclerView
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_poi, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        // POI actuel
        val poi = pois[position]

        // Affichage des informations du POI
        holder.title.text = poi.titre
        holder.type.text = poi.type
        holder.rating.rating = poi.note.toFloat()
        holder.description.text = poi.description

        // Action lorsqu'on clique sur un POI
        holder.itemView.setOnClickListener {
            onClick(poi)
        }
    }

    override fun getItemCount(): Int {

        // Nombre total de POI dans la liste
        return pois.size
    }
}