package com.lapino.gestion.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lapino.gestion.i18n.LocalAppStrings
import com.lapino.gestion.i18n.menu
import com.lapino.gestion.model.*
import java.util.Locale
import kotlin.math.abs

fun fmtNum(v: Double): String =
    if (v == Math.floor(v)) String.format(Locale.US, "%,.0f", v).replace(',', ' ')
    else String.format(Locale.US, "%,.1f", v).replace(',', ' ')

private fun String.toDoubleSafe(): Double = replace(',', '.').toDoubleOrNull() ?: 0.0

// ───────────────────────── Calendrier du cycle ─────────────────────────

private data class DateStep(
    val label: String,
    val iso: String,
    val done: Boolean,
    val color: Color,
    /** Étape facultative : une date passée signifie « possible », pas « en retard ». */
    val optional: Boolean = false
)

/** Toutes les dates clés de la lapine : saillie, palpation, boîte à nid, mise bas, remise au mâle, sevrage. */
@Composable
fun CycleDatesBlock(femelle: Femelle, modifier: Modifier = Modifier) {
    val ms = LocalAppStrings.current.menu
    val c = femelle.cycle
    val n = femelle.nursing
    val steps = buildList {
        val birth = n?.dateMiseBasReelle
        if (n != null && !birth.isNullOrBlank()) {
            add(DateStep(ms.stepBirth, birth, true, MintNursing))
            if (femelle.portee == null) {
                add(
                    DateStep(
                        "${ms.stepRemate} (+${CycleRules.remateDays})",
                        DateHelper.addDays(birth, CycleRules.remateDays),
                        false, PinkSaillie, optional = true
                    )
                )
            }
            add(DateStep(ms.stepWeanEarly, DateHelper.addDays(birth, CycleRules.WEAN_EARLY_DAYS), false, MintNursing, optional = true))
            add(DateStep(ms.stepWeanLate, DateHelper.addDays(birth, CycleRules.WEAN_LATE_DAYS), false, MintNursing))
        }
        if (c != null && c.statut != "allaitante" && c.dateSaillie.isNotBlank()) {
            add(DateStep(ms.stepMating, c.dateSaillie, true, PinkSaillie))
            add(
                DateStep(
                    ms.stepPalpation,
                    c.datePalpationPrevue ?: DateHelper.addDays(c.dateSaillie, CycleRules.PALPATION_DAYS),
                    c.datePalpationReelle != null, OrangeAlert
                )
            )
            add(
                DateStep(
                    ms.stepNestBox,
                    c.dateBoiteNidPrevue ?: DateHelper.addDays(c.dateSaillie, CycleRules.NEST_DAYS),
                    c.boiteNidPosee, BlueGestante
                )
            )
            add(DateStep(ms.stepBirthEstimated, c.dateMiseBasPrevue, false, BlueGestante))
        }
    }.sortedBy { it.iso }
    if (steps.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            "📅 ${ms.cycleCalendar}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        steps.forEach { s ->
            val d = DateHelper.daysUntil(s.iso) ?: 0
            val (state, stateColor) = when {
                s.done -> "✓ ${ms.done}" to MaterialTheme.colorScheme.onSurfaceVariant
                d == 0 -> ms.today to redTextColor()
                d > 0 -> ms.inDays(d) to MaterialTheme.colorScheme.onSurfaceVariant
                s.optional -> ms.possibleNow to mintTextColor()
                else -> ms.lateDays(abs(d)) to redTextColor()
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(if (s.done) s.color.copy(alpha = 0.4f) else s.color))
                Spacer(Modifier.width(8.dp))
                Text(
                    s.label,
                    fontSize = 12.5.sp,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (s.done) 0.6f else 1f)
                )
                Text(
                    DateHelper.formatFr(s.iso),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (s.done) 0.6f else 1f)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    state,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = stateColor,
                    modifier = Modifier.widthIn(min = 58.dp)
                )
            }
        }
    }
}

/** Avertissements affichés dans le dialogue de saillie (lapine allaitante, trop jeune). */
@Composable
fun MatingChecks(femelle: Femelle) {
    val ms = LocalAppStrings.current.menu
    val birth = femelle.nursing?.dateMiseBasReelle
    if (!birth.isNullOrBlank()) {
        val day = DateHelper.daysElapsed(birth)
        val rule = CycleRules.remateDays
        val ok = day >= rule
        NoticeBox(
            text = if (ok) ms.nursingRemateOk(day, rule)
            else ms.nursingRemateTooEarly(day, DateHelper.formatFr(DateHelper.addDays(birth, rule)), rule),
            bg = if (ok) MintContainer else OrangeContainer,
            fg = if (ok) MintNursing else OrangeAlert
        )
    }
    val born = DateHelper.parseIso(femelle.dateNaissance)
    if (born != null && DateHelper.daysElapsed(femelle.dateNaissance) < 120) {
        NoticeBox(ms.tooYoungWarning, OrangeContainer, OrangeAlert)
    }
}

