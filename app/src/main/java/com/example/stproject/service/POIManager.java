package com.example.stproject.service;

import com.example.stproject.data.CloudFirestoreCommunicator;
import com.example.stproject.models.POI;

import java.util.List;

public class POIManager {
    private final CloudFirestoreCommunicator cfCommunicator;

    public POIManager() {
        this.cfCommunicator = new CloudFirestoreCommunicator();
    }

    public interface AjoutPOICallback {
        void onSuccess(POI poi);
        void onError(String message);
    }

    public void ajouterPOI (String titre, String description, int note, double latitude, double longitude, String type, AjoutPOICallback callback) {
        if (titre == null || titre.trim().isEmpty()) {
            callback.onError("Veuilllez entrer un titre pour le POI");
            return;
        }

        POI poi = new POI (
                titre.trim(),
                description,
                note,
                latitude,
                longitude,
                type
        );
        // le new cloud sert à créer un listener temporaire pour attendre la réponse asynchrone de Firestore.
        cfCommunicator.ajout_poi(poi, new CloudFirestoreCommunicator.POICallback() {
            @Override
            public void onComplete(List<POI> pois) {
                callback.onSuccess(poi);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);

            }
        });
    }
    public void definirVoyageActif(String voyageid) {
        cfCommunicator.demarrerNouveauVoyage(voyageid);
    }


}
