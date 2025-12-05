import { Platform } from 'react-native';
import * as SQLite from 'expo-sqlite';
import { addDays, formatDateISO } from '../utils/dateUtils';

// Variable pour stocker la DB native ou le mock web
let db = null;
let mockDb = {
    clapets: [
        { id: 1, numero: 'A1' },
        { id: 2, numero: 'A2' },
        { id: 3, numero: 'B1' },
    ],
    femelles: [],
    vaccins: [],
    vaccinations: [],
    aliments: [],
    cycles: [],
    lastId: {
        clapets: 3,
        femelles: 0,
        vaccins: 0,
        vaccinations: 0,
        aliments: 0,
        cycles: 0
    }
};

// === WEB MOCK HELPERS ===
const mockDelay = () => new Promise(resolve => setTimeout(resolve, 100));

// === DATABASE HELPER ===
const executeSqlAsync = (sql, params = []) => {
    return new Promise((resolve, reject) => {
        db.transaction(tx => {
            tx.executeSql(
                sql,
                params,
                (_, { rows, insertId, rowsAffected }) => {
                    resolve({ rows: rows._array, insertId, rowsAffected });
                },
                (_, error) => {
                    console.error(`Error executing SQL: ${sql}`, error);
                    reject(error);
                    return false; // Stop transaction
                }
            );
        }, (error) => {
            console.error("Transaction error:", error);
            reject(error);
        });
    });
};

