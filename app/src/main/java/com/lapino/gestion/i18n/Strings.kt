package com.lapino.gestion.i18n

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.LayoutDirection
import java.util.Locale

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val flag: String,
    val layoutDirection: LayoutDirection
) {
    FR("fr", "Français", "🇫🇷", LayoutDirection.Ltr),
    AR("ar", "العربية", "🇸🇦", LayoutDirection.Rtl)
}

object LanguageManager {
    private const val PREFS_NAME = "lapino_prefs"
    private const val KEY_LANG = "selected_language"

    fun getSavedLanguage(context: Context): AppLanguage {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val code = prefs.getString(KEY_LANG, null)
        return if (code != null) {
            AppLanguage.values().find { it.code == code } ?: AppLanguage.FR
        } else {
            if (Locale.getDefault().language == "ar") AppLanguage.AR else AppLanguage.FR
        }
    }

    fun saveLanguage(context: Context, lang: AppLanguage) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, lang.code).apply()
    }
}

val LocalAppStrings = staticCompositionLocalOf<AppStrings> { AppStringsFR }
val LocalAppLanguage = staticCompositionLocalOf<AppLanguage> { AppLanguage.FR }

interface AppStrings {
    val lang: AppLanguage

    // Top Bar & App
    val appTitle: String
    val femaleDetailTitle: String
    val back: String
    val themeToggleDesc: String
    val langToggleDesc: String

    // Bottom Navigation & Actions
    val navHome: String
    fun navRabbits(count: Int): String
    val navElevage: String
    val navVaccines: String
    val fabNewRabbit: String

    // Dashboard Overview
    val statActiveRabbits: String
    fun statOccupiedCages(occupied: Int, total: Int): String
    val statKitsInNest: String
    fun statNursingMothers(count: Int): String
    fun pillRest(count: Int): String
    fun pillMating(count: Int): String
    fun pillPregnant(count: Int): String
    fun pillNursing(count: Int): String
    fun sectionPriorityActions(count: Int): String
    fun sectionLowFeedStock(count: Int): String
    fun sectionVaccineReminders(count: Int): String
    fun sectionMyRabbits(count: Int): String

    fun feedStockInfo(stockKg: Double, dailyKg: Double): String
    fun feedDaysRemaining(days: Int): String
    fun vaccineDueDate(dateStr: String): String
    val overdue: String
    fun inDays(days: Int): String

    // Rabbit Card / Status
    val statusRest: String
    fun statusMatingElapsed(days: Int): String
    fun statusPregnantElapsed(days: Int): String
    fun statusNursingElapsed(days: Int): String
    fun cageAssigned(num: String): String
    val cageUnassigned: String
    val readyForMating: String
    fun matingDetail(date: String, male: String?): String
    fun birthOverdueDays(days: Int): String
    val birthDueToday: String
    fun birthDueInDays(days: Int?, date: String): String
    fun nursingDetail(vivants: Int, date: String): String

    fun alertPalpationNow(days: Int): String
    fun alertPalpationAdvised(days: Int): String
    fun alertNestUrgent(days: Int): String
    val alertNestInstalled: String
    fun alertWeaningRecommended(days: Int): String
    val alertKitsEatingSolid: String

    // Action Labels
    val actionMate: String
    val actionPalpate: String
    val actionDeclareBirth: String
    val actionWean: String
    val actionDoWean: String
    val actionNestPlaced: String
    val incidentDescription: String

    // Filters & Search
    val searchPlaceholder: String
    fun filterAll(count: Int): String
    val filterRest: String
    val filterMating: String
    val filterPregnant: String
    val filterNursing: String

    // Detail Screen
    val edit: String
    val estimatedAge: String
    val birthDate: String
    val generalStatus: String
    val statusLiving: String
    val statusSold: String
    val statusDead: String
    val currentCycleHeader: String
    val zootechnicalSummaryHeader: String
    val kpiFertility: String
    val kpiLitters: String
    val kpiAlivePerLitter: String
    val kpiWeaned: String
    fun bornSummary(vivants: Int, morts: Int): String
    fun weaningSurvivalRate(rate: String): String
    fun historyHeader(count: Int): String
    val noCyclesRecorded: String
    fun cycleMatingLine(date: String, male: String?): String
    val statusWeanedSuccess: String
    val statusFailed: String
    fun cycleBirthLine(date: String, vivants: Int, morts: Int): String
    fun cycleWeanedLine(count: Int, pct: String): String
    fun cycleReason(reason: String): String
    fun cycleNotes(notes: String): String
    fun vaccinationsSectionHeader(count: Int): String
    val vaccinateBtn: String
    val noVaccinationsRecorded: String
    fun vaccinationLine(doneDate: String, nextDate: String): String
    val deleteRabbitBtn: String
    fun deleteRabbitTitle(num: String): String
    val deleteRabbitMessage: String
    val confirmDelete: String
    val cancel: String
    val save: String
    val close: String

