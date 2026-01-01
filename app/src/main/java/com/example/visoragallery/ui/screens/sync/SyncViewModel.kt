package com.example.visoragallery.ui.screens.sync

import android.app.Application
import android.content.Intent
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.visoragallery.utils.GoogleDriveHelper
import com.example.visoragallery.utils.SyncResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed class SyncUiState {
    object SignedOut : SyncUiState()
    object SignedIn : SyncUiState()
    data class Syncing(val current: Int, val total: Int, val direction: SyncDirection) : SyncUiState()
    data class SyncComplete(val result: SyncResult, val direction: SyncDirection) : SyncUiState()
    data class Error(val message: String) : SyncUiState()
}

enum class SyncDirection {
    FROM_DRIVE, // Download from Drive
    TO_DRIVE    // Upload to Drive
}

class SyncViewModel(application: Application) : AndroidViewModel(application) {

    private val driveHelper = GoogleDriveHelper(application)

    private val _uiState = MutableStateFlow<SyncUiState>(
        if (driveHelper.isSignedIn()) SyncUiState.SignedIn else SyncUiState.SignedOut
    )
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime.asStateFlow()

    fun getSignInIntent(): Intent {
        return driveHelper.getSignInIntent()
    }

    fun handleSignInResult(data: Intent?) {
        viewModelScope.launch {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                driveHelper.handleSignInResult(account)
                _uiState.value = SyncUiState.SignedIn
            } catch (e: ApiException) {
                android.util.Log.e("SyncViewModel", "Sign in failed", e)
                _uiState.value = SyncUiState.Error("Sign in failed: ${e.message}")
            }
        }
    }

    fun signOut() {
        driveHelper.signOut {
            _uiState.value = SyncUiState.SignedOut
            _lastSyncTime.value = null
        }
    }

    fun syncFromDrive() {
        viewModelScope.launch {
            try {
                _uiState.value = SyncUiState.Syncing(0, 0, SyncDirection.FROM_DRIVE)

                val picturesDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "VisoraGallery"
                )

                if (!picturesDir.exists()) {
                    picturesDir.mkdirs()
                }

                val result = driveHelper.syncFromDrive(picturesDir) { current, total ->
                    _uiState.value = SyncUiState.Syncing(current, total, SyncDirection.FROM_DRIVE)
                }

                _lastSyncTime.value = System.currentTimeMillis()
                _uiState.value = SyncUiState.SyncComplete(result, SyncDirection.FROM_DRIVE)

            } catch (e: Exception) {
                android.util.Log.e("SyncViewModel", "Sync from Drive failed", e)
                _uiState.value = SyncUiState.Error("Sync failed: ${e.message}")
            }
        }
    }

    fun syncToDrive(localPhotos: List<File>) {
        viewModelScope.launch {
            try {
                _uiState.value = SyncUiState.Syncing(0, localPhotos.size, SyncDirection.TO_DRIVE)

                val uploadedCount = driveHelper.syncToDrive(localPhotos) { current, total ->
                    _uiState.value = SyncUiState.Syncing(current, total, SyncDirection.TO_DRIVE)
                }

                _lastSyncTime.value = System.currentTimeMillis()

                val result = SyncResult(
                    downloaded = uploadedCount,
                    total = localPhotos.size,
                    files = emptyList()
                )

                _uiState.value = SyncUiState.SyncComplete(result, SyncDirection.TO_DRIVE)

            } catch (e: Exception) {
                android.util.Log.e("SyncViewModel", "Sync to Drive failed", e)
                _uiState.value = SyncUiState.Error("Sync failed: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = if (driveHelper.isSignedIn()) {
            SyncUiState.SignedIn
        } else {
            SyncUiState.SignedOut
        }
    }
}