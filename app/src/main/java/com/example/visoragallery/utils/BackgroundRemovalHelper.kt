// app/src/main/java/com/example/visoragallery/utils/BackgroundRemovalHelper.kt
package com.example.visoragallery.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class BackgroundRemovalHelper(private val context: Context) {

    companion object {
        private const val TAG = "BackgroundRemoval"
        private const val API_KEY = "YOUR_REMOVE_BG_API_KEY" // Get free key from remove.bg
        private const val API_URL = "https://api.remove.bg/v1.0/removebg"
    }

    private val client = OkHttpClient()

    /**
     * Remove background from image using remove.bg API
     * @param inputFile Input image file
     * @param onProgress Progress callback (0.0 to 1.0)
     * @return Output file with background removed, or null if failed
     */
    suspend fun removeBackground(
        inputFile: File,
        onProgress: (Float) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            onProgress(0.1f)

            // Prepare request
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "image_file",
                    inputFile.name,
                    inputFile.asRequestBody("image/*".toMediaType())
                )
                .addFormDataPart("size", "auto")
                .build()

            onProgress(0.3f)

            val request = Request.Builder()
                .url(API_URL)
                .addHeader("X-Api-Key", API_KEY)
                .post(requestBody)
                .build()

            onProgress(0.5f)

            // Execute request
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.e(TAG, "API Error: ${response.code} - ${response.message}")
                return@withContext null
            }

            onProgress(0.7f)

            // Save result
            val outputFile = createOutputFile(inputFile)
            response.body?.byteStream()?.use { input ->
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                }
            }

            onProgress(1.0f)

            // Scan media
            MediaStoreHelper(context).scanMedia(outputFile.absolutePath)

            Log.d(TAG, "Background removed successfully: ${outputFile.absolutePath}")
            outputFile

        } catch (e: IOException) {
            Log.e(TAG, "Network error", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error removing background", e)
            null
        }
    }

    /**
     * Alternative: Remove background using local processing (Less accurate but free)
     * This is a simple implementation, for production use ML Kit or TensorFlow Lite
     */
    suspend fun removeBackgroundLocal(
        inputFile: File,
        onProgress: (Float) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            onProgress(0.1f)

            // Load bitmap
            val bitmap = BitmapFactory.decodeFile(inputFile.absolutePath)
            onProgress(0.3f)

            // Simple background removal (This is very basic, consider using ML Kit)
            val processedBitmap = processImageSimple(bitmap)
            onProgress(0.7f)

            // Save result
            val outputFile = createOutputFile(inputFile)
            FileOutputStream(outputFile).use { out ->
                processedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            bitmap.recycle()
            processedBitmap.recycle()

            onProgress(1.0f)

            MediaStoreHelper(context).scanMedia(outputFile.absolutePath)

            Log.d(TAG, "Background removed locally: ${outputFile.absolutePath}")
            outputFile

        } catch (e: Exception) {
            Log.e(TAG, "Error removing background locally", e)
            null
        }
    }

    private fun processImageSimple(bitmap: Bitmap): Bitmap {
        // This is a VERY basic implementation
        // For production, use ML Kit Subject Segmentation or similar
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // Simple edge detection and background removal
        // This is placeholder code - replace with proper ML model
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val alpha = (pixel shr 24) and 0xff
            val red = (pixel shr 16) and 0xff
            val green = (pixel shr 8) and 0xff
            val blue = pixel and 0xff

            // Very basic: Remove white/light backgrounds
            if (red > 200 && green > 200 && blue > 200) {
                pixels[i] = 0x00000000 // Transparent
            }
        }

        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }

    private fun createOutputFile(inputFile: File): File {
        val timestamp = System.currentTimeMillis()
        val fileName = "no_bg_${timestamp}.png"

        val picturesDir = android.os.Environment.getExternalStoragePublicDirectory(
            android.os.Environment.DIRECTORY_PICTURES
        )
        val outputDir = File(picturesDir, "VisoraGallery")

        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        return File(outputDir, fileName)
    }
}