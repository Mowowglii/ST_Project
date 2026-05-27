package com.example.stproject.utils;

import android.location.Location;
import java.util.ArrayList;
import java.util.List;
//pas sur pour les modules 

public class ReductionListPoint{

    public static List<Location> douglasPeucker(List<Location> points) {
        if (points == null || points.size() < 3) {
            return points;
        }
        Location premier = points.get(0);
        Location dernier = points.get(points.size() - 1);

        double x1 = premier.getLongitude();
        double y1 = premier.getLatitude();
        double x2 = dernier.getLongitude();
        double y2 = dernier.getLatitude();

        double base= Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        if (base ==0){
            base=0.00001;
        }
        double maxaire = -1;
        int indicedupivot=-1;

        for (int i = 1; i < points.size() - 1; i++) {
            Location pointactuel = points.get(i);
            double px = pointactuel.getLongitude();
            double py = pointactuel.getLatitude();
            double hauteur = Math.abs((x2 - x1) * (y1 - py) - (x1 - px) * (y2 - y1)) / base;
            double aireactuel=(base * hauteur) / 2.0;

            if (aireactuel>maxaire) {
                maxaire = aireactuel;
                indicedupivot = i;
            }
        }
        List<Location> listereduit = new ArrayList<>();
        if (maxaire>0.00005 && indicedupivot != -1){
            List<Location> debutAuPivot = points.subList(0, indicedupivot + 1);
            List<Location> pivotAlaFin = points.subList(indicedupivot, points.size());
            List<Location> resultatGauche = douglasPeucker(debutAuPivot);
            List<Location> resultatDroite = douglasPeucker(pivotAlaFin);
            listereduit.addAll(resultatGauche);
            listereduit.addAll(resultatDroite.subList(1, resultatDroite.size()));
           
        }
        else{
            listereduit.add(premier);
            listereduit.add(dernier); 
        }
        
        return listereduit;
    }
}
