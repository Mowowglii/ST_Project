package com.example.stproject.data;

import android.location.Location;
import android.util.Log;

import com.example.stproject.models.POI;
import com.example.stproject.models.Voyage;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.StorageReference;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudFirestoreCommunicator {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final CloudStorageCommunicator CSCommunicator = new CloudStorageCommunicator();

    private List<Voyage> sacvoyage = new ArrayList<>();
    private List<POI> sacpoi = new ArrayList<>();
    private String voyageidnow;
    private String idvoyageencours;


    public interface PhotoCallback {
        void onPhotosRecuperees(List<String> photo_paths);
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
        void onComplete(List<Map<String, Object>> path);
        void onError(String error);
    }

    // Partie Voyage

    public String cleUnique() {
        return db.collection("voyages")
                .document()
                .getId();
    }

    public void ajout_d_un_voyage(Voyage nouveauvoyage) {
        db.collection("voyages")
                .document(idvoyageencours)
                .set(nouveauvoyage)
                .addOnSuccessListener(aVoid ->
                        Log.d("Firestore", "Voyage ajouté avec succès"))
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Erreur ajout voyage : " + e.getMessage());

                });
    }
    public interface NomVoyageCallback {
        void onResult(boolean existe);
        void onError(String erreur);
    }

    public void verifierNomVoyageExiste(String titre, NomVoyageCallback callback) {
        db.collection("voyages")
                .whereEqualTo("titre", titre)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean existe = !queryDocumentSnapshots.isEmpty();
                    callback.onResult(existe);
                })
                .addOnFailureListener(e ->
                        callback.onError(e.getMessage()));
    }
    public String recuperer_id_du_voyage() {
        return this.voyageidnow;
    }
    public void demarrerNouveauVoyage(String voyageId) {
        this.idvoyageencours = voyageId;
    }

    public void arreterVoyageActif() {
        this.idvoyageencours = null;
    }
    
    public String getIdVoyageEnCours() {
        return this.idvoyageencours;
    }



    public void modification_dun_voyage(String desc, Integer note) {
        Map<String, Object> updates = new HashMap<>();
        if (desc != null) updates.put("description", desc);
        if (note != null) updates.put("note", note);
        db.collection("voyages").document(voyageidnow).update(updates);
    }

    public void supp_voyage(PhotoCallback callback) {
        if (voyageidnow == null) {
            callback.onFailure("Aucun voyage sélectionné.");
            return;
        }

        String idASupprimer = voyageidnow;
        if (idASupprimer.equals(idvoyageencours)) {
            arreterVoyageActif();
        }

        db.collection("voyages").document(idASupprimer).collection("pois").get()
                .addOnSuccessListener(snapshots -> {
                    List<POI> poisASupprimer = new ArrayList<>();
                    for (DocumentSnapshot ds : snapshots) {
                        POI p = ds.toObject(POI.class);
                        if (p != null) poisASupprimer.add(p);
                    }
                    supp_poi(poisASupprimer, new POICallback() {
                        @Override
                        public void onComplete(List<POI> pois) {
                            Log.d("Firestore", "POIs supprimés avec succès pendant la suppression du voyage.");
                        }

                        @Override
                        public void onError(String error) {
                            Log.e("Firestore", "Erreur suppression POIs pendant le supp_voyage: " + error);
                        }
                    });

                    supprimer_path(idASupprimer);

                    recup_photo_pour_une_liste_de_voyage(List.of(idASupprimer), new PhotoCallback() {
                        @Override
                        public void onPhotosRecuperees(List<String> paths) {
                            supprimer_liste_photo_path(paths, new PhotoCallback() {
                                @Override
                                public void onSuccess() {
                                    db.collection("voyages").document(idASupprimer).delete()
                                            .addOnSuccessListener(unused -> callback.onSuccess())
                                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                                }

                                @Override
                                public void onFailure(String e) {
                                    callback.onFailure(e);
                                }

                                @Override
                                public void onPhotosRecuperees(List<String> p) {
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
    public void ajout_poi(POI nouveauPoi, POICallback callback) {
        if (voyageidnow == null) {
            callback.onError("Impossible d'ajouter le POI : aucun voyage actif (voyageidnow est nul).");
            return;
        }
        nouveauPoi.setIdVoyage(voyageidnow);
        String documentIdUnique = nouveauPoi.getLatitude() + "_" + nouveauPoi.getLongitude() + "_" + voyageidnow;
        
        db.collection("voyages").document(voyageidnow)
                .collection("pois").document(documentIdUnique)
                .set(nouveauPoi)
                .addOnSuccessListener(aVoid -> callback.onComplete(null)) 
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void supp_poi(List<POI> poisASupprimer,POICallback callback) {
        if (poisASupprimer == null || poisASupprimer.isEmpty()) {
            callback.onComplete(new ArrayList<>()); 
            return;
        }
        WriteBatch batch = db.batch();
        for (POI cible : poisASupprimer) {
            String documentIdUnique = cible.getLatitude() + "_" + cible.getLongitude() + "_" + voyageidnow;
            if (cible.getIdVoyage() != null) {
                DocumentReference ref = db.collection("voyages").document(cible.getIdVoyage())
                        .collection("pois").document(documentIdUnique);
                batch.delete(ref);
            }
        }
        batch.commit()
            .addOnSuccessListener(aVoid -> callback.onComplete(null))
            .addOnFailureListener(e -> callback.onError(e.getMessage()));;
    }

    public void modification_dun_poi(POI ancien, String titre, String desc, String type, Integer note, POICallback callback) {
        if (voyageidnow == null) {
            callback.onError("Impossible de modifier le POI : aucun voyage actif (voyageidnow est nul).");
            return;
        }        
        String documentIdUnique = ancien.getLatitude() + "_" + ancien.getLongitude() + "_" + voyageidnow;
        Map<String, Object> up = new HashMap<>();
        if (titre != null) up.put("titre", titre);
        if (desc != null) up.put("description", desc);
        if (type != null) up.put("type", type);
        if (note != null) up.put("note", note);
        
        if (up.isEmpty()) {
            callback.onComplete(null) ;
            return;
        }
        db.collection("voyages").document(voyageidnow)
                .collection("pois").document(documentIdUnique)
                .update(up)
                .addOnSuccessListener(aVoid -> callback.onComplete(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
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
    public void ajouter_photo(String photo_paths) {
        if (voyageidnow == null || photo_paths == null || photo_paths.isEmpty()) {
        Log.e("Firestore", "Impossible d'ajouter la photo : voyageidnow est nul ou chemin vide.");
        return;
        }


        Map<String, Object> photodata = new HashMap<>();
        photodata.put("idvoyage",voyageidnow);
        photodata.put("photo_path", photo_paths);
        db.collection("voyages").document(voyageidnow)
                .collection("photos")
                .add(photodata)
                .addOnSuccessListener(documentReference -> Log.d("Firestore", "Photo ajoutée à la sous-collection avec l'ID : " + documentReference.getId()))
                .addOnFailureListener(e -> Log.e("Firestore", "Erreur ajout photo sous-collection : " + e.getMessage()));
        }

    public void supprimer_liste_photo_path(List<String> photo_paths, PhotoCallback callback) {
        if (photo_paths == null || photo_paths.isEmpty()) {
            callback.onSuccess();
            return;
        }

        List<Task<Void>> deletionTasks = new ArrayList<>();

        for (String path : photo_paths) {
            if (path != null && !path.isEmpty()) {
                Task<Void> deleteOriginalTask = CSCommunicator.deleteImage(path);
                deletionTasks.add(deleteOriginalTask);
            }
        }

        Tasks.whenAll(deletionTasks)  //verifier que deletionTasks a bien le meme nombre d elementd que photo_path
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }


    public void recup_photo_pour_une_liste_de_voyage(List<String> ids, PhotoCallback callback) {
            if (ids.isEmpty()) {
                callback.onPhotosRecuperees(new ArrayList<>());
                return;
            }

            db.collection("photos").whereIn("idvoyage", ids).get().addOnSuccessListener(docs -> {
                    List<String> listephotoPaths = new ArrayList<>();
                    for (DocumentSnapshot d : docs) {
                        String path = d.getString("photo_path"); 
                         if (path != null) {
                            listephotoPaths.add(path);
                        }
                    }
                callback.onPhotosRecuperees(listephotoPaths);
            }).addOnFailureListener(e -> {
                callback.onFailure(e.getMessage());
        });
    }

    // Partie Path
    public void ajout_path(List<Map<String, Object>> pointsGps) {
        // Ce qui est problématique avec l'attribut voyageidnow c'est que si l'attribut change de valeur pour x ou y raison alors que le suivi en temps réel est actif, les coordonnées GPS seront détournées.
        // idVoyageEnCours cree pour eviter le soucis merci de l avoir remarque
        if (idvoyageencours == null)
            return;
        WriteBatch batch = db.batch();
        for (Map<String, Object> pt : pointsGps) {
            DocumentReference ref = db.collection("voyages").document(idvoyageencours).collection("path").document();
            batch.set(ref, pt);
        }
        batch.commit();
    }

    public void recuperer_path_voyage(String voyageId, PathCallback callback) {
        db.collection("voyages").document(voyageId).collection("path").get()
                .addOnSuccessListener(docs -> {
                    List<Map<String, Object>> path = new ArrayList<>();
                    for (DocumentSnapshot d : docs) {
                        Map<String, Object> p = d.getData();
                        if (p != null) path.add(p);
                    }
                    callback.onComplete(path);
                }).addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void supprimer_path(String voyageId) {
        db.collection("voyages").document(voyageId).collection("path").get()
            .addOnSuccessListener(docs -> {
                WriteBatch batch = db.batch(); 
                for (DocumentSnapshot d : docs) {
                    batch.delete(d.getReference());
                }
                batch.commit();
            });
    }
}
