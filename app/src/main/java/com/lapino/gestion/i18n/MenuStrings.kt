package com.lapino.gestion.i18n

/** Textes des écrans ajoutés en v3 (accueil en tuiles, mâles, ventes, portées, conseils, calendrier du cycle). */
interface MenuStrings {
    // Accueil
    val greeting: String
    val defaultFarmName: String
    val tapToChangePhoto: String
    val settings: String
    val notifications: String
    fun herdSummary(females: Int, males: Int, kits: Int): String
    fun alertsToHandle(count: Int): String
    val noAlerts: String

    val tileFemales: String
    val tileMales: String
    val tileVaccines: String
    val tileLitters: String
    val tileFeed: String
    val tileCages: String
    val tileTips: String
    val tileSales: String
    fun countFemales(n: Int): String
    fun countMales(n: Int): String
    fun countLitters(n: Int): String
    fun countCages(n: Int): String
    fun countVaccines(n: Int): String
    fun countFeedLow(n: Int): String
    val feedOk: String
    val tipsSubtitle: String

    // Réglages
    val farmName: String
    val language: String
    val darkMode: String
    val remateRule: String
    val remateRuleHelp: String
    fun remateOption(days: Int): String
    val close: String

    // Calendrier du cycle
    val cycleCalendar: String
    val stepMating: String
    val stepPalpation: String
    val stepNestBox: String
    val stepBirthEstimated: String
    val stepBirth: String
    val stepRemate: String
    val stepWeanEarly: String
    val stepWeanLate: String
    val today: String
    fun inDays(n: Int): String
    fun lateDays(n: Int): String
    val done: String
    val possibleNow: String
    val actionRemate: String
    val actionWeanLitter: String
    fun litterUnderMother(kits: Int): String
    fun nursingRemateTooEarly(day: Int, date: String, rule: Int): String
    fun nursingRemateOk(day: Int, rule: Int): String
    val tooYoungWarning: String
    val pickMale: String
    fun alertRemateTitle(numero: String): String
    fun alertRemateSub(day: Int): String

    // Mâles
    val addMale: String
    val editMale: String
    val maleNumber: String
    val breed: String
    val birthDateIso: String
    fun maleStats(matings: Int, births: Int): String
    val noMales: String
    val age: String

    // Portées
    val noLitters: String
    fun litterTitle(mother: String): String
    fun litterBorn(date: String, day: Int): String
    fun litterKits(alive: Int): String
    val weanBetween: String
    fun totalKits(n: Int): String

    // Aliment (achats)
    val feedPurchases: String
    val addPurchase: String
    val purchaseDate: String
    val purchaseQty: String
    val purchasePrice: String
    val purchaseFeed: String
    val noPurchases: String
    fun purchaseLine(kg: String, price: String): String
    fun totalSpent(amount: String): String

    // Ventes
    val addSale: String
    val saleDate: String
    val saleCount: String
    val saleUnitPrice: String
    val saleNotes: String
    val noSales: String
    fun saleLine(count: Int, unit: String): String
    val totalSales: String
    val totalExpenses: String
    val profit: String
    val currency: String

    // Vaccins
    val vaccinateAll: String
    fun vaccinateAllConfirm(name: String, count: Int): String
    fun lastVaccination(date: String): String
    fun nextVaccination(date: String): String
    val neverVaccinated: String
    val vaccinationDate: String

    // Conseils
    val tips: List<Pair<String, String>>

    // Ergonomie & actions rapides
    val quickNewFemale: String
    val quickNewSale: String
    val quickNewPurchase: String
    val sortUrgent: String
    val sortName: String
    val resetFilters: String
    val noRabbitsMatch: String
    val allClearBanner: String
    val btnMinusDay: String
    val btnToday: String
    val btnPlusDay: String

    // Licence
    val licenseTitle: String
    val licenseFreeStatus: String
    val licenseLifeStatus: String
    fun licenseYearStatus(date: String): String
    fun licenseExpiredStatus(date: String): String
    fun licenseFreeLimitInfo(max: Int): String
    val licenseDeviceCode: String
    val licenseCopy: String
    val licenseCopied: String
    val licenseOrderWhatsApp: String
    val licenseKeyLabel: String
    val licensePaste: String
    val licenseActivate: String
    val licenseManage: String
    val licenseSuccess: String
    val licenseErrFormat: String
    val licenseErrSignature: String
    val licenseErrDevice: String
    val licenseErrExpired: String
}

