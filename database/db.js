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
        { id: 4, numero: 'B2' }
    ],
    femelles: [
        { id: 1, numero: 'Lola', clapet_id: 1, date_naissance: '2023-01-01', statut: 'vivante' },
        { id: 2, numero: 'Bella', clapet_id: 2, date_naissance: '2023-02-15', statut: 'vivante' },
        { id: 3, numero: 'Daisy', clapet_id: 3, date_naissance: '2023-03-10', statut: 'vivante' },
        { id: 4, numero: 'Ruby', clapet_id: null, date_naissance: '2023-05-20', statut: 'vivante' }
    ],
    vaccins: [
        { id: 1, nom: 'Myxo-RHD', duree_jours: 180 },
        { id: 2, nom: 'VHD2', duree_jours: 365 }
    ],
    vaccinations: [
        { id: 1, femelle_id: 1, vaccin_id: 1, date_vaccination: '2023-11-01', date_prochain: '2024-05-01' }
    ],
    aliments: [
        { id: 1, nom: 'Granulés Mère', stock_kg: 25, consommation_g: 150 },
        { id: 2, nom: 'Foin Crau', stock_kg: 10, consommation_g: 50 }
    ],
    cycles: [
        // Lola: Gestante (Saillie il y a 20 jours)
        {
            id: 1,
            femelle_id: 1,
            date_saillie: new Date(Date.now() - 20 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            date_mise_bas_prevue: new Date(Date.now() + 11 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            statut: 'gestante'
        },
        // Bella: Allaitante (Mise bas il y a 10 jours)
        {
            id: 2,
            femelle_id: 2,
            date_saillie: new Date(Date.now() - 41 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            date_mise_bas_prevue: new Date(Date.now() - 10 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            date_mise_bas_reelle: new Date(Date.now() - 10 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            nombre_vivants: 6,
            nombre_morts: 0,
            date_sevrage_prevue: new Date(Date.now() + 25 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            statut: 'allaitante'
        }
    ],
    lastId: { clapets: 4, femelles: 4, vaccins: 2, vaccinations: 1, aliments: 2, cycles: 2 }
};

if (Platform.OS !== 'web') {
    db = SQLite.openDatabase('lapins.db');
}

// === WEB MOCK HELPERS ===
const mockDelay = () => new Promise(resolve => setTimeout(resolve, 100));

// Initialiser la base de données
export const initDatabase = async () => {
    if (Platform.OS === 'web') {
        console.log('Web detected: Using in-memory mock database');
        return Promise.resolve();
    }

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

export const getAllClapets = async () => {
    if (Platform.OS === 'web') {
        await mockDelay();
        return mockDb.clapets.map(c => {
            const femelle = mockDb.femelles.find(f => f.clapet_id === c.id && f.statut === 'vivante');
            return { ...c, femelle_numero: femelle ? femelle.numero : null };
        }).sort((a, b) => a.numero.localeCompare(b.numero));
    }

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

export const addClapet = async (numero) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        if (mockDb.clapets.some(c => c.numero === numero)) return Promise.reject("Unique constraint mock");
        mockDb.lastId.clapets++;
        const id = mockDb.lastId.clapets;
        mockDb.clapets.push({ id, numero });
        return id;
    }

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

export const deleteClapet = async (id) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        mockDb.clapets = mockDb.clapets.filter(c => c.id !== id);
        // Set clapet_id to null for females in this clapet
        mockDb.femelles = mockDb.femelles.map(f => f.clapet_id === id ? { ...f, clapet_id: null } : f);
        return;
    }

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

export const getFemelleById = async (id) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        const f = mockDb.femelles.find(fem => fem.id === id);
        if (!f) return Promise.reject("Not found");
        const clapet = mockDb.clapets.find(c => c.id === f.clapet_id);
        return { ...f, clapet_numero: clapet ? clapet.numero : null };
    }

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

export const addFemelle = async (numero, clapatId, dateNaissance, statut = 'vivante') => {
    if (Platform.OS === 'web') {
        await mockDelay();
        mockDb.lastId.femelles++;
        const id = mockDb.lastId.femelles;
        mockDb.femelles.push({ id, numero, clapet_id: clapatId, date_naissance: dateNaissance, statut });
        return id;
    }

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

export const updateFemelle = async (id, numero, clapatId, dateNaissance, statut) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        const index = mockDb.femelles.findIndex(f => f.id === id);
        if (index !== -1) {
            mockDb.femelles[index] = { ...mockDb.femelles[index], numero, clapet_id: clapatId, date_naissance: dateNaissance, statut };
        }
        return;
    }

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

export const deleteFemelle = async (id) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        mockDb.femelles = mockDb.femelles.filter(f => f.id !== id);
        mockDb.vaccinations = mockDb.vaccinations.filter(v => v.femelle_id !== id);
        return;
    }

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

export const getAllVaccins = async () => {
    if (Platform.OS === 'web') {
        await mockDelay();
        return [...mockDb.vaccins].sort((a, b) => a.nom.localeCompare(b.nom));
    }

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

export const addVaccin = async (nom, dureeJours) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        mockDb.lastId.vaccins++;
        const id = mockDb.lastId.vaccins;
        mockDb.vaccins.push({ id, nom, duree_jours: dureeJours });
        return id;
    }

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

export const updateVaccin = async (id, nom, dureeJours) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        const index = mockDb.vaccins.findIndex(v => v.id === id);
        if (index !== -1) {
            mockDb.vaccins[index] = { ...mockDb.vaccins[index], nom, duree_jours: dureeJours };
        }
        return;
    }

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

export const deleteVaccin = async (id) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        mockDb.vaccins = mockDb.vaccins.filter(v => v.id !== id);
        mockDb.vaccinations = mockDb.vaccinations.filter(v => v.vaccin_id !== id);
        return;
    }

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

export const deleteVaccination = async (id) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        mockDb.vaccinations = mockDb.vaccinations.filter(v => v.id !== id);
        return;
    }

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

    return new Promise((resolve, reject) => {
        db.transaction(tx => {
            // D'abord compter les femelles vivantes
            tx.executeSql(
                "SELECT COUNT(*) as total FROM femelles WHERE statut = 'vivante'",
                [],
                (_, { rows }) => {
                    const totalFemelles = rows._array[0].total;

                    // Puis récupérer les aliments
                    tx.executeSql(
                        'SELECT * FROM aliments ORDER BY nom',
                        [],
                        (_, { rows }) => {
                            const aliments = rows._array.map(a => {
                                const consoJourKg = (totalFemelles * a.consommation_g) / 1000;
                                const joursRestants = consoJourKg > 0 ? Math.floor(a.stock_kg / consoJourKg) : 999;
                                return {
                                    ...a,
                                    conso_jour_kg: consoJourKg.toFixed(2),
                                    jours_restants: joursRestants
                                };
                            });
                            resolve(aliments);
                        },
                        (_, error) => reject(error)
                    );
                },
                (_, error) => reject(error)
            );
        });
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

    return new Promise((resolve, reject) => {
        db.transaction(tx => {
            tx.executeSql(
                'INSERT INTO aliments (nom, stock_kg, consommation_g) VALUES (?, ?, ?);',
                [nom, stockKg, consommationG],
                (_, result) => resolve(result.insertId),
                (_, error) => reject(error)
            );
        });
    });
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

    return new Promise((resolve, reject) => {
        db.transaction(tx => {
            tx.executeSql(
                'UPDATE aliments SET nom = ?, stock_kg = ?, consommation_g = ? WHERE id = ?;',
                [nom, stockKg, consommationG, id],
                () => resolve(),
                (_, error) => reject(error)
            );
        });
    });
};

export const deleteAliment = async (id) => {
    if (Platform.OS === 'web') {
        await mockDelay();
        if (mockDb.aliments) {
            mockDb.aliments = mockDb.aliments.filter(a => a.id !== id);
        }
        return;
    }

    return new Promise((resolve, reject) => {
        db.transaction(tx => {
            tx.executeSql(
                'DELETE FROM aliments WHERE id = ?;',
                [id],
                () => resolve(),
                (_, error) => reject(error)
            );
        });
    });
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

    return new Promise((resolve, reject) => {
        db.transaction(tx => {
            tx.executeSql(
                `SELECT f.*, c.numero as clapet_numero,
                 cr.id as cycle_id, cr.date_saillie, cr.date_mise_bas_prevue, cr.date_mise_bas_reelle,
                 cr.date_sevrage_prevue, cr.statut as cycle_statut, cr.nombre_vivants, cr.nombre_morts
                 FROM femelles f 
                 LEFT JOIN clapets c ON f.clapet_id = c.id
                 LEFT JOIN cycles_reproduction cr ON f.id = cr.femelle_id 
                    AND cr.statut IN ('saillie', 'gestante', 'allaitante')
                 ORDER BY f.numero`,
                [],
                (_, { rows }) => {
                    const results = rows._array.map(row => {
                        const { cycle_id, date_saillie, date_mise_bas_prevue, date_mise_bas_reelle,
                            date_sevrage_prevue, cycle_statut, nombre_vivants, nombre_morts, ...femelle } = row;

                        return {
                            ...femelle,
                            cycle: cycle_id ? {
                                id: cycle_id,
                                date_saillie,
                                date_mise_bas_prevue,
                                date_mise_bas_reelle,
                                date_sevrage_prevue,
                                statut: cycle_statut,
                                nombre_vivants,
                                nombre_morts
                            } : null
                        };
                    });
                    resolve(results);
                },
                (_, error) => reject(error)
            );
        });
    });
};

export const startCycle = async (femelleId, dateSaillie) => {
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

    return new Promise((resolve, reject) => {
        db.transaction(tx => {
            tx.executeSql(
                `INSERT INTO cycles_reproduction 
                (femelle_id, date_saillie, date_mise_bas_prevue, statut) 
                VALUES (?, ?, ?, 'saillie');`,
                [femelleId, dateSaillie, dateMiseBasPrevue],
                (_, result) => resolve(result.insertId),
                (_, error) => reject(error)
            );
        });
    });
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

    return new Promise((resolve, reject) => {
        db.transaction(tx => {
            tx.executeSql(
                `UPDATE cycles_reproduction 
                 SET date_mise_bas_reelle = ?, nombre_vivants = ?, nombre_morts = ?, 
                     date_sevrage_prevue = ?, statut = 'allaitante'
                 WHERE id = ?;`,
                [dateNaissance, vivants, morts, dateSevrage, cycleId],
                () => resolve(),
                (_, error) => reject(error)
            );
        });
    });
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

    return new Promise((resolve, reject) => {
        db.transaction(tx => {
            tx.executeSql(
                'UPDATE cycles_reproduction SET statut = ? WHERE id = ?;',
                [reason, cycleId],
                () => resolve(),
                (_, error) => reject(error)
            );
        });
    });
};

export default db;
