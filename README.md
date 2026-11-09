# 🐰 Lapino Élevage - Application Android Native

Application mobile de gestion d'élevage cunicole (lapines reproductrices) développée en **100 % Natif Android** avec **Kotlin**, **Jetpack Compose (Material 3)** et **SQLite**.

---

## ⚡ Caractéristiques Techniques

- **Langage & UI** : Kotlin + Jetpack Compose (Material 3)
- **Multilingue instantané** : Français 🇫🇷 et Arabe 🇸🇦 avec support RTL (Right-to-Left) natif
- **Base de données** : SQLite natif embarqué (`SQLiteOpenHelper`) avec migrations automatiques
- **Taille de l'APK** : **~0,97 Mo** (minifiée et optimisée avec **R8 / ProGuard**)
- **Compatibilité** : Android 7.0 (API 24) jusqu'à Android 16 (API 36)
- **100 % Hors-ligne** : Aucune dépendance à un serveur cloud, fonctionnement instantané sur le terrain

---

## 📋 Gestion Cunicole Intégrée

L'application intègre le cycle biologique de la cuniculiculture moderne :

1. **Mise au mâle (Saillie)** : Enregistrement de la date et du mâle reproducteur.
2. **Palpation abdominale (J+11 post-saillie)** : Alerte automatique pour effectuer le diagnostic de gestation (ampoules fœtales). Si négatif, remise au mâle immédiate.
3. **Pose de la boîte à nid (J+28)** : Alerte critique pour installer le nid garni de paille propre avant la mise bas (J+31).
4. **Mise bas & Carnet rose** : Comptage des lapereaux nés vivants et mort-nés.
5. **Sevrage (J+30 à J+35)** : Comptage des sevrés et calcul du **taux de survie au nid**.
6. **Bilan zootechnique par lapine** : Taux de fertilité, moyenne de lapereaux par portée, total sevrés.
7. **Suivi des vaccins & aliments** : Alertes pour rappels de vaccination (VHD, Myxomatose) et autonomie restante en jours de granulés / foin.

---

## 🛠️ Compilation & Exécution

### 1. Génération de l'APK autonome (`Lapino.apk`)
Double-cliquez sur :
```bat
BUILD_APK.bat
```
L'APK optimisée sera générée à la racine : **`Lapino.apk`**.

### 2. Développement sous Android Studio
Ouvrez directement ce dossier dans Android Studio. Le projet Gradle sera synchronisé automatiquement.
