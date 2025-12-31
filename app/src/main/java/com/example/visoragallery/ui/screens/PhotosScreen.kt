package com.example.visoragallery.ui.screens
import android.Manifest
import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.example.visoragallery.ui.components.PhotoGrid
import com.example.visoragallery.ui.screens.photos.PhotosUiState
import com.example.visoragallery.ui.screens.photos.PhotosViewModel
//import com.example.visoragallery.utils.CameraHelper
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

    var showColumnDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Camera permission state
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Camera helper
//    val cameraHelper = remember { CameraHelper(context) }
//
//    // Camera launcher
//    val cameraLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            // Photo captured successfully
//            cameraHelper.notifyMediaScanner()
//
//            // Refresh photos to show new photo
//            viewModel.refreshPhotos()
//
//            // Show success message
//            // You can add a Snackbar here if you want
//        }
//    }

    Scaffold(
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
                        IconButton(onClick = { /* TODO: Share */ }) {
                            Icon(Icons.Filled.Share, contentDescription = "Share")
                        }
                        IconButton(onClick = { /* TODO: Delete */ }) {
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
                                        // TODO: Navigate to settings
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
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.refreshPhotos()
                isRefreshing = false
            },
            modifier = Modifier.padding(paddingValues)
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
                        PhotoGrid(
                            photos = state.photos,
                            spanCount = spanCount,
                            selectedPhotos = selectedPhotos,
                            isSelectionMode = isSelectionMode,
                            onPhotoClick = { photo, index ->
                                if (isSelectionMode) {
                                    viewModel.togglePhotoSelection(photo.path)
                                } else {
                                    // Navigate to single photo view
                                    try {
                                        val photoPaths = state.photos.map { it.path }.toTypedArray()
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "photoPaths",
                                            photoPaths
                                        )
                                        navController.navigate("single_photo/$index")
                                    } catch (e: Exception) {
                                        // Log error or show toast
                                        android.util.Log.e("PhotosScreen", "Navigation error", e)
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