package com.example.util

import android.content.Context

object NotificationPreferences {
    private const val PREFS_NAME = "parent_notification_prefs"
    private const val KEY_GOOD_THRESHOLD = "good_performance_threshold"
    private const val KEY_LOW_THRESHOLD = "low_performance_threshold"

    fun getGoodThreshold(context: Context): Double {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat(KEY_GOOD_THRESHOLD, 70.0f).toDouble()
    }

    fun setGoodThreshold(context: Context, value: Double) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_GOOD_THRESHOLD, value.toFloat()).apply()
    }

    fun getLowThreshold(context: Context): Double {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat(KEY_LOW_THRESHOLD, 40.0f).toDouble()
    }

    fun setLowThreshold(context: Context, value: Double) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_LOW_THRESHOLD, value.toFloat()).apply()
    }
}
