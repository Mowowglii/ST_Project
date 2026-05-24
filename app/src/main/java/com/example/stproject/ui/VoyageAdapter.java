// VoyageAdapter contrôle seulement l'affichage de chaque voyage dans la liste.
package com.example.stproject.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stproject.R;
import com.example.stproject.models.Voyage;

import java.util.List;

public class VoyageAdapter extends RecyclerView.Adapter<VoyageAdapter.VoyageViewHolder> {

    // Liste des voyages à afficher
    private final List<Voyage> voyages;

    // Action quand on clique sur un voyage
    private final OnVoyageClickListener listener;

    // Interface pour gérer le clic
    public interface OnVoyageClickListener {
        void onVoyageClick(Voyage voyage);
    }

    public VoyageAdapter(List<Voyage> voyages,
                         OnVoyageClickListener listener) {

        this.voyages = voyages;
        this.listener = listener;
    }

    // Représente une ligne du RecyclerView
    public static class VoyageViewHolder extends RecyclerView.ViewHolder {

        TextView tripName;
        TextView tripDuration;

        public VoyageViewHolder(@NonNull View itemView) {
            super(itemView);

            tripName = itemView.findViewById(R.id.tripName);
            tripDuration = itemView.findViewById(R.id.tripDuration);
        }
    }

    @NonNull
    @Override
    public VoyageViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        // Relie item_trip.xml au RecyclerView
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip, parent, false);

        return new VoyageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull VoyageViewHolder holder,
            int position
    ) {

        // Voyage actuel
        Voyage voyage = voyages.get(position);

        // Affiche les informations du voyage
        holder.tripName.setText(voyage.getTitre());
        holder.tripDuration.setText("Durée non définie");

        // Clique sur un voyage
        holder.itemView.setOnClickListener(v ->
                listener.onVoyageClick(voyage)
        );
    }

    @Override
    public int getItemCount() {

        // Nombre de voyages dans la liste
        return voyages.size();
    }
}