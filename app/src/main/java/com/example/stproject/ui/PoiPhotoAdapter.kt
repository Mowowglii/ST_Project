package com.example.stproject.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.stproject.R
import com.example.stproject.models.Photo

class PoiPhotoAdapter(
    private val photos: List<Photo>,
    private val onClick: (Photo) -> Unit
) : RecyclerView.Adapter<PoiPhotoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val photoMiniature: ImageView = view.findViewById(R.id.photoMiniature)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_poi_photo, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val photo = photos[position]

        photo.imageURI?.let {
            holder.photoMiniature.setImageURI(it)
        }

        holder.itemView.setOnClickListener {
            onClick(photo)
        }
    }

    override fun getItemCount(): Int {
        return photos.size
    }
}