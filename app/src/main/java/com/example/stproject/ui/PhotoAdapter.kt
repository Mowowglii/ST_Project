package com.example.stproject.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.stproject.R
import com.example.stproject.models.Photo
import android.view.View

/**
 * Adapter utilisé pour afficher les photos du voyage dans le menu "Mes Photos".
 */
class PhotoAdapter(
    private val photos: List<Photo>
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photoImage: ImageView = itemView.findViewById(R.id.photoImage)
        val btnDeletePhoto: ImageView = itemView.findViewById(R.id.btnDeletePhoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo, parent, false)

        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photos[position]

        photo.imageURI?.let {
            holder.photoImage.setImageURI(it)
        }
    }

    override fun getItemCount(): Int {
        return photos.size
    }
}