    // Dialogs: Add/Edit Rabbit
    val dialogNewRabbitTitle: String
    fun dialogEditRabbitTitle(num: String): String
    val inputRabbitName: String
    val inputRabbitNamePlaceholder: String
    val inputBirthDate: String
    val inputCageSelect: String
    val noneOption: String

    // Dialogs: Reproduction
    val dialogMatingTitle: String
    fun rabbitIndicator(num: String): String
    val matingTip: String
    val inputMatingDate: String
    val inputMale: String
    val inputMalePlaceholder: String
    val inputObsOptional: String
    val btnValidateMating: String

    val dialogPalpationTitle: String
    val palpationTip: String
    val inputControlDate: String
    val btnPregnantYes: String
    val btnPregnantNo: String

    val dialogNestTitle: String
    val nestTip: String
    val btnConfirmNest: String

    val dialogBirthTitle: String
    val birthTip: String
    val inputDeliveryDate: String
    val inputBornAlive: String
    val inputStillborn: String
    val inputLitterNotes: String
    val btnSaveBirth: String

    val dialogWeaningTitle: String
    val weaningTip: String
    val inputWeaningDate: String
    val inputWeanedAlive: String
    fun nestSurvivalRate(weaned: Int, alive: Int, pct: Int): String
    val inputWeaningNotes: String
    val btnCloseCycleSuccess: String

    val dialogIncidentTitle: String
    val motifMiscarriage: String
    val motifNotPregnant: String
    val motifLitterMortality: String
    val motifAbandon: String
    val motifOther: String
    val inputIncidentDetails: String
    val btnConfirmStopCycle: String

    // Stocks & Clapets
    val feedStockHeader: String
    val btnAddFeed: String
    fun feedStockRow(stockKg: Double, rationG: Int): String
    fun feedAutonomyRow(days: Int): String
    fun cagesManagementHeader(count: Int): String
    val inputNewCage: String
    val btnAdd: String
    fun cageTitle(num: String): String
    fun cageOccupiedBy(num: String): String
    val cageEmpty: String
    val dialogAddFeedTitle: String
    val dialogEditFeedTitle: String
    val inputFeedName: String
    val inputFeedStock: String
    val inputFeedDailyRation: String

    // Vaccines Tab
    val vaccinesRefHeader: String
    val inputVaccineName: String
    val inputValidityDays: String
    fun vaccineReminderInterval(days: Int): String
    val dialogRecordVaccinationTitle: String
    val inputVaccinationDate: String

    // Date & Age
    val ageUnderMonth: String
    fun ageMonths(m: Int): String
    fun ageYearsAndMonths(y: Int, m: Int): String
    fun ageYears(y: Int): String
    val unknown: String

    // Reproduction Alert Helpers
    fun alertBadgeDay(day: Int): String
    fun alertBadgeUrgent(day: Int): String
    val alertBadgeBirth: String
    fun alertTitlePalpation(num: String): String
    fun alertSubPalpation(date: String, male: String?): String
    fun alertTitleNest(num: String): String
    fun alertSubNest(days: Int): String
    fun alertTitleBirthOverdue(num: String, days: Int): String
    fun alertTitleBirthToday(num: String): String
    fun alertTitleBirthInDays(num: String, days: Int): String
    val alertSubBirth: String
    fun alertTitleWeaning(num: String): String
    fun alertSubWeaning(count: Int): String
}

object AppStringsFR : AppStrings {
    override val lang: AppLanguage = AppLanguage.FR

    override val appTitle: String = "🐰 Lapino Élevage"
    override val femaleDetailTitle: String = "Fiche Lapine"
    override val back: String = "Retour"
    override val themeToggleDesc: String = "Thème clair/sombre"
    override val langToggleDesc: String = "Changer de langue"

    override val navHome: String = "Accueil"
    override fun navRabbits(count: Int): String = "Lapines ($count)"
    override val navElevage: String = "Clapets & Stocks"
    override val navVaccines: String = "Vaccins"
    override val fabNewRabbit: String = "Lapine"

    override val statActiveRabbits: String = "Lapines actives"
    override fun statOccupiedCages(occupied: Int, total: Int): String = "$occupied/$total loges occupées"
    override val statKitsInNest: String = "Lapereaux au nid"
    override fun statNursingMothers(count: Int): String = "$count mère(s) allaitante(s)"
    override fun pillRest(count: Int): String = "💤 Repos: $count"
    override fun pillMating(count: Int): String = "❤️ Saillie: $count"
    override fun pillPregnant(count: Int): String = "🤰 Gestante: $count"
    override fun pillNursing(count: Int): String = "🍼 Allaitante: $count"
    override fun sectionPriorityActions(count: Int): String = "🚨 Actions Élevage Prioritaires ($count)"
    override fun sectionLowFeedStock(count: Int): String = "⚠️ Stock Alimentation Bas ($count)"
    override fun sectionVaccineReminders(count: Int): String = "💉 Rappels Vaccins ($count)"
    override fun sectionMyRabbits(count: Int): String = "Mes Lapines Reproductrices ($count)"

