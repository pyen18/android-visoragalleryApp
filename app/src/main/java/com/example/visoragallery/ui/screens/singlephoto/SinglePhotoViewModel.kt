package com.example.visoragallery.ui.screens.singlephoto

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.visoragallery.data.PhotoItem
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
    val isFavorite: Boolean = false
)

class SinglePhotoViewModel(application: Application) : AndroidViewModel(application) {

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

            // Check if current photo is favorite
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
}