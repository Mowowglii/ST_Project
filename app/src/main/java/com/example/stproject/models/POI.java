package com.example.stproject.models;

import java.util.ArrayList;
import java.util.List;

public class POI {

    private String titre;
    private String description;
    private int note;
    private double latitude;
    private double longitude;
    private String type;
    private String idVoyage;
    private List<String> photos;

    public POI() {

        photos = new ArrayList<>();
    }

    public POI(
            String titre,
            String description,
            int note,
            double latitude,
            double longitude,
            String type
    ) {

        this.titre = titre;
        this.description = description;
        this.note = note;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;

        this.photos = new ArrayList<>();
    }

    public String getTitre() {
        return titre;
    }

    public String getDescription() {
        return description;
    }

    public int getNote() {
        return note;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getType() {
        return type;
    }

    public String getIdVoyage() {
        return idVoyage;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setIdVoyage(String idVoyage) {
        this.idVoyage = idVoyage;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setNote(int note) {
        this.note = note;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos;
    }
}