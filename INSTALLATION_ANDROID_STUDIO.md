# Installation d'Android Studio et Configuration d'un Émulateur

## 🎯 Option 1 : Utiliser Expo Go sur votre téléphone (PLUS SIMPLE)

Si vous avez un téléphone Android, c'est la méthode la plus rapide :

1. **Installez Expo Go** depuis le Google Play Store
2. **Lancez le serveur** :
   ```bash
   npm start
   ```
3. **Scannez le QR code** affiché dans le terminal avec Expo Go
4. L'application se lancera automatiquement !

✅ **Avantages** : Pas besoin d'installer Android Studio, plus rapide, test sur un vrai appareil

---

## 🖥️ Option 2 : Installer Android Studio et créer un émulateur

### Étape 1 : Télécharger Android Studio

1. Allez sur : https://developer.android.com/studio
2. Cliquez sur **Download Android Studio**
3. Téléchargez le fichier d'installation pour Windows

### Étape 2 : Installer Android Studio

1. Lancez le fichier d'installation téléchargé
2. Suivez l'assistant d'installation :
   - Acceptez les licences
   - Choisissez les composants par défaut (Android SDK, Android SDK Platform, Android Virtual Device)
   - Choisissez le dossier d'installation (par défaut : `C:\Program Files\Android\Android Studio`)
3. Cliquez sur **Install** et attendez la fin de l'installation
4. Cliquez sur **Finish**

### Étape 3 : Configuration initiale

1. Lancez **Android Studio**
2. Si c'est la première fois, choisissez **Standard** pour la configuration
3. Acceptez les licences Android SDK
4. Attendez que le téléchargement des composants se termine

### Étape 4 : Créer un émulateur Android

1. Dans Android Studio, allez dans **Tools > Device Manager**
   (ou **More Actions > Virtual Device Manager**)

2. Cliquez sur **Create Device**

3. **Choisissez un appareil** :
   - Recommandé : **Pixel 5** ou **Pixel 6**
   - Cliquez sur **Next**

4. **Choisissez une image système** :
   - Recommandé : **API 33 (Android 13)** ou **API 31 (Android 12)**
   - Si l'image n'est pas téléchargée, cliquez sur **Download** à côté
   - Attendez le téléchargement (peut prendre plusieurs minutes)
   - Cliquez sur **Next**

5. **Vérifiez la configuration** :
   - Nom de l'AVD : Laissez par défaut ou personnalisez
   - Cliquez sur **Finish**

### Étape 5 : Démarrer l'émulateur

1. Dans **Device Manager**, vous verrez votre émulateur listé
2. Cliquez sur le bouton **Play** (▶) à côté de votre émulateur
3. Attendez que l'émulateur démarre (1-2 minutes la première fois)

### Étape 6 : Lancer l'application

1. Une fois l'émulateur démarré, dans votre terminal, lancez :
   ```bash
   npm start
   ```

2. Appuyez sur **`a`** dans le terminal pour ouvrir sur Android

   OU

   ```bash
   npm run android
   ```

3. L'application devrait se lancer automatiquement sur l'émulateur !

## ⚙️ Configuration optionnelle : Variable d'environnement

Pour faciliter l'utilisation d'ADB (Android Debug Bridge) :

1. Trouvez le chemin de votre Android SDK :
   - Généralement : `C:\Users\VotreNom\AppData\Local\Android\Sdk`
   - Ou : `C:\Program Files\Android\Android Studio\sdk`

2. Ajoutez au PATH Windows :
   - `C:\Users\VotreNom\AppData\Local\Android\Sdk\platform-tools`
   - `C:\Users\VotreNom\AppData\Local\Android\Sdk\tools`

3. Redémarrez votre terminal après modification

## ❓ Problèmes courants

### L'émulateur est lent

- Réduisez la RAM allouée dans les paramètres de l'AVD
- Activez l'accélération matérielle (HAXM) si disponible
- Utilisez un émulateur avec moins de RAM

### Erreur "HAXM not installed"

1. Téléchargez Intel HAXM depuis : https://github.com/intel/haxm/releases
2. Installez-le
3. Redémarrez l'émulateur

### L'émulateur ne démarre pas

- Vérifiez que la virtualisation est activée dans le BIOS
- Réduisez la RAM allouée à l'émulateur
- Essayez de créer un nouvel émulateur avec moins de ressources

## 💡 Recommandation

Pour un développement rapide, **utilisez Expo Go sur votre téléphone** (Option 1). C'est plus simple et plus rapide que d'installer Android Studio.

Pour des tests plus approfondis ou pour créer un APK, utilisez Android Studio (Option 2).

