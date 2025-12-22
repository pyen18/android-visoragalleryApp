package com.example.visoragallery.navigation

object NavRoutes {
    const val PHOTOS = "photos"
    const val ALBUMS = "albums"
    const val SEARCH = "search"
    const val MORE = "more"
    const val SINGLE_PHOTO = "single_photo"
    const val SETTINGS = "settings"
    const val TRASH_BIN = "trash_bin"
    const val SINGLE_ALBUM = "single_album/{albumId}"
}

// Helper to create single photo route with arguments
fun createSinglePhotoRoute(photoIndex: Int): String {
    return "${NavRoutes.SINGLE_PHOTO}/$photoIndex"
}
