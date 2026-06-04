@echo off
REM ============================================
REM Script de démarrage de l'émulateur Vidygo
REM ============================================

echo.
echo ========================================
echo  Vidygo - Demarrage Emulateur
echo ========================================
echo.

REM Vérifier si ANDROID_HOME est défini
if "%ANDROID_HOME%"=="" (
    echo ERREUR: ANDROID_HOME n'est pas défini!
    echo.
    echo Solution:
    echo 1. Ajouter une variable d'environnement ANDROID_HOME
    echo 2. Valeur: C:\Users\%USERNAME%\AppData\Local\Android\Sdk
    echo 3. Redémarrer ce script
    pause
    exit /b 1
)

echo Android SDK trouvé: %ANDROID_HOME%
echo.

REM Étape 1: Tuer les processus ADB/Emulator existants
echo [1/4] Arrêt des processus ADB existants...
adb kill-server
timeout /t 2 /nobreak

REM Étape 2: Redémarrer ADB
echo [2/4] Démarrage du serveur ADB...
adb start-server
timeout /t 3 /nobreak

REM Étape 3: Afficher les AVDs disponibles
echo [3/4] Vérification des émulateurs disponibles...
echo.
%ANDROID_HOME%\emulator\emulator.exe -list-avds
echo.

REM Étape 4: Lancer l'émulateur
echo [4/4] Lancement de l'émulateur "Medium Phone API 34"...
echo.
echo Cela peut prendre 1-2 minutes...
echo Gardez cette fenêtre ouverte.
echo.

%ANDROID_HOME%\emulator\emulator.exe -avd Medium_Phone_API_34 -verbose

echo.
echo ========================================
echo Emulateur demarré avec succes!
echo ========================================
pause

