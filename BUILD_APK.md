# Guide de Compilation de l'APK Lapino (`Lapino.apk`)

L'application **Lapino** est une application **100 % Native Android** conçue en **Kotlin**, **Jetpack Compose (Material 3)** et **SQLite natif**, optimisée par le compilateur **R8**.

- **Taille de l'APK** : **~0,96 Mo (< 1 Mo)**
- **Compatibilité** : **Android 7.0 (API 24) à Android 16 (API 36)**
- **Fluidité** : 60 / 120 fps même sur smartphones d'entrée de gamme
- **Mode hors-ligne** : 100 % autonome, aucune dépendance serveur

---

## 🚀 Compiler l'APK en 1 Clic (Local)

Double-cliquez simplement sur le script situé à la racine du projet :
👉 [**`BUILD_APK.bat`**](file:///d:/Mes%20Projets/CURSOR/Lapino/BUILD_APK.bat)

### Fonctionnement du script :
1. Détection et configuration automatique du **JDK 17** (`Eclipse Adoptium\jdk-17` ou `Java\jdk-21`).
2. Détection du **SDK Android** (`%LOCALAPPDATA%\Android\Sdk`).
3. Compilation optimisée avec minification de code R8 (`gradlew.bat assembleRelease`).
4. Relance automatique (jusqu'à 5 tentatives) en cas de micro-coupure réseau liée au pare-feu.
5. Copie de l'APK finale directement à la racine : **`Lapino.apk`**.

---

## 💻 Ouverture dans Android Studio

Ouvrez simplement le dossier racine `Lapino` dans Android Studio :
1. Android Studio détectera automatiquement `settings.gradle.kts` et synchronisera le projet Gradle.
2. Cliquez sur **Run 'app'** (bouton vert ▶) pour lancer l'application sur votre smartphone ou émulateur.
