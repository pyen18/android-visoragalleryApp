package com.example.visoragallery.ui.screens.albums
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
import com.example.visoragallery.data.PhotoDataHolder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleAlbumScreen(
    navController: NavController,
    albumId: String,
    viewModel: SingleAlbumViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedPhotos by viewModel.selectedPhotos.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()

    var showMoreMenu by remember { mutableStateOf(false) }

    LaunchedEffect(albumId) {
        viewModel.loadAlbum(albumId)
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text("${selectedPhotos.size} selected") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.exitSelectionMode() }) {
                            Icon(Icons.Filled.Close, "Exit")
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: Delete */ }) {
                            Icon(Icons.Filled.Delete, "Delete")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            } else {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = uiState.title(),
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (uiState is SingleAlbumUiState.Success) {
                                val photos = (uiState as SingleAlbumUiState.Success).photos
                                Text(
                                    text = "${photos.size} photo${if (photos.size != 1) "s" else ""}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Filled.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { showMoreMenu = true }) {
                                Icon(Icons.Filled.MoreVert, "More")
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
                                    leadingIcon = { Icon(Icons.Filled.CheckCircle, null) }
                                )
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is SingleAlbumUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is SingleAlbumUiState.Success -> {
                if (state.photos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No photos in this album",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    PhotoGrid(
                        photos = state.photos,
                        spanCount = 4,
                        selectedPhotos = selectedPhotos,
                        isSelectionMode = isSelectionMode,
                        onPhotoClick = { photo, index ->
                            if (isSelectionMode) {
                                viewModel.togglePhotoSelection(photo.path)
                            } else {
                                val photoPaths = state.photos.map { it.path }.toTypedArray()
                                PhotoDataHolder.setPhotoPaths(photoPaths)
                                navController.navigate("single_photo/$index")
                            }
                        },
                        onPhotoLongClick = { photo ->
                            if (!isSelectionMode) {
                                viewModel.enterSelectionMode()
                            }
                            viewModel.togglePhotoSelection(photo.path)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
            }

            is SingleAlbumUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Error loading album",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadAlbum(albumId) }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}