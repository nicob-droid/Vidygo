#!/usr/bin/env powershell
<#
    .SYNOPSIS
    Script pour corriger les erreurs de compilation Vidygo

    .DESCRIPTION
    Exécute toutes les étapes pour corriger les problèmes de compilation

    .EXAMPLE
    .\fix-compilation.ps1
#>

Write-Host "`n" -ForegroundColor Green
Write-Host "╔════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║         Correcteur de Compilation - Vidygo            ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════════════════════╝`n" -ForegroundColor Green

$projectRoot = "C:\Development\Android\Vidygo"

Write-Host "1️⃣ Arrêt des processus..." -ForegroundColor Cyan
Write-Host "   - Fermeture d'Android Studio..."
Get-Process adb -ErrorAction SilentlyContinue | Stop-Process -Force
Get-Process emulator -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep -Seconds 2

Write-Host "`n2️⃣ Nettoyage des caches..." -ForegroundColor Cyan
Write-Host "   - Suppression du répertoire build..."
Remove-Item -Path "$projectRoot\app\build" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "$projectRoot\build" -Recurse -Force -ErrorAction SilentlyContinue
Write-Host "   ✅ Caches supprimés"

Write-Host "`n3️⃣ Synchronisation Gradle..." -ForegroundColor Cyan
Write-Host "   - Exécution de sync..."
Set-Location $projectRoot
& .\gradlew.bat sync 2>&1 | Out-Null
Write-Host "   ✅ Sync complétée"

Write-Host "`n4️⃣ Compilation propre..." -ForegroundColor Cyan
Write-Host "   - Rebuild complet..."
& .\gradlew.bat clean build 2>&1 | Where-Object { $_ -match "BUILD|FAILED" }

Write-Host "`n5️⃣ Vérification du résultat..." -ForegroundColor Cyan
$buildResult = & .\gradlew.bat build 2>&1 | Select-String "BUILD"
if ($buildResult -like "*BUILD SUCCESSFUL*") {
    Write-Host "   ✅ COMPILATION RÉUSSIE!" -ForegroundColor Green
} else {
    Write-Host "   ⚠️ Compilation échouée, reportez-vous à FIX_COMPILATION_ERRORS.md" -ForegroundColor Yellow
}

Write-Host "`n6️⃣ Recommandations..." -ForegroundColor Cyan
Write-Host "   1. Ouvrez Android Studio"
Write-Host "   2. File → Sync Now"
Write-Host "   3. Build → Rebuild Project"
Write-Host "   4. Run → Run 'app'"

Write-Host "`n" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════════════════════╝`n" -ForegroundColor Green

