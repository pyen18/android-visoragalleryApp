package com.example.visoragallery.ui.screens.singlephoto

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.visoragallery.ui.components.AddToAlbumDialog
import com.example.visoragallery.ui.components.CreateAlbumDialog
import com.example.visoragallery.ui.components.ZoomableImage
import java.io.File

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun SinglePhotoScreen(
    navController: NavController,
    photoPaths: Array<String>,
    initialPosition: Int,
    viewModel: SinglePhotoViewModel = viewModel()
) {

    // =========================
    // STATE
    // =========================
    val uiState by viewModel.uiState.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showDeleteSuccessSnackbar by remember { mutableStateOf(false) }
    val showAddToAlbumDialog by viewModel.showAddToAlbumDialog.collectAsState()
    val availableAlbums by viewModel.availableAlbums.collectAsState()
    val showCreateAlbumDialogState by viewModel.showCreateAlbumDialog.collectAsState()

    // Background Removal States
    val showBackgroundRemovalDialog by viewModel.showBackgroundRemovalDialog.collectAsState()
    val backgroundRemovalProgress by viewModel.backgroundRemovalProgress.collectAsState()
    var showMethodDialog by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var resultFile by remember { mutableStateOf<File?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // =========================
    // INIT DATA (CHỈ 1 LẦN)
    // =========================
    LaunchedEffect(Unit) {
        viewModel.initPhotos(photoPaths, initialPosition)
    }

    // =========================
    // ❗ CHẶN COMPOSE KHI DATA CHƯA CÓ
    // =========================
    if (uiState.photos.isEmpty()) {
        return
    }

    // =========================
    // PAGER STATE (KHÔNG initialPage)
    // =========================
    val pagerState = rememberPagerState {
        uiState.photos.size
    }

    // =========================
    // SCROLL ĐÚNG INDEX KHI DATA SẴN SÀNG
    // =========================
    LaunchedEffect(uiState.photos) {
        if (uiState.photos.isNotEmpty()) {
            pagerState.scrollToPage(initialPosition)
            viewModel.setCurrentIndex(initialPosition)
        }
    }

    // =========================
    // SYNC PAGE → VIEWMODEL
    // =========================
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage in uiState.photos.indices) {
            viewModel.setCurrentIndex(pagerState.currentPage)
        }
    }

    // =========================
    // NAV BACK IF EMPTY
    // =========================
    LaunchedEffect(uiState.photos.isEmpty()) {
        if (uiState.photos.isEmpty()) {
            navController.navigateUp()
        }
    }

    // =========================
    // UI
    // =========================
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(paddingValues)
        ) {

            // =========================
            // PHOTO PAGER
            // =========================
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val photo = uiState.photos.getOrNull(page)
                val file = photo?.file

                if (photo != null && file != null) {
                    ZoomableImage(
                        file = file,
                        contentDescription = photo.name,
                        onTap = { viewModel.toggleUI() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // =========================
            // TOP BAR
            // =========================
            AnimatedVisibility(
                visible = uiState.showUI,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(300)
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(300)
                ) + fadeOut()
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = viewModel.getFormattedDate(),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = viewModel.getFormattedTime(),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {

                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                imageVector = if (uiState.isFavorite)
                                    Icons.Filled.Favorite
                                else
                                    Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (uiState.isFavorite)
                                    Color(0xFFF85D58)
                                else
                                    Color.White
                            )
                        }

                        Box {
                            IconButton(onClick = { showMoreMenu = true }) {
                                Icon(
                                    Icons.Filled.MoreVert,
                                    contentDescription = "More",
                                    tint = Color.White
                                )
                            }

                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Photo Info") },
                                    onClick = { showMoreMenu = false },
                                    leadingIcon = {
                                        Icon(Icons.Filled.Info, null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Set as...") },
                                    onClick = { showMoreMenu = false },
                                    leadingIcon = {
                                        Icon(Icons.Filled.Wallpaper, null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Rotate") },
                                    onClick = { showMoreMenu = false },
                                    leadingIcon = {
                                        Icon(Icons.Filled.RotateRight, null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Add to Album") },
                                    onClick = {
                                        viewModel.showAddToAlbumDialog()
                                        showMoreMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.LibraryAdd, null)
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Photo Info") },
                                    onClick = {
                                        // TODO: Show info dialog
                                        showMoreMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.Info, null)
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.7f),
                        titleContentColor = Color.White
                    )
                )
            }

            // =========================
            // BOTTOM BAR
            // =========================
            AnimatedVisibility(
                visible = uiState.showUI,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                BottomAppBar(
                    containerColor = Color.Black.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {

                        IconButton(onClick = {}) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.Share, null, tint = Color.White)
                                Text("Share", color = Color.White)
                            }
                        }

                        IconButton(onClick = { showMethodDialog = true }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.Edit, null, tint = Color.White)
                                Text("Edit", color = Color.White)
                            }
                        }

                        IconButton(onClick = { viewModel.showAddToAlbumDialog() }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Filled.LibraryAdd,
                                    contentDescription = "Add to Album",
                                    tint = Color.White
                                )
                                Text(
                                    "Add to Album",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }

                        IconButton(
                            onClick = { showDeleteDialog = true },
                            enabled = !uiState.deleteInProgress
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (uiState.deleteInProgress) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                } else {
                                    Icon(Icons.Filled.Delete, null, tint = Color.White)
                                }
                                Text("Delete", color = Color.White)
                            }
                        }
                    }
                }
            }

            // =========================
            // PAGE INDICATOR
            // =========================
            if (uiState.showUI && uiState.photos.size > 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 80.dp)
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1} / ${uiState.photos.size}",
                            color = Color.White,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }

    // =========================
    // DIALOGS
    // =========================

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Move to Trash?") },
            text = { Text("This photo will be moved to trash.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteCurrentPhoto { success ->
                            if (success) {
                                navController.navigateUp()
                            }
                        }
                    }
                ) {
                    Text("Move to Trash")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add to Album Dialog
    if (showAddToAlbumDialog) {
        AddToAlbumDialog(
            albums = availableAlbums,
            selectedPhotosCount = 1,
            onDismiss = { viewModel.hideAddToAlbumDialog() },
            onAlbumSelected = { album ->
                viewModel.hideAddToAlbumDialog()
                viewModel.addCurrentPhotoToAlbum(album.id) { success ->
                    if (success) {
                        showDeleteSuccessSnackbar = true
                    }
                }
            },
            onCreateNewAlbum = {
                viewModel.hideAddToAlbumDialog()
                viewModel.showCreateAlbumDialog()
            }
        )
    }

    // Create Album Dialog
    if (showCreateAlbumDialogState) {
        CreateAlbumDialog(
            onDismiss = { viewModel.hideCreateAlbumDialog() },
            onConfirm = { albumName ->
                viewModel.hideCreateAlbumDialog()
                viewModel.createAlbumAndAddCurrentPhoto(albumName) { success ->
                    if (success) {
                        showDeleteSuccessSnackbar = true
                    }
                }
            }
        )
    }

    // =========================
    // BACKGROUND REMOVAL DIALOGS
    // =========================

    // Method Selection Dialog
    if (showMethodDialog) {
        AlertDialog(
            onDismissRequest = { showMethodDialog = false },
            icon = {
                Icon(Icons.Filled.Edit, contentDescription = null)
            },
            title = {
                Text("Remove Background")
            },
            text = {
                Column {
                    Text("Choose removal method:")
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            showMethodDialog = false
                            viewModel.showBackgroundRemovalDialog()
                            viewModel.removeBackground(useAPI = true) { success, file ->
                                if (success && file != null) {
                                    resultFile = file
                                    showResultDialog = true
                                }
                                viewModel.hideBackgroundRemovalDialog()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Cloud, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Use API (Better Quality)")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            showMethodDialog = false
                            viewModel.showBackgroundRemovalDialog()
                            viewModel.removeBackground(useAPI = false) { success, file ->
                                if (success && file != null) {
                                    resultFile = file
                                    showResultDialog = true
                                }
                                viewModel.hideBackgroundRemovalDialog()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.PhoneAndroid, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Process Locally (Free)")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showMethodDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Background Removal Progress Dialog
    if (showBackgroundRemovalDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text("Removing Background...")
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { backgroundRemovalProgress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${(backgroundRemovalProgress * 100).toInt()}%")
                }
            },
            confirmButton = {}
        )
    }

    // Result Dialog
    if (showResultDialog && resultFile != null) {
        AlertDialog(
            onDismissRequest = { showResultDialog = false },
            icon = {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text("Background Removed!")
            },
            text = {
                Column {
                    Text("Image saved to:")
                    Text(
                        resultFile!!.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showResultDialog = false
                    navController.navigateUp()
                }) {
                    Text("View in Gallery")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResultDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // =========================
    // SNACKBAR
    // =========================
    LaunchedEffect(showDeleteSuccessSnackbar) {
        if (showDeleteSuccessSnackbar) {
            snackbarHostState.showSnackbar("Moved to trash")
            showDeleteSuccessSnackbar = false
        }
    }
}