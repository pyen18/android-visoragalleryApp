package com.example.visoragallery.data

import android.net.Uri
import java.io.File

data class PhotoItem(
    val uri: Uri,
    val file: File? = null,
    val path: String = file?.absolutePath.orEmpty(),
    val name: String = file?.name.orEmpty(),
    val size: Long = file?.length() ?: 0L,
    val lastModified: Long = file?.lastModified() ?: 0L,
    val isSelected: Boolean = false
)


data class AlbumItem(
    val id: String,
    val name: String,
    val type: AlbumType,
    val photoCount: Int = 0,
    val coverPhotoPath: String? = null
)

enum class AlbumType {
    FAVORITES,
    PRIVATE,
    TRASH_BIN,
    USER_DEFINED
}

sealed class NavigationItem(val route: String, val title: String) {
    object Photos : NavigationItem("photos", "Photos")
    object Albums : NavigationItem("albums", "Albums")
    object Search : NavigationItem("search", "Search")
    object More : NavigationItem("more", "More")
}