val AppStrings.menu: MenuStrings
    get() = if (lang == AppLanguage.AR) MenuStringsAR else MenuStringsFR

object MenuStringsFR : MenuStrings {
    override val greeting = "Bonjour 👋"
    override val defaultFarmName = "Mon élevage"
    override val tapToChangePhoto = "Photo de profil"
    override val settings = "Réglages"
    override val notifications = "Notifications"
    override fun herdSummary(females: Int, males: Int, kits: Int) =
        "$females lapines · $males mâles · $kits lapereaux"
    override fun alertsToHandle(count: Int) = if (count == 1) "1 action à faire aujourd'hui" else "$count actions à faire aujourd'hui"
    override val noAlerts = "Tout est à jour ✓"

    override val tileFemales = "Lapines"
    override val tileMales = "Mâles"
    override val tileVaccines = "Vaccinations"
    override val tileLitters = "Portées"
    override val tileFeed = "Aliment"
    override val tileCages = "Cages"
    override val tileTips = "Conseils"
    override val tileSales = "Ventes"
    override fun countFemales(n: Int) = "$n reproductrices"
    override fun countMales(n: Int) = "$n reproducteurs"
    override fun countLitters(n: Int) = "$n sous la mère"
    override fun countCages(n: Int) = "$n cages"
    override fun countVaccines(n: Int) = when (n) {
        0 -> "À jour"
        1 -> "1 rappel"
        else -> "$n rappels"
    }
    override fun countFeedLow(n: Int) = "$n stock bas"
    override val feedOk = "Stock suffisant"
    override val tipsSubtitle = "Guide de l'éleveur"

    override val farmName = "Nom de l'élevage"
    override val language = "Langue"
    override val darkMode = "Mode sombre"
    override val remateRule = "Remise au mâle après mise bas"
    override val remateRuleHelp = "Détermine la date conseillée affichée pour chaque lapine allaitante."
    override fun remateOption(days: Int) = when (days) {
        11 -> "J+11 (intensif)"
        18 -> "J+18 (semi-intensif)"
        25 -> "J+25"
        else -> "J+$days (après sevrage)"
    }
    override val close = "Fermer"

    override val cycleCalendar = "Calendrier du cycle"
    override val stepMating = "Mise au mâle"
    override val stepPalpation = "Palpation (J+10)"
    override val stepNestBox = "Boîte à nid (J+28)"
    override val stepBirthEstimated = "Mise bas estimée (J+31)"
    override val stepBirth = "Mise bas"
    override val stepRemate = "Remise au mâle"
    override val stepWeanEarly = "Sevrage à 28 jours"
    override val stepWeanLate = "Sevrage à 35 jours"
    override val today = "aujourd'hui"
    override fun inDays(n: Int) = "dans $n j"
    override fun lateDays(n: Int) = "retard $n j"
    override val done = "fait"
    override val possibleNow = "possible"
    override val actionRemate = "Mettre au mâle"
    override val actionWeanLitter = "Sevrer la portée"
    override fun litterUnderMother(kits: Int) = "🍼 Portée précédente : $kits lapereaux sous la mère"
    override fun nursingRemateTooEarly(day: Int, date: String, rule: Int) =
        "⚠️ Allaitante depuis $day j. Remise au mâle conseillée à partir du $date (J+$rule après mise bas)."
    override fun nursingRemateOk(day: Int, rule: Int) =
        "✅ Allaitante depuis $day j : la remise au mâle est possible (règle J+$rule)."
    override val tooYoungWarning = "⚠️ Lapine de moins de 4 mois : trop jeune pour la reproduction."
    override val pickMale = "Choisir un mâle"
    override fun alertRemateTitle(numero: String) = "💞 $numero • Remise au mâle possible"
    override fun alertRemateSub(day: Int) = "Allaitante depuis $day jours. Présenter la lapine au mâle."

