---
name: db-migration
description: Procédure sûre pour modifier le schéma SQLite de Lapino (nouvelle colonne, nouvelle table, index) sans perdre les données des éleveurs. Utiliser dès qu'une fonctionnalité demande de stocker une nouvelle information (poids, mâles, ventes, santé, etc.).
---

# Migration du schéma SQLite (LapinoDatabase.kt)

Les éleveurs ont des données réelles sur leur téléphone : une migration ratée = données perdues. Pas de Room, tout est manuel.

## Checklist (dans cet ordre)
1. **Version** : incrémenter le 4e argument de `SQLiteOpenHelper(context, "lapino.db", null, N)` dans `class LapinoDatabase`.
2. **createTables** : mettre à jour le `CREATE TABLE IF NOT EXISTS` (installations neuves). Une nouvelle table ici suffit aussi pour les mises à jour, car `onUpgrade` appelle `createTables(db)`.
3. **onUpgrade** : pour une nouvelle colonne sur une table existante, ajouter `"ALTER TABLE t ADD COLUMN c TYPE [DEFAULT x];"` à la liste `migrations` (chaque requête est dans un try/catch → idempotent). Toujours `NULL`-able ou avec `DEFAULT`.
   - Ne jamais `DROP` ni renommer une colonne. Pour une transformation de données, l'écrire conditionnée par `oldVersion < N`.
4. **Model** : ajouter le champ dans la data class de `model/Models.kt` (nullable si la colonne peut être NULL).
5. **Mapping** : mettre à jour TOUTES les lectures `Cursor` qui construisent ce modèle (chercher avec `graft_find_all` ou Grep le nom de la data class, ex. `CycleReproduction(`). Utiliser `getColumnIndexOrThrow` + les helpers `getStringOrNull`/`getIntOrNull`.
6. **Écriture** : `ContentValues` dans les fonctions add/update concernées.
7. **Seed** : si pertinent, compléter `seedInitialDataIfEmpty`.
8. Compiler (skill `build-apk`, `compileReleaseKotlin`).

## Conventions
- Noms SQL en `snake_case` français (`date_mise_bas_prevue`), Kotlin en `camelCase` (`dateMiseBasPrevue`).
- Dates : TEXT ISO `yyyy-MM-dd` (via `DateHelper.todayIso()` / `addDays`). Booléens : INTEGER 0/1.
- Clés étrangères activées (`onConfigure`) : choisir `ON DELETE CASCADE` ou `SET NULL` explicitement.
- Requêtes paramétrées (`arrayOf(id.toString())`), jamais de concaténation de valeurs saisies.

## Test manuel de migration
Installer l'ancienne APK, saisir des données, puis `adb install -r` la nouvelle : les données doivent rester et l'app ne doit pas crasher.
