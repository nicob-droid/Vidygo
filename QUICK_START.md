# 🚀 Guide d'Installation Vidygo - 5 minutes

## ✨ Bienvenue dans Vidygo !

Cette application a été créée complètement pour vous permettre de gérer facilement vos vidéos YouTube préférées.

## 📋 Prérequis (avant de commencer)

Vérifiez que vous avez installé :
- ✅ **Android Studio** (4.0+)
- ✅ **JDK 11** ou plus récent
- ✅ **Android SDK** compilé pour API 36+

## ⚡ Installation en 5 étapes

### Étape 1: Ouvrir le projet

```
Android Studio → File → Open → C:\Development\Android\Vidygo
```

### Étape 2: Attendre la synchronisation

Attendez que Gradle se synchronise (vous verrez "Gradle sync finished" en bas)

### Étape 3: Créer un émulateur (si nécessaire)

```
Tools → AVD Manager → Create Virtual Device
→ Choisir Pixel 4 → Android 13+ → Finish
```

### Étape 4: Lancer l'émulateur

```
AVD Manager → Sélectionner votre device → ▶️ (play icon)
```

### Étape 5: Exécuter l'application

```
Run → Run 'app'
Ou: Appuyez sur Shift + F10
```

## ✅ Vérification du succès

Si tout va bien, vous verrez :
1. ✅ L'application se lance après ~30 secondes
2. ✅ L'écran Vidygo s'affiche avec un message vide
3. ✅ Un bouton **+** en bas à droite (ajouter vidéo)

## 🧪 Test rapide

1. **Cliquez sur le bouton +** en bas à droite
2. **Remplissez le formulaire:**
   - URL: `https://youtube.com/watch?v=dQw4w9WgXcQ`
   - Titre: `Test Video`
   - Chaîne: `Test Channel`
3. **Cliquez Enregistrer**
4. Vous devriez voir la vidéo dans la liste !

## 📚 Besoin d'aide ?

Consultez :
- **README.md** - Vue générale
- **GETTING_STARTED.md** - Guide complet
- **DOCUMENTATION.md** - Architecture
- **START_HERE.txt** - Résumé visuel

## 🔧 Troubleshooting

### L'application ne lance pas
```bash
./gradlew clean build
```

### Erreur "Cannot resolve symbol"
```
File → Invalidate Caches → Invalidate and Restart
```

### Gradle sync fails
```bash
File → Settings → Build, Execution, Deployment → Gradle
→ Gradle JDK → Select JDK 11
```

## 🎉 Bravo !

L'application est maintenant prête à utiliser. Consultez la documentation pour apprendre comment contribuer ou ajouter des fonctionnalités !

---

**Besoin de plus de détails? Lisez GETTING_STARTED.md**

