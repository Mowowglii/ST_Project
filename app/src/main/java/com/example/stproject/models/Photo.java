package com.example.stproject.models;

import com.google.firebase.storage.StorageReference;
import android.net.Uri;


public class Photo {
    private final StorageReference photoRef;

    private final Uri imageURI;

    private String associatedTrip;

    private POI associatedPOI;

    public Photo(StorageReference refPhoto, Uri uriPhoto){

        this.photoRef = refPhoto;
        this.imageURI = uriPhoto;

        StorageReference tripName = refPhoto.getParent();

    }

    public StorageReference getRef(){
        return this.dbRef;
    }
}