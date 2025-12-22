package com.example.visoragallery.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

object TestImageGenerator {

    private const val TAG = "TestImageGenerator"

    /**
     * Generate test images and save to gallery
     * @param context Application context
     * @param count Number of images to generate (default 20)
     */
    suspend fun generateTestImages(context: Context, count: Int = 20) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Generating $count test images...")

            repeat(count) { index ->
                val bitmap = createColoredBitmap(index)
                val success = saveImageToGallery(context, bitmap, index)

                if (success) {
                    Log.d(TAG, "Generated image ${index + 1}/$count")
                } else {
                    Log.e(TAG, "Failed to generate image ${index + 1}")
                }

                bitmap.recycle() // Free memory
            }

            Log.d(TAG, "Finished generating $count test images")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error generating test images", e)
            false
        }
    }

    /**
     * Create a colored bitmap with text
     */
    private fun createColoredBitmap(index: Int): Bitmap {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background colors
        val colors = listOf(
            Color.parseColor("#FF6B6B"), // Red
            Color.parseColor("#4ECDC4"), // Teal
            Color.parseColor("#45B7D1"), // Blue
            Color.parseColor("#FFA07A"), // Salmon
            Color.parseColor("#98D8C8"), // Mint
            Color.parseColor("#F7DC6F"), // Yellow
            Color.parseColor("#BB8FCE"), // Purple
            Color.parseColor("#85C1E2"), // Sky Blue
            Color.parseColor("#F8B88B"), // Peach
            Color.parseColor("#A3E4D7")  // Aqua
        )

        // Draw gradient background
        val color1 = colors[index % colors.size]
        val color2 = colors[(index + 1) % colors.size]

        val paint = Paint().apply {
            style = Paint.Style.FILL
        }

        // Simple gradient effect
        canvas.drawColor(color1)

        // Draw diagonal gradient
        for (i in 0 until height step 20) {
            paint.alpha = (255 * (1 - i.toFloat() / height)).toInt()
            paint.color = color2
            canvas.drawRect(0f, i.toFloat(), width.toFloat(), (i + 20).toFloat(), paint)
        }

        // Draw text - Image number
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 180f
            textAlign = Paint.Align.CENTER
            style = Paint.Style.FILL
            isAntiAlias = true
            setShadowLayer(10f, 0f, 0f, Color.BLACK)
        }

        canvas.drawText(
            "#${index + 1}",
            width / 2f,
            height / 2f,
            textPaint
        )

        // Draw subtitle
        val subtitlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 80f
            textAlign = Paint.Align.CENTER
            style = Paint.Style.FILL
            isAntiAlias = true
            setShadowLayer(8f, 0f, 0f, Color.BLACK)
        }

        canvas.drawText(
            "Test Image",
            width / 2f,
            height / 2f + 150f,
            subtitlePaint
        )

        // Draw timestamp
        val timePaint = Paint().apply {
            color = Color.WHITE
            textSize = 40f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            alpha = 200
        }

        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        canvas.drawText(
            timeFormat.format(Date()),
            width / 2f,
            height - 100f,
            timePaint
        )

        return bitmap
    }

    /**
     * Save bitmap to gallery
     */
    private fun saveImageToGallery(context: Context, bitmap: Bitmap, index: Int): Boolean {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(Date())
            val fileName = "VISORAGALLERY_TEST_${timeStamp}_${index + 1}.jpg"

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                put(MediaStore.Images.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/VisoraGallery")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )

            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(it, contentValues, null, null)
                }

                true
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error saving image", e)
            false
        }
    }
}