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

                        IconButton(onClick = {}) {
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
                        // Update snackbar message
                    }
                }
            },
            onCreateNewAlbum = {
                viewModel.hideAddToAlbumDialog()
                viewModel.showCreateAlbumDialog()
            }
        )
    }

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


    LaunchedEffect(showDeleteSuccessSnackbar) {
        if (showDeleteSuccessSnackbar) {
            snackbarHostState.showSnackbar("Moved to trash")
            showDeleteSuccessSnackbar = false
        }
    }
}
