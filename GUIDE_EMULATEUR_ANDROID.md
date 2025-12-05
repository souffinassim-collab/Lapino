# Guide : Lancer l'application sur un émulateur Android

## 📋 Prérequis

1. **Android Studio** doit être installé
   - Téléchargez-le depuis : https://developer.android.com/studio
   - Installez-le avec les composants Android SDK

2. **Variable d'environnement ANDROID_HOME** (optionnel mais recommandé)
   - Windows : Ajoutez `C:\Users\VotreNom\AppData\Local\Android\Sdk` à votre PATH
   - Ou définissez `ANDROID_HOME=C:\Users\VotreNom\AppData\Local\Android\Sdk`

## 🚀 Étapes pour lancer l'émulateur

### Étape 1 : Créer un émulateur Android (si vous n'en avez pas)

1. Ouvrez **Android Studio**
2. Allez dans **Tools > Device Manager** (ou **More Actions > Virtual Device Manager**)
3. Cliquez sur **Create Device**
4. Choisissez un appareil (ex: **Pixel 5**)
5. Sélectionnez une image système (ex: **API 33 - Android 13**)
6. Cliquez sur **Finish**

### Étape 2 : Démarrer l'émulateur

1. Dans **Device Manager**, cliquez sur le bouton **Play** (▶) à côté de votre émulateur
2. Attendez que l'émulateur démarre complètement (peut prendre 1-2 minutes)

### Étape 3 : Lancer l'application

1. Dans le terminal, lancez :
   ```bash
   npm start
   ```

2. Une fois le serveur Expo démarré, appuyez sur **`a`** dans le terminal pour ouvrir sur Android

   OU

   Si l'émulateur n'est pas détecté automatiquement :
   ```bash
   npm run android
   ```

## 🔧 Vérification

Pour vérifier que l'émulateur est détecté :

1. Ouvrez un nouveau terminal PowerShell
2. Naviguez vers le dossier Android SDK (généralement dans `%LOCALAPPDATA%\Android\Sdk\platform-tools`)
3. Exécutez :
   ```bash
   .\adb devices
   ```

Vous devriez voir votre émulateur listé.

## ❓ Problèmes courants

### L'émulateur n'est pas détecté

1. Vérifiez que l'émulateur est bien démarré
2. Vérifiez que ADB est dans votre PATH
3. Redémarrez l'émulateur
4. Redémarrez le serveur Expo

### Erreur "Execution Policy" dans PowerShell

Exécutez PowerShell en **administrateur** et tapez :
```powershell
Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned
```

### L'application ne se lance pas

1. Vérifiez les logs dans le terminal Expo
2. Vérifiez la console de l'émulateur dans Android Studio
3. Assurez-vous que l'émulateur a une connexion Internet

## 💡 Alternative : Utiliser Expo Go sur un appareil physique

Si l'émulateur pose problème, vous pouvez utiliser votre téléphone Android :

1. Installez **Expo Go** depuis le Play Store
2. Lancez `npm start`
3. Scannez le QR code avec Expo Go

