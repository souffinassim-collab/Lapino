package com.lapino.gestion.model

import com.lapino.gestion.i18n.AppStrings
import com.lapino.gestion.i18n.AppStringsFR
import com.lapino.gestion.i18n.menu
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

data class Clapet(
    val id: Long,
    val numero: String,
    val occupanteNumero: String? = null
)

data class CycleReproduction(
    val id: Long,
    val femelleId: Long,
    val maleNumero: String?,
    val dateSaillie: String,
    val datePalpationPrevue: String?,
    val datePalpationReelle: String?,
    val resultatPalpation: String?, // "positive", "negative", "douteuse"
    val dateBoiteNidPrevue: String?,
    val boiteNidPosee: Boolean,
    val dateMiseBasPrevue: String,
    val dateMiseBasReelle: String?,
    val nombreVivants: Int?,
    val nombreMorts: Int?,
    val dateSevragePrevue: String?,
    val dateSevrageReelle: String?,
    val nombreSevres: Int?,
    val statut: String, // "saillie", "gestante", "allaitante", "termine", "echec"
    val motifEchec: String?,
    val notes: String?
)

data class Femelle(
    val id: Long,
    val numero: String,
    val clapetId: Long?,
    val clapetNumero: String?,
    val dateNaissance: String?,
    val statut: String, // "vivante", "vendue", "morte"
    val cycle: CycleReproduction? = null,
    /** Portée encore sous la mère alors qu'un nouveau cycle (saillie/gestante) a déjà commencé. */
    val portee: CycleReproduction? = null
) {
    /** Cycle d'allaitement en cours, qu'il soit le cycle courant ou la portée précédente. */
    val nursing: CycleReproduction?
        get() = portee ?: cycle?.takeIf { it.statut == "allaitante" }
}

/** Règles du calendrier de reproduction (jours). */
object CycleRules {
    const val PALPATION_DAYS = 10
    const val NEST_DAYS = 28
    const val GESTATION_DAYS = 31
    const val WEAN_EARLY_DAYS = 28
    const val WEAN_LATE_DAYS = 35
    val REMATE_OPTIONS = listOf(11, 18, 25, 35)

    /** Jours après la mise bas à partir desquels la remise au mâle est conseillée (réglable). */
    @Volatile
    var remateDays: Int = 11
}

data class Male(
    val id: Long,
    val numero: String,
    val race: String?,
    val dateNaissance: String?,
    val totalSaillies: Int = 0,
    val totalMisesBas: Int = 0
)

data class Vente(
    val id: Long,
    val date: String,
    val nombre: Int,
    val prixUnitaire: Double,
    val notes: String?
) {
    val total: Double get() = nombre * prixUnitaire
}

data class AchatAliment(
    val id: Long,
    val alimentId: Long?,
    val alimentNom: String?,
    val date: String,
    val quantiteKg: Double,
    val prix: Double
)

data class Vaccin(
    val id: Long,
    val nom: String,
    val dureeJours: Int
)

data class VaccinationFemelle(
    val id: Long,
    val femelleId: Long,
    val femelleNumero: String,
    val vaccinId: Long,
    val vaccinNom: String,
    val dateVaccination: String,
    val dateProchain: String
)

data class Aliment(
    val id: Long,
    val nom: String,
    val stockKg: Double,
    val consommationG: Double,
    val consoJourKg: Double = 0.0,
    val joursRestants: Int = 999
)

data class FemelleStats(
    val totalSaillies: Int = 0,
    val totalMisesBas: Int = 0,
    val totalVivants: Int = 0,
    val totalMorts: Int = 0,
    val totalSevres: Int = 0,
    val tauxFertilite: Int = 0,
    val moyennePortee: String = "0.0",
    val tauxSevrage: String = "0%"
)

enum class ReproAlertType {
    PALPATION,
    BOITE_NID,
    MISE_BAS,
    SEVRAGE,
    REMISE_MALE
}