// Initialiser la base de données
export const initDatabase = async () => {
    if (Platform.OS === 'web') {
        console.log('Web detected: Using in-memory mock database');
        return Promise.resolve();
    }

    // Ouvrir la base de données SQLite
    db = await SQLite.openDatabaseAsync('lapino.db');

    const schema = [
        `CREATE TABLE IF NOT EXISTS clapets (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        numero TEXT UNIQUE NOT NULL
      );`,
        `CREATE TABLE IF NOT EXISTS femelles (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        numero TEXT NOT NULL,
        clapet_id INTEGER,
        date_naissance TEXT,
        statut TEXT DEFAULT 'vivante' CHECK(statut IN ('vivante', 'vendue', 'morte')),
        FOREIGN KEY (clapet_id) REFERENCES clapets(id) ON DELETE SET NULL
      );`,
        `CREATE TABLE IF NOT EXISTS vaccins (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        nom TEXT NOT NULL,
        duree_jours INTEGER NOT NULL
      );`,
        `CREATE TABLE IF NOT EXISTS vaccinations_femelles (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        femelle_id INTEGER NOT NULL,
        vaccin_id INTEGER NOT NULL,
        date_vaccination TEXT NOT NULL,
        date_prochain TEXT NOT NULL,
        FOREIGN KEY (femelle_id) REFERENCES femelles(id) ON DELETE CASCADE,
        FOREIGN KEY (vaccin_id) REFERENCES vaccins(id) ON DELETE CASCADE
      );`,
        'CREATE INDEX IF NOT EXISTS idx_femelles_clapet ON femelles(clapet_id);',
        'CREATE INDEX IF NOT EXISTS idx_vaccinations_femelle ON vaccinations_femelles(femelle_id);',
        'CREATE INDEX IF NOT EXISTS idx_vaccinations_date ON vaccinations_femelles(date_prochain);',
        // Table Aliments
        `CREATE TABLE IF NOT EXISTS aliments (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            nom TEXT NOT NULL,
            stock_kg REAL DEFAULT 0,
            consommation_g REAL DEFAULT 0
        );`,
        // Table Cycles Reproduction
        `CREATE TABLE IF NOT EXISTS cycles_reproduction (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            femelle_id INTEGER NOT NULL,
            date_saillie TEXT NOT NULL,
            date_mise_bas_prevue TEXT NOT NULL,
            date_mise_bas_reelle TEXT,
            nombre_vivants INTEGER,
            nombre_morts INTEGER,
            date_sevrage_prevue TEXT,
            statut TEXT NOT NULL,
            FOREIGN KEY (femelle_id) REFERENCES femelles(id) ON DELETE CASCADE
        );`
    ];

    return new Promise((resolve, reject) => {
        db.transaction(tx => {
            schema.forEach(query => {
                tx.executeSql(
                    query,
                    [],
                    () => { },
                    (_, error) => {
                        console.error('Error init schema:', error);
                        return false;
                    }
                );
            });
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

export const getAllClapets = async () => {
    if (Platform.OS === 'web') {
        await mockDelay();
        return mockDb.clapets.map(c => {
            const femelle = mockDb.femelles.find(f => f.clapet_id === c.id && f.statut === 'vivante');
            return { ...c, femelle_numero: femelle ? femelle.numero : null };
        }).sort((a, b) => a.numero.localeCompare(b.numero));
    }

    const { rows } = await executeSqlAsync(
        `SELECT c.*, f.numero as femelle_numero 
         FROM clapets c 
         LEFT JOIN femelles f ON c.id = f.clapet_id AND f.statut = 'vivante'
         ORDER BY c.numero`
    );
    return rows;
};

export const addClapet = async (numero) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        if (mockDb.clapets.some(c => c.numero === numero)) return Promise.reject("Unique constraint mock");
        mockDb.lastId.clapets++;
        const id = mockDb.lastId.clapets;
        mockDb.clapets.push({ id, numero });
        return id;
    }

    const { insertId } = await executeSqlAsync(
        'INSERT INTO clapets (numero) VALUES (?);',
        [numero]
    );
    return insertId;
};

export const deleteClapet = async (id) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        mockDb.clapets = mockDb.clapets.filter(c => c.id !== id);
        // Set clapet_id to null for females in this clapet
        mockDb.femelles = mockDb.femelles.map(f => f.clapet_id === id ? { ...f, clapet_id: null } : f);
        return;
    }

    await executeSqlAsync('DELETE FROM clapets WHERE id = ?;', [id]);
};

// ========== FEMELLES ==========

export const getAllFemelles = async () => {
    if (Platform.OS === 'web') {
        await mockDelay();
        return mockDb.femelles.map(f => {
            const clapet = mockDb.clapets.find(c => c.id === f.clapet_id);

            const vaccs = mockDb.vaccinations.filter(v => v.femelle_id === f.id)
                .sort((a, b) => new Date(b.date_vaccination) - new Date(a.date_vaccination));

            const lastVacc = vaccs[0];

            return {
                ...f,
                clapet_numero: clapet ? clapet.numero : null,
                dernier_vaccin: lastVacc ? lastVacc.date_vaccination : null,
                prochain_vaccin: lastVacc ? lastVacc.date_prochain : null
            };
        }).sort((a, b) => a.numero.localeCompare(b.numero));
    }

    const { rows } = await executeSqlAsync(
        `SELECT f.*, c.numero as clapet_numero,
         (SELECT date_vaccination FROM vaccinations_femelles vf 
          WHERE vf.femelle_id = f.id 
          ORDER BY vf.date_vaccination DESC LIMIT 1) as dernier_vaccin,
         (SELECT date_prochain FROM vaccinations_femelles vf 
          WHERE vf.femelle_id = f.id 
          ORDER BY vf.date_vaccination DESC LIMIT 1) as prochain_vaccin
         FROM femelles f 
         LEFT JOIN clapets c ON f.clapet_id = c.id
         ORDER BY f.numero`
    );
    return rows;
};

export const getFemelleById = async (id) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        const f = mockDb.femelles.find(fem => fem.id === id);
        if (!f) return Promise.reject("Not found");
        const clapet = mockDb.clapets.find(c => c.id === f.clapet_id);
        return { ...f, clapet_numero: clapet ? clapet.numero : null };
    }

    const { rows } = await executeSqlAsync(
        `SELECT f.*, c.numero as clapet_numero 
         FROM femelles f 
         LEFT JOIN clapets c ON f.clapet_id = c.id 
         WHERE f.id = ?`,
        [id]
    );
    return rows[0];
};

export const addFemelle = async (numero, clapatId, dateNaissance, statut = 'vivante') => {
    if (Platform.OS === 'web') {
        await mockDelay();
        mockDb.lastId.femelles++;
        const id = mockDb.lastId.femelles;
        mockDb.femelles.push({ id, numero, clapet_id: clapatId, date_naissance: dateNaissance, statut });
        return id;
    }

    const { insertId } = await executeSqlAsync(
        'INSERT INTO femelles (numero, clapet_id, date_naissance, statut) VALUES (?, ?, ?, ?);',
        [numero, clapatId, dateNaissance, statut]
    );
    return insertId;
};

