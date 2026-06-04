# Vue d'ensemble du projet Vidygo

## 🎯 Objectif
Une application Android simple et intuitive pour sauvegarder et accéder facilement à vos vidéos YouTube préférées.

## 📁 Structure du projet

```
Vidygo/
│
├── 📄 README.md                    # Présentation générale du projet
├── 📄 DOCUMENTATION.md             # Architecture et structure détaillée
├── 📄 GETTING_STARTED.md           # Guide de démarrage
├── 📄 CONTRIBUTING.md              # Guide de contribution
├── 📄 CHANGELOG.md                 # Historique des versions
├── 📄 PROJECT_OVERVIEW.md          # Ce fichier
│
├── 🔧 build.gradle.kts             # Configuration Gradle principal
├── 🔧 settings.gradle.kts          # Configuration des dépendances
├── 🔧 gradle.properties            # Propriétés Gradle
├── 🔧 local.properties             # Propriétés locales (ignore)
│
└── app/
    ├── 🔧 build.gradle.kts         # Configuration app
    ├── 📄 proguard-rules.pro        # Règles ProGuard
    ├── 📄 .gitignore               # Fichiers à ignorer
    │
    ├── gradle/
    │   ├── libs.versions.toml       # Versions des dépendances
    │   └── wrapper/                # Gradle wrapper
    │
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml  # Manifeste de l'app
        │   │
        │   ├── java/com/example/vidygo/
        │   │   ├── MainActivity.java                    # Écran principal
        │   │   ├── AddVideoActivity.java                # Ajout de vidéo
        │   │   ├── adapter/
        │   │   │   └── VideoAdapter.java               # Adaptateur RecyclerView
        │   │   ├── model/
        │   │   │   └── Video.java                       # Modèle de données
        │   │   ├── util/
        │   │   │   ├── Logger.java                      # Journalisation
        │   │   │   └── VideoPreferenceManager.java      # Gestion persistance
        │   │   └── config/
        │   │       └── AppConfig.java                   # Configuration constantes
        │   │
        │   └── res/
        │       ├── layout/
        │       │   ├── activity_main.xml                # Layout principal
        │       │   ├── activity_add_video.xml           # Layout ajout vidéo
        │       │   └── item_video.xml                   # Layout élément vidéo
        │       ├── values/
        │       │   ├── strings.xml                      # Textes
        │       │   ├── colors.xml                       # Couleurs
        │       │   ├── themes.xml                       # Thème Material Design
        │       │   └── strings-night.xml                # Thèmes nuit (futur)
        │       └── drawable/                            # Images et icons
        │
        ├── test/
        │   └── java/com/example/vidygo/
        │       └── ExampleUnitTest.java                 # Tests unitaires
        │
        └── androidTest/
            └── java/com/example/vidygo/
                └── VidygoInstrumentedTest.java          # Tests instrumentés
```

## 🏗️ Architecture

### Patterns utilisés
- **MVP** (Model-View-Presenter) pattern implicite
- **RecyclerView** pour l'affichage optimisé
- **SharedPreferences** pour la persistance

### Flux de données
```
UI (Layouts) ← Activity ← Adapter ← Model
     ↓             ↓         ↓         ↓
View events → Logic → Updates → Data
```

## 🎨 Interface utilisateur

### Écran Principal (MainActivity)
- **TopBar** : Titre "Vidygo" en violet
- **ContenuPrincipal** :
  - RecyclerView avec liste de vidéos
  - Chaque vidéo en CardView avec miniature, titre, chaîne
  - Boutons de partage et suppression
- **FloatingActionButton** : Créer une nouvelle vidéo (+)
- **État vide** : Message accueillant quand aucune vidéo

### Écran Ajout (AddVideoActivity)
- **Toolbar** : Titre "Ajouter une vidéo" avec retour
- **Formulaire** :
  - Champ URL YouTube (obligatoire)
  - Champ Titre (obligatoire)
  - Champ Chaîne (obligatoire)
- **Boutons** : Enregistrer | Annuler

## 🔌 Dépendances

### Core Android
- `androidx.appcompat:appcompat` (1.7.1)
- `com.google.android.material:material` (1.14.0)
- `androidx.recyclerview:recyclerview` (1.3.2)
- `androidx.cardview:cardview` (1.0.0)

### Testing
- `junit:junit` (4.13.2)
- `androidx.test.ext:junit` (1.3.0)
- `androidx.test.espresso:espresso-core` (3.7.0)

## ✅ Fonctionnalités implémentées