    override fun feedStockInfo(stockKg: Double, dailyKg: Double): String =
        String.format(Locale.FRANCE, "%.1f kg en stock (%.2f kg/j)", stockKg, dailyKg)
    override fun feedDaysRemaining(days: Int): String = "Reste $days j"
    override fun vaccineDueDate(dateStr: String): String = "Échéance : $dateStr"
    override val overdue: String = "En retard"
    override fun inDays(days: Int): String = "Dans ${days}j"

    override val statusRest: String = "En repos"
    override fun statusMatingElapsed(days: Int): String = "Saillie (J$days)"
    override fun statusPregnantElapsed(days: Int): String = "Gestante (J$days/31)"
    override fun statusNursingElapsed(days: Int): String = "Allaitante (J$days/35)"
    override fun cageAssigned(num: String): String = "Loge : Clapet $num"
    override val cageUnassigned: String = "Loge : Non assignée"
    override val readyForMating: String = "Prête pour une nouvelle saillie"
    override fun matingDetail(date: String, male: String?): String =
        "Accouplée le $date" + (male?.let { " • Mâle: $it" } ?: "")
    override fun birthOverdueDays(days: Int): String = "⚠️ Mise bas dépassée de $days jour(s) !"
    override val birthDueToday: String = "🐰 Mise bas prévue aujourd'hui !"
    override fun birthDueInDays(days: Int?, date: String): String =
        "Mise bas prévue dans ${days ?: "?"}j ($date)"
    override fun nursingDetail(vivants: Int, date: String): String =
        "$vivants lapereau(x) vivant(s) • Nés le $date"

    override fun alertPalpationNow(days: Int): String =
        "🔍 Palpation à réaliser ! (J$days - ampoules fœtales palpables)"
    override fun alertPalpationAdvised(days: Int): String =
        "Palpation conseillée à J11 (dans ${days}j)"
    override fun alertNestUrgent(days: Int): String =
        "📦 Urgent (J$days) : Poser la boîte à nid avec paille !"
    override val alertNestInstalled: String = "✓ Boîte à nid propre installée"
    override fun alertWeaningRecommended(days: Int): String =
        "🧺 Sevrage recommandé (J$days - prêts pour l'engraissement)"
    override val alertKitsEatingSolid: String =
        "Sortie du nid : les petits commencent à manger du solide"

    override val actionMate: String = "Mettre au mâle"
    override val actionPalpate: String = "Palpation (Diagnostic)"
    override val actionDeclareBirth: String = "Déclarer Naissance"
    override val actionWean: String = "Sevrage"
    override val actionDoWean: String = "Effectuer Sevrage"
    override val actionNestPlaced: String = "Nid posé ✓"
    override val incidentDescription: String = "Signaler un incident"

    override val searchPlaceholder: String = "Rechercher par nom ou clapet..."
    override fun filterAll(count: Int): String = "Toutes ($count)"
    override val filterRest: String = "Repos"
    override val filterMating: String = "Saillie"
    override val filterPregnant: String = "Gestante"
    override val filterNursing: String = "Allaitante"

    override val edit: String = "Modifier"
    override val estimatedAge: String = "Âge estimé :"
    override val birthDate: String = "Date de naissance :"
    override val generalStatus: String = "Statut général :"
    override val statusLiving: String = "VIVANTE"
    override val statusSold: String = "VENDUE"
    override val statusDead: String = "MORTE"
    override val currentCycleHeader: String = "🌪️ Cycle Reproductif Actuel"
    override val zootechnicalSummaryHeader: String = "📊 Bilan Zootechnique de la Lapine"
    override val kpiFertility: String = "Fertilité"
    override val kpiLitters: String = "Portées"
    override val kpiAlivePerLitter: String = "Vivants/portée"
    override val kpiWeaned: String = "Sevrés"
    override fun bornSummary(vivants: Int, morts: Int): String =
        "Nés : $vivants vivants • $morts mort-nés"
    override fun weaningSurvivalRate(rate: String): String = "Survie sevrage : $rate"
    override fun historyHeader(count: Int): String = "📜 Historique des Cycles & Portées ($count)"
    override val noCyclesRecorded: String = "Aucun cycle enregistré."
    override fun cycleMatingLine(date: String, male: String?): String =
        "Saillie : $date" + (male?.let { " • Mâle: $it" } ?: "")
    override val statusWeanedSuccess: String = "Sevré ✓"
    override val statusFailed: String = "Échec"
    override fun cycleBirthLine(date: String, vivants: Int, morts: Int): String =
        "🐰 Mise bas : $date • $vivants vivants, $morts mort-nés"
    override fun cycleWeanedLine(count: Int, pct: String): String =
        "🧺 Sevrés : $count lapereaux$pct"
    override fun cycleReason(reason: String): String = "Motif : $reason"
    override fun cycleNotes(notes: String): String = "Obs : $notes"
    override fun vaccinationsSectionHeader(count: Int): String = "💉 Vaccinations ($count)"
    override val vaccinateBtn: String = "+ Vacciner"
    override val noVaccinationsRecorded: String = "Aucune vaccination enregistrée."
    override fun vaccinationLine(doneDate: String, nextDate: String): String =
        "Fait: $doneDate → Rappel: $nextDate"
    override val deleteRabbitBtn: String = "Supprimer cette lapine"
    override fun deleteRabbitTitle(num: String): String = "Supprimer $num ?"
    override val deleteRabbitMessage: String =
        "Cette action supprimera la lapine et tout son historique de reproduction."
    override val confirmDelete: String = "Supprimer"
    override val cancel: String = "Annuler"
    override val save: String = "Enregistrer"
    override val close: String = "Fermer"

