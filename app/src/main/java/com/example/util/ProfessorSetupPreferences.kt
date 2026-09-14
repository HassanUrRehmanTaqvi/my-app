package com.example.util

import android.content.Context
import android.content.SharedPreferences

object ProfessorSetupPreferences {
    private const val PREFS_NAME = "professor_setup_prefs"
    private const val KEY_SETUP_COMPLETED = "setup_completed"
    private const val KEY_TEACHER_ID = "selected_teacher_id"
    private const val KEY_TEACHER_NAME = "selected_teacher_name"
    private const val KEY_CLASS_ID = "selected_class_id"
    private const val KEY_CLASS_NAME = "selected_class_name"
    private const val KEY_CLASS_LEVEL = "selected_class_level"
    private const val KEY_GROUP_NAME = "selected_group_name"
    private const val KEY_SELECTION_MODE = "student_selection_mode" // "ALL", "EVEN", "ODD", "CUSTOM"
    private const val KEY_STUDENT_COUNT = "selected_student_count"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isSetupCompleted(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SETUP_COMPLETED, false)
    }

    fun setSetupCompleted(context: Context, completed: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SETUP_COMPLETED, completed).apply()
    }

    fun getTeacherId(context: Context): String? {
        return getPrefs(context).getString(KEY_TEACHER_ID, null)
    }

    fun setTeacherId(context: Context, id: String) {
        getPrefs(context).edit().putString(KEY_TEACHER_ID, id).apply()
    }

    fun getTeacherName(context: Context): String? {
        return getPrefs(context).getString(KEY_TEACHER_NAME, null)
    }

    fun setTeacherName(context: Context, name: String) {
        getPrefs(context).edit().putString(KEY_TEACHER_NAME, name).apply()
    }

    fun getClassId(context: Context): String? {
        return getPrefs(context).getString(KEY_CLASS_ID, null)
    }

    fun setClassId(context: Context, classId: String) {
        getPrefs(context).edit().putString(KEY_CLASS_ID, classId).apply()
    }

    fun getClassName(context: Context): String {
        return getPrefs(context).getString(KEY_CLASS_NAME, "فرسٹ ایئر") ?: "فرسٹ ایئر"
    }

    fun setClassName(context: Context, name: String) {
        getPrefs(context).edit().putString(KEY_CLASS_NAME, name).apply()
    }

    fun getClassLevel(context: Context): String {
        return getPrefs(context).getString(KEY_CLASS_LEVEL, "First Year") ?: "First Year"
    }

    fun setClassLevel(context: Context, level: String) {
        getPrefs(context).edit().putString(KEY_CLASS_LEVEL, level).apply()
    }

    fun getGroupName(context: Context): String {
        return getPrefs(context).getString(KEY_GROUP_NAME, "تمام گروپس") ?: "تمام گروپس"
    }

    fun setGroupName(context: Context, group: String) {
        getPrefs(context).edit().putString(KEY_GROUP_NAME, group).apply()
    }

    fun getSelectionMode(context: Context): String {
        return getPrefs(context).getString(KEY_SELECTION_MODE, "ALL") ?: "ALL"
    }

    fun setSelectionMode(context: Context, mode: String) {
        getPrefs(context).edit().putString(KEY_SELECTION_MODE, mode).apply()
    }

    fun getStudentCount(context: Context): Int {
        return getPrefs(context).getInt(KEY_STUDENT_COUNT, 0)
    }

    fun setStudentCount(context: Context, count: Int) {
        getPrefs(context).edit().putInt(KEY_STUDENT_COUNT, count).apply()
    }

    fun resetSetup(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
