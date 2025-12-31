package com.example.visoragallery.ui.screens.settings
import android.app.Application
import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.visoragallery.settings.AppConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class StorageInfo(
    val usedSpace: Long = 0,
    val totalSpace: Long = 0,
    val freeSpace: Long = 0
) {
    val usedPercentage: Float
        get() = if (totalSpace > 0) (usedSpace.toFloat() / totalSpace.toFloat()) else 0f

    fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }
}

data class SettingsUiState(
    val nightMode: Boolean = false,
    val trashMode: Boolean = true,
    val timeLapse: String = "2 seconds",
    val defaultColumns: Int = 4,
    val storageInfo: StorageInfo = StorageInfo()
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val appConfig = AppConfig.getInstance(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        loadStorageInfo()
    }

    private fun loadSettings() {
        _uiState.value = _uiState.value.copy(
            nightMode = appConfig.getNightMode(),
            trashMode = appConfig.getTrashMode(),
            timeLapse = appConfig.getTimeLapse()
        )
    }

    fun toggleNightMode(enabled: Boolean) {
        appConfig.setNightMode(enabled)
        _uiState.value = _uiState.value.copy(nightMode = enabled)
    }

    fun toggleTrashMode(enabled: Boolean) {
        appConfig.setTrashMode(enabled)
        _uiState.value = _uiState.value.copy(trashMode = enabled)
    }

    fun setTimeLapse(value: String) {
        appConfig.setTimeLapse(value)
        _uiState.value = _uiState.value.copy(timeLapse = value)
    }

    fun setDefaultColumns(columns: Int) {
        // TODO: Save to preferences when needed
        _uiState.value = _uiState.value.copy(defaultColumns = columns)
    }

    private fun loadStorageInfo() {
        viewModelScope.launch {
            try {
                val picturesDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES
                )
                val visoraDir = File(picturesDir, "VisoraGallery")

                // Calculate used space in VisoraGallery folder
                val usedSpace = if (visoraDir.exists()) {
                    calculateFolderSize(visoraDir)
                } else {
                    0L
                }

                // Get total storage info
                val stat = StatFs(Environment.getExternalStorageDirectory().path)
                val totalSpace = stat.blockCountLong * stat.blockSizeLong
                val freeSpace = stat.availableBlocksLong * stat.blockSizeLong

                _uiState.value = _uiState.value.copy(
                    storageInfo = StorageInfo(
                        usedSpace = usedSpace,
                        totalSpace = totalSpace,
                        freeSpace = freeSpace
                    )
                )
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    private fun calculateFolderSize(directory: File): Long {
        var size = 0L
        if (directory.isDirectory) {
            directory.listFiles()?.forEach { file ->
                size += if (file.isDirectory) {
                    calculateFolderSize(file)
                } else {
                    file.length()
                }
            }
        }
        return size
    }

    fun refreshStorageInfo() {
        loadStorageInfo()
    }
}