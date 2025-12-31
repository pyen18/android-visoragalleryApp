package com.example.visoragallery.ui.screens.albums

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.visoragallery.data.AlbumItem
import com.example.visoragallery.repository.AlbumRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AlbumsUiState {
    object Loading : AlbumsUiState()
    data class Success(val albums: List<AlbumItem>) : AlbumsUiState()
    data class Error(val message: String) : AlbumsUiState()
}

class AlbumsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AlbumRepository(application)

    private val _uiState = MutableStateFlow<AlbumsUiState>(AlbumsUiState.Loading)
    val uiState: StateFlow<AlbumsUiState> = _uiState.asStateFlow()
    private val _showCreateAlbumDialog = MutableStateFlow(false)
    val showCreateAlbumDialog: StateFlow<Boolean> = _showCreateAlbumDialog.asStateFlow()

    private val _showAddPhotosDialog = MutableStateFlow(false)
    val showAddPhotosDialog: StateFlow<Boolean> = _showAddPhotosDialog.asStateFlow()

    private val _selectedAlbum = MutableStateFlow<AlbumItem?>(null)
    val selectedAlbum: StateFlow<AlbumItem?> = _selectedAlbum.asStateFlow()

    init {
        loadAlbums()
    }

    fun loadAlbums() {
        viewModelScope.launch {
            _uiState.value = AlbumsUiState.Loading
            try {
                val albums = repository.getAllAlbums()
                _uiState.value = AlbumsUiState.Success(albums)
            } catch (e: Exception) {
                _uiState.value = AlbumsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun showCreateAlbumDialog() {
        _showCreateAlbumDialog.value = true
    }

    fun hideCreateAlbumDialog() {
        _showCreateAlbumDialog.value = false
    }

    fun createAlbum(albumName: String, onComplete: (success: Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val success = repository.createAlbum(albumName)
                if (success) {
                    loadAlbums()
                }
                onComplete(success)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    fun deleteAlbum(albumId: String, onComplete: (success: Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val success = repository.deleteAlbum(albumId)
                if (success) {
                    loadAlbums()
                }
                onComplete(success)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    fun renameAlbum(albumId: String, newName: String, onComplete: (success: Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val success = repository.renameAlbum(albumId, newName)
                if (success) {
                    loadAlbums()
                }
                onComplete(success)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    fun refreshAlbums() {
        loadAlbums()
    }
}