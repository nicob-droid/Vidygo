# Vidygo - Gestionnaire de Vidéos YouTube

## Description
Vidygo est une application Android simple et intuitive pour sauvegarder et accéder facilement à toutes vos vidéos YouTube préférées.

## Fonctionnalités principales

### 📹 Gestion des vidéos
- **Ajouter des vidéos** : Sauvegardez facilement l'URL d'une vidéo YouTube
- **Affichage en liste** : Consultez toutes vos vidéos sauvegardées dans une interface claire
- **Miniatures** : Chaque vidéo est affichée avec sa miniature
- **Informations** : Titre, nom de la chaîne, et date d'ajout

### 🎬 Actions rapides
- **Ouvrir** : Cliquez sur une vidéo pour l'ouvrir directement dans YouTube
- **Partager** : Partagez l'URL avec vos amis via SMS, email, réseaux sociaux, etc.
- **Supprimer** : Supprimez les vidéos que vous ne voulez plus garder

### 🎨 Interface utilisitive
- **Material Design 3** : Interface moderne et intuitive
- **État vide** : Message accueillant quand aucune vidéo n'est sauvegardée
- **Bouton flottant** : Accès facile à l'ajout de nouvelles vidéos
- **Cartes** : Chaque vidéo est affichée dans une belle carte

## Structure du projet

```
app/src/main/
├── java/com/example/vidygo/
│   ├── MainActivity.java           # Activité principale
│   ├── adapter/
│   │   └── VideoAdapter.java       # Adaptateur pour RecyclerView
│   └── model/
│       └── Video.java              # Modèle de données
├── res/
│   ├── layout/
│   │   ├── activity_main.xml       # Layout principal
│   │   └── item_video.xml          # Layout d'un élément vidéo
│   └── values/
│       ├── strings.xml             # Chaînes de caractères
│       ├── colors.xml              # Palette de couleurs
│       └── themes.xml              # Thème de l'application
└── AndroidManifest.xml
```

## Dépendances

- **AndroidX AppCompat** : Compatibilité rétroactive
- **Material Components** : Composants Material Design 3
- **AndroidX RecyclerView** : Affichage optimisé des listes
- **AndroidX CardView** : Cartes pour afficher les vidéos

## À faire

- [ ] Implémenter la persistance des données (SQLite ou Room)
- [ ] Créer un dialog/formulaire pour ajouter une vidéo
- [ ] Charger les miniatures avec Glide ou Picasso
- [ ] Ajouter la recherche et le filtrage
- [ ] Implémenter les catégories/listes de lecture
- [ ] Ajouter les tests unitaires
- [ ] Implémenter l'export/import de listes

## Installation

1. Clonez le projet
2. Ouvrez-le dans Android Studio
3. Attendez que Gradle se synchronise
4. Exécutez l'application sur un émulateur ou un appareil

## Publication Play Store

Consultez `PLAY_STORE_FIRST_RELEASE.md` pour la checklist complete de premiere livraison (versioning, signature upload key, AAB, Play Console).

## Utilisation

1. **Ajouter une vidéo** : Appuyez sur le bouton `+` en bas à droite
2. **Ouvrir une vidéo** : Cliquez sur une vidéo pour l'ouvrir sur YouTube
3. **Partager** : Appuyez sur l'icône de partage
4. **Supprimer** : Appuyez sur l'icône de suppression

## License

Ce projet est sous licence MIT.

## Auteur

Créé avec ❤️ pour Android