    override val addMale = "Ajouter un mâle"
    override val editMale = "Modifier le mâle"
    override val maleNumber = "Nom / N° du mâle"
    override val breed = "Race / type"
    override val birthDateIso = "Date de naissance (AAAA-MM-JJ)"
    override fun maleStats(matings: Int, births: Int) = "$matings saillies · $births mises bas"
    override val noMales = "Aucun mâle enregistré. Ajoutez vos reproducteurs."
    override val age = "Âge"

    override val noLitters = "Aucune portée sous la mère pour le moment."
    override fun litterTitle(mother: String) = "Portée de $mother"
    override fun litterBorn(date: String, day: Int) = "Nés le $date · $day jours"
    override fun litterKits(alive: Int) = "$alive lapereaux"
    override val weanBetween = "Sevrage"
    override fun totalKits(n: Int) = "$n lapereaux sous la mère au total"

    override val feedPurchases = "Achats d'aliment"
    override val addPurchase = "Nouvel achat"
    override val purchaseDate = "Date d'achat (AAAA-MM-JJ)"
    override val purchaseQty = "Quantité achetée (kg)"
    override val purchasePrice = "Prix payé (DA)"
    override val purchaseFeed = "Aliment concerné"
    override val noPurchases = "Aucun achat enregistré."
    override fun purchaseLine(kg: String, price: String) = "$kg kg · $price DA"
    override fun totalSpent(amount: String) = "Total dépensé : $amount DA"

    override val addSale = "Nouvelle vente"
    override val saleDate = "Date de vente (AAAA-MM-JJ)"
    override val saleCount = "Nombre de lapins"
    override val saleUnitPrice = "Prix par lapin (DA)"
    override val saleNotes = "Note (acheteur, poids…)"
    override val noSales = "Aucune vente enregistrée."
    override fun saleLine(count: Int, unit: String) = "$count lapins × $unit DA"
    override val totalSales = "Ventes"
    override val totalExpenses = "Achats aliment"
    override val profit = "Bénéfice"
    override val currency = "DA"

    override val vaccinateAll = "Vacciner tout l'élevage"
    override fun vaccinateAllConfirm(name: String, count: Int) = "Enregistrer « $name » pour les $count lapines ?"
    override fun lastVaccination(date: String) = "Dernière vaccination : $date"
    override fun nextVaccination(date: String) = "Prochaine vaccination : $date"
    override val neverVaccinated = "Pas encore de vaccination enregistrée"
    override val vaccinationDate = "Date de vaccination (AAAA-MM-JJ)"

    override val tips = listOf(
        "💞 Mise au mâle" to "Amenez toujours la femelle dans la cage du mâle, jamais l'inverse. Préférez le matin tôt ou le soir, au frais.",
        "🩺 Palpation à 10 jours" to "Palpez doucement l'abdomen 10 à 14 jours après la saillie. Si la lapine est vide, représentez-la au mâle tout de suite.",
        "📦 Boîte à nid à J+28" to "Posez une boîte propre garnie de paille sèche 3 jours avant la mise bas. La lapine s'arrache les poils pour finir le nid.",
        "🐣 Après la mise bas" to "Contrôlez le nid le lendemain : retirez les morts, comptez les vivants. Évitez le bruit et les manipulations inutiles.",
        "🍼 Sevrage 28 ou 35 jours" to "Sevrage à 28 jours en rythme intensif, 35 jours pour des lapereaux plus robustes. Retirez la mère, pas les petits.",
        "💉 Vaccinations" to "Entérotoxémie tous les 6 mois, maladie hémorragique (VHD) tous les ans. Traitez la gale dès les premiers signes aux oreilles.",
        "🌾 Aliment et eau" to "Eau propre à volonté, toute la journée. Une lapine allaitante mange et boit deux à trois fois plus qu'au repos.",
        "🧼 Hygiène" to "Nettoyez et désinfectez cages et boîtes à nid entre chaque portée. Une bonne aération sans courant d'air limite les maladies."
    )

