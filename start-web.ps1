# Script PowerShell pour lancer Expo Web
# Contourne les problèmes de politique d'exécution

Write-Host "Démarrage du serveur Expo Web..." -ForegroundColor Green

# Arrêter les processus Node.js existants
Write-Host "Arrêt des processus Node.js existants..." -ForegroundColor Yellow
Get-Process -Name node -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2

# Lancer Expo avec le port 8085
Write-Host "Lancement sur http://localhost:8085..." -ForegroundColor Cyan
& npm run web:port

