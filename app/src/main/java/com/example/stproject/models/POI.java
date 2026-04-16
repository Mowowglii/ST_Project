package com.example.stproject.models;

import java.util.UUID;
public class POI {

    private String titre;
    private String description;
    private int note;
    // On utilise double parce que les coordonnées GPS necessitent une grande précision.
    private double latitude;
    private double longitude;
    private UUID id;


    public POI(String titre, String description, int note, double latitude, double longitude) {
        this.titre = titre;
        this.description = description;
        this.note = note;
        this.latitude = latitude;
        this.longitude = longitude;
        this.id = id;

    }

    // Les getters permettent d'accéder à des données déjà privée de manière contrôlée.

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

    public UUID getId() {
        return id;
    }

}