    override val quickNewFemale = "+ Lapine"
    override val quickNewSale = "+ Vente"
    override val quickNewPurchase = "+ Aliment"
    override val sortUrgent = "🔥 Urgentes"
    override val sortName = "🔤 Nom"
    override val resetFilters = "Réinitialiser"
    override val noRabbitsMatch = "Aucune lapine ne correspond à la recherche."
    override val allClearBanner = "🎉 Tout est à jour — aucune alerte urgente aujourd'hui."
    override val btnMinusDay = "-1j"
    override val btnToday = "Aujourd'hui"
    override val btnPlusDay = "+1j"

    override val licenseTitle = "Licence Lapino"
    override val licenseFreeStatus = "Mode Découverte (max 5 lapines)"
    override val licenseLifeStatus = "✅ Licence À vie active"
    override fun licenseYearStatus(date: String) = "✅ Licence active jusqu'au $date"
    override fun licenseExpiredStatus(date: String) = "⚠️ Licence expirée le $date"
    override fun licenseFreeLimitInfo(max: Int) =
        "La version gratuite est limitée à $max lapines. Activez votre licence pour gérer un élevage illimité (vos données restent conservées)."
    override val licenseDeviceCode = "Votre Code Appareil"
    override val licenseCopy = "Copier"
    override val licenseCopied = "Code copié !"
    override val licenseOrderWhatsApp = "Commander sur WhatsApp (0770 20 86 26)"
    override val licenseKeyLabel = "Clé d'activation (LPN1...)"
    override val licensePaste = "Coller"
    override val licenseActivate = "Activer"
    override val licenseManage = "Gérer la licence"
    override val licenseSuccess = "✅ Licence activée avec succès ! Élevage illimité débloqué."
    override val licenseErrFormat = "Format de clé invalide."
    override val licenseErrSignature = "Clé non reconnue ou corrompue."
    override val licenseErrDevice = "Cette clé appartient à un autre appareil."
    override val licenseErrExpired = "Cette clé de licence est expirée."
}

object MenuStringsAR : MenuStrings {
    override val greeting = "مرحباً 👋"
    override val defaultFarmName = "مزرعتي"
    override val tapToChangePhoto = "صورة البروفيل"
    override val settings = "إعدادات"
    override val notifications = "إشعارات"
    override fun herdSummary(females: Int, males: Int, kits: Int) =
        "$females إناث · $males ذكور · $kits صغار"
    override fun alertsToHandle(count: Int) = "$count مهام يجب القيام بها اليوم"
    override val noAlerts = "كل شيء على ما يرام ✓"

    override val tileFemales = "أرانب إناث"
    override val tileMales = "أرانب ذكور"
    override val tileVaccines = "تطعيمات"
    override val tileLitters = "الخلفة"
    override val tileFeed = "العلف"
    override val tileCages = "الأقفاص"
    override val tileTips = "نصائح وإرشاد"
    override val tileSales = "المبيعات"
    override fun countFemales(n: Int) = "$n أمهات"
    override fun countMales(n: Int) = "$n فحول"
    override fun countLitters(n: Int) = "$n تحت الأم"
    override fun countCages(n: Int) = "$n أقفاص"
    override fun countVaccines(n: Int) = if (n == 0) "محدّثة" else "$n تذكير"
    override fun countFeedLow(n: Int) = "$n مخزون منخفض"
    override val feedOk = "المخزون كافٍ"
    override val tipsSubtitle = "دليل المربي"

    override val farmName = "اسم المزرعة"
    override val language = "اللغة"
    override val darkMode = "الوضع الليلي"
    override val remateRule = "إعادة الأنثى للذكر بعد الولادة"
    override val remateRuleHelp = "يحدد التاريخ المقترح المعروض لكل أرنبة مرضعة."
    override fun remateOption(days: Int) = when (days) {
        11 -> "بعد 11 يوم (مكثف)"
        18 -> "بعد 18 يوم (شبه مكثف)"
        25 -> "بعد 25 يوم"
        else -> "بعد $days يوم (بعد الفطام)"
    }
    override val close = "إغلاق"

