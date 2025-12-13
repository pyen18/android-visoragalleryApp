package com.example.visoragallery.repository

import android.content.Context
import com.example.visoragallery.data.PhotoItem
import com.example.visoragallery.utils.MediaStoreHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PhotoRepository(private val context: Context) {

    private val mediaStoreHelper = MediaStoreHelper(context)

    fun getAllPhotos(): Flow<List<PhotoItem>> = flow {
        val photos = mediaStoreHelper.getAllPhotos()
        emit(photos)
    }

    suspend fun refreshPhotos(): List<PhotoItem> {
        return mediaStoreHelper.getAllPhotos()
    }

    fun scanMedia(path: String) {
        mediaStoreHelper.scanMedia(path)
    }
}