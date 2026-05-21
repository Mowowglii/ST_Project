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

        // Génération d'un ID unique Firebase
        String id = databaseRef.child("voyages").push().getKey();

        if (id == null) {
            return;
        }

        currentVoyageId = id;

        // Création de l'objet Voyage
        currentVoyage = new Voyage(
                id,
                titre,
                "",
                null,
                string,
                null,
                new ArrayList<>()
        );

        // Envoi dans Firebase
        // une fonction que j'appelle à Olivier.
//        databaseRef.child("voyages")
//                .child(id)
//                .setValue(currentVoyage);
    }

    public String getCurrentVoyageId() {
        return currentVoyageId;
    }

    public Voyage getCurrentVoyage() {
        return currentVoyage;
    }
}