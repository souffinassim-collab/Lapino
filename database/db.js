import { Platform } from 'react-native';
import * as SQLite from 'expo-sqlite';
import { addDays, formatDateISO, daysUntil } from '../utils/dateUtils';

// Variable pour stocker la DB native ou le mock web
let db = null;

// Données démo par défaut pour le Web si le localStorage est vide
const getInitialMockData = () => {
    const today = new Date();
    const dMinus11 = formatDateISO(addDays(today, -11));
    const dMinus28 = formatDateISO(addDays(today, -28));
    const dMinus18 = formatDateISO(addDays(today, -18));

    return {
        clapets: [
            { id: 1, numero: 'A1' },
            { id: 2, numero: 'A2' },
            { id: 3, numero: 'B1' },
            { id: 4, numero: 'B2' },
        ],
        femelles: [
            { id: 1, numero: 'F01 - Blanchette', clapet_id: 1, date_naissance: '2025-03-15', statut: 'vivante' },
            { id: 2, numero: 'F02 - Fauve', clapet_id: 2, date_naissance: '2025-04-10', statut: 'vivante' },
            { id: 3, numero: 'F03 - Noisette', clapet_id: 3, date_naissance: '2025-05-01', statut: 'vivante' },
            { id: 4, numero: 'F04 - Papillon', clapet_id: 4, date_naissance: '2025-06-20', statut: 'vivante' },
        ],
        vaccins: [
            { id: 1, nom: 'VHD 1 & 2 (Filavac)', duree_jours: 365 },
            { id: 2, nom: 'Myxomatose (Nobilis)', duree_jours: 365 },
        ],
        vaccinations: [
            { id: 1, femelle_id: 1, vaccin_id: 1, date_vaccination: '2026-01-15', date_prochain: '2027-01-15' },
            { id: 2, femelle_id: 2, vaccin_id: 2, date_vaccination: '2026-02-10', date_prochain: '2027-02-10' },
        ],
        aliments: [
            { id: 1, nom: 'Granulés Mères & Lapereaux', stock_kg: 25, consommation_g: 150 },
            { id: 2, nom: 'Foin de Crau (Ballot)', stock_kg: 15, consommation_g: 80 },
        ],
        cycles: [
            // F01: Allaitante (Jour 18 post mise bas, 7 lapereaux vivants)
            {
                id: 1,
                femelle_id: 1,
                male_numero: 'M01 - Rex',
                date_saillie: formatDateISO(addDays(today, -49)),
                date_palpation_prevue: formatDateISO(addDays(today, -38)),
                date_palpation_reelle: formatDateISO(addDays(today, -38)),
                resultat_palpation: 'positive',
                date_boite_nid_prevue: formatDateISO(addDays(today, -21)),
                boite_nid_posee: 1,
                date_mise_bas_prevue: formatDateISO(addDays(today, -18)),
                date_mise_bas_reelle: dMinus18,
                nombre_vivants: 7,
                nombre_morts: 0,
                date_sevrage_prevue: formatDateISO(addDays(today, 17)),
                date_sevrage_reelle: null,
                nombre_sevres: null,
                statut: 'allaitante',
                motif_echec: null,
                notes: 'Superbe portée vigoureuse, bonne lactation.'
            },
            // F02: Gestante à J28 (Boîte à nid à poser impérativement aujourd hui !)
            {
                id: 2,
                femelle_id: 2,
                male_numero: 'M02 - Géant',
                date_saillie: dMinus28,
                date_palpation_prevue: formatDateISO(addDays(today, -17)),
                date_palpation_reelle: formatDateISO(addDays(today, -17)),
                resultat_palpation: 'positive',
                date_boite_nid_prevue: formatDateISO(today),
                boite_nid_posee: 0,
                date_mise_bas_prevue: formatDateISO(addDays(today, 3)),
                date_mise_bas_reelle: null,
                nombre_vivants: null,
                nombre_morts: null,
                date_sevrage_prevue: null,
                date_sevrage_reelle: null,
                nombre_sevres: null,
                statut: 'gestante',
                motif_echec: null,
                notes: 'Palpation nette (plusieurs embryons bien sentis). Nid à poser !'
            },
            // F03: Saillie à J11 (Palpation abdominale à réaliser maintenant !)
            {
                id: 3,
                femelle_id: 3,
                male_numero: 'M01 - Rex',
                date_saillie: dMinus11,
                date_palpation_prevue: formatDateISO(today),
                date_palpation_reelle: null,
                resultat_palpation: null,
                date_boite_nid_prevue: formatDateISO(addDays(today, 17)),
                boite_nid_posee: 0,
                date_mise_bas_prevue: formatDateISO(addDays(today, 20)),
                date_mise_bas_reelle: null,
                nombre_vivants: null,
                nombre_morts: null,
                date_sevrage_prevue: null,
                date_sevrage_reelle: null,
                nombre_sevres: null,
                statut: 'saillie',
                motif_echec: null,
                notes: 'Accouplement rapide et net.'
            }
        ],
        lastId: {
            clapets: 4,
            femelles: 4,
            vaccins: 2,
            vaccinations: 2,
            aliments: 2,
            cycles: 3,
            daily_checks: 0,
            settings: 0
        }
    };
};

let mockDb = getInitialMockData();

