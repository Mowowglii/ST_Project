package com.example.stproject.data;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.stproject.models.POI;
import com.example.stproject.models.Voyage;
import com.example.stproject.models.Photo;

import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CloudFirestoreCommunicator {

    private DatabaseReference databaseRef;

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


    public CloudFirestoreCommunicator() {
        databaseRef = FirebaseDatabase.getInstance().getReference();
    }


    public List<Voyage> getSacVoyage() {
        return sacvoyage;
    }
    public List<POI> getSacPoi() {
        return sacpoi;
    }

    public void setVoyageIdNow(String id) {
        this.voyageidnow = id;
    }

    public void ajout_d_un_voyage(String nomVoyage) {
        String uniqueID = databaseRef.child("voyages").push().getKey();
        if (uniqueID != null) {
            databaseRef.child("voyages")
                    .child(uniqueID)
                    .child("nom")
                    .setValue(nomVoyage);
        }
    }

    public void recuperer_des_voyages(List<String> listeId, VoyageCallback callback) {
        sacvoyage.clear();
        for (String id : listeId) {
            databaseRef.child("voyages")
                    .child(id)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Voyage voyage = snapshot.getValue(Voyage.class);

                                if (voyage != null) {
                                    sacvoyage.add(voyage);
                                }
                            }
                            callback.onComplete(sacvoyage);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            callback.onError(error.getMessage());
                        }
                    });
        }
    }

    public void tous_les_voyages(VoyageCallback callback) {
        databaseRef.child("voyages")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> listeId = new ArrayList<>();
                        for (DataSnapshot dshot : snapshot.getChildren()) {
                            String id = dshot.getKey();
                            if (id != null) listeId.add(id);
                        }
                        if (!listeId.isEmpty()) {
                            recuperer_des_voyages(listeId, callback);
                        } else {
                            callback.onComplete(new ArrayList<>());
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    public void ajout_poi(POI nouveauPoi) {
        if (voyageidnow != null) {
            databaseRef.child("voyages")
                    .child(voyageidnow)
                    .child("pois")
                    .push()
                    .setValue(nouveauPoi);

            Log.d("POI", "Ajout OK");
        } else {
            Log.e("POI", "voyageId null");
        }
    }

    public void supp_poi(List<POI> poisASupprimer) {
        if (poisASupprimer == null || poisASupprimer.isEmpty()) {
            Log.e("POI", "Liste vide");
            return;
        }
        databaseRef.child("voyages")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot voyageSnap : snapshot.getChildren()) {

                            DataSnapshot poisSnap = voyageSnap.child("pois");

                            for (DataSnapshot poiSnap : poisSnap.getChildren()) {

                                POI p = poiSnap.getValue(POI.class);

                                if (p != null) {
                                    for (POI cible : poisASupprimer) {

                                        if (Math.abs(p.getLatitude() - cible.getLatitude()) < 0.00001 &&
                                                Math.abs(p.getLongitude() - cible.getLongitude()) < 0.00001) {

                                            poiSnap.getRef().removeValue();
                                        }
                                    }
                                }
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("POI", error.getMessage());
                    }
                });
    }

    public void supp_voyage(List<String> listeId) {
        if (listeId == null || listeId.isEmpty()) {
            Log.e("VOYAGE", "Liste vide");
            return;
        }
        for (String id : listeId) {
            databaseRef.child("voyages")
                    .child(id)
                    .removeValue();
        }
    }

    public void recuperer_une_liste_de_poi(POICallback callback) {
        sacpoi.clear();
        databaseRef.child("voyages")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dshot : snapshot.getChildren()) {
                            DataSnapshot poisSnap = dshot.child("pois");
                            for (DataSnapshot poiSnap : poisSnap.getChildren()) {
                                POI p = poiSnap.getValue(POI.class);
                                if (p != null) {
                                    sacpoi.add(p);
                                }
                            }
                        }
                        callback.onComplete(sacpoi);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    public List<POI> filtrer_par_voyages(List<String> listeVoyageIds) {
        List<POI> resultats = new ArrayList<>();
        for (Voyage v : sacvoyage) {
            if (listeVoyageIds.contains(v.getId())) {
                resultats.addAll(v.getListePois());
            }
        }
        return resultats;
    }

    public List<POI> filtrer_par_types(List<String> typesVoulus) {
        List<POI> resultats = new ArrayList<>();
        for (POI p : sacpoi) {
            if (typesVoulus.contains(p.getType())) {
                resultats.add(p);
            }
        }
        return resultats;
    }

    public void afficher_poi(List<POI> listeaaffichee) {
        if (listeaaffichee == null || listeaaffichee.isEmpty()) {
            Log.d("POI", "Liste vide");
            return;
        }
        StringBuilder description = new StringBuilder();
        for (POI p : listeaaffichee) {
            description.append(p.getTitre()).append("\n");
        }
        Log.d("POI", description.toString());
    }

