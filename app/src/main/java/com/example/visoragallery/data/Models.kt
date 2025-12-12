package com.example.visoragallery.data

import java.io.File

data class PhotoItem(
    val file: File,
    val path: String = file.absolutePath,
    val name: String = file.name,
    val size: Long = file.length(),
    val lastModified: Long = file.lastModified(),
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