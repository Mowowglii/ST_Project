package com.example.stproject.service;

import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;


public class DetecteurClicCarte implements MapEventsReceiver{
    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        double latitude = p.getLatitude();
        double longitude = p.getLongitude();

        System.out.println("Latitude:" + latitude + "Longitude" + longitude);

        return true;
    }
    @Override
    public boolean longPressHelper(GeoPoint P){
        return false;
    }
}
