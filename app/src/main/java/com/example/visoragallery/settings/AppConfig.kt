package com.example.visoragallery.settings

import android.content.Context
import android.content.SharedPreferences

class AppConfig private constructor(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_config", Context.MODE_PRIVATE)

    companion object {
        private const val NIGHT_MODE = "night_mode"
        private const val TRASH_MODE = "trash_mode"
        private const val TIME_LAPSE = "time_lapse"

        @Volatile
        private var instance: AppConfig? = null

        fun getInstance(context: Context): AppConfig {
            return instance ?: synchronized(this) {
                instance ?: AppConfig(context.applicationContext).also { instance = it }
            }
        }
    }

    fun getNightMode(): Boolean = sharedPreferences.getBoolean(NIGHT_MODE, false)

    fun getTrashMode(): Boolean = sharedPreferences.getBoolean(TRASH_MODE, true)

    fun getTimeLapse(): String = sharedPreferences.getString(TIME_LAPSE, "1 seconds") ?: "1 seconds"

    fun setNightMode(value: Boolean) {
        if (getNightMode() != value) {
            sharedPreferences.edit().putBoolean(NIGHT_MODE, value).apply()
        }
    }

    fun setTrashMode(value: Boolean) {
        if (getTrashMode() != value) {
            sharedPreferences.edit().putBoolean(TRASH_MODE, value).apply()
        }
    }

    fun setTimeLapse(value: String) {
        if (getTimeLapse() != value) {
            sharedPreferences.edit().putString(TIME_LAPSE, value).apply()
        }
    }
}