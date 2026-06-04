# Documentation de Vidygo

## Vue d'ensemble architecturale

Vidygo suit une architecture simple et claire basée sur les composants Android standards.

```
┌─────────────────────────────────────────────┐
│          Interface Utilisateur               │
│  (MainActivity, AddVideoActivity, Layouts)   │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│            Logique Métier                    │
│  (VideoAdapter, Listeners, Actions)          │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│           Données & Stockage                 │
│  (VideoPreferenceManager, SharedPreferences) │
└──────────────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│           Base de Données                    │
│  (Persistance - SharedPreferences/Room)     │
└──────────────────────────────────────────────┘
```

## Structure des packages

```
com.example.vidygo/
├── adapter/
│   └── VideoAdapter.java          # Adaptateur RecyclerView pour afficher les vidéos
├── config/
│   └── AppConfig.java             # Constantes de configuration
├── model/
│   └── Video.java                 # Modèle de données pour une vidéo
├── util/
│   ├── Logger.java                # Utilitaire de journalisation
│   └── VideoPreferenceManager.java # Gestion de la persistance
├── MainActivity.java              # Écran principal avec liste des vidéos
└── AddVideoActivity.java          # Écran pour ajouter une nouvelle vidéo
```

## Classes principales

### Video.java
Modèle de données représentant une vidéo YouTube.

**Attributs:**
- `id` : Identifiant unique de la vidéo
- `title` : Titre de la vidéo
- `channel` : Nom de la chaîne YouTube
- `thumbnailUrl` : URL de la miniature
- `videoUrl` : Lien vers la vidéo
- `dateAdded` : Timestamp d'ajout

**Méthodes:**
- Getters et setters pour tous les attributs

### VideoAdapter.java
Adaptateur pour afficher la liste des vidéos dans un RecyclerView.

**Interface:**
```java
public interface OnVideoActionListener {
    void onVideoClick(Video video);      // Clic sur une vidéo
    void onVideoDelete(Video video);     // Suppression
    void onVideoShare(Video video);      // Partage
}
```

**ViewHolder:**
- Affiche la miniature, titre, nom de la chaîne
- Boutons d'action : partager, supprimer

### MainActivity.java
Activité principale de l'application.

**Responsabilités:**
- Affichage de la liste des vidéos
- Gestion des clics sur les vidéos
- Ouverture de AddVideoActivity
- Gestion de l'état vide

**Implémentation d'interfaces:**
- `VideoAdapter.OnVideoActionListener`

**Méthodes principales:**
- `initializeViews()` : Initialise les références aux vues
- `initializeVideoList()` : Charge les vidéos
- `setupRecyclerView()` : Configure le RecyclerView
- `setupActions()` : Configure les listeners
- `updateEmptyState()` : Affiche/masque l'état vide

### AddVideoActivity.java
Activité pour ajouter une nouvelle vidéo.

**Champs de formulaire:**
- URL de la vidéo
- Titre
- Nom de la chaîne

**Validation:**
- Vérification que les champs ne sont pas vides
- Affichage d'erreurs appropriées

### VideoPreferenceManager.java
Gère la persistance des vidéos en SharedPreferences.

**Méthodes:**
- `saveVideos(List<Video>)` : Sauvegarde une liste de vidéos
- `getVideos()` : Récupère les vidéos sauvegardées
- `saveVideo(Video)` : Ajoute une vidéo
- `deleteVideo(String)` : Supprime une vidéo
- `clearAll()` : Efface toutes les vidéos

## Flux d'utilisation

### 1. Affichage de la liste (MainActivity)
```
onCreate()
  ├─> initializeViews()
  ├─> initializeVideoList()
  ├─> setupRecyclerView()
  ├─> setupActions()
  └─> updateEmptyState()
```

### 2. Ajout d'une vidéo
```
MainActivity.fabAddVideo.onClick()
  ├─> startActivity(AddVideoActivity)
  └─> AddVideoActivity.onCreate()
      ├─> initializeViews()
      └─> setupActions()
          └─> saveButton.onClick()
              ├─> validateInputs()
              └─> saveVideo()
```

### 3. Ouverture d'une vidéo
```
VideoAdapter.onVideoClick()
  └─> MainActivity.onVideoClick()
      └─> startActivity(Intent.ACTION_VIEW, videoUrl)
```

### 4. Partage d'une vidéo
```
VideoAdapter.onVideoShare()
  └─> MainActivity.onVideoShare()
      └─> startActivity(ACTION_SEND)
```

### 5. Suppression d'une vidéo
```
VideoAdapter.onVideoDelete()
  └─> MainActivity.onVideoDelete()
      ├─> videoList.remove(video)
      ├─> videoAdapter.updateVideos()
      └─> updateEmptyState()
```

## Fichiers de ressources

### Layouts
- `activity_main.xml` : Layout principal avec RecyclerView et FAB
- `activity_add_video.xml` : Layout pour ajouter une vidéo
- `item_video.xml` : Layout d'un élément vidéo

### Valeurs
- `strings.xml` : Chaînes de caractères
- `colors.xml` : Palette de couleurs
- `themes.xml` : Thème Material Design 3

## Prochaines étapes d'amélioration

### Court terme
- [ ] Implémenter la persistance réelle (Room Database)
- [ ] Charger les miniatures (Glide/Picasso)
- [ ] Ajouter l'extraction d'information depuis l'URL YouTube
- [ ] Implémenter la recherche
- [ ] Ajouter des tests unitaires

### Moyen terme
- [ ] Catégories/Listes de lecture
- [ ] Notifications
- [ ] Export/Import JSON
- [ ] Synchronisation cloud
- [ ] Recommandations basées sur les catégories

### Long terme
- [ ] Application Wear OS
- [ ] Widget
- [ ] Intégration avec YouTube API
- [ ] Analyses et statistiques
- [ ] Mode hors ligne avancé

## Convention de codage

### Nommage des variables
- `camelCase` pour les variables
- `UPPER_SNAKE_CASE` pour les constantes
- Préfixe `m` pour les variables membres privées optionnel

### Nommage des ressources
- ID des vues : `{type}_{description}` (ex: `btn_save`, `tv_title`)
- Drawables : `ic_` pour les icônes, `img_` pour les images
- Strings : `{section}_{key}` (ex: `empty_state_title`)

### Commentaires
- Javadoc pour les classes et méthodes publiques
- Commentaires en ligne pour la logique complexe
- TODO et FIXME pour les améliorations futures

### Structure des fichiers
```java
// 1. Package
package com.example.vidygo;

// 2. Imports (organisés)
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
// ...

// 3. Classe
/**
 * Documentation Javadoc
 */
public class MyClass {
    // Constantes
    // Variables de classe
    // Variables d'instance
    // Constructeurs
    // Méthodes publiques
    // Méthodes privées
}
```

## Gestion des erreurs

### Exceptions
- `try-catch` pour les opérations risquées (parsing JSON, accès fichiers)
- Logs des erreurs avec Logger
- Toast pour informer l'utilisateur

### Validation
- Validation des inputs dans AddVideoActivity
- Vérification de null partout

## Performance

### Optimisations implémentées
- RecyclerView pour l'affichage des listes
- Chargement des données asynchrone (en cours)

### Optimisations futures
- Pagination pour les listes longues
- Cache des miniatures
- Lazy loading des images
- Background threads pour les opérations I/O

