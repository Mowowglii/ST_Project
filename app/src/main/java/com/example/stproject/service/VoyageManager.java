package com.example.stproject.service;

import com.example.stproject.models.Voyage;
import com.example.stproject.data.CloudFirestoreCommunicator;
import java.util.ArrayList;

public class VoyageManager {

    private final CloudFirestoreCommunicator cfCommunicator = new CloudFirestoreCommunicator();
    private String currentVoyageId;
    private Voyage currentVoyage;
    public void creerNouveauVoyage(String titre) {
        // Création de l'objet Voyage
        currentVoyage = new Voyage(
                "",
                titre,
                "",
                0,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );

        cfCommunicator.ajout_d_un_voyage(currentVoyage);
    }

    public String getCurrentVoyageId() {
        return currentVoyageId;
    }

    public Voyage getCurrentVoyage() {
        return currentVoyage;
    }
}