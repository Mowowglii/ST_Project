package com.example.stproject.Manager;

import com.example.stproject.data.CFCommunicator;
import com.example.stproject.models.Voyage;

import java.util.ArrayList;
import java.util.List;

public class TripManager {

    private final CFCommunicator cfCommunicator;
    private String currentVoyageId;
    private Voyage currentVoyage;

    public TripManager() {
        this.cfCommunicator = new CFCommunicator();
    }

    // Pour update / delete
    // succès ou erreur
    public interface TripCallback {
        void onSuccess();
        void onError(String error);
    }

    // Pour createNewTrip
    public interface TripCreationCallback {
        void onSuccess(String tripId, Voyage trip);
        void onError(String error);
    }

    public interface TripsCallback {
        void onSuccess(List<Voyage> voyages);
        void onError(String error);
    }

    public void createNewTrip(String title, TripCreationCallback callback) {

        if (title == null || title.trim().isEmpty()) {
            callback.onError("Please enter a trip name");
            return;
        }

        String cleanTitle = title.trim();

        cfCommunicator.checkTripTitle(
                cleanTitle,
                new CFCommunicator.TripTitleCallback() {

                    @Override
                    public void onSuccess(boolean exists) {

                        if (exists) {
                            callback.onError("A trip with this name already exists");
                            return;
                        }
                        Voyage trip = new Voyage(
                                null,
                                cleanTitle,
                                "",
                                null,
                                false
                        );

                        cfCommunicator.addTrip(trip);
                        currentVoyage = trip;

                        callback.onSuccess(trip.getId(), trip);
                    }

                    @Override
                    public void onFailure(String error) {
                        callback.onError(error);
                    }
                }
        );
    }

    public void updateTrip(Voyage trip, TripCallback callback) {

        if (trip == null) {
            callback.onError("Trip is invalid");
            return;
        }

        if (trip.getTitre() == null || trip.getTitre().trim().isEmpty()) {
            callback.onError("Trip title is required");
            return;
        }

        cfCommunicator.modifyTrip(trip);

        callback.onSuccess();
    }

    public void deleteTrip(Voyage trip, TripCallback callback) {

        if (trip == null) {
            callback.onError("Trip is invalid");
            return;
        }

        if (trip.getTitre() == null || trip.getTitre().trim().isEmpty()) {
            callback.onError("Trip title is required");
            return;
        }

        cfCommunicator.deleteTrip(trip);

        callback.onSuccess();
    }

    public void getAllTrips(final TripsCallback callback) {
        cfCommunicator.getAllTrips(new CFCommunicator.TripsCallback() {
            @Override
            public void onSuccess(List<Voyage> voyages) {
                callback.onSuccess(voyages);
            }

            @Override
            public void onFailure(String error) {
                callback.onError(error);
            }
        });
    }
}







//    // Retourne l'id du voyage courant
//    public String getCurrentVoyageId() {
//        return currentVoyageId;
//    }
//
//    // Retourne le voyage courant
//    public Voyage getCurrentVoyage() {
//        return currentVoyage;
//    }
//
//    // Chargement des voyages:
//
//    public interface ListeVoyagesCallback {
//        void onSuccess(List<Voyage> voyages);
//        void onError(String message);
//    }
//
//    public void recupererTousLesVoyages(ListeVoyagesCallback callback) {
//        cfCommunicator.tous_les_voyages(new CloudFirestoreCommunicator.VoyageCallback() {
//            @Override
//            public void onComplete(List<Voyage> voyages) {
//                callback.onSuccess(voyages);
//            }
//
//            @Override
//            public void onError (String error) {
//                callback.onError((error));
//            }
//        });
//
//
//    }
//
//    // Callback pour savoir si la suppression du voyage a réussi ou échoué
//    public interface SuppressionVoyageCallback {
//        void onSuccess();
//        void onError(String message);
//    }
//
//    public void supprimerVoyage(Voyage voyage, SuppressionVoyageCallback callback) {
//
//        if (voyage == null || voyage.getId() == null) {
//            callback.onError("Aucun voyage sélectionné.");
//            return;
//        }
//
//        // Définit le voyage à supprimer comme voyage courant
//        cfCommunicator.definirVoyageConsulte(voyage.getId());
//
//        // Appelle la suppression complète dans CloudFirestoreCommunicator
//        cfCommunicator.supp_voyage(new CloudFirestoreCommunicator.PhotoCallback() {
//
//            @Override
//            public void onSuccess() {
//                callback.onSuccess();
//            }
//
//            @Override
//            public void onFailure(String erreur) {
//                callback.onError(erreur);
//            }
//
//            @Override
//            public void onPhotosRecuperees(List<String> photoPaths) {
//                // Obligatoire car PhotoCallback l'impose, mais pas utilisé ici
//            }
//        });
//    }
//}