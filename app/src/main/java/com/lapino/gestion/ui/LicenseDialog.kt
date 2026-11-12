package com.lapino.gestion.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lapino.gestion.i18n.AppLanguage
import com.lapino.gestion.i18n.LocalAppStrings
import com.lapino.gestion.i18n.menu
import com.lapino.gestion.license.ActivationResult
import com.lapino.gestion.license.LicenseManager
import com.lapino.gestion.license.LicenseStatus
import com.lapino.gestion.license.LicenseType
import com.lapino.gestion.model.DateHelper

@Composable
fun LicenseDialog(
    status: LicenseStatus,
    limitReached: Boolean = false,
    onActivated: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val ms = strings.menu
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    var keyInput by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var copied by remember { mutableStateOf(false) }

    val statusText = when {
        status.type == LicenseType.LIFE -> ms.licenseLifeStatus
        status.type == LicenseType.YEAR && !status.isExpired ->
            ms.licenseYearStatus(DateHelper.formatFr(status.expiryIso))
        status.type == LicenseType.YEAR && status.isExpired ->
            ms.licenseExpiredStatus(DateHelper.formatFr(status.expiryIso))
        else -> ms.licenseFreeStatus
    }
    val statusBg = if (status.isLicensed) MintContainer else if (status.isExpired) RedContainer else BrandContainer
    val statusFg = if (status.isLicensed) MintNursing else if (status.isExpired) RedCritical else BrandPrimary

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = BrandPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text(ms.licenseTitle, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                }

                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(statusBg)
                        .padding(12.dp)
                ) {
                    Text(statusText, color = statusFg, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                }

                if (limitReached && !status.isLicensed) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = ms.licenseFreeLimitInfo(LicenseManager.FREE_MAX_FEMALES),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(14.dp))

                Text(ms.licenseDeviceCode, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = status.deviceCode,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(status.deviceCode))
                            copied = true
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (copied) ms.licenseCopied else ms.licenseCopy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        val uri = LicenseManager.buildWhatsAppUri(status.deviceCode, strings.lang == AppLanguage.AR)
                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("💬 ${ms.licenseOrderWhatsApp}", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                }

                Spacer(Modifier.height(14.dp))

                OutlinedTextField(
                    value = keyInput,
                    onValueChange = {
                        keyInput = it
                        feedback = null
                    },
                    label = { Text(ms.licenseKeyLabel) },
                    trailingIcon = {
                        IconButton(onClick = {
                            clipboard.getText()?.text?.let {
                                keyInput = it.trim()
                                feedback = null
                            }
                        }) {
                            Icon(Icons.Default.ContentPaste, contentDescription = ms.licensePaste)
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                feedback?.let { (ok, msg) ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = msg,
                        color = if (ok) MintNursing else RedCritical,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(ms.close)
                    }
                    Button(
                        onClick = {
                            when (LicenseManager.activate(context, keyInput)) {
                                ActivationResult.SUCCESS -> {
                                    feedback = true to ms.licenseSuccess
                                    onActivated()
                                }
                                ActivationResult.INVALID_FORMAT -> feedback = false to ms.licenseErrFormat
                                ActivationResult.INVALID_SIGNATURE -> feedback = false to ms.licenseErrSignature
                                ActivationResult.WRONG_DEVICE -> feedback = false to ms.licenseErrDevice
                                ActivationResult.EXPIRED -> feedback = false to ms.licenseErrExpired
                            }
                        },
                        enabled = keyInput.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = Color.White)
                    ) {
                        Text(ms.licenseActivate, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

