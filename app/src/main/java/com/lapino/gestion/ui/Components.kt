package com.lapino.gestion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.lapino.gestion.model.CycleRules
import com.lapino.gestion.model.DateHelper
import com.lapino.gestion.model.Femelle
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun RabbitStatusCard(
    femelle: Femelle,
    onAction: (Femelle, String) -> Unit,
    onCardClick: (Femelle) -> Unit
) {
    val strings = LocalAppStrings.current
    val cycle = femelle.cycle

    var statusLabel = strings.statusRest
    var statusColor = Color(0xFF757575)
    var statusBg = Color(0xFFEEEEEE)
    var progress = 0f
    var detailText = strings.readyForMating
    var actionLabel = strings.actionMate
    var actionType = "start"

    var alertText: String? = null
    var alertBg = OrangeContainer
    var alertFg = OrangeAlert
    var showQuickNestBtn = false

    if (cycle != null) {
        when (cycle.statut) {
            "saillie" -> {
                val elapsed = DateHelper.daysElapsed(cycle.dateSaillie)
                statusLabel = strings.statusMatingElapsed(elapsed)
                statusColor = PinkSaillie
                statusBg = PinkContainer
                progress = (elapsed.toFloat() / CycleRules.PALPATION_DAYS).coerceIn(0f, 1f)
                detailText = strings.matingDetail(DateHelper.formatFr(cycle.dateSaillie), cycle.maleNumero)
                actionLabel = strings.actionPalpate
                actionType = "verify"

                if (elapsed >= 10) {
                    alertText = strings.alertPalpationNow(elapsed)
                    alertBg = OrangeContainer
                    alertFg = OrangeAlert
                } else {
                    alertText = strings.alertPalpationAdvised(max(1, CycleRules.PALPATION_DAYS - elapsed))
                    alertBg = BlueContainer
                    alertFg = BlueGestante
                }
            }
            "gestante" -> {
                val elapsed = DateHelper.daysElapsed(cycle.dateSaillie)
                val daysLeft = DateHelper.daysUntil(cycle.dateMiseBasPrevue)
                statusLabel = strings.statusPregnantElapsed(elapsed)
                statusColor = BlueGestante
                statusBg = BlueContainer
                progress = DateHelper.calculateProgress(cycle.dateSaillie, cycle.dateMiseBasPrevue)
                detailText = when {
                    daysLeft != null && daysLeft < 0 -> strings.birthOverdueDays(abs(daysLeft))
                    daysLeft == 0 -> strings.birthDueToday
                    else -> strings.birthDueInDays(daysLeft, DateHelper.formatFr(cycle.dateMiseBasPrevue))
                }
                actionLabel = strings.actionDeclareBirth
                actionType = "birth"

                if (elapsed >= 27 && !cycle.boiteNidPosee) {
                    alertText = strings.alertNestUrgent(elapsed)
                    alertBg = RedContainer
                    alertFg = RedCritical
                    showQuickNestBtn = true
                } else if (cycle.boiteNidPosee) {
                    alertText = strings.alertNestInstalled
                    alertBg = MintContainer
                    alertFg = MintNursing
                }
            }
            "allaitante" -> {
                val elapsed = DateHelper.daysElapsed(cycle.dateMiseBasReelle)
                val vivants = cycle.nombreVivants ?: 0
                statusLabel = strings.statusNursingElapsed(elapsed)
                statusColor = MintNursing
                statusBg = MintContainer
                progress = DateHelper.calculateProgress(cycle.dateMiseBasReelle, cycle.dateSevragePrevue)
                detailText = strings.nursingDetail(vivants, DateHelper.formatFr(cycle.dateMiseBasReelle))
                actionLabel = if (elapsed >= CycleRules.WEAN_EARLY_DAYS) strings.actionDoWean else strings.actionWean
                actionType = "wean"

                if (elapsed >= 32) {
                    alertText = strings.alertWeaningRecommended(elapsed)
                    alertBg = OrangeContainer
                    alertFg = OrangeAlert
                } else if (elapsed >= 21) {
                    alertText = strings.alertKitsEatingSolid
                    alertBg = BlueContainer
                    alertFg = BlueGestante
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onCardClick(femelle) },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
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
                        text = femelle.numero,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "🏠 " + (femelle.clapetNumero?.let { strings.cageAssigned(it) } ?: strings.cageUnassigned),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = detailText,
                fontSize = 13.5.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (cycle != null) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = statusColor,
                    trackColor = statusColor.copy(alpha = 0.18f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                CycleDatesBlock(femelle)
            }

            if (alertText != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(alertBg)
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = alertText,
                        color = alertFg,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    if (showQuickNestBtn) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = { onAction(femelle, "nest") },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(strings.actionNestPlaced, fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }

            // Portée précédente encore sous la mère (nouveau cycle déjà lancé)
            femelle.portee?.let { p ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MintContainer)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.menu.litterUnderMother(p.nombreVivants ?: 0),
                        color = MintNursing,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = { onAction(femelle, "wean") },
                        colors = ButtonDefaults.buttonColors(containerColor = MintNursing, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(strings.menu.actionWeanLitter, fontSize = 11.sp, color = Color.White)
                    }
                }
            }

            // Allaitante sans nouveau cycle : proposer la remise au mâle
            if (cycle?.statut == "allaitante") {
                val day = DateHelper.daysElapsed(cycle.dateMiseBasReelle)
                Spacer(modifier = Modifier.height(8.dp))
                FilledTonalButton(
                    onClick = { onAction(femelle, "start") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (day >= CycleRules.remateDays) PinkContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (day >= CycleRules.remateDays) PinkSaillie
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("💞 " + strings.menu.actionRemate, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (cycle != null) {
                    IconButton(
                        onClick = { onAction(femelle, "fail") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = strings.incidentDescription,
                            tint = redTextColor()
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Button(
                    onClick = { onAction(femelle, actionType) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (cycle == null) BrandPrimary else statusColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(actionLabel, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ReproductionDialog(
    actionType: String?, // "start", "verify", "nest", "birth", "wean", "fail"
    femelle: Femelle?,
    males: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onStartCycle: (dateSaillie: String, male: String, notes: String) -> Unit,
    onVerifyGestation: (dateVerif: String, isPregnant: Boolean, notes: String) -> Unit,
    onConfirmNestBox: () -> Unit,
    onConfirmBirth: (dateNaissance: String, vivants: Int, morts: Int, notes: String) -> Unit,
    onConfirmWeaning: (dateSevrage: String, sevres: Int, notes: String) -> Unit,
    onStopCycle: (motif: String, notes: String) -> Unit
) {
    if (actionType == null || femelle == null) return
    val strings = LocalAppStrings.current

    var dateInput by remember(femelle, actionType) { mutableStateOf(DateHelper.todayIso()) }
    var maleInput by remember(femelle, actionType) { mutableStateOf(femelle.cycle?.maleNumero ?: "") }
    var notesInput by remember(femelle, actionType) { mutableStateOf("") }
    var vivantsInput by remember(femelle, actionType) { mutableStateOf("") }
    var mortsInput by remember(femelle, actionType) { mutableStateOf("0") }
    var sevresInput by remember(femelle, actionType) {
        mutableStateOf((femelle.nursing ?: femelle.cycle)?.nombreVivants?.toString() ?: "")
    }
    var motifFail by remember(femelle, actionType) { mutableStateOf("fausse_couche") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                when (actionType) {
                    "start" -> {
                        Text(strings.dialogMatingTitle, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        Text(strings.rabbitIndicator(femelle.numero), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(10.dp))
                        CuniTipBox(strings.matingTip)
                        MatingChecks(femelle)
                        OutlinedTextField(
                            value = dateInput,
                            onValueChange = { dateInput = it },
                            label = { Text(strings.inputMatingDate) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = maleInput,
                            onValueChange = { maleInput = it },
                            label = { Text(strings.inputMale) },
                            placeholder = { Text(strings.inputMalePlaceholder) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        MaleChips(males, maleInput) { maleInput = it }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = notesInput,
                            onValueChange = { notesInput = it },
                            label = { Text(strings.inputObsOptional) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { onStartCycle(dateInput, maleInput, notesInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = PinkSaillie, contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(strings.btnValidateMating, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    "verify" -> {
                        Text(strings.dialogPalpationTitle, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        Text(strings.rabbitIndicator(femelle.numero), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(10.dp))
                        CuniTipBox(strings.palpationTip)
                        OutlinedTextField(
                            value = dateInput,
                            onValueChange = { dateInput = it },
                            label = { Text(strings.inputControlDate) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = notesInput,
                            onValueChange = { notesInput = it },
                            label = { Text(strings.inputObsOptional) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { onVerifyGestation(dateInput, true, notesInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(strings.btnPregnantYes, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onVerifyGestation(dateInput, false, notesInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = RedCritical, contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(strings.btnPregnantNo, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    "nest" -> {
                        Text(strings.dialogNestTitle, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        Text(strings.rabbitIndicator(femelle.numero), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(10.dp))
                        CuniTipBox(strings.nestTip)
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onConfirmNestBox,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(strings.btnConfirmNest, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    "birth" -> {
                        Text(strings.dialogBirthTitle, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        Text(strings.rabbitIndicator(femelle.numero), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(10.dp))
                        CuniTipBox(strings.birthTip)
                        OutlinedTextField(
                            value = dateInput,
                            onValueChange = { dateInput = it },
                            label = { Text(strings.inputDeliveryDate) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = vivantsInput,
                                onValueChange = { vivantsInput = it.filter { ch -> ch.isDigit() } },
                                label = { Text(strings.inputBornAlive) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = mortsInput,
                                onValueChange = { mortsInput = it.filter { ch -> ch.isDigit() } },
                                label = { Text(strings.inputStillborn) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = notesInput,
                            onValueChange = { notesInput = it },
                            label = { Text(strings.inputLitterNotes) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                val v = vivantsInput.toIntOrNull() ?: 0
                                val m = mortsInput.toIntOrNull() ?: 0
                                onConfirmBirth(dateInput, v, m, notesInput)
                            },
                            enabled = vivantsInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(strings.btnSaveBirth, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    "wean" -> {
                        Text(strings.dialogWeaningTitle, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        Text(strings.rabbitIndicator(femelle.numero), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(10.dp))
                        CuniTipBox(strings.weaningTip)
                        OutlinedTextField(
                            value = dateInput,
                            onValueChange = { dateInput = it },
                            label = { Text(strings.inputWeaningDate) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = sevresInput,
                            onValueChange = { sevresInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text(strings.inputWeanedAlive) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        val nesVivants = femelle.cycle?.nombreVivants ?: 0
                        val sevresCount = sevresInput.toIntOrNull()
                        if (nesVivants > 0 && sevresCount != null) {
                            val pct = ((sevresCount.toDouble() / nesVivants) * 100.0).roundToInt()
                            Text(
                                text = strings.nestSurvivalRate(sevresCount, nesVivants, pct),
                                fontSize = 12.5.sp,
                                color = brandTextColor(),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = notesInput,
                            onValueChange = { notesInput = it },
                            label = { Text(strings.inputWeaningNotes) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                val s = sevresInput.toIntOrNull() ?: 0
                                onConfirmWeaning(dateInput, s, notesInput)
                            },
                            enabled = sevresInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(strings.btnCloseCycleSuccess, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    "fail" -> {
                        Text(
                            strings.dialogIncidentTitle,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = redTextColor()
                        )
                        Text(strings.rabbitIndicator(femelle.numero), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(10.dp))

                        val motifs = listOf(
                            "fausse_couche" to strings.motifMiscarriage,
                            "non_gestante" to strings.motifNotPregnant,
                            "mortalite_portee" to strings.motifLitterMortality,
                            "abandon" to strings.motifAbandon,
                            "autre" to strings.motifOther
                        )
                        motifs.forEach { (code, label) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { motifFail = code }
                                    .padding(vertical = 2.dp)
                            ) {
                                RadioButton(
                                    selected = motifFail == code,
                                    onClick = { motifFail = code }
                                )
                                Text(label, fontSize = 13.5.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = notesInput,
                            onValueChange = { notesInput = it },
                            label = { Text(strings.inputIncidentDetails) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { onStopCycle(motifFail, notesInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = RedCritical, contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(strings.btnConfirmStopCycle, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(strings.close)
                }
            }
        }
    }
}

@Composable
fun CuniTipBox(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(BrandContainer)
            .padding(10.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF1E1B4B),
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
    }
}

