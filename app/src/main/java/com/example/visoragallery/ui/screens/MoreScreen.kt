package com.example.visoragallery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.* // Import thêm outlined icon cho đẹp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Library & Tools",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState) // Cho phép cuộn nếu màn hình nhỏ
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp) // Khoảng cách đều giữa các phần tử
        ) {
            // ===========================
            // 1. FEATURE HIGHLIGHT (AI)
            // ===========================
            Text(
                "New Features",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )

            FeatureCard(
                title = "AI Stories",
                subtitle = "Create magical stories from your photos",
                icon = Icons.Default.AutoAwesome,
                gradientColors = listOf(Color(0xFF9C27B0), Color(0xFFE040FB)),
                onClick = { navController.navigate("ai_stories") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ===========================
            // 2. LIBRARY GRID (2 Columns)
            // ===========================
            Text(
                "Collections",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )

            // Row 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ModernAlbumCard(
                    modifier = Modifier.weight(1f),
                    title = "Favorites",
                    subtitle = "0 photos",
                    icon = Icons.Default.Favorite,
                    color = FavoritesPink,
                    onClick = { navController.navigate("favorites") }
                )
                ModernAlbumCard(
                    modifier = Modifier.weight(1f),
                    title = "Privacy",
                    subtitle = "Locked",
                    icon = Icons.Default.Lock,
                    color = PrivateBlue,
                    onClick = { navController.navigate("privacy") }
                )
            }

            // Row 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ModernAlbumCard(
                    modifier = Modifier.weight(1f),
                    title = "Trash Bin",
                    subtitle = "Empty",
                    icon = Icons.Default.Delete,
                    color = TrashGray,
                    onClick = { navController.navigate("trash_bin") }
                )
                ModernAlbumCard(
                    modifier = Modifier.weight(1f),
                    title = "Drive Sync",
                    subtitle = "Backup",
                    icon = Icons.Default.CloudSync,
                    color = Color(0xFFA5D6A7),
                    onClick = { navController.navigate("drive_sync") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // ===========================
            // 3. DEVELOPER TOOLS
            // ===========================
            Text(
                "Developer Tools",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )

            OutlinedButton(
                onClick = {
                    scope.launch {
                        isGenerating = true
                        val success = TestImageGenerator.generateTestImages(context, 20)
                        isGenerating = false
                        if (success) showSuccessDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isGenerating,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generating...")
                } else {
                    Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate 20 Test Images")
                }
            }

            Text(
                text = "For testing only - Creates colorful dummy images in Pictures folder.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }

    // DIALOG (Giữ nguyên logic)
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Success") },
            text = {
                Text("20 test images have been generated successfully! Go to Photos tab to view them.")
            },
            confirmButton = {
                Button(onClick = {
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

// ==========================================
// CUSTOM COMPONENTS (Làm đẹp UI ở đây)
// ==========================================

@Composable
fun FeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp) // Chiều cao cố định cho đẹp
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(gradientColors))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@Composable
fun ModernAlbumCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(140.dp) // Thẻ hình chữ nhật đứng
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)), // Nền nhạt
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Flat style
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start // Canh lề trái cho hiện đại
        ) {
            // Icon trong vòng tròn
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color)
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            // Text
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black.copy(alpha = 0.8f) // Text đậm màu
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black.copy(alpha = 0.5f)
                )
            }
        }
    }
}