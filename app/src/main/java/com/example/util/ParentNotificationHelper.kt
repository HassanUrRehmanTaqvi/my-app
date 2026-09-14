package com.example.util

import java.util.Locale

data class StudentMonthlyAttendanceStats(
    val presentPeriods: Int,
    val absentPeriods: Int,
    val totalPeriods: Int,
    val attendancePercentage: Double,
    val missedPeriodsToday: Int,
    val consecutiveAbsentDays: Int
)

object ParentNotificationHelper {

    const val DEFAULT_COLLEGE_NAME = "گورنمنٹ ایسوسی ایٹ کالج مخدوم رشید ملتان"

    /**
     * Ensures the teacher's name is ALWAYS displayed with the prefix "پروفیسر"
     * regardless of whether it's already in the designation or name.
     */
    fun formatTeacherName(rawName: String?): String {
        val clean = (rawName ?: "").trim()
        if (clean.isBlank()) return "پروفیسر حسن الرحمٰن تقوی"

        var stripped = clean
        val knownPrefixes = listOf(
            "پروفیسر",
            "پرو فیسر",
            "Prof.",
            "Prof ",
            "Professor "
        )
        for (p in knownPrefixes) {
            if (stripped.startsWith(p)) {
                stripped = stripped.substring(p.length).trim()
            }
        }
        return "پروفیسر $stripped"
    }

    /**
     * Resolves the teacher's department gracefully.
     */
    fun resolveDepartment(
        userDepartment: String?,
        classSubject: String? = null,
        defaultSubj: String? = null
    ): String {
        if (!userDepartment.isNullOrBlank()) return userDepartment.trim()
        if (!classSubject.isNullOrBlank()) return classSubject.trim()
        if (!defaultSubj.isNullOrBlank()) return defaultSubj.trim()
        return "اسلامیات"
    }

    /**
     * Generates a respectful, clear Urdu absence notification message for parents.
     */
    fun generateAbsenceNotification(
        studentName: String,
        rollNumber: String,
        date: String,
        missedPeriodsToday: Int,
        monthlyPresent: Int?,
        monthlyAbsent: Int?,
        attendancePercentage: Double?,
        consecutiveAbsentDays: Int,
        teacherName: String,
        department: String,
        collegeName: String = DEFAULT_COLLEGE_NAME
    ): String {
        val teacherWithPrefix = formatTeacherName(teacherName)
        val sb = StringBuilder()

        sb.append("محترم والدین/سرپرست!\n\n")
        sb.append("آپ کے بچے $studentName، رول نمبر $rollNumber، کی $date کو کالج میں غیر حاضری درج ہوئی ہے۔\n\n")

        // Monthly statistics block: only show if data is available and has real periods
        val hasMonthlyData = monthlyPresent != null && monthlyAbsent != null && (monthlyPresent + monthlyAbsent > 0)
        if (hasMonthlyData) {
            val total = (monthlyPresent ?: 0) + (monthlyAbsent ?: 0)
            val pct = attendancePercentage ?: if (total > 0) ((monthlyPresent ?: 0).toDouble() / total) * 100.0 else 0.0
            val pctFormatted = String.format(Locale.US, "%.1f", pct).removeSuffix(".0")

            sb.append("اس ماہ اب تک:\n")
            sb.append("حاضری: $monthlyPresent پیریڈ\n")
            sb.append("غیر حاضری: $monthlyAbsent پیریڈ\n")
            sb.append("حاضری کا تناسب: $pctFormatted٪\n\n")
        }

        val periodsToday = if (missedPeriodsToday > 0) missedPeriodsToday else 1
        sb.append("آج $periodsToday پیریڈ میں غیر حاضری ہوئی ہے۔\n\n")

        // Consecutive absences line: only shown when student has been absent for 2 or more consecutive days
        if (consecutiveAbsentDays >= 2) {
            sb.append("طالب علم گزشتہ $consecutiveAbsentDays دن سے مسلسل غیر حاضر ہے۔\n\n")
        }

        sb.append("براہِ کرم بچے کی باقاعدہ حاضری پر خصوصی توجہ دیں تاکہ اس کی تعلیم متاثر نہ ہو۔\n\n")
        sb.append("$teacherWithPrefix\n")
        if (department.isNotBlank()) {
            sb.append("شعبہ: $department\n")
        }
        sb.append(collegeName.ifBlank { DEFAULT_COLLEGE_NAME })

        return sb.toString().trim()
    }

    /**
     * Generates a respectful, clear Urdu test result report message for parents.
     */
    fun generateTestResultNotification(
        studentName: String,
        rollNumber: String,
        testName: String,
        subject: String,
        date: String,
        obtainedMarks: Double,
        totalMarks: Double,
        percentage: Double,
        teacherName: String,
        department: String,
        collegeName: String = DEFAULT_COLLEGE_NAME,
        goodThreshold: Double = 70.0,
        lowThreshold: Double = 40.0
    ): String {
        val teacherWithPrefix = formatTeacherName(teacherName)
        val sb = StringBuilder()

        sb.append("محترم والدین/سرپرست!\n\n")
        sb.append("آپ کے بچے $studentName، رول نمبر $rollNumber نے $date کو $subject کا \"$testName\" ٹیسٹ دیا۔\n\n")

        val obtainedStr = if (obtainedMarks % 1.0 == 0.0) obtainedMarks.toInt().toString() else String.format(Locale.US, "%.1f", obtainedMarks)
        val totalStr = if (totalMarks % 1.0 == 0.0) totalMarks.toInt().toString() else String.format(Locale.US, "%.1f", totalMarks)
        val pctStr = String.format(Locale.US, "%.1f", percentage).removeSuffix(".0")

        sb.append("حاصل کردہ نمبر: $obtainedStr / $totalStr\n")
        sb.append("نتیجہ: $pctStr٪\n\n")

        // Performance evaluation message
        val performanceSentence = when {
            percentage >= goodThreshold -> {
                "الحمدللہ، بچے کی کارکردگی اچھی رہی۔ اسے مزید محنت جاری رکھنے کی ترغیب دیں۔"
            }
            percentage < lowThreshold -> {
                "براہِ کرم بچے کی اس مضمون میں مزید محنت پر خصوصی توجہ دیں۔"
            }
            else -> {
                "براہِ کرم بچے کی تعلیمی کارکردگی پر توجہ دیں اور اسے مزید محنت کی ترغیب دیں۔"
            }
        }
        sb.append("$performanceSentence\n\n")

        sb.append("$teacherWithPrefix\n")
        if (department.isNotBlank()) {
            sb.append("شعبہ: $department\n")
        }
        sb.append(collegeName.ifBlank { DEFAULT_COLLEGE_NAME })

        return sb.toString().trim()
    }
}