@Composable
fun NoticeBox(text: String, bg: Color, fg: Color) {
    Text(
        text,
        color = fg,
        fontSize = 12.5.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(10.dp)
    )
}

/** Choix rapide d'un mâle enregistré. */
@Composable
fun MaleChips(males: List<String>, selected: String, onPick: (String) -> Unit) {
    if (males.isEmpty()) return
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        males.forEach { m ->
            FilterChip(selected = selected == m, onClick = { onPick(m) }, label = { Text(m, fontSize = 12.sp) })
        }
    }
}

// ───────────────────────── Éléments communs ─────────────────────────

@Composable
private fun ScreenHeader(title: String, actionLabel: String?, onAction: (() -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        if (actionLabel != null && onAction != null) {
            Button(onClick = onAction, shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(actionLabel, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EmptyState(emoji: String, text: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 46.sp)
        Spacer(Modifier.height(8.dp))
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
    }
}

@Composable
private fun FormDialog(
    title: String,
    canSave: Boolean,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val strings = LocalAppStrings.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 4.dp) {
            Column(modifier = Modifier.padding(22.dp).verticalScroll(rememberScrollState())) {
                Text(title, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(12.dp))
                content()
                Spacer(Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(strings.cancel) }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onSave, enabled = canSave, shape = RoundedCornerShape(16.dp)) {
                        Text(strings.save, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun Field(value: String, onChange: (String) -> Unit, label: String, numeric: Boolean = false) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = if (numeric) KeyboardOptions(keyboardType = KeyboardType.Decimal) else KeyboardOptions.Default,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    )
}

private fun isValidDate(s: String) = DateHelper.parseIso(s) != null

// ───────────────────────── Mâles ─────────────────────────

@Composable
fun MalesScreen(
    males: List<Male>,
    onSave: (Long?, String, String?, String?) -> Unit,
    onDelete: (Long) -> Unit
) {
    val strings = LocalAppStrings.current
    val ms = strings.menu
    var showDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Male?>(null) }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 48.dp)) {
        item {
            ScreenHeader(ms.countMales(males.size), ms.addMale) {
                editing = null
                showDialog = true
            }
        }
        if (males.isEmpty()) item { EmptyState("🐇", ms.noMales) }
        itemsIndexed(males, key = { _, m -> m.id }) { i, m ->
            GlassCard(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).staggeredAppear(i)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(50.dp).clip(RoundedCornerShape(16.dp)).background(BlueContainer),
                        contentAlignment = Alignment.Center
                    ) { Text("🐇", fontSize = 24.sp) }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(m.numero, fontWeight = FontWeight.Bold, fontSize = 16.5.sp)
                        if (!m.race.isNullOrBlank()) {
                            Text("${ms.breed} : ${m.race}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            "${strings.birthDate} : ${DateHelper.formatFr(m.dateNaissance)} · ${DateHelper.calculateAgeLabel(m.dateNaissance, strings)}",
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            ms.maleStats(m.totalSaillies, m.totalMisesBas),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = blueTextColor()
                        )
                    }
                    IconButton(onClick = {
                        editing = m
                        showDialog = true
                    }) { Icon(Icons.Default.Edit, contentDescription = strings.edit, tint = MaterialTheme.colorScheme.onSurface) }
                    IconButton(onClick = { onDelete(m.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = strings.confirmDelete, tint = redTextColor())
                    }
                }
            }
        }
    }

    if (showDialog) {
        var numero by remember(editing) { mutableStateOf(editing?.numero ?: "") }
        var race by remember(editing) { mutableStateOf(editing?.race ?: "") }
        var naissance by remember(editing) { mutableStateOf(editing?.dateNaissance ?: "") }
        FormDialog(
            title = if (editing == null) ms.addMale else ms.editMale,
            canSave = numero.isNotBlank() && (naissance.isBlank() || isValidDate(naissance)),
            onDismiss = { showDialog = false },
            onSave = {
                onSave(editing?.id, numero, race, naissance)
                showDialog = false
            }
        ) {
            Field(numero, { numero = it }, ms.maleNumber)
            Field(race, { race = it }, ms.breed)
            Field(naissance, { naissance = it }, ms.birthDateIso)
        }
    }
}

// ───────────────────────── Portées ─────────────────────────

