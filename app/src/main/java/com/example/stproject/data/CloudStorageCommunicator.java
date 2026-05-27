package com.example.stproject.data;


import android.net.Uri;
import android.util.Log;


import androidx.annotation.NonNull;

import com.example.stproject.models.Photo;
import com.example.stproject.models.Voyage;
import com.example.stproject.utils.Quadruple;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public class CloudStorageCommunicator {
    private final FirebaseStorage CSCInstance = FirebaseStorage.getInstance(); // Instance Firebase Storage

    private final CFCommunicator cloudFirestoreCommunicator; // Instance du communicator Cloud Firestore
    private final StorageReference CSCRef = CSCInstance.getReference(); // Référence Firebase Storage
    private final Queue<Quadruple<StorageReference, Uri, String, StorageMetadata>> uploadQueue; // File des envois à la base de donnée

    // Constructeur de la classe
    public CloudStorageCommunicator(CFCommunicator cfc){
        this.cloudFirestoreCommunicator = cfc;
        uploadQueue = new LinkedList<>(); // Initialisation de la file des envois
    }

    private StorageReference fromStringToRef(String path){
        return CSCRef.child(path);
    }

    public StorageReference getCSCRef(){return this.CSCRef;}

    public interface PictureCallback{
        void onPictureRecovered(Photo picture);
    }

    public void addImageToTrip(Photo picture, Voyage trip){
        // Associer le voyage à la photo si ce n'est pas déjà fait
        picture.setAssociatedTrip(trip.getId());
        
        // Récupérer la clé dans Firestore
        picture.setPhotoIdInFirestore(this.cloudFirestoreCommunicator.getPictureKey(trip));

        // Déterminer le chemin de stockage
        StorageReference uploadRef = CSCRef.child(trip.getTitre())
                .child(Objects.requireNonNull(picture.getImageURI().getLastPathSegment()));

        // Créer la méta-donnée
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .setCustomMetadata("uri", picture.getImageURI().toString())
                .setCustomMetadata("tripId", picture.getAssociatedTrip())
                .setCustomMetadata("firestorePhotoId", picture.getPhotoIdInFirestore())
                .setCustomMetadata("timestamp", String.valueOf(picture.getTimeStamp()))
                .setCustomMetadata("longitude", String.valueOf(picture.getLongitude()))
                .setCustomMetadata("latitude", String.valueOf(picture.getLatitude()))
                .build();

        // Créer le Quadruplet StorageReference et Uri
        Quadruple<StorageReference, Uri, String, StorageMetadata> quadruple = new Quadruple<StorageReference, Uri, String, StorageMetadata>(uploadRef, picture.getImageURI(), trip.getId(), metadata);

        // Ajouter à la file d'attente
        this.uploadQueue.add(quadruple);
    }

    public void uploadQueueToDB(){
        // Boucle sur la file d'attente
        while(!this.uploadQueue.isEmpty()){
            // Récupérer l'élément de la file d'attente
            Quadruple<StorageReference, Uri, String, StorageMetadata> quadruple = this.uploadQueue.remove();
            // Envoie du fichier à la base de donnée
            UploadTask uTask = quadruple.first.putFile(quadruple.second, quadruple.fourth);
            // En cas de succès de l'envoi du fichier à la base de donnée
            uTask.addOnSuccessListener(taskSnapshot -> {
                // Il faut envoyer la réference de stockage du fichier à la base de donnée Firestore
                StorageReference fileRef = taskSnapshot.getStorage();
                
                // Récupérer les métadonnées depuis le snapshot (plus sûr que getResult())
                StorageMetadata metadata = taskSnapshot.getMetadata();
                String firestorePhotoId = (metadata != null) ? metadata.getCustomMetadata("firestorePhotoId") : null;

                // Récupérer le voyage concerné et l'ajouter à la Firestore Database
                cloudFirestoreCommunicator.sendPictureToDb(fileRef.getPath(), quadruple.third, firestorePhotoId);
            }).addOnFailureListener(e -> {
                // On récupère l'erreur et on la traite
                int errorCode = ((StorageException) e).getErrorCode();
                switch (errorCode){
                    case StorageException.ERROR_OBJECT_NOT_FOUND:
                        Log.e("CloudStorage", "Fichier non trouvé, il faut demander à l'utilisateur son nouvel emplacement");
                        break;
                    case StorageException.ERROR_BUCKET_NOT_FOUND:
                        Log.e("CloudStorage", "Bucket non trouvé");
                        break;
                    case StorageException.ERROR_CANCELED:
                        Log.e("CloudStorage", "L'utilisateur a annulé l'envoi du fichier");
                        break;
                    case StorageException.ERROR_INVALID_CHECKSUM:
                        Log.e("CloudStorage", "Fichier corrompu");
                        break;
                    case StorageException.ERROR_NOT_AUTHENTICATED:
                        Log.e("CloudStorage", "Authentification Ratée");
                        break;
                    case StorageException.ERROR_NOT_AUTHORIZED:
                        Log.e("CloudStorage", "Ecriture non autorisée");
                        break;
                    case StorageException.ERROR_PROJECT_NOT_FOUND:
                        Log.e("CloudStorage", "DB non ajoutée au projet");
                        break;
                    case StorageException.ERROR_QUOTA_EXCEEDED:
                        Log.e("CloudStorage", "Quota dépassé");
                        break;
                    case StorageException.ERROR_RETRY_LIMIT_EXCEEDED:
                        Log.e("CloudStorage", "Limite de quota de l'abonnement dépassé");
                        break;
                    case StorageException.ERROR_UNKNOWN:
                        Log.e("CloudStorage", "Problème inconnu");
                        break;
                }
            });
        }
    }

    public void getPhotoFromPath(String picturePathInStorage, PictureCallback callback){
        Task<StorageMetadata> mTask = CSCRef.child(picturePathInStorage).getMetadata();
        mTask.addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                // Construire l'objet Photo à partir des Meta-Données de la photo
                Photo picture = new Photo();
                picture.setImageURI(Uri.parse(storageMetadata.getCustomMetadata("uri")));
                picture.setAssociatedTrip(storageMetadata.getCustomMetadata("tripId"));
                picture.setPhotoIdInFirestore(storageMetadata.getCustomMetadata("firestorePhotoId"));
                picture.setTimeStamp(Long.parseLong(Objects.requireNonNull(storageMetadata.getCustomMetadata("timestamp"))));
                picture.setLongitude(Double.parseDouble(Objects.requireNonNull(storageMetadata.getCustomMetadata("longitude"))));
                picture.setLatitude(Double.parseDouble(Objects.requireNonNull(storageMetadata.getCustomMetadata("latitude"))));
                callback.onPictureRecovered(picture);
            }
        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("CloudStorage", "Error during recovering metadata of the picture :"+e.getMessage());
                    }
                });
    }

    public void deletePicture(String picturePathInStorage){
        // 1. Récupérer d'abord les métadonnées pour savoir quel document supprimer dans Firestore
        getPhotoFromPath(picturePathInStorage, new PictureCallback() {
            @Override
            public void onPictureRecovered(Photo picture) {
                // 2. Une fois les infos récupérées, supprimer le fichier dans Cloud Storage
                CSCRef.child(picturePathInStorage)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            // 3. Enfin, supprimer la référence dans Firestore
                            cloudFirestoreCommunicator.delPicture(picture);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("CloudStorage", "Error during deleting picture from Storage: "+e.getMessage());
                        });
            }
        });
    }
}
