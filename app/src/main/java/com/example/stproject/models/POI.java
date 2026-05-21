package com.example.stproject.models;

public class POI {

    private String titre;
    private String description;
    private int note;
    private double latitude;
    private double longitude;
    private String type;
    private String idVoyage; 

    public POI() {}

    public POI(String titre, String description, int note, double latitude, double longitude, String type) {
        this.titre = titre;
        this.description = description;
        this.note = note;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        this.note = note;
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