@Composable
fun LittersScreen(
    femelles: List<Femelle>,
    onWean: (Femelle) -> Unit,
    onOpenFemelle: (Femelle) -> Unit
) {
    val ms = LocalAppStrings.current.menu
    val strings = LocalAppStrings.current
    val litters = femelles.filter { it.nursing != null }
    val total = litters.sumOf { it.nursing?.nombreVivants ?: 0 }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 48.dp)) {
        item { ScreenHeader(ms.totalKits(total), null, null) }
        if (litters.isEmpty()) item { EmptyState("🍼", ms.noLitters) }
        itemsIndexed(litters, key = { _, f -> f.id }) { i, f ->
            val n = f.nursing ?: return@itemsIndexed
            val birth = n.dateMiseBasReelle ?: ""
            val day = DateHelper.daysElapsed(birth)
            val target by animateFloatAsState(
                targetValue = (day.toFloat() / CycleRules.WEAN_LATE_DAYS).coerceIn(0f, 1f),
                animationSpec = tween(700),
                label = "litter"
            )
            GlassCard(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).staggeredAppear(i),
                onClick = { onOpenFemelle(f) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(ms.litterTitle(f.numero), fontWeight = FontWeight.Bold, fontSize = 16.5.sp)
                            Text(
                                ms.litterBorn(DateHelper.formatFr(birth), day),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(color = MintContainer, shape = RoundedCornerShape(50)) {
                            Text(
                                "🐣 ${ms.litterKits(n.nombreVivants ?: 0)}",
                                color = MintNursing,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = target,
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),
                        color = mintTextColor(),
                        trackColor = mintTextColor().copy(alpha = 0.22f)
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${ms.weanBetween} : ${DateHelper.formatFr(DateHelper.addDays(birth, CycleRules.WEAN_EARLY_DAYS))}" +
                                " → ${DateHelper.formatFr(DateHelper.addDays(birth, CycleRules.WEAN_LATE_DAYS))}",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { onWean(f) },
                            enabled = day >= 21,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MintNursing, contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) { Text(strings.actionWean, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White) }
                    }
                }
            }
        }
    }
}

// ───────────────────────── Ventes ─────────────────────────