public void modification_dun_voyage(String voyageId, String nouveautitre, String nouvelledescription, Integer nouvellenote) {
    if (voyageId == null || voyageId.isEmpty()) {
        Log.e("Firebase_Update", "ID du voyage nul ou vide");
        return;
    }
    HashMap<String, Object> misesAJour = new HashMap<>();
    if (nouveautitre != null && !nouveautitre.isEmpty()) {
        misesAJour.put("titre", nouveautitre);
    }
    if (nouvelledescription != null) {
        misesAJour.put("description", nouvelledescription);
    }
    if (nouvellenote != null) {
        misesAJour.put("note", nouvellenote);
    }
    if (!misesAJour.isEmpty()) {
        databaseRef.child("voyages").child(voyageId)
            .updateChildren(misesAJour)
            .addOnSuccessListener(aVoid -> {
                Log.d("modification voyage reussi","misa a jour reussi pour le voyage:"+voyageId);
            })
            .addOnFailureListener(e -> {
                Log.e("modification voyage rate","erreur de la mise a jour d un voyage"+voyageId+":"+e.getMessage());
            });
    } else {
            Log.d("modification du voyage ","aucune modif effectuer champs null ");
    }
}
  
    public void modification_dun_poi(POI ancienpoi,String nouveautitre,String nouveaudescription, String nouveautype , Integer nouvellenote){
        if(ancienpoi==null){
            Log.d("erreur modif poi","pas d information sur le poi a modifier ")
            return;
        }
        databaseRef.child("voyages").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot){
                for (DataSnapshot voyageSnapshot :snapshot.getChildren()){
                    DataSnapshot poiSnapshot=voyageSnapshot.child("pois");
                    for (DataSnapshot poisSnapshot : poiSnapshot.getChildren()){
                        POI p = poisSnapshot.getValue(POI.class);
                        if (p!= null && p.getLatitude()==ancienpoi.getLatitude() && p.getLongitude()==ancienpoi.getLongitude()){
                            HashMap<String,Object> miseajour=new HashMap<>();
                            if (nouveautitre != null) miseajour.put("titre", nouveautitre);
                            if (nouveaudescription != null)  miseajour.put("description", nouveaudescription);
                            if (nouveautype != null)  miseajour.put("type", nouveautype);
                            if (nouvellenote != null)  miseajour.put("note", nouvellenote);

                            if(!miseajour.isEmpty()){
                                poiSnapshot.getRef().updateChildren(miseajour).addOnSuccessListener(aVoid->{
                                    Log.d("modification poi reussi","reussite de la modification du poi"+p.getLatitude()+","+p.getLongitude());
                                })
                                .addOnFailureListener(e->{
                                    Log.e("modification poi rate","erreur de la modif poi "+e.getMessage());
                                });
                            }else{
                                Log.d("modification poi ras","aucun changement detecte pour le poi "+p.getLatitude() +","+p.getLongitude());
                            }
                            return;
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError erreur){
                Log.e("modife poi","erreur lors de la modification du poi:"+erreur.getMessage());
            }
        });
    }

    public void ajouter_photo(List<Photo> listephoto, PhotoCallback callback) {
        if (listephoto == null || listephoto.isEmpty()) {
            callback.onFailure("liste vide");
            return;
        }
        final int[] count = {0};
        for (Photo photo : listephoto) {
            databaseRef.child("photos")
                    .push()
                    .setValue(photo)
                    .addOnSuccessListener(aVoid -> {
                        count[0]++;
                        if (count[0] == listephoto.size()) {
                            callback.onSuccess();
                        }
                    })
                    .addOnFailureListener(e ->
                            callback.onFailure(e.getMessage()));
        }
    }

    public void supprimer_liste_photo(List<Photo> listephotoasupp, PhotoCallback callback) {
        if (listephotoasupp == null || listephotoasupp.isEmpty()) {
            callback.onFailure("liste vide");
            return;
        }
        databaseRef.child("photos").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dshot : snapshot.getChildren()) {
                            Photo photo = dshot.getValue(Photo.class);
                            if (photo != null) {
                                for (Photo cible : listephotoasupp) {
                                    if (photo.getUrl().equals(cible.getUrl())) {
                                        FirebaseStorage.getInstance()
                                                .getReference()
                                                .child(photo.getStoragePath())
                                                .delete()
                                                .addOnSuccessListener(aVoid ->
                                                        dshot.getRef().removeValue()
                                                                .addOnSuccessListener(unused ->
                                                                        callback.onSuccess()))
                                                .addOnFailureListener(e ->
                                                        callback.onFailure(e.getMessage()));
                                    }
                                }
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });
    }

    public void recup_photo_pour_une_liste_de_voyage(List<String> voyageIds, PhotoCallback callback) {
        databaseRef.child("photos").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Photo> result = new ArrayList<>();
                        for (DataSnapshot dshot : snapshot.getChildren()) {
                            Photo p = dshot.getValue(Photo.class);
                            if (p != null && voyageIds.contains(p.getVoyageId())) {
                                result.add(p);
                            }
                        }
                        callback.onPhotosRecuperees(result);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });
    }

    public void recup_photos_proches_pois(List<POI> pois, PhotoCallback callback) {
        databaseRef.child("photos").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Photo> result = new ArrayList<>();
                        for (DataSnapshot dshot : snapshot.getChildren()) {
                            Photo photo = dshot.getValue(Photo.class);
                            if (photo != null) {
                                for (POI poi : pois) {
                                    float[] res = new float[1];
                                    Location.distanceBetween(
                                            poi.getLatitude(), poi.getLongitude(),
                                            photo.getLatitude(), photo.getLongitude(),
                                            res
                                    );
                                    if (res[0] < 50) { 
                                        result.add(photo);
                                        break;
                                    }
                                }
                            }
                        }
                        callback.onPhotosRecuperees(result);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });
    }

    public void recup_photos_par_type_poi(List<String> types, PhotoCallback callback) {
        List<POI> poisFiltres = filtrer_par_types(types);
        recup_photos_proches_pois(poisFiltres, callback);
    }


    public void send_loc(List<Location> data) {
        // pas compris
    }
}