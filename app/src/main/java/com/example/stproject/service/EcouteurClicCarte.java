/* On utilise un interface pour éviter que le DetecteurCliclCarte dépende de la class Carte.
Comme je veux récupérer les coordonnées GPS d’un clic, je dois éviter de le lier à une seule classe,
car je pourrais avoir besoin de l’utiliser dans différentes classes.
L’interface permet à toute classe qui implémente EcouteurClicCarte de recevoir les coordonnées.
Cela rend le code plus flexible et réutilisable.
*/
package com.example.stproject.service;

public interface EcouteurClicCarte {
    void recevoirClic(double latitude, double longitude);
}