@Composable
fun SalesScreen(
    ventes: List<Vente>,
    totalAchats: Double,
    onAdd: (String, Int, Double, String?) -> Unit,
    onDelete: (Long) -> Unit
) {
    val strings = LocalAppStrings.current
    val ms = strings.menu
    var showDialog by remember { mutableStateOf(false) }
    val totalVentes = ventes.sumOf { it.total }
    val profit = totalVentes - totalAchats

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 48.dp)) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredAppear(0)
                    .clip(RoundedCornerShape(28.dp))
                    .background(HeroGradient)
                    .padding(20.dp)
            ) {
                Column {
                    Text(ms.profit, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    Text(
                        "${if (profit < 0) "−" else ""}${fmtNum(abs(profit))} ${ms.currency}",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MoneyChip("↗ ${ms.totalSales}", "${fmtNum(totalVentes)} ${ms.currency}", Modifier.weight(1f))
                        MoneyChip("↘ ${ms.totalExpenses}", "${fmtNum(totalAchats)} ${ms.currency}", Modifier.weight(1f))
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            ScreenHeader("${ms.tileSales} (${ventes.size})", ms.addSale) { showDialog = true }
        }
        if (ventes.isEmpty()) item { EmptyState("💰", ms.noSales) }
        itemsIndexed(ventes, key = { _, v -> v.id }) { i, v ->
            GlassCard(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).staggeredAppear(i + 1)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("📅 ${DateHelper.formatFr(v.date)}", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(ms.saleLine(v.nombre, fmtNum(v.prixUnitaire)), fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
                        if (!v.notes.isNullOrBlank()) {
                            Text(v.notes, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text(
                        "${fmtNum(v.total)} ${ms.currency}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = mintTextColor()
                    )
                    IconButton(onClick = { onDelete(v.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = strings.confirmDelete, tint = redTextColor())
                    }
                }
            }
        }
    }

    if (showDialog) {
        var date by remember { mutableStateOf(DateHelper.todayIso()) }
        var nombre by remember { mutableStateOf("") }
        var prix by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        FormDialog(
            title = "💰 ${ms.addSale}",
            canSave = isValidDate(date) && (nombre.toIntOrNull() ?: 0) > 0 && prix.toDoubleSafe() > 0,
            onDismiss = { showDialog = false },
            onSave = {
                onAdd(date.trim(), nombre.toIntOrNull() ?: 0, prix.toDoubleSafe(), notes)
                showDialog = false
            }
        ) {
            Field(date, { date = it }, ms.saleDate)
            Field(nombre, { nombre = it.filter { ch -> ch.isDigit() } }, ms.saleCount, numeric = true)
            Field(prix, { prix = it }, ms.saleUnitPrice, numeric = true)
            Field(notes, { notes = it }, ms.saleNotes)
            Text(
                "= ${fmtNum((nombre.toIntOrNull() ?: 0) * prix.toDoubleSafe())} ${ms.currency}",
                fontWeight = FontWeight.Bold,
                color = mintTextColor()
            )
        }
    }
}

@Composable
private fun MoneyChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(label, color = Color.White.copy(alpha = 0.85f), fontSize = 11.5.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
    }
}

// ───────────────────────── Achats d'aliment ─────────────────────────

/** Section « achats » ajoutée sous la liste des stocks d'aliment. */
fun LazyListScope.feedPurchaseItems(
    achats: List<AchatAliment>,
    header: @Composable () -> Unit,
    row: @Composable (Int, AchatAliment) -> Unit
) {
    item(key = "achats_header") { header() }
    itemsIndexed(achats, key = { _, a -> "achat_${a.id}" }) { i, a -> row(i, a) }
}

@Composable
fun FeedPurchasesHeader(achats: List<AchatAliment>, onAdd: () -> Unit) {
    val ms = LocalAppStrings.current.menu
    Column {
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("🧾 ${ms.feedPurchases}", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Button(onClick = onAdd, shape = RoundedCornerShape(16.dp)) { Text(ms.addPurchase, fontWeight = FontWeight.Bold) }
        }
        Text(
            ms.totalSpent(fmtNum(achats.sumOf { it.prix })),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        if (achats.isEmpty()) {
            Text(ms.noPurchases, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun FeedPurchaseRow(index: Int, achat: AchatAliment, onDelete: () -> Unit) {
    val strings = LocalAppStrings.current
    val ms = strings.menu
    GlassCard(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp).staggeredAppear(index)) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("📅 ${DateHelper.formatFr(achat.date)}", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    ms.purchaseLine(fmtNum(achat.quantiteKg), fmtNum(achat.prix)),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp
                )
                if (!achat.alimentNom.isNullOrBlank()) {
                    Text(achat.alimentNom, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = strings.confirmDelete, tint = redTextColor())
            }
        }
    }
}

@Composable
fun FeedPurchaseDialog(
    aliments: List<Aliment>,
    onDismiss: () -> Unit,
    onSave: (Long?, String, Double, Double) -> Unit
) {
    val ms = LocalAppStrings.current.menu
    var date by remember { mutableStateOf(DateHelper.todayIso()) }
    var qty by remember { mutableStateOf("") }
    var prix by remember { mutableStateOf("") }
    var alimentId by remember { mutableStateOf(aliments.firstOrNull()?.id) }
    FormDialog(
        title = "🧾 ${ms.addPurchase}",
        canSave = isValidDate(date) && qty.toDoubleSafe() > 0,
        onDismiss = onDismiss,
        onSave = { onSave(alimentId, date.trim(), qty.toDoubleSafe(), prix.toDoubleSafe()) }
    ) {
        Field(date, { date = it }, ms.purchaseDate)
        Field(qty, { qty = it }, ms.purchaseQty, numeric = true)
        Field(prix, { prix = it }, ms.purchasePrice, numeric = true)
        if (aliments.isNotEmpty()) {
            Text(ms.purchaseFeed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                aliments.forEach { a ->
                    FilterChip(selected = alimentId == a.id, onClick = { alimentId = a.id }, label = { Text(a.nom, fontSize = 12.sp) })
                }
            }
        }
    }
}

// ───────────────────────── Vaccination du cheptel ─────────────────────────

@Composable
fun VaccinateAllDialog(vaccin: Vaccin, femellesCount: Int, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    val ms = LocalAppStrings.current.menu
    var date by remember { mutableStateOf(DateHelper.todayIso()) }
    FormDialog(
        title = "💉 ${ms.vaccinateAll}",
        canSave = isValidDate(date) && femellesCount > 0,
        onDismiss = onDismiss,
        onSave = { onConfirm(date.trim()) }
    ) {
        Text(ms.vaccinateAllConfirm(vaccin.nom, femellesCount), fontSize = 14.sp)
        Spacer(Modifier.height(10.dp))
        Field(date, { date = it }, ms.vaccinationDate)
        Text(
            ms.nextVaccination(DateHelper.formatFr(DateHelper.addDays(date, vaccin.dureeJours))),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = brandTextColor()
        )
    }
}

// ───────────────────────── Conseils ─────────────────────────

@Composable
fun TipsScreen() {
    val ms = LocalAppStrings.current.menu
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 48.dp)) {
        itemsIndexed(ms.tips) { i, (title, body) ->
            GlassCard(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).staggeredAppear(i)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(body, fontSize = 13.5.sp, lineHeight = 19.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
