# 🎬 ÉTAPES VISUELLES - Déboguer Vidygo sur API 34

## 🎯 Objective
Déboguer l'application Vidygo sur l'émulateur Medium Phone API 34

## ⏱️ Temps requis: 5-10 minutes

---

## STEP 1️⃣ : FERMER ANDROID STUDIO (1 min)

```
1. Cliquez File → Exit (fermer complètement)
2. Attendez que tout se ferme
```

✅ Android Studio est complètement fermé

---

## STEP 2️⃣ : LANCER L'ÉMULATEUR (3 min)

### Méthode A : Double-clic (LA PLUS SIMPLRE)

```
1. Ouvrez l'explorateur Windows
2. Allez à: C:\Development\Android\Vidygo
3. Trouvez: start-emulator.bat
4. Double-cliquez dessus
5. Une fenêtre CMD apparaît
```

**Attendez ⏳ 2-3 minutes**

Vous verrez (dans l'ordre):
- [ ] Fenêtre noire
- [ ] Logo Android avec animation
- [ ] "Android" bleu en bas
- [ ] Écran d'accueil complet ✅

### Méthode B : Android Studio (Alternative)

```
1. Ouvrez Android Studio
2. Tools → Device Manager
3. Cherchez: Medium_Phone_API_34
4. Cliquez: ▶️ (bouton play vert)
5. Attendez le démarrage
```

### Méthode C : Ligne de commande (Pour experts)

```powershell
# PowerShell:
$ANDROID_HOME = 'C:\Users\nbillaud\AppData\Local\Android\Sdk'
& "$ANDROID_HOME\emulator\emulator.exe" -avd Medium_Phone_API_34
```

✅ L'émulateur est lancé et affiche Android

---

## STEP 3️⃣ : VÉRIFIER QUE ADB LE VOIT (1 min)

Ouvrez PowerShell ou CMD et exécutez:

```bash
adb devices
```

**Résultat attendu:**

```
List of devices attached
emulator-5554   device
```

✅ Si vous voyez `emulator-5554   device`, c'est bon!

❌ Si rien n'apparaît:
   → Attendez 30 secondes de plus
   → Puis réessayez

---

## STEP 4️⃣ : OUVRIR ANDROID STUDIO (2 min)

```
1. Cliquez sur l'icône Android Studio
2. Ouvrez le projet C:\Development\Android\Vidygo
3. Attendez la synchronisation (voir en bas)
   "Gradle sync..." → "Gradle sync finished" ✅
```

✅ Android Studio est synchronisé et prêt

---

## STEP 5️⃣ : VÉRIFIER QUE TOUT EST BIEN

À gauche dans Android Studio:

```
Project view:
├── app
├── src
│   ├── main
│   │   ├── java (code couleur normal)
│   │   └── res (ressources)
│   ├── test
│   └── androidTest
└── build.gradle.kts (sans erreurs rouges)
```

✅ Pas d'erreurs rouges dans les fichiers

---

## STEP 6️⃣ : PLACER UN BREAKPOINT (1 min)

**Dans le fichier MainActivity.java:**

```
1. File → Project → app → src → main → java → com.example.vidygo
2. Double-cliquez: MainActivity.java
3. Cherchez: onCreate()
4. Cliquez sur le numéro de ligne (ex: ligne 30)
5. Un point rouge aparaît ● <-- C'est le breakpoint!
```

✅ Breakpoint placé et visible (cercle rouge)

---

## STEP 7️⃣ : LANCER LE DEBUG (2 min)

**Dans Android Studio:**

```
Menu: Run → Debug 'app'
```

Ou clavier: **Shift+F9**

Une fenêtre peut demander:

```
"Select deployment target"
Sélectionner: emulator-5554 [Android 14]
✅ Cliquer: OK
```

**Attending... ⏳ (30-60 secondes)**

Vous verrez:
- [ ] "Waiting for process..." dans la console
- [ ] App qui s'installe sur l'émulateur
- [ ] "Process com.example.vidygo created"
- [ ] **Debug panel s'ouvre** ✅

---

## STEP 8️⃣ : DÉBOGUER! 🎉

Vous verrez maintenant:

```
Left Panel: Variables
┌─ Local
│  ├─ this: MainActivity
│  ├─ savedInstanceState: Bundle
│  └─ ...
└─ Watch
   └─ (empty)

Bottom Panel: Console
└─ Logs de l'application

Right Panel: Code
└─ Highlighting de la ligne en pause
   ← Vous êtes au breakpoint!
```

### Vous pouvez maintenant:

**Inspecting variables:**
```
Hover sur une variable dans le code
→ Voir sa valeur
```

**Step through code:**
```
F10 : Step over (saut simle)
F11 : Step into (rentre dans méthode)
Shift+F11 : Step out (sors de méthode)
```

**Continue execution:**
```
F8 : Continue jusqu'au prochain breakpoint
```

**Add watches:**
```
Console → Taper: this
→ Voir tous les attributs
```

✅ **VOUS ÊTES EN DEBUG!**

---

## 🎬 EXEMPLE LIVE

Vous mettez un breakpoint dans `MainActivity.onCreate()`:

```java
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);  ← BREAKPOINT ICI
    
    initializeViews();  
    initializeVideoList();
}
```

Quand vous lancez:
1. App s'exécute
2. **S'arrête au breakpoint** 🛑
3. Vous voyez `savedInstanceState` Bundle
4. Vous pouvez inspecter chaque variable
5. Appuyez F8 pour continuer

---

## ✅ CHECKLIST FINAL

- [ ] Émulateur lancé (écran Android visible)
- [ ] `adb devices` montre `emulator-5554   device`
- [ ] Android Studio synchronisé (Gradle done)
- [ ] Breakpoint placé (cercle rouge visible)
- [ ] Debug lancé (Shift+F9)
- [ ] Breakpoint déclenché (pause en rouge)
- [ ] Variables inspectables (left panel)
- [ ] Console affiche les logs

---

## 🆘 SI ÇA NE MARCHE PAS

### Émulateur ne démarre pas
```
→ Vérifier 4GB RAM libre
→ Vérifier 3GB disque libre
→ Redémarrer l'ordinateur
```

### ADB ne voit pas l'émulateur
```
adb kill-server
adb start-server
adb devices
```

### Android Studio ne le détecte pas
```
1. Fermer Android Studio
2. Attendre que l'émulateur soit prêt
3. Rouvrir Android Studio
4. Re-essayer Run → Debug
```

### Erreur de build
```
Build → Clean Project
Build → Rebuild Project
```

---

## 📞 VOIR AUSSI

- `GUIDE_FINAL_DEBUG.md` - Guide complet
- `DEBUG_EMULATOR_GUIDE.md` - Troubleshooting
- `SOLUTION_TROUVEE.md` - Explication du problème

---

**Bon débogage! 🚀**
Date: 29 mai 2026
For: Vidygo Android

