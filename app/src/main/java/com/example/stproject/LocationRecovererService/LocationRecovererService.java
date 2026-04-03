package com.example.stproject.LocationRecovererService;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationRecovererService extends Service {
    private FusedLocationProviderClient flpClient;
    private LocationRequest locationReq;

    private static class callbackOnLocation extends LocationCallback {
        @Override
        public void onLocationAvailability(LocationAvailability availability){

        }

        @Override
        public void onLocationResult(LocationResult result){
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

    }

    @Override
    public IBinder onBind(Intent intent){

    }

    @Override
    public void onCreate(){
        /* Initialiser le Client FusedLocationProvider */
        flpClient = LocationServices.getFusedLocationProviderClient(this);

        /* Initialiser le Constructeur de Requêtes : ici, l'intervalle entre les requêtes est de 10sec*/
        LocationRequest.Builder locationReqBuilder = new LocationRequest.Builder(10000)
                .setMaxUpdateDelayMillis(300000) // La récupération des localisations se fera toutes les 5min
                .setMinUpdateDistanceMeters(1) // Une nouvelle localisation située à moins de 1m de distance de la localisation précédente ne sera pas gardée
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY); // La priorité de la requête est équilibrée entre précision du suivi et économie d'énergie
        /* Initialiser la requête de localisation */
        locationReq = locationReqBuilder.build();
    }

    @Override
    public void onDestroy(){

    }
}
