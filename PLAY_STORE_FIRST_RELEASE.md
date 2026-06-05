# Premiere livraison Play Store (Vidygo)

Ce guide couvre la mise en production de la premiere version Android App Bundle (AAB).

## 1) Verifier la configuration actuelle

- `versionCode` / `versionName` sont derives de `VERSION_MAJOR`, `VERSION_MINOR`, `VERSION_PATCH` dans `gradle.properties`.
- Le bundle release est generable via `:app:bundleRelease`.
- La signature release est configurable via proprietes (voir section 3).

## 2) Incrementer la version avant publication

Modifier `gradle.properties`:

```properties
VERSION_MAJOR=1
VERSION_MINOR=0
VERSION_PATCH=1
```

Regle pratique:
- patch: correctifs
- minor: nouvelles fonctionnalites retro-compatibles
- major: changements importants

## 3) Configurer la cle d'upload (recommande)

Ajouter dans `local.properties` (ne pas commit):

```properties
upload.store.file=C:/keystore/vidygo-upload.jks
upload.store.password=CHANGE_ME
upload.key.alias=vidygo_upload
upload.key.password=CHANGE_ME
```

Si ces 4 proprietes sont presentes, le build `release` sera signe automatiquement avec cette cle.

## 4) Build du bundle AAB

```powershell
Set-Location "C:\Development\Android\Vidygo"
.\gradlew.bat :app:bundleRelease --no-daemon
```

Sortie attendue:
- `app/build/outputs/bundle/release/app-release.aab`

## 5) Checklist Play Console (premiere publication)

### Fiche Store
- Titre app
- Description courte
- Description complete
- Icône 512x512
- Feature graphic 1024x500
- Captures ecran (telephone, et tablette si ciblee)
- Adresse email support
- URL politique de confidentialite

### Donnees et conformite
- Questionnaire "Securite des donnees" (Data safety)
- Declaration Ads (l'app affiche de la publicite)
- Classification du contenu (Content rating)
- Public cible (Target audience)

### Monetaire / pub
- IDs AdMob production dans `local.properties`:

```properties
admob.app.id=ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy
admob.banner.home.unit.id=ca-app-pub-xxxxxxxxxxxxxxxx/zzzzzzzzzz
```

- Tester en environnement de dev avec IDs de test uniquement.

## 6) Tests minimaux avant soumission

- Installation propre + ouverture app
- Rotation ecran (filtre conserve)
- Ajout/suppression videos
- Ecran Parametres + reset application
- Affichage de la banniere sans crash
- Theme clair/sombre

## 7) Publication recommandee

- Piste interne (test interne) d'abord
- Puis production progressive (rollout 10% -> 50% -> 100%)

## 8) Commandes utiles

```powershell
Set-Location "C:\Development\Android\Vidygo"
.\gradlew.bat :app:assembleRelease --no-daemon
.\gradlew.bat :app:bundleRelease --no-daemon
.\gradlew.bat :app:lintRelease --no-daemon
```

## Notes

- `local.properties` est ignore par Git: ideal pour secrets.
- Conserver la cle d'upload en lieu sur (backup chiffre).
- Une fois l'app publiee, ne jamais reutiliser un `versionCode` deja livre.

