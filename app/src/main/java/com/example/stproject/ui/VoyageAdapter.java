package com.example.stproject.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stproject.R;
import com.example.stproject.models.Voyage;

import java.util.List;

public class VoyageAdapter extends RecyclerView.Adapter<VoyageAdapter.VoyageViewHolder> {

    private final List<Voyage> voyages;
    private final OnVoyageClickListener listener;

    public interface OnVoyageClickListener {
        void onVoyageClick(Voyage voyage);
        void onVoyageEdit(Voyage voyage);
        void onVoyageDelete(Voyage voyage);
    }

    public VoyageAdapter(List<Voyage> voyages, OnVoyageClickListener listener) {
        this.voyages = voyages;
        this.listener = listener;
    }

    public static class VoyageViewHolder extends RecyclerView.ViewHolder {

        TextView tripName;
        TextView tripDescription;
        TextView tripDuration;
        RatingBar tripRating;

        ImageView btnEdit;
        ImageView btnDelete;

        public VoyageViewHolder(@NonNull View itemView) {
            super(itemView);

            tripName = itemView.findViewById(R.id.tripName);
            tripDescription = itemView.findViewById(R.id.tripDescription);
            tripDuration = itemView.findViewById(R.id.tripDuration);
            tripRating = itemView.findViewById(R.id.tripRating);

            btnEdit = itemView.findViewById(R.id.btnSelectTrip);
            btnDelete = itemView.findViewById(R.id.btnEditTrip);
        }
    }

    @NonNull
    @Override
    public VoyageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip, parent, false);

        return new VoyageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoyageViewHolder holder, int position) {
        Voyage voyage = voyages.get(position);

        holder.tripName.setText(voyage.getTitre());

        if (voyage.getDescription() == null || voyage.getDescription().trim().isEmpty()) {
            holder.tripDescription.setText("Aucune description");
        } else {
            holder.tripDescription.setText(voyage.getDescription());
        }

        holder.tripDuration.setText("Durée non définie");

        if (voyage.getNote() != null) {
            holder.tripRating.setRating(voyage.getNote());
        } else {
            holder.tripRating.setRating(0);
        }

        holder.itemView.setOnClickListener(v ->
                listener.onVoyageClick(voyage)
        );

        holder.btnEdit.setOnClickListener(v ->
                listener.onVoyageEdit(voyage)
        );

        holder.btnDelete.setOnClickListener(v ->
                listener.onVoyageDelete(voyage)
        );
    }

    @Override
    public int getItemCount() {
        return voyages.size();
    }
}