package com.autoskip.yt

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "autoskip_preferences"
        private const val KEY_AUTO_SKIP = "pref_auto_skip"
        private const val KEY_YT_MUSIC = "pref_yt_music"
        private const val KEY_ADS_SKIPPED = "pref_ads_skipped_count"
        private const val KEY_SERVICE_START_TIME = "pref_service_start_time"
        private const val KEY_LAST_ACTIVITY_TIME = "pref_last_activity_time"
    }

    var isAutoSkipEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_SKIP, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_SKIP, value).apply()

    var isYtMusicEnabled: Boolean
        get() = prefs.getBoolean(KEY_YT_MUSIC, true)
        set(value) = prefs.edit().putBoolean(KEY_YT_MUSIC, value).apply()

    var adsSkippedCount: Int
        get() = prefs.getInt(KEY_ADS_SKIPPED, 0)
        set(value) = prefs.edit().putInt(KEY_ADS_SKIPPED, value).apply()

    fun incrementAdsSkipped() {
        val next = adsSkippedCount + 1
        prefs.edit()
            .putInt(KEY_ADS_SKIPPED, next)
            .putLong(KEY_LAST_ACTIVITY_TIME, System.currentTimeMillis())
            .apply()
    }

    var serviceStartTimestamp: Long
        get() = prefs.getLong(KEY_SERVICE_START_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_SERVICE_START_TIME, value).apply()

    var lastActivityTimestamp: Long
        get() = prefs.getLong(KEY_LAST_ACTIVITY_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_ACTIVITY_TIME, value).apply()

    fun getFormattedUptime(): String {
        val start = serviceStartTimestamp
        if (start <= 0L) return "0 м"
        val diffMs = System.currentTimeMillis() - start
        val minutes = (diffMs / (1000 * 60)) % 60
        val hours = (diffMs / (1000 * 60 * 60))
        return if (hours > 0) {
            "${hours} ч ${minutes} м"
        } else {
            "${minutes} м"
        }
    }
}
