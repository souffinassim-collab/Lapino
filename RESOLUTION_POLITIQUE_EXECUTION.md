# 🔧 Résolution du Problème de Politique d'Exécution PowerShell

## ❌ Problème Identifié

Vous avez une erreur de politique d'exécution PowerShell qui empêche `npm` et `npx` de fonctionner :
```
Impossible de charger le fichier ... car l'exécution de scripts est désactivée
```

## ✅ Solution 1 : Modifier la Politique d'Exécution (RECOMMANDÉ)

### Étape 1 : Ouvrir PowerShell en Administrateur

1. Appuyez sur **Windows + X**
2. Sélectionnez **"Windows PowerShell (Admin)"** ou **"Terminal (Admin)"**
3. Cliquez sur **"Oui"** pour autoriser les modifications

### Étape 2 : Modifier la Politique

Dans PowerShell Admin, exécutez :

```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

Quand il demande confirmation, tapez **`Y`** et appuyez sur Entrée.

### Étape 3 : Vérifier

Fermez et rouvrez votre terminal, puis testez :
```powershell
npm --version
```

Cela devrait maintenant fonctionner !

## ✅ Solution 2 : Utiliser cmd.exe au lieu de PowerShell

1. Ouvrez **Invite de commandes** (cmd.exe) au lieu de PowerShell
2. Naviguez vers votre projet :
   ```cmd
   cd "D:\Projet Cursor\Lapino"
   ```
3. Lancez :
   ```cmd
   npm run web
   ```

## ✅ Solution 3 : Utiliser Node.js Directement

Si les solutions ci-dessus ne fonctionnent pas, vous pouvez utiliser node.exe directement :

```powershell
# Trouver le chemin de node_modules\.bin\expo
$expoPath = ".\node_modules\.bin\expo.cmd"

# Lancer Expo
& $expoPath start --web --port 8085
```

## 🚀 Après Avoir Résolu le Problème

Une fois que npm fonctionne, lancez :

```bash
npm run web
```

Ou avec un port spécifique :

```bash
npm start -- --port 8085
```

Puis appuyez sur **`w`** dans le terminal pour ouvrir dans le navigateur.

## 📝 Note

La politique `RemoteSigned` permet d'exécuter des scripts locaux (comme npm) tout en exigeant une signature pour les scripts téléchargés depuis Internet. C'est une configuration sécurisée recommandée.

