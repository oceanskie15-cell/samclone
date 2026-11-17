package com.samcloner.app.model

import java.util.UUID
import kotlin.random.Random

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val isSystemApp: Boolean
)

data class VirtualLocationConfig(
    val enabled: Boolean = false,
    val latitude: Double = 23.2599,   // Default: Bhopal
    val longitude: Double = 77.4126,
    val label: String = "Bhopal, IN"
)

data class DeviceDisguiseConfig(
    val enabled: Boolean = false,
    val brand: String = "Google",
    val manufacturer: String = "Google",
    val model: String = "Pixel 9 Pro",
    val device: String = "caiman",
    val product: String = "caiman",
    val androidId: String = randomHex(16),
    val wifiMac: String = randomMac()
) {
    companion object {
        fun randomHex(length: Int): String {
            val chars = "0123456789abcdef"
            return buildString {
                repeat(length) { append(chars[Random.nextInt(chars.length)]) }
            }
        }

        fun randomMac(): String {
            val bytes = ByteArray(6) { Random.nextInt(256).toByte() }
            bytes[0] = (bytes[0].toInt() and 0xFE or 0x02).toByte()
            return bytes.joinToString(":") { "%02X".format(it) }
        }
    }
}

object DevicePresets {
    val all = listOf(
        DeviceDisguiseConfig(
            enabled = true,
            brand = "Google",
            manufacturer = "Google",
            model = "Pixel 9 Pro",
            device = "caiman",
            product = "caiman"
        ),
        DeviceDisguiseConfig(
            enabled = true,
            brand = "samsung",
            manufacturer = "samsung",
            model = "SM-S928B",
            device = "e3q",
            product = "e3qxxx"
        ),
        DeviceDisguiseConfig(
            enabled = true,
            brand = "OnePlus",
            manufacturer = "OnePlus",
            model = "CPH2581",
            device = "OP5958L1",
            product = "CPH2581"
        ),
        DeviceDisguiseConfig(
            enabled = true,
            brand = "Xiaomi",
            manufacturer = "Xiaomi",
            model = "24030PN60G",
            device = "aurora",
            product = "aurora_global"
        )
    )
}

enum class CloneIconStyle(val displayName: String) {
    BADGE_NUMBER("Number Badge"),
    BORDER_RING("Color Ring"),
    MINIMAL_DOT("Minimal Dot")
}

data class ClonedApp(
    val id: String = UUID.randomUUID().toString(),
    val originalPackageName: String,
    val originalAppName: String,
    val customLabel: String,
    val userId: Int,
    val badgeColorHex: Long = 0xFF4F8DFDL,
    val iconStyle: CloneIconStyle = CloneIconStyle.BADGE_NUMBER,
    val virtualLocation: VirtualLocationConfig = VirtualLocationConfig(),
    val deviceDisguise: DeviceDisguiseConfig = DeviceDisguiseConfig(),
    val createdAt: Long = System.currentTimeMillis()
)
