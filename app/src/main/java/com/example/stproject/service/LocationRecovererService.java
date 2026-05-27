package com.example.stproject.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.stproject.R;
import com.example.stproject.data.CFCommunicator;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.example.stproject.data.LocationRepository;
/* import olivier a verifier  */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.example.stproject.utils.ReductionListPoint;
import android.location.Location;
import android.util.Log;

public class LocationRecovererService extends Service {
    /* Définir l'id du channel pour la notification */
    private static final String CHANNEL_ID = "location_service_channel";

    /* Définir l'id de la notification */
    private static final int NOTIFICATION_ID = 1;
    
    private static Location dernierpoint = null;
    
    private FusedLocationProviderClient flpClient;

    private LocationRequest locationReq;

    private CallbackOnLocation callBack;

    private static String tripId;

    /* Instance du communicator de Firestore database */
    private static final CFCommunicator cloudFirestoreCommunicator = new CFCommunicator();

    /* Booléen qui précise si on est déjà en suivi ou non */
    private boolean isTracking = false;

    /* Un champ privé est reservé ici pour accueillir l'instance de classe pour joindre la DB (merci Olivier). */

    private static class CallbackOnLocation extends LocationCallback {
        @Override
        public void onLocationAvailability(LocationAvailability availability){
            if (!availability.isLocationAvailable()) {
                /* Préciser que l'obtention de sa localisation est impossible */
                Log.d("LocationRecovererService", "Location Not Available");
            }
        }

        @Override
        public void onLocationResult(@NonNull LocationResult result){
            /* Je vais utiliser la fonction qu'Olivier va créer pour upload le résultat de la requête (batch de localisation) dans la DB. */
            /*cloudFirestoreCommunicator.ajout_path(result.getLocations());*/
            /*preparation d une liste a traiter  */
            List<Location> pointatraite = new ArrayList<>();
            /*ajout du dernier points pour une continuité entre chaque ajout */
            if ( dernierpoint !=null){
                pointatraite.add(dernierpoint);
            }
            /* on ajoute le nouveau paquet de points  */
            List<Location> touslespoints = result.getLocations();
            // Mathushan:
            // Envoie les nouveaux points GPS dans le Flow.
            // La carte pourra les recevoir en temps réel sans accéder directement au service.
            LocationRepository.INSTANCE.addLocations(touslespoints);
            pointatraite.addAll(touslespoints);
            /* on recupere le nouveau dernier point  */
            dernierpoint=pointatraite.get(pointatraite.size()-1);
            /* on reduit la liste grace a notre algo douglasPeucker */
            List<Location> pointreduit = ReductionListPoint.douglasPeucker(pointatraite);
            
            /* Pour éviter les doublons dans Firestore, on retire le premier point s'il s'agit du point de raccordement */
            if (!pointreduit.isEmpty() && pointatraite.size() > touslespoints.size()) {
                pointreduit.remove(0);
            }

            /* firestore je prend pas d object lourd donc on ajoute les donnees dans une liste d hasmap  */
            List<Map<String, Object>> pointaenvoyer = getMapList(pointreduit);
            cloudFirestoreCommunicator.addPathPoints(tripId, pointaenvoyer);
        }

        @NonNull
        private static List<Map<String, Object>> getMapList(List<Location> pointreduit) {
            List<Map<String, Object>> pointaenvoyer = new ArrayList<>();
            for (int i = 0; i < pointreduit.size(); i++){
                Location localisation= pointreduit.get(i);
                if (localisation !=null){
                    Map<String,Object>point=new HashMap<>();
                    point.put("latitude",localisation.getLatitude());
                    point.put("longitude",localisation.getLongitude());
                    point.put("timestamp", localisation.getTime());
                    pointaenvoyer.add(point);
                }

            }
            return pointaenvoyer;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        /* regarder les permissions de l'app */
        if (
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_LOCATION) == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)  == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
        ) {
            return START_NOT_STICKY; // l'OS peut demander l'interruption du service
        }
        /* l'OS essaie de relancer le service sans Intent */
        if (intent == null){
            return START_STICKY; // réessayer un lancement
        }
        /* Vérifier que le message reçu est bon */
        String action;
        if (Objects.equals(intent.getAction(), "ACTION_START") || Objects.equals(intent.getAction(), "ACTION_PAUSE")){
            action = intent.getAction();
        } else {
            return START_STICKY; // réessayer un lancement
        }

        // Recover TripId safely and handle trip transitions
        String receivedTripId = intent.getStringExtra("tripId");
        if (receivedTripId != null) {
            // If we switch to a DIFFERENT trip, we must reset the continuity point
            if (tripId != null && !tripId.equals(receivedTripId)) {
                dernierpoint = null;
            }
            tripId = receivedTripId;
        }

        switch (action){
            case "ACTION_START" :
                if (!isTracking){
                    try {
                        /* créer un foreground service avec une notification */
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            startForeground(NOTIFICATION_ID, buildNotification("Searching for position..."), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
                        } else {
                            startForeground(NOTIFICATION_ID, buildNotification("Searching for position..."));
                        }
                        flpClient.requestLocationUpdates(locationReq, callBack, Looper.getMainLooper());
                        isTracking = true;
                    } catch (SecurityException e){
                        stopSelf();
                        return START_NOT_STICKY; // L'OS peut demander l'interruption du service
                    }
                }
                break;
            case "ACTION_PAUSE" :
                this.pauseTracking();
                break;
        }

        return START_STICKY; /* Tout est bon, on peut dire à l'OS de garder le service en vie */
    }

    @Override
    public IBinder onBind(Intent intent){ /* Cette méthode ne sera jamais utilisée */
        return null;
    }

    @Override
    public void onCreate(){
        /* Créer le service */
        super.onCreate();

        /* Initialiser le Client FusedLocationProvider */
        flpClient = LocationServices.getFusedLocationProviderClient(this);

        /* Initialiser le Constructeur de Requêtes : ici, l'intervalle entre les requêtes est de 10sec*/
        LocationRequest.Builder locationReqBuilder = new LocationRequest.Builder(10000)
                .setMaxUpdateDelayMillis(300000) // La récupération des localisations se fera toutes les 5min.
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
            updateNotification();
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

    private void updateNotification(){
        Notification notification = buildNotification("Tracking Paused");
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, notification);
    }
}
