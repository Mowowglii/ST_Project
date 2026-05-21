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
    private String idVoyage;

    public Photo() {}

    public Photo(StorageReference refPhoto, Uri uriPhoto){
        this.photoRef = refPhoto;
        this.imageURI = uriPhoto;
    }

    public StorageReference getRef(){
        return this.photoRef;
    }

    public void setRef(StorageReference photoRef) {
        this.photoRef = photoRef;
    }

    public Uri getImageURI() {
        return imageURI;
    }

    public void setImageURI(Uri imageURI) {
        this.imageURI = imageURI;
    }

    public String getAssociatedTrip() {
        return associatedTrip;
    }

    public void setAssociatedTrip(String associatedTrip) {
        this.associatedTrip = associatedTrip;
    }

    public POI getAssociatedPOI() {
        return associatedPOI;
    }

    public void setAssociatedPOI(POI associatedPOI) {
        this.associatedPOI = associatedPOI;
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

    public String getIdVoyage() {
        return idVoyage;
    }

    public void setIdVoyage(String idVoyage) {
        this.idVoyage = idVoyage;
    }
}
