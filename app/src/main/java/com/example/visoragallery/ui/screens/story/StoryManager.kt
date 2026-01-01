// app/src/main/java/com/example/visoragallery/ui/screens/story/StoryManager.kt
package com.example.visoragallery.ui.screens.story

import android.content.Context
import android.util.Log
import com.example.visoragallery.data.database.AppDatabase
import com.example.visoragallery.data.database.PhotoStoryEntity
import com.example.visoragallery.utils.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

class StoryManager private constructor(context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val storyDao = database.photoStoryDao()
    private val geminiGenerator = GeminiStoryGenerator(context)

    companion object {
        private const val TAG = "StoryManager"

        @Volatile
        private var INSTANCE: StoryManager? = null

        fun getInstance(context: Context): StoryManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StoryManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    /**
     * Tạo story mới cho ảnh
     */
    suspend fun generateStory(
        photoFile: File,
        storyType: StoryType = StoryType.CREATIVE,
        language: Language = Language.VIETNAMESE,
        onProgress: (Float) -> Unit
    ): PhotoStoryEntity? {
        return try {
            val result = geminiGenerator.generateStory(
                photoFile,
                storyType,
                language,
                onProgress
            )

            if (result != null) {
                val entity = PhotoStoryEntity(
                    photoPath = photoFile.absolutePath,
                    story = result.story,
                    type = result.type.name,
                    createdAt = result.timestamp
                )

                val id = storyDao.insertStory(entity)
                entity.copy(id = id)
            } else {
                null
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error generating story", e)
            null
        }
    }

    /**
     * Tạo nhiều stories cho nhiều ảnh
     */
    suspend fun generateBatchStories(
        photoFiles: List<File>,
        storyType: StoryType = StoryType.CREATIVE,
        language: Language = Language.VIETNAMESE,
        onProgress: (current: Int, total: Int) -> Unit
    ): List<PhotoStoryEntity> {
        val results = mutableListOf<PhotoStoryEntity>()

        val geminiResults = geminiGenerator.generateBatchStories(
            photoFiles,
            storyType,
            language,
            onProgress
        )

        geminiResults.forEach { (file, storyResult) ->
            try {
                val entity = PhotoStoryEntity(
                    photoPath = file.absolutePath,
                    story = storyResult.story,
                    type = storyResult.type.name,
                    createdAt = storyResult.timestamp
                )

                val id = storyDao.insertStory(entity)
                results.add(entity.copy(id = id))

            } catch (e: Exception) {
                Log.e(TAG, "Error saving story for ${file.name}", e)
            }
        }

        return results
    }

    /**
     * Lấy tất cả stories
     */
    fun getAllStories(): Flow<List<PhotoStoryEntity>> {
        return storyDao.getAllStories()
    }

    /**
     * Lấy stories cho một ảnh
     */
    fun getStoriesForPhoto(photoPath: String): Flow<List<PhotoStoryEntity>> {
        return storyDao.getStoriesForPhoto(photoPath)
    }

    /**
     * Lấy favorite stories
     */
    fun getFavoriteStories(): Flow<List<PhotoStoryEntity>> {
        return storyDao.getFavoriteStories()
    }

    /**
     * Lấy stories theo type
     */
    fun getStoriesByType(type: StoryType): Flow<List<PhotoStoryEntity>> {
        return storyDao.getStoriesByType(type.name)
    }

    /**
     * Xóa story
     */
    suspend fun deleteStory(story: PhotoStoryEntity) {
        storyDao.deleteStory(story)
    }

    /**
     * Toggle favorite
     */
    suspend fun toggleFavorite(story: PhotoStoryEntity) {
        storyDao.updateFavoriteStatus(story.id, !story.isFavorite)
    }

    /**
     * Lấy số lượng stories
     */
    suspend fun getStoriesCount(): Int {
        return storyDao.getStoriesCount()
    }

    /**
     * Kiểm tra ảnh đã có story chưa
     */
    suspend fun hasStory(photoPath: String): Boolean {
        return storyDao.getStoryCountForPhoto(photoPath) > 0
    }

    /**
     * Validate API key
     */
    suspend fun validateApiKey(): Boolean {
        return geminiGenerator.validateApiKey()
    }

    /**
     * Get statistics
     */
    fun getStatistics(): Flow<StoryStatistics> {
        return storyDao.getAllStories().map { stories ->
            StoryStatistics(
                totalStories = stories.size,
                favoriteCount = stories.count { it.isFavorite },
                typeBreakdown = stories.groupBy { it.type }
                    .mapValues { it.value.size },
                mostUsedType = stories.groupBy { it.type }
                    .maxByOrNull { it.value.size }?.key ?: "CREATIVE"
            )
        }
    }
}

data class StoryStatistics(
    val totalStories: Int,
    val favoriteCount: Int,
    val typeBreakdown: Map<String, Int>,
    val mostUsedType: String
)