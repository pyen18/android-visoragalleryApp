// app/src/main/java/com/example/visoragallery/data/database/PhotoStoryEntity.kt
package com.example.visoragallery.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "photo_stories")
data class PhotoStoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "photo_path")
    val photoPath: String,

    @ColumnInfo(name = "story")
    val story: String,

    @ColumnInfo(name = "type")
    val type: String, // CREATIVE, POETIC, FUNNY, ROMANTIC, MYSTERIOUS

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false
)

@Dao
interface PhotoStoryDao {

    @Query("SELECT * FROM photo_stories ORDER BY created_at DESC")
    fun getAllStories(): Flow<List<PhotoStoryEntity>>

    @Query("SELECT * FROM photo_stories WHERE photo_path = :photoPath ORDER BY created_at DESC")
    fun getStoriesForPhoto(photoPath: String): Flow<List<PhotoStoryEntity>>

    @Query("SELECT * FROM photo_stories WHERE is_favorite = 1 ORDER BY created_at DESC")
    fun getFavoriteStories(): Flow<List<PhotoStoryEntity>>

    @Query("SELECT * FROM photo_stories WHERE type = :type ORDER BY created_at DESC")
    fun getStoriesByType(type: String): Flow<List<PhotoStoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: PhotoStoryEntity): Long

    @Delete
    suspend fun deleteStory(story: PhotoStoryEntity)

    @Query("DELETE FROM photo_stories WHERE photo_path = :photoPath")
    suspend fun deleteStoriesForPhoto(photoPath: String)

    @Query("UPDATE photo_stories SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM photo_stories")
    suspend fun getStoriesCount(): Int

    @Query("SELECT COUNT(*) FROM photo_stories WHERE photo_path = :photoPath")
    suspend fun getStoryCountForPhoto(photoPath: String): Int
}