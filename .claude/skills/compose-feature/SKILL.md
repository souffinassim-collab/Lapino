---
name: compose-feature
description: Ajouter une fonctionnalité de bout en bout dans Lapino (donnée SQLite → requête → écran/dialog Jetpack Compose) en respectant les patterns existants et en économisant les tokens via graft. Utiliser pour toute nouvelle fonctionnalité, nouvel onglet, nouvelle carte, nouveau formulaire ou dialog.
---

# Ajouter une fonctionnalité dans Lapino

## 0. Repérage économe (graft)
- `graft_repo_map` une fois si besoin d'orientation ; `graft_file_api` sur `MainActivity.kt` (1 400 lignes) au lieu de le lire en entier.
- `graft_find_code "<onglet ou composant proche>"` → lire uniquement la plage `file:line` retournée avec `Read offset/limit`.
- Consulter le skill `cuniculture` si la fonctionnalité touche une règle métier.

## 1. Données
- Nouveau stockage → skill `db-migration`.
- Nouvelle requête dans `LapinoDatabase` : fonction publique qui retourne des data classes de `Models.kt` (jamais un `Cursor`). Fermer les curseurs (`use { }`).

## 2. UI (patterns existants)
- L'état vit dans `LapinoApp` (`remember { mutableStateOf(...) }`) ; les onglets reçoivent données + callbacks. Après une écriture DB, déclencher le rafraîchissement comme le font les callbacks existants (chercher un exemple avec `graft_trace_calls` sur `confirmBirth` ou `addFemelle`).
- Réutiliser avant de créer : `SummaryStatCard`, `MiniKpiBox`, `StatusPill`, `InfoRow` (MainActivity.kt), `RabbitStatusCard`, `CuniTipBox` (ui/Components.kt), couleurs de `ui/Theme.kt` / `MaterialTheme.colorScheme`.
- Formulaires : `AlertDialog` / `OutlinedTextField` comme `AddEditFemelleDialog` ; valider les nombres (`toIntOrNull`, valeurs ≥ 0) et les dates via `DateHelper`.
- Nouvel onglet : l'ajouter à la navigation de `LapinoApp` avec une icône `Icons.*` (material-icons-extended déjà inclus).
- Si un fichier dépasse ~1 500 lignes, placer le nouvel écran dans `ui/<NomEcran>.kt` plutôt que dans `MainActivity.kt`.

## 3. Textes & UX terrain
- Français, phrases courtes, emojis cohérents avec l'existant (🩺 📦 🐣 💉 🌾).
- Pensé pour un usage à la cage : gros boutons, peu de saisie, valeurs par défaut = aujourd'hui.
- Fonctionne 100 % hors-ligne ; pas de nouvelle dépendance sans accord (taille APK ~1 Mo).

## 4. Vérification
Compiler avec le skill `build-apk` (`compileReleaseKotlin` suffit en itération), puis build complet en fin de tâche. Si R8 casse quelque chose (réflexion), ajuster `app/proguard-rules.pro`.
