package com.samcloner.app.engine

import android.content.Context
import android.net.Uri
import android.util.Log
import com.samcloner.app.model.ClonedApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Default prototype engine so Sam Cloner builds and runs immediately.
 * Replace the body of each method with calls to BlackBox (BlackBoxCore)
 * or SpaceCore when integrating the native virtualization AAR.
 */
class StubVirtualEngine(private val context: Context) : VirtualEngine {

    companion object {
        private const val TAG = "SamClonerEngine"
    }

    override fun initialize() {
        Log.i(TAG, "Sam Cloner VirtualEngine initialized (Stub mode)")
        // TODO: BlackBoxCore.get().doCreate()
    }

    override suspend fun installClone(packageName: String, userId: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            delay(350)
            Log.i(TAG, "installClone package=$packageName userId=$userId")
            // TODO: BlackBoxCore.get().installPackageAsUser(packageName, userId)
            Result.success(Unit)
        }

    override suspend fun installApkFile(apkUri: Uri, userId: Int): Result<String> =
        withContext(Dispatchers.IO) {
            delay(400)
            Log.i(TAG, "installApkFile uri=$apkUri userId=$userId")
            // TODO: Copy APK to cacheDir and call BlackBoxCore.get().installPackageAsUser(file, userId)
            Result.success("com.external.apk")
        }

    override suspend fun launchClone(clone: ClonedApp): Result<String> =
        withContext(Dispatchers.Main) {
            applyLocationConfig(clone)
            applyDeviceConfig(clone)
            Log.i(
                TAG,
                "Launching ${clone.customLabel} (${clone.originalPackageName}) in space #${clone.userId}"
            )
            // TODO: BlackBoxCore.get().launchApk(clone.originalPackageName, clone.userId)
            Result.success(
                "Launched ${clone.customLabel} (Space #${clone.userId + 1})" +
                    if (clone.virtualLocation.enabled) " • GPS: ${clone.virtualLocation.label}" else "" +
                    if (clone.deviceDisguise.enabled) " • Device: ${clone.deviceDisguise.model}" else ""
            )
        }

    override suspend fun uninstallClone(clone: ClonedApp): Result<Unit> =
        withContext(Dispatchers.IO) {
            Log.i(TAG, "uninstallClone package=${clone.originalPackageName} userId=${clone.userId}")
            // TODO: BlackBoxCore.get().uninstallPackageAsUser(clone.originalPackageName, clone.userId)
            Result.success(Unit)
        }

    override suspend fun applyLocationConfig(clone: ClonedApp) {
        if (clone.virtualLocation.enabled) {
            Log.i(
                TAG,
                "Applying virtual GPS for ${clone.originalPackageName}#${clone.userId}: " +
                    "${clone.virtualLocation.latitude}, ${clone.virtualLocation.longitude}"
            )
            // TODO: BLocationManager.get().setLocation(clone.userId, clone.originalPackageName, BLocation(...))
        }
    }

    override suspend fun applyDeviceConfig(clone: ClonedApp) {
        if (clone.deviceDisguise.enabled) {
            Log.i(
                TAG,
                "Applying device disguise for ${clone.originalPackageName}#${clone.userId}: " +
                    "${clone.deviceDisguise.brand} ${clone.deviceDisguise.model}"
            )
            // TODO: Hook Build.* and Settings.Secure.ANDROID_ID inside the virtual process
        }
    }
}
