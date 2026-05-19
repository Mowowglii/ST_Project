package com.example.stproject.models;

public class POI {

    private String titre;
    private String description;
    private int note;
    // On utilise double parce que les coordonnées GPS necessitent une grande précision.
    private double latitude;
    private double longitude;

    public POI(String titre, String description, int note, double latitude, double longitude,String idvoyage) {
        this.titre = titre;
        this.description = description;
        this.note = note;
        this.latitude = latitude;
        this.longitude = longitude;
        this.idvoyage = idvoyage;
    }

    // Les getters permettent d'accéder à des données déjà privée de manière contrôlée.

    public String getTitre() {
        return titre;
    }

    public String getidvoyagepoi() {
        return idvoyage;
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

}
