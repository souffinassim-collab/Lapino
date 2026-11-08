# Lapino — Gestion d'élevage cunicole (Android natif)

App Android 100 % hors-ligne pour suivre les lapines reproductrices : cycles (saillie → palpation → boîte à nid → mise bas → sevrage), vaccins, stocks d'aliments, KPIs. UI et données en **français**.

## Stack
- Kotlin 1.9.24, Jetpack Compose (BOM 2024.06.00, Material 3), AGP 8.13.1, JDK 17
- SQLite natif via `SQLiteOpenHelper` (pas de Room, pas de ViewModel, pas de DI, pas de réseau)
- minSdk 24, compile/targetSdk 35, release minifiée R8 + shrinkResources, signée avec la clé debug
- Package : `com.lapino.gestion`

## Carte du code (3 100 lignes, 5 fichiers Kotlin)
| Fichier | Rôle |
|---|---|
| `app/src/main/java/com/lapino/gestion/MainActivity.kt` | `LapinoApp` (navigation par onglets + état), onglets `DashboardTab`, `FemellesListTab`, `FemelleDetailView`, `ElevageStocksTab`, `VaccinsTab`, dialog `AddEditFemelleDialog`, petits composants (`SummaryStatCard`, `StatusPill`, `InfoRow`, `MiniKpiBox`) |
| `.../data/LapinoDatabase.kt` | Schéma, migrations (`onUpgrade`), seed, toutes les requêtes CRUD, `getReproductionAlerts`, `getVaccineAlerts`, stats |
| `.../model/Models.kt` | Data classes (`Femelle`, `CycleReproduction`, `Clapet`, `Vaccin`, `VaccinationFemelle`, `Aliment`, `*Stats`, `ReproAlert`) + `DateHelper` (dates ISO `yyyy-MM-dd`) |
| `.../ui/Components.kt` | `RabbitStatusCard`, `ReproductionDialog` (actions du cycle), `CuniTipBox` |
| `.../ui/Theme.kt` | `LapinoTheme`, palette |

## Règles métier clés (voir skill `cuniculture`)
- `startCycle` : palpation J+11, boîte à nid J+28, mise bas J+31 (depuis saillie). Sevrage prévu = mise bas + 35 j.
- Statuts cycle : `saillie` → `gestante` → `allaitante` → `termine` | `echec`. Statuts femelle : `vivante` | `vendue` | `morte`.
- Alertes : palpation dès J10, boîte à nid dès J27 si non posée, mise bas à ≤ 2 j, sevrage dès J30 après mise bas.
- Dates stockées en TEXT ISO ; toujours passer par `DateHelper`.

## Conventions
- Toute modification de schéma : incrémenter la version dans le constructeur `SQLiteOpenHelper` + ajouter l'`ALTER TABLE` dans `onUpgrade` + `CREATE TABLE` à jour (skill `db-migration`). Ne jamais casser les données des éleveurs.
- Accès DB synchrone depuis l'UI puis rafraîchissement de l'état via un compteur/refresh dans `LapinoApp` — suivre le pattern existant.
- Textes UI en français, avec emojis comme dans l'existant. Garder l'APK < ~1 Mo : pas de nouvelle dépendance lourde sans demander.
- Après un changement de modèle/requête, vérifier les deux côtés (`Models.kt` ↔ mapping `Cursor` dans `LapinoDatabase.kt`).

## Build
- Build release : skill `build-apk` (ou double-clic `BUILD_APK.bat` côté utilisateur — il fait `pause`, ne pas le lancer depuis l'agent).
- Commande directe (Git Bash) : `JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-17.0.17.10-hotspot" ./gradlew assembleRelease --no-daemon`
- Sortie : `app/build/outputs/apk/release/app-release.apk` → copiée en `Lapino.apk` à la racine (git-ignoré).
- Pas de tests unitaires pour l'instant ; la vérification = compilation réussie + test manuel sur appareil (`adb install -r`).

## Économie de tokens — utiliser graft
Le repo est indexé par **graft** (dossier `graft/`, git-ignoré, auto-rafraîchi). Avant de lire un fichier entier (MainActivity.kt fait 1 400 lignes) :
1. Charger les outils MCP en un seul appel : `ToolSearch "select:mcp__graft__graft_find_code,mcp__graft__graft_find_all,mcp__graft__graft_trace_calls,mcp__graft__graft_file_api,mcp__graft__graft_repo_map"`
2. `graft_file_api` pour l'API d'un fichier, `graft_find_code` pour « où / comment », `graft_trace_calls` avant de renommer.
3. Puis `Read` avec `offset`/`limit` sur la plage exacte retournée.

Sans MCP, équivalents CLI : `graft ask "<question>"`, `graft skeleton <fichier>`. Si le graphe manque : `graft build`.
