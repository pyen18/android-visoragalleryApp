package com.example.visoragallery.ui.screens
import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.visoragallery.ui.components.AddToAlbumDialog
import com.example.visoragallery.ui.components.CreateAlbumDialog
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.example.visoragallery.ui.components.PhotoGrid
import com.example.visoragallery.ui.screens.photos.PhotosUiState
import com.example.visoragallery.ui.screens.photos.PhotosViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun PhotosScreen(
    navController: NavController,
    viewModel: PhotosViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val selectedPhotos by viewModel.selectedPhotos.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val spanCount by viewModel.spanCount.collectAsState()
    val deleteInProgress by viewModel.deleteInProgress.collectAsState()

    var showColumnDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var showDeleteResultSnackbar by remember { mutableStateOf(false) }
    var deleteResultMessage by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    // Add to Album Dialog
    val showAddToAlbumDialog by viewModel.showAddToAlbumDialog.collectAsState()
    val availableAlbums by viewModel.availableAlbums.collectAsState()
    val showCreateAlbumDialog by viewModel.showCreateAlbumDialog.collectAsState()

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
                        IconButton(onClick = { viewModel.selectAllPhotos() }) {
                            Icon(Icons.Filled.SelectAll, contentDescription = "Select all")
                        }
                        IconButton(
                            onClick = { viewModel.showAddToAlbumDialog() },
                            enabled = selectedPhotos.isNotEmpty()
                        ) {
                            Icon(Icons.Filled.LibraryAdd, contentDescription = "Add to album")
                        }
                        IconButton(
                            onClick = { /* TODO: Share */ },
                            enabled = selectedPhotos.isNotEmpty()
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = "Share")
                        }
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            enabled = selectedPhotos.isNotEmpty()
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
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
                    title = {
                        Text(
                            getCurrentDateFormatted(),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: Camera */ }) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = "Camera")
                        }

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
                                    text = { Text("Columns") },
                                    onClick = {
                                        showColumnDialog = true
                                        showMoreMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.GridView, null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Settings") },
                                    onClick = {
                                        navController.navigate("settings")
                                        showMoreMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.Settings, null)
                                    }
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
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    viewModel.refreshPhotos()
                    isRefreshing = false
                }
            ) {
                when (val state = uiState) {
                    is PhotosUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is PhotosUiState.Success -> {
                        if (state.photos.isEmpty()) {
                            EmptyPhotosState()
                        } else {
                            // Trong PhotosScreen.kt
// Thay thế phần PhotoGrid như sau:

                            PhotoGrid(
                                photos = state.photos,
                                spanCount = spanCount,
                                selectedPhotos = selectedPhotos,
                                isSelectionMode = isSelectionMode,
                                onPhotoClick = { photo, index ->
                                    if (isSelectionMode) {
                                        viewModel.togglePhotoSelection(photo.path)
                                    } else {
                                        // Debug logs
                                        android.util.Log.d("PhotosScreen", "Photo clicked at index: $index")
                                        android.util.Log.d("PhotosScreen", "Photo path: ${photo.path}")
                                        android.util.Log.d("PhotosScreen", "Total photos: ${state.photos.size}")

                                        // Lưu vào PhotoDataHolder thay vì savedStateHandle
                                        val photoPaths = state.photos.map { it.path }.toTypedArray()
                                        android.util.Log.d("PhotosScreen", "PhotoPaths array size: ${photoPaths.size}")

                                        com.example.visoragallery.data.PhotoDataHolder.setPhotoPaths(photoPaths)

                                        // Navigate
                                        val route = "single_photo/$index"
                                        android.util.Log.d("PhotosScreen", "Navigating to: $route")

                                        try {
                                            navController.navigate(route)
                                            android.util.Log.d("PhotosScreen", "Navigation called successfully")
                                        } catch (e: Exception) {
                                            android.util.Log.e("PhotosScreen", "Navigation failed", e)
                                        }
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

                    is PhotosUiState.Error -> {
                        ErrorState(
                            message = state.message,
                            onRetry = { viewModel.loadPhotos() }
                        )
                    }
                }
            }

            // Delete Progress Overlay
            if (deleteInProgress) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
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
                            Text("Moving to trash...")
                        }
                    }
                }
            }
        }
    }

    // Column selection dialog
    if (showColumnDialog) {
        AlertDialog(
            onDismissRequest = { showColumnDialog = false },
            title = { Text("Grid Columns") },
            text = {
                Column {
                    listOf(1, 2, 3, 4, 5, 6).forEach { count ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("$count columns")
                            RadioButton(
                                selected = spanCount == count,
                                onClick = {
                                    viewModel.setSpanCount(count)
                                    showColumnDialog = false
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showColumnDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text("Move to Trash?")
            },
            text = {
                Text(
                    "Move ${selectedPhotos.size} photo${if (selectedPhotos.size > 1) "s" else ""} to trash?\n\n" +
                            "You can restore them within 30 days."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteSelectedPhotos { success, count ->
                            deleteResultMessage = if (success) {
                                "Moved $count photo${if (count > 1) "s" else ""} to trash"
                            } else {
                                "Failed to move photos to trash"
                            }
                            showDeleteResultSnackbar = true
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Move to Trash")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    if (showAddToAlbumDialog) {
        AddToAlbumDialog(
            albums = availableAlbums,
            selectedPhotosCount = selectedPhotos.size,
            onDismiss = { viewModel.hideAddToAlbumDialog() },
            onAlbumSelected = { album ->
                viewModel.hideAddToAlbumDialog()
                viewModel.addSelectedPhotosToAlbum(album.id) { success, count ->
                    deleteResultMessage = if (success) {
                        "Added $count photo${if (count > 1) "s" else ""} to ${album.name}"
                    } else {
                        "Failed to add photos to album"
                    }
                    showDeleteResultSnackbar = true
                }
            },
            onCreateNewAlbum = {
                viewModel.hideAddToAlbumDialog()
                viewModel.showCreateAlbumDialog()
            }
        )
    }

    if (showCreateAlbumDialog) {
        CreateAlbumDialog(
            onDismiss = { viewModel.hideCreateAlbumDialog() },
            onConfirm = { albumName ->
                viewModel.hideCreateAlbumDialog()
                viewModel.createAlbumAndAddPhotos(albumName) { success ->
                    deleteResultMessage = if (success) {
                        "Created album '$albumName' and added photos"
                    } else {
                        "Failed to create album"
                    }
                    showDeleteResultSnackbar = true
                }
            }
        )
    }

    // Show result snackbar
    LaunchedEffect(showDeleteResultSnackbar) {
        if (showDeleteResultSnackbar) {
            snackbarHostState.showSnackbar(
                message = deleteResultMessage,
                duration = SnackbarDuration.Short
            )
            showDeleteResultSnackbar = false
        }
    }
}

@Composable
fun EmptyPhotosState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Image,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No photos found",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Take some photos to see them here",
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
                text = "Error loading photos",
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

fun getCurrentDateFormatted(): String {
    val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date())
}