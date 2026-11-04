package com.lapino.gestion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lapino.gestion.data.LapinoDatabase
import com.lapino.gestion.model.*
import com.lapino.gestion.ui.*
import java.util.Locale
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private lateinit var db: LapinoDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = LapinoDatabase(applicationContext)

        setContent {
            var isDark by remember { mutableStateOf(false) }
            LapinoTheme(darkTheme = isDark) {
                LapinoApp(
                    db = db,
                    isDark = isDark,
                    onToggleDark = { isDark = !isDark }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LapinoApp(
    db: LapinoDatabase,
    isDark: Boolean,
    onToggleDark: () -> Unit
) {
    var currentTab by remember { mutableIntStateOf(0) }
    var selectedFemelleDetailId by remember { mutableStateOf<Long?>(null) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    // États chargés depuis SQLite
    val globalStats = remember(refreshTrigger) { db.getGlobalStats() }
    val femelles = remember(refreshTrigger) { db.getFemellesWithStatus() }
    val reproAlerts = remember(refreshTrigger) { db.getReproductionAlerts() }
    val vaccineAlerts = remember(refreshTrigger) { db.getVaccineAlerts() }
    val aliments = remember(refreshTrigger) { db.getAllAliments() }
    val clapets = remember(refreshTrigger) { db.getAllClapets() }
    val vaccins = remember(refreshTrigger) { db.getAllVaccins() }

    // État Modal Reproduction
    var activeReproAction by remember { mutableStateOf<String?>(null) }
    var activeReproFemelle by remember { mutableStateOf<Femelle?>(null) }

    // État Modal Ajout/Édition Lapine
    var showAddEditFemelle by remember { mutableStateOf(false) }
    var editingFemelle by remember { mutableStateOf<Femelle?>(null) }

    val triggerReload: () -> Unit = { refreshTrigger += 1 }

    BackHandler(enabled = selectedFemelleDetailId != null) {
        selectedFemelleDetailId = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedFemelleDetailId != null) "Fiche Lapine" else "🐰 Lapino Élevage",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    if (selectedFemelleDetailId != null) {
                        IconButton(onClick = { selectedFemelleDetailId = null }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Retour",
                                tint = Color.White
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onToggleDark) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Thème clair/sombre",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary
                )
            )
        },
        bottomBar = {
            if (selectedFemelleDetailId == null) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Accueil") },
                        label = { Text("Accueil") }
                    )
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        icon = { Icon(Icons.Default.Pets, contentDescription = "Lapines") },
                        label = { Text("Lapines (${femelles.size})") }
                    )
                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { currentTab = 2 },
                        icon = { Icon(Icons.Default.HomeWork, contentDescription = "Élevage") },
                        label = { Text("Clapets & Stocks") }
                    )
                    NavigationBarItem(
                        selected = currentTab == 3,
                        onClick = { currentTab = 3 },
                        icon = { Icon(Icons.Default.MedicalServices, contentDescription = "Vaccins") },
                        label = { Text("Vaccins") }
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedFemelleDetailId == null && (currentTab == 0 || currentTab == 1)) {
                ExtendedFloatingActionButton(
                    onClick = {
                        editingFemelle = null
                        showAddEditFemelle = true
                    },
                    containerColor = GreenPrimary,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Lapine", fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (selectedFemelleDetailId != null) {
                val detailFemelle = remember(selectedFemelleDetailId, refreshTrigger) {
                    db.getFemelleById(selectedFemelleDetailId!!)
                }
                if (detailFemelle == null) {
                    selectedFemelleDetailId = null
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
                        onActionCycle = { f, action ->
                            activeReproFemelle = f
                            activeReproAction = action
                        },
                        onVaccinationAdded = triggerReload
                    )
                }
            } else {
                when (currentTab) {
                    0 -> DashboardTab(
                        stats = globalStats,
                        reproAlerts = reproAlerts,
                        alimentsBas = aliments.filter { it.joursRestants <= 7 },
                        vaccineAlerts = vaccineAlerts,
                        femelles = femelles,
                        onActionCycle = { f, action ->
                            activeReproFemelle = f
                            activeReproAction = action
                        },
                        onOpenFemelle = { f -> selectedFemelleDetailId = f.id },
                        onGoToStocks = { currentTab = 2 }
                    )
                    1 -> FemellesListTab(
                        femelles = femelles,
                        onActionCycle = { f, action ->
                            activeReproFemelle = f
                            activeReproAction = action
                        },
                        onOpenFemelle = { f -> selectedFemelleDetailId = f.id }
                    )
                    2 -> ElevageStocksTab(
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
                        }
                    )
                    3 -> VaccinsTab(
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
                        onOpenFemelle = { fId -> selectedFemelleDetailId = fId }
                    )
                }
            }
        }
    }

    // Modal unifié pour toutes les actions du cycle reproductif
    ReproductionDialog(
        actionType = activeReproAction,
        femelle = activeReproFemelle,
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
            activeReproFemelle?.cycle?.let { c ->
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
    onGoToStocks: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 88.dp)
    ) {
        // 1. Cartes de synthèse
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryStatCard(
                    title = "Lapines actives",
                    value = stats.totalFemelles.toString(),
                    subtitle = "${stats.clapetsRemplis}/${stats.clapetsTotal} loges occupées",
                    color = GreenPrimary,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                SummaryStatCard(
                    title = "Lapereaux au nid",
                    value = stats.lapereauxSousMere.toString(),
                    subtitle = "${stats.allaitantes} mère(s) allaitante(s)",
                    color = BlueGestante,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            // Puces par phase
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusPill("💤 Repos: ${stats.enRepos}", Color(0xFFEEEEEE), Color(0xFF424242))
                StatusPill("❤️ Saillie: ${stats.enSaillie}", PinkContainer, PinkSaillie)
                StatusPill("🤰 Gestante: ${stats.gestantes}", BlueContainer, BlueGestante)
                StatusPill("🍼 Allaitante: ${stats.allaitantes}", GreenContainer, GreenPrimary)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 2. Alertes Cuniculicoles Prioritaires
        if (reproAlerts.isNotEmpty()) {
            item {
                Text(
                    text = "🚨 Actions Élevage Prioritaires (${reproAlerts.size})",
                    fontSize = 16.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangeAlert,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(reproAlerts) { alert ->
                val bg = when (alert.type) {
                    ReproAlertType.BOITE_NID -> RedContainer
                    ReproAlertType.PALPATION -> OrangeContainer
                    ReproAlertType.MISE_BAS -> BlueContainer
                    ReproAlertType.SEVRAGE -> GreenContainer
                }
                val fg = when (alert.type) {
                    ReproAlertType.BOITE_NID -> RedCritical
                    ReproAlertType.PALPATION -> OrangeAlert
                    ReproAlertType.MISE_BAS -> BlueGestante
                    ReproAlertType.SEVRAGE -> GreenPrimary
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
                                text = alert.title,
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
                                    text = alert.badge,
                                    color = fg,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = alert.subtitle, fontSize = 12.5.sp, color = Color(0xFF333333))
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
        }

        // 3. Alertes Stock Alimentation
        if (alimentsBas.isNotEmpty()) {
            item {
                Text(
                    text = "⚠️ Stock Alimentation Bas (${alimentsBas.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = RedCritical,
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
                                "${alim.stockKg} kg en stock (${String.format(Locale.US, "%.2f", alim.consoJourKg)} kg/j)",
                                fontSize = 12.sp,
                                color = Color(0xFF424242)
                            )
                        }
                        Text(
                            "Reste ${alim.joursRestants} j",
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
                    text = "💉 Rappels Vaccins (${vaccineAlerts.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangeAlert,
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
                                "Échéance : ${DateHelper.formatFr(vac.dateProchain)}",
                                fontSize = 12.sp,
                                color = Color(0xFF424242)
                            )
                        }
                        Text(
                            text = if (days < 0) "En retard" else "Dans ${days}j",
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
                text = "Mes Lapines Reproductrices (${femelles.size})",
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
    onActionCycle: (Femelle, String) -> Unit,
    onOpenFemelle: (Femelle) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("all") }

    val filtered = remember(femelles, search, filter) {
        femelles.filter { f ->
            val matchQuery = search.isBlank() ||
                    f.numero.contains(search, ignoreCase = true) ||
                    (f.clapetNumero?.contains(search, ignoreCase = true) == true)
            val matchFilter = when (filter) {
                "repos" -> f.cycle == null
                "saillie" -> f.cycle?.statut == "saillie"
                "gestante" -> f.cycle?.statut == "gestante"
                "allaitante" -> f.cycle?.statut == "allaitante"
                else -> true
            }
            matchQuery && matchFilter
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("Rechercher par nom ou clapet...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(
                "all" to "Toutes (${femelles.size})",
                "repos" to "Repos",
                "saillie" to "Saillie",
                "gestante" to "Gestante",
                "allaitante" to "Allaitante"
            )
            filters.forEach { (key, label) ->
                FilterChip(
                    selected = filter == key,
                    onClick = { filter = key },
                    label = { Text(label) }
                )
            }
        }

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
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                                text = "Loge : ${femelle.clapetNumero?.let { "Clapet $it" } ?: "Non assignée"}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                        OutlinedButton(onClick = onEditFemelle) {
                            Text("Modifier")
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    InfoRow("Âge estimé :", DateHelper.calculateAgeLabel(femelle.dateNaissance))
                    InfoRow("Date de naissance :", DateHelper.formatFr(femelle.dateNaissance))
                    InfoRow("Statut général :", femelle.statut.uppercase(Locale.ROOT))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 2. Cycle actuel
        item {
            Text(
                "🌪️ Cycle Reproductif Actuel",
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
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "📊 Bilan Zootechnique de la Lapine",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.5.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MiniKpiBox("${stats.tauxFertilite}%", "Fertilité", GreenPrimary, Modifier.weight(1f))
                        MiniKpiBox("${stats.totalMisesBas}", "Portées", BlueGestante, Modifier.weight(1f))
                        MiniKpiBox(stats.moyennePortee, "Vivants/portée", GreenPrimary, Modifier.weight(1f))
                        MiniKpiBox("${stats.totalSevres}", "Sevrés", GreenPrimary, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Nés : ${stats.totalVivants} vivants • ${stats.totalMorts} mort-nés",
                            fontSize = 12.5.sp,
                            color = Color.Gray
                        )
                        Text(
                            "Survie sevrage : ${stats.tauxSevrage}",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary
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
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "📜 Historique des Cycles & Portées (${history.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (history.isEmpty()) {
                        Text("Aucun cycle enregistré.", fontSize = 13.sp, color = Color.Gray)
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
                                            "Saillie : ${DateHelper.formatFr(c.dateSaillie)}" +
                                                    (c.maleNumero?.let { " • Mâle: $it" } ?: ""),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = when (c.statut) {
                                                "termine" -> "Sevré ✓"
                                                "echec" -> "Échec"
                                                else -> c.statut.uppercase(Locale.ROOT)
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = when (c.statut) {
                                                "termine" -> GreenPrimary
                                                "echec" -> RedCritical
                                                else -> BlueGestante
                                            }
                                        )
                                    }
                                    if (!c.dateMiseBasReelle.isNullOrBlank()) {
                                        Text(
                                            "🐰 Mise bas : ${DateHelper.formatFr(c.dateMiseBasReelle)} • " +
                                                    "${c.nombreVivants ?: 0} vivants, ${c.nombreMorts ?: 0} mort-nés",
                                            fontSize = 12.5.sp
                                        )
                                    }
                                    if (c.nombreSevres != null) {
                                        val survie = if ((c.nombreVivants ?: 0) > 0) {
                                            " (${((c.nombreSevres.toDouble() / c.nombreVivants!!) * 100).roundToInt()}%)"
                                        } else ""
                                        Text(
                                            "🧺 Sevrés : ${c.nombreSevres} lapereaux$survie",
                                            fontSize = 12.5.sp,
                                            color = GreenPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    if (!c.motifEchec.isNullOrBlank()) {
                                        Text(
                                            "Motif : ${c.motifEchec}",
                                            fontSize = 12.sp,
                                            color = RedCritical
                                        )
                                    }
                                    if (!c.notes.isNullOrBlank()) {
                                        Text(
                                            "Obs : ${c.notes}",
                                            fontSize = 12.sp,
                                            fontStyle = FontStyle.Italic,
                                            color = Color.Gray
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
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "💉 Vaccinations (${vaccinations.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.5.sp
                        )
                        Button(
                            onClick = { showVaccinDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                        ) {
                            Text("+ Vacciner")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (vaccinations.isEmpty()) {
                        Text("Aucune vaccination enregistrée.", fontSize = 13.sp, color = Color.Gray)
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
                                    "Fait: ${DateHelper.formatFr(v.dateVaccination)} → Rappel: ${DateHelper.formatFr(v.dateProchain)}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))

            OutlinedButton(
                onClick = { showDeleteConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RedCritical),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Supprimer cette lapine")
            }
        }
    }

    if (showVaccinDialog && vaccins.isNotEmpty()) {
        var selectedVaccinId by remember { mutableLongStateOf(vaccins.first().id) }
        var dateVac by remember { mutableStateOf(DateHelper.todayIso()) }

        Dialog(onDismissRequest = { showVaccinDialog = false }) {
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("💉 Enregistrer une Vaccination", fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                        label = { Text("Date de vaccination (AAAA-MM-JJ)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showVaccinDialog = false }) { Text("Annuler") }
                        Button(
                            onClick = {
                                db.addVaccination(femelle.id, selectedVaccinId, dateVac)
                                showVaccinDialog = false
                                onVaccinationAdded()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                        ) {
                            Text("Enregistrer")
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Supprimer ${femelle.numero} ?") },
            text = { Text("Cette action supprimera la lapine et tout son historique de reproduction.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteFemelle()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedCritical)
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Annuler") }
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
    onDeleteAliment: (Long) -> Unit
) {
    var newClapetNum by remember { mutableStateOf("") }
    var showAlimDialog by remember { mutableStateOf(false) }
    var editingAliment by remember { mutableStateOf<Aliment?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 48.dp)
    ) {
        // Section Aliments
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🌾 Stocks d'Alimentation", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Button(
                    onClick = {
                        editingAliment = null
                        showAlimDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("+ Aliment")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(aliments, key = { "alim_${it.id}" }) { alim ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        editingAliment = alim
                        showAlimDialog = true
                    },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                            "Stock : ${alim.stockKg} kg • Ration : ${alim.consommationG.toInt()} g/j/lapine",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                        Text(
                            "Autonomie estimée : ${alim.joursRestants} jour(s)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (alim.joursRestants <= 7) RedCritical else GreenPrimary
                        )
                    }
                    IconButton(onClick = { onDeleteAliment(alim.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = RedCritical)
                    }
                }
            }
        }

        // Section Clapets
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text("🏠 Gestion des Clapets (${clapets.size})", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newClapetNum,
                    onValueChange = { newClapetNum = it },
                    label = { Text("Nouveau clapet (ex: C1)") },
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
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("Ajouter")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(clapets, key = { "clapet_${it.id}" }) { c ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Loge / Clapet ${c.numero}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            text = c.occupanteNumero?.let { "Occupé par : $it" } ?: "Libre / Vide",
                            fontSize = 13.sp,
                            color = if (c.occupanteNumero != null) GreenPrimary else Color.Gray
                        )
                    }
                    IconButton(onClick = { onDeleteClapet(c.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = RedCritical)
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
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        if (editingAliment == null) "Ajouter un Aliment" else "Modifier l'Aliment",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = nom,
                        onValueChange = { nom = it },
                        label = { Text("Nom de l'aliment") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it },
                        label = { Text("Stock disponible (en kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = conso,
                        onValueChange = { conso = it },
                        label = { Text("Ration quotidienne par lapine (en g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAlimDialog = false }) { Text("Annuler") }
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
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                        ) {
                            Text("Enregistrer")
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
    onOpenFemelle: (Long) -> Unit
) {
    var nom by remember { mutableStateOf("") }
    var duree by remember { mutableStateOf("365") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 48.dp)
    ) {
        item {
            Text("💉 Référentiel des Vaccins", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text("Nom du vaccin (ex: VHD)") },
                    singleLine = true,
                    modifier = Modifier.weight(1.4f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                OutlinedTextField(
                    value = duree,
                    onValueChange = { duree = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Validité (j)") },
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
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("+")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(vaccins, key = { it.id }) { v ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(v.nom, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Rappel tous les ${v.dureeJours} jours", fontSize = 13.sp, color = Color.Gray)
                    }
                    IconButton(onClick = { onDeleteVaccin(v.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = RedCritical)
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
    var numero by remember(existing) { mutableStateOf(existing?.numero ?: "") }
    var clapetId by remember(existing) { mutableStateOf(existing?.clapetId) }
    var dateNaissance by remember(existing) { mutableStateOf(existing?.dateNaissance ?: DateHelper.todayIso()) }
    var statut by remember(existing) { mutableStateOf(existing?.statut ?: "vivante") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    if (existing == null) "🐰 Nouvelle Lapine" else "Modifier ${existing.numero}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = numero,
                    onValueChange = { numero = it },
                    label = { Text("Nom / Numéro d'identification") },
                    placeholder = { Text("Ex: F05 - Argentée") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dateNaissance,
                    onValueChange = { dateNaissance = it },
                    label = { Text("Date de naissance (AAAA-MM-JJ)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text("Clapet / Loge :", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
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
                        label = { Text("Aucun") }
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
                    TextButton(onClick = onDismiss) { Text("Annuler") }
                    Button(
                        onClick = {
                            if (numero.isNotBlank()) {
                                onSave(numero, clapetId, dateNaissance, statut)
                            }
                        },
                        enabled = numero.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                    ) {
                        Text("Enregistrer")
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
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontSize = 13.sp, color = Color.Gray)
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(subtitle, fontSize = 11.5.sp, color = Color.Gray)
        }
    }
}

@Composable
fun StatusPill(label: String, bg: Color, fg: Color) {
    Surface(color = bg, shape = RoundedCornerShape(20.dp)) {
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
        Text(label, fontSize = 13.5.sp, color = Color.Gray)
        Text(value, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun MiniKpiBox(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = color)
        Text(label, fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center)
    }
}

