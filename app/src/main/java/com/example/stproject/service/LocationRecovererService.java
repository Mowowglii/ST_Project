package com.example.stproject.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;

import androidx.core.app.NotificationCompat;

import com.example.stproject.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationRecovererService extends Service {
    /* Définir l'id du channel pour la notification */
    private static final String CHANNEL_ID = "location_service_channel";

    /* Définir l'id de la notification */
    private static final int NOTIFICATION_ID = 1;

    private FusedLocationProviderClient flpClient;

    private LocationRequest locationReq;

    private CallbackOnLocation callBack;

    /* Booléen qui précise si on est déjà en suivi ou non */
    private boolean isTracking = false;

    /* Un champ privé est reservé ici pour accueillir l'instance de classe pour joindre la DB (merci Olivier) */

    private static class CallbackOnLocation extends LocationCallback {
        @Override
        public void onLocationAvailability(LocationAvailability availability){
            if (!availability.isLocationAvailable()) {
                /* Préciser que l'obtention de sa localisation est impossible */
            }
        }

        @Override
        public void onLocationResult(LocationResult result){
            /* Je vais utiliser la fonction qu'Olivier va créer pour upload le résultat de la requête (batch de localisation) dans la DB */
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        /* Il faut également regarder les permissions de l'app */

        switch (intent.getAction()){
            case "ACTION_START" :
                if (!isTracking){
                    try {
                        /* créer un foreground service avec une notification */
                        startForeground(NOTIFICATION_ID, buildNotification("Searching for position..."));
                        flpClient.requestLocationUpdates(locationReq, callBack, Looper.getMainLooper());
                        isTracking = true;
                    } catch (SecurityException e){
                        stopSelf();
                        return START_NOT_STICKY;
                    }
                }
                break;
            case "ACTION_PAUSE" :
                this.pauseTracking();
                break;
            default :
                return START_NOT_STICKY;
        }

        return START_STICKY; /* Tout est bon, on peut dire à l'OS de garder le service en vie */
    }

    @Override
    public IBinder onBind(Intent intent){
        /* Si l'utilisateur souhaite effectuer une pause dans son voyage (via un IBinder entre l'UI et le service) */
    }

    @Override
    public void onCreate(){
        /* Créer le service */
        super.onCreate();

        /* Initialiser le Client FusedLocationProvider */
        flpClient = LocationServices.getFusedLocationProviderClient(this);

        /* Initialiser le Constructeur de Requêtes : ici, l'intervalle entre les requêtes est de 10sec*/
        LocationRequest.Builder locationReqBuilder = new LocationRequest.Builder(10000)
                .setMaxUpdateDelayMillis(300000) // La récupération des localisations se fera toutes les 5min
                .setMinUpdateDistanceMeters(1) // Une nouvelle localisation située à moins de 1m de distance de la localisation précédente ne sera pas gardée
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY); // La priorité de la requête est équilibrée entre précision du suivi et économie d'énergie

        /* Initialiser la requête de localisation */
        locationReq = locationReqBuilder.build();

        /* Initialiser le callback pour les résultats de requêtes */
        callBack = new CallbackOnLocation();

        /* Créer la notification */
        createNotificationChannel();
    }

    @Override
    public void onDestroy(){
        // Dire à l'OS que ce service n'est plus en foreground
        stopForeground(STOP_FOREGROUND_REMOVE);

        // Mettre fin au suivi de localisation
        flpClient.removeLocationUpdates(callBack);

        // Finaliser la destruction du service
        super.onDestroy();
    }

    private void pauseTracking(){
        if (isTracking){
            flpClient.removeLocationUpdates(callBack);
            isTracking = false;
            /* Préciser dans la notification que le suivi est en pause */
            updateNotification("Tracking Paused");
        }
    }

    private void createNotificationChannel(){
        /* La version de l'OS doit être Android 8.0 (API 26) ou au-dessus */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "ST Tracking Service",
                    NotificationManager.IMPORTANCE_LOW
            );

            /* Inscrire le channel via le système */
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null){
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private Notification buildNotification(String text){
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("ST Tracking Service")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Logo/Image de la notification, on pourra la modifier plus tard
                .build();
    }

    private void updateNotification(String newText){
        Notification notification = buildNotification(newText);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, notification);
    }
}
