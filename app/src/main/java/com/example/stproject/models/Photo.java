package com.example.stproject.models;

import android.net.Uri;

public class Photo {
    private Uri imageURI;
    private String associatedTripId;
    private POI associatedPOI;
    private long timeStamp;
    private double latitude;
    private double longitude;

    public Photo() {}

    public Photo(Uri uriPhoto){
        this.imageURI = uriPhoto;
    }
    public Uri getImageURI() {
        return imageURI;
    }

    public void setImageURI(Uri imageURI) {
        this.imageURI = imageURI;
    }

    public String getAssociatedTrip() {
        return associatedTripId;
    }

    public void setAssociatedTrip(String associatedTrip) {
        this.associatedTripId = associatedTrip;
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
        return associatedTripId;
    }

    public void setIdVoyage(String idVoyage) {
        this.associatedTripId = idVoyage;
    }
}
