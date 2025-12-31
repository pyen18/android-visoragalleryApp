package com.example.visoragallery.ui.screens.trashbin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.visoragallery.ui.components.PhotoGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashBinScreen(
    navController: NavController,
    viewModel: TrashBinViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedPhotos by viewModel.selectedPhotos.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val operationInProgress by viewModel.operationInProgress.collectAsState()

    var showEmptyTrashDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadTrashPhotos()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (isSelectionMode) {
                // Selection Mode TopBar
                TopAppBar(
                    title = {
                        Text("${selectedPhotos.size} selected")
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.exitSelectionMode() }) {
                            Icon(Icons.Filled.Close, contentDescription = "Exit selection")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showRestoreDialog = true },
                            enabled = selectedPhotos.isNotEmpty()
                        ) {
                            Icon(Icons.Filled.RestoreFromTrash, contentDescription = "Restore")
                        }
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            enabled = selectedPhotos.isNotEmpty()
                        ) {
                            Icon(Icons.Filled.DeleteForever, contentDescription = "Delete permanently")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            } else {
                // Normal TopBar
                TopAppBar(
                    title = { Text("Trash Bin") },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { showMoreMenu = true }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "More")
                            }

                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Select") },
                                    onClick = {
                                        viewModel.enterSelectionMode()
                                        showMoreMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.CheckCircle, null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Restore All") },
                                    onClick = {
                                        viewModel.restoreAllPhotos { success, count ->
                                            val message = if (success) {
                                                "Restored $count photo${if (count > 1) "s" else ""}"
                                            } else {
                                                "Failed to restore photos"
                                            }
                                            // Show snackbar
                                        }
                                        showMoreMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.RestoreFromTrash, null)
                                    },
                                    enabled = uiState is TrashBinUiState.Success &&
                                            (uiState as TrashBinUiState.Success).photos.isNotEmpty()
                                )
                                DropdownMenuItem(
                                    text = { Text("Empty Trash") },
                                    onClick = {
                                        showEmptyTrashDialog = true
                                        showMoreMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Filled.DeleteForever,
                                            null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    enabled = uiState is TrashBinUiState.Success &&
                                            (uiState as TrashBinUiState.Success).photos.isNotEmpty()
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (val state = uiState) {
                is TrashBinUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is TrashBinUiState.Success -> {
                    if (state.photos.isEmpty()) {
                        EmptyTrashState()
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Info text
                            Text(
                                text = "Items in trash will be removed after 30 days.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.padding(16.dp)
                            )

                            // Photo grid
                            PhotoGrid(
                                photos = state.photos,
                                spanCount = 4,
                                selectedPhotos = selectedPhotos,
                                isSelectionMode = isSelectionMode,
                                onPhotoClick = { photo, index ->
                                    if (isSelectionMode) {
                                        viewModel.togglePhotoSelection(photo.path)
                                    } else {
                                        // TODO: Navigate to single trash photo view
                                    }
                                },
                                onPhotoLongClick = { photo ->
                                    if (!isSelectionMode) {
                                        viewModel.enterSelectionMode()
                                    }
                                    viewModel.togglePhotoSelection(photo.path)
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                is TrashBinUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadTrashPhotos() }
                    )
                }
            }

            // Operation Progress Overlay
            if (operationInProgress) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Processing...")
                        }
                    }
                }
            }
        }
    }

    // Empty Trash Dialog
    if (showEmptyTrashDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyTrashDialog = false },
            icon = {
                Icon(
                    Icons.Filled.DeleteForever,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text("Empty Trash?")
            },
            text = {
                Text(
                    "This will permanently delete all photos in trash.\n\n" +
                            "This action cannot be undone!"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEmptyTrashDialog = false
                        viewModel.emptyTrash { success ->
                            // Show snackbar
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Empty Trash")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyTrashDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Restore Dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            icon = {
                Icon(Icons.Filled.RestoreFromTrash, contentDescription = null)
            },
            title = {
                Text("Restore Photos?")
            },
            text = {
                Text(
                    "Restore ${selectedPhotos.size} photo${if (selectedPhotos.size > 1) "s" else ""}?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestoreDialog = false
                        viewModel.restoreSelectedPhotos { success, count ->
                            // Show snackbar
                        }
                    }
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Permanently Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Filled.DeleteForever,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text("Delete Permanently?")
            },
            text = {
                Text(
                    "Permanently delete ${selectedPhotos.size} photo${if (selectedPhotos.size > 1) "s" else ""}?\n\n" +
                            "This action cannot be undone!"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteSelectedPhotos { success, count ->
                            // Show snackbar
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EmptyTrashState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.DeleteOutline,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Trash is empty",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Deleted photos will appear here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Error loading trash",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}