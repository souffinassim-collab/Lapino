package com.lapino.gestion.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.lapino.gestion.model.*
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

class LapinoDatabase(context: Context) : SQLiteOpenHelper(context, "lapino.db", null, 2) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        createTables(db)
        seedInitialDataIfEmpty(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        createTables(db)
        val migrations = listOf(
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
        )
        for (sql in migrations) {
            try {
                db.execSQL(sql)
            } catch (_: Exception) {
            }
        }
    }

    private fun createTables(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS clapets (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                numero TEXT UNIQUE NOT NULL
            );
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS femelles (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                numero TEXT NOT NULL,
                clapet_id INTEGER,
                date_naissance TEXT,
                statut TEXT DEFAULT 'vivante',
                FOREIGN KEY (clapet_id) REFERENCES clapets(id) ON DELETE SET NULL
            );
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS vaccins (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nom TEXT NOT NULL,
                duree_jours INTEGER NOT NULL
            );
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS vaccinations_femelles (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                femelle_id INTEGER NOT NULL,
                vaccin_id INTEGER NOT NULL,
                date_vaccination TEXT NOT NULL,
                date_prochain TEXT NOT NULL,
                FOREIGN KEY (femelle_id) REFERENCES femelles(id) ON DELETE CASCADE,
                FOREIGN KEY (vaccin_id) REFERENCES vaccins(id) ON DELETE CASCADE
            );
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS aliments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nom TEXT NOT NULL,
                stock_kg REAL DEFAULT 0,
                consommation_g REAL DEFAULT 0
            );
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS cycles_reproduction (
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
            );
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS settings (
                key TEXT PRIMARY KEY,
                value TEXT
            );
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_femelles_clapet ON femelles(clapet_id);")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_vaccinations_femelle ON vaccinations_femelles(femelle_id);")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_vaccinations_date ON vaccinations_femelles(date_prochain);")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_cycles_femelle ON cycles_reproduction(femelle_id);")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_cycles_statut ON cycles_reproduction(statut);")
    }

    private fun seedInitialDataIfEmpty(db: SQLiteDatabase) {
        val cursor = db.rawQuery("SELECT COUNT(*) FROM clapets", null)
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()
        if (count > 0) return

        val today = DateHelper.todayIso()

        // Clapets
        val cA1 = db.insert("clapets", null, ContentValues().apply { put("numero", "A1") })
        val cA2 = db.insert("clapets", null, ContentValues().apply { put("numero", "A2") })
        val cB1 = db.insert("clapets", null, ContentValues().apply { put("numero", "B1") })
        val cB2 = db.insert("clapets", null, ContentValues().apply { put("numero", "B2") })

        // Femelles
        val f1 = db.insert("femelles", null, ContentValues().apply {
            put("numero", "F01 - Blanchette")
            put("clapet_id", cA1)
            put("date_naissance", "2025-03-15")
            put("statut", "vivante")
        })
        val f2 = db.insert("femelles", null, ContentValues().apply {
            put("numero", "F02 - Fauve")
            put("clapet_id", cA2)
            put("date_naissance", "2025-04-10")
            put("statut", "vivante")
        })
        val f3 = db.insert("femelles", null, ContentValues().apply {
            put("numero", "F03 - Noisette")
            put("clapet_id", cB1)
            put("date_naissance", "2025-05-01")
            put("statut", "vivante")
        })
        db.insert("femelles", null, ContentValues().apply {
            put("numero", "F04 - Papillon")
            put("clapet_id", cB2)
            put("date_naissance", "2025-06-20")
            put("statut", "vivante")
        })

        // Vaccins
        val v1 = db.insert("vaccins", null, ContentValues().apply {
            put("nom", "VHD 1 & 2 (Filavac)")
            put("duree_jours", 365)
        })
        val v2 = db.insert("vaccins", null, ContentValues().apply {
            put("nom", "Myxomatose (Nobilis)")
            put("duree_jours", 180)
        })

        // Vaccinations
        db.insert("vaccinations_femelles", null, ContentValues().apply {
            put("femelle_id", f1)
            put("vaccin_id", v1)
            put("date_vaccination", DateHelper.addDays(today, -120))
            put("date_prochain", DateHelper.addDays(today, 245))
        })
        db.insert("vaccinations_femelles", null, ContentValues().apply {
            put("femelle_id", f2)
            put("vaccin_id", v2)
            put("date_vaccination", DateHelper.addDays(today, -176))
            put("date_prochain", DateHelper.addDays(today, 4)) // Bientôt !
        })

        // Aliments
        db.insert("aliments", null, ContentValues().apply {
            put("nom", "Granulés Mères & Lapereaux")
            put("stock_kg", 25.0)
            put("consommation_g", 160.0)
        })
        db.insert("aliments", null, ContentValues().apply {
            put("nom", "Foin de Crau (Ballot)")
            put("stock_kg", 4.0)
            put("consommation_g", 150.0)
        })

        // Cycles de démonstration réalistes
        // 1. F01 : Allaitante (J18 post mise bas, 7 lapereaux)
        db.insert("cycles_reproduction", null, ContentValues().apply {
            put("femelle_id", f1)
            put("male_numero", "M01 - Rex")
            put("date_saillie", DateHelper.addDays(today, -49))
            put("date_palpation_prevue", DateHelper.addDays(today, -38))
            put("date_palpation_reelle", DateHelper.addDays(today, -38))
            put("resultat_palpation", "positive")
            put("date_boite_nid_prevue", DateHelper.addDays(today, -21))
            put("boite_nid_posee", 1)
            put("date_mise_bas_prevue", DateHelper.addDays(today, -18))
            put("date_mise_bas_reelle", DateHelper.addDays(today, -18))
            put("nombre_vivants", 7)
            put("nombre_morts", 0)
            put("date_sevrage_prevue", DateHelper.addDays(today, 17))
            put("statut", "allaitante")
            put("notes", "Portée vigoureuse, nid propre, très bonne lactation.")
        })

        // 2. F02 : Gestante à J28 (Boîte à nid urgente !)
        db.insert("cycles_reproduction", null, ContentValues().apply {
            put("femelle_id", f2)
            put("male_numero", "M02 - Géant")
            put("date_saillie", DateHelper.addDays(today, -28))
            put("date_palpation_prevue", DateHelper.addDays(today, -17))
            put("date_palpation_reelle", DateHelper.addDays(today, -17))
            put("resultat_palpation", "positive")
            put("date_boite_nid_prevue", today)
            put("boite_nid_posee", 0)
            put("date_mise_bas_prevue", DateHelper.addDays(today, 3))
            put("statut", "gestante")
            put("notes", "Palpation nette. Poser la boîte à nid aujourd'hui !")
        })

        // 3. F03 : En saillie à J11 (Palpation abdominale à faire !)
        db.insert("cycles_reproduction", null, ContentValues().apply {
            put("femelle_id", f3)
            put("male_numero", "M01 - Rex")
            put("date_saillie", DateHelper.addDays(today, -11))
            put("date_palpation_prevue", today)
            put("date_boite_nid_prevue", DateHelper.addDays(today, 17))
            put("boite_nid_posee", 0)
            put("date_mise_bas_prevue", DateHelper.addDays(today, 20))
            put("statut", "saillie")
            put("notes", "Accouplement rapide et net dans la loge du mâle.")
        })
    }

    // ========== CLAPETS ==========

    fun getAllClapets(): List<Clapet> {
        val list = mutableListOf<Clapet>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT c.id, c.numero,
                   (SELECT f.numero FROM femelles f WHERE f.clapet_id = c.id AND f.statut = 'vivante' LIMIT 1) as occupante
            FROM clapets c
            ORDER BY c.numero
            """.trimIndent(),
            null
        )
        while (cursor.moveToNext()) {
            list.add(
                Clapet(
                    id = cursor.getLong(0),
                    numero = cursor.getString(1),
                    occupanteNumero = cursor.getStringOrNull(2)
                )
            )
        }
        cursor.close()
        return list
    }

    fun addClapet(numero: String): Long {
        val db = writableDatabase
        val cv = ContentValues().apply { put("numero", numero.trim().uppercase(Locale.ROOT)) }
        return db.insertWithOnConflict("clapets", null, cv, SQLiteDatabase.CONFLICT_IGNORE)
    }

    fun deleteClapet(id: Long) {
        writableDatabase.delete("clapets", "id = ?", arrayOf(id.toString()))
    }

    // ========== FEMELLES & CYCLES ==========

    fun getFemellesWithStatus(includeNonVivantes: Boolean = false): List<Femelle> {
        val list = mutableListOf<Femelle>()
        val db = readableDatabase
        val whereClause = if (includeNonVivantes) "" else "WHERE f.statut = 'vivante'"
        val sql = """
            SELECT 
                f.id, f.numero, f.clapet_id, c.numero as clapet_numero, f.date_naissance, f.statut,
                cr.id as c_id,
                cr.male_numero,
                cr.date_saillie,
                cr.date_palpation_prevue,
                cr.date_palpation_reelle,
                cr.resultat_palpation,
                cr.date_boite_nid_prevue,
                cr.boite_nid_posee,
                cr.date_mise_bas_prevue,
                cr.date_mise_bas_reelle,
                cr.nombre_vivants,
                cr.nombre_morts,
                cr.date_sevrage_prevue,
                cr.date_sevrage_reelle,
                cr.nombre_sevres,
                cr.statut as c_statut,
                cr.motif_echec,
                cr.notes
            FROM femelles f
            LEFT JOIN clapets c ON f.clapet_id = c.id
            LEFT JOIN cycles_reproduction cr ON cr.id = (
                SELECT id FROM cycles_reproduction 
                WHERE femelle_id = f.id AND statut IN ('saillie', 'gestante', 'allaitante')
                ORDER BY date_saillie DESC, id DESC LIMIT 1
            )
            $whereClause
            ORDER BY f.numero
        """.trimIndent()

        val cursor = db.rawQuery(sql, null)
        while (cursor.moveToNext()) {
            val fId = cursor.getLong(0)
            val cycleId = if (cursor.isNull(6)) null else cursor.getLong(6)
            val cycle = if (cycleId != null) {
                CycleReproduction(
                    id = cycleId,
                    femelleId = fId,
                    maleNumero = cursor.getStringOrNull(7),
                    dateSaillie = cursor.getString(8) ?: "",
                    datePalpationPrevue = cursor.getStringOrNull(9),
                    datePalpationReelle = cursor.getStringOrNull(10),
                    resultatPalpation = cursor.getStringOrNull(11),
                    dateBoiteNidPrevue = cursor.getStringOrNull(12),
                    boiteNidPosee = (cursor.getIntOrNull(13) ?: 0) == 1,
                    dateMiseBasPrevue = cursor.getString(14) ?: "",
                    dateMiseBasReelle = cursor.getStringOrNull(15),
                    nombreVivants = cursor.getIntOrNull(16),
                    nombreMorts = cursor.getIntOrNull(17),
                    dateSevragePrevue = cursor.getStringOrNull(18),
                    dateSevrageReelle = cursor.getStringOrNull(19),
                    nombreSevres = cursor.getIntOrNull(20),
                    statut = cursor.getString(21) ?: "saillie",
                    motifEchec = cursor.getStringOrNull(22),
                    notes = cursor.getStringOrNull(23)
                )
            } else null

            list.add(
                Femelle(
                    id = fId,
                    numero = cursor.getString(1) ?: "",
                    clapetId = if (cursor.isNull(2)) null else cursor.getLong(2),
                    clapetNumero = cursor.getStringOrNull(3),
                    dateNaissance = cursor.getStringOrNull(4),
                    statut = cursor.getString(5) ?: "vivante",
                    cycle = cycle
                )
            )
        }
        cursor.close()
        return list
    }

    fun getFemelleById(id: Long): Femelle? {
        return getFemellesWithStatus(includeNonVivantes = true).find { it.id == id }
    }

    fun addFemelle(numero: String, clapetId: Long?, dateNaissance: String?, statut: String): Long {
        val cv = ContentValues().apply {
            put("numero", numero.trim())
            if (clapetId != null) put("clapet_id", clapetId) else putNull("clapet_id")
            put("date_naissance", dateNaissance)
            put("statut", statut)
        }
        return writableDatabase.insert("femelles", null, cv)
    }

    fun updateFemelle(id: Long, numero: String, clapetId: Long?, dateNaissance: String?, statut: String) {
        val cv = ContentValues().apply {
            put("numero", numero.trim())
            if (clapetId != null) put("clapet_id", clapetId) else putNull("clapet_id")
            put("date_naissance", dateNaissance)
            put("statut", statut)
        }
        writableDatabase.update("femelles", cv, "id = ?", arrayOf(id.toString()))
    }

    fun deleteFemelle(id: Long) {
        writableDatabase.delete("femelles", "id = ?", arrayOf(id.toString()))
    }

    // ========== ACTIONS DE REPRODUCTION (CUNICULICULTURE) ==========

    fun startCycle(femelleId: Long, dateSaillie: String, maleNumero: String?, notes: String?): Long {
        val datePalpation = DateHelper.addDays(dateSaillie, 11)
        val dateBoiteNid = DateHelper.addDays(dateSaillie, 28)
        val dateMiseBas = DateHelper.addDays(dateSaillie, 31)

        val cv = ContentValues().apply {
            put("femelle_id", femelleId)
            put("male_numero", maleNumero?.takeIf { it.isNotBlank() })
            put("date_saillie", dateSaillie)
            put("date_palpation_prevue", datePalpation)
            put("date_boite_nid_prevue", dateBoiteNid)
            put("boite_nid_posee", 0)
            put("date_mise_bas_prevue", dateMiseBas)
            put("statut", "saillie")
            put("notes", notes?.takeIf { it.isNotBlank() })
        }
        return writableDatabase.insert("cycles_reproduction", null, cv)
    }

    fun verifyGestation(cycleId: Long, dateVerif: String, isPregnant: Boolean, notes: String?) {
        val existingNotes = getCycleNotes(cycleId)
        val mergedNotes = mergeNotes(existingNotes, notes)
        val cv = ContentValues().apply {
            put("statut", if (isPregnant) "gestante" else "echec")
            put("date_palpation_reelle", dateVerif)
            put("resultat_palpation", if (isPregnant) "positive" else "negative")
            if (!isPregnant) put("motif_echec", "non_gestante")
            put("notes", mergedNotes)
        }
        writableDatabase.update("cycles_reproduction", cv, "id = ?", arrayOf(cycleId.toString()))
    }

    fun confirmNestBox(cycleId: Long, placed: Boolean = true) {
        val cv = ContentValues().apply {
            put("boite_nid_posee", if (placed) 1 else 0)
        }
        writableDatabase.update("cycles_reproduction", cv, "id = ?", arrayOf(cycleId.toString()))
    }

    fun confirmBirth(cycleId: Long, dateNaissance: String, vivants: Int, morts: Int, notes: String?) {
        val dateSevrage = DateHelper.addDays(dateNaissance, 35)
        val existingNotes = getCycleNotes(cycleId)
        val mergedNotes = mergeNotes(existingNotes, notes)
        val cv = ContentValues().apply {
            put("date_mise_bas_reelle", dateNaissance)
            put("nombre_vivants", max(0, vivants))
            put("nombre_morts", max(0, morts))
            put("date_sevrage_prevue", dateSevrage)
            put("statut", "allaitante")
            put("notes", mergedNotes)
        }
        writableDatabase.update("cycles_reproduction", cv, "id = ?", arrayOf(cycleId.toString()))
    }

    fun confirmWeaning(cycleId: Long, dateSevrage: String, sevres: Int, notes: String?) {
        val existingNotes = getCycleNotes(cycleId)
        val mergedNotes = mergeNotes(existingNotes, notes)
        val cv = ContentValues().apply {
            put("date_sevrage_reelle", dateSevrage)
            put("nombre_sevres", max(0, sevres))
            put("statut", "termine")
            put("notes", mergedNotes)
        }
        writableDatabase.update("cycles_reproduction", cv, "id = ?", arrayOf(cycleId.toString()))
    }

    fun stopCycle(cycleId: Long, reason: String = "echec", motifEchec: String = "autre", notes: String?) {
        val existingNotes = getCycleNotes(cycleId)
        val mergedNotes = mergeNotes(existingNotes, notes)
        val cv = ContentValues().apply {
            put("statut", reason)
            put("motif_echec", motifEchec)
            put("notes", mergedNotes)
        }
        writableDatabase.update("cycles_reproduction", cv, "id = ?", arrayOf(cycleId.toString()))
    }

    private fun getCycleNotes(cycleId: Long): String? {
        val c = readableDatabase.rawQuery(
            "SELECT notes FROM cycles_reproduction WHERE id = ?",
            arrayOf(cycleId.toString())
        )
        val res = if (c.moveToFirst()) c.getStringOrNull(0) else null
        c.close()
        return res
    }

    private fun mergeNotes(existing: String?, addition: String?): String? {
        val add = addition?.trim()?.takeIf { it.isNotEmpty() } ?: return existing
        val ex = existing?.trim()?.takeIf { it.isNotEmpty() } ?: return add
        return "$ex | $add"
    }

    fun getReproductionAlerts(): List<ReproAlert> {
        val alerts = mutableListOf<ReproAlert>()
        val femelles = getFemellesWithStatus()

        for (f in femelles) {
            val c = f.cycle ?: continue
            when (c.statut) {
                "saillie" -> {
                    val elapsed = DateHelper.daysElapsed(c.dateSaillie)
                    if (elapsed >= 10) {
                        alerts.add(
                            ReproAlert(
                                type = ReproAlertType.PALPATION,
                                femelle = f,
                                cycle = c,
                                badge = "J$elapsed",
                                title = "🩺 ${f.numero} • Palpation à faire !",
                                subtitle = "Saillie du ${DateHelper.formatFr(c.dateSaillie)}${c.maleNumero?.let { " avec $it" } ?: ""}. Diagnostic de gestation requis.",
                                actionType = "verify",
                                elapsed = elapsed,
                                daysLeft = null
                            )
                        )
                    }
                }
                "gestante" -> {
                    val elapsed = DateHelper.daysElapsed(c.dateSaillie)
                    val daysLeft = DateHelper.daysUntil(c.dateMiseBasPrevue)
                    if (elapsed >= 27 && !c.boiteNidPosee) {
                        alerts.add(
                            ReproAlert(
                                type = ReproAlertType.BOITE_NID,
                                femelle = f,
                                cycle = c,
                                badge = "Urgent J$elapsed",
                                title = "📦 ${f.numero} • Poser la boîte à nid !",
                                subtitle = "Mise bas dans ${max(0, daysLeft ?: 3)}j. Garnir le nid de paille propre.",
                                actionType = "nest",
                                elapsed = elapsed,
                                daysLeft = daysLeft
                            )
                        )
                    }
                    if (daysLeft != null && daysLeft <= 2) {
                        val msg = when {
                            daysLeft < 0 -> "Mise bas dépassée (${abs(daysLeft)}j de retard)"
                            daysLeft == 0 -> "Mise bas prévue aujourd'hui !"
                            else -> "Mise bas dans $daysLeft jour(s)"
                        }
                        alerts.add(
                            ReproAlert(
                                type = ReproAlertType.MISE_BAS,
                                femelle = f,
                                cycle = c,
                                badge = "Naissance",
                                title = "🐰 ${f.numero} • $msg",
                                subtitle = "Surveiller le nid et enregistrer la portée dès la naissance.",
                                actionType = "birth",
                                elapsed = elapsed,
                                daysLeft = daysLeft
                            )
                        )
                    }
                }
                "allaitante" -> {
                    val elapsed = DateHelper.daysElapsed(c.dateMiseBasReelle)
                    if (elapsed >= 30) {
                        alerts.add(
                            ReproAlert(
                                type = ReproAlertType.SEVRAGE,
                                femelle = f,
                                cycle = c,
                                badge = "J$elapsed",
                                title = "🧺 ${f.numero} • Sevrage à effectuer",
                                subtitle = "${c.nombreVivants ?: 0} lapereaux prêts à être séparés de la mère.",
                                actionType = "wean",
                                elapsed = elapsed,
                                daysLeft = null
                            )
                        )
                    }
                }
            }
        }
        return alerts
    }

    fun getGlobalStats(): GlobalStats {
        val femelles = getFemellesWithStatus()
        var repos = 0
        var saillie = 0
        var gestantes = 0
        var allaitantes = 0
        var lapereaux = 0

        for (f in femelles) {
            when (f.cycle?.statut) {
                null -> repos++
                "saillie" -> saillie++
                "gestante" -> gestantes++
                "allaitante" -> {
                    allaitantes++
                    lapereaux += f.cycle.nombreVivants ?: 0
                }
            }
        }

        val clapets = getAllClapets()
        val remplis = clapets.count { it.occupanteNumero != null }

        return GlobalStats(
            totalFemelles = femelles.size,
            enRepos = repos,
            enSaillie = saillie,
            gestantes = gestantes,
            allaitantes = allaitantes,
            lapereauxSousMere = lapereaux,
            clapetsTotal = clapets.size,
            clapetsRemplis = remplis
        )
    }

    fun getFemelleHistory(femelleId: Long): List<CycleReproduction> {
        val list = mutableListOf<CycleReproduction>()
        val cursor = readableDatabase.rawQuery(
            """
            SELECT id, femelle_id, male_numero, date_saillie, date_palpation_prevue, date_palpation_reelle,
                   resultat_palpation, date_boite_nid_prevue, boite_nid_posee, date_mise_bas_prevue,
                   date_mise_bas_reelle, nombre_vivants, nombre_morts, date_sevrage_prevue,
                   date_sevrage_reelle, nombre_sevres, statut, motif_echec, notes
            FROM cycles_reproduction
            WHERE femelle_id = ?
            ORDER BY date_saillie DESC, id DESC
            """.trimIndent(),
            arrayOf(femelleId.toString())
        )
        while (cursor.moveToNext()) {
            list.add(
                CycleReproduction(
                    id = cursor.getLong(0),
                    femelleId = cursor.getLong(1),
                    maleNumero = cursor.getStringOrNull(2),
                    dateSaillie = cursor.getString(3) ?: "",
                    datePalpationPrevue = cursor.getStringOrNull(4),
                    datePalpationReelle = cursor.getStringOrNull(5),
                    resultatPalpation = cursor.getStringOrNull(6),
                    dateBoiteNidPrevue = cursor.getStringOrNull(7),
                    boiteNidPosee = (cursor.getIntOrNull(8) ?: 0) == 1,
                    dateMiseBasPrevue = cursor.getString(9) ?: "",
                    dateMiseBasReelle = cursor.getStringOrNull(10),
                    nombreVivants = cursor.getIntOrNull(11),
                    nombreMorts = cursor.getIntOrNull(12),
                    dateSevragePrevue = cursor.getStringOrNull(13),
                    dateSevrageReelle = cursor.getStringOrNull(14),
                    nombreSevres = cursor.getIntOrNull(15),
                    statut = cursor.getString(16) ?: "",
                    motifEchec = cursor.getStringOrNull(17),
                    notes = cursor.getStringOrNull(18)
                )
            )
        }
        cursor.close()
        return list
    }

    fun getFemelleStats(femelleId: Long): FemelleStats {
        val cycles = getFemelleHistory(femelleId)
        val totalSaillies = cycles.size
        val sailliesReussies = cycles.count {
            it.statut in listOf("gestante", "allaitante", "termine") || !it.dateMiseBasReelle.isNullOrBlank()
        }
        val totalMisesBas = cycles.count { !it.dateMiseBasReelle.isNullOrBlank() }
        val totalVivants = cycles.sumOf { it.nombreVivants ?: 0 }
        val totalMorts = cycles.sumOf { it.nombreMorts ?: 0 }
        val totalSevres = cycles.sumOf { it.nombreSevres ?: 0 }

        val tauxFertilite = if (totalSaillies > 0) {
            ((sailliesReussies.toDouble() / totalSaillies) * 100.0).roundToInt()
        } else 0

        val moyennePortee = if (totalMisesBas > 0) {
            String.format(Locale.US, "%.1f", totalVivants.toDouble() / totalMisesBas)
        } else "0.0"

        val cyclesTerminesVivants = cycles.filter { it.statut == "termine" }.sumOf { it.nombreVivants ?: 0 }
        val tauxSevrage = if (cyclesTerminesVivants > 0) {
            "${((totalSevres.toDouble() / cyclesTerminesVivants) * 100.0).roundToInt()}%"
        } else if (totalMisesBas > 0) {
            "En cours"
        } else {
            "0%"
        }

        return FemelleStats(
            totalSaillies = totalSaillies,
            totalMisesBas = totalMisesBas,
            totalVivants = totalVivants,
            totalMorts = totalMorts,
            totalSevres = totalSevres,
            tauxFertilite = tauxFertilite,
            moyennePortee = moyennePortee,
            tauxSevrage = tauxSevrage
        )
    }

    // ========== VACCINS & VACCINATIONS ==========

    fun getAllVaccins(): List<Vaccin> {
        val list = mutableListOf<Vaccin>()
        val cursor = readableDatabase.rawQuery("SELECT id, nom, duree_jours FROM vaccins ORDER BY nom", null)
        while (cursor.moveToNext()) {
            list.add(Vaccin(cursor.getLong(0), cursor.getString(1), cursor.getInt(2)))
        }
        cursor.close()
        return list
    }

    fun addVaccin(nom: String, dureeJours: Int): Long {
        val cv = ContentValues().apply {
            put("nom", nom.trim())
            put("duree_jours", max(1, dureeJours))
        }
        return writableDatabase.insert("vaccins", null, cv)
    }

    fun deleteVaccin(id: Long) {
        writableDatabase.delete("vaccins", "id = ?", arrayOf(id.toString()))
    }

    fun getVaccinationsByFemelle(femelleId: Long): List<VaccinationFemelle> {
        val list = mutableListOf<VaccinationFemelle>()
        val cursor = readableDatabase.rawQuery(
            """
            SELECT vf.id, vf.femelle_id, f.numero, vf.vaccin_id, v.nom, vf.date_vaccination, vf.date_prochain
            FROM vaccinations_femelles vf
            JOIN femelles f ON vf.femelle_id = f.id
            JOIN vaccins v ON vf.vaccin_id = v.id
            WHERE vf.femelle_id = ?
            ORDER BY vf.date_vaccination DESC
            """.trimIndent(),
            arrayOf(femelleId.toString())
        )
        while (cursor.moveToNext()) {
            list.add(
                VaccinationFemelle(
                    id = cursor.getLong(0),
                    femelleId = cursor.getLong(1),
                    femelleNumero = cursor.getString(2),
                    vaccinId = cursor.getLong(3),
                    vaccinNom = cursor.getString(4),
                    dateVaccination = cursor.getString(5),
                    dateProchain = cursor.getString(6)
                )
            )
        }
        cursor.close()
        return list
    }

    fun addVaccination(femelleId: Long, vaccinId: Long, dateVaccination: String): Long {
        var duree = 365
        val c = readableDatabase.rawQuery("SELECT duree_jours FROM vaccins WHERE id = ?", arrayOf(vaccinId.toString()))
        if (c.moveToFirst()) duree = c.getInt(0)
        c.close()

        val dateProchain = DateHelper.addDays(dateVaccination, duree)
        val cv = ContentValues().apply {
            put("femelle_id", femelleId)
            put("vaccin_id", vaccinId)
            put("date_vaccination", dateVaccination)
            put("date_prochain", dateProchain)
        }
        return writableDatabase.insert("vaccinations_femelles", null, cv)
    }

    fun getVaccineAlerts(): List<VaccinationFemelle> {
        val list = mutableListOf<VaccinationFemelle>()
        val in7Days = DateHelper.addDays(DateHelper.todayIso(), 7)
        val cursor = readableDatabase.rawQuery(
            """
            SELECT vf.id, vf.femelle_id, f.numero, vf.vaccin_id, v.nom, vf.date_vaccination, vf.date_prochain
            FROM vaccinations_femelles vf
            JOIN femelles f ON vf.femelle_id = f.id
            JOIN vaccins v ON vf.vaccin_id = v.id
            WHERE f.statut = 'vivante' AND vf.date_prochain <= ?
            ORDER BY vf.date_prochain ASC
            """.trimIndent(),
            arrayOf(in7Days)
        )
        while (cursor.moveToNext()) {
            list.add(
                VaccinationFemelle(
                    id = cursor.getLong(0),
                    femelleId = cursor.getLong(1),
                    femelleNumero = cursor.getString(2),
                    vaccinId = cursor.getLong(3),
                    vaccinNom = cursor.getString(4),
                    dateVaccination = cursor.getString(5),
                    dateProchain = cursor.getString(6)
                )
            )
        }
        cursor.close()
        return list
    }

    // ========== ALIMENTS ==========

    fun getAllAliments(): List<Aliment> {
        val totalVivantes = getFemellesWithStatus().size
        val list = mutableListOf<Aliment>()
        val cursor = readableDatabase.rawQuery(
            "SELECT id, nom, stock_kg, consommation_g FROM aliments ORDER BY nom",
            null
        )
        while (cursor.moveToNext()) {
            val stockKg = cursor.getDouble(2)
            val consoG = cursor.getDouble(3)
            val consoJourKg = (totalVivantes * consoG) / 1000.0
            val joursRestants = if (consoJourKg > 0.0) floor(stockKg / consoJourKg).toInt() else 999
            list.add(
                Aliment(
                    id = cursor.getLong(0),
                    nom = cursor.getString(1),
                    stockKg = stockKg,
                    consommationG = consoG,
                    consoJourKg = consoJourKg,
                    joursRestants = joursRestants
                )
            )
        }
        cursor.close()
        return list
    }

    fun addOrUpdateAliment(id: Long?, nom: String, stockKg: Double, consommationG: Double) {
        val cv = ContentValues().apply {
            put("nom", nom.trim())
            put("stock_kg", max(0.0, stockKg))
            put("consommation_g", max(0.0, consommationG))
        }
        if (id == null) {
            writableDatabase.insert("aliments", null, cv)
        } else {
            writableDatabase.update("aliments", cv, "id = ?", arrayOf(id.toString()))
        }
    }

    fun deleteAliment(id: Long) {
        writableDatabase.delete("aliments", "id = ?", arrayOf(id.toString()))
    }

    private fun Cursor.getStringOrNull(index: Int): String? =
        if (isNull(index)) null else getString(index)

    private fun Cursor.getIntOrNull(index: Int): Int? =
        if (isNull(index)) null else getInt(index)
}

