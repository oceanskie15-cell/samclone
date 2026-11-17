package com.samcloner.app.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.samcloner.app.model.ClonedApp
import com.samcloner.app.model.VirtualLocationConfig
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VirtualLocationScreen(
    clone: ClonedApp,
    onBack: () -> Unit,
    onSave: (VirtualLocationConfig) -> Unit
) {
    var enabled by remember(clone.id) { mutableStateOf(clone.virtualLocation.enabled) }
    var lat by remember(clone.id) { mutableDoubleStateOf(clone.virtualLocation.latitude) }
    var lng by remember(clone.id) { mutableDoubleStateOf(clone.virtualLocation.longitude) }
    var label by remember(clone.id) { mutableStateOf(clone.virtualLocation.label) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    val presets = listOf(
        Triple("Bhopal", 23.2599, 77.4126),
        Triple("New Delhi", 28.6139, 77.2090),
        Triple("Mumbai", 19.0760, 72.8777),
        Triple("London", 51.5074, -0.1278),
        Triple("New York", 40.7128, -74.0060)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Virtual GPS (OpenStreetMap)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        onSave(
                            VirtualLocationConfig(
                                enabled = enabled,
                                latitude = lat,
                                longitude = lng,
                                label = label.ifBlank { "Custom Pin" }
                            )
                        )
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
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Enable Virtual Location", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Tap anywhere on the map to move the pin",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = enabled, onCheckedChange = { enabled = it })
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { (city, pLat, pLng) ->
                    AssistChip(
                        onClick = {
                            lat = pLat
                            lng = pLng
                            label = city
                            enabled = true
                            mapViewRef?.let { mv ->
                                val pt = GeoPoint(pLat, pLng)
                                mv.controller.animateTo(pt)
                                mv.overlays.filterIsInstance<Marker>().firstOrNull()?.position = pt
                                mv.invalidate()
                            }
                        },
                        label = { Text(city) }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = "%.5f".format(lat),
                    onValueChange = { it.toDoubleOrNull()?.let { v -> lat = v } },
                    label = { Text("Latitude") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = "%.5f".format(lng),
                    onValueChange = { it.toDoubleOrNull()?.let { v -> lng = v } },
                    label = { Text("Longitude") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(13.0)
                            val startPoint = GeoPoint(lat, lng)
                            controller.setCenter(startPoint)

                            val marker = Marker(this).apply {
                                position = startPoint
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = "Virtual Location"
                            }
                            overlays.add(marker)

                            val tapReceiver = object : MapEventsReceiver {
                                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                    lat = p.latitude
                                    lng = p.longitude
                                    label = "Custom Pin"
                                    enabled = true
                                    marker.position = p
                                    invalidate()
                                    return true
                                }

                                override fun longPressHelper(p: GeoPoint): Boolean = false
                            }
                            overlays.add(0, MapEventsOverlay(tapReceiver))
                            mapViewRef = this
                        }
                    },
                    onRelease = { mv ->
                        mv.onDetach()
                    }
                )
            }
        }
    }
}