    override val cycleCalendar = "رزنامة الدورة"
    override val stepMating = "تاريخ وضعها للذكر"
    override val stepPalpation = "تاريخ الجس (بعد 10 أيام)"
    override val stepNestBox = "وضع صندوق الولادة (يوم 28)"
    override val stepBirthEstimated = "تاريخ الولادة المقدر (يوم 31)"
    override val stepBirth = "تاريخ الولادة"
    override val stepRemate = "إعادتها للذكر"
    override val stepWeanEarly = "الفطام بعد 28 يوم"
    override val stepWeanLate = "الفطام بعد 35 يوم"
    override val today = "اليوم"
    override fun inDays(n: Int) = "بعد $n يوم"
    override fun lateDays(n: Int) = "تأخر $n يوم"
    override val done = "تم"
    override val possibleNow = "ممكن الآن"
    override val actionRemate = "وضعها للذكر"
    override val actionWeanLitter = "فطام الصغار"
    override fun litterUnderMother(kits: Int) = "🍼 الخلفة السابقة: $kits صغار تحت الأم"
    override fun nursingRemateTooEarly(day: Int, date: String, rule: Int) =
        "⚠️ مرضعة منذ $day يوم. يُنصح بوضعها للذكر ابتداءً من $date (بعد $rule يوم من الولادة)."
    override fun nursingRemateOk(day: Int, rule: Int) =
        "✅ مرضعة منذ $day يوم: يمكن وضعها للذكر (قاعدة $rule يوم)."
    override val tooYoungWarning = "⚠️ أرنبة عمرها أقل من 4 أشهر: صغيرة على التزاوج."
    override val pickMale = "اختر ذكراً"
    override fun alertRemateTitle(numero: String) = "💞 $numero • يمكن وضعها للذكر"
    override fun alertRemateSub(day: Int) = "مرضعة منذ $day يوم. قدّم الأنثى للذكر."

    override val addMale = "إضافة ذكر"
    override val editMale = "تعديل الذكر"
    override val maleNumber = "اسم / رقم الذكر"
    override val breed = "النوع / السلالة"
    override val birthDateIso = "تاريخ الميلاد (سنة-شهر-يوم)"
    override fun maleStats(matings: Int, births: Int) = "$matings تلقيحات · $births ولادات"
    override val noMales = "لا يوجد ذكور مسجلون. أضف فحولك."
    override val age = "العمر"

    override val noLitters = "لا توجد خلفة تحت الأم حالياً."
    override fun litterTitle(mother: String) = "خلفة $mother"
    override fun litterBorn(date: String, day: Int) = "وُلدت يوم $date · $day يوم"
    override fun litterKits(alive: Int) = "$alive صغار"
    override val weanBetween = "الفطام"
    override fun totalKits(n: Int) = "المجموع: $n صغار تحت الأمهات"

    override val feedPurchases = "مشتريات العلف"
    override val addPurchase = "شراء جديد"
    override val purchaseDate = "تاريخ الشراء (سنة-شهر-يوم)"
    override val purchaseQty = "كمية الشراء (كغ)"
    override val purchasePrice = "السعر المدفوع (دج)"
    override val purchaseFeed = "نوع العلف"
    override val noPurchases = "لا توجد مشتريات مسجلة."
    override fun purchaseLine(kg: String, price: String) = "$kg كغ · $price دج"
    override fun totalSpent(amount: String) = "مجموع المصاريف: $amount دج"

    override val addSale = "بيع جديد"
    override val saleDate = "يوم البيع (سنة-شهر-يوم)"
    override val saleCount = "عدد الأرانب"
    override val saleUnitPrice = "سعر البيع للأرنب (دج)"
    override val saleNotes = "ملاحظة (المشتري، الوزن…)"
    override val noSales = "لا توجد مبيعات مسجلة."
    override fun saleLine(count: Int, unit: String) = "$count أرانب × $unit دج"
    override val totalSales = "المبيعات"
    override val totalExpenses = "شراء العلف"
    override val profit = "الربح"
    override val currency = "دج"

