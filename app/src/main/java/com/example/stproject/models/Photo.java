package com.example.stproject.models;

import com.google.firebase.storage.StorageReference;
import android.net.Uri;


public class Photo {
    private StorageReference photoRef;
    private Uri imageURI;
    private String associatedTrip;
    private POI associatedPOI;
    private double latitude;
    private double longitude;

    public Photo() {}

    public Photo(StorageReference refPhoto, Uri uriPhoto){
        this.photoRef = refPhoto;
        this.imageURI = uriPhoto;
    }

    public StorageReference getRef(){
        return this.photoRef;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Uri getImageURI() {
        return imageURI;
    }

    public void setImageURI(Uri imageURI) {
        this.imageURI = imageURI;
    }
}