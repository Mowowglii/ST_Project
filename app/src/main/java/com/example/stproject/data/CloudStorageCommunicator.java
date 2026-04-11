package com.example.stproject.data;


import android.net.Uri;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Queue;

public class CloudStorageCommunicator {
    private final FirebaseStorage CSCInstance = FirebaseStorage.getInstance(); // Instance Firebase Storage
    private final StorageReference CSCRef = CSCInstance.getReference(); // Référence Firebase Storage
    private Queue<Pair<StorageReference, Uri>> uploadQueue; // File des envois à la base de donnée

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
            // Envoie du fichier à la base de donnée
            UploadTask uTask = this.uploadQueue.remove().first.putFile(this.uploadQueue.remove().second);
            // En cas de succès de l'envoi du fichier à la base de donnée
            uTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Il faut envoyer la réference de stockage du fichier à la base de donnée Firestore
                }
            }).addOnFailureListener( new OnFailureListener(){
                @Override
                public void onFailure(@NonNull Exception e) {
                    // En cas d'échec de l'envoi du fichier à la base de donnée
                }
            } );
        }
    }

}
