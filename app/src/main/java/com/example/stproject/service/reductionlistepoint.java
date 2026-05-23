package com.example.stproject.utils;

import android.location.Location;
import java.util.ArrayList;
import java.util.List;
//pas sur pour les modules 

public class reductionlistepoint {

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
        if (maxaire>0.000001 && indicedupivot != -1){
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




































public static List<Location> simplifierTrajet(List<Location> points) {
        // Condition d'arrêt de la récursion : s'il y a 2 points ou moins, on ne peut plus diviser
        if (points == null || points.size() <= 2) {
            return points;
        }

        // 1. Récupérer les x et y du premier et du dernier élément de la liste
        Location premier = points.get(0);
        Location dernier = points.get(points.size() - 1);

        double x1 = premier.getLongitude();
        double y1 = premier.getLatitude();
        double x2 = dernier.getLongitude();
        double y2 = dernier.getLatitude();

        // 2. Mettre dans une variable base le résultat de la racine carrée( (x2-x1)**2 + (y2-y1)**2 )
        double base = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));

        // Sécurité pour éviter la division par zéro si le premier et le dernier point se confondent
        if (base == 0) {
            base = 0.000001;
        }

        double maxAire = -1;
        int indexPivot = -1;

        // 3. Boucle sur la liste sans le premier et le dernier élément
        for (int i = 1; i < points.size() - 1; i++) {
            Location pointCourant = points.get(i);
            
            // Récupérer les coordonnées px et py
            double px = pointCourant.getLongitude();
            double py = pointCourant.getLatitude();

            // Calcule la valeur absolue de (x2-x1)(y1-py) - (x1-px)(y2-y1) le tout divisé par base
            double hauteur = Math.abs((x2 - x1) * (y1 - py) - (x1 - px) * (y2 - y1)) / base;

            // Mettre dans une variable aire le résultat de base * hauteur le tout divisé par deux
            double aire = (base * hauteur) / 2.0;

            // 4. Récupérer la plus grande aire calculée pour trouver le point pivot
            if (aire > maxAire) {
                maxAire = aire;
                indexPivot = i;
            }
        }

        // --- SEUIL DE TOLÉRANCE ---
        // Indispensable en récursion géométrique pour éviter de tourner à l'infini sur des micro-écarts.
        // Si la plus grande aire trouvée est inférieure à ce seuil, on considère la ligne comme droite.
        // Tu peux modifier cette valeur selon la précision voulue (ex: 0.000001).
        double seuilToleranceAire = 0.000001;

        List<Location> resultat = new ArrayList<>();

        // 5. Utiliser le pivot comme séparation de la liste si l'écart est significatif
        if (indexPivot != -1 && maxAire > seuilToleranceAire) {
            
            List<Location> debutAuPivot = points.subList(0, indexPivot + 1);
                        List<Location> pivotAlaFin = points.subList(indexPivot, points.size());

            List<Location> resultatGauche = simplifierTrajet(debutAuPivot);
            List<Location> resultatDroite = simplifierTrajet(pivotAlaFin);
            resultat.addAll(resultatGauche);
            // On utilise subList(1, ...) pour ne pas dupliquer le pivot qui est à la fois la fin de gauche et le début de droite
            resultat.addAll(resultatDroite.subList(1, resultatDroite.size()));
            
        } else {
            // Si aucune aire n'est assez grande, on ne garde que le début et la fin initiaux
            resultat.add(premier);
            resultat.add(dernier);
        }

        return resultat;
    }
}