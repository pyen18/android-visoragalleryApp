package com.example.visoragallery.ui.screens.story

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryGalleryScreen(
    navController: NavController,
    viewModel: StoryViewModel = viewModel()
) {
    // Lấy danh sách tất cả stories từ ViewModel
    val stories by viewModel.stories.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    // Khi mở màn hình này, load toàn bộ story thay vì chỉ load của 1 ảnh
    LaunchedEffect(Unit) {
        viewModel.loadAllStories()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Stories Gallery") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (stories.isEmpty()) {
                EmptyStoriesState() // Tái sử dụng Composable từ StoryGeneratorScreen
            } else {
                // Tái sử dụng StoriesList từ StoryGeneratorScreen
                StoriesList(
                    stories = stories,
                    onCopy = { story ->
                        clipboardManager.setText(AnnotatedString(story.story))
                    },
                    onShare = { /* TODO: Implement Share logic later */ },
                    onDelete = { story -> viewModel.deleteStory(story) },
                    onFavorite = { story -> viewModel.toggleFavorite(story) }
                )
            }
        }
    }
}