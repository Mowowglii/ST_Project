package com.example.stproject.Manager;

import android.content.Context;
import android.location.Location;
import android.net.Uri;

import com.example.stproject.models.PhotoAnalysisResult;
import com.example.stproject.models.PhotoAnalysisStatus;
import com.example.stproject.data.PhotoAnalyzer;

import java.util.ArrayList;
import java.util.List;

/**
 * Cette classe gère les photos sélectionnées par l'utilisateur.
 * Elle analyse chaque photo et sépare les résultats selon leur statut.
 */
public class PhotoManager {

    private final PhotoAnalyzer photoAnalyzer;

    public PhotoManager(Context context) {
        this.photoAnalyzer = new PhotoAnalyzer(context);
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
}