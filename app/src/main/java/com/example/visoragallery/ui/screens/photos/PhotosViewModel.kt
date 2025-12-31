package com.example.visoragallery.ui.screens.photos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.visoragallery.data.PhotoItem
import com.example.visoragallery.repository.PhotoRepository
import com.example.visoragallery.ui.screens.trashbin.TrashBinManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PhotosUiState {
    object Loading : PhotosUiState()
    data class Success(val photos: List<PhotoItem>) : PhotosUiState()
    data class Error(val message: String) : PhotosUiState()
}

class PhotosViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PhotoRepository(application)
    private val trashBinManager = TrashBinManager.getInstance(application)

    private val _uiState = MutableStateFlow<PhotosUiState>(PhotosUiState.Loading)
    val uiState: StateFlow<PhotosUiState> = _uiState.asStateFlow()

    private val _selectedPhotos = MutableStateFlow<Set<String>>(emptySet())
    val selectedPhotos: StateFlow<Set<String>> = _selectedPhotos.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _spanCount = MutableStateFlow(4)
    val spanCount: StateFlow<Int> = _spanCount.asStateFlow()

    private val _deleteInProgress = MutableStateFlow(false)
    val deleteInProgress: StateFlow<Boolean> = _deleteInProgress.asStateFlow()

    init {
        loadPhotos()
    }

    fun loadPhotos() {
        viewModelScope.launch {
            _uiState.value = PhotosUiState.Loading
            try {
                repository.getAllPhotos().collect { photos ->
                    _uiState.value = PhotosUiState.Success(photos)
                }
            } catch (e: Exception) {
                _uiState.value = PhotosUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun refreshPhotos() {
        viewModelScope.launch {
            try {
                val photos = repository.refreshPhotos()
                _uiState.value = PhotosUiState.Success(photos)
            } catch (e: Exception) {
                _uiState.value = PhotosUiState.Error(e.message ?: "Unknown error")
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

        // Exit selection mode if no photos selected
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
        if (state is PhotosUiState.Success) {
            _selectedPhotos.value = state.photos.map { it.path }.toSet()
        }
    }

    fun setSpanCount(count: Int) {
        _spanCount.value = count.coerceIn(1, 6)
    }

    // Delete selected photos
    fun deleteSelectedPhotos(onComplete: (success: Boolean, count: Int) -> Unit) {
        viewModelScope.launch {
            _deleteInProgress.value = true

            val photosToDelete = _selectedPhotos.value.toList()
            var successCount = 0

            photosToDelete.forEach { photoPath ->
                val success = trashBinManager.moveToTrash(photoPath)
                if (success) successCount++
            }

            _deleteInProgress.value = false

            // Exit selection mode and refresh
            exitSelectionMode()
            refreshPhotos()

            onComplete(successCount == photosToDelete.size, successCount)
        }
    }

    // Delete single photo
    fun deletePhoto(photoPath: String, onComplete: (success: Boolean) -> Unit) {
        viewModelScope.launch {
            _deleteInProgress.value = true

            val success = trashBinManager.moveToTrash(photoPath)

            _deleteInProgress.value = false

            if (success) {
                refreshPhotos()
            }

            onComplete(success)
        }
    }
}