    override val dialogNewRabbitTitle: String = "🐰 Nouvelle Lapine"
    override fun dialogEditRabbitTitle(num: String): String = "Modifier $num"
    override val inputRabbitName: String = "Nom / Numéro d'identification"
    override val inputRabbitNamePlaceholder: String = "Ex: F05 - Argentée"
    override val inputBirthDate: String = "Date de naissance (AAAA-MM-JJ)"
    override val inputCageSelect: String = "Clapet / Loge :"
    override val noneOption: String = "Aucun"

    override val dialogMatingTitle: String = "🌪️ Déclarer une Saillie"
    override fun rabbitIndicator(num: String): String = "Lapine : $num"
    override val matingTip: String =
        "💡 Conseil : Amenez toujours la lapine dans la loge du mâle (jamais l'inverse). La palpation abdominale sera programmée à J+11."
    override val inputMatingDate: String = "Date de saillie (AAAA-MM-JJ)"
    override val inputMale: String = "Mâle reproducteur (Nom / N°)"
    override val inputMalePlaceholder: String = "Ex: M01 - Rex"
    override val inputObsOptional: String = "Observations (Optionnel)"
    override val btnValidateMating: String = "Valider la Saillie"

    override val dialogPalpationTitle: String = "🩺 Palpation Abdominale"
    override val palpationTip: String =
        "💡 Palpez délicatement le bas-ventre entre le pouce et les doigts. Entre J10 et J14, les embryons forment un chapelet de billes de la taille d'une noisette."
    override val inputControlDate: String = "Date du contrôle (AAAA-MM-JJ)"
    override val btnPregnantYes: String = "✅ OUI - Gestante confirmée"
    override val btnPregnantNo: String = "❌ NON - Vide (Remettre en repos)"

    override val dialogNestTitle: String = "📦 Pose de la Boîte à Nid"
    override val nestTip: String =
        "💡 À J28 de gestation (3 jours avant la mise bas), installez une boîte à nid propre garnie de copeaux et de paille sèche. La lapine s'arrachera les poils du ventre pour le capitonner."
    override val btnConfirmNest: String = "Confirmer l'installation du nid ✓"

    override val dialogBirthTitle: String = "🐰 Déclarer la Mise Bas"
    override val birthTip: String =
        "💡 Contrôlez le nid au calme : retirez les éventuels mort-nés et vérifiez que les ventres des petits sont bien rebondis et blancs de lait."
    override val inputDeliveryDate: String = "Date de mise bas (AAAA-MM-JJ)"
    override val inputBornAlive: String = "Nés vivants"
    override val inputStillborn: String = "Mort-nés"
    override val inputLitterNotes: String = "Notes sur la portée (Optionnel)"
    override val btnSaveBirth: String = "Enregistrer la Naissance"

    override val dialogWeaningTitle: String = "🧺 Déclarer le Sevrage"
    override val weaningTip: String =
        "💡 Le sevrage (30 à 35 jours) sépare les lapereaux de la mère vers l'engraissement. La lapine retourne au statut Repos."
    override val inputWeaningDate: String = "Date de sevrage (AAAA-MM-JJ)"
    override val inputWeanedAlive: String = "Lapereaux sevrés vivants"
    override fun nestSurvivalRate(weaned: Int, alive: Int, pct: Int): String =
        "Taux de survie au nid : $weaned / $alive petits ($pct%)"
    override val inputWeaningNotes: String = "Notes / Cage engraissement (Optionnel)"
    override val btnCloseCycleSuccess: String = "Clôturer le Cycle (Sevrage Réussi)"

