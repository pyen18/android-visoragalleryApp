// app/src/main/java/com/example/visoragallery/utils/GeminiStoryGenerator.kt
package com.example.visoragallery.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.example.visoragallery.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.TimeUnit

class GeminiStoryGenerator(private val context: Context) {

    companion object {
        private const val TAG = "GeminiStory"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        private const val MODEL = "gemini-1.5-flash"
        private const val API_KEY = "AIzaSyCTAjbWaxcijz9hUeylxL3PajKW3hdlZOk"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Tạo story cho ảnh
     */
    suspend fun generateStory(
        photoFile: File,
        storyType: StoryType = StoryType.CREATIVE,
        language: Language = Language.VIETNAMESE,
        onProgress: (Float) -> Unit
    ): StoryResult? = withContext(Dispatchers.IO) {
        try {
            if (API_KEY.isEmpty()) {
                Log.e(TAG, "API key not configured")
                return@withContext null
            }

            onProgress(0.1f)

            // Đọc và encode ảnh
            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath) ?: run {
                Log.e(TAG, "Failed to decode image")
                return@withContext null
            }

            onProgress(0.2f)

            val base64Image = bitmapToBase64(bitmap)
            bitmap.recycle()

            onProgress(0.4f)

            // Tạo prompt
            val prompt = createPrompt(storyType, language)

            onProgress(0.5f)

            // Gọi API
            val requestBody = createGeminiRequest(base64Image, prompt)
            val url = "$BASE_URL/$MODEL:generateContent?key=$API_KEY"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()

            onProgress(0.7f)

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                Log.e(TAG, "API Error: ${response.code} - $errorBody")
                return@withContext null
            }

            onProgress(0.9f)

            // Parse response
            val responseBody = response.body?.string() ?: run {
                Log.e(TAG, "Empty response body")
                return@withContext null
            }

            val storyText = parseGeminiResponse(responseBody)

            if (storyText == null) {
                Log.e(TAG, "Failed to parse response: $responseBody")
                return@withContext null
            }

            onProgress(1.0f)

            Log.d(TAG, "Story generated successfully: ${storyText.take(50)}...")

            StoryResult(
                story = storyText.trim(),
                type = storyType,
                language = language,
                timestamp = System.currentTimeMillis()
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error generating story", e)
            null
        }
    }
    suspend fun debugAvailableModels() {
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models?key=$API_KEY") // URL lấy danh sách model
            .get()
            .build()

        try {
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            Log.d("GeminiDebug", "Status Code: ${response.code}")
            Log.d("GeminiDebug", "Available Models: $body")
        } catch (e: Exception) {
            Log.e("GeminiDebug", "Check failed", e)
        }
    }
    /**
     * Tạo batch stories (nhiều ảnh)
     */
    suspend fun generateBatchStories(
        photoFiles: List<File>,
        storyType: StoryType = StoryType.CREATIVE,
        language: Language = Language.VIETNAMESE,
        onProgress: (current: Int, total: Int) -> Unit
    ): List<Pair<File, StoryResult>> = withContext(Dispatchers.IO) {
        val results = mutableListOf<Pair<File, StoryResult>>()

        photoFiles.forEachIndexed { index, file ->
            onProgress(index + 1, photoFiles.size)

            try {
                val story = generateStory(file, storyType, language) { }
                if (story != null) {
                    results.add(file to story)
                }

                // Rate limiting: 60 requests/minute
                if (index < photoFiles.size - 1) {
                    kotlinx.coroutines.delay(1100)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating story for ${file.name}", e)
            }
        }

        results
    }

    /**
     * Kiểm tra API key có hợp lệ không
     */
    suspend fun validateApiKey(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (API_KEY.isEmpty() || API_KEY == "YOUR_API_KEY_HERE") {
                return@withContext false
            }

            val url = "$BASE_URL/$MODEL:generateContent?key=$API_KEY"
            val testRequest = createSimpleTestRequest()

            val request = Request.Builder()
                .url(url)
                .post(testRequest)
                .addHeader("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful

        } catch (e: Exception) {
            Log.e(TAG, "API key validation failed", e)
            false
        }
    }

    // =========================
    // PRIVATE HELPERS
    // =========================

    private fun createPrompt(storyType: StoryType, language: Language): String {
        val lang = when (language) {
            Language.VIETNAMESE -> "Tiếng Việt"
            Language.ENGLISH -> "English"
        }

        return when (storyType) {
            StoryType.CREATIVE -> """
                Analyze this image and create a creative, engaging short story.
                
                Requirements:
                - Length: 3-5 sentences
                - Language: $lang
                - Style: Creative, vivid, imaginative
                - Focus on emotions and details in the image
                - Make it engaging and memorable
                
                Return only the story, no explanations or meta-commentary.
            """.trimIndent()

            StoryType.POETIC -> """
                Analyze this image and compose a beautiful poem about it.
                
                Requirements:
                - Length: 4-6 lines of poetry
                - Language: $lang
                - Style: Lyrical, beautiful, emotional
                - Use poetic devices (metaphor, imagery)
                - Light rhyme if possible
                
                Return only the poem, no explanations.
            """.trimIndent()

            StoryType.FUNNY -> """
                Analyze this image and create a humorous, witty caption.
                
                Requirements:
                - Length: 1-2 sentences
                - Language: $lang
                - Style: Funny, clever, playful
                - Can be slightly sarcastic or punny
                - Include relevant emoji
                
                Return only the caption, no explanations.
            """.trimIndent()

            StoryType.ROMANTIC -> """
                Analyze this image and create a romantic, sweet story.
                
                Requirements:
                - Length: 3-4 sentences
                - Language: $lang
                - Style: Romantic, heartwarming, sweet
                - Focus on emotions and tender moments
                - Make it touching
                
                Return only the story, no explanations.
            """.trimIndent()

            StoryType.MYSTERIOUS -> """
                Analyze this image and create a mysterious, intriguing narrative.
                
                Requirements:
                - Length: 3-5 sentences
                - Language: $lang
                - Style: Mysterious, suspenseful, enigmatic
                - Leave some questions unanswered
                - Create intrigue
                
                Return only the story, no explanations.
            """.trimIndent()
        }
    }

    private fun createGeminiRequest(base64Image: String, prompt: String): RequestBody {
        val json = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                        put(JSONObject().apply {
                            put("inline_data", JSONObject().apply {
                                put("mime_type", "image/jpeg")
                                put("data", base64Image)
                            })
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.9)
                put("topK", 40)
                put("topP", 0.95)
                put("maxOutputTokens", 500)
            })
            put("safetySettings", JSONArray().apply {
                put(JSONObject().apply {
                    put("category", "HARM_CATEGORY_HARASSMENT")
                    put("threshold", "BLOCK_MEDIUM_AND_ABOVE")
                })
                put(JSONObject().apply {
                    put("category", "HARM_CATEGORY_HATE_SPEECH")
                    put("threshold", "BLOCK_MEDIUM_AND_ABOVE")
                })
                put(JSONObject().apply {
                    put("category", "HARM_CATEGORY_SEXUALLY_EXPLICIT")
                    put("threshold", "BLOCK_MEDIUM_AND_ABOVE")
                })
                put(JSONObject().apply {
                    put("category", "HARM_CATEGORY_DANGEROUS_CONTENT")
                    put("threshold", "BLOCK_MEDIUM_AND_ABOVE")
                })
            })
        }

