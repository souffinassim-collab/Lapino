# Gestion Lapins - Application Android

Application mobile de gestion d'élevage de lapins développée avec React Native et Expo.

## 🐰 Fonctionnalités

- **Dashboard**: Vue d'ensemble avec statistiques et alertes de vaccinations
- **Gestion des Femelles**: Ajout, modification, suivi des lapines
- **Gestion des Clapets**: Suivi de l'occupation des cages
- **Gestion des Vaccins**: Base de données des vaccins avec durées
- **Vaccinations**: Enregistrement automatique avec calcul des prochaines dates
- **Alertes**: Notifications pour vaccins en retard (rouge) ou à venir (orange)
- **Thème Clair/Sombre**: Basculement entre modes

## 📱 Installation

### Prérequis
- Node.js (v14 ou supérieur)
- npm ou yarn
- Expo CLI: `npm install -g expo-cli`

### Installation des dépendances

```bash
npm install
```

## 🚀 Lancement de l'application

### Mode développement

```bash
npm start
```

Ensuite, scannez le QR code avec l'application **Expo Go** sur votre téléphone Android.

Ou appuyez sur `a` pour lancer sur un émulateur Android.

## 📦 Build APK

### Méthode 1: EAS Build (Recommandé)

1. Installer EAS CLI:
```bash
npm install -g eas-cli
```

2. Se connecter à Expo:
```bash
eas login
```

3. Configurer le projet:
```bash
eas build:configure
```

4. Build de l'APK:
```bash
eas build --platform android --profile production
```

5. Télécharger l'APK depuis le lien fourni après le build.

### Méthode 2: Build Local

1. Installer Android Studio avec SDK Android
2. Configurer les variables d'environnement (`ANDROID_HOME`)
3. Lancer:
```bash
npx expo prebuild
cd android
./gradlew assembleRelease
```

L'APK sera dans `android/app/build/outputs/apk/release/`

## 🎨 Structure du Projet

```
Lapino/
├── App.js                  # Point d'entrée
├── app.json               # Configuration Expo
├── package.json           # Dépendances
├── eas.json              # Configuration EAS Build
├── navigation/
│   └── AppNavigator.js   # Navigation
├── screens/
│   ├── DashboardScreen.js
│   ├── ClapletsScreen.js
│   ├── VaccinsScreen.js
│   └── Femelles/
│       ├── ListScreen.js
│       ├── DetailScreen.js
│       └── AddEditScreen.js
├── components/
│   ├── StatCard.js
│   ├── VaccineAlert.js
│   └── CustomButton.js
├── database/
│   ├── schema.sql        # Schéma SQL
│   └── db.js            # Fonctions DB
├── theme/
│   └── theme.js         # Thèmes clair/sombre
└── utils/
    ├── dateUtils.js     # Utilitaires dates
    └── constants.js     # Constantes
```

## 💾 Base de Données

SQLite avec 4 tables:
- **clapets**: Cages
- **femelles**: Lapines
- **vaccins**: Types de vaccins
- **vaccinations_femelles**: Historique des vaccinations

## 🔧 Technologies

- React Native
- Expo SDK 50
- React Navigation 6
- Expo SQLite
- React Native Paper

## 📝 Utilisation

1. **Dashboard**: Consultez les statistiques et les alertes
2. **Femelles**: Ajoutez vos lapines et assignez-les à des clapets
3. **Vaccins**: Créez la liste des vaccins avec leurs durées
4. **Vaccinations**: Enregistrez les vaccinations depuis la fiche femelle
5. **Alertes**: Suivez les vaccins en retard ou à venir

## 🌓 Thème

Basculez entre mode clair et sombre via le bouton 🌓 dans le header.

## 📄 Licence

Propriétaire - Tous droits réservés
