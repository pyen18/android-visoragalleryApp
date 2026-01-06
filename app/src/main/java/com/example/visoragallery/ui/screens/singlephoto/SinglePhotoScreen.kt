package com.example.visoragallery.ui.screens.singlephoto

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    // STATE & LOGIC
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

    // Init Data
    LaunchedEffect(Unit) {
        viewModel.initPhotos(photoPaths, initialPosition)
    }

    if (uiState.photos.isEmpty()) return

    val pagerState = rememberPagerState { uiState.photos.size }

    LaunchedEffect(uiState.photos) {
        if (uiState.photos.isNotEmpty()) {
            pagerState.scrollToPage(initialPosition)
            viewModel.setCurrentIndex(initialPosition)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage in uiState.photos.indices) {
            viewModel.setCurrentIndex(pagerState.currentPage)
        }
    }

    LaunchedEffect(uiState.photos.isEmpty()) {
        if (uiState.photos.isEmpty()) navController.navigateUp()
    }

    // =========================
    // UI LAYOUT
    // =========================
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Black // Nền đen cho trải nghiệm xem ảnh tốt nhất
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // 1. PHOTO PAGER
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                pageSpacing = 16.dp // Tạo khoảng cách giữa các ảnh
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

            // 2. OVERLAY GRADIENTS (Tạo hiệu ứng bóng mờ để text dễ đọc)
            AnimatedVisibility(
                visible = uiState.showUI,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300)),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(Modifier.fillMaxSize()) {
                    // Gradient trên
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                                )
                            )
                    )
                    // Gradient dưới
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                )
                            )
                    )
                }
            }

            // 3. TOP BAR
            AnimatedVisibility(
                visible = uiState.showUI,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = viewModel.getFormattedDate(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Color.White
                            )
                            Text(
                                text = viewModel.getFormattedTime(),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            // Đã sửa lại icon tương thích
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        // Favorite Icon
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                imageVector = if (uiState.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (uiState.isFavorite) Color(0xFFFF4D4D) else Color.White
                            )
                        }

                        // More Menu
                        Box {
                            IconButton(onClick = { showMoreMenu = true }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = Color.White)
                            }

                            // Menu tối màu
                            MaterialTheme(
                                colorScheme = MaterialTheme.colorScheme.copy(surface = Color(0xFF2C2C2C))
                            ) {
                                DropdownMenu(
                                    expanded = showMoreMenu,
                                    onDismissRequest = { showMoreMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Photo Info", color = Color.White) },
                                        onClick = { showMoreMenu = false },
                                        leadingIcon = { Icon(Icons.Filled.Info, null, tint = Color.White) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Set as...", color = Color.White) },
                                        onClick = { showMoreMenu = false },
                                        leadingIcon = { Icon(Icons.Filled.Wallpaper, null, tint = Color.White) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Rotate", color = Color.White) },
                                        onClick = { showMoreMenu = false },
                                        leadingIcon = { Icon(Icons.Filled.RotateRight, null, tint = Color.White) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Add to Album", color = Color.White) },
                                        onClick = {
                                            viewModel.showAddToAlbumDialog()
                                            showMoreMenu = false
                                        },
                                        leadingIcon = { Icon(Icons.Filled.LibraryAdd, null, tint = Color.White) }
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent // Quan trọng: Để nhìn xuyên qua ảnh
                    )
                )
            }

            // 4. BOTTOM BAR (Minimal Style)
            AnimatedVisibility(
                visible = uiState.showUI,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                // Custom Row thay vì BottomAppBar mặc định
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp, top = 12.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Share
                    BottomActionItem(
                        icon = Icons.Filled.Share,
                        label = "Share",
                        onClick = {}
                    )

                    // Edit
                    BottomActionItem(
                        icon = Icons.Filled.Edit,
                        label = "Edit",
                        onClick = { showMethodDialog = true }
                    )

                    // Add to Album
                    BottomActionItem(
                        icon = Icons.Filled.LibraryAdd,
                        label = "Album",
                        onClick = { viewModel.showAddToAlbumDialog() }
                    )

                    // Delete
                    BottomActionItem(
                        icon = Icons.Filled.Delete,
                        label = "Delete",
                        onClick = { showDeleteDialog = true },
                        enabled = !uiState.deleteInProgress,
                        isLoading = uiState.deleteInProgress
                    )
                }
            }

            // 5. PAGE INDICATOR (Viên thuốc)
            if (uiState.showUI && uiState.photos.size > 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 80.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1} / ${uiState.photos.size}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
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
                            if (success) navController.navigateUp()
                        }
                    }
                ) { Text("Move to Trash", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
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
                    if (success) showDeleteSuccessSnackbar = true
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
                    if (success) showDeleteSuccessSnackbar = true
                }
            }
        )
    }

    // Method Selection Dialog
    if (showMethodDialog) {
        AlertDialog(
            onDismissRequest = { showMethodDialog = false },
            icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
            title = { Text("Remove Background") },
            text = {
                Column {
                    Text("Choose removal method:")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            showMethodDialog = false
                            viewModel.showBackgroundRemovalDialog()
                            viewModel.removeBackground(useAPI = true) { success, file ->
                                if (success && file != null) { resultFile = file; showResultDialog = true }
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
                                if (success && file != null) { resultFile = file; showResultDialog = true }
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
            dismissButton = { TextButton(onClick = { showMethodDialog = false }) { Text("Cancel") } }
        )
    }

    // Background Removal Progress Dialog
    if (showBackgroundRemovalDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Removing Background...") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(progress = { backgroundRemovalProgress }, modifier = Modifier.fillMaxWidth())
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
            icon = { Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Background Removed!") },
            text = {
                Column {
                    Text("Image saved to:")
                    Text(resultFile!!.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            },
            confirmButton = {
                TextButton(onClick = { showResultDialog = false; navController.navigateUp() }) { Text("View in Gallery") }
            },
            dismissButton = { TextButton(onClick = { showResultDialog = false }) { Text("OK") } }
        )
    }

    // Snackbar
    LaunchedEffect(showDeleteSuccessSnackbar) {
        if (showDeleteSuccessSnackbar) {
            snackbarHostState.showSnackbar("Success")
            showDeleteSuccessSnackbar = false
        }
    }
}

// =========================
// HELPER COMPONENTS
// =========================

@Composable
fun BottomActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(48.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 10.sp
        )
    }
}