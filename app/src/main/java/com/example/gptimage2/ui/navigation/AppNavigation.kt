package com.example.gptimage2.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gptimage2.ui.screens.gallery.GalleryScreen
import com.example.gptimage2.ui.screens.gallery.ImageDetailScreen
import com.example.gptimage2.ui.screens.home.HomeScreen
import com.example.gptimage2.ui.screens.imageedit.ImageEditScreen
import com.example.gptimage2.ui.screens.mask.MaskEditorScreen
import com.example.gptimage2.ui.screens.settings.SettingsScreen
import java.net.URLDecoder

sealed class Screen(val route: String, val label: String, val icon: @Composable () -> Unit) {
    data object Home : Screen("home", "文生图", { Icon(Icons.Default.AutoFixHigh, contentDescription = null) })
    data object ImageEdit : Screen("imageEdit", "图生图", { Icon(Icons.Default.Image, contentDescription = null) })
    data object MaskEditor : Screen("maskEditor", "局部重绘", { Icon(Icons.Default.Draw, contentDescription = null) })
    data object Gallery : Screen("gallery", "画廊", { Icon(Icons.Default.Collections, contentDescription = null) })
    data object Settings : Screen("settings", "设置", { Icon(Icons.Default.Settings, contentDescription = null) })
    data object ImageDetail : Screen("imageDetail/{filePath}", "图片详情", { Icon(Icons.Default.Image, contentDescription = null) })
}

val bottomNavScreens = listOf(
    Screen.Home,
    Screen.ImageEdit,
    Screen.MaskEditor,
    Screen.Gallery
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = bottomNavScreens.any { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavScreens.forEach { screen ->
                        NavigationBarItem(
                            icon = screen.icon,
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToDetail = { filePath -> navController.navigate("imageDetail/${java.net.URLEncoder.encode(filePath, "UTF-8")}") }
                )
            }
            composable(Screen.ImageEdit.route) {
                ImageEditScreen(
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToDetail = { filePath -> navController.navigate("imageDetail/${java.net.URLEncoder.encode(filePath, "UTF-8")}") }
                )
            }
            composable(Screen.MaskEditor.route) {
                MaskEditorScreen(
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToDetail = { filePath -> navController.navigate("imageDetail/${java.net.URLEncoder.encode(filePath, "UTF-8")}") }
                )
            }
            composable(Screen.Gallery.route) {
                GalleryScreen(
                    onNavigateToDetail = { filePath -> navController.navigate("imageDetail/${java.net.URLEncoder.encode(filePath, "UTF-8")}") }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Screen.ImageDetail.route,
                arguments = listOf(navArgument("filePath") { type = NavType.StringType })
            ) { backStackEntry ->
                val encodedPath = backStackEntry.arguments?.getString("filePath") ?: ""
                val filePath = URLDecoder.decode(encodedPath, "UTF-8")
                ImageDetailScreen(
                    filePath = filePath,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
