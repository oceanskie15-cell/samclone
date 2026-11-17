package com.samcloner.app.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.samcloner.app.model.ClonedApp
import com.samcloner.app.model.DeviceDisguiseConfig
import com.samcloner.app.model.DevicePresets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDisguiseScreen(
    clone: ClonedApp,
    onBack: () -> Unit,
    onSave: (DeviceDisguiseConfig) -> Unit
) {
    var config by remember(clone.id) { mutableStateOf(clone.deviceDisguise) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Info Disguise") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        onSave(config)
                        onBack()
                    }) {
                        Text("Apply", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Enable Device Disguise", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Spoof Build properties & hardware IDs for this clone",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = config.enabled,
                    onCheckedChange = { config = config.copy(enabled = it) }
                )
            }

            Text("Quick Presets", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DevicePresets.all.forEach { preset ->
                    AssistChip(
                        onClick = {
                            config = preset.copy(
                                enabled = true,
                                androidId = DeviceDisguiseConfig.randomHex(16),
                                wifiMac = DeviceDisguiseConfig.randomMac()
                            )
                        },
                        label = { Text("${preset.brand} ${preset.model}") }
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    config = config.copy(
                        enabled = true,
                        androidId = DeviceDisguiseConfig.randomHex(16),
                        wifiMac = DeviceDisguiseConfig.randomMac()
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Casino, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Randomize Android ID & Wi-Fi MAC")
            }

            OutlinedTextField(
                value = config.brand,
                onValueChange = { config = config.copy(brand = it) },
                label = { Text("Build.BRAND") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = config.manufacturer,
                onValueChange = { config = config.copy(manufacturer = it) },
                label = { Text("Build.MANUFACTURER") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = config.model,
                onValueChange = { config = config.copy(model = it) },
                label = { Text("Build.MODEL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = config.device,
                onValueChange = { config = config.copy(device = it) },
                label = { Text("Build.DEVICE") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = config.androidId,
                onValueChange = { config = config.copy(androidId = it) },
                label = { Text("Settings.Secure.ANDROID_ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = config.wifiMac,
                onValueChange = { config = config.copy(wifiMac = it) },
                label = { Text("Wi-Fi MAC Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}
