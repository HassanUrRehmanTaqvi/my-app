package com.example.util

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import java.util.Calendar

object GoogleCalendarHelper {

    /**
     * Launches the system Calendar intent to create a recurring or month-end event
     * for attendance finalization, monthly report generation, and Google Drive backup.
     */
    fun createMonthEndReminderIntent(
        collegeName: String = "گورنمنٹ ایسوسی ایٹ کالج مخدوم رشید ملتان",
        teacherName: String = "پروفیسر حسن الرحمن تقویٰ"
    ): Intent {
        val calendar = Calendar.getInstance()
        // Set to last day of current month at 2:00 PM
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 14)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        val beginTime = calendar.timeInMillis
        val endTime = beginTime + (60 * 60 * 1000) // 1 hour duration

        val title = "کالج ماہانہ حاضری فائنل اور ڈرائیو بیک اپ — مخدوم رشید"
        val description = """
            یاددہانی برائے $teacherName:
            1. تمام کلاسز کے رجسٹر فائنل کریں (Present/Absent/Leaves)۔
            2. 75 فیصد سے کم حاضری والے طلبہ اور والدین کو SMS الرٹ بھیجیں۔
            3. ماہانہ حاضری اور ٹیسٹ رزلٹ PDF رپورٹ جنریٹ کریں۔
            4. Google Drive میں تازہ ترین ڈیٹا بیک اپ محفوظ کریں۔
            ادارہ: $collegeName
        """.trimIndent()

        return Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.Events.DESCRIPTION, description)
            putExtra(CalendarContract.Events.EVENT_LOCATION, collegeName)
            putExtra(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_PRIVATE)
            putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
            // Recurring monthly rule on the last day of every month
            putExtra(CalendarContract.Events.RRULE, "FREQ=MONTHLY;BYMONTHDAY=-1")
        }
    }
}
