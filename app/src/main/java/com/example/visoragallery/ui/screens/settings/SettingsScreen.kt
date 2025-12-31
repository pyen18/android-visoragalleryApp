package com.example.visoragallery.ui.screens.settings
import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showTimeLapseDialog by remember { mutableStateOf(false) }
    var showColumnsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Display Section
            SettingsSectionHeader("Display")

            SettingsItem(
                icon = Icons.Filled.DarkMode,
                title = "Dark Mode",
                subtitle = if (uiState.nightMode) "Enabled" else "Disabled",
                trailing = {
                    Switch(
                        checked = uiState.nightMode,
                        onCheckedChange = {
                            viewModel.toggleNightMode(it)
                            // Restart activity to apply theme
                            (context as? Activity)?.recreate()
                        }
                    )
                }
            )

            SettingsItem(
                icon = Icons.Filled.GridView,
                title = "Default Grid Columns",
                subtitle = "${uiState.defaultColumns} columns",
                onClick = { showColumnsDialog = true }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Photo Management Section
            SettingsSectionHeader("Photo Management")

            SettingsItem(
                icon = Icons.Filled.Delete,
                title = "Move to Trash",
                subtitle = if (uiState.trashMode)
                    "Photos will be moved to trash before deletion"
                else
                    "Photos will be deleted immediately",
                trailing = {
                    Switch(
                        checked = uiState.trashMode,
                        onCheckedChange = { viewModel.toggleTrashMode(it) }
                    )
                }
            )

            SettingsItem(
                icon = Icons.Filled.Slideshow,
                title = "Slideshow Time Lapse",
                subtitle = "Change photo every ${uiState.timeLapse}",
                onClick = { showTimeLapseDialog = true }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Storage Section
            SettingsSectionHeader("Storage")

            StorageInfoCard(
                storageInfo = uiState.storageInfo,
                onRefresh = { viewModel.refreshStorageInfo() }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // About Section
            SettingsSectionHeader("About")

            SettingsItem(
                icon = Icons.Filled.Info,
                title = "Version",
                subtitle = "1.0.0"
            )

            SettingsItem(
                icon = Icons.Filled.People,
                title = "Developed by",
                subtitle = "Infinity Team"
            )

            SettingsItem(
                icon = Icons.Filled.Code,
                title = "Open Source",
                subtitle = "Built with Jetpack Compose"
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Time Lapse Dialog
    if (showTimeLapseDialog) {
        TimelapseDialog(
            currentValue = uiState.timeLapse,
            onDismiss = { showTimeLapseDialog = false },
            onSelect = {
                viewModel.setTimeLapse(it)
                showTimeLapseDialog = false
            }
        )
    }

    // Columns Dialog
    if (showColumnsDialog) {
        ColumnsDialog(
            currentValue = uiState.defaultColumns,
            onDismiss = { showColumnsDialog = false },
            onSelect = {
                viewModel.setDefaultColumns(it)
                showColumnsDialog = false
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Surface(
        onClick = { onClick?.invoke() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            if (trailing != null) {
                trailing()
            } else if (onClick != null) {
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun StorageInfoCard(
    storageInfo: StorageInfo,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VisoraGallery Storage",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { storageInfo.usedPercentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Used: ${storageInfo.formatSize(storageInfo.usedSpace)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Free: ${storageInfo.formatSize(storageInfo.freeSpace)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "Total: ${storageInfo.formatSize(storageInfo.totalSpace)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun TimelapseDialog(
    currentValue: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val options = listOf("1 seconds", "2 seconds", "3 seconds", "4 seconds", "5 seconds")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Slideshow Time Lapse") },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(option)
                        RadioButton(
                            selected = option == currentValue,
                            onClick = { onSelect(option) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun ColumnsDialog(
    currentValue: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    val options = listOf(1, 2, 3, 4, 5, 6)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Default Grid Columns") },
        text = {
            Column {
                options.forEach { columns ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("$columns columns")
                        RadioButton(
                            selected = columns == currentValue,
                            onClick = { onSelect(columns) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}