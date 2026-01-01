package com.example.visoragallery.utils
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class GoogleDriveHelper(private val context: Context) {

    companion object {
        private const val TAG = "GoogleDriveHelper"
        private const val FOLDER_NAME = "VisoraGallery"
        private val SCOPES = listOf(
            DriveScopes.DRIVE
        )
    }

    private var driveService: Drive? = null
    private var currentAccount: GoogleSignInAccount? = null

    // Check if user is signed in
    fun isSignedIn(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        currentAccount = account
        return account != null && hasPermissions(account)
    }

    private fun hasPermissions(account: GoogleSignInAccount): Boolean {
        return account.grantedScopes.containsAll(
            SCOPES.map { Scope(it) }
        )
    }

    // Get sign in intent
    // Trong file GoogleDriveHelper.kt

    fun getSignInIntent(): Intent {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE))
            .build()

        return GoogleSignIn.getClient(context, signInOptions).signInIntent
    }


    // Handle sign in result
    fun handleSignInResult(account: GoogleSignInAccount?) {
        currentAccount = account
        if (account != null) {
            initializeDriveService(account)
        }
    }

    // Initialize Drive service
    private fun initializeDriveService(account: GoogleSignInAccount) {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            SCOPES
        )
        credential.selectedAccount = account.account

        driveService = Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Visora Gallery")
            .build()
    }


    // Sign out
    fun signOut(onComplete: () -> Unit) {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        val client = GoogleSignIn.getClient(context, signInOptions)
        client.signOut().addOnCompleteListener {
            currentAccount = null
            driveService = null
            onComplete()
        }
    }

    // Upload photo to Drive
    suspend fun uploadPhoto(photoFile: File, onProgress: (Float) -> Unit): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val service = driveService ?: run {
                    Log.e(TAG, "Drive service not initialized")
                    return@withContext false
                }

                // Get or create folder
                val folderId = getOrCreateFolder(service)
                if (folderId == null) {
                    Log.e(TAG, "Failed to get or create folder")
                    return@withContext false
                }

                // Check if file already exists
                val existingFileId = findFileByName(service, photoFile.name, folderId)
                if (existingFileId != null) {
                    Log.d(TAG, "File already exists: ${photoFile.name}")
                    return@withContext true
                }

                // Create file metadata
                val fileMetadata = com.google.api.services.drive.model.File()
                fileMetadata.name = photoFile.name
                fileMetadata.parents = listOf(folderId)

                // Upload file
                val mediaContent = com.google.api.client.http.FileContent(
                    getMimeType(photoFile),
                    photoFile
                )

                service.files().create(fileMetadata, mediaContent)
                    .setFields("id, name")
                    .execute()

                Log.d(TAG, "Uploaded: ${photoFile.name}")
                onProgress(1f)
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading photo", e)
                false
            }
        }

    // Download photo from Drive
    suspend fun downloadPhoto(
        fileId: String,
        fileName: String,
        destinationDir: File,
        onProgress: (Float) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            val service = driveService ?: run {
                Log.e(TAG, "Drive service not initialized")
                return@withContext null
            }

            val outputFile = File(destinationDir, fileName)

            // Download file
            val outputStream = FileOutputStream(outputFile)
            service.files().get(fileId)
                .executeMediaAndDownloadTo(outputStream)

            outputStream.close()
            onProgress(1f)

            Log.d(TAG, "Downloaded: $fileName")
            outputFile
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading photo", e)
            null
        }
    }

    // Get all photos from Drive
    suspend fun getAllPhotosFromDrive(): List<DrivePhoto> = withContext(Dispatchers.IO) {
        try {
            val service = driveService ?: run {
                Log.e(TAG, "Drive service not initialized")
                return@withContext emptyList()
            }

            val folderId = getOrCreateFolder(service)
            if (folderId == null) {
                Log.e(TAG, "Failed to get or create folder")
                return@withContext emptyList()
            }

            val photos = mutableListOf<DrivePhoto>()
            var pageToken: String? = null

            do {
                val result = service.files().list()
                    .setQ("'$folderId' in parents and trashed=false and mimeType contains 'image/'")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name, size, createdTime, modifiedTime)")
                    .setPageToken(pageToken)
                    .execute()

                result.files?.forEach { file ->
                    photos.add(
                        DrivePhoto(
                            id = file.id,
                            name = file.name,
                            size = file.getSize() ?: 0,
                            createdTime = file.createdTime?.value ?: 0,
                            modifiedTime = file.modifiedTime?.value ?: 0
                        )
                    )
                }

                pageToken = result.nextPageToken
            } while (pageToken != null)

            Log.d(TAG, "Found ${photos.size} photos on Drive")
            photos
        } catch (e: Exception) {
            Log.e(TAG, "Error getting photos from Drive", e)
            emptyList()
        }
    }

    // Sync photos (download new photos from Drive)
    suspend fun syncFromDrive(
        localPhotosDir: File,
        onProgress: (current: Int, total: Int) -> Unit
    ): SyncResult = withContext(Dispatchers.IO) {
        try {
            val drivePhotos = getAllPhotosFromDrive()

            if (drivePhotos.isEmpty()) {
                return@withContext SyncResult(0, 0, emptyList())
            }

            // Get existing local photos
            val localPhotoNames = localPhotosDir.listFiles()
                ?.map { it.name }
                ?.toSet() ?: emptySet()

            // Find photos to download
            val photosToDownload = drivePhotos.filter {
                it.name !in localPhotoNames
            }

            if (photosToDownload.isEmpty()) {
                return@withContext SyncResult(0, 0, emptyList())
            }

            val downloaded = mutableListOf<File>()
            photosToDownload.forEachIndexed { index, photo ->
                onProgress(index + 1, photosToDownload.size)

                val file = downloadPhoto(
                    photo.id,
                    photo.name,
                    localPhotosDir
                ) { }

                if (file != null) {
                    downloaded.add(file)
                }
            }

            SyncResult(
                downloaded = downloaded.size,
                total = photosToDownload.size,
                files = downloaded
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing from Drive", e)
            SyncResult(0, 0, emptyList())
        }
    }

    // Upload local photos to Drive
    suspend fun syncToDrive(
        localPhotos: List<File>,
        onProgress: (current: Int, total: Int) -> Unit
    ): Int = withContext(Dispatchers.IO) {
        try {
            val drivePhotos = getAllPhotosFromDrive()
            val drivePhotoNames = drivePhotos.map { it.name }.toSet()

            // Find photos to upload
            val photosToUpload = localPhotos.filter {
                it.name !in drivePhotoNames
            }

            if (photosToUpload.isEmpty()) {
                return@withContext 0
            }

            var uploadedCount = 0
            photosToUpload.forEachIndexed { index, photo ->
                onProgress(index + 1, photosToUpload.size)

                val success = uploadPhoto(photo) { }
                if (success) {
                    uploadedCount++
                }
            }

            uploadedCount
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing to Drive", e)
            0
        }
    }

    // Helper: Get or create folder
    private fun getOrCreateFolder(service: Drive): String? {
        try {
            // Search for existing folder
            val result = service.files().list()
                .setQ("name='$FOLDER_NAME' and mimeType='application/vnd.google-apps.folder' and trashed=false")
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute()

            if (result.files.isNotEmpty()) {
                return result.files[0].id
            }

            // Create new folder
            val folderMetadata = com.google.api.services.drive.model.File()
            folderMetadata.name = FOLDER_NAME
            folderMetadata.mimeType = "application/vnd.google-apps.folder"

            val folder = service.files().create(folderMetadata)
                .setFields("id")
                .execute()

            return folder.id
        } catch (e: Exception) {
            Log.e(TAG, "Error getting or creating folder", e)
            return null
        }
    }

    // Helper: Find file by name
    private fun findFileByName(service: Drive, fileName: String, parentId: String): String? {
        try {
            val result = service.files().list()
                .setQ("name='$fileName' and '$parentId' in parents and trashed=false")
                .setSpaces("drive")
                .setFields("files(id)")
                .execute()

            return result.files.firstOrNull()?.id
        } catch (e: Exception) {
            Log.e(TAG, "Error finding file", e)
            return null
        }
    }

    // Helper: Get MIME type
    private fun getMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            else -> "image/*"
        }
    }
}

// Data classes
data class DrivePhoto(
    val id: String,
    val name: String,
    val size: Long,
    val createdTime: Long,
    val modifiedTime: Long
)

data class SyncResult(
    val downloaded: Int,
    val total: Int,
    val files: List<File>
)