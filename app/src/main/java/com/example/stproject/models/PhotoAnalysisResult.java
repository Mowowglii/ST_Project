package com.example.stproject.models;
import com.example.stproject.models.PhotoAnalysisStatus;
/**
 * Cette classe contient les informations temporaires issues de l'analyse d'une photo.
 */
public class PhotoAnalysisResult {

    private Photo photo;
    private PhotoAnalysisStatus status;
    private double distanceToTrip;
    private String message;

    public PhotoAnalysisResult() {}

    public PhotoAnalysisResult(Photo photo,
                               PhotoAnalysisStatus status,
                               double distanceToTrip,
                               String message) {
        this.photo = photo;
        this.status = status;
        this.distanceToTrip = distanceToTrip;
        this.message = message;
    }

    public Photo getPhoto() {
        return photo;
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }

    public PhotoAnalysisStatus getStatus() {
        return status;
    }

    public void setStatus(PhotoAnalysisStatus status) {
        this.status = status;
    }

    public double getDistanceToTrip() {
        return distanceToTrip;
    }

    public void setDistanceToTrip(double distanceToTrip) {
        this.distanceToTrip = distanceToTrip;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
