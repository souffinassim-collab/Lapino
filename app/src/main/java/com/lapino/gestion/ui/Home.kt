package com.lapino.gestion.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lapino.gestion.i18n.AppLanguage
import com.lapino.gestion.i18n.LocalAppStrings
import com.lapino.gestion.i18n.menu
import com.lapino.gestion.model.CycleRules
import com.lapino.gestion.model.GlobalStats
import java.io.File
import java.io.FileOutputStream

/** Préférences simples de l'application (nom de l'élevage, thème, règle de remise au mâle, photo). */
object AppPrefs {
    private const val PREFS = "lapino_prefs"
    private const val PROFILE_FILE = "profile.jpg"

    private fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun farmName(c: Context): String = prefs(c).getString("farm_name", "") ?: ""
    fun setFarmName(c: Context, v: String) = prefs(c).edit().putString("farm_name", v.trim()).apply()

    fun isDark(c: Context): Boolean = prefs(c).getBoolean("dark_mode", false)
    fun setDark(c: Context, v: Boolean) = prefs(c).edit().putBoolean("dark_mode", v).apply()

    fun loadRules(c: Context) {
        val d = prefs(c).getInt("remate_days", 11)
        CycleRules.remateDays = if (d in CycleRules.REMATE_OPTIONS) d else 11
    }

    fun setRemateDays(c: Context, v: Int) {
        CycleRules.remateDays = v
        prefs(c).edit().putInt("remate_days", v).apply()
    }

    fun loadProfile(c: Context): Bitmap? {
        val f = File(c.filesDir, PROFILE_FILE)
        if (!f.exists()) return null
        return try {
            BitmapFactory.decodeFile(f.absolutePath)
        } catch (_: Throwable) {
            null
        }
    }

    /** Copie la photo choisie dans le stockage privé, réduite à ~512 px. */
    fun saveProfile(c: Context, uri: Uri): Bitmap? {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            c.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1
            while (bounds.outWidth / sample > 1024 || bounds.outHeight / sample > 1024) sample *= 2
            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            val bmp = c.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
                ?: return null
            FileOutputStream(File(c.filesDir, PROFILE_FILE)).use { bmp.compress(Bitmap.CompressFormat.JPEG, 85, it) }
            bmp
        } catch (_: Throwable) {
            null
        }
    }
}

private data class Tile(
    val key: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val colors: List<Color>,
    val badge: Int = 0
)

