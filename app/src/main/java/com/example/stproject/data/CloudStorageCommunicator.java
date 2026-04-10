package com.example.stproject.data;


import android.net.Uri;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class CloudStorageCommunicator {
    private FirebaseStorage CSCInstance = FirebaseStorage.getInstance(); // Instance Firebase Storage
    private StorageReference CSCRef = CSCInstance.getReference(); // Référence Firebase Storage

    public void uploadTripImage(String imagePath, String tripName){
        // Créer l'Uri du fichier
        Uri uri = Uri.fromFile( new File(imagePath) );

        // Déterminer le chemin de stockage
        StorageReference uploadRef = CSCRef.child(tripName+uri.getLastPathSegment());


    }

}
