package com.example.stproject.data;

import android.content.Context;
import android.net.Uri;

import androidx.exifinterface.media.ExifInterface;

import com.example.stproject.models.Photo;
import com.example.stproject.models.PhotoAnalysisResult;
import com.example.stproject.models.PhotoAnalysisStatus;

import java.io.InputStream;
import android.location.Location;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import android.database.Cursor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Cette classe sert à analyser une photo sélectionnée
 * avant son envoi vers Firebase Storage.
 *
 * Elle vérifie notamment si la photo contient des
 * coordonnées GPS dans ses données EXIF.
 */
public class PhotoAnalyzer {

    // Contexte Android nécessaire pour accéder aux fichiers
    private final Context context;

    /**
     * Constructeur de la classe
     */
    public PhotoAnalyzer(Context context) {
        this.context = context;
    }

    /**
     * Analyse une photo sélectionnée par l'utilisateur.
     */
    public PhotoAnalysisResult analyzePhoto(Uri imageUri, List<Location> path) {

        Photo photo = new Photo();
        photo.setImageURI(imageUri);

        final double MAX_DISTANCE_METERS = 100.0;
        final long MAX_TIME_DIFFERENCE_MS = 10 * 60 * 1000; // 10 minutes

        try {
            ParcelFileDescriptor fileDescriptor =
                    context.getContentResolver().openFileDescriptor(imageUri, "r");

            if (fileDescriptor == null) {
                return new PhotoAnalysisResult(
                        photo,
                        PhotoAnalysisStatus.MANUAL_VALIDATION_REQUIRED,
                        -1,
                        "Impossible de lire la photo."
                );
            }

            ExifInterface exifInterface =
                    new ExifInterface(fileDescriptor.getFileDescriptor());

            float[] latLong = new float[2];
            boolean hasGps = exifInterface.getLatLong(latLong);

            if (hasGps) {
                photo.setLatitude(latLong[0]);
                photo.setLongitude(latLong[1]);

                fileDescriptor.close();

                double distanceToTrip = calculateMinDistanceToPath(
                        photo.getLatitude(),
                        photo.getLongitude(),
                        path
                );

                if (distanceToTrip <= MAX_DISTANCE_METERS) {
                    return new PhotoAnalysisResult(
                            photo,
                            PhotoAnalysisStatus.ACCEPTED,
                            distanceToTrip,
                            "La photo appartient au voyage."
                    );
                }

                return new PhotoAnalysisResult(
                        photo,
                        PhotoAnalysisStatus.OUTSIDE_TRIP,
                        distanceToTrip,
                        "La photo est trop éloignée du trajet."
                );
            }

            String dateString =
                    exifInterface.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL);

            fileDescriptor.close();

            long photoTime = parseExifDateToTimestamp(dateString);

            if (photoTime == -1 || path == null || path.isEmpty()) {
                return new PhotoAnalysisResult(
                        photo,
                        PhotoAnalysisStatus.MANUAL_VALIDATION_REQUIRED,
                        -1,
                        "Aucune donnée GPS ou date exploitable."
                );
            }

            Location closestLocation =
                    findClosestLocationByTime(photoTime, path, MAX_TIME_DIFFERENCE_MS);

            if (closestLocation == null) {
                return new PhotoAnalysisResult(
                        photo,
                        PhotoAnalysisStatus.MANUAL_VALIDATION_REQUIRED,
                        -1,
                        "Aucun point du trajet ne correspond à l'heure de la photo."
                );
            }

            photo.setTimeStamp(photoTime);
            photo.setLatitude(closestLocation.getLatitude());
            photo.setLongitude(closestLocation.getLongitude());

            return new PhotoAnalysisResult(
                    photo,
                    PhotoAnalysisStatus.ACCEPTED,
                    0,
                    "La photo a été associée au trajet grâce à son heure de prise."
            );

        } catch (Exception e) {
            return new PhotoAnalysisResult(
                    photo,
                    PhotoAnalysisStatus.MANUAL_VALIDATION_REQUIRED,
                    -1,
                    "Erreur pendant l'analyse de la photo."
            );
        }
    }

    /**
     * Calcule la distance en mètres entre deux positions GPS.
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];

        Location.distanceBetween(
                lat1,
                lon1,
                lat2,
                lon2,
                results
        );

        return results[0];
    }

    private double calculateMinDistanceToPath(
            double photoLatitude,
            double photoLongitude,
            List<Location> path
    ) {
        if (path == null || path.isEmpty()) {
            return -1;
        }

        double minDistance = Double.MAX_VALUE;

        for (Location pathPoint : path) {
            double distance = calculateDistance(
                    photoLatitude,
                    photoLongitude,
                    pathPoint.getLatitude(),
                    pathPoint.getLongitude()
            );

            if (distance < minDistance) {
                minDistance = distance;
            }
        }

        return minDistance;
    }
    /**
     * Calcule la distance minimale entre la photo et les points du tracé du voyage.
     */
    private long parseExifDateToTimestamp(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return -1;
        }

        try {
            SimpleDateFormat format =
                    new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault());

            Date date = format.parse(dateString);

            if (date == null) {
                return -1;
            }

            return date.getTime();

        } catch (Exception e) {
            return -1;
        }
    }

    private Location findClosestLocationByTime(
            long photoTime,
            List<Location> path,
            long maxDifferenceMs
    ) {
        Location closestLocation = null;
        long smallestDifference = Long.MAX_VALUE;

        for (Location location : path) {

            long difference =
                    Math.abs(location.getTime() - photoTime);

            if (difference < smallestDifference) {
                smallestDifference = difference;
                closestLocation = location;
            }
        }

        Toast.makeText(
                context,
                "Différence = " + smallestDifference,
                Toast.LENGTH_LONG
        ).show();

        return closestLocation;
    }
}