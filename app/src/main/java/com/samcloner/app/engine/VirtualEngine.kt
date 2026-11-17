package com.samcloner.app.engine

import android.net.Uri
import com.samcloner.app.model.ClonedApp

interface VirtualEngine {
    fun initialize()
    suspend fun installClone(packageName: String, userId: Int): Result<Unit>
    suspend fun installApkFile(apkUri: Uri, userId: Int): Result<String>
    suspend fun launchClone(clone: ClonedApp): Result<String>
    suspend fun uninstallClone(clone: ClonedApp): Result<Unit>
    suspend fun applyLocationConfig(clone: ClonedApp)
    suspend fun applyDeviceConfig(clone: ClonedApp)
}
