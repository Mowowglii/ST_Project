package com.example.stproject.data;

import android.content.Context;
import android.net.Uri;

import androidx.exifinterface.media.ExifInterface;

import com.example.stproject.models.Photo;
import com.example.stproject.models.PhotoAnalysisResult;
import com.example.stproject.models.PhotoAnalysisStatus;

import java.io.InputStream;
import android.location.Location;
import java.util.List;

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

        try {
            // accès au donnée de l'image
            InputStream inputStream =
                    context.getContentResolver().openInputStream(imageUri);

            if (inputStream == null) {
                return new PhotoAnalysisResult(
                        photo,
                        PhotoAnalysisStatus.MANUAL_VALIDATION_REQUIRED,
                        -1,
                        "Impossible de lire la photo."
                );
            }

            ExifInterface exifInterface = new ExifInterface(inputStream);

            float[] latLong = new float[2];
            boolean hasGps = exifInterface.getLatLong(latLong);

            if (!hasGps) {
                return new PhotoAnalysisResult(
                        photo,
                        PhotoAnalysisStatus.MANUAL_VALIDATION_REQUIRED,
                        -1,
                        "La photo ne contient pas de coordonnées GPS."
                );
            }

            photo.setLatitude(latLong[0]);
            photo.setLongitude(latLong[1]);

            double distanceToTrip = calculateMinDistanceToPath(
                    photo.getLatitude(),
                    photo.getLongitude(),
                    path
            );

            if (distanceToTrip == -1) {
                return new PhotoAnalysisResult(
                        photo,
                        PhotoAnalysisStatus.MANUAL_VALIDATION_REQUIRED,
                        -1,
                        "Impossible de comparer la photo avec le tracé du voyage."
                );
            }

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
                    "La photo est trop éloignée du tracé du voyage."
            );

        } catch (Exception e) {
            return new PhotoAnalysisResult(
                    photo,
                    PhotoAnalysisStatus.MANUAL_VALIDATION_REQUIRED,
                    -1,
                    "Erreur pendant l'analyse EXIF de la photo."
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
    /**
     * Calcule la distance minimale entre la photo et les points du tracé du voyage.
     */
    private double calculateMinDistanceToPath(double photoLatitude,
                                              double photoLongitude,
                                              List<Location> path) {
        if (path == null || path.isEmpty()) {
            // Aucun calcul possible car aucune donnée géographique disponible
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
}