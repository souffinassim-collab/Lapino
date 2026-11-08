---
name: build-apk
description: Compile l'APK release Lapino (R8), copie Lapino.apk à la racine, et optionnellement l'installe sur un téléphone via adb. Utiliser quand l'utilisateur demande de builder, compiler, générer l'APK, installer sur le téléphone, ou vérifier que le code compile.
---

# Build de l'APK Lapino

Ne PAS lancer `BUILD_APK.bat` (il fait `pause` et ouvre l'Explorateur). Utiliser Gradle directement via le Bash tool (Git Bash).

## 1. Compilation
```bash
cd "/d/Mes Projets/CURSOR/Lapino"
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-17.0.17.10-hotspot"
export ANDROID_HOME="$LOCALAPPDATA/Android/Sdk"
./gradlew assembleRelease --no-daemon -q 2>&1 | tail -40
```
- Timeout conseillé : 600000 ms. Pour vérifier seulement que le Kotlin compile (plus rapide) : `./gradlew :app:compileReleaseKotlin --no-daemon -q`.
- Si échec réseau (pare-feu / téléchargement de dépendances) : relancer jusqu'à 3 fois, puis essayer `--offline`.
- Si échec de compilation : montrer uniquement les lignes `e: file:///...:ligne:col` et corriger. Ne pas coller tout le log.
- JDK 17 introuvable : fallback `/c/Program Files/Java/jdk-21`.

## 2. Copie
```bash
cp app/build/outputs/apk/release/app-release.apk Lapino.apk && ls -lh Lapino.apk
```
Signaler la taille ; si elle dépasse nettement ~1 Mo, le mentionner (objectif du projet).

## 3. Installation (seulement si demandé)
```bash
ADB="$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe"
"$ADB" devices
"$ADB" install -r Lapino.apk
"$ADB" shell monkey -p com.lapino.gestion -c android.intent.category.LAUNCHER 1
```
Aucun appareil listé → demander à l'utilisateur d'activer le débogage USB. En cas de crash au lancement : `"$ADB" logcat -d -s AndroidRuntime:E | tail -50`.

## 4. Nouvelle version (seulement si demandé)
Incrémenter `versionCode` (+1) et `versionName` dans `app/build.gradle.kts` avant de compiler.
