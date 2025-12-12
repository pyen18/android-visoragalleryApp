package com.example.visoragallery.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.visoragallery.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("More") },
                actions = {
                    IconButton(onClick = { /* TODO: Navigate to settings */ }) {
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
                    onClick = { /* TODO: Navigate to favorites */ }
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
                    onClick = { /* TODO: Navigate to trash */ }
                )
            }
        }
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
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(56.dp),
            tint = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = photoCount,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}