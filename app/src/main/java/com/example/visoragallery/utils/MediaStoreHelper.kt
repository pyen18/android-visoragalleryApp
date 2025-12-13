package com.example.visoragallery.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.visoragallery.data.PhotoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MediaStoreHelper(private val context: Context) {

    suspend fun getAllPhotos(): List<PhotoItem> = withContext(Dispatchers.IO) {
        val photos = mutableListOf<PhotoItem>()

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_MODIFIED
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"

        context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val path = cursor.getString(dataColumn)
                val name = cursor.getString(nameColumn)
                val size = cursor.getLong(sizeColumn)
                val dateModified = cursor.getLong(dateColumn) * 1000 // Convert to milliseconds

                val file = File(path)
                if (file.exists()) {
                    val uriForItem = android.content.ContentUris.withAppendedId(collection, id)
                    photos.add(
                        PhotoItem(
                            uri = uriForItem,
                            file = file,
                            path = path,
                            name = name,
                            size = size,
                            lastModified = dateModified
                        )
                    )
                }
            }
        }

        photos
    }

    fun scanMedia(path: String) {
        val file = File(path)
        val uri = Uri.fromFile(file)
        val scanFileIntent = android.content.Intent(
            android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
            uri
        )
        context.sendBroadcast(scanFileIntent)
    }
}