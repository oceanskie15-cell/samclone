package com.samcloner.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.samcloner.app.model.ClonedApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    clones: List<ClonedApp>,
    onAddClick: () -> Unit,
    onLaunchClick: (ClonedApp) -> Unit,
    onSettingsClick: (ClonedApp) -> Unit,
    onDeleteClick: (ClonedApp) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Sam Cloner", fontWeight = FontWeight.Bold)
                        Text(
                            "${clones.size} cloned space(s)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Clone App") }
            )
        }
    ) { padding ->
        if (clones.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "No cloned apps yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Tap \"Clone App\" to create an isolated multi-account space with custom name, virtual GPS, and device disguise.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(clones, key = { it.id }) { clone ->
                    CloneCard(
                        clone = clone,
                        onLaunch = { onLaunchClick(clone) },
                        onSettings = { onSettingsClick(clone) },
                        onDelete = { onDeleteClick(clone) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CloneCard(
    clone: ClonedApp,
    onLaunch: () -> Unit,
    onSettings: () -> Unit,
    onDelete: () -> Unit
) {
    val badgeColor = Color(clone.badgeColorHex)
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onLaunch),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(badgeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${clone.userId + 1}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = clone.customLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = clone.originalPackageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onLaunch) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Launch")
                }
                IconButton(onClick = onSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }

            if (clone.virtualLocation.enabled || clone.deviceDisguise.enabled) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (clone.virtualLocation.enabled) {
                        AssistChip(
                            onClick = onSettings,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text(clone.virtualLocation.label) }
                        )
                    }
                    if (clone.deviceDisguise.enabled) {
                        AssistChip(
                            onClick = onSettings,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text(clone.deviceDisguise.model) }
                        )
                    }
                }
            }
        }
    }
}
