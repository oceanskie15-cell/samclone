package com.samcloner.app

import android.app.Application
import com.samcloner.app.data.CloneRepository
import com.samcloner.app.engine.StubVirtualEngine
import com.samcloner.app.engine.VirtualEngine
import org.osmdroid.config.Configuration
import java.io.File

class SamClonerApp : Application() {

    lateinit var virtualEngine: VirtualEngine
        private set

    lateinit var cloneRepository: CloneRepository
        private set

    override fun onCreate() {
        super.onCreate()

        // Initialize osmdroid configuration (OpenStreetMap requires a valid User-Agent)
        Configuration.getInstance().apply {
            userAgentValue = packageName
            osmdroidBasePath = File(cacheDir, "osmdroid")
            osmdroidTileCache = File(cacheDir, "osmdroid/tiles")
        }

        virtualEngine = StubVirtualEngine(this).apply {
            initialize()
        }
        cloneRepository = CloneRepository(this, virtualEngine)
    }
}
