package com.example.stproject.data;

import com.example.stproject.models.POI;
import java.util.ArrayList;
import java.util.List;

public class gestionnairePOI {
    // private dis seulement qu'on veut initialisé mais il n'est pas encore crée
    private List<POI> poiList;

    public gestionnairePOI() {
        // c'est ici qu'on crée une liste
        poiList = new ArrayList<>();
    }

    public void ajouterPOI(POI poi) {
        poiList.add(poi);
    }

    public List<POI> gettouslesPOI() {
        return poiList;
    }
}