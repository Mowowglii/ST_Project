# Carnet de Voyage Intelligent
Une application mobile Android permettant aux voyageurs de tracer leur parcours en temps réel et de documenter les lieux visités.

## Vue d'ensemble
Problème identifié : Lors d'un voyage, l'utilisateur souhaite garder une trace visuelle et détaillée de son trajet et des lieux découverts, sans avoir à saisir manuellement ses déplacements.

Notre solution : Une application Android qui :

✅ Suit le trajet en temps réel via GPS
✅ Enregistre les Points d'Intérêts (POI) avec titre, description, avis et photos
✅ Stocke les données et médias dans le cloud

## Stack technique
Langage : Java + Kotlin
IDE : Android Studio
Localisation : FusedLocationProvider API (Google Play Services)
Stockage des données du voyage : Firebase Firestore
Stockage photos : Firebase Cloud Storage

## Ma contribution
- Conception d'architecture
- Design global de l'application et organisation des modules
- Service de localisation temps réel
- Implémentation du suivi continu de position avec FusedLocationProvider API
- Gestion des mises à jour de localisation et accuracy
- Visualisation du trajet en temps réel
- Gestion des Photos
- Upload de photos vers Firebase Cloud Storage
- Récupération et affichage des images depuis la base de données

## Statut du projet
✅ Compilation : L'app compile sans erreur
✅ Simulateur Android Studio : Le service de localisation est pleinement fonctionnel
⚠️ Déploiement sur device : Bloqué par des problèmes d'intégration

## Installation & Exécution
Prérequis :
- Android Studio (dernière version)
- Gradle configuré
- Firebase projet configuré avec google-services.json
- Cloner le repo
- Ouvrir le projet dans Android Studio
- Configurer l'émulateur avec localisation activée
- Lancer l'app sur l'émulateur (AVD Manager)
Note : Le service de localisation est testé et validé sur émulateur.

## Fonctionnalités principales
- Suivi GPS temps réel : Localisation continue avec FusedLocationProvider API
- Visualisation du trajet : Affichage de l'historique de déplacement
- Ajout de Points d'Intérêts : Création de POI avec titre, description, avis et photos
- Upload photos : Sauvegarde des images vers Firebase Cloud Storage
- Récupération photos : Chargement des images depuis la base de données

## Apprentissages clés
Gestion avancée de la localisation et permissions Android
Intégration Firebase en temps réel (Firestore + Cloud Storage)
Défis d'intégration en équipe et troubleshooting d'architecture

## Questions ?
Pour toute question sur ce projet, n'hésite pas à me contacter !
