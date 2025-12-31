package com.example.visoragallery.repository

import android.content.Context
import android.net.Uri
import com.example.visoragallery.data.AlbumItem
import com.example.visoragallery.data.AlbumType
import com.example.visoragallery.data.PhotoItem
import com.example.visoragallery.data.database.AlbumDatabase
import com.example.visoragallery.ui.screens.trashbin.TrashBinManager
import com.example.visoragallery.utils.MediaStoreHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AlbumRepository(private val context: Context) {

    private val database = AlbumDatabase.getInstance(context)

    private val mediaStoreHelper = MediaStoreHelper(context)
    private val trashBinManager = TrashBinManager.Companion.getInstance(context)

    suspend fun createAlbum(albumName: String): Boolean = withContext(Dispatchers.IO) {
        val albumId = java.util.UUID.randomUUID().toString()
        database.createAlbum(albumId, albumName)
    }

    suspend fun getUserAlbums(): List<AlbumItem> = withContext(Dispatchers.IO) {
        val albums = mutableListOf<AlbumItem>()

        database.getUserAlbums().forEach { (id, name) ->
            val photoCount = database.getAlbumPhotoCount(id)
            val photoPaths = database.getAlbumPhotoPaths(id)

            albums.add(
                AlbumItem(
                    id = id,
                    name = name,
                    type = AlbumType.USER_DEFINED,
                    photoCount = photoCount,
                    coverPhotoPath = photoPaths.firstOrNull()
                )
            )
        }

        albums
    }

    suspend fun addPhotosToAlbum(albumId: String, photoPaths: List<String>): Boolean =
        withContext(Dispatchers.IO) {
            database.addPhotosToAlbum(albumId, photoPaths)
        }

    suspend fun deleteAlbum(albumId: String): Boolean = withContext(Dispatchers.IO) {
        database.deleteAlbum(albumId)
    }

    suspend fun renameAlbum(albumId: String, newName: String): Boolean =
        withContext(Dispatchers.IO) {
            database.renameAlbum(albumId, newName)
        }


    suspend fun getAllAlbums(): List<AlbumItem> = withContext(Dispatchers.IO) {
        val albums = mutableListOf<AlbumItem>()

        // Add system albums
        albums.add(getFavoritesAlbum())
        albums.add(getPrivateAlbum())
        albums.add(getTrashBinAlbum())

        // Add user albums
        albums.addAll(getUserAlbums())

        // Add folder albums
        val allPhotos = mediaStoreHelper.getAllPhotos()
        val photosByFolder = allPhotos.groupBy { photo ->
            File(photo.path).parentFile?.absolutePath ?: "Unknown"
        }

        photosByFolder.forEach { (folderPath, photos) ->
            // Skip if it's already a user album
            if (!albums.any { it.id == folderPath }) {
                val folderName = File(folderPath).name
                val coverPhoto = photos.firstOrNull()?.path

                albums.add(
                    AlbumItem(
                        id = folderPath,
                        name = folderName,
                        type = AlbumType.USER_DEFINED,
                        photoCount = photos.size,
                        coverPhotoPath = coverPhoto
                    )
                )
            }
        }

        albums
    }
    private fun getFavoritesAlbum(): AlbumItem {
        val favPaths = database.getFavoritePaths()
        return AlbumItem(
            id = "favorites",
            name = "Favorites",
            type = AlbumType.FAVORITES,
            photoCount = favPaths.size,
            coverPhotoPath = favPaths.firstOrNull()
        )
    }

    private fun getPrivateAlbum(): AlbumItem {
        val privatePaths = database.getPrivatePaths()
        return AlbumItem(
            id = "private",
            name = "Privacy",
            type = AlbumType.PRIVATE,
            photoCount = privatePaths.size,
            coverPhotoPath = privatePaths.firstOrNull()
        )
    }

    private fun getTrashBinAlbum(): AlbumItem {
        val trashFiles = trashBinManager.getTrashFiles()
        return AlbumItem(
            id = "trash_bin",
            name = "Trash Bin",
            type = AlbumType.TRASH_BIN,
            photoCount = trashFiles.size,
            coverPhotoPath = trashFiles.firstOrNull()?.absolutePath
        )
    }

    suspend fun getAlbumPhotos(albumId: String): List<PhotoItem> = withContext(Dispatchers.IO) {
        when (albumId) {
            "favorites" -> getFavoritesPhotos()
            "private" -> getPrivatePhotos()
            "trash_bin" -> getTrashBinPhotos()
            else -> {
                // Check if it's a user album
                val userAlbumPaths = database.getAlbumPhotoPaths(albumId)
                if (userAlbumPaths.isNotEmpty()) {
                    getUserAlbumPhotos(userAlbumPaths)
                } else {
                    getFolderPhotos(albumId)
                }
            }
        }
    }
    private suspend fun getUserAlbumPhotos(photoPaths: List<String>): List<PhotoItem> {
        return photoPaths.mapNotNull { path ->
            val file = File(path)
            if (file.exists()) {
                PhotoItem(
                    file = file,
                    path = path,
                    name = file.name,
                    size = file.length(),
                    lastModified = file.lastModified(),
                    uri = android.net.Uri.fromFile(file)
                )
            } else null
        }
    }

    // Cập nhật getFavoritesPhotos
    private suspend fun getFavoritesPhotos(): List<PhotoItem> {
        val favPaths = database.getFavoritePaths()
        return getUserAlbumPhotos(favPaths)
    }

    // Cập nhật getPrivatePhotos
    private suspend fun getPrivatePhotos(): List<PhotoItem> {
        val privatePaths = database.getPrivatePaths()
        return getUserAlbumPhotos(privatePaths)
    }

    private suspend fun getTrashBinPhotos(): List<PhotoItem> {
        val trashFiles = trashBinManager.getTrashFiles()
        return trashFiles.map { file ->
            PhotoItem(
                file = file,
                path = file.absolutePath,
                name = file.name,
                size = file.length(),
                lastModified = file.lastModified(),
                uri = Uri.fromFile(file)
            )
        }
    }

    private suspend fun getFolderPhotos(folderPath: String): List<PhotoItem> {
        val allPhotos = mediaStoreHelper.getAllPhotos()
        return allPhotos.filter { photo ->
            File(photo.path).parentFile?.absolutePath == folderPath
        }
    }
}