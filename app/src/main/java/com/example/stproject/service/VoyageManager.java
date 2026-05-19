package com.example.stproject.service;

import com.example.stproject.models.Voyage;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class VoyageManager {

    private final DatabaseReference databaseRef;

    private String currentVoyageId;
    private Voyage currentVoyage;

    public VoyageManager() {
        databaseRef = FirebaseDatabase.getInstance().getReference();
    }

    public void creerNouveauVoyage(String titre) {

        String id = databaseRef.child("voyages").push().getKey();

        if (id == null) {
            return;
        }

        currentVoyageId = id;

        currentVoyage = new Voyage();
        currentVoyage.setId(id);
        currentVoyage.setTitre(titre);
        currentVoyage.setDescription("");
        currentVoyage.setNote(null);
        currentVoyage.setDateDebut(String.valueOf(System.currentTimeMillis()));
        currentVoyage.setDateFin(null);
        currentVoyage.setListePois(new ArrayList<>());

        databaseRef.child("voyages")
                .child(id)
                .setValue(currentVoyage);
    }

    public String getCurrentVoyageId() {
        return currentVoyageId;
    }

    public Voyage getCurrentVoyage() {
        return currentVoyage;
    }
}