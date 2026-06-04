#!/usr/bin/env powershell
<#
    .SYNOPSIS
    Script de démarrage et debug pour Vidygo Android

    .DESCRIPTION
    Lance l'émulateur API 34 et prépare Vidygo pour le debug

    .EXAMPLE
    .\setup-debug.ps1
#>

param(
    [ValidateSet("emulator", "debug", "reset")]
    [string]$Action = "emulator"
)

# Couleurs
$Red = [ConsoleColor]::Red
$Green = [ConsoleColor]::Green
$Yellow = [ConsoleColor]::Yellow
$Cyan = [ConsoleColor]::Cyan

function Write-Header {
    param([string]$Text)
    Write-Host "`n" -NoNewline
    Write-Host "╔══════════════════════════════════════════════════════╗" -ForegroundColor $Cyan
    Write-Host "║ $Text".PadRight(53) + "║" -ForegroundColor $Cyan
    Write-Host "╚══════════════════════════════════════════════════════╝" -ForegroundColor $Cyan
}

function Write-Success {
    param([string]$Text)
    Write-Host "✅ $Text" -ForegroundColor $Green
}

function Write-Error {
    param([string]$Text)
    Write-Host "❌ ERREUR: $Text" -ForegroundColor $Red
}

function Write-Info {
    param([string]$Text)
    Write-Host "ℹ️  $Text" -ForegroundColor $Cyan
}

function Write-Warning {
    param([string]$Text)
    Write-Host "⚠️  $Text" -ForegroundColor $Yellow
}

# Vérifier ANDROID_HOME
Write-Header "Vérification Configuration"

if (Test-Path env:ANDROID_HOME) {
    $androidHome = $env:ANDROID_HOME
    Write-Success "ANDROID_HOME trouvé: $androidHome"
} else {
    Write-Error "ANDROID_HOME n'est pas défini"
    Write-Info "Configuration requise:"
    Write-Host "  1. Aller à: Paramètres → Variables d'environnement"
    Write-Host "  2. Ajouter: ANDROID_HOME = C:\Users\$env:USERNAME\AppData\Local\Android\Sdk"
    Write-Host "  3. Redémarrer PowerShell"
    exit 1
}

# Vérifier emulator
$emulatorPath = "$androidHome\emulator\emulator.exe"
if (Test-Path $emulatorPath) {
    Write-Success "Emulateur trouvé"
} else {
    Write-Error "Emulateur non trouvé à $emulatorPath"
    exit 1
}

# Vérifier adb
$adbPath = "$androidHome\platform-tools\adb.exe"
if (Test-Path $adbPath) {
    Write-Success "ADB trouvé"
} else {
    Write-Error "ADB non trouvé à $adbPath"
    exit 1
}

# Actions
if ($Action -eq "reset") {
    Write-Header "Réinitialisation ADB"
    Write-Info "Arrêt du serveur ADB..."
    & $adbPath kill-server
    Start-Sleep -Seconds 2
    Write-Info "Redémarrage du serveur ADB..."
    & $adbPath start-server
    Start-Sleep -Seconds 3
    Write-Success "ADB réinitialisé"
}

if ($Action -eq "emulator") {
    Write-Header "Lancement Emulateur"

    Write-Info "Étape 1: Arrêt des services existants..."
    & $adbPath kill-server
    Start-Sleep -Seconds 2

    Write-Info "Étape 2: Redémarrage ADB..."
    & $adbPath start-server
    Start-Sleep -Seconds 3

    Write-Info "Étape 3: Vérification des AVDs..."
    & $emulatorPath -list-avds

    Write-Info "Étape 4: Lancement de l'émulateur (cela peut prendre 2 min)..."
    Write-Warning "Gardez cette fenêtre ouverte pendant que l'émulateur démarre"

    & $emulatorPath -avd "Medium Phone API 34" -verbose
}

if ($Action -eq "debug") {
    Write-Header "Préparation Debug"

    Write-Info "Étape 1: Nettoyage Gradle..."
    & ./gradlew clean

    Write-Info "Étape 2: Synchronisation Gradle..."
    & ./gradlew sync

    Write-Info "Étape 3: Vérification des devices..."
    & $adbPath devices

    Write-Success "Prêt pour déboguer!"
    Write-Info "Ouvrez Android Studio et lancez Run → Debug 'app'"
}

Write-Host "`n"