        return json.toString().toRequestBody("application/json".toMediaType())
    }

    private fun createSimpleTestRequest(): RequestBody {
        val json = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "Hello")
                        })
                    })
                })
            })
        }

        return json.toString().toRequestBody("application/json".toMediaType())
    }

    private fun parseGeminiResponse(responseBody: String): String? {
        return try {
            val json = JSONObject(responseBody)

            // Check for errors
            if (json.has("error")) {
                val error = json.getJSONObject("error")
                Log.e(TAG, "API returned error: ${error.getString("message")}")
                return null
            }

            val candidates = json.getJSONArray("candidates")
            if (candidates.length() == 0) {
                Log.e(TAG, "No candidates in response")
                return null
            }

            val content = candidates.getJSONObject(0).getJSONObject("content")
            val parts = content.getJSONArray("parts")
            if (parts.length() == 0) {
                Log.e(TAG, "No parts in content")
                return null
            }

            parts.getJSONObject(0).getString("text")

        } catch (e: Exception) {
            Log.e(TAG, "Parse error", e)
            null
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()

        // Resize để tiết kiệm bandwidth và token
        val maxDimension = 1024
        val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val scale = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        } else {
            bitmap
        }

        // Compress JPEG 85% quality
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val byteArray = outputStream.toByteArray()

        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle()
        }

        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}

// =========================
// DATA CLASSES & ENUMS
// =========================

enum class StoryType(val displayName: String, val emoji: String) {
    CREATIVE("Creative Story", "✨"),
    POETIC("Poetic", "📜"),
    FUNNY("Funny", "😂"),
    ROMANTIC("Romantic", "💕"),
    MYSTERIOUS("Mysterious", "🔮")
}

enum class Language(val displayName: String) {
    VIETNAMESE("Tiếng Việt"),
    ENGLISH("English")
}

data class StoryResult(
    val story: String,
    val type: StoryType,
    val language: Language,
    val timestamp: Long
)