// === WEB LOCALSTORAGE HELPERS ===
const loadMockDbFromStorage = () => {
    if (typeof window !== 'undefined' && window.localStorage) {
        try {
            const data = window.localStorage.getItem('lapino_mock_db_v2');
            if (data) {
                mockDb = JSON.parse(data);
                return;
            }
        } catch (e) {
            console.warn('Impossible de charger mockDb depuis localStorage:', e);
        }
    }
};

const saveMockDbToStorage = () => {
    if (typeof window !== 'undefined' && window.localStorage) {
        try {
            window.localStorage.setItem('lapino_mock_db_v2', JSON.stringify(mockDb));
        } catch (e) {
            console.warn('Impossible de sauvegarder mockDb dans localStorage:', e);
        }
    }
};

const mockDelay = () => new Promise(resolve => setTimeout(resolve, 50));

// === DATABASE HELPER ===
const executeSqlAsync = async (sql, params = []) => {
    if (!db) {
        throw new Error("Base de données non initialisée");
    }

    const isSelect = /^\s*(SELECT|PRAGMA)/i.test(sql);

    try {
        if (isSelect) {
            const rows = await db.getAllAsync(sql, params);
            return { rows: rows, insertId: null, rowsAffected: 0 };
        } else {
            const result = await db.runAsync(sql, params);
            return { rows: [], insertId: result.lastInsertRowId, rowsAffected: result.changes };
        }
    } catch (error) {
        console.error(`Erreur SQL: ${sql}`, error);
        throw error;
    }
};

// Initialiser la base de données
export const initDatabase = async () => {
    if (Platform.OS === 'web') {
        console.log('Web: Chargement des données locales (localStorage)');
        loadMockDbFromStorage();
        return Promise.resolve();
    }

    try {
        db = await SQLite.openDatabaseAsync('lapino.db');
        await db.execAsync('PRAGMA foreign_keys = ON;');

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
            `CREATE TABLE IF NOT EXISTS aliments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nom TEXT NOT NULL,
                stock_kg REAL DEFAULT 0,
                consommation_g REAL DEFAULT 0
            );`,
            `CREATE TABLE IF NOT EXISTS cycles_reproduction (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                femelle_id INTEGER NOT NULL,
                male_numero TEXT,
                date_saillie TEXT NOT NULL,
                date_palpation_prevue TEXT,
                date_palpation_reelle TEXT,
                resultat_palpation TEXT,
                date_boite_nid_prevue TEXT,
                boite_nid_posee INTEGER DEFAULT 0,
                date_mise_bas_prevue TEXT NOT NULL,
                date_mise_bas_reelle TEXT,
                nombre_vivants INTEGER,
                nombre_morts INTEGER,
                date_sevrage_prevue TEXT,
                date_sevrage_reelle TEXT,
                nombre_sevres INTEGER,
                statut TEXT NOT NULL,
                motif_echec TEXT,
                notes TEXT,
                FOREIGN KEY (femelle_id) REFERENCES femelles(id) ON DELETE CASCADE
            );`,
            `CREATE TABLE IF NOT EXISTS daily_checks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT UNIQUE NOT NULL,
                statut TEXT NOT NULL
            );`,
            `CREATE TABLE IF NOT EXISTS settings (
                key TEXT PRIMARY KEY,
                value TEXT
            );`,
            'CREATE INDEX IF NOT EXISTS idx_femelles_clapet ON femelles(clapet_id);',
            'CREATE INDEX IF NOT EXISTS idx_vaccinations_femelle ON vaccinations_femelles(femelle_id);',
            'CREATE INDEX IF NOT EXISTS idx_vaccinations_date ON vaccinations_femelles(date_prochain);',
            'CREATE INDEX IF NOT EXISTS idx_cycles_femelle ON cycles_reproduction(femelle_id);',
            'CREATE INDEX IF NOT EXISTS idx_cycles_statut ON cycles_reproduction(statut);'
        ];

        for (const query of schema) {
            await db.execAsync(query);
        }

        // Migrations douces pour ajouter d'éventuelles colonnes manquantes sans écraser la BD existante
        const migrations = [
            "ALTER TABLE cycles_reproduction ADD COLUMN male_numero TEXT;",
            "ALTER TABLE cycles_reproduction ADD COLUMN date_palpation_prevue TEXT;",
            "ALTER TABLE cycles_reproduction ADD COLUMN date_palpation_reelle TEXT;",
            "ALTER TABLE cycles_reproduction ADD COLUMN resultat_palpation TEXT;",
            "ALTER TABLE cycles_reproduction ADD COLUMN date_boite_nid_prevue TEXT;",
            "ALTER TABLE cycles_reproduction ADD COLUMN boite_nid_posee INTEGER DEFAULT 0;",
            "ALTER TABLE cycles_reproduction ADD COLUMN date_sevrage_reelle TEXT;",
            "ALTER TABLE cycles_reproduction ADD COLUMN nombre_sevres INTEGER;",
            "ALTER TABLE cycles_reproduction ADD COLUMN motif_echec TEXT;",
            "ALTER TABLE cycles_reproduction ADD COLUMN notes TEXT;"
        ];

        for (const migration of migrations) {
            try {
                await db.execAsync(migration);
            } catch (ignored) {
                // Colonne existe déjà, comportement attendu
            }
        }

        console.log('Base de données SQLite initialisée et migrée avec succès');
    } catch (error) {
        console.error('Erreur initialisation DB:', error);
        throw error;
    }
};

