package com.example.stproject.service;

import com.example.stproject.data.CloudFirestoreCommunicator;
import com.example.stproject.models.Voyage;

import java.util.ArrayList;

public class VoyageManager {

    private final CloudFirestoreCommunicator cfCommunicator;
    private String currentVoyageId;
    private Voyage currentVoyage;

    public VoyageManager() {
        //Constructeur du VoyageManager
        //Initialise le communicateur Firestore afin que le manager puisse
        // effectuer des opérations sur la base de donnée.
        this.cfCommunicator = new CloudFirestoreCommunicator();
    }

    public void creerNouveauVoyage(String titre) {

        String id = cfCommunicator.cleUnique();

        currentVoyage = new Voyage(
                id,
                titre,
                "",
                0,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );

        currentVoyageId = id;

        cfCommunicator.ajout_d_un_voyage(currentVoyage);
    }

    public String getCurrentVoyageId() {
        return currentVoyageId;
    }

    public Voyage getCurrentVoyage() {
        return currentVoyage;
    }
}
