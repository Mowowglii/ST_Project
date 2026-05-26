package com.example.stproject.ui

import android.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter simple utilisé pour afficher une liste de textes
// Utilisé actuellement pour l'onglet Photos en attendant le backend photo
class SuggestionsAdapter(

    // Liste des éléments à afficher
    private val items: List<String>,

    // Action exécutée lorsqu'un élément est sélectionné
    private val onClick: (String) -> Unit

) : RecyclerView.Adapter<SuggestionsAdapter.ViewHolder>() {

    // Représente une ligne du RecyclerView
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        // Texte affiché dans la ligne
        val text: TextView = view.findViewById(R.id.text1)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        // Utilise le layout Android simple_list_item_1
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.simple_list_item_1, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        // Élément actuel de la liste
        val item = items[position]

        // Affichage du texte
        holder.text.text = item

        // Action lorsqu'on clique sur un élément
        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }

    override fun getItemCount(): Int {

        // Nombre total d'éléments dans la liste
        return items.size
    }
}