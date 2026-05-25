package com.example.stproject.Manager;

import com.example.stproject.data.CloudFirestoreCommunicator;
import com.example.stproject.models.Voyage;

import java.util.ArrayList;
import java.util.List;

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
    // On crée des CallBack pour attendre que les requêtes au niveau de la base de donnée soit fini
    // Sinon il lancera la carte alors que les requêtes au niveau de la base de donnée n'est pas fini.
    public interface CreationVoyageCallback {
        void onSuccess(String voyageId, Voyage voyage);
        void onError(String message);
    }
    public void creerNouveauVoyage(String titre, CreationVoyageCallback callback) {
        if (titre == null ||titre.trim().isEmpty()) {
            callback.onError("Veuillez entrer un nom de voyage");
            return;
        }
        // Nettoyage des espaces au début et à la fin
        String titreNettoye = titre.trim();

        // vérifie si un voyage avec ce nom existe déjà
        cfCommunicator.verifierNomVoyageExiste(
                titreNettoye,
                // Si le voyage existe ou Si le voyage n'existe pas
                new CloudFirestoreCommunicator.NomVoyageCallback() {

                    @Override
                    public void onResult(boolean existe) {

                        // Le voyage existe déjà
                        if (existe) {
                            callback.onError("Ce voyage existe déjà");
                            return;
                        }

                        // Génération de l'identifiant unique
                        String id = cfCommunicator.cleUnique();

                        // Création de l'objet voyage
                        currentVoyage = new Voyage(
                                id,
                                titre,
                                "",
                                0,
                                new ArrayList<>(),
                                new ArrayList<>(),
                                new ArrayList<>()
                        );

                        // Définit le voyage courant
                        currentVoyageId = id;
                        cfCommunicator.demarrerNouveauVoyage(currentVoyageId);

                        // Sauvegarde dans Firestore
                        cfCommunicator.ajout_d_un_voyage(currentVoyage);
                        // retour succès
                        callback.onSuccess(currentVoyageId, currentVoyage);
                    }

                    @Override
                    public void onError(String erreur) {
                        callback.onError(erreur);
                    }
                }
        );
    }

    // Retourne l'id du voyage courant
    public String getCurrentVoyageId() {
        return currentVoyageId;
    }

    // Retourne le voyage courant
    public Voyage getCurrentVoyage() {
        return currentVoyage;
    }

    // Chargement des voyages:

    public interface ListeVoyagesCallback {
        void onSuccess(List<Voyage> voyages);
        void onError(String message);
    }

    public void recupererTousLesVoyages(ListeVoyagesCallback callback) {
        cfCommunicator.tous_les_voyages(new CloudFirestoreCommunicator.VoyageCallback() {
            @Override
            public void onComplete(List<Voyage> voyages) {
                callback.onSuccess(voyages);
            }

            @Override
            public void onError (String error) {
                callback.onError((error));
            }
        });
    }
}