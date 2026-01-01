// app/src/main/java/com/example/visoragallery/ui/screens/story/StoryViewModel.kt
package com.example.visoragallery.ui.screens.story

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.visoragallery.data.database.PhotoStoryEntity
import com.example.visoragallery.utils.Language
import com.example.visoragallery.utils.StoryType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed class StoryUiState {
    object Idle : StoryUiState()
    object Loading : StoryUiState()
    data class Generating(val progress: Float) : StoryUiState()
    data class Success(val story: PhotoStoryEntity) : StoryUiState()
    data class BatchGenerating(val current: Int, val total: Int) : StoryUiState()
    data class BatchSuccess(val stories: List<PhotoStoryEntity>) : StoryUiState()
    data class Error(val message: String) : StoryUiState()
}

class StoryViewModel(application: Application) : AndroidViewModel(application) {

    private val storyManager = StoryManager.getInstance(application)

    private val _uiState = MutableStateFlow<StoryUiState>(StoryUiState.Idle)
    val uiState: StateFlow<StoryUiState> = _uiState.asStateFlow()

    private val _stories = MutableStateFlow<List<PhotoStoryEntity>>(emptyList())
    val stories: StateFlow<List<PhotoStoryEntity>> = _stories.asStateFlow()

    private val _selectedStoryType = MutableStateFlow(StoryType.CREATIVE)
    val selectedStoryType: StateFlow<StoryType> = _selectedStoryType.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(Language.VIETNAMESE)
    val selectedLanguage: StateFlow<Language> = _selectedLanguage.asStateFlow()

    private val _statistics = MutableStateFlow<StoryStatistics?>(null)
    val statistics: StateFlow<StoryStatistics?> = _statistics.asStateFlow()

    init {
        loadAllStories()
        loadStatistics()
    }

    /**
     * Tạo story cho một ảnh
     */
    fun generateStory(photoPath: String) {
        viewModelScope.launch {
            try {
                _uiState.value = StoryUiState.Loading

                val photoFile = File(photoPath)
                if (!photoFile.exists()) {
                    _uiState.value = StoryUiState.Error("Photo file not found")
                    return@launch
                }

                val story = storyManager.generateStory(
                    photoFile,
                    _selectedStoryType.value,
                    _selectedLanguage.value
                ) { progress ->
                    _uiState.value = StoryUiState.Generating(progress)
                }

                if (story != null) {
                    _uiState.value = StoryUiState.Success(story)
                    loadAllStories() // Refresh list
                } else {
                    _uiState.value = StoryUiState.Error("Failed to generate story")
                }

            } catch (e: Exception) {
                _uiState.value = StoryUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Tạo stories cho nhiều ảnh
     */
    fun generateBatchStories(photoPaths: List<String>) {
        viewModelScope.launch {
            try {
                _uiState.value = StoryUiState.Loading

                val photoFiles = photoPaths.mapNotNull { path ->
                    File(path).takeIf { it.exists() }
                }

                if (photoFiles.isEmpty()) {
                    _uiState.value = StoryUiState.Error("No valid photos found")
                    return@launch
                }

                val results = storyManager.generateBatchStories(
                    photoFiles,
                    _selectedStoryType.value,
                    _selectedLanguage.value
                ) { current, total ->
                    _uiState.value = StoryUiState.BatchGenerating(current, total)
                }

                if (results.isNotEmpty()) {
                    _uiState.value = StoryUiState.BatchSuccess(results)
                    loadAllStories()
                } else {
                    _uiState.value = StoryUiState.Error("Failed to generate stories")
                }

            } catch (e: Exception) {
                _uiState.value = StoryUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Load tất cả stories
     */
    fun loadAllStories() {
        viewModelScope.launch {
            storyManager.getAllStories().collect { stories ->
                _stories.value = stories
            }
        }
    }

    /**
     * Load stories cho một ảnh
     */
    fun loadStoriesForPhoto(photoPath: String) {
        viewModelScope.launch {
            storyManager.getStoriesForPhoto(photoPath).collect { stories ->
                _stories.value = stories
            }
        }
    }

    /**
     * Load favorite stories
     */
    fun loadFavoriteStories() {
        viewModelScope.launch {
            storyManager.getFavoriteStories().collect { stories ->
                _stories.value = stories
            }
        }
    }

    /**
     * Load statistics
     */
    fun loadStatistics() {
        viewModelScope.launch {
            storyManager.getStatistics().collect { stats ->
                _statistics.value = stats
            }
        }
    }

    /**
     * Toggle favorite
     */
    fun toggleFavorite(story: PhotoStoryEntity) {
        viewModelScope.launch {
            storyManager.toggleFavorite(story)
            loadAllStories()
        }
    }

    /**
     * Delete story
     */
    fun deleteStory(story: PhotoStoryEntity) {
        viewModelScope.launch {
            storyManager.deleteStory(story)
            loadAllStories()
        }
    }

    /**
     * Set story type
     */
    fun setStoryType(type: StoryType) {
        _selectedStoryType.value = type
    }

    /**
     * Set language
     */
    fun setLanguage(language: Language) {
        _selectedLanguage.value = language
    }

    /**
     * Validate API key
     */
    fun validateApiKey(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val isValid = storyManager.validateApiKey()
            onResult(isValid)
        }
    }

    /**
     * Reset state
     */
    fun resetState() {
        _uiState.value = StoryUiState.Idle
    }

    /**
     * Check if photo has story
     */
    suspend fun hasStory(photoPath: String): Boolean {
        return storyManager.hasStory(photoPath)
    }
}