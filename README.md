# Projet Mobile - Application de gestion de tâches

## Membres du groupe
- Théo Stoffelbach
- Nathan Reungoat

### Date de rendu : 12 janvier 2025

## Description du projet

Ce projet Android a été conçu pour gérer des tâches personnelles à l'aide d'une interface utilisateur intuitive et de la persistance de données. Il permet de créer, modifier et supprimer des tâches, ainsi que de recevoir des notifications pour les rappels de tâches programmées.

L'application contient également plusieurs pages essentielles telles que :
- **Import :** Permet à l'utilisateur de saisir ou de *mettre à jour* son numéro de téléphone, qui sert d'identifiant unique pour l'utilisateur.
- **About :** Contient des informations sur les créateurs du projet (Théo Stoffelbach et Nathan Reungoat).
- **FAQ :** Fournit des réponses aux questions fréquemment posées par les utilisateurs.

## Fonctionnalités principales

- **Persistance des données :** Utilisation de Firebase pour stocker les tâches et des préférences partagées (SharedPreferences) pour sauvegarder les informations utilisateur (ID utilisateur).
- **Gestion des activités :** L'application dispose de plusieurs écrans permettant d'afficher les tâches, de les ajouter, de les supprimer et de les modifier.
- **Gestion des notifications :** Les utilisateurs reçoivent des rappels de tâches via des notifications programmées.
- **Interface graphique et interaction utilisateur :** Une interface conviviale avec une bonne expérience utilisateur, comprenant un menu et des boîtes de dialogue pour la saisie des informations.

## Pages supplémentaires

1. **Import :**  
   Cette page permet à l'utilisateur de saisir un numéro de téléphone, ce qui est utilisé comme identifiant pour récupérer et gérer ses tâches dans l'application. Si un numéro est déjà enregistré, l'utilisateur peut le mettre à jour. Cependant, il ne peut pas modifier librement ce numéro en dehors de cette page.

2. **About :**  
   La page "À propos" affiche les noms des membres du groupe (Théo Stoffelbach et Nathan Reungoat) et offre une vue d'ensemble de notre travail sur ce projet.

3. **FAQ :**  
   La page FAQ contient une série de questions fréquentes pour aider l'utilisateur à mieux comprendre l'utilisation de l'application et résoudre les problèmes courants.

## Critères de notation

### Essentials

- **Mise en place de la persistance des données (Preferences et Firebase)** :
  Nous avons utilisé `SharedPreferences` pour stocker l'ID utilisateur et Firebase pour la gestion des tâches.
  
- **Changement et nombre d'activités** : 
  Plusieurs activités ont été ajoutées, notamment pour l'affichage des tâches, l'ajout de nouvelles tâches et la gestion des paramètres.

- **Gestion du bouton back** : 
  La gestion du bouton retour a été implémentée pour éviter que l'application ne se ferme involontairement.

- **Modèle métier / BDD** :
  Un modèle de données solide a été créé pour gérer les tâches avec des classes comme `Task`.

- **Qualité du code** : 
  Le code est structuré, avec une séparation claire entre les responsabilités des différentes classes et chaque fonction fait moins de 30 lignes.

- **Affichage d'une liste avec son adapter** :
  Les tâches sont affichées dans une liste via un `Adapter` pour une gestion dynamique des éléments.

- **Pertinence d'utilisation des layouts** : 
  Les layouts ont été utilisés de manière optimale pour une expérience utilisateur fluide et intuitive.

- **Qualité de l'interaction utilisateur** :
  L'application est réactive et facile à utiliser avec des notifications et des alertes adaptées.

### Bonus

- **Tâches en background** :  
  Nous avons implémenté la planification de notifications pour les rappels de tâches.

- **Réalisation de composants graphiques custom** :  
  Nous avons créé un bouton de suppression personnalisé pour chaque tâche dans la liste.

- **Qualité de l'interface graphique** :
  L'interface est propre et simple d'utilisation avec des éléments bien disposés.

- **Codage d'un menu** : 
  Un menu a été intégré pour accéder facilement aux différentes fonctionnalités de l'application.

## Technologies utilisées

- **Android Studio** pour le développement de l'application.
- **Firebase** pour la gestion des données.
- **Java** pour la logique de l'application.
- **SharedPreferences** pour le stockage des données locales.
- **Notifications** pour les rappels de tâches.

## Installation et utilisation

1. Clonez ce repository sur votre machine locale.
2. Ouvrez le projet avec Android Studio.
3. Exécutez l'applicati
