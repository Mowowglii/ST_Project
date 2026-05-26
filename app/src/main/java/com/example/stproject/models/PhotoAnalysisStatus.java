package com.example.stproject.models;

/**
 * Cette énumération sert à représenter les différents
 * résultats possibles après l'analyse d'une photo.
 */
public enum PhotoAnalysisStatus {

    // La photo appartient au voyage
    ACCEPTED,

    // La photo est trop éloignée du trajet
    OUTSIDE_TRIP,

    // La photo ne contient pas de GPS EXIF
    MANUAL_VALIDATION_REQUIRED
}