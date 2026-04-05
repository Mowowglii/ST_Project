package com.example.stproject.DBCommunicators;
//manque apparament le databasereference et firebasedatabase 

import android.location.Location;

import java.util.List;

public class CloudFirestoreCommunicator {
    private static firebaseService instance;
    private databasereference database;
    
    public CloudFirestoreCommunicator(){
        database = FirebaseDatabase.getInstance().getReference();
    } // Si j'ai bien compris, c'est le constructeur de la classe

    public void ajout_d_un_voyage(){
        //creation d un voyage
    }
    public void recuperer_un_voyage(){
        //recuperer les donnees d un voyage 

    }
    public void tous_les_voyages(){
        //pour recuperer les donnees de tous les voyages
    }
    public void ajout_poi(){
        //ajout d un points 

    }
    public void  supp_poi(){
        //supp d un points

    }
    public void supp_voyage(){
        //supp du voyage avec l ensemble des poi

    }
    public void recuperer_un_poi(){
        //recuperer les info d un poi

    }
    public void modification_dun_voyage(){

    }
    public void modification_dun_poi(){

    }

    /* Envoie de localisations à la DB */
    public void send_loc(List<Location> data){

    }
    
}
