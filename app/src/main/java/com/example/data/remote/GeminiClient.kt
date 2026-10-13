package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return !key.isNullOrBlank() && key != "MY_GEMINI_API_KEY"
    }

    suspend fun generateContent(
        systemInstruction: String,
        conversationHistory: List<Pair<String, String>>, // role ("user" or "model"), content
        userMessage: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!isApiKeyConfigured()) {
            return@withContext Result.failure(IllegalStateException("Gemini API Key is not configured."))
        }

        try {
            val rootJson = JSONObject()

            // System Instruction
            if (systemInstruction.isNotBlank()) {
                val sysPart = JSONObject().put("text", systemInstruction)
                val sysParts = JSONArray().put(sysPart)
                rootJson.put("systemInstruction", JSONObject().put("parts", sysParts))
            }

            // Contents array
            val contentsArray = JSONArray()

            // Historical turns (up to 10 context messages)
            for ((role, text) in conversationHistory) {
                val partObj = JSONObject().put("text", text)
                val partsArr = JSONArray().put(partObj)
                val contentObj = JSONObject().put("role", role).put("parts", partsArr)
                contentsArray.put(contentObj)
            }

            // Current message
            val currentPartObj = JSONObject().put("text", userMessage)
            val currentPartsArr = JSONArray().put(currentPartObj)
            val currentContentObj = JSONObject().put("role", "user").put("parts", currentPartsArr)
            contentsArray.put(currentContentObj)

            rootJson.put("contents", contentsArray)

            // Generation config
            val genConfig = JSONObject()
                .put("temperature", 0.3)
                .put("topP", 0.95)
            rootJson.put("generationConfig", genConfig)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = rootJson.toString().toRequestBody(mediaType)

            val url = "$BASE_URL?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API error: ${response.code} $responseString")
                return@withContext Result.failure(
                    Exception("Gemini HTTP Error ${response.code}: ${response.message}")
                )
            }

            val responseJson = JSONObject(responseString)
            val candidates = responseJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val text = parts.getJSONObject(0).optString("text")
                    return@withContext Result.success(text)
                }
            }

            Result.failure(Exception("Empty candidate response from Gemini"))
        } catch (e: Exception) {
            Log.e(TAG, "Gemini call failed", e)
            Result.failure(e)
        }
    }
}
