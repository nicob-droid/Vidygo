# 🔧 Guide Complet - Fixation Problème Emulateur API 34

## 🚨 Problème: "Please select Android SDK"

Vous avez ce problème car :
1. ❌ L'émulateur n'est pas lancé
2. ❌ Android SDK n'est pas bien configuré
3. ❌ ADB ne voit pas l'émulateur

---

## ✅ SOLUTION EN 5 ÉTAPES

### Étape 1: Vérifier la configuration SDK

```bash
# Vérifier que Android SDK est bien défini
echo %ANDROID_HOME%
```

Si vide, ajoutez à vos variables d'environnement Windows :
- `ANDROID_HOME` = `C:\Users\nbillaud\AppData\Local\Android\Sdk`
- `PATH` = Ajouter `%ANDROID_HOME%\platform-tools`

---

### Étape 2: Relancer Android Studio

1. Fermez complètement Android Studio
2. Rouvrez-le
3. Attendez le chargement complet (2-3 min)

---

### Étape 3: Créer/Lancer l'Émulateur

#### Option A : Depuis Android Studio (RECOMMANDÉ)

```
Tools → Device Manager → Create device
→ Pixel 4 ou Medium Phone
→ API 34 (Android 14)
→ Next → Finish
```

Puis cliquez le bouton ▶️ pour démarrer

#### Option B : Ligne de commande

```bash
# Si emulator n'est pas trouvé, ajouter au PATH:
set PATH=%PATH%;C:\Users\nbillaud\AppData\Local\Android\Sdk\emulator

# Lancer l'émulateur
emulator -avd "Medium Phone API 34"
```

---

### Étape 4: Vérifier ADB

```bash
# Attendre 30 secondes après lancement de l'émulateur, puis:
adb devices

# RÉSULTAT ATTENDU:
# emulator-5554   device
```

---

### Étape 5: Build et Debug

```bash
# Dans Android Studio:
# 1. File → Sync Now
# 2. Run → Debug 'app'
# 3. Sélectionner l'émulateur
# 4. Cliquer "OK"
```

---

## 🛠️ TROUBLESHOOTING AVANCÉ

### Si ADB ne voit toujours pas l'émulateur:

```bash
# Redémarrer ADB
adb kill-server
adb start-server

# Vérifier à nouveau
adb devices
```

### Si l'émulateur ne démarre pas:

```bash
# Vérifier la configuration du SDK
sdkmanager --list

# Installer API 34 si nécessaire
sdkmanager "platforms;android-34"
sdkmanager "system-images;android-34;default;x86_64"
```

### Si "Compilation error":

```bash
# Dans Android Studio:
Build → Clean Project
Build → Rebuild Project

# Ou en ligne de commande:
./gradlew clean
./gradlew build
```

---

## 📋 CHECKLIST FINALE

- [ ] Émulateur lancé et vu dans `adb devices`
- [ ] Android Studio synchronisé (Gradle)
- [ ] Compilé sans erreur (Build tab)
- [ ] Sélectionné l'émulateur dans Run configuration
- [ ] Debug démarré (F9 ou Run → Debug)
- [ ] App visible sur l'émulateur
- [ ] Breakpoints déclenché (Debug tab)

---

## 🎯 RÉSOLUTION RAPIDE (3 MIN)

Si vous avez peu de temps :

1. **Fermer Android Studio**
2. **Rouvrir Android Studio**
3. **Tools → Device Manager → Lancer Medium Phone API 34**
4. **Attendre 1 min**
5. **Run → Debug 'app'**
6. **Sélectionner l'émulateur et OK**

Si ça ne marche pas :

```bash
adb kill-server
adb start-server
adb devices
```

Puis réessayer Run → Debug 'app'

---

## 📱 ALTERNATIVE: Utiliser un Device Physique

Si l'émulateur reste problématique :

1. Connecter un téléphone Android
2. Activer USB Debugging (Paramètres → À propos → Build → Appuyer 7x)
3. `adb devices` doit le voir
4. Run → Debug 'app' → Sélectionner le phone

---

## 🆘 SUPPORT SUPPLÉMENTAIRE

**Erreur persistante ?**

Vérifiez :
- [ ] Windows Hypervisor activé (si AMD/Intel)
- [ ] 4GB RAM minimum pour l'émulateur
- [ ] Espace disque (3GB min)
- [ ] Pas de conflit avec Virtualbox/VMware

Redémarrez l'ordinateur et réessayez.

---

**Created**: 29 mai 2026
**For**: Vidygo
**Status**: Guide complet de debug