    override val dialogIncidentTitle: String = "⚠️ Signaler un Incident"
    override val motifMiscarriage: String = "Fausse couche / avortement"
    override val motifNotPregnant: String = "Non gestante (vide)"
    override val motifLitterMortality: String = "Mortalité complète de la portée"
    override val motifAbandon: String = "Abandon du nid / manque de lait"
    override val motifOther: String = "Autre incident"
    override val inputIncidentDetails: String = "Détails de l'incident (Optionnel)"
    override val btnConfirmStopCycle: String = "Confirmer et Remettre en Repos"

    override val feedStockHeader: String = "🌾 Stocks d'Alimentation"
    override val btnAddFeed: String = "+ Aliment"
    override fun feedStockRow(stockKg: Double, rationG: Int): String =
        "Stock : $stockKg kg • Ration : $rationG g/j/lapine"
    override fun feedAutonomyRow(days: Int): String = "Autonomie estimée : $days jour(s)"
    override fun cagesManagementHeader(count: Int): String = "🏠 Gestion des Clapets ($count)"
    override val inputNewCage: String = "Nouveau clapet (ex: C1)"
    override val btnAdd: String = "Ajouter"
    override fun cageTitle(num: String): String = "Loge / Clapet $num"
    override fun cageOccupiedBy(num: String): String = "Occupé par : $num"
    override val cageEmpty: String = "Libre / Vide"
    override val dialogAddFeedTitle: String = "Ajouter un Aliment"
    override val dialogEditFeedTitle: String = "Modifier l'Aliment"
    override val inputFeedName: String = "Nom de l'aliment"
    override val inputFeedStock: String = "Stock disponible (en kg)"
    override val inputFeedDailyRation: String = "Ration quotidienne par lapine (en g)"

    override val vaccinesRefHeader: String = "💉 Référentiel des Vaccins"
    override val inputVaccineName: String = "Nom du vaccin (ex: VHD)"
    override val inputValidityDays: String = "Validité (j)"
    override fun vaccineReminderInterval(days: Int): String = "Rappel tous les $days jours"
    override val dialogRecordVaccinationTitle: String = "💉 Enregistrer une Vaccination"
    override val inputVaccinationDate: String = "Date de vaccination (AAAA-MM-JJ)"

    override val ageUnderMonth: String = "Moins d'un mois"
    override fun ageMonths(m: Int): String = "$m mois"
    override fun ageYearsAndMonths(y: Int, m: Int): String = "$y an(s) et $m mois"
    override fun ageYears(y: Int): String = "$y an(s)"
    override val unknown: String = "Inconnu"

    override fun alertBadgeDay(day: Int): String = "J$day"
    override fun alertBadgeUrgent(day: Int): String = "Urgent J$day"
    override val alertBadgeBirth: String = "Naissance"
    override fun alertTitlePalpation(num: String): String = "🩺 $num • Palpation à faire !"
    override fun alertSubPalpation(date: String, male: String?): String =
        "Saillie du $date${male?.let { " avec $it" } ?: ""}. Diagnostic de gestation requis."
    override fun alertTitleNest(num: String): String = "📦 $num • Poser la boîte à nid !"
    override fun alertSubNest(days: Int): String =
        "Mise bas dans ${days}j. Garnir le nid de paille propre."
    override fun alertTitleBirthOverdue(num: String, days: Int): String =
        "🐰 $num • Mise bas dépassée (${days}j de retard)"
    override fun alertTitleBirthToday(num: String): String =
        "🐰 $num • Mise bas prévue aujourd'hui !"
    override fun alertTitleBirthInDays(num: String, days: Int): String =
        "🐰 $num • Mise bas dans $days jour(s)"
    override val alertSubBirth: String =
        "Surveiller le nid et enregistrer la portée dès la naissance."
    override fun alertTitleWeaning(num: String): String = "🧺 $num • Sevrage à effectuer"
    override fun alertSubWeaning(count: Int): String =
        "$count lapereaux prêts à être séparés de la mère."
}

object AppStringsAR : AppStrings {
    override val lang: AppLanguage = AppLanguage.AR

    override val appTitle: String = "🐰 لابينو - تربية الأرانب"
    override val femaleDetailTitle: String = "بطاقة الأرنبة"
    override val back: String = "رجوع"
    override val themeToggleDesc: String = "المظهر الفاتح / الداكن"
    override val langToggleDesc: String = "تغيير اللغة"

    override val navHome: String = "الرئيسية"
    override fun navRabbits(count: Int): String = "الأرانب ($count)"
    override val navElevage: String = "الأقفاص والمخزون"
    override val navVaccines: String = "اللقاحات"
    override val fabNewRabbit: String = "أرنبة"

