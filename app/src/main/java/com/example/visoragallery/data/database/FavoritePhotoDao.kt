
package com.example.visoragallery.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritePhotoDao {

    @Query("SELECT * FROM favorite_photos ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoritePhotoEntity>>

    @Query("SELECT * FROM favorite_photos WHERE photoPath = :path LIMIT 1")
    suspend fun getFavorite(path: String): FavoritePhotoEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_photos WHERE photoPath = :path)")
    suspend fun isFavorite(path: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(photo: FavoritePhotoEntity)

    @Delete
    suspend fun removeFavorite(photo: FavoritePhotoEntity)

    @Query("DELETE FROM favorite_photos WHERE photoPath = :path")
    suspend fun removeFavoriteByPath(path: String)

    @Query("SELECT COUNT(*) FROM favorite_photos")
    suspend fun getFavoritesCount(): Int
}
