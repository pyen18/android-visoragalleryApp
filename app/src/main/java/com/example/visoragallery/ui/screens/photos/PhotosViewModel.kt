package com.example.visoragallery.ui.screens.photos

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.visoragallery.data.PhotoItem
import com.example.visoragallery.data.AlbumItem
import com.example.visoragallery.data.AlbumType
import com.example.visoragallery.repository.AlbumRepository
import com.example.visoragallery.repository.PhotoRepository
import com.example.visoragallery.ui.screens.trashbin.TrashBinManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/* -------------------- UI STATE -------------------- */

sealed class PhotosUiState {
    object Loading : PhotosUiState()
    data class Success(val photos: List<PhotoItem>) : PhotosUiState()
    data class Error(val message: String) : PhotosUiState()
}

/* -------------------- VIEW MODEL -------------------- */

class PhotosViewModel(application: Application) : AndroidViewModel(application) {

    /* -------------------- Repository -------------------- */

    private val photoRepository = PhotoRepository(application)
    private val albumRepository = AlbumRepository(application)
    private val trashBinManager = TrashBinManager.getInstance(application)

    /* -------------------- UI STATE -------------------- */

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

    /* -------------------- Album UI State -------------------- */

    private val _availableAlbums = MutableStateFlow<List<AlbumItem>>(emptyList())
    val availableAlbums: StateFlow<List<AlbumItem>> = _availableAlbums.asStateFlow()

    private val _showAddToAlbumDialog = MutableStateFlow(false)
    val showAddToAlbumDialog: StateFlow<Boolean> = _showAddToAlbumDialog.asStateFlow()

    private val _showCreateAlbumDialog = MutableStateFlow(false)
    val showCreateAlbumDialog: StateFlow<Boolean> = _showCreateAlbumDialog.asStateFlow()

    /* -------------------- INIT -------------------- */

    init {
        loadPhotos()
    }

    /* -------------------- PHOTO LOADING -------------------- */

    fun loadPhotos() {
        viewModelScope.launch {
            _uiState.value = PhotosUiState.Loading
            try {
                photoRepository.getAllPhotos().collect { photos ->
                    _uiState.value = PhotosUiState.Success(photos)
                }
            } catch (e: Exception) {
                _uiState.value =
                    PhotosUiState.Error(e.message ?: "Load photos failed")
            }
        }
    }

    fun refreshPhotos() {
        viewModelScope.launch {
            try {
                val photos = photoRepository.refreshPhotos()
                _uiState.value = PhotosUiState.Success(photos)
            } catch (e: Exception) {
                _uiState.value =
                    PhotosUiState.Error(e.message ?: "Refresh failed")
            }
        }
    }

    /* -------------------- SELECTION -------------------- */

    fun enterSelectionMode() {
        _isSelectionMode.value = true
    }

    fun exitSelectionMode() {
        _isSelectionMode.value = false
        _selectedPhotos.value = emptySet()
    }

    fun togglePhotoSelection(photoPath: String) {
        val set = _selectedPhotos.value.toMutableSet()
        if (!set.add(photoPath)) set.remove(photoPath)
        _selectedPhotos.value = set

        if (set.isEmpty()) {
            _isSelectionMode.value = false
        }
    }

    fun selectAllPhotos() {
        val state = _uiState.value
        if (state is PhotosUiState.Success) {
            _selectedPhotos.value = state.photos.map { it.path }.toSet()
            _isSelectionMode.value = true
        }
    }

    /* -------------------- GRID -------------------- */

    fun setSpanCount(count: Int) {
        _spanCount.value = count.coerceIn(1, 6)
    }

    /* -------------------- DELETE -------------------- */

    fun deleteSelectedPhotos(
        onComplete: (success: Boolean, deletedCount: Int) -> Unit
    ) {
        viewModelScope.launch {
            _deleteInProgress.value = true

            val targets = _selectedPhotos.value.toList()
            var successCount = 0

            targets.forEach {
                if (trashBinManager.moveToTrash(it)) {
                    successCount++
                }
            }

            _deleteInProgress.value = false
            exitSelectionMode()
            refreshPhotos()

            onComplete(successCount == targets.size, successCount)
        }
    }

    fun deleteSinglePhoto(
        photoPath: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _deleteInProgress.value = true
            val success = trashBinManager.moveToTrash(photoPath)
            _deleteInProgress.value = false

            if (success) refreshPhotos()
            onComplete(success)
        }
    }

    /* -------------------- ALBUM -------------------- */

    fun loadAvailableAlbums() {
        viewModelScope.launch {
            try {
                _availableAlbums.value =
                    albumRepository.getAllAlbums().filter {
                        it.type == AlbumType.USER_DEFINED && !it.id.startsWith("/")
                    }
            } catch (e: Exception) {
                Log.e("PhotosViewModel", "Load albums error", e)
            }
        }
    }

    fun showAddToAlbumDialog() {
        loadAvailableAlbums()
        _showAddToAlbumDialog.value = true
    }

    fun hideAddToAlbumDialog() {
        _showAddToAlbumDialog.value = false
    }

    fun showCreateAlbumDialog() {
        _showCreateAlbumDialog.value = true
    }

    fun hideCreateAlbumDialog() {
        _showCreateAlbumDialog.value = false
    }

    fun addSelectedPhotosToAlbum(
        albumId: String,
        onComplete: (success: Boolean, count: Int) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val photos = _selectedPhotos.value.toList()
                val success =
                    albumRepository.addPhotosToAlbum(albumId, photos)

                if (success) exitSelectionMode()
                onComplete(success, photos.size)
            } catch (e: Exception) {
                onComplete(false, 0)
            }
        }
    }

    fun createAlbumAndAddPhotos(
        albumName: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (!albumRepository.createAlbum(albumName)) {
                    onComplete(false)
                    return@launch
                }

                val album = albumRepository.getUserAlbums()
                    .firstOrNull { it.name == albumName }

                if (album == null) {
                    onComplete(false)
                    return@launch
                }

                val success = albumRepository.addPhotosToAlbum(
                    album.id,
                    _selectedPhotos.value.toList()
                )

                if (success) exitSelectionMode()
                onComplete(success)

            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }
}
