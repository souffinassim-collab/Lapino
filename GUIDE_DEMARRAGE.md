# Guide de Démarrage - Gestion Lapins

## 🚀 Étape 1: Installation des dépendances

Les dépendances ont déjà été installées. Si vous devez les réinstaller:

```bash
npm install
```

## 📱 Étape 2: Lancer l'application en développement

### Option A: Avec votre téléphone Android

1. Installez l'application **Expo Go** depuis le Play Store
2. Lancez le serveur de développement:
   ```bash
   npm start
   ```
3. Scannez le QR code affiché avec Expo Go

### Option B: Avec un émulateur Android

1. Installez Android Studio et configurez un émulateur
2. Lancez l'émulateur
3. Lancez le serveur:
   ```bash
   npm start
   ```
4. Appuyez sur `a` pour ouvrir sur Android

## 📦 Étape 3: Build de l'APK (Production)

### Prérequis
- Créer un compte Expo (gratuit): https://expo.dev
- Installer EAS CLI: `npm install -g eas-cli`

### Build

1. **Se connecter à Expo**:
   ```bash
   eas login
   ```

2. **Configurer le projet** (première fois uniquement):
   ```bash
   eas build:configure
   ```

3. **Lancer le build**:
   ```bash
   eas build --platform android --profile production
   ```

4. **Télécharger l'APK**:
   - Attendez la fin du build (~10-15 minutes)
   - Un lien de téléchargement sera fourni
   - Téléchargez et installez l'APK sur votre appareil Android

## 🔍 Vérification

Après le lancement, vérifiez:
- ✅ L'application démarre sans erreur
- ✅ La base de données SQLite est créée
- ✅ Navigation entre les écrans fonctionne
- ✅ Le thème clair/sombre fonctionne (bouton 🌓)
- ✅ Ajout/modification/suppression de données

## ❓ Problèmes courants

### Erreur "Execution Policy"
Sur Windows, exécutez PowerShell en administrateur:
```powershell
Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned
```

### Port déjà utilisé
Si le port 8081 est occupé:
```bash
npm start -- --port 8082
```

### Erreur de dépendances
Supprimez `node_modules` et réinstallez:
```bash
rm -rf node_modules
npm install
```

## 📚 Prochaines étapes

1. Testez l'application en développement
2. Ajoutez des données de test
3. Vérifiez les alertes de vaccination
4. Buildez l'APK pour production
5. Installez sur un appareil réel

Bon élevage ! 🐰
