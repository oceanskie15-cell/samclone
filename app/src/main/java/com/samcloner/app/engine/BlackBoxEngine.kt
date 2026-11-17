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
                // launchApk returns Boolean in the original API; `as? Boolean ?: true` also tolerates Unit.
                val result: Any? = BlackBoxCore.get().launchApk(clone.originalPackageName, clone.userId)
                val ok = result as? Boolean ?: true
                if (!ok) error("Engine could not start ${clone.customLabel}")
                "Opened ${clone.customLabel} in space #${clone.userId}"
            }.onFailure { Log.e(TAG, "launchClone failed", it) }
        }

    override suspend fun uninstallClone(clone: ClonedApp): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                BlackBoxCore.get().uninstallPackageAsUser(clone.originalPackageName, clone.userId)
                Unit
            }.onFailure { Log.e(TAG, "uninstallClone failed", it) }
        }

    // Not wired yet: get plain cloning + Run working first, then hook these up.
    override suspend fun applyLocationConfig(clone: ClonedApp) {
        if (clone.virtualLocation.enabled) Log.i(TAG, "Virtual GPS requested (not wired yet) for ${clone.originalPackageName}")
    }

    override suspend fun applyDeviceConfig(clone: ClonedApp) {
        if (clone.deviceDisguise.enabled) Log.i(TAG, "Device disguise requested (not wired yet) for ${clone.originalPackageName}")
    }
}