    override val vaccinateAll = "تطعيم كل القطيع"
    override fun vaccinateAllConfirm(name: String, count: Int) = "تسجيل « $name » لـ $count أرنبة؟"
    override fun lastVaccination(date: String) = "تاريخ التطعيم: $date"
    override fun nextVaccination(date: String) = "التطعيم القادم: $date"
    override val neverVaccinated = "لم يُسجَّل أي تطعيم بعد"
    override val vaccinationDate = "تاريخ التطعيم (سنة-شهر-يوم)"

    override val tips = listOf(
        "💞 وضع الأنثى للذكر" to "خذ الأنثى دائماً إلى قفص الذكر وليس العكس. الأفضل في الصباح الباكر أو المساء في الجو المعتدل.",
        "🩺 الجس بعد 10 أيام" to "جسّ البطن بلطف بين 10 و14 يوماً بعد التلقيح. إذا كانت فارغة أعدها للذكر مباشرة.",
        "📦 صندوق الولادة يوم 28" to "ضع صندوقاً نظيفاً بالتبن الجاف 3 أيام قبل الولادة. الأنثى تنتف وبرها لإكمال العش.",
        "🐣 بعد الولادة" to "افحص العش في اليوم الموالي: انزع الميت وعُدّ الأحياء. تجنب الضجيج واللمس غير الضروري.",
        "🍼 الفطام 28 أو 35 يوم" to "الفطام في 28 يوماً للنظام المكثف، و35 يوماً لصغار أقوى. انقل الأم واترك الصغار في قفصهم.",
        "💉 التطعيمات" to "المعوي كل ستة أشهر، النزيف الدموي كل سنة. عالج الجرب عند أول علامة في الأذنين.",
        "🌾 العلف والماء" to "ماء نظيف متوفر طوال اليوم. الأنثى المرضعة تأكل وتشرب ضعفين إلى ثلاثة أضعاف.",
        "🧼 النظافة" to "نظّف وعقّم الأقفاص وصناديق الولادة بين كل ولادة. التهوية الجيدة دون تيار هواء تقلل الأمراض."
    )

    override val quickNewFemale = "+ أرنبة"
    override val quickNewSale = "+ بيع"
    override val quickNewPurchase = "+ علف"
    override val sortUrgent = "🔥 عاجلة"
    override val sortName = "🔤 الاسم"
    override val resetFilters = "إعادة تعيين"
    override val noRabbitsMatch = "لا توجد أرنبة مطابقة للبحث."
    override val allClearBanner = "🎉 كل شيء محدّث — لا توجد مهام عاجلة اليوم."
    override val btnMinusDay = "-1 يوم"
    override val btnToday = "اليوم"
    override val btnPlusDay = "+1 يوم"

    override val licenseTitle = "رخصة Lapino"
    override val licenseFreeStatus = "الوضع التجريبي (حد أقصى 5 أرانب)"
    override val licenseLifeStatus = "✅ رخصة مدى الحياة مفعّلة"
    override fun licenseYearStatus(date: String) = "✅ رخصة مفعّلة حتى $date"
    override fun licenseExpiredStatus(date: String) = "⚠️ انتهت الرخصة في $date"
    override fun licenseFreeLimitInfo(max: Int) =
        "النسخة المجانية محدودة بـ $max أرانب إناث. فعّل الرخصة لإدارة عدد غير محدود (مع الحفاظ على كل بياناتك)."
    override val licenseDeviceCode = "رمز جهازك"
    override val licenseCopy = "نسخ"
    override val licenseCopied = "تم نسخ الرمز!"
    override val licenseOrderWhatsApp = "طلب التفعيل عبر واتساب (0770208626)"
    override val licenseKeyLabel = "مفتاح التفعيل (LPN1...)"
    override val licensePaste = "لصق"
    override val licenseActivate = "تفعيل"
    override val licenseManage = "إدارة الرخصة"
    override val licenseSuccess = "✅ تم تفعيل الرخصة بنجاح! حساب غير محدود."
    override val licenseErrFormat = "صيغة المفتاح غير صالحة."
    override val licenseErrSignature = "مفتاح غير صالح أو تالف."
    override val licenseErrDevice = "هذا المفتاح مخصص لجهاز آخر."
    override val licenseErrExpired = "انتهت صلاحية هذا المفتاح."
}
