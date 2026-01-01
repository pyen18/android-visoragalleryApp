// app/src/main/java/com/example/visoragallery/data/database/AppDatabase.kt
package com.example.visoragallery.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        FavoritePhotoEntity::class,
        PhotoStoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun favoritePhotoDao(): FavoritePhotoDao
    abstract fun photoStoryDao(): PhotoStoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "visora_gallery_database"
                )
                    .fallbackToDestructiveMigration()  // Auto migration
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}