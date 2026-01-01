// app/src/main/java/com/example/visoragallery/ui/screens/story/StoryGeneratorScreen.kt
package com.example.visoragallery.ui.screens.story

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.visoragallery.utils.Language
import com.example.visoragallery.utils.StoryType
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryGeneratorScreen(
    navController: NavController,
    photoPath: String,
    viewModel: StoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val stories by viewModel.stories.collectAsState()
    val selectedType by viewModel.selectedStoryType.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    var showTypeSelector by remember { mutableStateOf(false) }
    var showLanguageSelector by remember { mutableStateOf(false) }

    val photoFile = remember { File(photoPath) }

    LaunchedEffect(photoPath) {
        viewModel.loadStoriesForPhoto(photoPath)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Story Generator") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Language selector
                    IconButton(onClick = { showLanguageSelector = true }) {
                        Icon(Icons.Filled.Language, "Language")
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState !is StoryUiState.Loading && uiState !is StoryUiState.Generating) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.generateStory(photoPath) },
                    icon = { Icon(Icons.Filled.AutoAwesome, "Generate") },
                    text = { Text("Generate Story") }
                )
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // Photo preview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(16.dp)
            ) {
                AsyncImage(
                    model = photoFile,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Story type selector
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = { showTypeSelector = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = selectedType.emoji,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Story Type",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                selectedType.displayName,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // State content
            when (val state = uiState) {
                is StoryUiState.Idle -> {
                    if (stories.isEmpty()) {
                        EmptyStoriesState()
                    } else {
                        StoriesList(
                            stories = stories,
                            onCopy = { story ->
                                clipboardManager.setText(AnnotatedString(story.story))
                            },
                            onShare = { /* TODO */ },
                            onDelete = { story -> viewModel.deleteStory(story) },
                            onFavorite = { story -> viewModel.toggleFavorite(story) }
                        )
                    }
                }

                is StoryUiState.Loading -> {
                    LoadingState("Preparing...")
                }

                is StoryUiState.Generating -> {
                    GeneratingState(state.progress)
                }

                is StoryUiState.Success -> {
                    SuccessState(
                        story = state.story.story,
                        onDone = { viewModel.resetState() }
                    )
                }

                is StoryUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.generateStory(photoPath) },
                        onDismiss = { viewModel.resetState() }
                    )
                }

                else -> {}
            }
        }
    }

    // Type selector dialog
    if (showTypeSelector) {
        StoryTypeDialog(
            currentType = selectedType,
            onDismiss = { showTypeSelector = false },
            onSelect = { type ->
                viewModel.setStoryType(type)
                showTypeSelector = false
            }
        )
    }

    // Language selector dialog
    if (showLanguageSelector) {
        LanguageDialog(
            currentLanguage = selectedLanguage,
            onDismiss = { showLanguageSelector = false },
            onSelect = { lang ->
                viewModel.setLanguage(lang)
                showLanguageSelector = false
            }
        )
    }
}

@Composable
fun EmptyStoriesState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Filled.AutoStories,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "No Stories Yet",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Tap the button below to generate an AI story for this photo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LoadingState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(message)
        }
    }
}

@Composable
fun GeneratingState(progress: Float) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Filled.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Generating Story...",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun SuccessState(
    story: String,
    onDone: () -> Unit
) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        onDone()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color(0xFF4CAF50)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Story Generated!",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = story,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Filled.Error,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Error",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Button(onClick = onRetry) {
                    Icon(Icons.Filled.Refresh, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
fun StoriesList(
    stories: List<com.example.visoragallery.data.database.PhotoStoryEntity>,
    onCopy: (com.example.visoragallery.data.database.PhotoStoryEntity) -> Unit,
    onShare: (com.example.visoragallery.data.database.PhotoStoryEntity) -> Unit,
    onDelete: (com.example.visoragallery.data.database.PhotoStoryEntity) -> Unit,
    onFavorite: (com.example.visoragallery.data.database.PhotoStoryEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(stories, key = { it.id }) { story ->
            StoryCard(
                story = story,
                onCopy = { onCopy(story) },
                onShare = { onShare(story) },
                onDelete = { onDelete(story) },
                onFavorite = { onFavorite(story) }
            )
        }
    }
}

@Composable
fun StoryCard(
    story: com.example.visoragallery.data.database.PhotoStoryEntity,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onFavorite: () -> Unit
) {
    val storyType = try {
        StoryType.valueOf(story.type)
    } catch (e: Exception) {
        StoryType.CREATIVE
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = storyType.emoji,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        storyType.displayName,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                IconButton(onClick = onFavorite) {
                    Icon(
                        if (story.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (story.isFavorite) Color(0xFFF85D58) else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Story text
            Text(
                text = story.story,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = onCopy) {
                    Icon(Icons.Filled.ContentCopy, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy")
                }

                TextButton(onClick = onShare) {
                    Icon(Icons.Filled.Share, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share")
                }

                TextButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
fun StoryTypeDialog(
    currentType: StoryType,
    onDismiss: () -> Unit,
    onSelect: (StoryType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Story Type") },
        text = {
            Column {
                StoryType.values().forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(type) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = type.emoji,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.width(48.dp)
                        )
                        Text(
                            type.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        if (type == currentType) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun LanguageDialog(
    currentLanguage: Language,
    onDismiss: () -> Unit,
    onSelect: (Language) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Language") },
        text = {
            Column {
                Language.values().forEach { lang ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(lang) }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            lang.displayName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (lang == currentLanguage) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}