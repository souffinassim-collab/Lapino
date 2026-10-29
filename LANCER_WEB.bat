@echo off
chcp 65001 >nul
echo ========================================
echo   Lancement de Lapino sur le Web
echo ========================================
echo.

REM Vérifier si node_modules existe
if not exist "node_modules" (
    echo Dossier node_modules introuvable. Installation des dépendances...
    call npm install
    if errorlevel 1 (
        echo Erreur lors de l'installation des dépendances.
        pause
        exit /b 1
    )
)

REM Arrêter les processus Node.js existants
echo Arrêt des processus Node.js existants...
taskkill /F /IM node.exe >nul 2>&1
timeout /t 2 /nobreak >nul

REM Lancer Expo Web
echo Lancement du serveur sur http://localhost:8085...
echo.
echo Appuyez sur 'w' dans le terminal pour ouvrir dans le navigateur
echo Appuyez sur Ctrl+C pour arrêter le serveur
echo.

call npx expo start --web --port 8085 --clear

pause
