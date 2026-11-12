package com.lapino.gestion.license

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.Settings
import android.util.Base64
import com.lapino.gestion.model.DateHelper
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.UUID

enum class LicenseType { FREE, YEAR, LIFE }

data class LicenseStatus(
    val deviceCode: String,
    val type: LicenseType,
    val expiryIso: String? = null,
    val isExpired: Boolean = false
) {
    val isLicensed: Boolean get() = (type == LicenseType.LIFE || type == LicenseType.YEAR) && !isExpired
}

enum class ActivationResult {
    SUCCESS,
    INVALID_FORMAT,
    INVALID_SIGNATURE,
    WRONG_DEVICE,
    EXPIRED
}

/**
 * Gestionnaire de licence 100 % hors-ligne basé sur ECDSA P-256 (SHA256withECDSA).
 * La clé publique vérifie les clés générées par l'outil vendeur sans jamais exposer la clé privée.
 */
object LicenseManager {
    const val WHATSAPP_NUMBER = "213770208626"
    const val FREE_MAX_FEMALES = 5

    private const val PREFS = "lapino_license"
    private const val KEY_LICENSE = "license_key"
    private const val KEY_FALLBACK_ID = "fallback_device_id"

    // Clé publique ECDSA P-256 (X.509 DER Base64)
    private const val PUBLIC_KEY_B64 =
        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE7a0B98qpU6Ootx/aQZ/HvBSmUpU3fbu/opPbF2KjDpIQ/tPs0f/d3/Edcee8LYVxuW/ZUNATTWv5v/4Qr9Xo5g=="

    private fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    @SuppressLint("HardwareIds")
    fun getDeviceCode(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?.takeIf { it.isNotBlank() && it != "9774d56d682e549c" }
            ?: run {
                val p = prefs(context)
                p.getString(KEY_FALLBACK_ID, null) ?: UUID.randomUUID().toString().also {
                    p.edit().putString(KEY_FALLBACK_ID, it).apply()
                }
            }
        val digest = MessageDigest.getInstance("SHA-256").digest("LAPINO:$androidId".toByteArray(Charsets.UTF_8))
        val hex = digest.take(4).joinToString("") { "%02X".format(it) }
        return "LPN-${hex.substring(0, 4)}-${hex.substring(4, 8)}"
    }

    fun getStatus(context: Context): LicenseStatus {
        val deviceCode = getDeviceCode(context)
        val savedKey = prefs(context).getString(KEY_LICENSE, null)?.trim()
        if (savedKey.isNullOrBlank()) {
            return LicenseStatus(deviceCode = deviceCode, type = LicenseType.FREE)
        }
        val parsed = parseAndVerify(savedKey, deviceCode) ?: return LicenseStatus(deviceCode = deviceCode, type = LicenseType.FREE)
        val (type, expiryIso) = parsed
        val expired = type == LicenseType.YEAR && isDateExpired(expiryIso)
        return LicenseStatus(
            deviceCode = deviceCode,
            type = type,
            expiryIso = if (type == LicenseType.YEAR) expiryIso else null,
            isExpired = expired
        )
    }

    fun activate(context: Context, rawKey: String): ActivationResult {
        val key = rawKey.trim().replace("\\s+".toRegex(), "")
        val parts = key.split(".")
        if (parts.size != 3 || parts[0] != "LPN1") return ActivationResult.INVALID_FORMAT

        val payloadBytes = decodeBase64Url(parts[1]) ?: return ActivationResult.INVALID_FORMAT
        val sigBytes = decodeBase64Url(parts[2]) ?: return ActivationResult.INVALID_FORMAT
        if (!verifySignature(payloadBytes, sigBytes)) return ActivationResult.INVALID_SIGNATURE

        val payloadStr = String(payloadBytes, Charsets.UTF_8)
        val fields = payloadStr.split("|")
        if (fields.size != 3) return ActivationResult.INVALID_FORMAT

        val keyDevice = fields[0]
        val typeStr = fields[1]
        val expiryIso = fields[2]

        val myDevice = getDeviceCode(context)
        if (!keyDevice.equals(myDevice, ignoreCase = true)) return ActivationResult.WRONG_DEVICE

        val type = when (typeStr) {
            "LIFE" -> LicenseType.LIFE
            "YEAR" -> LicenseType.YEAR
            else -> return ActivationResult.INVALID_FORMAT
        }
        if (type == LicenseType.YEAR && isDateExpired(expiryIso)) return ActivationResult.EXPIRED

        prefs(context).edit().putString(KEY_LICENSE, key).apply()
        return ActivationResult.SUCCESS
    }

