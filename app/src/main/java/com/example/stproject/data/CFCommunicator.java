package com.example.stproject.data;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.stproject.models.POI;
import com.example.stproject.models.Voyage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CFCommunicator {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final CloudStorageCommunicator CSC = new CloudStorageCommunicator(this);

    // Voyage
    public String getTripKey(){
        return db.collection("voyages")
                .document()
                .getId();
    }

    public String getKeyFromTripTitle(Voyage trip){
        // Faire une query pour trouver le voyage qui contient ce nom
        List<DocumentSnapshot> result = db.collection("voyages")
                .whereEqualTo("titre", trip.getTitre())
                .get()
                .getResult()
                .getDocuments();
        return result.get(0).getId();
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
        // Créer l'emplacement pour le voyage
        String id = getTripKey();

        db.collection("voyages")
                .document(id)
                .set(trip)
                .addOnSuccessListener(aVoid ->
                        Log.d("Firestore", "Voyage ajouté avec succès"))
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Erreur ajout voyage : " + e.getMessage());

                });
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
        // First, find the document ID asynchronously by the trip title
        db.collection("voyages")
                .whereEqualTo("titre", trip.getTitre())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the ID of the first matching document
                        String docId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Update the document
                        db.collection("voyages")
                                .document(docId)
                                .update(
                                        "description", trip.getDescription(),
                                        "note", trip.getNote()
                                )
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Voyage updated successfully"))
                                .addOnFailureListener(e -> Log.e("Firestore", "Error updating voyage: " + e.getMessage()));
                    } else {
                        Log.e("Firestore", "Voyage not found for title: " + trip.getTitre());
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error searching for voyage: " + e.getMessage()));
    }

    public void deleteTrip(Voyage trip){
        // Définir l'id à supprimer
        String delId = getKeyFromTripTitle(trip);

        // Supprimer les POIs des voyages
        delAllPoi(delId);

        // Supprimer les coordonnées de voyages
        delPath(delId);

        // Supprimer Photos
        delAllPictures(trip.getTitre());

        // suppression du voyage
        db.collection("voyages")
                .document(delId)
                .delete()
                .addOnSuccessListener(aVoid ->
                        Log.d("Firestore", "Voyage supprimé avec succès"))
                .addOnFailureListener(e ->
                        Log.e("Firestore", "Erreur suppression voyage : " + e.getMessage()));
    }

    // POI

    public void addPOIToTrip(POI poi, Voyage trip){
        db.collection("voyages")
                .document(getKeyFromTripTitle(trip))
                .collection("pois")
                .document()
                .set(poi)
                .addOnSuccessListener(aVoid->{
                    Log.d("FireStore", "POI ajouté au voyage dans la base de donnée");
                })
                .addOnFailureListener(e->{
                    Log.e("FireStore", "erreur ajout :" + e.getMessage());
                });
    }

    public void modifyPOIatTrip(POI poi, Voyage trip){
        // WARNING: getKeyFromTripTitle(trip) uses .getResult() which blocks the UI thread.
        // It is better to use an asynchronous chain like this:
        db.collection("voyages")
                .whereEqualTo("titre", trip.getTitre())
                .get()
                .addOnSuccessListener(tripQuery -> {
                    if (!tripQuery.isEmpty()) {
                        String tripId = tripQuery.getDocuments().get(0).getId();

                        // Now find the specific POI in that trip
                        db.collection("voyages")
                                .document(tripId)
                                .collection("pois")
                                .whereEqualTo("longitude", poi.getLongitude())
                                .whereEqualTo("latitude", poi.getLatitude())
                                .get()
                                .addOnSuccessListener(poiQuery -> {
                                    if (!poiQuery.isEmpty()) {
                                        // GET THE DOCUMENT REFERENCE AND UPDATE
                                        DocumentSnapshot document = poiQuery.getDocuments().get(0);
                                        document.getReference().set(poi)
                                                .addOnSuccessListener(aVoid -> Log.d("FireStore", "POI modifié avec succès"))
                                                .addOnFailureListener(e -> Log.e("FireStore", "Erreur modification POI : " + e.getMessage()));
                                    } else {
                                        Log.e("FireStore", "POI introuvable avec ces coordonnées");
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> Log.e("FireStore", "Erreur recherche voyage : " + e.getMessage()));
    }

    public void delPOIfromTrip(POI poi, Voyage trip){
        db.collection("voyages")
                .document(getKeyFromTripTitle(trip))
                .collection("pois")
                .whereEqualTo("latitude", poi.getLatitude())
                .whereEqualTo("longitude", poi.getLongitude())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        queryDocumentSnapshots.getDocuments()
                                .get(0)
                                .getReference()
                                .delete()
                                .addOnSuccessListener(aVoid ->
                                        Log.d("Firestore", "POI supprimé avec succès"))
                                .addOnFailureListener(e ->
                                        Log.e("Firestore", "Erreur suppression POI", e));
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("Firestore", "Erreur recherche POI", e));
    }

    private void delAllPoi(String tripId){
        db.collection("voyages")
                .document(tripId)
                .collection("pois")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Create a batch to perform multiple deletions in one request
                    WriteBatch batch = db.batch();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.delete(doc.getReference());
                    }

                    // Execute the batch
                    batch.commit()
                            .addOnSuccessListener(aVoid -> Log.d("Firestore", "Sub-collection 'pois' deleted successfully"))
                            .addOnFailureListener(e -> Log.e("Firestore", "Error committing batch delete", e));
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching POIs for deletion", e));
    }
    public interface POIsCallback {
        void onSuccess(List<POI> pois);
        void onFailure(String error);
    }

    public void getPOIsForTrip(Voyage trip, POIsCallback callback) {

        db.collection("voyages")
                .document(getKeyFromTripTitle(trip))
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

    public void sendPictureToDb(String pathToPicture, String tripName){
        Uri fileUri = Uri.fromFile(new File(pathToPicture));

        StorageReference pictureRef = CSC.getCSCRef()
                .child(tripName)
                .child(fileUri.getLastPathSegment());

        pictureRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot ->
                        Log.d("CloudStorage", "Image envoyée avec succès"))
                .addOnFailureListener(e ->
                        Log.e("CloudStorage", "Erreur envoi image : " + e.getMessage()));
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

                        String path = document.getString("photo_path");

                        if (path != null) {
                            photoPaths.add(path);
                        }
                    }

                    callback.onSuccess(photoPaths);

                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));
    }

    public void delAllPictures(String tripName) {
        // Asynchronously list all files in the trip folder
        CSC.getCSCRef().child(tripName).listAll()
                .addOnSuccessListener(listResult -> {
                    // Iterate through all files and delete them
                    for (StorageReference file : listResult.getItems()) {
                        file.delete().addOnFailureListener(e ->
                                Log.e("CloudStorage", "Failed to delete " + file.getName()));
                    }
                })
                .addOnFailureListener(e -> Log.e("CloudStorage", "Error listing files: " + e.getMessage()));
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



    private void delPath(String tripId){
        db.collection("voyages")
                .document(tripId)
                .collection("path")
                .get()
                .addOnCompleteListener(docs ->{
                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot d : docs.getResult().getDocuments()) {
                        batch.delete(d.getReference());
                    }
                    batch.commit();
                });
    }


}
