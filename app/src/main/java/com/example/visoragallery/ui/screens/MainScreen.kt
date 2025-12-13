package com.example.visoragallery.ui.screens
import android.os.Build
import android.os.Environment
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.example.visoragallery.data.NavigationItem
import edu.team08.visoragallery.ui.screens.PhotosScreen

data class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    permissionsState: MultiplePermissionsState,
    onRequestManageStorage: () -> Unit
) {
    val navController = rememberNavController()

    // Check permissions
    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    // Check manage storage permission for Android 11+
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                onRequestManageStorage()
            }
        }
    }

    val bottomNavItems = listOf(
        BottomNavItem(
            route = NavigationItem.Photos.route,
            title = NavigationItem.Photos.title,
            selectedIcon = Icons.Filled.Image,
            unselectedIcon = Icons.Outlined.Image
        ),
        BottomNavItem(
            route = NavigationItem.Albums.route,
            title = NavigationItem.Albums.title,
            selectedIcon = Icons.Filled.Folder,
            unselectedIcon = Icons.Outlined.Folder
        ),
        BottomNavItem(
            route = NavigationItem.Search.route,
            title = NavigationItem.Search.title,
            selectedIcon = Icons.Filled.Search,
            unselectedIcon = Icons.Outlined.Search
        ),
        BottomNavItem(
            route = NavigationItem.More.route,
            title = NavigationItem.More.title,
            selectedIcon = Icons.Filled.MoreVert,
            unselectedIcon = Icons.Filled.MoreVert
        )
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route == item.route
                    } == true

                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavigationItem.Photos.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            composable(NavigationItem.Photos.route) {
                if (permissionsState.allPermissionsGranted) {
                    PhotosScreen(navController = navController)
                } else {
                    PermissionDeniedScreen(
                        onRequestPermissions = {
                            permissionsState.launchMultiplePermissionRequest()
                        }
                    )
                }
            }

            composable(NavigationItem.Albums.route) {
                if (permissionsState.allPermissionsGranted) {
                    AlbumsScreen(navController = navController)
                } else {
                    PermissionDeniedScreen(
                        onRequestPermissions = {
                            permissionsState.launchMultiplePermissionRequest()
                        }
                    )
                }
            }

            composable(NavigationItem.Search.route) {
                if (permissionsState.allPermissionsGranted) {
                    SearchScreen()
                } else {
                    PermissionDeniedScreen(
                        onRequestPermissions = {
                            permissionsState.launchMultiplePermissionRequest()
                        }
                    )
                }
            }

            composable(NavigationItem.More.route) {
                MoreScreen(navController = navController)
            }
        }
    }
}

@Composable
fun PermissionDeniedScreen(onRequestPermissions: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(androidx.compose.foundation.layout.PaddingValues(16.dp)),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Image,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text = "Storage Permission Required",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Visora Gallery needs access to your photos to display them.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.height(24.dp)
            )

            Button(
                onClick = onRequestPermissions,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Grant Permission")
            }
        }
    }
}
