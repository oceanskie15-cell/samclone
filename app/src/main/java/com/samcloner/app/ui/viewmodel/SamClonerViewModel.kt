package com.samcloner.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.samcloner.app.SamClonerApp
import com.samcloner.app.model.ClonedApp
import com.samcloner.app.model.InstalledAppInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SamClonerViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as SamClonerApp
    private val repository = app.cloneRepository
    private val engine = app.virtualEngine

    val clones: StateFlow<List<ClonedApp>> = repository.clones

    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps.asStateFlow()

    private val _isLoadingApps = MutableStateFlow(false)
    val isLoadingApps: StateFlow<Boolean> = _isLoadingApps.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun loadInstalledApps() {
        viewModelScope.launch {
            _isLoadingApps.value = true
            _installedApps.value = repository.getInstalledApps()
            _isLoadingApps.value = false
        }
    }

    fun cloneApp(installedApp: InstalledAppInfo, onDone: (ClonedApp) -> Unit = {}) {
        viewModelScope.launch {
            repository.createClone(installedApp)
                .onSuccess { created ->
                    _statusMessage.value = "Cloned ${created.customLabel}"
                    onDone(created)
                }
                .onFailure { err ->
                    _statusMessage.value = "Clone failed: ${err.message}"
                }
        }
    }

    fun launchClone(clone: ClonedApp) {
        viewModelScope.launch {
            engine.launchClone(clone)
                .onSuccess { msg -> _statusMessage.value = msg }
                .onFailure { err -> _statusMessage.value = "Launch failed: ${err.message}" }
        }
    }

    fun updateClone(clone: ClonedApp) {
        repository.updateClone(clone)
        _statusMessage.value = "Saved settings for ${clone.customLabel}"
    }

    fun deleteClone(clone: ClonedApp) {
        viewModelScope.launch {
            repository.deleteClone(clone)
            _statusMessage.value = "Removed ${clone.customLabel}"
        }
    }

    fun getClone(id: String): ClonedApp? = repository.getCloneById(id)

    fun clearStatus() {
        _statusMessage.value = null
    }
}
