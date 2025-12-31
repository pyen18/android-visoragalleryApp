package com.example.visoragallery.ui.screens.singlephoto

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
import java.text.SimpleDateFormat
import java.util.*

data class SinglePhotoUiState(
    val photos: List<PhotoItem> = emptyList(),
    val currentIndex: Int = 0,
    val currentPhoto: PhotoItem? = null,
    val showUI: Boolean = true,
    val isFavorite: Boolean = false,
    val deleteInProgress: Boolean = false
)

class SinglePhotoViewModel(application: Application) : AndroidViewModel(application) {

    private val trashBinManager = TrashBinManager.getInstance(application)

    private val _uiState = MutableStateFlow(SinglePhotoUiState())
    val uiState: StateFlow<SinglePhotoUiState> = _uiState.asStateFlow()

    fun initPhotos(photoPaths: Array<String>, currentPosition: Int) {
        viewModelScope.launch {
            val photos = photoPaths.mapNotNull { path ->
                val file = File(path)
                if (file.exists()) {
                    PhotoItem(
                        file = file,
                        path = path,
                        name = file.name,
                        size = file.length(),
                        lastModified = file.lastModified(),
                        uri = Uri.fromFile(file)
                    )
                } else null
            }

            _uiState.value = _uiState.value.copy(
                photos = photos,
                currentIndex = currentPosition,
                currentPhoto = photos.getOrNull(currentPosition)
            )

            checkFavoriteStatus()
        }
    }

    fun setCurrentIndex(index: Int) {
        val photos = _uiState.value.photos
        if (index in photos.indices) {
            _uiState.value = _uiState.value.copy(
                currentIndex = index,
                currentPhoto = photos[index]
            )
            checkFavoriteStatus()
        }
    }

    fun toggleUI() {
        _uiState.value = _uiState.value.copy(
            showUI = !_uiState.value.showUI
        )
    }

    fun showUI() {
        _uiState.value = _uiState.value.copy(showUI = true)
    }

    fun hideUI() {
        _uiState.value = _uiState.value.copy(showUI = false)
    }

    fun toggleFavorite() {
        val currentFavorite = _uiState.value.isFavorite
        _uiState.value = _uiState.value.copy(
            isFavorite = !currentFavorite
        )
        // TODO: Save to database
    }

    private fun checkFavoriteStatus() {
        // TODO: Check from database
        _uiState.value = _uiState.value.copy(isFavorite = false)
    }

    fun deleteCurrentPhoto(onComplete: (success: Boolean) -> Unit) {
        viewModelScope.launch {
            val currentPhoto = _uiState.value.currentPhoto
            if (currentPhoto == null) {
                onComplete(false)
                return@launch
            }

            _uiState.value = _uiState.value.copy(deleteInProgress = true)

            val success = trashBinManager.moveToTrash(currentPhoto.path)

            _uiState.value = _uiState.value.copy(deleteInProgress = false)

            if (success) {
                // Remove deleted photo from list
                val updatedPhotos = _uiState.value.photos.toMutableList()
                val deletedIndex = _uiState.value.currentIndex

                if (deletedIndex in updatedPhotos.indices) {
                    updatedPhotos.removeAt(deletedIndex)

                    // Update current index
                    val newIndex = when {
                        updatedPhotos.isEmpty() -> -1
                        deletedIndex >= updatedPhotos.size -> updatedPhotos.size - 1
                        else -> deletedIndex
                    }

                    _uiState.value = _uiState.value.copy(
                        photos = updatedPhotos,
                        currentIndex = newIndex,
                        currentPhoto = if (newIndex >= 0) updatedPhotos.getOrNull(newIndex) else null
                    )
                }
            }

            onComplete(success)
        }
    }

    fun getFormattedDate(): String {
        val photo = _uiState.value.currentPhoto ?: return ""
        val date = Date(photo.lastModified)
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }

    fun getFormattedTime(): String {
        val photo = _uiState.value.currentPhoto ?: return ""
        val date = Date(photo.lastModified)
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return timeFormat.format(date)
    }

    fun getPhotoInfo(): String {
        val photo = _uiState.value.currentPhoto ?: return ""
        val sizeMB = photo.size / (1024.0 * 1024.0)
        return String.format("%.2f MB", sizeMB)
    }

    fun getCurrentPhotoPath(): String? {
        return _uiState.value.currentPhoto?.path
    }

    fun hasNextPhoto(): Boolean {
        val state = _uiState.value
        return state.currentIndex < state.photos.size - 1
    }

    fun hasPreviousPhoto(): Boolean {
        return _uiState.value.currentIndex > 0
    }

    fun isEmpty(): Boolean {
        return _uiState.value.photos.isEmpty()
    }
}