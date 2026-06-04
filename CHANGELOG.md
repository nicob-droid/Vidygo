# Changelog

Tous les changements notables de Vidygo sont documentés dans ce fichier.

Le format suit [Keep a Changelog](https://keepachangelog.com/),
et ce projet suit [Semantic Versioning](https://semver.org/).

## [1.0.0] - 2026-05-29

### Ajouté
- ✅ Interface utilisateur principale (MainActivity)
  - Affichage des vidéos en RecyclerView
  - Support pour l'état vide
  - Bouton flottant pour ajouter une vidéo
  - Toolbar avec titre de l'app

- ✅ Actions sur les vidéos
  - Ouvrir une vidéo sur YouTube
  - Partager une vidéo via Intent
  - Supprimer une vidéo de la liste
  - Clic direct pour ouvrir

- ✅ Ajout de nouvelles vidéos (AddVideoActivity)
  - Formulaire avec validation
  - Champs : URL, Titre, Chaîne
  - Gestion des erreurs
  - Navigation fluide

- ✅ Architecture et utilitaires
  - Modèle Video avec getters/setters
  - VideoAdapter personnalisé
  - VideoPreferenceManager pour la persistance
  - Logger pour le debugging
  - AppConfig pour les constantes

- ✅ Interface Material Design 3
  - Colors et thème cohérent
  - CardView pour l'affichage des vidéos
  - AppBarLayout avec MaterialToolbar
  - FloatingActionButton
  - TextInputLayout pour les formulaires

- ✅ Ressources et documentation
  - README.md - Vue d'ensemble
  - DOCUMENTATION.md - Architecture détaillée
  - GETTING_STARTED.md - Guide de démarrage
  - CONTRIBUTING.md - Guide de contribution
  - Strings et Colors localisés

- ✅ Tests de base
  - Tests instrumentés (VidygoInstrumentedTest)
  - Tests pour création et suppression de vidéos

### À faire
- [ ] Base de données Room pour la persistance
- [ ] Chargement des miniatures (Glide/Picasso)
- [ ] Extraction automatique d'info depuis YouTube
- [ ] Recherche et filtrage
- [ ] Catégories/Listes de lecture
- [ ] Notifications
- [ ] Export/Import de données
- [ ] Interface en français et anglais
- [ ] Tests complets
- [ ] CI/CD avec GitHub Actions

## [0.9.0] - 2026-05-28

### Ajouté
- Initialisation du projet Android
- Configuration Gradle de base
- Structure de paquets
- Fichiers de configuration initial

### Changé
- Mise à jour des dépendances vers les dernières versions

---

## Notes de version

### Version 1.0.0 - Première release
Vidygo vous permet de :
- 📹 Sauvegarder vos vidéos YouTube préférées
- 🎯 Accéder facilement à votre collection
- 🔗 Ouvrir les vidéos directement sur YouTube
- 📤 Partager les vidéos avec vos amis
- 🗑️ Gérer votre liste

Les données sont sauvegardées localement pour une expérience rapide.

---

## Compatibilité

| Élément | Version |
|---------|---------|
| Kotlin Gradle Plugin | 9.1.1+ |
| Android SDK Compilé | 36 |
| Target SDK | 36 |
| Min SDK | 26 |
| Java | 11+ |
| AndroidX AppCompat | 1.7.1+ |
| Material Components | 1.14.0+ |

---

## Migration depuis anciennes versions

Actuellement en première version (1.0.0).

Les futures mises à jour éviteront les breaking changes si possible.
Si nécessaire, des guides de migration seront fournis.

---

## Support des versions

| Version | Support |
|---------|---------|
| 1.0.x | ✅ Actif |
| 0.9.x | ⚠️ Maintenance |

---

Pour les détails des modifications, consultez :
- Les [Pull Requests](https://github.com/yourusername/Vidygo/pulls)
- L'[historique Git](https://github.com/yourusername/Vidygo/commits)

---

**Prochaine version plannifiée : 1.1.0**
- Room Database
- Support des images
- Optimisations de performance