data class ReproAlert(
    val type: ReproAlertType,
    val femelle: Femelle,
    val cycle: CycleReproduction,
    val badge: String = "",
    val title: String = "",
    val subtitle: String = "",
    val actionType: String, // "verify", "nest", "birth", "wean"
    val elapsed: Int = 0,
    val daysLeft: Int? = null
) {
    fun getBadge(strings: AppStrings): String = when (type) {
        ReproAlertType.PALPATION -> strings.alertBadgeDay(elapsed)
        ReproAlertType.BOITE_NID -> strings.alertBadgeUrgent(elapsed)
        ReproAlertType.MISE_BAS -> strings.alertBadgeBirth
        ReproAlertType.SEVRAGE -> strings.alertBadgeDay(elapsed)
        ReproAlertType.REMISE_MALE -> strings.alertBadgeDay(elapsed)
    }

    fun getTitle(strings: AppStrings): String = when (type) {
        ReproAlertType.PALPATION -> strings.alertTitlePalpation(femelle.numero)
        ReproAlertType.BOITE_NID -> strings.alertTitleNest(femelle.numero)
        ReproAlertType.MISE_BAS -> {
            val dl = daysLeft ?: 0
            when {
                dl < 0 -> strings.alertTitleBirthOverdue(femelle.numero, abs(dl))
                dl == 0 -> strings.alertTitleBirthToday(femelle.numero)
                else -> strings.alertTitleBirthInDays(femelle.numero, dl)
            }
        }
        ReproAlertType.SEVRAGE -> strings.alertTitleWeaning(femelle.numero)
        ReproAlertType.REMISE_MALE -> strings.menu.alertRemateTitle(femelle.numero)
    }

    fun getSubtitle(strings: AppStrings): String = when (type) {
        ReproAlertType.PALPATION -> strings.alertSubPalpation(DateHelper.formatFr(cycle.dateSaillie), cycle.maleNumero)
        ReproAlertType.BOITE_NID -> strings.alertSubNest(max(0, daysLeft ?: 3))
        ReproAlertType.MISE_BAS -> strings.alertSubBirth
        ReproAlertType.SEVRAGE -> strings.alertSubWeaning(cycle.nombreVivants ?: 0)
        ReproAlertType.REMISE_MALE -> strings.menu.alertRemateSub(elapsed)
    }
}

data class GlobalStats(
    val totalFemelles: Int = 0,
    val enRepos: Int = 0,
    val enSaillie: Int = 0,
    val gestantes: Int = 0,
    val allaitantes: Int = 0,
    val lapereauxSousMere: Int = 0,
    val clapetsTotal: Int = 0,
    val clapetsRemplis: Int = 0
)

object DateHelper {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val frFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)

    fun todayIso(): String = isoFormat.format(Date())

    fun parseIso(dateStr: String?): Date? {
        if (dateStr.isNullOrBlank()) return null
        return try {
            isoFormat.parse(dateStr.trim())
        } catch (e: Exception) {
            null
        }
    }

    fun addDays(isoDate: String, days: Int): String {
        val d = parseIso(isoDate) ?: Date()
        val cal = Calendar.getInstance()
        cal.time = d
        cal.add(Calendar.DAY_OF_YEAR, days)
        return isoFormat.format(cal.time)
    }

    fun formatFr(isoDate: String?): String {
        val d = parseIso(isoDate) ?: return "-"
        return frFormat.format(d)
    }

    fun daysElapsed(startIso: String?): Int {
        val start = parseIso(startIso) ?: return 0
        val calStart = Calendar.getInstance().apply {
            time = start
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val calNow = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffMs = calNow.timeInMillis - calStart.timeInMillis
        return max(0, TimeUnit.MILLISECONDS.toDays(diffMs).toInt())
    }

    fun daysUntil(targetIso: String?): Int? {
        val target = parseIso(targetIso) ?: return null
        val calTarget = Calendar.getInstance().apply {
            time = target
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val calNow = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffMs = calTarget.timeInMillis - calNow.timeInMillis
        return ceil(diffMs.toDouble() / (1000 * 60 * 60 * 24)).toInt()
    }

    fun calculateProgress(startIso: String?, endIso: String?): Float {
        val start = parseIso(startIso)?.time ?: return 0f
        val end = parseIso(endIso)?.time ?: return 0f
        val now = System.currentTimeMillis()
        if (end <= start) return 0f
        if (now <= start) return 0f
        if (now >= end) return 1f
        val ratio = (now - start).toFloat() / (end - start).toFloat()
        return max(0f, min(1f, ratio))
    }

    fun calculateAgeLabel(birthIso: String?, strings: AppStrings = AppStringsFR): String {
        val birth = parseIso(birthIso) ?: return strings.unknown
        val calBirth = Calendar.getInstance().apply { time = birth }
        val calNow = Calendar.getInstance()
        val months = (calNow.get(Calendar.YEAR) - calBirth.get(Calendar.YEAR)) * 12 +
                (calNow.get(Calendar.MONTH) - calBirth.get(Calendar.MONTH))
        if (months < 1) return strings.ageUnderMonth
        if (months < 12) return strings.ageMonths(months)
        val years = months / 12
        val remMonths = months % 12
        return if (remMonths > 0) strings.ageYearsAndMonths(years, remMonths) else strings.ageYears(years)
    }
}

