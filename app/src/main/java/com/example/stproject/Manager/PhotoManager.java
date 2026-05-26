package com.example.stproject.Manager;

import android.content.Context;
import android.location.Location;
import android.net.Uri;

import com.example.stproject.models.Photo;
import com.example.stproject.models.PhotoAnalysisResult;
import com.example.stproject.models.PhotoAnalysisStatus;
import com.example.stproject.data.PhotoAnalyzer;
import com.example.stproject.data.CFCommunicator;

import java.util.ArrayList;
import java.util.List;

/**
 * Cette classe gère les photos sélectionnées par l'utilisateur.
 * Elle analyse chaque photo et sépare les résultats selon leur statut.
 */
public class PhotoManager {

    private final CFCommunicator cfCommunicator;

    private final PhotoAnalyzer photoAnalyzer;


    public PhotoManager(Context context) {
        this.photoAnalyzer = new PhotoAnalyzer(context);
        this.cfCommunicator = new CFCommunicator();
    }

    public PhotoSelectionResult analyzeSelectedPhotos(List<Uri> imageUris,
                                                      List<Location> path) {
        List<PhotoAnalysisResult> acceptedPhotos = new ArrayList<>();
        List<PhotoAnalysisResult> outsideTripPhotos = new ArrayList<>();
        List<PhotoAnalysisResult> manualValidationPhotos = new ArrayList<>();

        for (Uri uri : imageUris) {
            PhotoAnalysisResult result =
                    photoAnalyzer.analyzePhoto(uri, path);

            if (result.getStatus() == PhotoAnalysisStatus.ACCEPTED) {
                acceptedPhotos.add(result);
            } else if (result.getStatus() == PhotoAnalysisStatus.OUTSIDE_TRIP) {
                outsideTripPhotos.add(result);
            } else if (result.getStatus() == PhotoAnalysisStatus.MANUAL_VALIDATION_REQUIRED) {
                manualValidationPhotos.add(result);
            }
        }

        return new PhotoSelectionResult(
                acceptedPhotos,
                outsideTripPhotos,
                manualValidationPhotos
        );
    }

    /**
     * Résultat global de l'analyse de plusieurs photos.
     */
    public static class PhotoSelectionResult {

        private final List<PhotoAnalysisResult> acceptedPhotos;
        private final List<PhotoAnalysisResult> outsideTripPhotos;
        private final List<PhotoAnalysisResult> manualValidationPhotos;

        public PhotoSelectionResult(List<PhotoAnalysisResult> acceptedPhotos,
                                    List<PhotoAnalysisResult> outsideTripPhotos,
                                    List<PhotoAnalysisResult> manualValidationPhotos) {
            this.acceptedPhotos = acceptedPhotos;
            this.outsideTripPhotos = outsideTripPhotos;
            this.manualValidationPhotos = manualValidationPhotos;
        }

        public List<PhotoAnalysisResult> getAcceptedPhotos() {
            return acceptedPhotos;
        }

        public List<PhotoAnalysisResult> getOutsideTripPhotos() {
            return outsideTripPhotos;
        }

        public List<PhotoAnalysisResult> getManualValidationPhotos() {
            return manualValidationPhotos;
        }
    }
    private String currentTripId;

    public void setCurrentTripId(String tripId) {
        this.currentTripId = tripId;
    }

    public interface PhotoCallback {
        void onSuccess(Photo photo);
        void onError(String error);
    }

    public void savePhotoToTrip(Photo photo, PhotoCallback callback) {

        if (currentTripId == null || currentTripId.trim().isEmpty()) {
            callback.onError("No trip selected");
            return;
        }

        if (photo == null) {
            callback.onError("Photo is invalid");
            return;
        }

        if (photo.getImageURI() == null) {
            callback.onError("Photo URI is missing");
            return;
        }

        photo.setIdVoyage(currentTripId);

        cfCommunicator.savePhotoToTrip(
                photo,
                currentTripId,
                new CFCommunicator.PhotoCallback() {

                    @Override
                    public void onSuccess(Photo savedPhoto) {
                        callback.onSuccess(savedPhoto);
                    }

                    @Override
                    public void onFailure(String error) {
                        callback.onError(error);
                    }
                }
        );
    }
}