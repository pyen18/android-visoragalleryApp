package com.example.visoragallery.ui.screens.trashbin

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.visoragallery.data.PhotoItem
import com.example.visoragallery.ui.screens.trashbin.TrashBinManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed class TrashBinUiState {
    object Loading : TrashBinUiState()
    data class Success(val photos: List<PhotoItem>) : TrashBinUiState()
    data class Error(val message: String) : TrashBinUiState()
}

class TrashBinViewModel(application: Application) : AndroidViewModel(application) {

    private val trashBinManager = TrashBinManager.getInstance(application)

    private val _uiState = MutableStateFlow<TrashBinUiState>(TrashBinUiState.Loading)
    val uiState: StateFlow<TrashBinUiState> = _uiState.asStateFlow()

    private val _selectedPhotos = MutableStateFlow<Set<String>>(emptySet())
    val selectedPhotos: StateFlow<Set<String>> = _selectedPhotos.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _operationInProgress = MutableStateFlow(false)
    val operationInProgress: StateFlow<Boolean> = _operationInProgress.asStateFlow()

    fun loadTrashPhotos() {
        viewModelScope.launch {
            _uiState.value = TrashBinUiState.Loading
            try {
                val files = trashBinManager.getTrashFiles()
                val photos = files.map { file ->
                    PhotoItem(
                        file = file,
                        path = file.absolutePath,
                        name = file.name,
                        size = file.length(),
                        lastModified = file.lastModified(),
                        uri = Uri.fromFile(file)
                    )
                }
                _uiState.value = TrashBinUiState.Success(photos)
            } catch (e: Exception) {
                _uiState.value = TrashBinUiState.Error(e.message ?: "Unknown error")
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

    fun restoreSelectedPhotos(onComplete: (success: Boolean, count: Int) -> Unit) {
        viewModelScope.launch {
            _operationInProgress.value = true

            val photosToRestore = _selectedPhotos.value.toList()
            var successCount = 0

            photosToRestore.forEach { photoPath ->
                val success = trashBinManager.restorePhoto(photoPath)
                if (success) successCount++
            }

            _operationInProgress.value = false
            exitSelectionMode()
            loadTrashPhotos()

            onComplete(successCount == photosToRestore.size, successCount)
        }
    }

    fun deleteSelectedPhotos(onComplete: (success: Boolean, count: Int) -> Unit) {
        viewModelScope.launch {
            _operationInProgress.value = true

            val photosToDelete = _selectedPhotos.value.toList()
            var successCount = 0

            photosToDelete.forEach { photoPath ->
                val success = trashBinManager.permanentlyDelete(photoPath)
                if (success) successCount++
            }

            _operationInProgress.value = false
            exitSelectionMode()
            loadTrashPhotos()

            onComplete(successCount == photosToDelete.size, successCount)
        }
    }

    fun restoreAllPhotos(onComplete: (success: Boolean, count: Int) -> Unit) {
        viewModelScope.launch {
            _operationInProgress.value = true

            val state = _uiState.value
            if (state is TrashBinUiState.Success) {
                var successCount = 0
                state.photos.forEach { photo ->
                    val success = trashBinManager.restorePhoto(photo.path)
                    if (success) successCount++
                }

                _operationInProgress.value = false
                loadTrashPhotos()

                onComplete(successCount == state.photos.size, successCount)
            } else {
                _operationInProgress.value = false
                onComplete(false, 0)
            }
        }
    }

    fun emptyTrash(onComplete: (success: Boolean) -> Unit) {
        viewModelScope.launch {
            _operationInProgress.value = true

            val success = trashBinManager.emptyTrash()

            _operationInProgress.value = false
            loadTrashPhotos()

            onComplete(success)
        }
    }
}