export const updateFemelle = async (id, numero, clapatId, dateNaissance, statut) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        const index = mockDb.femelles.findIndex(f => f.id === id);
        if (index !== -1) {
            mockDb.femelles[index] = { ...mockDb.femelles[index], numero, clapet_id: clapatId, date_naissance: dateNaissance, statut };
        }
        return;
    }

    await executeSqlAsync(
        'UPDATE femelles SET numero = ?, clapet_id = ?, date_naissance = ?, statut = ? WHERE id = ?;',
        [numero, clapatId, dateNaissance, statut, id]
    );
};

export const deleteFemelle = async (id) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        mockDb.femelles = mockDb.femelles.filter(f => f.id !== id);
        mockDb.vaccinations = mockDb.vaccinations.filter(v => v.femelle_id !== id);
        return;
    }

    await executeSqlAsync('DELETE FROM femelles WHERE id = ?;', [id]);
};

// ========== VACCINS ==========

export const getAllVaccins = async () => {
    if (Platform.OS === 'web') {
        await mockDelay();
        return [...mockDb.vaccins].sort((a, b) => a.nom.localeCompare(b.nom));
    }

    const { rows } = await executeSqlAsync('SELECT * FROM vaccins ORDER BY nom');
    return rows;
};

export const addVaccin = async (nom, dureeJours) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        mockDb.lastId.vaccins++;
        const id = mockDb.lastId.vaccins;
        mockDb.vaccins.push({ id, nom, duree_jours: dureeJours });
        return id;
    }

    const { insertId } = await executeSqlAsync(
        'INSERT INTO vaccins (nom, duree_jours) VALUES (?, ?);',
        [nom, dureeJours]
    );
    return insertId;
};

export const updateVaccin = async (id, nom, dureeJours) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        const index = mockDb.vaccins.findIndex(v => v.id === id);
        if (index !== -1) {
            mockDb.vaccins[index] = { ...mockDb.vaccins[index], nom, duree_jours: dureeJours };
        }
        return;
    }

    await executeSqlAsync(
        'UPDATE vaccins SET nom = ?, duree_jours = ? WHERE id = ?;',
        [nom, dureeJours, id]
    );
};

export const deleteVaccin = async (id) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        mockDb.vaccins = mockDb.vaccins.filter(v => v.id !== id);
        mockDb.vaccinations = mockDb.vaccinations.filter(v => v.vaccin_id !== id);
        return;
    }

    await executeSqlAsync('DELETE FROM vaccins WHERE id = ?;', [id]);
};

// ========== VACCINATIONS ==========

export const getVaccinationsByFemelle = async (femelleId) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        return mockDb.vaccinations
            .filter(v => v.femelle_id === femelleId)
            .map(v => {
                const vaccin = mockDb.vaccins.find(vac => vac.id === v.vaccin_id);
                return { ...v, vaccin_nom: vaccin ? vaccin.nom : 'Inconnu' };
            })
            .sort((a, b) => new Date(b.date_vaccination) - new Date(a.date_vaccination));
    }

    const { rows } = await executeSqlAsync(
        `SELECT vf.*, v.nom as vaccin_nom 
         FROM vaccinations_femelles vf 
         JOIN vaccins v ON vf.vaccin_id = v.id 
         WHERE vf.femelle_id = ? 
         ORDER BY vf.date_vaccination DESC`,
        [femelleId]
    );
    return rows;
};

export const addVaccination = async (femelleId, vaccinId, dateVaccination) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        const vaccin = mockDb.vaccins.find(v => v.id === vaccinId);
        if (!vaccin) return Promise.reject("Vaccin not found");

        const dateProchain = addDays(dateVaccination, vaccin.duree_jours);

        mockDb.lastId.vaccinations++;
        const id = mockDb.lastId.vaccinations;

        mockDb.vaccinations.push({
            id,
            femelle_id: femelleId,
            vaccin_id: vaccinId,
            date_vaccination: dateVaccination,
            date_prochain: formatDateISO(dateProchain)
        });
        return id;
    }

    // D'abord récupérer la durée du vaccin
    const { rows } = await executeSqlAsync('SELECT duree_jours FROM vaccins WHERE id = ?', [vaccinId]);
    if (rows.length === 0) {
        throw new Error('Vaccin non trouvé');
    }
    const dureeJours = rows[0].duree_jours;
    const dateProchain = addDays(dateVaccination, dureeJours);

    const { insertId } = await executeSqlAsync(
        'INSERT INTO vaccinations_femelles (femelle_id, vaccin_id, date_vaccination, date_prochain) VALUES (?, ?, ?, ?);',
        [femelleId, vaccinId, dateVaccination, formatDateISO(dateProchain)]
    );
    return insertId;
};

