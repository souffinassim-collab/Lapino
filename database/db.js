import * as SQLite from 'expo-sqlite';
import { addDays, formatDateISO } from '../utils/dateUtils';

const db = SQLite.openDatabase('lapins.db');

// Initialiser la base de données
export const initDatabase = () => {
    return new Promise((resolve, reject) => {
        db.transaction(tx => {
            // Table Clapets
            tx.executeSql(
                `CREATE TABLE IF NOT EXISTS clapets (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          numero TEXT UNIQUE NOT NULL
        );`,
                [],
                () => console.log('Table clapets créée'),
                (_, error) => {
                    console.error('Erreur création table clapets:', error);
                    return false;
                }
            );

            // Table Femelles
            tx.executeSql(
                `CREATE TABLE IF NOT EXISTS femelles (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          numero TEXT NOT NULL,
          clapet_id INTEGER,
          date_naissance TEXT,
          statut TEXT DEFAULT 'vivante' CHECK(statut IN ('vivante', 'vendue', 'morte')),
          FOREIGN KEY (clapet_id) REFERENCES clapets(id) ON DELETE SET NULL
        );`,
                [],
                () => console.log('Table femelles créée'),
                (_, error) => {
                    console.error('Erreur création table femelles:', error);
                    return false;
                }
            );

            // Table Vaccins
            tx.executeSql(
                `CREATE TABLE IF NOT EXISTS vaccins (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          nom TEXT NOT NULL,
          duree_jours INTEGER NOT NULL
        );`,
                [],
                () => console.log('Table vaccins créée'),
                (_, error) => {
                    console.error('Erreur création table vaccins:', error);
                    return false;
                }
            );

            // Table Vaccinations
            tx.executeSql(
                `CREATE TABLE IF NOT EXISTS vaccinations_femelles (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          femelle_id INTEGER NOT NULL,
          vaccin_id INTEGER NOT NULL,
          date_vaccination TEXT NOT NULL,
          date_prochain TEXT NOT NULL,
          FOREIGN KEY (femelle_id) REFERENCES femelles(id) ON DELETE CASCADE,
          FOREIGN KEY (vaccin_id) REFERENCES vaccins(id) ON DELETE CASCADE
        );`,
                [],
                () => console.log('Table vaccinations_femelles créée'),
                (_, error) => {
                    console.error('Erreur création table vaccinations_femelles:', error);
                    return false;
                }
            );

            // Index
            tx.executeSql(
                'CREATE INDEX IF NOT EXISTS idx_femelles_clapet ON femelles(clapet_id);'
            );
            tx.executeSql(
                'CREATE INDEX IF NOT EXISTS idx_vaccinations_femelle ON vaccinations_femelles(femelle_id);'
            );
            tx.executeSql(
                'CREATE INDEX IF NOT EXISTS idx_vaccinations_date ON vaccinations_femelles(date_prochain);'
            );
        },
            (error) => {
                console.error('Erreur transaction DB:', error);
                reject(error);
            },
            () => {
                console.log('Base de données initialisée avec succès');
                resolve();
            });
    });
};

// ========== CLAPETS ==========

export const getAllClapets = () => {
    return new Promise((resolve, reject) => {
        db.transaction(tx => {
            tx.executeSql(
                `SELECT c.*, f.numero as femelle_numero 
         FROM clapets c 
         LEFT JOIN femelles f ON c.id = f.clapet_id AND f.statut = 'vivante'
         ORDER BY c.numero`,
                [],
                (_, { rows }) => resolve(rows._array),
                (_, error) => reject(error)
            );
        });
    });
};

export const addClapet = (numero) => {
    return new Promise((resolve, reject) => {
        db.transaction(tx => {
            tx.executeSql(
                'INSERT INTO clapets (numero) VALUES (?);',
                [numero],
                (_, result) => resolve(result.insertId),
                (_, error) => reject(error)
            );
        });
    });
};

export const deleteClapet = (id) => {
    return new Promise((resolve, reject) => {
        db.transaction(tx => {
            tx.executeSql(
                'DELETE FROM clapets WHERE id = ?;',
                [id],
                () => resolve(),
                (_, error) => reject(error)
            );
        });
    });
};

// ========== FEMELLES ==========