    override val statActiveRabbits: String = "الأرانب النشطة"
    override fun statOccupiedCages(occupied: Int, total: Int): String = "$occupied/$total أقفاص مشغولة"
    override val statKitsInNest: String = "الصغار في العش"
    override fun statNursingMothers(count: Int): String = "$count أمهات مرضعات"
    override fun pillRest(count: Int): String = "💤 راحة: $count"
    override fun pillMating(count: Int): String = "❤️ تلقيح: $count"
    override fun pillPregnant(count: Int): String = "🤰 حامل: $count"
    override fun pillNursing(count: Int): String = "🍼 مرضعة: $count"
    override fun sectionPriorityActions(count: Int): String = "🚨 مهام تربية ذات أولوية ($count)"
    override fun sectionLowFeedStock(count: Int): String = "⚠️ تنبيه انخفاض العلف ($count)"
    override fun sectionVaccineReminders(count: Int): String = "💉 تذكير باللقاحات ($count)"
    override fun sectionMyRabbits(count: Int): String = "أمهات الأرانب المنتجة ($count)"

    override fun feedStockInfo(stockKg: Double, dailyKg: Double): String =
        String.format(Locale.US, "%.1f كغ بالمخزون (%.2f كغ/يوم)", stockKg, dailyKg)
    override fun feedDaysRemaining(days: Int): String = "باقي $days يوم"
    override fun vaccineDueDate(dateStr: String): String = "الموعد: $dateStr"
    override val overdue: String = "متأخر"
    override fun inDays(days: Int): String = "خلال $days يوم"

    override val statusRest: String = "في راحة"
    override fun statusMatingElapsed(days: Int): String = "تلقيح (يوم $days)"
    override fun statusPregnantElapsed(days: Int): String = "حامل (يوم $days/31)"
    override fun statusNursingElapsed(days: Int): String = "مرضعة (يوم $days/35)"
    override fun cageAssigned(num: String): String = "القفص : $num"
    override val cageUnassigned: String = "القفص : غير مخصص"
    override val readyForMating: String = "جاهزة لتلقيح جديد"
    override fun matingDetail(date: String, male: String?): String =
        "تم التلقيح في $date" + (male?.let { " • الذكر: $it" } ?: "")
    override fun birthOverdueDays(days: Int): String = "⚠️ تأخرت الولادة بـ $days يوم!"
    override val birthDueToday: String = "🐰 الولادة متوقعة اليوم!"
    override fun birthDueInDays(days: Int?, date: String): String =
        "الولادة متوقعة خلال ${days ?: "؟"} يوم ($date)"
    override fun nursingDetail(vivants: Int, date: String): String =
        "$vivants صغار أحياء • تاريخ الولادة $date"

    override fun alertPalpationNow(days: Int): String =
        "🔍 يجب إجراء الجس! (يوم $days - فحص نمو الأجنة)"
    override fun alertPalpationAdvised(days: Int): String =
        "يُنصح بالجس في اليوم 11 (خلال $days يوم)"
    override fun alertNestUrgent(days: Int): String =
        "📦 عاجل (يوم $days): وضع صندوق الولادة مع القش!"
    override val alertNestInstalled: String = "✓ تم تركيب صندوق الولادة النظيف"
    override fun alertWeaningRecommended(days: Int): String =
        "🧺 يُنصح بالفطام (يوم $days - جاهزون للتسمين)"
    override val alertKitsEatingSolid: String =
        "الخروج من العش: بدأت الصغار بتناول العلف الصلب"

    override val actionMate: String = "تلقيح / تزاوج"
    override val actionPalpate: String = "جس البطن (فحص)"
    override val actionDeclareBirth: String = "تسجيل الولادة"
    override val actionWean: String = "فطام"
    override val actionDoWean: String = "إجراء الفطام"
    override val actionNestPlaced: String = "تم وضع العش ✓"
    override val incidentDescription: String = "تسجيل طارئ أو حادث"

    override val searchPlaceholder: String = "بحث بالاسم أو رقم القفص..."
    override fun filterAll(count: Int): String = "الكل ($count)"
    override val filterRest: String = "راحة"
    override val filterMating: String = "تلقيح"
    override val filterPregnant: String = "حامل"
    override val filterNursing: String = "مرضعة"