@Composable
fun HomeScreen(
    farmName: String,
    stats: GlobalStats,
    malesCount: Int,
    littersCount: Int,
    reproAlertsCount: Int,
    vaccineAlertsCount: Int,
    lowFeedCount: Int,
    salesTotal: Double,
    onOpen: (String) -> Unit,
    onOpenFemalesFilter: (String) -> Unit = {},
    onQuickAction: (String) -> Unit = {},
    onOpenSettings: () -> Unit
) {
    val ms = LocalAppStrings.current.menu
    val context = LocalContext.current
    var profile by remember { mutableStateOf(AppPrefs.loadProfile(context)) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) AppPrefs.saveProfile(context, uri)?.let { profile = it }
    }
    val totalAlerts = reproAlertsCount + vaccineAlertsCount + lowFeedCount

    val tiles = listOf(
        Tile("femelles", ms.tileFemales, ms.countFemales(stats.totalFemelles), Icons.Default.Pets,
            listOf(Color(0xFFEC4899), Color(0xFFF472B6)), reproAlertsCount),
        Tile("males", ms.tileMales, ms.countMales(malesCount), Icons.Default.Male,
            listOf(Color(0xFF3B82F6), Color(0xFF60A5FA))),
        Tile("vaccins", ms.tileVaccines, ms.countVaccines(vaccineAlertsCount), Icons.Default.Vaccines,
            listOf(Color(0xFF10B981), Color(0xFF34D399)), vaccineAlertsCount),
        Tile("litters", ms.tileLitters, ms.countLitters(stats.lapereauxSousMere), Icons.Default.ChildCare,
            listOf(Color(0xFFF59E0B), Color(0xFFFBBF24))),
        Tile("feed", ms.tileFeed, if (lowFeedCount > 0) ms.countFeedLow(lowFeedCount) else ms.feedOk, Icons.Default.Grass,
            listOf(Color(0xFF84CC16), Color(0xFFA3E635)), lowFeedCount),
        Tile("cages", ms.tileCages, ms.countCages(stats.clapetsTotal), Icons.Default.HomeWork,
            listOf(Color(0xFF8B5CF6), Color(0xFFA78BFA))),
        Tile("tips", ms.tileTips, ms.tipsSubtitle, Icons.Default.Lightbulb,
            listOf(Color(0xFF06B6D4), Color(0xFF22D3EE))),
        Tile("sales", ms.tileSales, "${fmtNum(salesTotal)} ${ms.currency}", Icons.Default.Payments,
            listOf(Color(0xFFEF4444), Color(0xFFFB7185)))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = 14.dp, bottom = 28.dp)
    ) {
        // En-tête : photo de profil, nom de l'élevage, notifications, réglages
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .pressScale { picker.launch("image/*") }
                    .clip(CircleShape)
                    .background(HeroGradient)
                    .border(2.dp, Color.White.copy(alpha = 0.85f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val bmp = profile
                if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = ms.tapToChangePhoto,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("🐰", fontSize = 28.sp)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(ms.greeting, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    farmName.ifBlank { ms.defaultFarmName },
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            GlassIconButton(Icons.Default.Notifications, ms.notifications, badge = totalAlerts) { onOpen("alerts") }
            Spacer(Modifier.width(8.dp))
            GlassIconButton(Icons.Default.Settings, ms.settings, onClick = onOpenSettings)
        }

        Spacer(Modifier.height(18.dp))

        // Carte « héro » : résumé du cheptel et actions du jour
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .staggeredAppear(0)
                .pressScale { onOpen("alerts") }
                .clip(RoundedCornerShape(30.dp))
                .background(HeroGradient)
        ) {
            // reflets de verre
            Box(Modifier.matchParentSize()) {
            Box(
                Modifier
                    .size(190.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 60.dp, y = (-70).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.16f))
            )
            Box(
                Modifier
                    .size(120.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-40).dp, y = 50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.10f))
            )
            }
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    ms.herdSummary(stats.totalFemelles, malesCount, stats.lapereauxSousMere),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    if (totalAlerts > 0) ms.alertsToHandle(totalAlerts) else ms.noAlerts,
                    color = Color.White,
                    fontSize = 23.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HeroChip("💞", stats.enSaillie) { onOpenFemalesFilter("saillie") }
                    HeroChip("🤰", stats.gestantes) { onOpenFemalesFilter("gestante") }
                    HeroChip("🍼", stats.allaitantes) { onOpenFemalesFilter("allaitante") }
                    HeroChip("😴", stats.enRepos) { onOpenFemalesFilter("repos") }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Actions rapides en 1 geste
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionPill(
                label = ms.quickNewFemale,
                icon = Icons.Default.Add,
                tint = Color(0xFFEC4899),
                modifier = Modifier.weight(1f),
                onClick = { onQuickAction("new_female") }
            )
            QuickActionPill(
                label = ms.quickNewSale,
                icon = Icons.Default.Payments,
                tint = Color(0xFFEF4444),
                modifier = Modifier.weight(1f),
                onClick = { onQuickAction("new_sale") }
            )
            QuickActionPill(
                label = ms.quickNewPurchase,
                icon = Icons.Default.ShoppingCart,
                tint = Color(0xFF10B981),
                modifier = Modifier.weight(1f),
                onClick = { onQuickAction("new_purchase") }
            )
        }

        Spacer(Modifier.height(14.dp))

        // Grille de tuiles (ordre du croquis)
        tiles.chunked(2).forEachIndexed { rowIndex, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEachIndexed { i, tile ->
                    MenuTile(
                        tile = tile,
                        modifier = Modifier
                            .weight(1f)
                            .staggeredAppear(1 + rowIndex * 2 + i),
                        onClick = { onOpen(tile.key) }
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
private fun HeroChip(emoji: String, count: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .pressScale(onClick = onClick)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.22f))
            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(50))
            .padding(horizontal = 11.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 14.sp)
        Spacer(Modifier.width(5.dp))
        Text(count.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
private fun QuickActionPill(
    label: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(modifier = modifier.height(44.dp), shape = RoundedCornerShape(16.dp), onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(5.dp))
            Text(
                text = label,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MenuTile(tile: Tile, modifier: Modifier = Modifier, onClick: () -> Unit) {
    GlassCard(modifier = modifier.height(132.dp), shape = RoundedCornerShape(26.dp), onClick = onClick) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(tile.colors)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(tile.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.weight(1f))
                if (tile.badge > 0) Badge(tile.badge)
            }
            Spacer(Modifier.weight(1f))
            Text(
                tile.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                tile.subtitle,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Badge(count: Int) {
    val s = pulse()
    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = s
                scaleY = s
            }
            .defaultMinSize(minWidth = 22.dp, minHeight = 22.dp)
            .clip(CircleShape)
            .background(RedCritical)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(count.toString(), color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GlassIconButton(icon: ImageVector, description: String, badge: Int = 0, onClick: () -> Unit) {
    Box {
        GlassCard(modifier = Modifier.size(46.dp), shape = CircleShape, onClick = onClick) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = description, tint = MaterialTheme.colorScheme.onSurface)
            }
        }
        if (badge > 0) {
            Box(Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp)) { Badge(badge) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    farmName: String,
    isDark: Boolean,
    currentLang: AppLanguage,
    licenseSummary: String = "",
    isLicensed: Boolean = false,
    onOpenLicense: () -> Unit = {},
    onFarmNameChange: (String) -> Unit,
    onToggleDark: () -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit,
    onRulesChanged: () -> Unit,
    onDismiss: () -> Unit
) {
    val ms = LocalAppStrings.current.menu
    val context = LocalContext.current
    var name by remember { mutableStateOf(farmName) }
    var remate by remember { mutableIntStateOf(CycleRules.remateDays) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 4.dp) {
            Column(modifier = Modifier.padding(22.dp).verticalScroll(rememberScrollState())) {
                Text("⚙️ ${ms.settings}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        onFarmNameChange(it)
                    },
                    label = { Text(ms.farmName) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Text(ms.language, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppLanguage.values().forEach { lang ->
                        FilterChip(
                            selected = currentLang == lang,
                            onClick = { onLanguageChanged(lang) },
                            label = { Text("${lang.flag} ${lang.displayName}") }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(ms.darkMode, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Switch(checked = isDark, onCheckedChange = { onToggleDark() })
                }
                Spacer(Modifier.height(12.dp))
                Text(ms.remateRule, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(ms.remateRuleHelp, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                CycleRules.REMATE_OPTIONS.forEach { d ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                remate = d
                                AppPrefs.setRemateDays(context, d)
                                onRulesChanged()
                            }
                    ) {
                        RadioButton(selected = remate == d, onClick = null, modifier = Modifier.padding(8.dp))
                        Text(ms.remateOption(d), fontSize = 14.sp)
                    }
                }
                Spacer(Modifier.height(14.dp))
                if (licenseSummary.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isLicensed) MintContainer else BrandContainer)
                            .clickable { onOpenLicense() }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(ms.licenseTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isLicensed) MintNursing else BrandPrimary)
                            Text(licenseSummary, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Text(ms.licenseManage, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandPrimary)
                    }
                    Spacer(Modifier.height(14.dp))
                }
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Text(ms.close, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
