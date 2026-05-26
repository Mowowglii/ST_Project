package com.example.stproject.Manager;

import com.example.stproject.data.CFCommunicator;
import com.example.stproject.models.POI;
import com.example.stproject.models.Voyage;

import java.util.List;

public class POIManager {

    private final CFCommunicator cfCommunicator;
    private Voyage currentTrip;

    public POIManager() {
        this.cfCommunicator = new CFCommunicator();
    }

    public void setCurrentTrip(Voyage trip) {
        this.currentTrip = trip;
    }

    public interface POICallback {
        void onSuccess(POI poi);
        void onError(String error);
    }

    public interface POIsCallback {
        void onSuccess(List<POI> pois);
        void onError(String error);
    }

    public void addPOI(POI poi, POICallback callback) {

        if (currentTrip == null) {
            callback.onError("No trip selected");
            return;
        }

        if (poi == null) {
            callback.onError("POI is invalid");
            return;
        }

        if (poi.getTitre() == null || poi.getTitre().trim().isEmpty()) {
            callback.onError("POI title is required");
            return;
        }

        cfCommunicator.addPOIToTrip(poi, currentTrip);

        callback.onSuccess(poi);
    }

    public void updatePOI(POI poi, POICallback callback) {

        if (currentTrip == null) {
            callback.onError("No trip selected");
            return;
        }

        if (poi == null) {
            callback.onError("POI is invalid");
            return;
        }

        if (poi.getTitre() == null || poi.getTitre().trim().isEmpty()) {
            callback.onError("POI title is required");
            return;
        }

        cfCommunicator.modifyPOIatTrip(poi, currentTrip);

        callback.onSuccess(poi);
    }

    public void deletePOI(POI poi, POICallback callback) {

        if (currentTrip == null) {
            callback.onError("No trip selected");
            return;
        }

        if (poi == null) {
            callback.onError("POI is invalid");
            return;
        }

        cfCommunicator.delPOIfromTrip(poi, currentTrip);

        callback.onSuccess(poi);
    }

    public void getPOIsForCurrentTrip(POIsCallback callback) {

        if (currentTrip == null) {
            callback.onError("No trip selected");
            return;
        }

        cfCommunicator.getPOIsForTrip(
                currentTrip,
                new CFCommunicator.POIsCallback() {

                    @Override
                    public void onSuccess(List<POI> pois) {
                        callback.onSuccess(pois);
                    }

                    @Override
                    public void onFailure(String error) {
                        callback.onError(error);
                    }
                }
        );
    }
}