    override val edit: String = "تعديل"
    override val estimatedAge: String = "العمر التقديري :"
    override val birthDate: String = "تاريخ الميلاد :"
    override val generalStatus: String = "الحالة العامة :"
    override val statusLiving: String = "حية"
    override val statusSold: String = "مباعة"
    override val statusDead: String = "نافقة"
    override val currentCycleHeader: String = "🌪️ دورة التناسل الحالية"
    override val zootechnicalSummaryHeader: String = "📊 الحصيلة الإنتاجية للأرنبة"
    override val kpiFertility: String = "الخصوبة"
    override val kpiLitters: String = "الولادات"
    override val kpiAlivePerLitter: String = "أحياء/بطن"
    override val kpiWeaned: String = "المفطومة"
    override fun bornSummary(vivants: Int, morts: Int): String =
        "المواليد : $vivants أحياء • $morts نافقة"
    override fun weaningSurvivalRate(rate: String): String = "نسبة البقاء للفطام : $rate"
    override fun historyHeader(count: Int): String = "📜 سجل الدورات والولادات ($count)"
    override val noCyclesRecorded: String = "لا توجد دورات مسجلة."
    override fun cycleMatingLine(date: String, male: String?): String =
        "تلقيح : $date" + (male?.let { " • الذكر: $it" } ?: "")
    override val statusWeanedSuccess: String = "تم الفطام ✓"
    override val statusFailed: String = "إخفاق"
    override fun cycleBirthLine(date: String, vivants: Int, morts: Int): String =
        "🐰 ولادة : $date • $vivants أحياء، $morts نافقة"
    override fun cycleWeanedLine(count: Int, pct: String): String =
        "🧺 مفطومة : $count أرنب$pct"
    override fun cycleReason(reason: String): String = "السبب : $reason"
    override fun cycleNotes(notes: String): String = "ملاحظة : $notes"
    override fun vaccinationsSectionHeader(count: Int): String = "💉 التطعيمات ($count)"
    override val vaccinateBtn: String = "+ تطعيم"
    override val noVaccinationsRecorded: String = "لا توجد تطعيمات مسجلة."
    override fun vaccinationLine(doneDate: String, nextDate: String): String =
        "أُعطي: $doneDate ← التذكير: $nextDate"
    override val deleteRabbitBtn: String = "حذف هذه الأرنبة"
    override fun deleteRabbitTitle(num: String): String = "حذف $num ؟"
    override val deleteRabbitMessage: String =
        "سيؤدي هذا الإجراء إلى حذف الأرنبة وكافة بيانات تناسلها نهائياً."
    override val confirmDelete: String = "حذف"
    override val cancel: String = "إلغاء"
    override val save: String = "حفظ"
    override val close: String = "إغلاق"

    override val dialogNewRabbitTitle: String = "🐰 إضافة أرنبة جديدة"
    override fun dialogEditRabbitTitle(num: String): String = "تعديل $num"
    override val inputRabbitName: String = "الاسم / رقم التعريف"
    override val inputRabbitNamePlaceholder: String = "مثال: F05 - فضية"
    override val inputBirthDate: String = "تاريخ الميلاد (سنة-شهر-يوم)"
    override val inputCageSelect: String = "القفص / العين :"
    override val noneOption: String = "بدون"

    override val dialogMatingTitle: String = "🌪️ تسجيل عملية التلقيح"
    override fun rabbitIndicator(num: String): String = "الأرنبة : $num"
    override val matingTip: String =
        "💡 نصيحة: انقل دائماً الأنثى إلى قفص الذكر (وليس العكس). سيتم جدولة فحص الجس في اليوم 11."
    override val inputMatingDate: String = "تاريخ التلقيح (سنة-شهر-يوم)"
    override val inputMale: String = "الذكر الملقح (الاسم / الرقم)"
    override val inputMalePlaceholder: String = "مثال: M01 - ركس"
    override val inputObsOptional: String = "ملاحظات (اختياري)"
    override val btnValidateMating: String = "تأكيد التلقيح"

    override val dialogPalpationTitle: String = "🩺 فحص الحمل بالجس"
    override val palpationTip: String =
        "💡 تحسس أسفل البطن برفق بين الإبهام وباقي الأصابع. بين اليوم 10 و 14، تظهر الأجنة كحبيبات بحجم حبة البندق."
    override val inputControlDate: String = "تاريخ الفحص (سنة-شهر-يوم)"
    override val btnPregnantYes: String = "✅ نعم - حامل مؤكد"
    override val btnPregnantNo: String = "❌ لا - فارغة (إعادة للراحة)"

    override val dialogNestTitle: String = "📦 تركيب صندوق الولادة"
    override val nestTip: String =
        "💡 في اليوم 28 من الحمل (قبل 3 أيام من الولادة)، ركّب صندوق ولادة نظيفاً مفروشاً بنشارة الخشب والقش الجاف. ستقوم الأرنبة بنتف فرو بطنها لتجهيز العش."
    override val btnConfirmNest: String = "تأكيد تركيب صندوق الولادة ✓"

    override val dialogBirthTitle: String = "🐰 تسجيل الولادة"
    override val birthTip: String =
        "💡 افحص العش بهدوء: استبعد المواليد النافقة وتأكد من امتلاء بطون الصغار بالحليب."
    override val inputDeliveryDate: String = "تاريخ الولادة (سنة-شهر-يوم)"
    override val inputBornAlive: String = "مواليد أحياء"
    override val inputStillborn: String = "مواليد نافقة"
    override val inputLitterNotes: String = "ملاحظات حول البطن (اختياري)"
    override val btnSaveBirth: String = "حفظ الولادة"

