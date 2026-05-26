package com.example.stproject.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.stproject.R
import com.example.stproject.models.Photo

/**
 * Adapter utilisé pour afficher les photos du voyage dans le menu "Mes Photos".
 */
class PhotoAdapter(
    private val photos: List<Photo>
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    class PhotoViewHolder(val imageView: ImageView) :
        RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val imageView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo, parent, false) as ImageView

        return PhotoViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photos[position]

        val uri: Uri? = photo.imageURI

        if (uri != null) {
            holder.imageView.setImageURI(uri)
        }
    }

    override fun getItemCount(): Int {
        return photos.size
    }
}