export const getAllFemelles = () => {
    return new Promise((resolve, reject) => {
        db.transaction(tx => {
            tx.executeSql(
                `SELECT f.*, c.numero as clapet_numero,
         (SELECT date_vaccination FROM vaccinations_femelles vf 
          WHERE vf.femelle_id = f.id 
          ORDER BY vf.date_vaccination DESC LIMIT 1) as dernier_vaccin,
         (SELECT date_prochain FROM vaccinations_femelles vf 
          WHERE vf.femelle_id = f.id 
          ORDER BY vf.date_vaccination DESC LIMIT 1) as prochain_vaccin
         FROM femelles f 
         LEFT JOIN clapets c ON f.clapet_id = c.id
         ORDER BY f.numero`,
                [],
                (_, { rows }) => resolve(rows._array),
                (_, error) => reject(error)
            );
        });
    });
};

export const getFemelleById = (id) => {
    return new Promise((resolve, reject) => {
        db.transaction(tx => {
            tx.executeSql(
                `SELECT f.*, c.numero as clapet_numero 
         FROM femelles f 
         LEFT JOIN clapets c ON f.clapet_id = c.id 
         WHERE f.id = ?`,
                [id],
                (_, { rows }) => resolve(rows._array[0]),
                (_, error) => reject(error)
            );
        });
    });
};

export const addFemelle = (numero, clapatId, dateNaissance, statut = 'vivante') => {
    return new Promise((resolve, reject) => {
        db.transaction(tx => {
            tx.executeSql(
                'INSERT INTO femelles (numero, clapet_id, date_naissance, statut) VALUES (?, ?, ?, ?);',
                [numero, clapatId, dateNaissance, statut],
                (_, result) => resolve(result.insertId),
                (_, error) => reject(error)
            );
        });
    });
};

export const updateFemelle = (id, numero, clapatId, dateNaissance, statut) => {
    return new Promise((resolve, reject) => {
        db.transaction(tx => {
            tx.executeSql(
                'UPDATE femelles SET numero = ?, clapet_id = ?, date_naissance = ?, statut = ? WHERE id = ?;',
                [numero, clapatId, dateNaissance, statut, id],
                () => resolve(),
                (_, error) => reject(error)
            );
        });
    });
};

export const deleteFemelle = (id) => {
    return new Promise((resolve, reject) => {
        db.transaction(tx => {
            tx.executeSql(
                'DELETE FROM femelles WHERE id = ?;',
                [id],
                () => resolve(),
                (_, error) => reject(error)
            );
        });
    });
};

// ========== VACCINS ==========

export const getAllVaccins = () => {
    return new Promise((resolve, reject) => {
        db.transaction(tx => {
            tx.executeSql(
                'SELECT * FROM vaccins ORDER BY nom',
                [],
                (_, { rows }) => resolve(rows._array),
                (_, error) => reject(error)
            );
        });
    });
};

export const addVaccin = (nom, dureeJours) => {
    return new Promise((resolve, reject) => {
        db.transaction(tx => {
            tx.executeSql(
                'INSERT INTO vaccins (nom, duree_jours) VALUES (?, ?);',
                [nom, dureeJours],
                (_, result) => resolve(result.insertId),
                (_, error) => reject(error)
            );
        });
    });
};

export const updateVaccin = (id, nom, dureeJours) => {
    return new Promise((resolve, reject) => {
        db.transaction(tx => {
            tx.executeSql(
                'UPDATE vaccins SET nom = ?, duree_jours = ? WHERE id = ?;',
                [nom, dureeJours, id],
                () => resolve(),
                (_, error) => reject(error)
            );
        });
    });
};

export const deleteVaccin = (id) => {
    return new Promise((resolve, reject) => {
        db.transaction(tx => {
            tx.executeSql(
                'DELETE FROM vaccins WHERE id = ?;',
                [id],
                () => resolve(),
                (_, error) => reject(error)
            );
        });
    });
};

// ========== VACCINATIONS ==========

