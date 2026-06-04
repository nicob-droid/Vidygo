# 🚀 SOLUTION IMMÉDIATE - Problème Émulateur API 34

## ❌ Votre problème:
```
Edit configuration : Error: Please select Android SDK
```

## ✅ Solution rapide (2 minutes):

### Étape 1: Fermer Android Studio
- Fermez complètement l'application

### Étape 2: Lancer l'émulateur AVANT Android Studio

**Option A - Script fourni:**
```bash
# Double-cliquez sur: start-emulator.bat
# Ou en PowerShell:
.\setup-debug.ps1 -Action emulator
```

**Option B - Ligne de commande:**
```bash
# Ouvrir PowerShell/CMD et exécuter:
set ANDROID_HOME=C:\Users\nbillaud\AppData\Local\Android\Sdk
%ANDROID_HOME%\emulator\emulator.exe -avd "Medium Phone API 34"
```

⏳ **Attendre 1-2 minutes** (l'écran de démarrage Android apparaîtra)

### Étape 3: Rouvrir Android Studio
- Android Studio va détecter automatiquement l'émulateur

### Étape 4: Synchroniser et Build
```
File → Sync Now
Build → Rebuild Project
```

### Étape 5: Déboguer
```
Run → Debug 'app'
```

---

## 🔧 Si ça ne marche pas:

### Réinitialiser ADB:
```bash
# PowerShell/CMD:
adb kill-server
adb start-server
adb devices
```

### Nettoyer le cache Gradle:
```bash
cd C:\Development\Android\Vidygo
./gradlew clean
./gradlew build
```

### Vérifier variables d'environnement:
1. **Panneau de configuration → Variables d'environnement**
2. **Vérifier/Ajouter:**
   - `ANDROID_HOME` = `C:\Users\nbillaud\AppData\Local\Android\Sdk`
   - `PATH` inclut `%ANDROID_HOME%\platform-tools`
3. **Redémarrer PowerShell/CMD**

---

## 📋 Checklist avant de relancer

- [ ] Windows Hypervisor activé (PC requis)
- [ ] 4GB RAM disponible pour l'émulateur
- [ ] 3GB espace disque libre
- [ ] Pas de Virtualbox/VMware en conflit
- [ ] Android SDK API 34 installé
- [ ] Émulateur "Medium Phone API 34" existe

---

## 🎯 Commande ultime (copy-paste):

```powershell
# PowerShell en mode admin:
$env:ANDROID_HOME = "C:\Users\nbillaud\AppData\Local\Android\Sdk"
& "$env:ANDROID_HOME\emulator\emulator.exe" -avd "Medium Phone API 34"
```

Attendez que l'émulateur démarre, puis dans Android Studio :
```
Run → Debug 'app'
```

---

## 💡 Astuce Pro

Créer un raccourci sur le bureau pour lancer l'émulateur :

**Créer un fichier `launch-emulator.bat`:**
```batch
@echo off
set ANDROID_HOME=C:\Users\nbillaud\AppData\Local\Android\Sdk
%ANDROID_HOME%\emulator\emulator.exe -avd "Medium Phone API 34"
pause
```

Double-cliquez pour lancer l'émulateur facilement!

---

## 📞 Besoin d'aide?

Si encore un problème :
1. Vérifiez la console (Run tab ou Logcat)
2. Consultez `DEBUG_EMULATOR_GUIDE.md`
3. Vérifiez que API 34 est installé : `sdkmanager --list`

---

**Essayez maintenant et dites-moi si ça fonctionne!** 🚀

