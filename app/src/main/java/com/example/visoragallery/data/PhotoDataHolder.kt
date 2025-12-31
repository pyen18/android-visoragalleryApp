package com.example.visoragallery.data

import android.util.Log

object PhotoDataHolder {
    private const val TAG = "PhotoDataHolder"
    private var photoPaths: Array<String>? = null

    fun setPhotoPaths(paths: Array<String>) {
        photoPaths = paths
        Log.d(TAG, "Saved ${paths.size} photo paths")
    }

    fun getPhotoPaths(): Array<String>? {
        Log.d(TAG, "Retrieved ${photoPaths?.size ?: 0} photo paths")
        return photoPaths
    }

    fun clear() {
        Log.d(TAG, "Cleared photo paths")
        photoPaths = null
    }
}