export const getVaccinationsByFemelle = (femelleId) => {
    return new Promise((resolve, reject) => {
        db.transaction(tx => {
            tx.executeSql(
                `SELECT vf.*, v.nom as vaccin_nom 
         FROM vaccinations_femelles vf 
         JOIN vaccins v ON vf.vaccin_id = v.id 
         WHERE vf.femelle_id = ? 
         ORDER BY vf.date_vaccination DESC`,
                [femelleId],
                (_, { rows }) => resolve(rows._array),
                (_, error) => reject(error)
            );
        });
    });
};

export const addVaccination = (femelleId, vaccinId, dateVaccination) => {
    return new Promise((resolve, reject) => {
        // D'abord récupérer la durée du vaccin
        db.transaction(tx => {
            tx.executeSql(
                'SELECT duree_jours FROM vaccins WHERE id = ?',
                [vaccinId],
                (_, { rows }) => {
                    if (rows.length === 0) {
                        reject(new Error('Vaccin non trouvé'));
                        return;
                    }

                    const dureeJours = rows._array[0].duree_jours;
                    const dateProchain = addDays(dateVaccination, dureeJours);

                    // Insérer la vaccination avec date_prochain calculée
                    tx.executeSql(
                        'INSERT INTO vaccinations_femelles (femelle_id, vaccin_id, date_vaccination, date_prochain) VALUES (?, ?, ?, ?);',
                        [femelleId, vaccinId, dateVaccination, formatDateISO(dateProchain)],
                        (_, result) => resolve(result.insertId),
                        (_, error) => reject(error)
                    );
                },
                (_, error) => reject(error)
            );
        });
    });
};

export const deleteVaccination = (id) => {
    return new Promise((resolve, reject) => {
        db.transaction(tx => {
            tx.executeSql(
                'DELETE FROM vaccinations_femelles WHERE id = ?;',
                [id],
                () => resolve(),
                (_, error) => reject(error)
            );
        });
    });
};

// ========== STATISTIQUES & ALERTES ==========

export const getStatistics = () => {
    return new Promise((resolve, reject) => {
        db.transaction(tx => {
            const stats = {};

            // Total femelles
            tx.executeSql(
                "SELECT COUNT(*) as total FROM femelles WHERE statut = 'vivante'",
                [],
                (_, { rows }) => {
                    stats.totalFemelles = rows._array[0].total;
                }
            );

            // Clapets remplis/vides
            tx.executeSql(
                `SELECT 
          COUNT(*) as total,
          SUM(CASE WHEN f.id IS NOT NULL THEN 1 ELSE 0 END) as remplis
         FROM clapets c 
         LEFT JOIN femelles f ON c.id = f.clapet_id AND f.statut = 'vivante'`,
                [],
                (_, { rows }) => {
                    const data = rows._array[0];
                    stats.clapetsRemplis = data.remplis || 0;
                    stats.clapetsVides = data.total - (data.remplis || 0);
                    resolve(stats);
                },
                (_, error) => reject(error)
            );
        });
    });
};

export const getVaccinsEnRetard = () => {
    return new Promise((resolve, reject) => {
        const today = formatDateISO(new Date());
        db.transaction(tx => {
            tx.executeSql(
                `SELECT vf.*, f.numero as femelle_numero, v.nom as vaccin_nom
         FROM vaccinations_femelles vf
         JOIN femelles f ON vf.femelle_id = f.id
         JOIN vaccins v ON vf.vaccin_id = v.id
         WHERE vf.date_prochain < ? AND f.statut = 'vivante'
         ORDER BY vf.date_prochain`,
                [today],
                (_, { rows }) => resolve(rows._array),
                (_, error) => reject(error)
            );
        });
    });
};

export const getVaccinsBientot = () => {
    return new Promise((resolve, reject) => {
        const today = new Date();
        const in7Days = addDays(today, 7);

        db.transaction(tx => {
            tx.executeSql(
                `SELECT vf.*, f.numero as femelle_numero, v.nom as vaccin_nom
         FROM vaccinations_femelles vf
         JOIN femelles f ON vf.femelle_id = f.id
         JOIN vaccins v ON vf.vaccin_id = v.id
         WHERE vf.date_prochain >= ? AND vf.date_prochain <= ? AND f.statut = 'vivante'
         ORDER BY vf.date_prochain`,
                [formatDateISO(today), formatDateISO(in7Days)],
                (_, { rows }) => resolve(rows._array),
                (_, error) => reject(error)
            );
        });
    });
};

export default db;
