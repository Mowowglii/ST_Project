package com.example.stproject.Manager;

import android.content.Context;
import android.location.Location;
import android.net.Uri;

import com.example.stproject.data.CFCommunicator;
import com.example.stproject.data.PhotoAnalyzer;
import com.example.stproject.models.Photo;
import com.example.stproject.models.PhotoAnalysisResult;
import com.example.stproject.models.PhotoAnalysisStatus;
import com.example.stproject.models.Voyage;

import java.util.ArrayList;
import java.util.List;

public class PhotoManager {

    private final CFCommunicator cfCommunicator;
    private final PhotoAnalyzer photoAnalyzer;

    private Voyage currentTrip;

    public PhotoManager(Context context) {
        this.photoAnalyzer = new PhotoAnalyzer(context);
        this.cfCommunicator = new CFCommunicator();
    }

    public void setCurrentTrip(Voyage trip) {
        this.currentTrip = trip;
    }

    public PhotoSelectionResult analyzeSelectedPhotos(List<Uri> imageUris, List<Location> path) {
        List<PhotoAnalysisResult> acceptedPhotos = new ArrayList<>();
        List<PhotoAnalysisResult> outsideTripPhotos = new ArrayList<>();
        List<PhotoAnalysisResult> manualValidationPhotos = new ArrayList<>();

        for (Uri uri : imageUris) {
            PhotoAnalysisResult result = photoAnalyzer.analyzePhoto(uri, path);

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

    public interface PhotoSaveCallback {
        void onSuccess(String photoPath);
        void onError(String error);
    }

    public interface PhotoListCallback {
        void onSuccess(List<String> photoPaths);
        void onError(String error);
    }

    public void savePhotoToTrip(String pathInStorage, PhotoSaveCallback callback) {

        if (currentTrip == null || currentTrip.getId() == null || currentTrip.getId().trim().isEmpty()) {
            callback.onError("No trip selected");
            return;
        }

        if (pathInStorage == null || pathInStorage.trim().isEmpty()) {
            callback.onError("Photo path is missing");
            return;
        }

        String photoId = cfCommunicator.getPictureKey(currentTrip);

        cfCommunicator.sendPictureToDb(
                pathInStorage,
                currentTrip.getId(),
                photoId
        );

        callback.onSuccess(pathInStorage);
    }

    public void getPhotosForCurrentTrip(PhotoListCallback callback) {

        if (currentTrip == null || currentTrip.getId() == null || currentTrip.getId().trim().isEmpty()) {
            callback.onError("No trip selected");
            return;
        }

        cfCommunicator.getPhotosForTrip(
                currentTrip,
                new CFCommunicator.PhotoCallback() {

                    @Override
                    public void onSuccess(List<String> photoPaths) {
                        callback.onSuccess(photoPaths);
                    }

                    @Override
                    public void onFailure(String error) {
                        callback.onError(error);
                    }
                }
        );
    }

    public static class PhotoSelectionResult {

        private final List<PhotoAnalysisResult> acceptedPhotos;
        private final List<PhotoAnalysisResult> outsideTripPhotos;
        private final List<PhotoAnalysisResult> manualValidationPhotos;

        public PhotoSelectionResult(
                List<PhotoAnalysisResult> acceptedPhotos,
                List<PhotoAnalysisResult> outsideTripPhotos,
                List<PhotoAnalysisResult> manualValidationPhotos
        ) {
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
}