    fun buildWhatsAppUri(deviceCode: String, isArabic: Boolean): Uri {
        val msg = if (isArabic) {
            "السلام عليكم، أريد تفعيل رخصة تطبيق Lapino.\nرمز جهازي هو: $deviceCode"
        } else {
            "Bonjour, je souhaite activer une licence Lapino.\nMon code appareil est : $deviceCode"
        }
        return Uri.parse("https://wa.me/$WHATSAPP_NUMBER?text=${Uri.encode(msg)}")
    }

    private fun parseAndVerify(key: String, deviceCode: String): Pair<LicenseType, String>? {
        val parts = key.split(".")
        if (parts.size != 3 || parts[0] != "LPN1") return null
        val payloadBytes = decodeBase64Url(parts[1]) ?: return null
        val sigBytes = decodeBase64Url(parts[2]) ?: return null
        if (!verifySignature(payloadBytes, sigBytes)) return null

        val fields = String(payloadBytes, Charsets.UTF_8).split("|")
        if (fields.size != 3) return null
        if (!fields[0].equals(deviceCode, ignoreCase = true)) return null

        val type = when (fields[1]) {
            "LIFE" -> LicenseType.LIFE
            "YEAR" -> LicenseType.YEAR
            else -> return null
        }
        return type to fields[2]
    }

    private fun isDateExpired(expiryIso: String): Boolean {
        if (expiryIso == "0000-00-00") return false
        val daysLeft = DateHelper.daysUntil(expiryIso) ?: return true
        return daysLeft < 0
    }

    private fun decodeBase64Url(s: String): ByteArray? = try {
        Base64.decode(s, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    } catch (_: Throwable) {
        null
    }

    private fun verifySignature(payload: ByteArray, rawOrDerSig: ByteArray): Boolean = try {
        val derSig = if (rawOrDerSig.size == 64) rawP1363ToDer(rawOrDerSig) else rawOrDerSig
        val pubBytes = Base64.decode(PUBLIC_KEY_B64, Base64.DEFAULT)
        val pubKey = KeyFactory.getInstance("EC").generatePublic(X509EncodedKeySpec(pubBytes))
        val verifier = Signature.getInstance("SHA256withECDSA")
        verifier.initVerify(pubKey)
        verifier.update(payload)
        verifier.verify(derSig)
    } catch (_: Throwable) {
        false
    }

    /** Convertit une signature ECDSA P-256 brute (r||s, 64 octets) en ASN.1 DER pour Android API 24+. */
    private fun rawP1363ToDer(raw: ByteArray): ByteArray {
        val r = toDerInt(raw.copyOfRange(0, 32))
        val s = toDerInt(raw.copyOfRange(32, 64))
        val totalLen = 2 + r.size + 2 + s.size
        val out = ByteArray(2 + totalLen)
        out[0] = 0x30
        out[1] = totalLen.toByte()
        var idx = 2
        out[idx++] = 0x02
        out[idx++] = r.size.toByte()
        r.copyInto(out, idx)
        idx += r.size
        out[idx++] = 0x02
        out[idx++] = s.size.toByte()
        s.copyInto(out, idx)
        return out
    }

    private fun toDerInt(bytes: ByteArray): ByteArray {
        var i = 0
        while (i < bytes.size - 1 && bytes[i] == 0.toByte()) i++
        val trimmed = bytes.copyOfRange(i, bytes.size)
        return if ((trimmed[0].toInt() and 0x80) != 0) byteArrayOf(0) + trimmed else trimmed
    }
}

