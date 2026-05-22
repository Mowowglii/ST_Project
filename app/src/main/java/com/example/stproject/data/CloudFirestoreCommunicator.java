package com.example.stproject.data;

import android.location.Location;
import android.util.Log;

import com.example.stproject.models.POI;
import com.example.stproject.models.Path;
import com.example.stproject.models.Photo;
import com.example.stproject.models.Voyage;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudFirestoreCommunicator {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<Voyage> sacvoyage = new ArrayList<>();
    private List<POI> sacpoi = new ArrayList<>();
    private String voyageidnow;

    public interface PhotoCallback {
        void onPhotosRecuperees(List<Photo> photos);
        void onSuccess();
        void onFailure(String erreur);
    }

    public interface POICallback {
        void onComplete(List<POI> pois);
        void onError(String error);
    }

    public interface VoyageCallback {
        void onComplete(List<Voyage> voyages);
        void onError(String error);
    }

    public interface PathCallback {
        void onComplete(List<Path> path);
        void onError(String error);
    }

    // Partie Voyage
    public void ajout_d_un_voyage(String nomVoyage) {
        String uniqueID = db.collection("voyages").document().getId();
        Voyage nouveauVoyage = new Voyage();
        nouveauVoyage.setId(uniqueID);
        nouveauVoyage.setTitre(nomVoyage);
        nouveauVoyage.setDescription("");
        nouveauVoyage.setNote(0);

        db.collection("voyages").document(uniqueID).set(nouveauVoyage)
                .addOnSuccessListener(aVoid -> this.voyageidnow = uniqueID)
                .addOnFailureListener(e -> Log.e("Firestore", "Erreur création voyage: " + e.getMessage()));
    }

    public String recuperer_id_du_voyage() {
        return this.voyageidnow;
    }

    public String recoverIdFromTripName(String tripName){
        final String[] docId = new String[1];
        db.collection("voyages")
                .whereEqualTo("titre", tripName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null){
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot.size() > 1){
                            Log.e("Firestore","Too much trip has the same name");
                        } else {
                            docId[0] = querySnapshot.getDocuments().get(0).getId();
                        }
                    }
                });
        return docId[0];
    }

    public void modification_dun_voyage(String voyageId, String titre, String desc, Integer note) {
        Map<String, Object> updates = new HashMap<>();
        if (titre != null) updates.put("titre", titre);
        if (desc != null) updates.put("description", desc);
        if (note != null) updates.put("note", note);
        db.collection("voyages").document(voyageId).update(updates);
    }

    public void supp_voyage(String voyageId, PhotoCallback callback) {
        db.collection("voyages").document(voyageId).collection("pois").get()
                .addOnSuccessListener(snapshots -> {
                    List<POI> poisASupprimer = new ArrayList<>();
                    for (DocumentSnapshot ds : snapshots) {
                        POI p = ds.toObject(POI.class);
                        if (p != null) poisASupprimer.add(p);
                    }
                    supp_poi(poisASupprimer);
                });
        supprimer_path(voyageId);

        recup_photo_pour_une_liste_de_voyage(List.of(voyageId), new PhotoCallback() {
            @Override
            public void onPhotosRecuperees(List<Photo> photos) {
                supprimer_liste_photo(photos, new PhotoCallback() {
                    @Override
                    public void onSuccess() {
                        db.collection("voyages").document(voyageId).delete()
                                .addOnSuccessListener(unused -> callback.onSuccess());
                    }

                    @Override
                    public void onFailure(String e) {
                        callback.onFailure(e);
                    }

                    @Override
                    public void onPhotosRecuperees(List<Photo> p) {
                    }
                });
            }

            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(String e) {
                callback.onFailure(e);
            }
        });
    }

    public void tous_les_voyages(VoyageCallback callback) {
        db.collection("voyages").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                sacvoyage.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    sacvoyage.add(doc.toObject(Voyage.class));
                }
                callback.onComplete(sacvoyage);
            } else {
                callback.onError(task.getException() != null ? task.getException().getMessage() : "Erreur inconnue");
            }
        });
    }

    // Partie POI
    public void ajout_poi(POI nouveauPoi) {
        if (voyageidnow != null) {
            nouveauPoi.setIdVoyage(voyageidnow);
            String documentIdUnique = nouveauPoi.getLatitude() + "_" + nouveauPoi.getLongitude() + "_" + voyageidnow;
            db.collection("voyages").document(voyageidnow).collection("pois").document(documentIdUnique).set(nouveauPoi);
        }
    }

    public void supp_poi(List<POI> poisASupprimer) {
        if (poisASupprimer == null || poisASupprimer.isEmpty()) return;

        WriteBatch batch = db.batch();
        for (POI cible : poisASupprimer) {
            String documentIdUnique = cible.getLatitude() + "_" + cible.getLongitude();
            if (cible.getIdVoyage() != null) {
                DocumentReference ref = db.collection("voyages").document(cible.getIdVoyage())
                        .collection("pois").document(documentIdUnique);
                batch.delete(ref);
            }
        }
        batch.commit();
    }

    public void modification_dun_poi(POI ancien, String titre, String desc, String type, Integer note) {
        String documentIdUnique = ancien.getLatitude() + "_" + ancien.getLongitude();
        Map<String, Object> up = new HashMap<>();
        if (titre != null) up.put("titre", titre);
        if (desc != null) up.put("description", desc);
        if (type != null) up.put("type", type);
        if (note != null) up.put("note", note);
        if (!up.isEmpty() && voyageidnow != null) {
            db.collection("voyages").document(voyageidnow)
                    .collection("pois").document(documentIdUnique)
                    .update(up)
                    .addOnFailureListener(e -> Log.e("Firestore", "Erreur modif POI: " + e.getMessage()));
        }
    }

    public void recuperer_une_liste_de_poi(POICallback callback) {
        db.collectionGroup("pois").get().addOnSuccessListener(snapshots -> {
            sacpoi.clear();
            for (DocumentSnapshot d : snapshots) {
                POI p = d.toObject(POI.class);
                if (p != null) sacpoi.add(p);
            }
            callback.onComplete(sacpoi);
        }).addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public List<POI> filtrer_par_voyages(List<POI> source, List<String> ids) {
        List<POI> res = new ArrayList<>();
        for (POI p : source) {
            if (p.getIdVoyage() != null && ids.contains(p.getIdVoyage())) {
                res.add(p);
            }
        }
        return res;
    }

    public List<POI> filtrer_par_types(List<POI> source, List<String> types) {
        List<POI> res = new ArrayList<>();
        for (POI p : source) {
            if (p.getType() != null && types.contains(p.getType())) {
                res.add(p);
            }
        }
        return res;
    }

    public List<POI> filtrer_par_note(List<POI> source, int note) {
        List<POI> res = new ArrayList<>();
        for (POI p : source) {
            if (p.getNote() >= note) {
                res.add(p);
            }
        }
        return res;
    }

    // Partie Photo
    public void ajouter_photo(StorageReference photoRef, String tripId) {
        // Récupérer les photos du voyage concerné
        Object unknownObject = db.collection("voyages")
                .document(tripId)
                .get()
                .getResult()
                .get("listePhotos");

        if (unknownObject instanceof List){
            @SuppressWarnings("unchecked")
            List<StorageReference> tripPictures = (List<StorageReference>) unknownObject;
            // Ajouter la référence de la photo
            tripPictures.add(photoRef);

            // Mettre à jour le contenu de la liste de photo du voyage
            db.collection("voyages")
                    .document(tripId)
                    .update("listePhotos", tripPictures);
        }
    }

    public void supprimer_liste_photo(List<Photo> liste, PhotoCallback callback) {
        if (liste.isEmpty()) {
            callback.onSuccess();
            return;
        }
        // Logic to delete from Storage should go here, but for now we just call success
        callback.onSuccess();
    }

    public void recup_photos_proches_pois(List<POI> pois, PhotoCallback callback) {
        List<String> ids = new ArrayList<>();
        for (POI p : pois) if (p.getIdVoyage() != null && !ids.contains(p.getIdVoyage())) ids.add(p.getIdVoyage());

        recup_photo_pour_une_liste_de_voyage(ids, new PhotoCallback() {
            @Override
            public void onPhotosRecuperees(List<Photo> toutes) {
                List<Photo> result = new ArrayList<>();
                for (Photo ph : toutes) {
                    for (POI poi : pois) {
                        float[] dist = new float[1];
                        Location.distanceBetween(poi.getLatitude(), poi.getLongitude(), ph.getLatitude(), ph.getLongitude(), dist);
                        if (dist[0] <= 25) {
                            result.add(ph);
                            break;
                        }
                    }
                }
                callback.onPhotosRecuperees(result);
            }

            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(String e) {
            }
        });
    }

    public void recup_photo_pour_une_liste_de_voyage(List<String> ids, PhotoCallback callback) {
        if (ids.isEmpty()) {
            callback.onPhotosRecuperees(new ArrayList<>());
            return;
        }
        db.collection("photos").whereIn("idVoyage", ids).get().addOnSuccessListener(docs -> {
            List<Photo> res = new ArrayList<>();
            for (DocumentSnapshot d : docs) res.add(d.toObject(Photo.class));
            callback.onPhotosRecuperees(res);
        });
    }

    // Partie Path
    public void ajout_path(List<Map<String, Object>> pointsGps) {
        if (voyageidnow == null) return;
        WriteBatch batch = db.batch();
        for (Map<String, Object> pt : pointsGps) {
            DocumentReference ref = db.collection("voyages").document(voyageidnow).collection("path").document();
            batch.set(ref, pt);
        }
        batch.commit();
    }

    public void recuperer_path_voyage(String voyageId, PathCallback callback) {
        db.collection("voyages").document(voyageId).collection("path").get()
                .addOnSuccessListener(docs -> {
                    List<Path> path = new ArrayList<>();
                    for (DocumentSnapshot d : docs) {
                        Path p = d.toObject(Path.class);
                        if (p != null) path.add(p);
                    }
                    callback.onComplete(path);
                }).addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void supprimer_path(String voyageId) {
        db.collection("voyages").document(voyageId).collection("path").get()
                .addOnSuccessListener(docs -> {
                    for (DocumentSnapshot d : docs) d.getReference().delete();
                });
    }
}
