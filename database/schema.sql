-- Schema de base de données pour Gestion Lapins
-- SQLite

-- Table des Clapets (Cages)
CREATE TABLE IF NOT EXISTS clapets (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  numero TEXT UNIQUE NOT NULL
);

-- Table des Femelles
CREATE TABLE IF NOT EXISTS femelles (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  numero TEXT NOT NULL,
  clapet_id INTEGER,
  date_naissance TEXT,
  statut TEXT DEFAULT 'vivante' CHECK(statut IN ('vivante', 'vendue', 'morte')),
  FOREIGN KEY (clapet_id) REFERENCES clapets(id) ON DELETE SET NULL
);

-- Table des Vaccins
CREATE TABLE IF NOT EXISTS vaccins (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  nom TEXT NOT NULL,
  duree_jours INTEGER NOT NULL
);

-- Table des Vaccinations des Femelles
CREATE TABLE IF NOT EXISTS vaccinations_femelles (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  femelle_id INTEGER NOT NULL,
  vaccin_id INTEGER NOT NULL,
  date_vaccination TEXT NOT NULL,
  date_prochain TEXT NOT NULL,
  FOREIGN KEY (femelle_id) REFERENCES femelles(id) ON DELETE CASCADE,
  FOREIGN KEY (vaccin_id) REFERENCES vaccins(id) ON DELETE CASCADE
);

-- Index pour optimiser les requêtes
CREATE INDEX IF NOT EXISTS idx_femelles_clapet ON femelles(clapet_id);
CREATE INDEX IF NOT EXISTS idx_vaccinations_femelle ON vaccinations_femelles(femelle_id);
CREATE INDEX IF NOT EXISTS idx_vaccinations_date ON vaccinations_femelles(date_prochain);
