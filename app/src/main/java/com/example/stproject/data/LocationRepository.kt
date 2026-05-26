// Flow sert de liaison temps réel entre le service GPS et la carte.
// Le service envoie les nouvelles positions au Repository.
// Le Repository diffuse automatiquement les données via le StateFlow.
// La carte observe ce Flow et met à jour la Polyline sans accéder directement au service.
package com.example.stproject.data;

import android.location.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object LocationRepository {

    // Distance minimale entre deux points gardés.
    // Si l'utilisateur bouge de moins de 5 mètres,
    // le nouveau point est ignoré pour éviter les doublons.
    private const val DISTANCE_MIN_METRES = 5f

    // Précision GPS maximale acceptée.
    // Un point avec une précision trop mauvaise est ignoré.
    private const val PRECISION_MAX_METRES = 25f


    // Liste privée contenant le trajet actuel.
    // MutableStateFlow permet de modifier les données.
    private val _currentPath =
        MutableStateFlow<List<Location>>(emptyList())


    // Version publique du Flow.
    // Les écrans peuvent observer les données
    // mais ne peuvent pas modifier la liste directement.
    val currentPath: StateFlow<List<Location>> =
        _currentPath.asStateFlow()


    // Fonction appelée quand le service GPS reçoit de nouvelles positions.
    fun addLocations(locations: List<Location>) {

        // update récupère l'ancienne liste du trajet
        // puis retourne une nouvelle liste mise à jour.
        _currentPath.update { oldList ->

            // Création d'une copie modifiable du trajet actuel.
            val newList = oldList.toMutableList()

            // Dernier point conservé dans le trajet.
            // Sert à comparer les distances.
            var lastKeptLocation = newList.lastOrNull()

            // Parcours des nouvelles positions GPS reçues.
            for (location in locations) {

                // Ignore les points GPS trop imprécis.
                if (
                    location.hasAccuracy() &&
                    location.accuracy > PRECISION_MAX_METRES
                ) {
                    continue
                }

                // Si aucun point n'existe encore,
                // on garde automatiquement le premier.
                if (lastKeptLocation == null) {
                    newList.add(location)
                    lastKeptLocation = location
                    continue
                }

                // Distance entre le dernier point gardé
                // et le nouveau point GPS.
                val distance =
                    lastKeptLocation.distanceTo(location)

                // Garde le point seulement si la distance
                // minimale est respectée.
                if (distance >= DISTANCE_MIN_METRES) {
                    newList.add(location)
                    lastKeptLocation = location
                }
            }

            // Retourne la nouvelle liste filtrée.
            // Le Flow prévient automatiquement la carte.
            newList
        }
    }


    // Réinitialise complètement le trajet actuel.
    // Utilisé au début d'un nouveau voyage.
    fun clearPath() {
        _currentPath.value = emptyList()
    }
}