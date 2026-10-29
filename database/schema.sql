-- Schéma de base de données pour Gestion Lapins (Lapino)
-- SQLite

-- Table des Clapets (Cages)
CREATE TABLE IF NOT EXISTS clapets (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  numero TEXT UNIQUE NOT NULL
);

-- Table des Femelles (Lapines reproductrices)
CREATE TABLE IF NOT EXISTS femelles (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  numero TEXT NOT NULL,
  clapet_id INTEGER,
  date_naissance TEXT,
  statut TEXT DEFAULT 'vivante' CHECK(statut IN ('vivante', 'vendue', 'morte')),
  FOREIGN KEY (clapet_id) REFERENCES clapets(id) ON DELETE SET NULL
);

-- Table des Mâles (Reproducteurs)
CREATE TABLE IF NOT EXISTS males (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  numero TEXT UNIQUE NOT NULL,
  clapet_id INTEGER,
  date_naissance TEXT,
  statut TEXT DEFAULT 'vivant' CHECK(statut IN ('vivant', 'vendu', 'mort')),
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

-- Table des Aliments
CREATE TABLE IF NOT EXISTS aliments (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  nom TEXT NOT NULL,
  stock_kg REAL DEFAULT 0,
  consommation_g REAL DEFAULT 0
);

-- Table des Cycles de Reproduction (Cuniculiculture)
CREATE TABLE IF NOT EXISTS cycles_reproduction (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  femelle_id INTEGER NOT NULL,
  male_numero TEXT,
  date_saillie TEXT NOT NULL,
  date_palpation_prevue TEXT,
  date_palpation_reelle TEXT,
  resultat_palpation TEXT CHECK(resultat_palpation IN ('positive', 'negative', 'douteuse', NULL)),
  date_boite_nid_prevue TEXT,
  boite_nid_posee INTEGER DEFAULT 0,
  date_mise_bas_prevue TEXT NOT NULL,
  date_mise_bas_reelle TEXT,
  nombre_vivants INTEGER,
  nombre_morts INTEGER,
  date_sevrage_prevue TEXT,
  date_sevrage_reelle TEXT,
  nombre_sevres INTEGER,
  statut TEXT NOT NULL CHECK(statut IN ('saillie', 'gestante', 'allaitante', 'termine', 'echec')),
  motif_echec TEXT,
  notes TEXT,
  FOREIGN KEY (femelle_id) REFERENCES femelles(id) ON DELETE CASCADE
);

-- Table des Contrôles Quotidiens
CREATE TABLE IF NOT EXISTS daily_checks (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  date TEXT UNIQUE NOT NULL,
  statut TEXT NOT NULL
);

-- Table des Paramètres
CREATE TABLE IF NOT EXISTS settings (
  key TEXT PRIMARY KEY,
  value TEXT
);

-- Index pour optimiser les requêtes
CREATE INDEX IF NOT EXISTS idx_femelles_clapet ON femelles(clapet_id);
CREATE INDEX IF NOT EXISTS idx_vaccinations_femelle ON vaccinations_femelles(femelle_id);
CREATE INDEX IF NOT EXISTS idx_vaccinations_date ON vaccinations_femelles(date_prochain);
CREATE INDEX IF NOT EXISTS idx_cycles_femelle ON cycles_reproduction(femelle_id);
CREATE INDEX IF NOT EXISTS idx_cycles_statut ON cycles_reproduction(statut);