export const deleteVaccination = async (id) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        mockDb.vaccinations = mockDb.vaccinations.filter(v => v.id !== id);
        return;
    }

    await executeSqlAsync('DELETE FROM vaccinations_femelles WHERE id = ?;', [id]);
};

// ========== STATISTIQUES & ALERTES ==========

export const getStatistics = async () => {
    if (Platform.OS === 'web') {
        await mockDelay();
        const vivantes = mockDb.femelles.filter(f => f.statut === 'vivante');
        const remplis = mockDb.clapets.filter(c =>
            mockDb.femelles.some(f => f.clapet_id === c.id && f.statut === 'vivante')
        ).length;

        return {
            totalFemelles: vivantes.length,
            clapetsRemplis: remplis,
            clapetsVides: mockDb.clapets.length - remplis
        };
    }

    const stats = {};

    // Total femelles
    const { rows: rowsFemelles } = await executeSqlAsync("SELECT COUNT(*) as total FROM femelles WHERE statut = 'vivante'");
    stats.totalFemelles = rowsFemelles[0].total;

    // Clapets remplis/vides
    const { rows: rowsClapets } = await executeSqlAsync(
        `SELECT 
          COUNT(*) as total,
          SUM(CASE WHEN f.id IS NOT NULL THEN 1 ELSE 0 END) as remplis
         FROM clapets c 
         LEFT JOIN femelles f ON c.id = f.clapet_id AND f.statut = 'vivante'`
    );
    const data = rowsClapets[0];
    stats.clapetsRemplis = data.remplis || 0;
    stats.clapetsVides = data.total - (data.remplis || 0);

    return stats;
};

export const getVaccinsEnRetard = async () => {
    if (Platform.OS === 'web') {
        await mockDelay();
        const today = new Date().toISOString().split('T')[0];

        return mockDb.vaccinations
            .filter(v => {
                const femelle = mockDb.femelles.find(f => f.id === v.femelle_id);
                const isLatest = !mockDb.vaccinations.some(v2 =>
                    v2.femelle_id === v.femelle_id &&
                    v2.vaccin_id === v.vaccin_id &&
                    v2.id > v.id // Simplification: higher ID is later
                );
                return femelle && femelle.statut === 'vivante' && v.date_prochain < today;
            })
            .map(v => {
                const femelle = mockDb.femelles.find(f => f.id === v.femelle_id);
                const vaccin = mockDb.vaccins.find(vac => vac.id === v.vaccin_id);
                return {
                    ...v,
                    femelle_numero: femelle ? femelle.numero : '?',
                    vaccin_nom: vaccin ? vaccin.nom : '?'
                };
            })
            .sort((a, b) => a.date_prochain.localeCompare(b.date_prochain));
    }

    const today = formatDateISO(new Date());
    const { rows } = await executeSqlAsync(
        `SELECT vf.*, f.numero as femelle_numero, v.nom as vaccin_nom
         FROM vaccinations_femelles vf
         JOIN femelles f ON vf.femelle_id = f.id
         JOIN vaccins v ON vf.vaccin_id = v.id
         WHERE vf.date_prochain < ? AND f.statut = 'vivante'
         ORDER BY vf.date_prochain`,
        [today]
    );
    return rows;
};

