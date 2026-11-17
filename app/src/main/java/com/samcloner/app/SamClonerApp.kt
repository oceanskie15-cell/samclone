package com.samcloner.app

import android.app.Application
import com.samcloner.app.data.CloneRepository
import com.samcloner.app.engine.BlackBoxEngine
import com.samcloner.app.engine.VirtualEngine
import android.content.Context
import org.osmdroid.config.Configuration
import top.niunaijun.blackbox.BlackBoxCore
import top.niunaijun.blackbox.app.configuration.ClientConfiguration
import java.io.File

class SamClonerApp : Application() {

    lateinit var virtualEngine: VirtualEngine
        private set

    lateinit var cloneRepository: CloneRepository
        private set

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        BlackBoxCore.get().doAttachBaseContext(base, object : ClientConfiguration() {
            override fun getHostPackageName(): String = base.packageName
        })
    }

    override fun onCreate() {
        super.onCreate()
        BlackBoxCore.get().doCreate()

        // Initialize osmdroid configuration (OpenStreetMap requires a valid User-Agent)
        Configuration.getInstance().apply {
            userAgentValue = packageName
            osmdroidBasePath = File(cacheDir, "osmdroid")
            osmdroidTileCache = File(cacheDir, "osmdroid/tiles")
        }

        virtualEngine = BlackBoxEngine(this).apply {
            initialize()
        }
        cloneRepository = CloneRepository(this, virtualEngine)
    }
}