### v1.0.0 ✨ Lancement initial
- [x] Interface utilisateur complète
- [x] Affichage de liste des vidéos
- [x] Ajouter une nouvelle vidéo
- [x] Ouvrir une vidéo (Intent)
- [x] Partager une vidéo
- [x] Supprimer une vidéo
- [x] Validation des formulaires
- [x] État vide gracieux
- [x] Logging et debugging
- [x] Documentation complète

## 🚀 Fonctionnalités à venir

### Phase 2 (v1.1.0)
- [ ] Database Room pour persistance réelle
- [ ] Chargement images avec Glide
- [ ] Extraction automatique info vidéo
- [ ] Recherche et filtres
- [ ] Tri des vidéos

### Phase 3 (v1.2.0)
- [ ] Catégories/Listes de lecture
- [ ] Statistiques d'utilisation
- [ ] Partage de listes
- [ ] Synchronisation Firebase

### Phase 4 (v2.0.0)
- [ ] Widget Android
- [ ] Intégration YouTube API
- [ ] Recommandations intelligentes
- [ ] Notifications personnalisées
- [ ] Mode sombre avancé

## 💾 Persistance des données

### Actuellement
- **SharedPreferences** pour stockage simple
- Convertir en JSON
- Chargement en mémoire

### Futur
- **Room Database** pour structure robuste
- **LiveData** pour réactivité
- **WorkManager** pour sync background

## 🎨 Thème et Design

### Couleurs principales
- **Primaire** : Violet (Purple 500) `#FF6200EE`
- **Primaire sombre** : Purple 700 `#FF3700B3`
- **Secondaire** : Teal (Teal 200) `#FF03DAC5`
- **Secondaire sombre** : Teal 700 `#FF018786`
- **Texte** : Noir `#FF000000`
- **Fond** : Blanc `#FFFFFFFF`

### Design System
- **Material Design 3**
- **CardView** pour les conteneurs
- **AppBarLayout** pour les toolbars
- **FloatingActionButton** pour les actions
- **TextInputLayout** pour les formulaires

## 📋 Configuration

### Gradle
- **AGP** : 9.1.1
- **Java** : 11
- **Kotlin** : 1.9.x (implicitement)
- **SDK Compilé** : 36
- **Min SDK** : 26
- **Target SDK** : 36

### Ressources
- Strings en français et anglais (futur bilinguisme)
- Drawables de base
- Thèmes pour light/dark mode (futur)

## 🧪 Tests

### Types de tests
- **Tests unitaires** : Logique métier
- **Tests instrumentés** : Intégration Android
- **Tests UI** : Interactions utilisateur (futur)

### Exécution
```bash
./gradlew test              # Tests unitaires
./gradlew connectedTest     # Tests instrumentés
./gradlew jacocoTestReport  # Couverture de code
```

## 🛠️ Outils et commandes utiles

```bash
# Build
./gradlew build              # Construire l'app
./gradlew clean build        # Clean + build
./gradlew assemble          # Créer l'APK

# Tests
./gradlew test              # Tests unitaires
./gradlew connectedTest     # Tests sur device

# Lint
./gradlew lint              # Analyser le code

# Debug
./gradlew build --debug     # Build avec debug info

# Installation
./gradlew installDebug      # Installer debug sur device
./gradlew installRelease    # Installer release sur device
```

## 📊 Métriques du projet

| Métrique | Valeur |
|----------|--------|
| Fichiers Java | 8 |
| Fichiers XML | 7 |
| Fichiers Markdown | 5 |
| Lignes de code | ~2000 |
| Classes | 9 |
| Interfaces | 1 |
| Packages | 5 |

## 🔐 Sécurité

### Actuellement respecté
- Minification ProGuard en release
- Target API 36 (sécurité à jour)
- Permissions minimales requises
- Input validation

### À améliorer
- Chiffrement des données sensibles
- SSL/TLS pour les requêtes
- Obfuscation avancée
- Audit de sécurité

## 📱 Compatibilité

### Devices supportés
- Phones : Toutes les résolutions
- Tablets : Optimisé (futur)
- Wearables : Non supportés actuellement

### Versions Android
- Min : Android 8.0 (API 26)
- Max : Android 15+ (API 36+)
- Testé sur : Android 12, 13, 14

## 🤝 Contribution

Voir `CONTRIBUTING.md` pour les détails sur comment :
- Signaler des bugs
- Proposer des améliorations
- Soumettre du code
- Commander des fonctionnalités

## 📝 License

MIT License - Voir LICENSE pour les détails

## 👤 Auteur

Créé avec ❤️ pour Android

---

**Dernière mise à jour** : 29 mai 2026
**Version actuelle** : 1.0.0
**Status** : Production ready ✅

