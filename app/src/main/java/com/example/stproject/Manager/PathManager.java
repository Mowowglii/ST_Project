package com.example.stproject.Manager;

import com.example.stproject.data.CFCommunicator;

import java.util.List;
import java.util.Map;

public class PathManager {

    private final CFCommunicator cfCommunicator;
    private String currentTripId;

    public PathManager() {
        this.cfCommunicator = new CFCommunicator();
    }

    public void setCurrentTripId(String tripId) {
        this.currentTripId = tripId;
    }

    public interface PathCallback {
        void onSuccess(List<Map<String, Object>> points);
        void onError(String error);
    }

    public void getPathForCurrentTrip(PathCallback callback) {

        if (currentTripId == null || currentTripId.trim().isEmpty()) {
            callback.onError("No trip selected");
            return;
        }

        cfCommunicator.getPathPoints(
                currentTripId,
                new CFCommunicator.PathCallback() {

                    @Override
                    public void onSuccess(List<Map<String, Object>> points) {
                        callback.onSuccess(points);
                    }

                    @Override
                    public void onFailure(String error) {
                        callback.onError(error);
                    }
                }
        );
    }
}