///  Cette class fait en sorte d' afficher ligne par ligne les voyages récupéré par la class consultation
/// Cette class ne récupére rien de la base de donnée.

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stproject.models.Voyage;

import java.util.List;

public class VoyageAdapter extends RecyclerView.Adapter<VoyageAdapter.VoyageViewHolder> {

    private final List<Voyage> voyages;
    private final OnVoyageClickListener listener;

    public interface OnVoyageClickListener {
        void onVoyageClick(Voyage voyage);
    }

    public VoyageAdapter(List<Voyage> voyages, OnVoyageClickListener listener) {
        this.voyages = voyages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VoyageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);

        return new VoyageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoyageViewHolder holder, int position) {
        Voyage voyage = voyages.get(position);

        String titre = voyage.getTitre();

        if (titre == null || titre.isEmpty()) {
            titre = "Voyage sans titre";
        }

        holder.tripTitle.setText(titre);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVoyageClick(voyage);
            }
        });
    }

    @Override
    public int getItemCount() {
        return voyages.size();
    }

    static class VoyageViewHolder extends RecyclerView.ViewHolder {

        TextView tripTitle;

        public VoyageViewHolder(@NonNull View itemView) {
            super(itemView);
            tripTitle = itemView.findViewById(android.R.id.text1);
        }
    }
}