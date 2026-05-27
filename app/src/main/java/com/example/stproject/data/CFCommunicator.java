package com.example.stproject.data;

import android.util.Log;

import com.example.stproject.models.POI;
import com.example.stproject.models.Photo;
import com.example.stproject.models.Voyage;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CFCommunicator {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final CloudStorageCommunicator CSC = new CloudStorageCommunicator(this);

    public interface SimpleCallback {
        void onDone();
    }
    // Voyage
    public String getTripKey(){
        return db.collection("voyages")
                .document()
                .getId();
    }
    

    public interface TripKeytitleCallback {
        void onSuccess(String tripId);
        void onFailure(String error);
    }

    

    public void getKeyFromTripTitle(Voyage trip, TripKeytitleCallback callback) {
        db.collection("voyages")
                .whereEqualTo("titre", trip.getTitre())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String docId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        callback.onSuccess(docId);
                    } else {
                        callback.onFailure("Aucun voyage trouvé avec le titre : " + trip.getTitre());
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public interface TripTitleCallback {
        void onSuccess(boolean exists);
        void onFailure(String error);
    }

    public void checkTripTitle(String title, TripTitleCallback callback) {

        db.collection("voyages")
                .whereEqualTo("titre", title)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    boolean exists = !queryDocumentSnapshots.isEmpty();
                    callback.onSuccess(exists);

                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));
    }

    public void addTrip(Voyage trip){
        // Créer l'emplacement pour le voyage et déterminer l'id du trip
        trip.setId(getTripKey());

        db.collection("voyages")
                .document(trip.getId())
                .set(trip)
                .addOnSuccessListener(aVoid ->
                        Log.d("Firestore", "Voyage ajouté avec succès"))
                .addOnFailureListener(e -> Log.e("Firestore", "Erreur ajout voyage : " + e.getMessage()));
    }
    // récupère une liste de voyage.
    public interface TripsCallback {
        void onSuccess(List<Voyage> voyages);
        void onFailure(String error);
    }

    public void getAllTrips(TripsCallback callback) {
        db.collection("voyages")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    List<Voyage> voyages = new ArrayList<>();

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Voyage voyage = document.toObject(Voyage.class);

                        if (voyage != null) {
                            voyages.add(voyage);
                        }
                    }

                    callback.onSuccess(voyages);
                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));
    }

    public void modifyTrip(Voyage trip) {
        // OPTIMIZED: Use trip.getId() directly since it's already defined
        db.collection("voyages")
                .document(trip.getId())
                .update(
                        "description", trip.getDescription(),
                        "note", trip.getNote()
                )
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Voyage mis à jour"))
                .addOnFailureListener(e -> Log.e("Firestore", "Erreur mise à jour", e));
    }

    public void deleteTrip(Voyage trip) {
        String tripId = trip.getId();

        delAllPoi(tripId, new SimpleCallback() {
            @Override
            public void onDone() {

                delPath(tripId, new SimpleCallback() {
                    @Override
                    public void onDone() {

                        delAllPictures(trip.getTitre(), new SimpleCallback() {
                            @Override
                            public void onDone() {

                                db.collection("voyages")
                                    .document(tripId)
                                    .delete()
                                    .addOnSuccessListener(aVoid ->
                                            Log.d("Firestore", "Voyage supprimé"))
                                    .addOnFailureListener(e ->
                                            Log.e("Firestore", "Erreur suppression voyage", e));

                            }
                        });

                    }
                });

            }
        });
    }


    // POI

    public String getPOIKeyFromTrip(Voyage trip){
        return db.collection("voyages")
                .document(trip.getId())
                .collection("pois")
                .document()
                .getId();
    }

    public void addPOIToTrip(POI poi, Voyage trip){
        // Ajouter l'emplacement du POI
        poi.setIdPoi(getPOIKeyFromTrip(trip));

        db.collection("voyages")
                .document(trip.getId())
                .collection("pois")
                .document(poi.getIdPoi())
                .set(poi)
                .addOnSuccessListener(aVoid-> Log.d("FireStore", "Success of adding POI to trip in DB"))
                .addOnFailureListener(e -> Log.e("FireStore", "Error during adding POI to trip in DB : "+ e.getMessage()));
    }

    public void modifyPOIatTrip(POI poi, Voyage trip) {
        // Find the trip we are modifying the POI
        db.collection("voyages")
                .document(trip.getId())
                .collection("pois")
                .document(poi.getIdPoi())
                .set(poi)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "POI modifié"))
                .addOnFailureListener(e -> Log.e("Firestore", "Erreur modification POI", e));
    }
    public void delPOIfromTrip(POI poi, Voyage trip) {
        db.collection("voyages")
                .document(trip.getId())
                .collection("pois")
                .document(poi.getIdPoi())
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "POI supprimé"))
                .addOnFailureListener(e -> Log.e("Firestore", "Erreur suppression POI", e));
    }

    public void delAllPoi(String tripId, SimpleCallback callback) {
        db.collection("voyages")
            .document(tripId)
            .collection("pois")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {

                WriteBatch batch = db.batch();

                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    batch.delete(doc.getReference());
                }

                batch.commit()
                        .addOnSuccessListener(aVoid -> {
                            Log.d("Firestore", "POIs supprimés");
                            callback.onDone();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firestore", "Erreur batch POI", e);
                            callback.onDone(); // on continue quand même
                        });

            })
            .addOnFailureListener(e -> {
                Log.e("Firestore", "Erreur fetch POI", e);
                callback.onDone();
            });
    }

    public interface POIsCallback {
        void onSuccess(List<POI> pois);
        void onFailure(String error);
    }

    public void getPOIsForTrip(Voyage trip, POIsCallback callback) {

        db.collection("voyages")
                .document(trip.getId())
                .collection("pois")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    List<POI> pois = new ArrayList<>();

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        POI poi = document.toObject(POI.class);

                        if (poi != null) {
                            pois.add(poi);
                        }
                    }

                    callback.onSuccess(pois);
                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));
    }

    // Images

    public String getPictureKey(Voyage trip){
        return db.collection("voyages")
                .document(trip.getId())
                .collection("photos")
                .document()
                .getId();
    }

    public void sendPictureToDb(String pathToPictureInStorage, String tripId, String photoId){
        // Créer la donnée
        Map<String,Object> data = new HashMap<>();
        data.put("pathInStorage", pathToPictureInStorage);

        db.collection("voyages")
                .document(tripId)
                .collection("photos")
                .document(photoId)
                .set(data)
                .addOnSuccessListener(aVoid -> Log.d("FireStore", "Picture properly added to trip in DB"))
                .addOnFailureListener(e -> Log.e("FireStore", "Error during adding the picture to the DB :" + e.getMessage()));
    }

    public interface PhotoCallback {
        void onSuccess(List<String> photoPaths);
        void onFailure(String error);
    }
    public void getPhotosForTrip(Voyage trip, PhotoCallback callback) {

        db.collection("voyages")
                .document(trip.getId())
                .collection("photos")
                .get()
                .addOnSuccessListener(photoDocs -> {

                    List<String> photoPaths = new ArrayList<>();

                    for (DocumentSnapshot document : photoDocs.getDocuments()) {

                        String path = document.getString("pathInStorage");

                        if (path != null) {
                            photoPaths.add(path);
                        }
                    }

                    callback.onSuccess(photoPaths);

                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));
    }

    public void delAllPictures(String tripName, SimpleCallback callback) {
        CSC.getCSCRef().child(tripName).listAll()
            .addOnSuccessListener(listResult -> {

                if (listResult.getItems().isEmpty()) {
                    callback.onDone();
                    return;
                }

                final int[] remaining = {listResult.getItems().size()};

                for (StorageReference file : listResult.getItems()) {
                    file.delete()
                            .addOnSuccessListener(aVoid -> {
                               remaining[0]--;
                                if (remaining[0] == 0) {
                                    callback.onDone();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("CloudStorage", "Échec suppression fichier: " + file.getName());
                               remaining[0]--;
                                if (remaining[0] == 0) {
                                    callback.onDone();
                                }
                            });
                }
            })
            .addOnFailureListener(e -> {
                Log.e("CloudStorage", "Erreur listing files: " + e.getMessage());
               callback.onDone(); 
            });

    }

    public void delPicture(Photo pic){
        db.collection("voyages")
                .document(pic.getAssociatedTrip())
                .collection("photos")
                .document(pic.getPhotoIdInFirestore())
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Picture deleted properly"))
                .addOnFailureListener(e-> Log.d("Firestore", "Deletion failed"));
    }

    // Chemin

    public void addPathPoints(String tripId, List<Map<String, Object>> points) {

        WriteBatch batch = db.batch();

        for (Map<String, Object> point : points) {
            batch.set(
                    db.collection("voyages")
                            .document(tripId)
                            .collection("path")
                            .document(),
                    point
            );
        }

        batch.commit()
                .addOnSuccessListener(aVoid ->
                        Log.d("Firestore", "Points GPS ajoutés"))
                .addOnFailureListener(e ->
                        Log.e("Firestore", "Erreur ajout points GPS : " + e.getMessage()));
    }

    public interface PathCallback {
        void onSuccess(List<Map<String, Object>> points);
        void onFailure(String error);
    }

    public void getPathPoints(String tripId, PathCallback callback) {

        db.collection("voyages")
                .document(tripId)
                .collection("path")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    List<Map<String, Object>> points = new ArrayList<>();

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        points.add(document.getData());
                    }

                    callback.onSuccess(points);

                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));
    }



    public void delPath(String tripId, SimpleCallback callback) {
        db.collection("voyages")
            .document(tripId)
            .collection("path")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {

                WriteBatch batch = db.batch();

                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    batch.delete(doc.getReference());
                }

                batch.commit()
                        .addOnSuccessListener(aVoid -> {
                            Log.d("Firestore", "Path supprimé");
                            callback.onDone();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firestore", "Erreur path batch", e);
                            callback.onDone();
                        });

            })
            .addOnFailureListener(e -> callback.onDone());
    }



}
