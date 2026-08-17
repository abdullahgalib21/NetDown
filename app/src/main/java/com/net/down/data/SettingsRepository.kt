package com.net.down.data

import android.content.Context
import android.content.SharedPreferences

object SettingsRepository {
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences("netdown_settings", Context.MODE_PRIVATE)
    }

    var maxConcurrent: Int
        get() = prefs.getInt("max_concurrent", 1)
        set(value) {
            prefs.edit().putInt("max_concurrent", value.coerceIn(1, 3)).apply()
        }

    var defaultVideoFormat: String
        get() = prefs.getString("video_format", "bestvideo+bestaudio/best") ?: "bestvideo+bestaudio/best"
        set(value) {
            prefs.edit().putString("video_format", value).apply()
        }

    val isInitialized: Boolean
        get() = ::prefs.isInitialized
}
