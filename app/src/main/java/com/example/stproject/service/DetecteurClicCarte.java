package com.example.stproject.service;

import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;


public class DetecteurClicCarte implements MapEventsReceiver {
    private EcouteurClicCarte ecouteur;
    public DetecteurClicCarte(EcouteurClicCarte ecouteur) {
        this.ecouteur = ecouteur;
    }
    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        double latitude = p.getLatitude();
        double longitude = p.getLongitude();

        // stocke les coordonnées en appelant la méthode qui est dans interface.
        ecouteur.recevoirClic(latitude, longitude);
        return true;
    }
    @Override
    public boolean longPressHelper(GeoPoint P){
        return false;
    }
}
