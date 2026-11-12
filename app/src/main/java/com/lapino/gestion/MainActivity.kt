package com.lapino.gestion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lapino.gestion.data.LapinoDatabase
import com.lapino.gestion.i18n.*
import com.lapino.gestion.license.*
import com.lapino.gestion.model.*
import com.lapino.gestion.ui.*
import java.util.Locale
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private lateinit var db: LapinoDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = LapinoDatabase(applicationContext)
        AppPrefs.loadRules(applicationContext)

        setContent {
            val context = LocalContext.current
            var currentLang by remember { mutableStateOf(LanguageManager.getSavedLanguage(context)) }
            val strings = if (currentLang == AppLanguage.AR) AppStringsAR else AppStringsFR
            var isDark by remember { mutableStateOf(AppPrefs.isDark(context)) }

            // Bord à bord : le fond aurora passe sous les barres système
            DisposableEffect(isDark) {
                val transparent = android.graphics.Color.TRANSPARENT
                val style = if (isDark) SystemBarStyle.dark(transparent) else SystemBarStyle.light(transparent, transparent)
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose {}
            }

            CompositionLocalProvider(
                LocalAppLanguage provides currentLang,
                LocalAppStrings provides strings,
                LocalLayoutDirection provides currentLang.layoutDirection
            ) {
                LapinoTheme(darkTheme = isDark) {
                    LapinoApp(
                        db = db,
                        isDark = isDark,
                        currentLang = currentLang,
                        onToggleDark = {
                            isDark = !isDark
                            AppPrefs.setDark(context, isDark)
                        },
                        onLanguageChanged = { newLang ->
                            currentLang = newLang
                            LanguageManager.saveLanguage(context, newLang)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LapinoApp(
    db: LapinoDatabase,
    isDark: Boolean,
    currentLang: AppLanguage,
    onToggleDark: () -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit
) {
    val strings = LocalAppStrings.current
    val ms = strings.menu
    val context = LocalContext.current

    // Navigation : "home" (tuiles) puis un écran par tuile
    var screen by remember { mutableStateOf("home") }
    var femellesFilter by remember { mutableStateOf("all") }
    var selectedFemelleDetailId by remember { mutableStateOf<Long?>(null) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var farmName by remember { mutableStateOf(AppPrefs.farmName(context)) }
    var showSettings by remember { mutableStateOf(false) }
    var showLicenseDialog by remember { mutableStateOf(false) }
    var licenseLimitReached by remember { mutableStateOf(false) }
    var vaccinateAllTarget by remember { mutableStateOf<Vaccin?>(null) }
    var showPurchaseDialog by remember { mutableStateOf(false) }

    // États chargés depuis SQLite + Licence
    val licenseStatus = remember(refreshTrigger) { LicenseManager.getStatus(context) }
    val globalStats = remember(refreshTrigger) { db.getGlobalStats() }
    val femelles = remember(refreshTrigger) { db.getFemellesWithStatus() }
    val reproAlerts = remember(refreshTrigger) { db.getReproductionAlerts() }
    val vaccineAlerts = remember(refreshTrigger) { db.getVaccineAlerts() }
    val aliments = remember(refreshTrigger) { db.getAllAliments() }
    val clapets = remember(refreshTrigger) { db.getAllClapets() }
    val vaccins = remember(refreshTrigger) { db.getAllVaccins() }
    val males = remember(refreshTrigger) { db.getAllMales() }
    val ventes = remember(refreshTrigger) { db.getAllVentes() }
    val achats = remember(refreshTrigger) { db.getAllAchats() }
    val vaccinStatus = remember(refreshTrigger) { db.getVaccinHerdStatus() }
    val alimentsBas = aliments.filter { it.joursRestants <= 7 }

    // État Modal Reproduction
    var activeReproAction by remember { mutableStateOf<String?>(null) }
    var activeReproFemelle by remember { mutableStateOf<Femelle?>(null) }

    // État Modal Ajout/Édition Lapine
    var showAddEditFemelle by remember { mutableStateOf(false) }
    var editingFemelle by remember { mutableStateOf<Femelle?>(null) }

    val triggerReload: () -> Unit = { refreshTrigger += 1 }
    val openNewFemale: () -> Unit = {
        if (!licenseStatus.isLicensed && femelles.size >= LicenseManager.FREE_MAX_FEMALES) {
            licenseLimitReached = true
            showLicenseDialog = true
        } else {
            editingFemelle = null
            showAddEditFemelle = true
        }
    }
    val openCycleAction: (Femelle, String) -> Unit = { f, action ->
        activeReproFemelle = f
        activeReproAction = action
    }
    val goBack: () -> Unit = {
        if (selectedFemelleDetailId != null) selectedFemelleDetailId = null else screen = "home"
    }

    BackHandler(enabled = selectedFemelleDetailId != null || screen != "home") { goBack() }

    val target = if (selectedFemelleDetailId != null) "detail" else screen
    val title = when (target) {
        "detail" -> strings.femaleDetailTitle
        "alerts" -> ms.notifications
        "femelles" -> ms.tileFemales
        "males" -> ms.tileMales
        "vaccins" -> ms.tileVaccines
        "litters" -> ms.tileLitters
        "feed" -> ms.tileFeed
        "cages" -> ms.tileCages
        "tips" -> ms.tileTips
        "sales" -> ms.tileSales
        else -> ""
    }

    AuroraBackground {
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            topBar = {
                if (target != "home") {
                    TopAppBar(
                        title = { Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 21.sp, color = MaterialTheme.colorScheme.onBackground) },
                        navigationIcon = {
                            Box(modifier = Modifier.padding(start = 12.dp, end = 10.dp)) {
                                GlassIconButton(Icons.AutoMirrored.Filled.ArrowBack, strings.back, onClick = goBack)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = Color.Transparent,
                            titleContentColor = MaterialTheme.colorScheme.onBackground,
                            navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            },
            floatingActionButton = {
                if (target == "femelles") {
                    ExtendedFloatingActionButton(
                        onClick = openNewFemale,
                        containerColor = BrandPrimary,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(20.dp),
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text(strings.fabNewRabbit, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        ) { innerPadding ->
            AnimatedContent(
                targetState = target,
                transitionSpec = {
                    (fadeIn(tween(280)) + slideInVertically(tween(280)) { it / 14 }) togetherWith fadeOut(tween(140))
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding(),
                label = "screen"
            ) { current ->
                when (current) {
                    "detail" -> {
                        val id = selectedFemelleDetailId
                        val detailFemelle = remember(id, refreshTrigger) { id?.let { db.getFemelleById(it) } }
                        if (detailFemelle == null) {
                            if (id != null) selectedFemelleDetailId = null
                            Box(Modifier.fillMaxSize())
                        } else {
                            FemelleDetailView(
                                db = db,
                                femelle = detailFemelle,
                                vaccins = vaccins,
                                refreshKey = refreshTrigger,
                                onEditFemelle = {
                                    editingFemelle = detailFemelle
                                    showAddEditFemelle = true
                                },
                                onDeleteFemelle = {
                                    db.deleteFemelle(detailFemelle.id)
                                    selectedFemelleDetailId = null
                                    triggerReload()
                                },
                                onActionCycle = openCycleAction,
                                onVaccinationAdded = triggerReload
                            )
                        }
                    }
                    "alerts" -> DashboardTab(
                        stats = globalStats,
                        reproAlerts = reproAlerts,
                        alimentsBas = alimentsBas,
                        vaccineAlerts = vaccineAlerts,
                        femelles = femelles,
                        onActionCycle = openCycleAction,
                        onOpenFemelle = { f -> selectedFemelleDetailId = f.id },
                        onGoToStocks = { screen = "feed" },
                        onOpenFemalesFilter = { f ->
                            femellesFilter = f
                            screen = "femelles"
                        }
                    )
                    "femelles" -> FemellesListTab(
                        femelles = femelles,
                        reproAlerts = reproAlerts,
                        filter = femellesFilter,
                        onFilterChange = { femellesFilter = it },
                        onActionCycle = openCycleAction,
                        onOpenFemelle = { f -> selectedFemelleDetailId = f.id }
                    )
                    "males" -> MalesScreen(
                        males = males,
                        onSave = { id, numero, race, naissance ->
                            db.saveMale(id, numero, race, naissance)
                            triggerReload()
                        },
                        onDelete = { id ->
                            db.deleteMale(id)
                            triggerReload()
                        }
                    )
                    "vaccins" -> VaccinsTab(
                        vaccins = vaccins,
                        vaccineAlerts = vaccineAlerts,
                        onAddVaccin = { nom, duree ->
                            db.addVaccin(nom, duree)
                            triggerReload()
                        },
                        onDeleteVaccin = { id ->
                            db.deleteVaccin(id)
                            triggerReload()
                        },
                        onOpenFemelle = { fId -> selectedFemelleDetailId = fId },
                        herdStatus = vaccinStatus,
                        onVaccinateAll = { v -> vaccinateAllTarget = v }
                    )
                    "litters" -> LittersScreen(
                        femelles = femelles,
                        onWean = { f -> openCycleAction(f, "wean") },
                        onOpenFemelle = { f -> selectedFemelleDetailId = f.id }
                    )
                    "feed", "cages" -> ElevageStocksTab(
                        clapets = clapets,
                        aliments = aliments,
                        onAddClapet = { num ->
                            db.addClapet(num)
                            triggerReload()
                        },
                        onDeleteClapet = { id ->
                            db.deleteClapet(id)
                            triggerReload()
                        },
                        onSaveAliment = { id, nom, stock, conso ->
                            db.addOrUpdateAliment(id, nom, stock, conso)
                            triggerReload()
                        },
                        onDeleteAliment = { id ->
                            db.deleteAliment(id)
                            triggerReload()
                        },
                        section = current,
                        extraContent = {
                            feedPurchaseItems(
                                achats = achats,
                                header = { FeedPurchasesHeader(achats) { showPurchaseDialog = true } },
                                row = { i, a ->
                                    FeedPurchaseRow(i, a) {
                                        db.deleteAchat(a)
                                        triggerReload()
                                    }
                                }
                            )
                        }
                    )
                    "tips" -> TipsScreen()
                    "sales" -> SalesScreen(
                        ventes = ventes,
                        totalAchats = achats.sumOf { it.prix },
                        onAdd = { date, nombre, prix, notes ->
                            db.addVente(date, nombre, prix, notes)
                            triggerReload()
                        },
                        onDelete = { id ->
                            db.deleteVente(id)
                            triggerReload()
                        }
                    )
                    else -> HomeScreen(
                        farmName = farmName,
                        stats = globalStats,
                        malesCount = males.size,
                        littersCount = femelles.count { it.nursing != null },
                        reproAlertsCount = reproAlerts.size,
                        vaccineAlertsCount = vaccineAlerts.size,
                        lowFeedCount = alimentsBas.size,
                        salesTotal = ventes.sumOf { it.total },
                        onOpen = { key ->
                            if (key == "femelles") femellesFilter = "all"
                            screen = key
                        },
                        onOpenFemalesFilter = { f ->
                            femellesFilter = f
                            screen = "femelles"
                        },
                        onQuickAction = { action ->
                            when (action) {
                                "new_female" -> openNewFemale()
                                "new_sale" -> screen = "sales"
                                "new_purchase" -> showPurchaseDialog = true
                            }
                        },
                        onOpenSettings = { showSettings = true }
                    )
                }
            }
        }
    }

    if (showSettings) {
        val licSummary = when {
            licenseStatus.type == LicenseType.LIFE -> ms.licenseLifeStatus
            licenseStatus.type == LicenseType.YEAR && !licenseStatus.isExpired ->
                ms.licenseYearStatus(DateHelper.formatFr(licenseStatus.expiryIso))
            licenseStatus.type == LicenseType.YEAR && licenseStatus.isExpired ->
                ms.licenseExpiredStatus(DateHelper.formatFr(licenseStatus.expiryIso))
            else -> ms.licenseFreeStatus
        }
        SettingsDialog(
            farmName = farmName,
            isDark = isDark,
            currentLang = currentLang,
            licenseSummary = licSummary,
            isLicensed = licenseStatus.isLicensed,
            onOpenLicense = {
                showSettings = false
                licenseLimitReached = false
                showLicenseDialog = true
            },
            onFarmNameChange = {
                farmName = it
                AppPrefs.setFarmName(context, it)
            },
            onToggleDark = onToggleDark,
            onLanguageChanged = onLanguageChanged,
            onRulesChanged = triggerReload,
            onDismiss = { showSettings = false }
        )
    }

    if (showLicenseDialog) {
        LicenseDialog(
            status = licenseStatus,
            limitReached = licenseLimitReached,
            onActivated = {
                triggerReload()
                showLicenseDialog = false
            },
            onDismiss = { showLicenseDialog = false }
        )
    }

    vaccinateAllTarget?.let { v ->
        VaccinateAllDialog(
            vaccin = v,
            femellesCount = femelles.size,
            onDismiss = { vaccinateAllTarget = null },
            onConfirm = { date ->
                db.vaccinateAll(v.id, date)
                vaccinateAllTarget = null
                triggerReload()
            }
        )
    }

    if (showPurchaseDialog) {
        FeedPurchaseDialog(
            aliments = aliments,
            onDismiss = { showPurchaseDialog = false },
            onSave = { alimentId, date, qty, prix ->
                db.addAchat(alimentId, date, qty, prix)
                showPurchaseDialog = false
                triggerReload()
            }
        )
    }

    // Modal unifié pour toutes les actions du cycle reproductif
    ReproductionDialog(
        actionType = activeReproAction,
        femelle = activeReproFemelle,
        males = males.map { it.numero },
        onDismiss = {
            activeReproAction = null
            activeReproFemelle = null
        },
        onStartCycle = { dateSaillie, male, notes ->
            activeReproFemelle?.let { f ->
                db.startCycle(f.id, dateSaillie, male, notes)
                triggerReload()
            }
            activeReproAction = null
        },
        onVerifyGestation = { dateVerif, isPregnant, notes ->
            activeReproFemelle?.cycle?.let { c ->
                db.verifyGestation(c.id, dateVerif, isPregnant, notes)
                triggerReload()
            }
            activeReproAction = null
        },
        onConfirmNestBox = {
            activeReproFemelle?.cycle?.let { c ->
                db.confirmNestBox(c.id, true)
                triggerReload()
            }
            activeReproAction = null
        },
        onConfirmBirth = { dateNaissance, vivants, morts, notes ->
            activeReproFemelle?.cycle?.let { c ->
                db.confirmBirth(c.id, dateNaissance, vivants, morts, notes)
                triggerReload()
            }
            activeReproAction = null
        },
        onConfirmWeaning = { dateSevrage, sevres, notes ->
            activeReproFemelle?.let { it.portee ?: it.cycle }?.let { c ->
                db.confirmWeaning(c.id, dateSevrage, sevres, notes)
                triggerReload()
            }
            activeReproAction = null
        },
        onStopCycle = { motif, notes ->
            activeReproFemelle?.cycle?.let { c ->
                db.stopCycle(c.id, "echec", motif, notes)
                triggerReload()
            }
            activeReproAction = null
        }
    )

    // Modal Ajout / Modification Lapine
    if (showAddEditFemelle) {
        AddEditFemelleDialog(
            existing = editingFemelle,
            clapets = clapets,
            onDismiss = { showAddEditFemelle = false },
            onSave = { numero, clapetId, dateNaissance, statut ->
                if (editingFemelle == null) {
                    db.addFemelle(numero, clapetId, dateNaissance, statut)
                } else {
                    db.updateFemelle(editingFemelle!!.id, numero, clapetId, dateNaissance, statut)
                }
                showAddEditFemelle = false
                triggerReload()
            }
        )
    }
}

@Composable
fun DashboardTab(
    stats: GlobalStats,
    reproAlerts: List<ReproAlert>,
    alimentsBas: List<Aliment>,
    vaccineAlerts: List<VaccinationFemelle>,
    femelles: List<Femelle>,
    onActionCycle: (Femelle, String) -> Unit,
    onOpenFemelle: (Femelle) -> Unit,
    onGoToStocks: () -> Unit,
    onOpenFemalesFilter: (String) -> Unit = {}
) {
    val strings = LocalAppStrings.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 88.dp)
    ) {
        // 1. Cartes de synthèse
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryStatCard(
                    title = strings.statActiveRabbits,
                    value = stats.totalFemelles.toString(),
                    subtitle = strings.statOccupiedCages(stats.clapetsRemplis, stats.clapetsTotal),
                    color = brandTextColor(),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                SummaryStatCard(
                    title = strings.statKitsInNest,
                    value = stats.lapereauxSousMere.toString(),
                    subtitle = strings.statNursingMothers(stats.allaitantes),
                    color = blueTextColor(),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            // Puces par phase (cliquables pour filtrer directement)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusPill(strings.pillRest(stats.enRepos), Color(0xFFEEEEEE), Color(0xFF424242)) { onOpenFemalesFilter("repos") }
                StatusPill(strings.pillMating(stats.enSaillie), PinkContainer, PinkSaillie) { onOpenFemalesFilter("saillie") }
                StatusPill(strings.pillPregnant(stats.gestantes), BlueContainer, BlueGestante) { onOpenFemalesFilter("gestante") }
                StatusPill(strings.pillNursing(stats.allaitantes), MintContainer, MintNursing) { onOpenFemalesFilter("allaitante") }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bannière rassurante quand aucune alerte urgente n'est en cours
        if (reproAlerts.isEmpty() && alimentsBas.isEmpty() && vaccineAlerts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MintContainer)
                ) {
                    Text(
                        text = strings.menu.allClearBanner,
                        color = MintNursing,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }

        // 2. Alertes Cuniculicoles Prioritaires
        if (reproAlerts.isNotEmpty()) {
            item {
                Text(
                    text = strings.sectionPriorityActions(reproAlerts.size),
                    fontSize = 16.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = orangeTextColor(),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(reproAlerts) { alert ->
                val bg = when (alert.type) {
                    ReproAlertType.BOITE_NID -> RedContainer
                    ReproAlertType.PALPATION -> OrangeContainer
                    ReproAlertType.MISE_BAS -> BlueContainer
                    ReproAlertType.SEVRAGE -> MintContainer
                    ReproAlertType.REMISE_MALE -> PinkContainer
                }
                val fg = when (alert.type) {
                    ReproAlertType.BOITE_NID -> RedCritical
                    ReproAlertType.PALPATION -> OrangeAlert
                    ReproAlertType.MISE_BAS -> BlueGestante
                    ReproAlertType.SEVRAGE -> MintNursing
                    ReproAlertType.REMISE_MALE -> PinkSaillie
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onActionCycle(alert.femelle, alert.actionType) },
                    colors = CardDefaults.cardColors(containerColor = bg),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = alert.getTitle(strings),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = fg,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                color = fg.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = alert.getBadge(strings),
                                    color = fg,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = alert.getSubtitle(strings), fontSize = 12.5.sp, color = Color(0xFF333333))
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
        }

        // 3. Alertes Stock Alimentation
        if (alimentsBas.isNotEmpty()) {
            item {
                Text(
                    text = strings.sectionLowFeedStock(alimentsBas.size),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = redTextColor(),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            items(alimentsBas) { alim ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onGoToStocks() },
                    colors = CardDefaults.cardColors(containerColor = OrangeContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(alim.nom, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E1E))
                            Text(
                                strings.feedStockInfo(alim.stockKg, alim.consoJourKg),
                                fontSize = 12.sp,
                                color = Color(0xFF424242)
                            )
                        }
                        Text(
                            strings.feedDaysRemaining(alim.joursRestants),
                            fontWeight = FontWeight.Bold,
                            color = RedCritical
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
        }

        // 4. Alertes Vaccinations
        if (vaccineAlerts.isNotEmpty()) {
            item {
                Text(
                    text = strings.sectionVaccineReminders(vaccineAlerts.size),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = orangeTextColor(),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            items(vaccineAlerts) { vac ->
                val days = DateHelper.daysUntil(vac.dateProchain) ?: 0
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            femelles.find { it.id == vac.femelleId }?.let { onOpenFemelle(it) }
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (days < 0) RedContainer else OrangeContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "${vac.femelleNumero} • ${vac.vaccinNom}",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E1E1E)
                            )
                            Text(
                                strings.vaccineDueDate(DateHelper.formatFr(vac.dateProchain)),
                                fontSize = 12.sp,
                                color = Color(0xFF424242)
                            )
                        }
                        Text(
                            text = if (days < 0) strings.overdue else strings.inDays(days),
                            fontWeight = FontWeight.Bold,
                            color = if (days < 0) RedCritical else OrangeAlert
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
        }

        // 5. Liste directe des lapines
        item {
            Text(
                text = strings.sectionMyRabbits(femelles.size),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        items(femelles, key = { it.id }) { femelle ->
            RabbitStatusCard(
                femelle = femelle,
                onAction = onActionCycle,
                onCardClick = onOpenFemelle
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FemellesListTab(
    femelles: List<Femelle>,
    reproAlerts: List<ReproAlert> = emptyList(),
    filter: String = "all",
    onFilterChange: (String) -> Unit = {},
    onActionCycle: (Femelle, String) -> Unit,
    onOpenFemelle: (Femelle) -> Unit
) {
    val strings = LocalAppStrings.current
    val ms = strings.menu
    var search by remember { mutableStateOf("") }
    var sortUrgent by remember { mutableStateOf(true) }

    val alertPriorityByFemelle = remember(reproAlerts) {
        reproAlerts.mapIndexed { idx, a -> a.femelle.id to idx }.toMap()
    }

    val counts = remember(femelles) {
        mapOf(
            "repos" to femelles.count { it.cycle == null },
            "saillie" to femelles.count { it.cycle?.statut == "saillie" },
            "gestante" to femelles.count { it.cycle?.statut == "gestante" },
            "allaitante" to femelles.count { it.nursing != null }
        )
    }

    val filtered = remember(femelles, search, filter, sortUrgent, alertPriorityByFemelle) {
        val base = femelles.filter { f ->
            val matchQuery = search.isBlank() ||
                    f.numero.contains(search, ignoreCase = true) ||
                    (f.clapetNumero?.contains(search, ignoreCase = true) == true)
            val matchFilter = when (filter) {
                "repos" -> f.cycle == null
                "saillie" -> f.cycle?.statut == "saillie"
                "gestante" -> f.cycle?.statut == "gestante"
                "allaitante" -> f.nursing != null
                else -> true
            }
            matchQuery && matchFilter
        }
        if (sortUrgent) {
            base.sortedWith(compareBy({ alertPriorityByFemelle[it.id] ?: Int.MAX_VALUE }, { it.numero }))
        } else {
            base.sortedBy { it.numero }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text(strings.searchPlaceholder) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (search.isNotEmpty()) {
                    IconButton(onClick = { search = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = ms.resetFilters)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = sortUrgent,
                onClick = { sortUrgent = !sortUrgent },
                label = { Text(if (sortUrgent) ms.sortUrgent else ms.sortName) }
            )
            val filters = listOf(
                "all" to strings.filterAll(femelles.size),
                "repos" to "${strings.filterRest} (${counts["repos"] ?: 0})",
                "saillie" to "${strings.filterMating} (${counts["saillie"] ?: 0})",
                "gestante" to "${strings.filterPregnant} (${counts["gestante"] ?: 0})",
                "allaitante" to "${strings.filterNursing} (${counts["allaitante"] ?: 0})"
            )
            filters.forEach { (key, label) ->
                FilterChip(
                    selected = filter == key,
                    onClick = { onFilterChange(key) },
                    label = { Text(label) }
                )
            }
        }

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔍", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = ms.noRabbitsMatch,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    if (search.isNotEmpty() || filter != "all") {
                        Spacer(modifier = Modifier.height(12.dp))
                        FilledTonalButton(onClick = {
                            search = ""
                            onFilterChange("all")
                        }) {
                            Text(ms.resetFilters)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 88.dp)
            ) {
                items(filtered, key = { it.id }) { femelle ->
                    RabbitStatusCard(
                        femelle = femelle,
                        onAction = onActionCycle,
                        onCardClick = onOpenFemelle
                    )
                }
            }
        }
    }
}

@Composable
fun FemelleDetailView(
    db: LapinoDatabase,
    femelle: Femelle,
    vaccins: List<Vaccin>,
    refreshKey: Int,
    onEditFemelle: () -> Unit,
    onDeleteFemelle: () -> Unit,
    onActionCycle: (Femelle, String) -> Unit,
    onVaccinationAdded: () -> Unit
) {
    val strings = LocalAppStrings.current
    val stats = remember(femelle.id, refreshKey) { db.getFemelleStats(femelle.id) }
    val history = remember(femelle.id, refreshKey) { db.getFemelleHistory(femelle.id) }
    val vaccinations = remember(femelle.id, refreshKey) { db.getVaccinationsByFemelle(femelle.id) }

    var showVaccinDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 48.dp)
    ) {
        // 1. Carte Identité
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = glassCardColors(),
                border = glassBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🐰 ${femelle.numero}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "🏠 " + (femelle.clapetNumero?.let { strings.cageAssigned(it) } ?: strings.cageUnassigned),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(onClick = onEditFemelle) {
                            Text(strings.edit)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    InfoRow(strings.estimatedAge, DateHelper.calculateAgeLabel(femelle.dateNaissance, strings))
                    InfoRow(strings.birthDate, DateHelper.formatFr(femelle.dateNaissance))
                    val statutLabel = when (femelle.statut.lowercase()) {
                        "vivante" -> strings.statusLiving
                        "vendue" -> strings.statusSold
                        "morte" -> strings.statusDead
                        else -> femelle.statut.uppercase(Locale.ROOT)
                    }
                    InfoRow(strings.generalStatus, statutLabel)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 2. Cycle actuel
        item {
            Text(
                strings.currentCycleHeader,
                fontWeight = FontWeight.Bold,
                fontSize = 16.5.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            RabbitStatusCard(
                femelle = femelle,
                onAction = onActionCycle,
                onCardClick = {}
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 3. Bilan Zootechnique
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = glassCardColors(),
                border = glassBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        strings.zootechnicalSummaryHeader,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.5.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MiniKpiBox("${stats.tauxFertilite}%", strings.kpiFertility, brandTextColor(), Modifier.weight(1f))
                        MiniKpiBox("${stats.totalMisesBas}", strings.kpiLitters, blueTextColor(), Modifier.weight(1f))
                        MiniKpiBox(stats.moyennePortee, strings.kpiAlivePerLitter, brandTextColor(), Modifier.weight(1f))
                        MiniKpiBox("${stats.totalSevres}", strings.kpiWeaned, brandTextColor(), Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            strings.bornSummary(stats.totalVivants, stats.totalMorts),
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            strings.weaningSurvivalRate(stats.tauxSevrage),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = brandTextColor()
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 4. Historique des portées
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = glassCardColors(),
                border = glassBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        strings.historyHeader(history.size),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (history.isEmpty()) {
                        Text(strings.noCyclesRecorded, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        history.forEach { c ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.background
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            strings.cycleMatingLine(DateHelper.formatFr(c.dateSaillie), c.maleNumero),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = when (c.statut) {
                                                "termine" -> strings.statusWeanedSuccess
                                                "echec" -> strings.statusFailed
                                                "saillie" -> strings.filterMating
                                                "gestante" -> strings.filterPregnant
                                                "allaitante" -> strings.filterNursing
                                                else -> c.statut.uppercase(Locale.ROOT)
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = when (c.statut) {
                                                "termine" -> brandTextColor()
                                                "echec" -> redTextColor()
                                                else -> blueTextColor()
                                            }
                                        )
                                    }
                                    if (!c.dateMiseBasReelle.isNullOrBlank()) {
                                        Text(
                                            strings.cycleBirthLine(DateHelper.formatFr(c.dateMiseBasReelle), c.nombreVivants ?: 0, c.nombreMorts ?: 0),
                                            fontSize = 12.5.sp
                                        )
                                    }
                                    if (c.nombreSevres != null) {
                                        val survie = if ((c.nombreVivants ?: 0) > 0) {
                                            " (${((c.nombreSevres.toDouble() / c.nombreVivants!!) * 100).roundToInt()}%)"
                                        } else ""
                                        Text(
                                            strings.cycleWeanedLine(c.nombreSevres, survie),
                                            fontSize = 12.5.sp,
                                            color = brandTextColor(),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    if (!c.motifEchec.isNullOrBlank()) {
                                        Text(
                                            strings.cycleReason(c.motifEchec),
                                            fontSize = 12.sp,
                                            color = redTextColor()
                                        )
                                    }
                                    if (!c.notes.isNullOrBlank()) {
                                        Text(
                                            strings.cycleNotes(c.notes),
                                            fontSize = 12.sp,
                                            fontStyle = FontStyle.Italic,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 5. Vaccinations de la lapine
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = glassCardColors(),
                border = glassBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            strings.vaccinationsSectionHeader(vaccinations.size),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.5.sp
                        )
                        Button(
                            onClick = { showVaccinDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = Color.White)
                        ) {
                            Text(strings.vaccinateBtn, color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (vaccinations.isEmpty()) {
                        Text(strings.noVaccinationsRecorded, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        vaccinations.forEach { v ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(v.vaccinNom, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
                                Text(
                                    strings.vaccinationLine(DateHelper.formatFr(v.dateVaccination), DateHelper.formatFr(v.dateProchain)),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))

            OutlinedButton(
                onClick = { showDeleteConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = redTextColor()),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.deleteRabbitBtn)
            }
        }
    }

    if (showVaccinDialog && vaccins.isNotEmpty()) {
        var selectedVaccinId by remember { mutableLongStateOf(vaccins.first().id) }
        var dateVac by remember { mutableStateOf(DateHelper.todayIso()) }

        Dialog(onDismissRequest = { showVaccinDialog = false }) {
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(strings.dialogRecordVaccinationTitle, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    vaccins.forEach { v ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedVaccinId = v.id }
                        ) {
                            RadioButton(
                                selected = selectedVaccinId == v.id,
                                onClick = { selectedVaccinId = v.id }
                            )
                            Text("${v.nom} (${v.dureeJours}j)")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = dateVac,
                        onValueChange = { dateVac = it },
                        label = { Text(strings.inputVaccinationDate) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showVaccinDialog = false }) { Text(strings.cancel) }
                        Button(
                            onClick = {
                                db.addVaccination(femelle.id, selectedVaccinId, dateVac)
                                showVaccinDialog = false
                                onVaccinationAdded()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = Color.White)
                        ) {
                            Text(strings.save, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(strings.deleteRabbitTitle(femelle.numero)) },
            text = { Text(strings.deleteRabbitMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteFemelle()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedCritical, contentColor = Color.White)
                ) {
                    Text(strings.confirmDelete, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(strings.cancel) }
            }
        )
    }
}

@Composable
fun ElevageStocksTab(
    clapets: List<Clapet>,
    aliments: List<Aliment>,
    onAddClapet: (String) -> Unit,
    onDeleteClapet: (Long) -> Unit,
    onSaveAliment: (Long?, String, Double, Double) -> Unit,
    onDeleteAliment: (Long) -> Unit,
    section: String = "all", // "all", "feed", "cages"
    extraContent: LazyListScope.() -> Unit = {}
) {
    val strings = LocalAppStrings.current
    var newClapetNum by remember { mutableStateOf("") }
    var showAlimDialog by remember { mutableStateOf(false) }
    var editingAliment by remember { mutableStateOf<Aliment?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 48.dp)
    ) {
        // Section Aliments
        if (section != "cages") item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(strings.feedStockHeader, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Button(
                    onClick = {
                        editingAliment = null
                        showAlimDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = Color.White)
                ) {
                    Text(strings.btnAddFeed, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (section != "cages") items(aliments, key = { "alim_${it.id}" }) { alim ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        editingAliment = alim
                        showAlimDialog = true
                    },
                colors = glassCardColors(),
                border = glassBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(alim.nom, fontWeight = FontWeight.Bold, fontSize = 15.5.sp)
                        Text(
                            strings.feedStockRow(alim.stockKg, alim.consommationG.toInt()),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            strings.feedAutonomyRow(alim.joursRestants),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (alim.joursRestants <= 7) redTextColor() else brandTextColor()
                        )
                    }
                    IconButton(onClick = { onDeleteAliment(alim.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = strings.confirmDelete, tint = redTextColor())
                    }
                }
            }
        }

        if (section == "feed") extraContent()

        // Section Clapets
        if (section != "feed") item {
            if (section == "all") Spacer(modifier = Modifier.height(20.dp))
            Text(strings.cagesManagementHeader(clapets.size), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newClapetNum,
                    onValueChange = { newClapetNum = it },
                    label = { Text(strings.inputNewCage) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newClapetNum.isNotBlank()) {
                            onAddClapet(newClapetNum)
                            newClapetNum = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = Color.White)
                ) {
                    Text(strings.btnAdd, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (section != "feed") items(clapets, key = { "clapet_${it.id}" }) { c ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = glassCardColors(),
                border = glassBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(strings.cageTitle(c.numero), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            text = c.occupanteNumero?.let { strings.cageOccupiedBy(it) } ?: strings.cageEmpty,
                            fontSize = 13.sp,
                            color = if (c.occupanteNumero != null) brandTextColor() else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onDeleteClapet(c.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = strings.confirmDelete, tint = redTextColor())
                    }
                }
            }
        }
    }

    if (showAlimDialog) {
        var nom by remember(editingAliment) { mutableStateOf(editingAliment?.nom ?: "") }
        var stock by remember(editingAliment) { mutableStateOf(editingAliment?.stockKg?.toString() ?: "") }
        var conso by remember(editingAliment) { mutableStateOf(editingAliment?.consommationG?.toInt()?.toString() ?: "150") }

        Dialog(onDismissRequest = { showAlimDialog = false }) {
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        if (editingAliment == null) strings.dialogAddFeedTitle else strings.dialogEditFeedTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = nom,
                        onValueChange = { nom = it },
                        label = { Text(strings.inputFeedName) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it },
                        label = { Text(strings.inputFeedStock) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = conso,
                        onValueChange = { conso = it },
                        label = { Text(strings.inputFeedDailyRation) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAlimDialog = false }) { Text(strings.cancel) }
                        Button(
                            onClick = {
                                if (nom.isNotBlank()) {
                                    onSaveAliment(
                                        editingAliment?.id,
                                        nom,
                                        stock.toDoubleOrNull() ?: 0.0,
                                        conso.toDoubleOrNull() ?: 150.0
                                    )
                                    showAlimDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = Color.White)
                        ) {
                            Text(strings.save, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VaccinsTab(
    vaccins: List<Vaccin>,
    vaccineAlerts: List<VaccinationFemelle>,
    onAddVaccin: (String, Int) -> Unit,
    onDeleteVaccin: (Long) -> Unit,
    onOpenFemelle: (Long) -> Unit,
    herdStatus: Map<Long, Pair<String, String>> = emptyMap(),
    onVaccinateAll: (Vaccin) -> Unit = {}
) {
    val strings = LocalAppStrings.current
    var nom by remember { mutableStateOf("") }
    var duree by remember { mutableStateOf("365") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 48.dp)
    ) {
        item {
            Text(strings.vaccinesRefHeader, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text(strings.inputVaccineName) },
                    singleLine = true,
                    modifier = Modifier.weight(1.4f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                OutlinedTextField(
                    value = duree,
                    onValueChange = { duree = it.filter { ch -> ch.isDigit() } },
                    label = { Text(strings.inputValidityDays) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(0.8f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = {
                        if (nom.isNotBlank()) {
                            onAddVaccin(nom, duree.toIntOrNull() ?: 365)
                            nom = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = Color.White)
                ) {
                    Text("+", color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(vaccins, key = { it.id }) { v ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = glassCardColors(),
                border = glassBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(v.nom, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(strings.vaccineReminderInterval(v.dureeJours), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val st = herdStatus[v.id]
                        if (st != null) {
                            Text(
                                strings.menu.lastVaccination(DateHelper.formatFr(st.first)),
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                strings.menu.nextVaccination(DateHelper.formatFr(st.second)),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if ((DateHelper.daysUntil(st.second) ?: 99) <= 7) redTextColor() else mintTextColor()
                            )
                        } else {
                            Text(
                                strings.menu.neverVaccinated,
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = { onVaccinateAll(v) }) {
                        Icon(Icons.Default.Vaccines, contentDescription = strings.menu.vaccinateAll, tint = brandTextColor())
                    }
                    IconButton(onClick = { onDeleteVaccin(v.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = strings.confirmDelete, tint = redTextColor())
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditFemelleDialog(
    existing: Femelle?,
    clapets: List<Clapet>,
    onDismiss: () -> Unit,
    onSave: (numero: String, clapetId: Long?, dateNaissance: String?, statut: String) -> Unit
) {
    val strings = LocalAppStrings.current
    var numero by remember(existing) { mutableStateOf(existing?.numero ?: "") }
    var clapetId by remember(existing) { mutableStateOf(existing?.clapetId) }
    var dateNaissance by remember(existing) { mutableStateOf(existing?.dateNaissance ?: DateHelper.todayIso()) }
    var statut by remember(existing) { mutableStateOf(existing?.statut ?: "vivante") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    if (existing == null) strings.dialogNewRabbitTitle else strings.dialogEditRabbitTitle(existing.numero),
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = numero,
                    onValueChange = { numero = it },
                    label = { Text(strings.inputRabbitName) },
                    placeholder = { Text(strings.inputRabbitNamePlaceholder) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dateNaissance,
                    onValueChange = { dateNaissance = it },
                    label = { Text(strings.inputBirthDate) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(strings.inputCageSelect, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = clapetId == null,
                        onClick = { clapetId = null },
                        label = { Text(strings.noneOption) }
                    )
                    clapets.forEach { c ->
                        FilterChip(
                            selected = clapetId == c.id,
                            onClick = { clapetId = c.id },
                            label = { Text(c.numero) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(strings.cancel) }
                    Button(
                        onClick = {
                            if (numero.isNotBlank()) {
                                onSave(numero, clapetId, dateNaissance, statut)
                            }
                        },
                        enabled = numero.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = Color.White)
                    ) {
                        Text(strings.save, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryStatCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = glassCardColors(),
                border = glassBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(subtitle, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun StatusPill(label: String, bg: Color, fg: Color, onClick: (() -> Unit)? = null) {
    Surface(
        color = bg,
        shape = RoundedCornerShape(20.dp),
        modifier = if (onClick != null) Modifier.clip(RoundedCornerShape(20.dp)).clickable { onClick() } else Modifier
    ) {
        Text(
            text = label,
            color = fg,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun MiniKpiBox(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = color)
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

