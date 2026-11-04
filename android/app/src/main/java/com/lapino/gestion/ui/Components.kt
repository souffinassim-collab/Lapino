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
    val cycle = femelle.cycle

    var statusLabel = "En repos"
    var statusColor = Color(0xFF757575)
    var statusBg = Color(0xFFEEEEEE)
    var progress = 0f
    var detailText = "Prête pour une nouvelle saillie"
    var actionLabel = "Mettre au mâle"
    var actionType = "start"

    var alertText: String? = null
    var alertBg = OrangeContainer
    var alertFg = OrangeAlert
    var showQuickNestBtn = false

    if (cycle != null) {
        when (cycle.statut) {
            "saillie" -> {
                val elapsed = DateHelper.daysElapsed(cycle.dateSaillie)
                statusLabel = "Saillie (J$elapsed)"
                statusColor = PinkSaillie
                statusBg = PinkContainer
                progress = (elapsed.toFloat() / 11f).coerceIn(0f, 1f)
                detailText = "Accouplée le ${DateHelper.formatFr(cycle.dateSaillie)}" +
                        (cycle.maleNumero?.let { " • Mâle: $it" } ?: "")
                actionLabel = "Palpation (Diagnostic)"
                actionType = "verify"

                if (elapsed >= 10) {
                    alertText = "🔍 Palpation à réaliser ! (J$elapsed - ampoules fœtales palpables)"
                    alertBg = OrangeContainer
                    alertFg = OrangeAlert
                } else {
                    alertText = "Palpation conseillée à J11 (dans ${max(1, 11 - elapsed)}j)"
                    alertBg = BlueContainer
                    alertFg = BlueGestante
                }
            }
            "gestante" -> {
                val elapsed = DateHelper.daysElapsed(cycle.dateSaillie)
                val daysLeft = DateHelper.daysUntil(cycle.dateMiseBasPrevue)
                statusLabel = "Gestante (J$elapsed/31)"
                statusColor = BlueGestante
                statusBg = BlueContainer
                progress = DateHelper.calculateProgress(cycle.dateSaillie, cycle.dateMiseBasPrevue)
                detailText = when {
                    daysLeft != null && daysLeft < 0 -> "⚠️ Mise bas dépassée de ${abs(daysLeft)} jour(s) !"
                    daysLeft == 0 -> "🐰 Mise bas prévue aujourd'hui !"
                    else -> "Mise bas prévue dans ${daysLeft ?: "?"}j (${DateHelper.formatFr(cycle.dateMiseBasPrevue)})"
                }
                actionLabel = "Déclarer Naissance"
                actionType = "birth"

                if (elapsed >= 27 && !cycle.boiteNidPosee) {
                    alertText = "📦 Urgent (J$elapsed) : Poser la boîte à nid avec paille !"
                    alertBg = RedContainer
                    alertFg = RedCritical
                    showQuickNestBtn = true
                } else if (cycle.boiteNidPosee) {
                    alertText = "✓ Boîte à nid propre installée"
                    alertBg = GreenContainer
                    alertFg = GreenPrimary
                }
            }
            "allaitante" -> {
                val elapsed = DateHelper.daysElapsed(cycle.dateMiseBasReelle)
                val vivants = cycle.nombreVivants ?: 0
                statusLabel = "Allaitante (J$elapsed/35)"
                statusColor = GreenPrimary
                statusBg = GreenContainer
                progress = DateHelper.calculateProgress(cycle.dateMiseBasReelle, cycle.dateSevragePrevue)
                detailText = "$vivants lapereau(x) vivant(s) • Nés le ${DateHelper.formatFr(cycle.dateMiseBasReelle)}"
                actionLabel = if (elapsed >= 30) "Effectuer Sevrage" else "Sevrage"
                actionType = "wean"

                if (elapsed >= 32) {
                    alertText = "🧺 Sevrage recommandé (J$elapsed - prêts pour l'engraissement)"
                    alertBg = OrangeContainer
                    alertFg = OrangeAlert
                } else if (elapsed >= 21) {
                    alertText = "Sortie du nid : les petits commencent à manger du solide"
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
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                        text = "🏠 Loge : ${femelle.clapetNumero?.let { "Clapet $it" } ?: "Non assignée"}",
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
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Nid posé ✓", fontSize = 11.sp)
                        }
                    }
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
                            contentDescription = "Signaler un incident",
                            tint = RedCritical
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Button(
                    onClick = { onAction(femelle, actionType) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (cycle == null) GreenPrimary else statusColor
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(actionLabel, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun ReproductionDialog(
    actionType: String?, // "start", "verify", "nest", "birth", "wean", "fail"
    femelle: Femelle?,
    onDismiss: () -> Unit,
    onStartCycle: (dateSaillie: String, male: String, notes: String) -> Unit,
    onVerifyGestation: (dateVerif: String, isPregnant: Boolean, notes: String) -> Unit,
    onConfirmNestBox: () -> Unit,
    onConfirmBirth: (dateNaissance: String, vivants: Int, morts: Int, notes: String) -> Unit,
    onConfirmWeaning: (dateSevrage: String, sevres: Int, notes: String) -> Unit,
    onStopCycle: (motif: String, notes: String) -> Unit
) {
    if (actionType == null || femelle == null) return

    var dateInput by remember(femelle, actionType) { mutableStateOf(DateHelper.todayIso()) }
    var maleInput by remember(femelle, actionType) { mutableStateOf(femelle.cycle?.maleNumero ?: "") }
    var notesInput by remember(femelle, actionType) { mutableStateOf("") }
    var vivantsInput by remember(femelle, actionType) { mutableStateOf("") }
    var mortsInput by remember(femelle, actionType) { mutableStateOf("0") }
    var sevresInput by remember(femelle, actionType) {
        mutableStateOf(femelle.cycle?.nombreVivants?.toString() ?: "")
    }
    var motifFail by remember(femelle, actionType) { mutableStateOf("fausse_couche") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
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
                        Text("🌪️ Déclarer une Saillie", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        Text("Lapine : ${femelle.numero}", fontSize = 13.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(10.dp))
                        CuniTipBox(
                            "💡 Conseil : Amenez toujours la lapine dans la loge du mâle (jamais l'inverse). " +
                                    "La palpation abdominale sera programmée à J+11."
                        )
                        OutlinedTextField(
                            value = dateInput,
                            onValueChange = { dateInput = it },
                            label = { Text("Date de saillie (AAAA-MM-JJ)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = maleInput,
                            onValueChange = { maleInput = it },
                            label = { Text("Mâle reproducteur (Nom / N°)") },
                            placeholder = { Text("Ex: M01 - Rex") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = notesInput,
                            onValueChange = { notesInput = it },
                            label = { Text("Observations (Optionnel)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { onStartCycle(dateInput, maleInput, notesInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = PinkSaillie),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Valider la Saillie", fontWeight = FontWeight.Bold)
                        }
                    }

                    "verify" -> {
                        Text("🩺 Palpation Abdominale", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        Text("Lapine : ${femelle.numero}", fontSize = 13.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(10.dp))
                        CuniTipBox(
                            "💡 Palpez délicatement le bas-ventre entre le pouce et les doigts. " +
                                    "Entre J10 et J14, les embryons forment un chapelet de billes de la taille d'une noisette."
                        )
                        OutlinedTextField(
                            value = dateInput,
                            onValueChange = { dateInput = it },
                            label = { Text("Date du contrôle (AAAA-MM-JJ)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = notesInput,
                            onValueChange = { notesInput = it },
                            label = { Text("Observations (Optionnel)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { onVerifyGestation(dateInput, true, notesInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("✅ OUI - Gestante confirmée", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onVerifyGestation(dateInput, false, notesInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = RedCritical),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("❌ NON - Vide (Remettre en repos)", fontWeight = FontWeight.Bold)
                        }
                    }

                    "nest" -> {
                        Text("📦 Pose de la Boîte à Nid", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        Text("Lapine : ${femelle.numero}", fontSize = 13.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(10.dp))
                        CuniTipBox(
                            "💡 À J28 de gestation (3 jours avant la mise bas), installez une boîte à nid propre " +
                                    "garnie de copeaux et de paille sèche. La lapine s'arrachera les poils du ventre pour le capitonner."
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onConfirmNestBox,
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Confirmer l'installation du nid ✓", fontWeight = FontWeight.Bold)
                        }
                    }

                    "birth" -> {
                        Text("🐰 Déclarer la Mise Bas", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        Text("Lapine : ${femelle.numero}", fontSize = 13.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(10.dp))
                        CuniTipBox(
                            "💡 Contrôlez le nid au calme : retirez les éventuels mort-nés et vérifiez que les ventres " +
                                    "des petits sont bien rebondis et blancs de lait."
                        )
                        OutlinedTextField(
                            value = dateInput,
                            onValueChange = { dateInput = it },
                            label = { Text("Date de mise bas (AAAA-MM-JJ)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = vivantsInput,
                                onValueChange = { vivantsInput = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Nés vivants") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = mortsInput,
                                onValueChange = { mortsInput = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Mort-nés") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = notesInput,
                            onValueChange = { notesInput = it },
                            label = { Text("Notes sur la portée (Optionnel)") },
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
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Enregistrer la Naissance", fontWeight = FontWeight.Bold)
                        }
                    }

                    "wean" -> {
                        Text("🧺 Déclarer le Sevrage", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        Text("Lapine : ${femelle.numero}", fontSize = 13.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(10.dp))
                        CuniTipBox(
                            "💡 Le sevrage (30 à 35 jours) sépare les lapereaux de la mère vers l'engraissement. " +
                                    "La lapine retourne au statut Repos."
                        )
                        OutlinedTextField(
                            value = dateInput,
                            onValueChange = { dateInput = it },
                            label = { Text("Date de sevrage (AAAA-MM-JJ)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = sevresInput,
                            onValueChange = { sevresInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Lapereaux sevrés vivants") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        val nesVivants = femelle.cycle?.nombreVivants ?: 0
                        val sevresCount = sevresInput.toIntOrNull()
                        if (nesVivants > 0 && sevresCount != null) {
                            val pct = ((sevresCount.toDouble() / nesVivants) * 100.0).roundToInt()
                            Text(
                                text = "Taux de survie au nid : $sevresCount / $nesVivants petits ($pct%)",
                                fontSize = 12.5.sp,
                                color = GreenPrimary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = notesInput,
                            onValueChange = { notesInput = it },
                            label = { Text("Notes / Cage engraissement (Optionnel)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                val s = sevresInput.toIntOrNull() ?: 0
                                onConfirmWeaning(dateInput, s, notesInput)
                            },
                            enabled = sevresInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Clôturer le Cycle (Sevrage Réussi)", fontWeight = FontWeight.Bold)
                        }
                    }

                    "fail" -> {
                        Text(
                            "⚠️ Signaler un Incident",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = RedCritical
                        )
                        Text("Lapine : ${femelle.numero}", fontSize = 13.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(10.dp))

                        val motifs = listOf(
                            "fausse_couche" to "Fausse couche / avortement",
                            "non_gestante" to "Non gestante (vide)",
                            "mortalite_portee" to "Mortalité complète de la portée",
                            "abandon" to "Abandon du nid / manque de lait",
                            "autre" to "Autre incident"
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
                            label = { Text("Détails de l'incident (Optionnel)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { onStopCycle(motifFail, notesInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = RedCritical),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Confirmer et Remettre en Repos", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Fermer")
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
            .background(GreenContainer)
            .padding(10.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF1B5E20),
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
    }
}