export const getVaccinsBientot = async () => {
    if (Platform.OS === 'web') {
        await mockDelay();
        const today = new Date();
        const todayStr = formatDateISO(today);
        const in7Days = addDays(today, 7);
        const in7DaysStr = formatDateISO(in7Days);

        return mockDb.vaccinations
            .filter(v => {
                const femelle = mockDb.femelles.find(f => f.id === v.femelle_id);
                return femelle && femelle.statut === 'vivante' &&
                    v.date_prochain >= todayStr && v.date_prochain <= in7DaysStr;
            })
            .map(v => {
                const femelle = mockDb.femelles.find(f => f.id === v.femelle_id);
                const vaccin = mockDb.vaccins.find(vac => vac.id === v.vaccin_id);
                return {
                    ...v,
                    femelle_numero: femelle ? femelle.numero : '?',
                    vaccin_nom: vaccin ? vaccin.nom : '?'
                };
            })
            .sort((a, b) => a.date_prochain.localeCompare(b.date_prochain));
    }

    const today = new Date();
    const in7Days = addDays(today, 7);

    const { rows } = await executeSqlAsync(
        `SELECT vf.*, f.numero as femelle_numero, v.nom as vaccin_nom
         FROM vaccinations_femelles vf
         JOIN femelles f ON vf.femelle_id = f.id
         JOIN vaccins v ON vf.vaccin_id = v.id
         WHERE vf.date_prochain >= ? AND vf.date_prochain <= ? AND f.statut = 'vivante'
         ORDER BY vf.date_prochain`,
        [formatDateISO(today), formatDateISO(in7Days)]
    );
    return rows;
};


// ========== ALIMENTS ==========

export const getAllAliments = async () => {
    if (Platform.OS === 'web') {
        await mockDelay();
        // Initialiser aliments si manquant (hotfix pour mock)
        if (!mockDb.aliments) {
            mockDb.aliments = [];
            mockDb.lastId.aliments = 0;
        }

        // Calculer infos supplémentaires pour chaque aliment
        const totalFemelles = mockDb.femelles.filter(f => f.statut === 'vivante').length;

        return (mockDb.aliments || []).map(a => {
            // Conso totale par jour = (nb lapins * conso unitaire) / 1000 (pour avoir en kg)
            const consoJourKg = (totalFemelles * a.consommation_g) / 1000;
            const joursRestants = consoJourKg > 0 ? Math.floor(a.stock_kg / consoJourKg) : 999;

            return {
                ...a,
                conso_jour_kg: consoJourKg.toFixed(2),
                jours_restants: joursRestants
            };
        });
    }

    // D'abord compter les femelles vivantes
    const { rows: femellesRows } = await executeSqlAsync("SELECT COUNT(*) as total FROM femelles WHERE statut = 'vivante'");
    const totalFemelles = femellesRows[0].total;

    // Puis récupérer les aliments
    const { rows: alimentsRows } = await executeSqlAsync('SELECT * FROM aliments ORDER BY nom');
    return alimentsRows.map(a => {
        const consoJourKg = (totalFemelles * a.consommation_g) / 1000;
        const joursRestants = consoJourKg > 0 ? Math.floor(a.stock_kg / consoJourKg) : 999;
        return {
            ...a,
            conso_jour_kg: consoJourKg.toFixed(2),
            jours_restants: joursRestants
        };
    });
};

export const addAliment = async (nom, stockKg, consommationG) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        if (!mockDb.aliments) {
            mockDb.aliments = [];
            mockDb.lastId.aliments = 0;
        }

        mockDb.lastId.aliments++;
        const id = mockDb.lastId.aliments;
        mockDb.aliments.push({ id, nom, stock_kg: stockKg, consommation_g: consommationG });
        return id;
    }

    const { insertId } = await executeSqlAsync(
        'INSERT INTO aliments (nom, stock_kg, consommation_g) VALUES (?, ?, ?);',
        [nom, stockKg, consommationG]
    );
    return insertId;
};

export const updateAliment = async (id, nom, stockKg, consommationG) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        if (!mockDb.aliments) return;
        const index = mockDb.aliments.findIndex(a => a.id === id);
        if (index !== -1) {
            mockDb.aliments[index] = { ...mockDb.aliments[index], nom, stock_kg: stockKg, consommation_g: consommationG };
        }
        return;
    }

    await executeSqlAsync(
        'UPDATE aliments SET nom = ?, stock_kg = ?, consommation_g = ? WHERE id = ?;',
        [nom, stockKg, consommationG, id]
    );
};

