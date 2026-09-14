package com.example.util

import java.util.Calendar

data class AcademicSessionInfo(
    val sessionName: String, // e.g. "2026–2028"
    val startYear: Int,      // 2026
    val endYear: Int,        // 2028
    val defaultLevel: String // "First Year", "Second Year", "Completed"
)

typealias AcademicLevel = SessionHelper.AcademicLevel

object SessionHelper {

    enum class AcademicLevel(val englishTitle: String, val urduLabel: String) {
        FIRST_YEAR("First Year", "فرسٹ ایئر (سال اول)"),
        SECOND_YEAR("Second Year", "سیکنڈ ایئر (سال دوم)"),
        COMPLETED("Completed", "مکمل / فارغ التحصیل")
    }

    val DEFAULT_ACTIVE_SESSION = "2026–2028"

    val KNOWN_SESSIONS = listOf(
        "2026–2028",
        "2025–2027",
        "2024–2026",
        "2027–2029"
    )

    val DEFAULT_SESSIONS = KNOWN_SESSIONS

    fun determineLevelForSession(
        session: String,
        activeSession: String = DEFAULT_ACTIVE_SESSION
    ): AcademicLevel {
        val levelStr = calculateLevelForSession(session, activeSession)
        return when (levelStr) {
            "First Year" -> AcademicLevel.FIRST_YEAR
            "Second Year" -> AcademicLevel.SECOND_YEAR
            else -> AcademicLevel.COMPLETED
        }
    }

    /**
     * Resolves the academic level of a student given their session string and active intake session.
     * When 2026–2028 is active (incoming 1st year):
     * - 2026–2028 -> "First Year" (سال اول)
     * - 2025–2027 -> "Second Year" (سال دوم)
     * - 2024–2026 or older -> "Completed" (فارغ التحصیل / آرکائیو)
     * - 2027–2029 -> "First Year" (آئندہ سیشن)
     */
    fun calculateLevelForSession(
        studentSession: String,
        activeIntakeSession: String = DEFAULT_ACTIVE_SESSION
    ): String {
        val cleanStudentSession = studentSession.replace("-", "–").trim()
        val cleanActiveSession = activeIntakeSession.replace("-", "–").trim()

        val studentStartYear = parseStartYear(cleanStudentSession)
        val activeStartYear = parseStartYear(cleanActiveSession)

        if (studentStartYear == null || activeStartYear == null) {
            return "First Year"
        }

        val yearDiff = activeStartYear - studentStartYear
        return when (yearDiff) {
            0 -> "First Year"
            1 -> "Second Year"
            in Int.MIN_VALUE until 0 -> "First Year" // Upcoming intake
            else -> "Completed" // 2 or more years ago -> Graduated/Archived
        }
    }

    fun parseStartYear(session: String): Int? {
        val regex = Regex("(\\d{4})")
        val match = regex.find(session)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }

    fun parseEndYear(session: String): Int? {
        val regex = Regex("\\d{4}[–\\-](\\d{4})")
        val match = regex.find(session)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }

    /**
     * Returns Urdu label for a level
     */
    fun getLevelUrdu(level: String): String {
        return when (level) {
            "First Year" -> "فرسٹ ایئر (سال اول)"
            "Second Year" -> "سیکنڈ ایئر (سال دوم)"
            "Completed" -> "مکمل / فارغ التحصیل"
            else -> level
        }
    }

    /**
     * Auto-detect current academic session based on system date (August intake cycle)
     */
    fun detectCurrentSystemSession(): String {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) // 0-indexed, 7 is August

        // In Punjab colleges, academic session starts in August/September
        val intakeYear = if (month >= Calendar.JULY) year else year - 1
        return "$intakeYear–${intakeYear + 2}"
    }
}
