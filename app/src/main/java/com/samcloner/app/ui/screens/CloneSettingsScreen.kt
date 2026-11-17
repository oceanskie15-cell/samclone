package com.samcloner.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.samcloner.app.data.ShortcutHelper
import com.samcloner.app.model.CloneIconStyle
import com.samcloner.app.model.ClonedApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloneSettingsScreen(
    clone: ClonedApp,
    onBack: () -> Unit,
    onSave: (ClonedApp) -> Unit,
    onOpenVirtualLocation: () -> Unit,
    onOpenDeviceDisguise: () -> Unit
) {
    val context = LocalContext.current
    var label by remember(clone.id) { mutableStateOf(clone.customLabel) }
    var selectedColor by remember(clone.id) { mutableLongStateOf(clone.badgeColorHex) }
    var selectedStyle by remember(clone.id) { mutableStateOf(clone.iconStyle) }
    var shortcutFeedback by remember { mutableStateOf<String?>(null) }

    val colorPalette = listOf(
        0xFF2563EBL, 0xFF0D9488L, 0xFFDC2626L, 0xFFD97706L, 0xFF7C3AEDL, 0xFFDB2777L
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clone Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        onSave(
                            clone.copy(
                                customLabel = label.ifBlank { clone.originalAppName },
                                badgeColorHex = selectedColor,
                                iconStyle = selectedStyle
                            )
                        )
                        onBack()
                    }) {
                        Text("Save", fontWeight = FontWeight.Bold)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Custom Name & Icon", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Custom App Label") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("Badge Color", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                colorPalette.forEach { hex ->
                    val isSelected = selectedColor == hex
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(hex))
                            .then(
                                if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                else Modifier
                            )
                            .clickable { selectedColor = hex }
                    )
                }
            }

            Text("Icon Style", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CloneIconStyle.entries.forEach { style ->
                    FilterChip(
                        selected = selectedStyle == style,
                        onClick = { selectedStyle = style },
                        label = { Text(style.displayName) }
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    val updated = clone.copy(
                        customLabel = label.ifBlank { clone.originalAppName },
                        badgeColorHex = selectedColor,
                        iconStyle = selectedStyle
                    )
                    onSave(updated)
                    val ok = ShortcutHelper.pinCloneShortcut(context, updated)
                    shortcutFeedback = if (ok) "Shortcut request sent to launcher" else "Launcher does not support pinned shortcuts"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AddToHomeScreen, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add Custom Icon to Home Screen")
            }

            shortcutFeedback?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }

            HorizontalDivider()

            Text("Privacy & Spoofing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenVirtualLocation)
            ) {
                ListItem(
                    leadingContent = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    headlineContent = { Text("Virtual GPS Location") },
                    supportingContent = {
                        Text(
                            if (clone.virtualLocation.enabled)
                                "${clone.virtualLocation.label} (${"%.4f".format(clone.virtualLocation.latitude)}, ${"%.4f".format(clone.virtualLocation.longitude)})"
                            else "Disabled (using real device GPS)"
                        )
                    }
                )
            }

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenDeviceDisguise)
            ) {
                ListItem(
                    leadingContent = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
                    headlineContent = { Text("Device Info Disguise") },
                    supportingContent = {
                        Text(
                            if (clone.deviceDisguise.enabled)
                                "${clone.deviceDisguise.brand} ${clone.deviceDisguise.model} • ID: ${clone.deviceDisguise.androidId}"
                            else "Disabled (using real device identity)"
                        )
                    }
                )
            }
        }
    }
}
