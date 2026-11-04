@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

echo ========================================================
echo   🐰 Compilation de l'APK Native Lapino (Kotlin/Compose)
echo ========================================================
echo.

REM 1. Configuration automatique du JDK 17 ou 21
if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot"
) else if exist "C:\Program Files\Java\jdk-21\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-21"
)
echo [OK] JAVA_HOME : %JAVA_HOME%

REM 2. Configuration automatique du SDK Android
if exist "%LOCALAPPDATA%\Android\Sdk" (
    set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
    set "ANDROID_SDK_ROOT=%LOCALAPPDATA%\Android\Sdk"
)
echo [OK] ANDROID_HOME : %ANDROID_HOME%
echo.

cd /d "%~dp0"

REM 3. Boucle de compilation résiliente aux micro-coupures du pare-feu
set MAX_RETRIES=5
set RETRY_COUNT=1

:BUILD_LOOP
echo [Tentative !RETRY_COUNT!/%MAX_RETRIES%] Compilation Gradle Release (R8 activé)...
call gradlew.bat assembleRelease --no-daemon
if !ERRORLEVEL! EQU 0 goto BUILD_SUCCESS

set /a RETRY_COUNT+=1
if !RETRY_COUNT! LEQ %MAX_RETRIES% (
    echo.
    echo [Attention] Coupure réseau/pare-feu détectée. Nouvelle tentative dans 5 secondes...
    timeout /t 5 /nobreak >nul
    goto BUILD_LOOP
)

echo.
echo [ERREUR] La compilation a échoué après %MAX_RETRIES% tentatives.
pause
exit /b 1

:BUILD_SUCCESS
set "APK_SOURCE=app\build\outputs\apk\release\app-release.apk"
set "APK_DEST=Lapino.apk"

if exist "%APK_SOURCE%" (
    copy /Y "%APK_SOURCE%" "%APK_DEST%" >nul
    echo.
    echo ========================================================
    echo   ✅ SUCCÈS ! APK générée et optimisée avec succès :
    echo   📦 Fichier : %~dp0Lapino.apk
    echo ========================================================
) else (
    echo [ERREUR] Fichier APK introuvable dans %APK_SOURCE%.
)

echo.
pause
