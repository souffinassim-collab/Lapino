# Dépannage - Problème de connexion Web

## 🔍 Vérifications à faire

### 1. Vérifier que le serveur est démarré

Dans le terminal, vous devriez voir quelque chose comme :
```
Metro waiting on exp://192.168.x.x:8081
› Press w │ open web
```

### 2. Accéder à l'application

- **URL par défaut** : `http://localhost:8081`
- **Port personnalisé** : `http://localhost:8085` (si vous avez spécifié ce port)

### 3. Si "Connection Failed" apparaît

#### Solution 1 : Redémarrer le serveur proprement

1. Arrêtez tous les processus Node.js :
   ```powershell
   taskkill /F /IM node.exe
   ```

2. Nettoyez le cache :
   ```bash
   npm start -- --clear
   ```

3. Lancez le serveur web :
   ```bash
   npm run web
   ```

#### Solution 2 : Vérifier le port

Si le port 8081 est occupé :

```bash
npm start -- --port 8085
```

Puis appuyez sur `w` pour ouvrir dans le navigateur.

#### Solution 3 : Vérifier les erreurs dans la console

1. Ouvrez la console du navigateur (F12)
2. Regardez l'onglet **Console** pour les erreurs JavaScript
3. Regardez l'onglet **Network** pour voir si les fichiers se chargent

#### Solution 4 : Vérifier les dépendances

Si des erreurs apparaissent concernant des modules manquants :

```bash
npm install
```

#### Solution 5 : Vider le cache du navigateur

- Appuyez sur **Ctrl + Shift + Delete**
- Cochez "Images et fichiers en cache"
- Cliquez sur "Effacer les données"

Puis rechargez la page avec **Ctrl + F5**

### 4. Erreurs courantes

#### "Cannot GET /"
- Le serveur n'est pas démarré correctement
- Redémarrez avec `npm run web`

#### "ERR_CONNECTION_REFUSED"
- Le serveur n'écoute pas sur ce port
- Vérifiez que le serveur est bien démarré
- Vérifiez le port dans le terminal

#### Page blanche
- Ouvrez la console (F12) pour voir les erreurs
- Vérifiez que tous les fichiers se chargent dans l'onglet Network

### 5. Commandes utiles

```bash
# Démarrer le serveur web
npm run web

# Démarrer avec un port spécifique
npm start -- --port 8085

# Démarrer avec cache vidé
npm start -- --clear

# Voir tous les processus Node.js
Get-Process | Where-Object {$_.ProcessName -like "*node*"}

# Arrêter tous les processus Node.js
taskkill /F /IM node.exe
```

## 📞 Si le problème persiste

1. Vérifiez les logs dans le terminal où Expo est lancé
2. Vérifiez la console du navigateur (F12)
3. Assurez-vous que vous utilisez une version récente de Node.js
4. Essayez un autre navigateur (Chrome, Edge, Firefox)

