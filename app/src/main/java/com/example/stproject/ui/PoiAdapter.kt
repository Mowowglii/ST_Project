package com.example.stproject.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stproject.R
import com.example.stproject.models.POI
import com.example.stproject.models.Photo

class PoiAdapter(
    private val pois: List<POI>,
    private val getPhotosForPoi: (POI) -> List<Photo>,
    private val onClick: (POI) -> Unit
) : RecyclerView.Adapter<PoiAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.poiTitle)
        val type: TextView = view.findViewById(R.id.poiType)
        val rating: RatingBar = view.findViewById(R.id.poiRating)
        val description: TextView = view.findViewById(R.id.poiDescription)
        val photosRecyclerView: RecyclerView = view.findViewById(R.id.poiPhotosRecyclerView)
        val btnDeletePoi: ImageView = view.findViewById(R.id.btnDeletePoi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_poi, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val poi = pois[position]
        val photos = getPhotosForPoi(poi)

        holder.title.text = poi.titre
        holder.type.text = poi.type
        holder.rating.rating = poi.note.toFloat()
        holder.description.text = poi.description

        holder.photosRecyclerView.layoutManager =
            LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)

        holder.photosRecyclerView.adapter = PoiPhotoAdapter(photos) { photo ->
            Toast.makeText(
                holder.itemView.context,
                "Photo sélectionnée",
                Toast.LENGTH_SHORT
            ).show()
        }

        holder.itemView.setOnClickListener {
            onClick(poi)
        }

        holder.btnDeletePoi.setOnClickListener {
            Toast.makeText(
                holder.itemView.context,
                "Suppression POI à relier",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun getItemCount(): Int {
        return pois.size
    }
}