    override val dialogWeaningTitle: String = "🧺 تسجيل الفطام"
    override val weaningTip: String =
        "💡 الفطام (من 30 إلى 35 يوماً): فصل الصغار عن الأم إلى أقفاص التسمين، وتعود الأم لمرحلة الراحة."
    override val inputWeaningDate: String = "تاريخ الفطام (سنة-شهر-يوم)"
    override val inputWeanedAlive: String = "المفطومة الحية"
    override fun nestSurvivalRate(weaned: Int, alive: Int, pct: Int): String =
        "نسبة النجاة بالعش : $weaned / $alive صغار ($pct%)"
    override val inputWeaningNotes: String = "ملاحظات / قفص التسمين (اختياري)"
    override val btnCloseCycleSuccess: String = "إنهاء الدورة (فطام ناجح)"

    override val dialogIncidentTitle: String = "⚠️ تسجيل حادث أو طارئ"
    override val motifMiscarriage: String = "إجهاض"
    override val motifNotPregnant: String = "غير حامل (فارغة)"
    override val motifLitterMortality: String = "نفوق كامل البطن"
    override val motifAbandon: String = "هجر العش / نقص الحليب"
    override val motifOther: String = "سبب آخر"
    override val inputIncidentDetails: String = "تفاصيل الحادث (اختياري)"
    override val btnConfirmStopCycle: String = "تأكيد والعودة لمرحلة الراحة"

    override val feedStockHeader: String = "🌾 مخزون الأعلاف"
    override val btnAddFeed: String = "+ علف"
    override fun feedStockRow(stockKg: Double, rationG: Int): String =
        "المخزون : $stockKg كغ • الوجبة : $rationG غ/يوم/أرنبة"
    override fun feedAutonomyRow(days: Int): String = "الكفاية المقدرة : $days يوم"
    override fun cagesManagementHeader(count: Int): String = "🏠 إدارة الأقفاص ($count)"
    override val inputNewCage: String = "قفص جديد (مثال: C1)"
    override val btnAdd: String = "إضافة"
    override fun cageTitle(num: String): String = "قفص / عين $num"
    override fun cageOccupiedBy(num: String): String = "مشغول بواسطة : $num"
    override val cageEmpty: String = "شاغر / فارغ"
    override val dialogAddFeedTitle: String = "إضافة نوع علف"
    override val dialogEditFeedTitle: String = "تعديل العلف"
    override val inputFeedName: String = "اسم العلف"
    override val inputFeedStock: String = "المخزون المتوفر (كغ)"
    override val inputFeedDailyRation: String = "الوجبة اليومية لكل أرنبة (غ)"

    override val vaccinesRefHeader: String = "💉 دليل اللقاحات"
    override val inputVaccineName: String = "اسم اللقاح (مثال: VHD)"
    override val inputValidityDays: String = "المدة (يوم)"
    override fun vaccineReminderInterval(days: Int): String = "جرعة تذكيرية كل $days يوم"
    override val dialogRecordVaccinationTitle: String = "💉 تسجيل تطعيم"
    override val inputVaccinationDate: String = "تاريخ التطعيم (سنة-شهر-يوم)"

    override val ageUnderMonth: String = "أقل من شهر"
    override fun ageMonths(m: Int): String = "$m شهر"
    override fun ageYearsAndMonths(y: Int, m: Int): String = "$y سنة و $m شهر"
    override fun ageYears(y: Int): String = "$y سنة"
    override val unknown: String = "غير معروف"

    override fun alertBadgeDay(day: Int): String = "يوم $day"
    override fun alertBadgeUrgent(day: Int): String = "عاجل يوم $day"
    override val alertBadgeBirth: String = "ولادة"
    override fun alertTitlePalpation(num: String): String = "🩺 $num • فحص الحمل (الجس)!"
    override fun alertSubPalpation(date: String, male: String?): String =
        "تلقيح بتاريخ $date${male?.let { " مع $it" } ?: ""}. يلزم فحص الحمل."
    override fun alertTitleNest(num: String): String = "📦 $num • وضع صندوق الولادة!"
    override fun alertSubNest(days: Int): String =
        "الولادة خلال $days أيام. جهز الصندوق بالقش النظيف."
    override fun alertTitleBirthOverdue(num: String, days: Int): String =
        "🐰 $num • تأخرت الولادة ($days يوم)"
    override fun alertTitleBirthToday(num: String): String =
        "🐰 $num • الولادة متوقعة اليوم!"
    override fun alertTitleBirthInDays(num: String, days: Int): String =
        "🐰 $num • الولادة خلال $days يوم"
    override val alertSubBirth: String =
        "مراقبة العش وتسجيل المواليد فور الولادة."
    override fun alertTitleWeaning(num: String): String = "🧺 $num • موعد الفطام"
    override fun alertSubWeaning(count: Int): String =
        "$count من الصغار جاهزون للفصل عن الأم."
}