export const deleteAliment = async (id) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        if (mockDb.aliments) {
            mockDb.aliments = mockDb.aliments.filter(a => a.id !== id);
        }
        return;
    }

    await executeSqlAsync('DELETE FROM aliments WHERE id = ?;', [id]);
};


// ========== REPRODUCTION ==========

export const getFemellesWithStatus = async () => {
    if (Platform.OS === 'web') {
        await mockDelay();
        if (!mockDb.cycles) mockDb.cycles = [];

        return mockDb.femelles.map(f => {
            const clapet = mockDb.clapets.find(c => c.id === f.clapet_id);
            // Get current active cycle
            const activeCycle = mockDb.cycles.find(c =>
                c.femelle_id === f.id &&
                ['saillie', 'gestante', 'allaitante'].includes(c.statut)
            );

            return {
                ...f,
                clapet_numero: clapet ? clapet.numero : null,
                cycle: activeCycle || null
            };
        }).sort((a, b) => a.numero.localeCompare(b.numero));
    }

    const { rows } = await executeSqlAsync(
        `SELECT f.*, c.numero as clapet_numero,
         (SELECT id FROM cycles_reproduction cr 
          WHERE cr.femelle_id = f.id 
          AND cr.statut IN ('saillie', 'gestante', 'allaitante')
          ORDER BY cr.date_saillie DESC LIMIT 1) as cycle_id
         FROM femelles f 
         LEFT JOIN clapets c ON f.clapet_id = c.id
         ORDER BY f.numero`
    );
    // Note: In a real app we might want to fetch full cycle details here if needed,
    // but the UI typically just needs to know if there is an active cycle.
    return rows;
};

export const addCycle = async (femelleId, dateSaillie) => {
    const dateMiseBasPrevue = formatDateISO(addDays(dateSaillie, 31)); // 31 jours de gestation

    if (Platform.OS === 'web') {
        await mockDelay();
        if (!mockDb.cycles) {
            mockDb.cycles = [];
            mockDb.lastId.cycles = 0;
        }

        mockDb.lastId.cycles++;
        mockDb.cycles.push({
            id: mockDb.lastId.cycles,
            femelle_id: femelleId,
            date_saillie: dateSaillie,
            date_mise_bas_prevue: dateMiseBasPrevue,
            statut: 'saillie'
        });
        return mockDb.lastId.cycles;
    }

    const { insertId } = await executeSqlAsync(
        `INSERT INTO cycles_reproduction 
         (femelle_id, date_saillie, date_mise_bas_prevue, statut) 
         VALUES (?, ?, ?, 'saillie');`,
        [femelleId, dateSaillie, dateMiseBasPrevue]
    );
    return insertId;
};

export const confirmBirth = async (cycleId, dateNaissance, vivants, morts) => {
    const dateSevrage = formatDateISO(addDays(dateNaissance, 35)); // 35 jours de sevrage

    if (Platform.OS === 'web') {
        await mockDelay();
        const index = mockDb.cycles.findIndex(c => c.id === cycleId);
        if (index !== -1) {
            mockDb.cycles[index] = {
                ...mockDb.cycles[index],
                date_mise_bas_reelle: dateNaissance,
                nombre_vivants: vivants,
                nombre_morts: morts,
                date_sevrage_prevue: dateSevrage,
                statut: 'allaitante'
            };
        }
        return;
    }

    await executeSqlAsync(
        `UPDATE cycles_reproduction 
         SET date_mise_bas_reelle = ?, nombre_vivants = ?, nombre_morts = ?, 
             date_sevrage_prevue = ?, statut = 'allaitante'
         WHERE id = ?;`,
        [dateNaissance, vivants, morts, dateSevrage, cycleId]
    );
};

export const stopCycle = async (cycleId, reason = 'termine') => {
    // reason: 'termine' (sevré) or 'echec' (fausse couche/mort)
    if (Platform.OS === 'web') {
        await mockDelay();
        const index = mockDb.cycles.findIndex(c => c.id === cycleId);
        if (index !== -1) {
            mockDb.cycles[index].statut = reason;
        }
        return;
    }

    await executeSqlAsync(
        'UPDATE cycles_reproduction SET statut = ? WHERE id = ?;',
        [reason, cycleId]
    );
};

export default db;
