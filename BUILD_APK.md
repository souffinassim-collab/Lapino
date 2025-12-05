# Instructions Build APK

## Méthode Recommandée: EAS Build (Cloud)

### 1. Installation d'EAS CLI

```bash
npm install -g eas-cli
```

### 2. Connexion à Expo

```bash
eas login
```

Si vous n'avez pas de compte, créez-en un gratuitement sur https://expo.dev

### 3. Configuration du projet

```bash
eas build:configure
```

### 4. Build de l'APK

```bash
eas build --platform android --profile production
```

### 5. Téléchargement

Attendez la fin du build (10-15 minutes). Un lien sera fourni pour télécharger l'APK.

## Méthode Alternative: Build Local

⚠️ **Plus complexe** - Nécessite Android Studio

### 1. Prérequis

- Android Studio installé
- Android SDK configuré
- Variable d'environnement `ANDROID_HOME` définie

### 2. Prebuild

```bash
npx expo prebuild
```

### 3. Build

```bash
cd android
./gradlew assembleRelease
```

### 4. APK

L'APK sera dans: `android/app/build/outputs/apk/release/app-release.apk`

## Signature de l'APK (Production)

Pour publier sur le Play Store, vous devrez signer l'APK:

1. Générez une clé de signature via EAS
2. Configurez les credentials dans `eas.json`
3. Build avec le profil production

## Tester l'APK

1. Transférez l'APK sur votre appareil Android
2. Activez "Sources inconnues" dans les paramètres
3. Installez l'APK
4. Testez toutes les fonctionnalités

## APK vs AAB

- **APK**: Pour distribution directe, test
- **AAB** (Android App Bundle): Pour Google Play Store

Pour AAB:
```bash
eas build --platform android --profile production
```
Puis modifiez `eas.json` pour utiliser `buildType: "aab"`
