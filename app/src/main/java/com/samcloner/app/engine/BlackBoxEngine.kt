package com.samcloner.app.engine

import android.content.Context
import android.net.Uri
import android.util.Log
import com.samcloner.app.model.ClonedApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.niunaijun.blackbox.BlackBoxCore
import java.io.File

/**
 * Real virtual engine backed by NewBlackbox (ALEX5402/NewBlackbox).
 *
 * NOTE: BlackBoxCore calls below follow the original BlackBox API and NewBlackbox's Docs.md.
 * If the first compile complains about a name/signature, adjust that one line to match the AAR.
 */
class BlackBoxEngine(private val context: Context) : VirtualEngine {

    companion object {
        private const val TAG = "SamClonerEngine"

        // Google services the clone needs; copied from the phone into the clone space when present.
        private val GMS_PACKAGES = listOf(
            "com.google.android.gsf",
            "com.google.android.gms",
            "com.google.android.gsf.login",
            "com.android.vending"
        )
    }

    private fun ensureGmsInSpace(userId: Int) {
        val prefs = context.getSharedPreferences("sam_cloner_engine", Context.MODE_PRIVATE)
        val key = "gms_installed_$userId"
        if (prefs.getBoolean(key, false)) return
        GMS_PACKAGES.forEach { pkg ->
            val onPhone = runCatching { context.packageManager.getPackageInfo(pkg, 0) }.isSuccess
            if (onPhone) {
                runCatching { BlackBoxCore.get().installPackageAsUser(pkg, userId) }
                    .onFailure { Log.w(TAG, "Google services: could not add $pkg to space #$userId", it) }
            }
        }
        prefs.edit().putBoolean(key, true).apply()
    }

    override fun initialize() {
        Log.i(TAG, "VirtualEngine initialized (NewBlackbox)")
        // BlackBoxCore.doAttachBaseContext / doCreate are called from SamClonerApp.
    }

    override suspend fun installClone(packageName: String, userId: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                BlackBoxCore.get().installPackageAsUser(packageName, userId)
                Unit
            }.onFailure { Log.e(TAG, "installClone failed for $packageName#$userId", it) }
        }

    override suspend fun installApkFile(apkUri: Uri, userId: Int): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val tmp = File(context.cacheDir, "import_${System.currentTimeMillis()}.apk")
                context.contentResolver.openInputStream(apkUri)!!.use { input ->
                    tmp.outputStream().use { input.copyTo(it) }
                }
                BlackBoxCore.get().installPackageAsUser(tmp, userId)
                val pkg = context.packageManager
                    .getPackageArchiveInfo(tmp.absolutePath, 0)?.packageName ?: "unknown"
                tmp.delete()
                pkg
            }.onFailure { Log.e(TAG, "installApkFile failed", it) }
        }

    override suspend fun launchClone(clone: ClonedApp): Result<String> =
        withContext(Dispatchers.Main) {
            applyLocationConfig(clone)
            applyDeviceConfig(clone)
            runCatching {
                withContext(Dispatchers.IO) { ensureGmsInSpace(clone.userId) }
                // launchApk returns Boolean in the original API; `as? Boolean ?: true` also tolerates Unit.
                val result: Any? = BlackBoxCore.get().launchApk(clone.originalPackageName, clone.userId)
                val ok = result as? Boolean ?: true
                if (!ok) error("Engine could not start ${clone.customLabel}")
                val gps = when {
                    !clone.virtualLocation.enabled -> ""
                    lastLocationError == null -> " with virtual GPS (${clone.virtualLocation.label})"
                    else -> " (virtual GPS failed: $lastLocationError)"
                }
                "Opened ${clone.customLabel} in space #${clone.userId}$gps"
            }.onFailure { Log.e(TAG, "launchClone failed", it) }
        }

    override suspend fun uninstallClone(clone: ClonedApp): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                BlackBoxCore.get().uninstallPackageAsUser(clone.originalPackageName, clone.userId)
                Unit
            }.onFailure { Log.e(TAG, "uninstallClone failed", it) }
        }

    /**
     * Virtual GPS through NewBlackbox's BLocationManager. Called by reflection so a wrong
     * name in the engine only produces a log line / status note instead of breaking the build.
     * Returns null on success, or a short reason when it failed.
     */
    private fun setVirtualLocation(userId: Int, pkg: String, enabled: Boolean, lat: Double, lng: Double): String? =
        runCatching {
            val mgrClass = Class.forName("top.niunaijun.blackbox.fake.frameworks.BLocationManager")
            val mgr = mgrClass.getMethod("get").invoke(null)
            val int = Int::class.javaPrimitiveType
            val setPattern = mgrClass.getMethod("setPattern", int, String::class.java, int)
            fun mode(name: String, fallback: Int) =
                runCatching { mgrClass.getField(name).getInt(null) }.getOrDefault(fallback)

            if (enabled) {
                val locClass = Class.forName("top.niunaijun.blackbox.entity.location.BLocation")
                val dbl = Double::class.javaPrimitiveType
                val loc = locClass.getConstructor(dbl, dbl).newInstance(lat, lng)
                setPattern.invoke(mgr, userId, pkg, mode("OWN_MODE", 2))
                mgrClass.getMethod("setLocation", int, String::class.java, locClass)
                    .invoke(mgr, userId, pkg, loc)
                Log.i(TAG, "Virtual GPS set for $pkg#$userId: $lat, $lng")
            } else {
                setPattern.invoke(mgr, userId, pkg, mode("CLOSE_MODE", 0))
            }
            null
        }.getOrElse {
            val cause = it.cause ?: it
            Log.e(TAG, "Virtual GPS failed for $pkg#$userId", cause)
            "${cause.javaClass.simpleName}: ${cause.message}"
        }

    @Volatile private var lastLocationError: String? = null

    override suspend fun applyLocationConfig(clone: ClonedApp) {
        val cfg = clone.virtualLocation
        lastLocationError = setVirtualLocation(
            clone.userId, clone.originalPackageName, cfg.enabled, cfg.latitude, cfg.longitude
        )
    }

    override suspend fun applyDeviceConfig(clone: ClonedApp) {
        if (clone.deviceDisguise.enabled) Log.i(TAG, "Device disguise requested (not wired yet) for ${clone.originalPackageName}")
    }
}
