
package com.example.visoragallery.data.database
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "favorite_photos")
data class FavoritePhotoEntity(
    @PrimaryKey
    val photoPath: String,
    val addedAt: Long = System.currentTimeMillis()
)