package com.example.visoragallery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.visoragallery.ui.theme.FavoritesPink
import com.example.visoragallery.ui.theme.PrivateBlue
import com.example.visoragallery.ui.theme.TrashGray
import com.example.visoragallery.utils.TestImageGenerator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isGenerating by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("More") },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("settings")
                    }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AlbumCard(
                    modifier = Modifier.weight(1f),
                    title = "Favorites",
                    photoCount = "0 photos",
                    icon = Icons.Filled.Favorite,
                    backgroundColor = FavoritesPink,
                    onClick = {
                        navController.navigate("favorites")
                    }
                )

                AlbumCard(
                    modifier = Modifier.weight(1f),
                    title = "Privacy",
                    photoCount = "0 photos",
                    icon = Icons.Filled.Lock,
                    backgroundColor = PrivateBlue,
                    onClick = { /* TODO: Navigate to privacy */ }
                )

                AlbumCard(
                    modifier = Modifier.weight(1f),
                    title = "Trash Bin",
                    photoCount = "0 photos",
                    icon = Icons.Filled.Delete,
                    backgroundColor = TrashGray,
                    onClick = {
                        navController.navigate("trash_bin")
                    }
                )
                AlbumCard(
                    modifier = Modifier.weight(1f),
                    title = "Drive Sync",
                    photoCount = "Cloud backup",
                    icon = Icons.Filled.CloudSync,
                    backgroundColor = Color(0xFFA5D6A7), // Green
                    onClick = {
                        navController.navigate("drive_sync")
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Generate Test Images Button (FOR TESTING ONLY)
            Button(
                onClick = {
                    scope.launch {
                        isGenerating = true
                        val success = TestImageGenerator.generateTestImages(context, 20)
                        isGenerating = false
                        if (success) {
                            showSuccessDialog = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                enabled = !isGenerating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generating...")
                } else {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate 20 Test Images")
                }
            }

            Text(
                text = "For testing only - Creates 20 colorful test images",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Test Images Generated!") },
            text = {
                Text("20 test images have been created. Go to Photos tab and pull-to-refresh to see them.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    navController.navigate("photos") {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }) {
                    Text("Go to Photos")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun AlbumCard(
    modifier: Modifier = Modifier,
    title: String,
    photoCount: String,
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween  // Changed from Center to SpaceBetween
    ) {
        // Icon at top
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier
                .size(56.dp)
                .weight(1f),
            tint = Color.White
        )

        // Text at bottom
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White  // Changed to white for better contrast
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = photoCount,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)  // Changed to white
            )
        }
    }
}