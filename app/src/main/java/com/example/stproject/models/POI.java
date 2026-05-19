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

    public String getDescription() {
        return description;
    }

    public String type() {
        return type;
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
    public String getIdVoyage() { 
	return idVoyage; 
	}
    public void setIdVoyage(String idVoyage) { 
	this.idVoyage = idVoyage; 
	}



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

