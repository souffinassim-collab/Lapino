@echo off
echo ========================================
echo   Lancement de Lapino sur le Web
echo ========================================
echo.

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

call node_modules\.bin\expo.cmd start --web --port 8085 --clear

pause

