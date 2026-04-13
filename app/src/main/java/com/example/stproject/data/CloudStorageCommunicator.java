package com.example.stproject.data;


import android.net.Uri;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Queue;

public class CloudStorageCommunicator {
    private final FirebaseStorage CSCInstance = FirebaseStorage.getInstance(); // Instance Firebase Storage
    private final StorageReference CSCRef = CSCInstance.getReference(); // Référence Firebase Storage
    private Queue<Pair<StorageReference, Uri>> uploadQueue; // File des envois à la base de donnée

    // Constructeur de la classe
    public CloudStorageCommunicator(){
    }

    public void addTripImage(String imagePath, String tripName){
        // Créer l'Uri du fichier
        Uri uri = Uri.fromFile( new File(imagePath) );

        // Déterminer le chemin de stockage
        StorageReference uploadRef = CSCRef.child(tripName+uri.getLastPathSegment());

        // Créer la pair StorageReference et Uri
        Pair<StorageReference, Uri> pair = new Pair<>(uploadRef, uri);

        // Ajouter à la file d'attente
        this.uploadQueue.add(pair);
    }

    public void uploadQueueToDB(){
        // Boucle sur la file d'attente
        while(!this.uploadQueue.isEmpty()){
            // Récupérer l'élément de la file d'attente
            Pair<StorageReference, Uri> pair = this.uploadQueue.remove();
            // Envoie du fichier à la base de donnée
            UploadTask uTask = pair.first.putFile(pair.second);
            // En cas de succès de l'envoi du fichier à la base de donnée
            uTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Il faut envoyer la réference de stockage du fichier à la base de donnée Firestore
                }
            }).addOnFailureListener( new OnFailureListener(){
                @Override
                public void onFailure(@NonNull Exception e) {
                    // On récupère l'erreur et on la traite
                    int errorCode = ((StorageException) e).getErrorCode();
                    switch (errorCode){
                        case StorageException.ERROR_OBJECT_NOT_FOUND:
                            // Fichier non trouvé, il faut demander à l'utilisateur son nouvel emplacement
                            break;
                        case StorageException.ERROR_BUCKET_NOT_FOUND:
                            // Bucket non trouvé
                            break;
                        case StorageException.ERROR_CANCELED:
                            // L'utilisateur a annulé l'envoi du fichier
                            break;
                        case StorageException.ERROR_INVALID_CHECKSUM:
                            // Fichier corrompu
                            break;
                        case StorageException.ERROR_NOT_AUTHENTICATED:
                            // A priori on ne rentre pas dans cette case
                            break;
                        case StorageException.ERROR_NOT_AUTHORIZED:
                            // En fonction des règles de stockage, on peut avoir cette erreur
                            break;
                        case StorageException.ERROR_PROJECT_NOT_FOUND:
                            // A priori on ne rentre pas dans cette case
                            break;
                        case StorageException.ERROR_QUOTA_EXCEEDED:
                            // Problème de quota
                            break;
                        case StorageException.ERROR_RETRY_LIMIT_EXCEEDED:
                            // Problème de quota (en fonction des tarifs Cloud Storage)
                            break;
                        case StorageException.ERROR_UNKNOWN:
                            // Problème inconnu
                            break;
                    }
                }
            } );
        }
    }

}
