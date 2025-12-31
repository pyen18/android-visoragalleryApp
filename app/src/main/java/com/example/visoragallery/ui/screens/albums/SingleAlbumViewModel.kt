package com.example.visoragallery.ui.screens.albums

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.visoragallery.data.PhotoItem
import com.example.visoragallery.repository.AlbumRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SingleAlbumUiState {

    object Loading : SingleAlbumUiState()

    data class Success(
        val albumName: String,
        val photos: List<PhotoItem>
    ) : SingleAlbumUiState()

    data class Error(val message: String) : SingleAlbumUiState()

    fun title(): String = when (this) {
        is Success -> albumName
        is Error -> "Album"
        is Loading -> "Album"
    }
}



class SingleAlbumViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AlbumRepository(application)

    private val _uiState = MutableStateFlow<SingleAlbumUiState>(SingleAlbumUiState.Loading)
    val uiState: StateFlow<SingleAlbumUiState> = _uiState.asStateFlow()

    private val _selectedPhotos = MutableStateFlow<Set<String>>(emptySet())
    val selectedPhotos: StateFlow<Set<String>> = _selectedPhotos.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    fun loadAlbum(albumId: String) {
        viewModelScope.launch {
            _uiState.value = SingleAlbumUiState.Loading
            try {
                val photos = repository.getAlbumPhotos(albumId)
                val albumName = title(albumId)
                _uiState.value = SingleAlbumUiState.Success(albumName, photos)
            } catch (e: Exception) {
                _uiState.value = SingleAlbumUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun title(albumId: String): String {
        return when (albumId) {
            "favorites" -> "Favorites"
            "private" -> "Privacy"
            "trash_bin" -> "Trash Bin"
            else -> java.io.File(albumId).name
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
    fun addPhotosToAlbum(photoPaths: List<String>, onComplete: (success: Boolean) -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state is SingleAlbumUiState.Success) {
                try {
                    val albumId = getAlbumIdFromState()
                    val success = repository.addPhotosToAlbum(albumId, photoPaths)
                    if (success) {
                        loadAlbum(albumId)
                    }
                    onComplete(success)
                } catch (e: Exception) {
                    onComplete(false)
                }
            }
        }
    }

    private fun getAlbumIdFromState(): String {
        // Get from current loaded album
        return "" // You'll need to store this
    }
}