// ========== CLAPETS ==========

export const getAllClapets = async () => {
    if (Platform.OS === 'web') {
        await mockDelay();
        return [...mockDb.clapets].sort((a, b) => a.numero.localeCompare(b.numero));
    }
    const { rows } = await executeSqlAsync('SELECT * FROM clapets ORDER BY numero');
    return rows;
};

export const addClapet = async (numero) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        const exists = mockDb.clapets.some(c => c.numero.toUpperCase() === numero.toUpperCase());
        if (exists) throw new Error('Ce numéro de clapet existe déjà');
        mockDb.lastId.clapets++;
        const newClapet = { id: mockDb.lastId.clapets, numero };
        mockDb.clapets.push(newClapet);
        saveMockDbToStorage();
        return newClapet.id;
    }
    const { insertId } = await executeSqlAsync('INSERT INTO clapets (numero) VALUES (?);', [numero]);
    return insertId;
};

export const deleteClapet = async (id) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        mockDb.clapets = mockDb.clapets.filter(c => c.id !== id);
        mockDb.femelles.forEach(f => {
            if (f.clapet_id === id) f.clapet_id = null;
        });
        saveMockDbToStorage();
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
            return {
                ...f,
                clapet_numero: clapet ? clapet.numero : null
            };
        }).sort((a, b) => a.numero.localeCompare(b.numero));
    }
    const { rows } = await executeSqlAsync(
        `SELECT f.*, c.numero as clapet_numero 
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
        if (!f) return null;
        const clapet = mockDb.clapets.find(c => c.id === f.clapet_id);
        const activeCycle = (mockDb.cycles || []).find(c =>
            c.femelle_id === id && ['saillie', 'gestante', 'allaitante'].includes(c.statut)
        );
        return {
            ...f,
            clapet_numero: clapet ? clapet.numero : null,
            cycle: activeCycle || null
        };
    }
    const { rows } = await executeSqlAsync(
        `SELECT f.*, c.numero as clapet_numero 
         FROM femelles f 
         LEFT JOIN clapets c ON f.clapet_id = c.id 
         WHERE f.id = ?`,
        [id]
    );
    if (!rows || rows.length === 0) return null;
    const femelle = rows[0];

    const { rows: cycleRows } = await executeSqlAsync(
        `SELECT * FROM cycles_reproduction 
         WHERE femelle_id = ? AND statut IN ('saillie', 'gestante', 'allaitante')
         ORDER BY date_saillie DESC LIMIT 1`,
        [id]
    );
    femelle.cycle = cycleRows.length > 0 ? cycleRows[0] : null;
    return femelle;
};

export const addFemelle = async (numero, clapetId, dateNaissance, statut = 'vivante') => {
    if (Platform.OS === 'web') {
        await mockDelay();
        mockDb.lastId.femelles++;
        const newFemelle = {
            id: mockDb.lastId.femelles,
            numero,
            clapet_id: clapetId || null,
            date_naissance: dateNaissance || null,
            statut
        };
        mockDb.femelles.push(newFemelle);
        saveMockDbToStorage();
        return newFemelle.id;
    }
    const { insertId } = await executeSqlAsync(
        'INSERT INTO femelles (numero, clapet_id, date_naissance, statut) VALUES (?, ?, ?, ?);',
        [numero, clapetId, dateNaissance, statut]
    );
    return insertId;
};

export const updateFemelle = async (id, numero, clapetId, dateNaissance, statut) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        const index = mockDb.femelles.findIndex(f => f.id === id);
        if (index !== -1) {
            mockDb.femelles[index] = {
                ...mockDb.femelles[index],
                numero,
                clapet_id: clapetId || null,
                date_naissance: dateNaissance || null,
                statut
            };
            saveMockDbToStorage();
        }
        return;
    }
    await executeSqlAsync(
        'UPDATE femelles SET numero = ?, clapet_id = ?, date_naissance = ?, statut = ? WHERE id = ?;',
        [numero, clapetId, dateNaissance, statut, id]
    );
};

export const deleteFemelle = async (id) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        mockDb.femelles = mockDb.femelles.filter(f => f.id !== id);
        mockDb.vaccinations = mockDb.vaccinations.filter(v => v.femelle_id !== id);
        mockDb.cycles = (mockDb.cycles || []).filter(c => c.femelle_id !== id);
        saveMockDbToStorage();
        return;
    }
    await executeSqlAsync('DELETE FROM femelles WHERE id = ?;', [id]);
};

// ========== VACCINS & VACCINATIONS ==========

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
        const newVaccin = { id: mockDb.lastId.vaccins, nom, duree_jours: dureeJours };
        mockDb.vaccins.push(newVaccin);
        saveMockDbToStorage();
        return newVaccin.id;
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
            saveMockDbToStorage();
        }
        return;
    }
    await executeSqlAsync('UPDATE vaccins SET nom = ?, duree_jours = ? WHERE id = ?;', [nom, dureeJours, id]);
};

export const deleteVaccin = async (id) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        mockDb.vaccins = mockDb.vaccins.filter(v => v.id !== id);
        mockDb.vaccinations = mockDb.vaccinations.filter(v => v.vaccin_id !== id);
        saveMockDbToStorage();
        return;
    }
    await executeSqlAsync('DELETE FROM vaccins WHERE id = ?;', [id]);
};

export const getVaccinationsByFemelle = async (femelleId) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        return mockDb.vaccinations
            .filter(v => v.femelle_id === femelleId)
            .map(v => {
                const vaccin = mockDb.vaccins.find(vac => vac.id === v.vaccin_id);
                return {
                    ...v,
                    vaccin_nom: vaccin ? vaccin.nom : '?'
                };
            })
            .sort((a, b) => b.date_vaccination.localeCompare(a.date_vaccination));
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
    let dureeJours = 365;
    if (Platform.OS === 'web') {
        const vaccin = mockDb.vaccins.find(v => v.id === vaccinId);
        if (vaccin) dureeJours = vaccin.duree_jours;
    } else {
        const { rows } = await executeSqlAsync('SELECT duree_jours FROM vaccins WHERE id = ?', [vaccinId]);
        if (rows.length > 0) dureeJours = rows[0].duree_jours;
    }

    const dateProchain = formatDateISO(addDays(new Date(dateVaccination), dureeJours));

    if (Platform.OS === 'web') {
        await mockDelay();
        mockDb.lastId.vaccinations++;
        const newV = {
            id: mockDb.lastId.vaccinations,
            femelle_id: femelleId,
            vaccin_id: vaccinId,
            date_vaccination: dateVaccination,
            date_prochain: dateProchain
        };
        mockDb.vaccinations.push(newV);
        saveMockDbToStorage();
        return newV.id;
    }

    const { insertId } = await executeSqlAsync(
        `INSERT INTO vaccinations_femelles 
         (femelle_id, vaccin_id, date_vaccination, date_prochain) 
         VALUES (?, ?, ?, ?);`,
        [femelleId, vaccinId, dateVaccination, dateProchain]
    );
    return insertId;
};

export const getVaccinsEnRetard = async () => {
    if (Platform.OS === 'web') {
        await mockDelay();
        const today = formatDateISO(new Date());
        return mockDb.vaccinations
            .filter(v => {
                const femelle = mockDb.femelles.find(f => f.id === v.femelle_id);
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
    const today = new Date();
    const todayStr = formatDateISO(today);
    const in7DaysStr = formatDateISO(addDays(today, 7));

    if (Platform.OS === 'web') {
        await mockDelay();
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

    const { rows } = await executeSqlAsync(
        `SELECT vf.*, f.numero as femelle_numero, v.nom as vaccin_nom
         FROM vaccinations_femelles vf
         JOIN femelles f ON vf.femelle_id = f.id
         JOIN vaccins v ON vf.vaccin_id = v.id
         WHERE vf.date_prochain >= ? AND vf.date_prochain <= ? AND f.statut = 'vivante'
         ORDER BY vf.date_prochain`,
        [todayStr, in7DaysStr]
    );
    return rows;
};

// ========== ALIMENTS ==========

export const getAllAliments = async () => {
    if (Platform.OS === 'web') {
        await mockDelay();
        const totalFemelles = mockDb.femelles.filter(f => f.statut === 'vivante').length;
        return (mockDb.aliments || []).map(a => {
            const consoJourKg = (totalFemelles * a.consommation_g) / 1000;
            const joursRestants = consoJourKg > 0 ? Math.floor(a.stock_kg / consoJourKg) : 999;
            return {
                ...a,
                conso_jour_kg: consoJourKg.toFixed(2),
                jours_restants: joursRestants
            };
        });
    }

    const { rows: femellesRows } = await executeSqlAsync("SELECT COUNT(*) as total FROM femelles WHERE statut = 'vivante'");
    const totalFemelles = femellesRows[0]?.total || 0;

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
        mockDb.lastId.aliments++;
        const id = mockDb.lastId.aliments;
        mockDb.aliments.push({ id, nom, stock_kg: stockKg, consommation_g: consommationG });
        saveMockDbToStorage();
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
        const index = mockDb.aliments.findIndex(a => a.id === id);
        if (index !== -1) {
            mockDb.aliments[index] = { ...mockDb.aliments[index], nom, stock_kg: stockKg, consommation_g: consommationG };
            saveMockDbToStorage();
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
        mockDb.aliments = mockDb.aliments.filter(a => a.id !== id);
        saveMockDbToStorage();
        return;
    }
    await executeSqlAsync('DELETE FROM aliments WHERE id = ?;', [id]);
};

// ========== REPRODUCTION (CUNICULICULTURE PROFESSIONNELLE) ==========

/**
 * Récupère les femelles avec leur clapet et leur cycle actif complet
 */
export const getFemellesWithStatus = async () => {
    if (Platform.OS === 'web') {
        await mockDelay();
        if (!mockDb.cycles) mockDb.cycles = [];

        return mockDb.femelles
            .filter(f => f.statut === 'vivante')
            .map(f => {
                const clapet = mockDb.clapets.find(c => c.id === f.clapet_id);
                const activeCycle = mockDb.cycles.find(c =>
                    c.femelle_id === f.id &&
                    ['saillie', 'gestante', 'allaitante'].includes(c.statut)
                );

                return {
                    ...f,
                    clapet_numero: clapet ? clapet.numero : null,
                    cycle: activeCycle ? { ...activeCycle } : null
                };
            })
            .sort((a, b) => a.numero.localeCompare(b.numero));
    }

    // SQLite natif : Récupère femelles + cycle actif complet joint
    const { rows } = await executeSqlAsync(
        `SELECT 
            f.id, f.numero, f.clapet_id, f.date_naissance, f.statut,
            c.numero as clapet_numero,
            cr.id as c_id,
            cr.male_numero as c_male_numero,
            cr.date_saillie as c_date_saillie,
            cr.date_palpation_prevue as c_date_palpation_prevue,
            cr.date_palpation_reelle as c_date_palpation_reelle,
            cr.resultat_palpation as c_resultat_palpation,
            cr.date_boite_nid_prevue as c_date_boite_nid_prevue,
            cr.boite_nid_posee as c_boite_nid_posee,
            cr.date_mise_bas_prevue as c_date_mise_bas_prevue,
            cr.date_mise_bas_reelle as c_date_mise_bas_reelle,
            cr.nombre_vivants as c_nombre_vivants,
            cr.nombre_morts as c_nombre_morts,
            cr.date_sevrage_prevue as c_date_sevrage_prevue,
            cr.date_sevrage_reelle as c_date_sevrage_reelle,
            cr.nombre_sevres as c_nombre_sevres,
            cr.statut as c_statut,
            cr.notes as c_notes
         FROM femelles f 
         LEFT JOIN clapets c ON f.clapet_id = c.id
         LEFT JOIN cycles_reproduction cr ON cr.femelle_id = f.id 
              AND cr.statut IN ('saillie', 'gestante', 'allaitante')
         WHERE f.statut = 'vivante'
         ORDER BY f.numero`
    );

    return rows.map(r => {
        let cycle = null;
        if (r.c_id) {
            cycle = {
                id: r.c_id,
                femelle_id: r.id,
                male_numero: r.c_male_numero,
                date_saillie: r.c_date_saillie,
                date_palpation_prevue: r.c_date_palpation_prevue,
                date_palpation_reelle: r.c_date_palpation_reelle,
                resultat_palpation: r.c_resultat_palpation,
                date_boite_nid_prevue: r.c_date_boite_nid_prevue,
                boite_nid_posee: r.c_boite_nid_posee,
                date_mise_bas_prevue: r.c_date_mise_bas_prevue,
                date_mise_bas_reelle: r.c_date_mise_bas_reelle,
                nombre_vivants: r.c_nombre_vivants,
                nombre_morts: r.c_nombre_morts,
                date_sevrage_prevue: r.c_date_sevrage_prevue,
                date_sevrage_reelle: r.c_date_sevrage_reelle,
                nombre_sevres: r.c_nombre_sevres,
                statut: r.c_statut,
                notes: r.c_notes
            };
        }
        return {
            id: r.id,
            numero: r.numero,
            clapet_id: r.clapet_id,
            date_naissance: r.date_naissance,
            statut: r.statut,
            clapet_numero: r.clapet_numero,
            cycle
        };
    });
};

/**
 * Démarre un nouveau cycle de reproduction (Saillie / Mise au mâle)
 * Calcule automatiquement :
 * - Date de palpation recommandée : J+11 (plage J10 à J14)
 * - Date de pose de la boîte à nid : J+28 (impératif J27-J28)
 * - Date de mise bas prévue : J+31
 */
export const startCycle = async (femelleId, dateSaillie, maleNumero = '', notes = '') => {
    const dSaillie = new Date(dateSaillie);
    const datePalpationPrevue = formatDateISO(addDays(dSaillie, 11));
    const dateBoiteNidPrevue = formatDateISO(addDays(dSaillie, 28));
    const dateMiseBasPrevue = formatDateISO(addDays(dSaillie, 31));

    if (Platform.OS === 'web') {
        await mockDelay();
        if (!mockDb.cycles) mockDb.cycles = [];
        mockDb.lastId.cycles++;
        const newCycle = {
            id: mockDb.lastId.cycles,
            femelle_id: femelleId,
            male_numero: maleNumero || null,
            date_saillie: dateSaillie,
            date_palpation_prevue: datePalpationPrevue,
            date_palpation_reelle: null,
            resultat_palpation: null,
            date_boite_nid_prevue: dateBoiteNidPrevue,
            boite_nid_posee: 0,
            date_mise_bas_prevue: dateMiseBasPrevue,
            date_mise_bas_reelle: null,
            nombre_vivants: null,
            nombre_morts: null,
            date_sevrage_prevue: null,
            date_sevrage_reelle: null,
            nombre_sevres: null,
            statut: 'saillie',
            motif_echec: null,
            notes: notes || null
        };
        mockDb.cycles.push(newCycle);
        saveMockDbToStorage();
        return newCycle.id;
    }

    const { insertId } = await executeSqlAsync(
        `INSERT INTO cycles_reproduction 
         (femelle_id, male_numero, date_saillie, date_palpation_prevue, date_boite_nid_prevue, 
          date_mise_bas_prevue, boite_nid_posee, statut, notes) 
         VALUES (?, ?, ?, ?, ?, ?, 0, 'saillie', ?);`,
        [femelleId, maleNumero || null, dateSaillie, datePalpationPrevue, dateBoiteNidPrevue, dateMiseBasPrevue, notes || null]
    );
    return insertId;
};

// Alias pour compatibilité
export const addCycle = startCycle;

/**
 * Diagnostic de gestation (Palpation abdominale entre J10 et J14)
 */
export const verifyGestation = async (cycleId, dateVerification, isPregnant, notes = '') => {
    const statut = isPregnant ? 'gestante' : 'echec';
    const resultat = isPregnant ? 'positive' : 'negative';
    const motifEchec = isPregnant ? null : 'non_gestante';

    if (Platform.OS === 'web') {
        await mockDelay();
        const index = mockDb.cycles.findIndex(c => c.id === cycleId);
        if (index !== -1) {
            mockDb.cycles[index] = {
                ...mockDb.cycles[index],
                statut,
                date_palpation_reelle: dateVerification,
                resultat_palpation: resultat,
                motif_echec: motifEchec,
                notes: notes ? `${mockDb.cycles[index].notes ? mockDb.cycles[index].notes + ' | ' : ''}${notes}` : mockDb.cycles[index].notes
            };
            saveMockDbToStorage();
        }
        return;
    }

    await executeSqlAsync(
        `UPDATE cycles_reproduction 
         SET statut = ?, date_palpation_reelle = ?, resultat_palpation = ?, 
             motif_echec = ?, notes = COALESCE(notes || ' | ', '') || ?
         WHERE id = ?;`,
        [statut, dateVerification, resultat, motifEchec, notes || '', cycleId]
    );
};

/**
 * Confirme la pose de la boîte à nid avec paille propre (J28)
 */
export const confirmNestBox = async (cycleId, isPlaced = 1) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        const index = mockDb.cycles.findIndex(c => c.id === cycleId);
        if (index !== -1) {
            mockDb.cycles[index].boite_nid_posee = isPlaced ? 1 : 0;
            saveMockDbToStorage();
        }
        return;
    }

    await executeSqlAsync(
        'UPDATE cycles_reproduction SET boite_nid_posee = ? WHERE id = ?;',
        [isPlaced ? 1 : 0, cycleId]
    );
};

/**
 * Enregistre la mise bas (naissance des lapereaux)
 * Calcule automatiquement la date de sevrage à J+35
 */
export const confirmBirth = async (cycleId, dateNaissance, vivants, morts = 0, notes = '') => {
    const dNaissance = new Date(dateNaissance);
    const dateSevrage = formatDateISO(addDays(dNaissance, 35));

    if (Platform.OS === 'web') {
        await mockDelay();
        const index = mockDb.cycles.findIndex(c => c.id === cycleId);
        if (index !== -1) {
            mockDb.cycles[index] = {
                ...mockDb.cycles[index],
                date_mise_bas_reelle: dateNaissance,
                nombre_vivants: parseInt(vivants) || 0,
                nombre_morts: parseInt(morts) || 0,
                date_sevrage_prevue: dateSevrage,
                statut: 'allaitante',
                notes: notes ? `${mockDb.cycles[index].notes ? mockDb.cycles[index].notes + ' | ' : ''}${notes}` : mockDb.cycles[index].notes
            };
            saveMockDbToStorage();
        }
        return;
    }

    await executeSqlAsync(
        `UPDATE cycles_reproduction 
         SET date_mise_bas_reelle = ?, nombre_vivants = ?, nombre_morts = ?, 
             date_sevrage_prevue = ?, statut = 'allaitante',
             notes = COALESCE(notes || ' | ', '') || ?
         WHERE id = ?;`,
        [dateNaissance, parseInt(vivants) || 0, parseInt(morts) || 0, dateSevrage, notes || '', cycleId]
    );
};

/**
 * Enregistre le sevrage de la portée (séparation des petits)
 * Clôture le cycle avec succès ('termine')
 */
export const confirmWeaning = async (cycleId, dateSevrage, sevres, notes = '') => {
    if (Platform.OS === 'web') {
        await mockDelay();
        const index = mockDb.cycles.findIndex(c => c.id === cycleId);
        if (index !== -1) {
            mockDb.cycles[index] = {
                ...mockDb.cycles[index],
                date_sevrage_reelle: dateSevrage,
                nombre_sevres: parseInt(sevres) || 0,
                statut: 'termine',
                notes: notes ? `${mockDb.cycles[index].notes ? mockDb.cycles[index].notes + ' | ' : ''}${notes}` : mockDb.cycles[index].notes
            };
            saveMockDbToStorage();
        }
        return;
    }

    await executeSqlAsync(
        `UPDATE cycles_reproduction 
         SET date_sevrage_reelle = ?, nombre_sevres = ?, statut = 'termine',
             notes = COALESCE(notes || ' | ', '') || ?
         WHERE id = ?;`,
        [dateSevrage, parseInt(sevres) || 0, notes || '', cycleId]
    );
};

/**
 * Arrêt du cycle pour incident (fausse couche, mortalité, abandon, etc.)
 */
export const stopCycle = async (cycleId, reason = 'echec', motifEchec = 'autre', notes = '') => {
    if (Platform.OS === 'web') {
        await mockDelay();
        const index = mockDb.cycles.findIndex(c => c.id === cycleId);
        if (index !== -1) {
            mockDb.cycles[index] = {
                ...mockDb.cycles[index],
                statut: reason,
                motif_echec: motifEchec,
                notes: notes ? `${mockDb.cycles[index].notes ? mockDb.cycles[index].notes + ' | ' : ''}${notes}` : mockDb.cycles[index].notes
            };
            saveMockDbToStorage();
        }
        return;
    }

    await executeSqlAsync(
        `UPDATE cycles_reproduction 
         SET statut = ?, motif_echec = ?, notes = COALESCE(notes || ' | ', '') || ? 
         WHERE id = ?;`,
        [reason, motifEchec, notes || '', cycleId]
    );
};

/**
 * Alertes de reproduction en temps réel pour l'éleveur :
 * - Palpations à faire (J10 à J14 post-saillie)
 * - Boîtes à nid à poser d'urgence (J28 post-saillie / <= 3 jours de mise bas)
 * - Mises bas imminentes (<= 2 jours ou dépassées)
 * - Sevrages à effectuer (lapereaux à >= 32 jours)
 */
export const getReproductionAlerts = async () => {
    const femelles = await getFemellesWithStatus();
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const alerts = {
        palpations: [],
        boitesNid: [],
        misesBas: [],
        sevrages: []
    };

    for (const f of femelles) {
        if (!f.cycle) continue;
        const c = f.cycle;

        if (c.statut === 'saillie') {
            const dSaillie = new Date(c.date_saillie);
            const daysSinceSaillie = Math.floor((today - dSaillie) / (1000 * 60 * 60 * 24));
            if (daysSinceSaillie >= 10) {
                alerts.palpations.push({
                    femelle: f,
                    cycle: c,
                    daysSinceSaillie,
                    message: `Palpation à faire (J${daysSinceSaillie})`
                });
            }
        } else if (c.statut === 'gestante') {
            const daysLeft = daysUntil(c.date_mise_bas_prevue);
            const dSaillie = new Date(c.date_saillie);
            const daysSinceSaillie = Math.floor((today - dSaillie) / (1000 * 60 * 60 * 24));

            // Boîte à nid à poser si J >= 27 et pas encore marquée posée
            if (daysSinceSaillie >= 27 && !c.boite_nid_posee) {
                alerts.boitesNid.push({
                    femelle: f,
                    cycle: c,
                    daysLeft,
                    message: `Poser boîte à nid (J${daysSinceSaillie}/31)`
                });
            }

            // Mise bas imminente si reste <= 2 jours ou en retard
            if (daysLeft !== null && daysLeft <= 2) {
                alerts.misesBas.push({
                    femelle: f,
                    cycle: c,
                    daysLeft,
                    message: daysLeft < 0 ? `Mise bas dépassée (${Math.abs(daysLeft)}j de retard)` : (daysLeft === 0 ? "Mise bas aujourd'hui !" : `Mise bas dans ${daysLeft} jour(s)`)
                });
            }
        } else if (c.statut === 'allaitante') {
            const daysLeft = daysUntil(c.date_sevrage_prevue);
            const dNaissance = new Date(c.date_mise_bas_reelle);
            const daysSinceBirth = Math.floor((today - dNaissance) / (1000 * 60 * 60 * 24));

            if (daysSinceBirth >= 30) {
                alerts.sevrages.push({
                    femelle: f,
                    cycle: c,
                    daysSinceBirth,
                    daysLeft,
                    message: `Prêt pour sevrage (J${daysSinceBirth} - ${c.nombre_vivants} petits)`
                });
            }
        }
    }

    return alerts;
};

/**
 * Statistiques globales du cheptel reproducteur
 */
export const getReproductionGlobalStats = async () => {
    const femelles = await getFemellesWithStatus();

    let enRepos = 0;
    let enSaillie = 0;
    let gestantes = 0;
    let allaitantes = 0;
    let lapereauxSousMere = 0;

    for (const f of femelles) {
        if (!f.cycle) {
            enRepos++;
        } else if (f.cycle.statut === 'saillie') {
            enSaillie++;
        } else if (f.cycle.statut === 'gestante') {
            gestantes++;
        } else if (f.cycle.statut === 'allaitante') {
            allaitantes++;
            lapereauxSousMere += parseInt(f.cycle.nombre_vivants) || 0;
        }
    }

    return {
        totalFemelles: femelles.length,
        enRepos,
        enSaillie,
        gestantes,
        allaitantes,
        lapereauxSousMere
    };
};

/**
 * Statistiques détaillées de productivité pour une lapine
 */
export const getFemelleStats = async (femelleId) => {
    let cycles = [];

    if (Platform.OS === 'web') {
        await mockDelay();
        cycles = (mockDb.cycles || []).filter(c => c.femelle_id === femelleId);
    } else {
        const { rows } = await executeSqlAsync(
            'SELECT * FROM cycles_reproduction WHERE femelle_id = ?',
            [femelleId]
        );
        cycles = rows;
    }

    const totalSaillies = cycles.length;
    const sailliesReussies = cycles.filter(c => ['gestante', 'allaitante', 'termine'].includes(c.statut) || c.date_mise_bas_reelle).length;
    const totalMisesBas = cycles.filter(c => c.date_mise_bas_reelle).length;
    const totalVivants = cycles.reduce((acc, c) => acc + (parseInt(c.nombre_vivants) || 0), 0);
    const totalMorts = cycles.reduce((acc, c) => acc + (parseInt(c.nombre_morts) || 0), 0);
    const totalSevres = cycles.reduce((acc, c) => acc + (parseInt(c.nombre_sevres) || 0), 0);

    const tauxFertilite = totalSaillies > 0 ? Math.round((sailliesReussies / totalSaillies) * 100) : 0;
    const moyenneVivants = totalMisesBas > 0 ? (totalVivants / totalMisesBas).toFixed(1) : 0;
    const tauxSevrage = totalVivants > 0 && totalSevres > 0 ? Math.round((totalSevres / totalVivants) * 100) : (totalMisesBas > 0 ? 'N/D' : 0);

    return {
        total_saillies: totalSaillies,
        total_mises_bas: totalMisesBas,
        total_vivants: totalVivants,
        total_morts: totalMorts,
        total_sevres: totalSevres,
        taux_fertilite: tauxFertilite,
        moyenne_portee: moyenneVivants,
        taux_sevrage: tauxSevrage
    };
};

/**
 * Historique des cycles passés d'une femelle
 */
export const getFemelleHistory = async (femelleId) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        return (mockDb.cycles || [])
            .filter(c => c.femelle_id === femelleId)
            .sort((a, b) => new Date(b.date_saillie) - new Date(a.date_saillie));
    }

    const { rows } = await executeSqlAsync(
        `SELECT * FROM cycles_reproduction 
         WHERE femelle_id = ? 
         ORDER BY date_saillie DESC`,
        [femelleId]
    );
    return rows;
};

// ========== STATISTIQUES DASHBOARD GENERALES ==========

export const getStatistics = async () => {
    const reproStats = await getReproductionGlobalStats();
    let clapetsRemplis = 0;
    let clapetsTotal = 0;

    if (Platform.OS === 'web') {
        await mockDelay();
        clapetsTotal = mockDb.clapets.length;
        const usedIds = new Set(mockDb.femelles.filter(f => f.statut === 'vivante' && f.clapet_id).map(f => f.clapet_id));
        clapetsRemplis = usedIds.size;
    } else {
        const { rows: rowsClapets } = await executeSqlAsync(
            `SELECT 
                COUNT(*) as total,
                COUNT(CASE WHEN f.id IS NOT NULL THEN 1 END) as remplis
             FROM clapets c
             LEFT JOIN femelles f ON c.id = f.clapet_id AND f.statut = 'vivante'`
        );
        if (rowsClapets.length > 0) {
            clapetsTotal = rowsClapets[0].total || 0;
            clapetsRemplis = rowsClapets[0].remplis || 0;
        }
    }

    return {
        totalFemelles: reproStats.totalFemelles,
        enRepos: reproStats.enRepos,
        enSaillie: reproStats.enSaillie,
        gestantes: reproStats.gestantes,
        allaitantes: reproStats.allaitantes,
        lapereauxSousMere: reproStats.lapereauxSousMere,
        clapetsRemplis,
        clapetsVides: Math.max(0, clapetsTotal - clapetsRemplis),
        clapetsTotal
    };
};

// ========== DAILY CHECKS ==========

export const getDailyCheckStatus = async (date) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        if (!mockDb.daily_checks) mockDb.daily_checks = [];
        return mockDb.daily_checks.find(c => c.date === date);
    }

    const { rows } = await executeSqlAsync('SELECT * FROM daily_checks WHERE date = ?', [date]);
    return rows[0];
};

export const performDailyCheck = async (date) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        if (!mockDb.daily_checks) mockDb.daily_checks = [];
        const exists = mockDb.daily_checks.find(c => c.date === date);
        if (!exists) {
            mockDb.lastId.daily_checks++;
            mockDb.daily_checks.push({ id: mockDb.lastId.daily_checks, date, statut: 'ok' });
            saveMockDbToStorage();
        }
        return;
    }

    await executeSqlAsync('INSERT OR IGNORE INTO daily_checks (date, statut) VALUES (?, "ok");', [date]);
};

// ========== SETTINGS ==========

export const getSetting = async (key) => {
    if (Platform.OS === 'web') {
        if (!mockDb.settings) mockDb.settings = [];
        const setting = mockDb.settings.find(s => s.key === key);
        return setting ? setting.value : null;
    }
    const { rows } = await executeSqlAsync('SELECT value FROM settings WHERE key = ?', [key]);
    return rows.length > 0 ? rows[0].value : null;
};

export const saveSetting = async (key, value) => {
    if (Platform.OS === 'web') {
        if (!mockDb.settings) mockDb.settings = [];
        const index = mockDb.settings.findIndex(s => s.key === key);
        if (index !== -1) {
            mockDb.settings[index].value = value;
        } else {
            mockDb.settings.push({ key, value });
        }
        saveMockDbToStorage();
        return;
    }
    await executeSqlAsync(
        'INSERT OR REPLACE INTO settings (key, value) VALUES (?, ?)',
        [key, value]
    );
};

export default db;
