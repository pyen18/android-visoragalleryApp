package com.example.visoragallery.ui.screens.favorites
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.visoragallery.data.PhotoItem
import com.example.visoragallery.ui.screens.favorites.FavoritesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FavoritesUiState {
    object Loading : FavoritesUiState()
    data class Success(val photos: List<PhotoItem>) : FavoritesUiState()
    data class Error(val message: String) : FavoritesUiState()
}

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val favoritesManager = FavoritesManager.getInstance(application)

    private val _uiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Loading)
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private val _selectedPhotos = MutableStateFlow<Set<String>>(emptySet())
    val selectedPhotos: StateFlow<Set<String>> = _selectedPhotos.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _operationInProgress = MutableStateFlow(false)
    val operationInProgress: StateFlow<Boolean> = _operationInProgress.asStateFlow()

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.value = FavoritesUiState.Loading
            try {
                favoritesManager.getFavoritePhotos().collect { photos ->
                    _uiState.value = FavoritesUiState.Success(photos)
                }
            } catch (e: Exception) {
                _uiState.value = FavoritesUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun togglePhotoSelection(photoPath: String) {
        val currentSelection = _selectedPhotos.value.toMutableSet()
        if (currentSelection.contains(photoPath)) {
            currentSelection.remove(photoPath)
        } else {
            currentSelection.add(photoPath)
        }
        _selectedPhotos.value = currentSelection

        if (currentSelection.isEmpty()) {
            _isSelectionMode.value = false
        }
    }

    fun enterSelectionMode() {
        _isSelectionMode.value = true
    }

    fun exitSelectionMode() {
        _isSelectionMode.value = false
        _selectedPhotos.value = emptySet()
    }

    fun selectAllPhotos() {
        val state = _uiState.value
        if (state is FavoritesUiState.Success) {
            _selectedPhotos.value = state.photos.map { it.path }.toSet()
        }
    }

    fun removeSelectedFromFavorites(onComplete: (success: Boolean, count: Int) -> Unit) {
        viewModelScope.launch {
            _operationInProgress.value = true

            val photosToRemove = _selectedPhotos.value.toList()
            val successCount = favoritesManager.removeMultipleFromFavorites(photosToRemove)

            _operationInProgress.value = false
            exitSelectionMode()

            onComplete(successCount == photosToRemove.size, successCount)
        }
    }

    suspend fun getFavoritesCount(): Int {
        return favoritesManager.getFavoritesCount()
    }
}