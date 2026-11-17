package com.samcloner.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.samcloner.app.ui.screens.AppPickerScreen
import com.samcloner.app.ui.screens.CloneSettingsScreen
import com.samcloner.app.ui.screens.DeviceDisguiseScreen
import com.samcloner.app.ui.screens.HomeScreen
import com.samcloner.app.ui.screens.VirtualLocationScreen
import com.samcloner.app.ui.theme.SamClonerTheme
import com.samcloner.app.ui.viewmodel.SamClonerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val launchCloneId = intent?.getStringExtra("launch_clone_id")

        setContent {
            SamClonerTheme {
                val vm: SamClonerViewModel = viewModel()
                val clones by vm.clones.collectAsState()
                val installedApps by vm.installedApps.collectAsState()
                val isLoadingApps by vm.isLoadingApps.collectAsState()
                val statusMessage by vm.statusMessage.collectAsState()

                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(launchCloneId) {
                    if (launchCloneId != null) {
                        vm.getClone(launchCloneId)?.let { vm.launchClone(it) }
                    }
                }

                LaunchedEffect(statusMessage) {
                    statusMessage?.let {
                        snackbarHostState.showSnackbar(it)
                        vm.clearStatus()
                    }
                }

                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(navController = navController, startDestination = "home") {
                            composable("home") {
                                HomeScreen(
                                    clones = clones,
                                    onAddClick = {
                                        vm.loadInstalledApps()
                                        navController.navigate("picker")
                                    },
                                    onLaunchClick = { vm.launchClone(it) },
                                    onSettingsClick = { navController.navigate("settings/${it.id}") },
                                    onDeleteClick = { vm.deleteClone(it) }
                                )
                            }

                            composable("picker") {
                                AppPickerScreen(
                                    apps = installedApps,
                                    isLoading = isLoadingApps,
                                    onBack = { navController.popBackStack() },
                                    onSelectApp = { appInfo ->
                                        vm.cloneApp(appInfo) {
                                            navController.popBackStack()
                                        }
                                    }
                                )
                            }

                            composable(
                                route = "settings/{cloneId}",
                                arguments = listOf(navArgument("cloneId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val cloneId = backStackEntry.arguments?.getString("cloneId") ?: return@composable
                                val clone = clones.firstOrNull { it.id == cloneId } ?: return@composable
                                CloneSettingsScreen(
                                    clone = clone,
                                    onBack = { navController.popBackStack() },
                                    onSave = { vm.updateClone(it) },
                                    onOpenVirtualLocation = { navController.navigate("location/$cloneId") },
                                    onOpenDeviceDisguise = { navController.navigate("disguise/$cloneId") }
                                )
                            }

                            composable(
                                route = "location/{cloneId}",
                                arguments = listOf(navArgument("cloneId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val cloneId = backStackEntry.arguments?.getString("cloneId") ?: return@composable
                                val clone = clones.firstOrNull { it.id == cloneId } ?: return@composable
                                VirtualLocationScreen(
                                    clone = clone,
                                    onBack = { navController.popBackStack() },
                                    onSave = { locConfig ->
                                        vm.updateClone(clone.copy(virtualLocation = locConfig))
                                    }
                                )
                            }

                            composable(
                                route = "disguise/{cloneId}",
                                arguments = listOf(navArgument("cloneId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val cloneId = backStackEntry.arguments?.getString("cloneId") ?: return@composable
                                val clone = clones.firstOrNull { it.id == cloneId } ?: return@composable
                                DeviceDisguiseScreen(
                                    clone = clone,
                                    onBack = { navController.popBackStack() },
                                    onSave = { devConfig ->
                                        vm.updateClone(clone.copy(deviceDisguise = devConfig))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
