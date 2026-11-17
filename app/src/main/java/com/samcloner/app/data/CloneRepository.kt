package com.samcloner.app.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.samcloner.app.engine.VirtualEngine
import com.samcloner.app.model.CloneIconStyle
import com.samcloner.app.model.ClonedApp
import com.samcloner.app.model.DeviceDisguiseConfig
import com.samcloner.app.model.InstalledAppInfo
import com.samcloner.app.model.VirtualLocationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class CloneRepository(
    private val context: Context,
    private val engine: VirtualEngine
) {
    private val storageFile = File(context.filesDir, "sam_cloner_profiles.json")
    private val _clones = MutableStateFlow<List<ClonedApp>>(emptyList())
    val clones: StateFlow<List<ClonedApp>> = _clones.asStateFlow()

    init {
        _clones.value = loadFromDisk()
    }

    suspend fun getInstalledApps(): List<InstalledAppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.packageName != context.packageName }
            .map { appInfo ->
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val label = pm.getApplicationLabel(appInfo).toString()
                val version = runCatching {
                    pm.getPackageInfo(appInfo.packageName, 0).versionName ?: "1.0"
                }.getOrDefault("1.0")
                InstalledAppInfo(
                    packageName = appInfo.packageName,
                    appName = label,
                    versionName = version,
                    isSystemApp = isSystem
                )
            }
            .sortedWith(compareBy<InstalledAppInfo> { it.isSystemApp }.thenBy { it.appName.lowercase() })
    }

    suspend fun createClone(app: InstalledAppInfo): Result<ClonedApp> {
        val existingForPkg = _clones.value.filter { it.originalPackageName == app.packageName }
        val nextUserId = (existingForPkg.maxOfOrNull { it.userId } ?: -1) + 1
        val installResult = engine.installClone(app.packageName, nextUserId)
        if (installResult.isFailure) {
            return Result.failure(installResult.exceptionOrNull() ?: Exception("Install failed"))
        }

        val clone = ClonedApp(
            originalPackageName = app.packageName,
            originalAppName = app.appName,
            customLabel = "${app.appName} (${nextUserId + 1})",
            userId = nextUserId
        )
        val updated = _clones.value + clone
        _clones.value = updated
        saveToDisk(updated)
        return Result.success(clone)
    }

    fun updateClone(updatedClone: ClonedApp) {
        val updated = _clones.value.map { if (it.id == updatedClone.id) updatedClone else it }
        _clones.value = updated
        saveToDisk(updated)
    }

    suspend fun deleteClone(clone: ClonedApp) {
        engine.uninstallClone(clone)
        val updated = _clones.value.filterNot { it.id == clone.id }
        _clones.value = updated
        saveToDisk(updated)
    }

    fun getCloneById(id: String): ClonedApp? = _clones.value.firstOrNull { it.id == id }

    private fun saveToDisk(list: List<ClonedApp>) {
        val arr = JSONArray()
        list.forEach { clone ->
            val obj = JSONObject().apply {
                put("id", clone.id)
                put("originalPackageName", clone.originalPackageName)
                put("originalAppName", clone.originalAppName)
                put("customLabel", clone.customLabel)
                put("userId", clone.userId)
                put("badgeColorHex", clone.badgeColorHex)
                put("iconStyle", clone.iconStyle.name)
                put("createdAt", clone.createdAt)
                put("virtualLocation", JSONObject().apply {
                    put("enabled", clone.virtualLocation.enabled)
                    put("latitude", clone.virtualLocation.latitude)
                    put("longitude", clone.virtualLocation.longitude)
                    put("label", clone.virtualLocation.label)
                })
                put("deviceDisguise", JSONObject().apply {
                    put("enabled", clone.deviceDisguise.enabled)
                    put("brand", clone.deviceDisguise.brand)
                    put("manufacturer", clone.deviceDisguise.manufacturer)
                    put("model", clone.deviceDisguise.model)
                    put("device", clone.deviceDisguise.device)
                    put("product", clone.deviceDisguise.product)
                    put("androidId", clone.deviceDisguise.androidId)
                    put("wifiMac", clone.deviceDisguise.wifiMac)
                })
            }
            arr.put(obj)
        }
        storageFile.writeText(arr.toString())
    }

    private fun loadFromDisk(): List<ClonedApp> {
        if (!storageFile.exists()) return emptyList()
        return runCatching {
            val arr = JSONArray(storageFile.readText())
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val locObj = o.optJSONObject("virtualLocation")
                    val devObj = o.optJSONObject("deviceDisguise")
                    add(
                        ClonedApp(
                            id = o.getString("id"),
                            originalPackageName = o.getString("originalPackageName"),
                            originalAppName = o.getString("originalAppName"),
                            customLabel = o.getString("customLabel"),
                            userId = o.getInt("userId"),
                            badgeColorHex = o.optLong("badgeColorHex", 0xFF4F8DFDL),
                            iconStyle = runCatching {
                                CloneIconStyle.valueOf(o.optString("iconStyle", CloneIconStyle.BADGE_NUMBER.name))
                            }.getOrDefault(CloneIconStyle.BADGE_NUMBER),
                            virtualLocation = if (locObj != null) {
                                VirtualLocationConfig(
                                    enabled = locObj.optBoolean("enabled", false),
                                    latitude = locObj.optDouble("latitude", 23.2599),
                                    longitude = locObj.optDouble("longitude", 77.4126),
                                    label = locObj.optString("label", "Bhopal, IN")
                                )
                            } else VirtualLocationConfig(),
                            deviceDisguise = if (devObj != null) {
                                DeviceDisguiseConfig(
                                    enabled = devObj.optBoolean("enabled", false),
                                    brand = devObj.optString("brand", "Google"),
                                    manufacturer = devObj.optString("manufacturer", "Google"),
                                    model = devObj.optString("model", "Pixel 9 Pro"),
                                    device = devObj.optString("device", "caiman"),
                                    product = devObj.optString("product", "caiman"),
                                    androidId = devObj.optString("androidId", DeviceDisguiseConfig.randomHex(16)),
                                    wifiMac = devObj.optString("wifiMac", DeviceDisguiseConfig.randomMac())
                                )
                            } else DeviceDisguiseConfig(),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }
}
