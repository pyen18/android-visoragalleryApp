package com.example.visoragallery.ui.screens

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.visoragallery.data.PhotoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat     // <-- FIX
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotosScreen(navController: NavController) {
    val context = LocalContext.current
    var photos by remember { mutableStateOf<List<PhotoItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Load photos when screen appears
    LaunchedEffect(Unit) {
        isLoading = true
        photos = withContext(Dispatchers.IO) {
            getPhotos(context)
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = getCurrentDateString(),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Filled.CameraAlt, "Camera")
                    }
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Filled.MoreVert, "More")
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Filled.Settings, "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            photos.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(android.R.drawable.ic_menu_gallery),
                            contentDescription = null,
                            modifier = Modifier.size(100.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("No photos found")
                    }
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(120.dp),
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(2.dp)
                ) {
                    items(photos) { photo ->
                        PhotoGridItem(
                            photo = photo,
                            onClick = { /* Ảnh click */ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoGridItem(
    photo: PhotoItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clickable { onClick() }
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = photo.uri,
                error = painterResource(android.R.drawable.ic_menu_gallery)
            ),
            contentScale = ContentScale.Crop,
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun getCurrentDateString(): String {
    val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    return dateFormat.format(Date())
}

/**
 * Lấy ảnh đúng chuẩn Android 10+ (không dùng MediaStore.DATA nữa)
 */
private fun getPhotos(context: Context): List<PhotoItem> {
    val photos = mutableListOf<PhotoItem>()

    val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DATE_ADDED
    )

    val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

    try {
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->

            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                )

                photos.add(PhotoItem(file = null, uri = uri))
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return photos
}
