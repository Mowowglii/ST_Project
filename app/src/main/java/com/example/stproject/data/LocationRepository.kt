// Flow sert de liaison temps réel entre le service GPS et la carte.
// Le service envoie les nouvelles positions au Repository.
// Le Repository diffuse automatiquement les données via le StateFlow.
// La carte observe ce Flow et met à jour la Polyline sans accéder directement au service.
package com.example.stproject.data;

package com.example.stproject.data

import android.location.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object LocationRepository {

    // Liste privée et modifiable des points GPS du trajet en cours
    private val _currentPath = MutableStateFlow<List<Location>>(emptyList())

    // Version publique en lecture seule.
    // La carte peut l'observer, mais ne peut pas la modifier directement.
    val currentPath: StateFlow<List<Location>> = _currentPath.asStateFlow()

    // Ajoute plusieurs nouvelles positions GPS au trajet actuel.
    // Cette fonction sera appelée par le service de localisation.
    fun addLocations(locations: List<Location>) {
        _currentPath.update { oldList ->
            oldList + locations
        }
    }

    // Vide le trajet en mémoire.
    // À appeler au début d'un nouveau voyage.
    fun clearPath() {
        _currentPath.value = emptyList()
    }
}