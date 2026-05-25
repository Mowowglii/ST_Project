package com.example.stproject.service;

import com.example.stproject.data.CloudFirestoreCommunicator;
import com.example.stproject.models.POI;

import java.util.ArrayList;
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

    public void modifierPOI(POI ancien, String nouveauTitre, String nouvelleDescription, String nouveauType, Integer nouvellenote, AjoutPOICallback callback) {
        if (ancien == null) {
            callback.onError("Aucun POI sélectionné");
            return;
        }
        if (nouveauTitre != null && nouveauTitre.trim().isEmpty()) {
            callback.onError("Le titre du POI ne peut pas être vide");
            return;
        }

        cfCommunicator.modification_dun_poi(ancien,
                nouveauTitre != null ? nouveauTitre.trim() : null,
                nouvelleDescription,
                nouveauType,
                nouvellenote,
                new CloudFirestoreCommunicator.POICallback() {
                    @Override
                    public void onComplete(List<POI> pois) {
                        if (nouveauTitre != null) ancien.setTitre(nouveauTitre.trim());
                        if (nouvelleDescription != null) ancien.setDescription(nouvelleDescription);
                        if (nouveauType != null) ancien.setType(nouveauType);
                        if (nouvellenote != null) ancien.setNote(nouvellenote);
                        callback.onSuccess(ancien);
                    }

                    @Override
                    public void onError(String error) {
                        callback.onError(error);
                    }
                }
        );
    }

    public void supprimerPOI(POI poi, AjoutPOICallback callback) {
        if (poi == null) {
            callback.onError("Aucun POI sélectionné");
            return;
        }
        List<POI> poisASupprimer = new ArrayList<>();
        poisASupprimer.add(poi);
        cfCommunicator.supp_poi(
                poisASupprimer,
                new CloudFirestoreCommunicator.POICallback() {
                    @Override
                    public void onComplete(List<POI> pois) {
                        callback.onSuccess(poi);
                    }

                    @Override
                    public void onError(String error) {
                        callback.onError(error);
                    }
                }
        );
    }

    public void recupererPOIDuVoyage(
            String voyageId,
            ListePOICallback callback
    ) {
        if (voyageId == null || voyageId.trim().isEmpty()) {
            callback.onError("Aucun voyage sélectionné");
            return;
        }

        cfCommunicator.recuperer_une_liste_de_poi(
                new CloudFirestoreCommunicator.POICallback() {
                    @Override
                    public void onComplete(List<POI> pois) {
                        List<String> ids = new ArrayList<>();
                        ids.add(voyageId);

                        List<POI> poisFiltres =
                                cfCommunicator.filtrer_par_voyages(pois, ids);

                        callback.onSuccess(poisFiltres);
                    }

                    @Override
                    public void onError(String error) {
                        callback.onError(error);
                    }
                }
        );
    }

    public interface ListePOICallback {
        void onSuccess(List<POI> pois);
        void onError(String message);
    }

}
