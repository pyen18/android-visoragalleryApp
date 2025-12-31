// Save as: app/src/main/java/com/example/visoragallery/ui/screens/favorites/FavoritesManager.kt
package com.example.visoragallery.ui.screens.favorites

import android.content.Context
import android.util.Log
import com.example.visoragallery.data.PhotoItem
import com.example.visoragallery.data.database.AppDatabase
import com.example.visoragallery.data.database.FavoritePhotoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File

class FavoritesManager private constructor(context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val favoriteDao = database.favoritePhotoDao()

    companion object {
        private const val TAG = "FavoritesManager"

        @Volatile
        private var INSTANCE: FavoritesManager? = null

        fun getInstance(context: Context): FavoritesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FavoritesManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    /**
     * Get all favorite photos as Flow
     * Automatically ignores missing files
     */
    fun getFavoritePhotos(): Flow<List<PhotoItem>> {
        return favoriteDao.getAllFavorites().map { entities ->
            entities.mapNotNull { entity ->
                val file = File(entity.photoPath)
                if (file.exists()) {
                    PhotoItem(
                        uri = android.net.Uri.fromFile(file),
                        file = file,
                        path = entity.photoPath,
                        name = file.name,
                        size = file.length(),
                        lastModified = file.lastModified()
                    )
                } else {
                    Log.w(TAG, "File doesn't exist, skipping: ${entity.photoPath}")
                    null
                }
            }
        }
    }

    /**
     * Check if photo is favorite
     */
    suspend fun isFavorite(photoPath: String): Boolean {
        return try {
            favoriteDao.isFavorite(photoPath)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking favorite", e)
            false
        }
    }

    /**
     * Add photo to favorites
     */
    suspend fun addToFavorites(photoPath: String): Boolean {
        return try {
            val file = File(photoPath)
            if (!file.exists()) {
                Log.w(TAG, "File does not exist: $photoPath")
                return false
            }

            favoriteDao.addFavorite(
                FavoritePhotoEntity(
                    photoPath = photoPath,
                    addedAt = System.currentTimeMillis()
                )
            )

            Log.d(TAG, "Added to favorites: $photoPath")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding favorite", e)
            false
        }
    }

    /**
     * Remove photo from favorites
     */
    suspend fun removeFromFavorites(photoPath: String): Boolean {
        return try {
            favoriteDao.removeFavoriteByPath(photoPath)
            Log.d(TAG, "Removed from favorites: $photoPath")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error removing favorite", e)
            false
        }
    }

    /**
     * Toggle favorite state
     */
    suspend fun toggleFavorite(photoPath: String): Boolean {
        return if (isFavorite(photoPath)) {
            removeFromFavorites(photoPath)
            false
        } else {
            addToFavorites(photoPath)
            true
        }
    }

    /**
     * Get total favorite count
     */
    suspend fun getFavoritesCount(): Int {
        return try {
            favoriteDao.getFavoritesCount()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting favorites count", e)
            0
        }
    }

    /**
     * Remove multiple favorites
     */
    suspend fun removeMultipleFromFavorites(photoPaths: List<String>): Int {
        var successCount = 0
        photoPaths.forEach { path ->
            if (removeFromFavorites(path)) {
                successCount++
            }
        }
        Log.d(TAG, "Removed $successCount / ${photoPaths.size} favorites")
        return successCount
    }

    /**
     * Add multiple favorites
     */
    suspend fun addMultipleToFavorites(photoPaths: List<String>): Int {
        var successCount = 0
        photoPaths.forEach { path ->
            if (addToFavorites(path)) {
                successCount++
            }
        }
        Log.d(TAG, "Added $successCount / ${photoPaths.size} favorites")
        return successCount
    }

    /**
     * Clear all favorites
     */
    suspend fun clearAllFavorites(): Boolean {
        return try {
            val entities = favoriteDao.getAllFavorites().first()
            entities.forEach { entity ->
                favoriteDao.removeFavorite(entity)
            }
            Log.d(TAG, "Cleared all favorites (${entities.size})")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing favorites", e)
            false
        }
    }

    /**
     * Get all favorite paths
     */
    suspend fun getAllFavoritePaths(): List<String> {
        return try {
            val paths = favoriteDao
                .getAllFavorites()
                .first()
                .map { it.photoPath }

            Log.d(TAG, "Retrieved ${paths.size} favorite paths")
            paths
        } catch (e: Exception) {
            Log.e(TAG, "Error getting favorite paths", e)
            emptyList()
        }
    }
}
