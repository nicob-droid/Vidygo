# Guide de démarrage - Vidygo

## Prérequis

- **Android Studio** (dernière version)
- **JDK 11** ou ultérieur
- **Android SDK** compilé pour API 36+
- **Émulateur Android** ou un appareil physique (API 26+)

## Installation et configuration

### 1. Cloner le projet

```bash
git clone <repository-url>
cd Vidygo
```

### 2. Synchroniser Gradle

Dans Android Studio :
1. Cliquez sur **File** → **Sync Now**
2. Attendez que la synchronisation se termine

Ou en ligne de commande :
```bash
./gradlew build
```

### 3. Créer un AVD (Émulateur)

Si vous n'avez pas d'émulateur :

1. Ouvrez **AVD Manager** (Tools → AVD Manager)
2. Cliquez **Create Virtual Device**
3. Sélectionnez un appareil (par exemple Pixel 4)
4. Choisissez Android API 30+
5. Cliquez **Finish**

### 4. Exécuter l'application

#### Sur un émulateur
1. Dans Android Studio, sélectionnez votre AVD
2. Cliquez sur **▶ Run** ou appuyez sur **Shift + F10**

#### Sur un appareil physique
1. Activez le mode USB Debugging sur votre appareil
   - Allez dans Paramètres → À propos du téléphone
   - Appuyez 7 fois sur le numéro de version
   - Allez dans Développement → USB Debugging
2. Connectez votre appareil
3. Cliquez sur **▶ Run** dans Android Studio

## Structure du projet après déploiement

```
Vidygo/
├── app/
│   ├── build.gradle.kts                    # Configuration Gradle
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/example/vidygo/
│   │   │   │   ├── adapter/
│   │   │   │   ├── config/
│   │   │   │   ├── model/
│   │   │   │   ├── util/
│   │   │   │   ├── MainActivity.java
│   │   │   │   └── AddVideoActivity.java
│   │   │   └── res/
│   │   │       ├── layout/
│   │   │       └── values/
│   │   └── test/
│   └── .gitignore
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── README.md
└── DOCUMENTATION.md
```

## Utilisation de base

### 📱 Écran Principal (MainActivity)

L'application s'ouvre sur la page principale affichant :

- **Liste des vidéos sauvegardées** au format carte
- Chaque carte contient :
  - Miniature de la vidéo
  - Titre
  - Nom de la chaîne
  - Boutons d'action (partage, suppression)
- **Bouton flottant (+)** en bas à droite pour ajouter une vidéo
- **État vide** si aucune vidéo n'est sauvegardée

### ➕ Ajouter une vidéo

1. Appuyez sur le bouton **+** en bas à droite
2. Remplissez le formulaire :
   - **URL YouTube** : Lien complet vers la vidéo
   - **Titre** : Titre de la vidéo
   - **Chaîne** : Nom du créateur
3. Cliquez sur **Enregistrer**
4. La vidéo apparait immédiatement dans la liste

### ▶️ Lire une vidéo

Appuyez sur une carte vidéo pour :
- Ouvrir la vidéo dans YouTube (si l'app est installée)
- Sinon, ouvrir dans le navigateur par défaut

### 📤 Partager une vidéo

Appuyez sur l'icône **Partager** sur une carte pour :
- Partager le lien via SMS, email, réseaux sociaux, etc.
- Une préparation du message avec le titre et le lien

### 🗑️ Supprimer une vidéo

Appuyez sur l'icône **Corbeille** pour supprimer définitivement une vidéo

## Troubleshooting

### L'application ne lance pas
- Vérifiez que l'AVD est en cours d'exécution
- Arrêtez l'ancienne version : `adb uninstall com.example.vidygo`
- Reconstruisez : `./gradlew clean build`

### Erreur "Cannot resolve symbol"
- Cliquez sur **File → Invalidate Caches → Invalidate and Restart**
- Refaites la synchronisation Gradle

### Les vidéos ne s'affichent pas
- C'est normal pour le moment, les données sont en mémoire
- Implémenter Room Database pour la persistance réelle

### L'émulateur est lent
- Activez l'accélération GPU
- Réduisez la résolution de l'écran
- Utilisez un appareil physique si possible

## Développement

### Ajouter une nouvelle fonctionnalité

1. Créez la classe/layout nécessaire
2. Intégrez-la dans l'architecture existante
3. Testez sur l'émulateur
4. Faites un commit avec un message descriptif

### Déboguer

- Utilisez **Logcat** (View → Tool Windows → Logcat)
- Filtrez par "Vidygo" pour voir les logs
- Utilisez le **Debugger** (Debug → Debug App)

### Modifier le thème

- Éditez `app/src/main/res/values/colors.xml`
- Éditez `app/src/main/res/values/themes.xml`
- Reconstruisez et testez

## Évolution du projet

### Phase 1 (Actuellement) ✅
- Interface utilisateur de base
- Affichage des vidéos
- Ajout/suppression basiques

### Phase 2 🔄 (À faire)
- Base de données Room
- Chargement des miniatures (Glide)
- Recherche et filtres

### Phase 3 (Futur)
- Catégories
- Statistiques
- Synchronisation cloud

## Ressources utiles

- [Documentation Android](https://developer.android.com/)
- [Material Design Guidelines](https://material.io/design)
- [AndroidX Releases](https://developer.android.com/jetpack/androidx/releases)
- [Gradle Documentation](https://gradle.org/documentation/)

## Support

Pour les questions ou problèmes :
1. Consultez la documentation
2. Vérifiez les issues GitHub
3. Créez une nouvelle issue descriptive

## Licence

Ce projet est sous licence MIT.

---

**Bon développement avec Vidygo! 🚀**

