package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val name: String,
    val email: String,
    val college: String,
    val designation: String,
    val defaultSubject: String,
    val smsTemplate: String,
    val isCurrent: Boolean = true
)

@Entity(
    tableName = "academic_years",
    indices = [Index(value = ["ownerId", "name"])]
)
data class AcademicYearEntity(
    @PrimaryKey val yearId: String,
    val name: String, // e.g. "2026–27"
    val ownerId: String,
    val isDefault: Boolean = false
)

@Entity(
    tableName = "students",
    indices = [
        Index(value = ["studentId"], unique = true),
        Index(value = ["academicYearId", "className"]),
        Index(value = ["academicYearId", "rollNumber"]),
        Index(value = ["ownerId"])
    ]
)
data class StudentEntity(
    @PrimaryKey val studentId: String, // Permanent UUID identity
    val rollNumber: String,
    val name: String,
    val fatherName: String,
    val phone: String,
    val className: String, // "First Year" or "Second Year"
    val section: String = "A", // Default section if assigned
    val groupName: String, // e.g. "ICS Physics", "Arts", "Pre-Medical", "IT Arts"
    val electiveSubjectsRaw: String, // e.g. "Psychology, Civics, Islamic Studies Elective"
    val academicYearId: String,
    val ownerId: String,
    val status: String = "Active", // "Active", "Archived", "Transferred"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "subjects",
    indices = [Index(value = ["name"])]
)
data class SubjectEntity(
    @PrimaryKey val subjectId: String,
    val name: String,
    val urduName: String,
    val englishName: String,
    val type: String, // "Compulsory" or "Elective"
    val academicLevel: String, // "First Year", "Second Year", "Both"
    val groupEligibility: String = "All", // "All", "Arts", "ICS", etc.
    val aliases: String = "", // Comma-separated synonyms for auto-normalization
    val active: Boolean = true,
    val ownerId: String
)

@Entity(
    tableName = "classes",
    indices = [
        Index(value = ["academicYearId", "className", "section", "subjectId"]),
        Index(value = ["ownerId"])
    ]
)
data class ClassEntity(
    @PrimaryKey val classId: String,
    val className: String, // e.g. "First Year B – Islamic Studies"
    val academicYearId: String,
    val level: String, // "First Year" or "Second Year"
    val section: String, // "A", "B", "C"
    val subjectId: String,
    val subjectName: String,
    val subjectType: String, // "Compulsory" or "Elective"
    val teacherName: String,
    val ownerId: String,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "class_memberships",
    indices = [
        Index(value = ["classId", "studentId"]),
        Index(value = ["studentId"]),
        Index(value = ["status"])
    ]
)
data class ClassMembershipEntity(
    @PrimaryKey val membershipId: String,
    val classId: String,
    val studentId: String,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val status: String = "Active", // "Active", "Transferred", "Removed"
    val transferReason: String? = null
)

@Entity(
    tableName = "attendance_sessions",
    indices = [
        Index(value = ["classId", "date"]),
        Index(value = ["ownerId"])
    ]
)
data class AttendanceSessionEntity(
    @PrimaryKey val sessionId: String,
    val classId: String,
    val academicYearId: String,
    val subjectId: String,
    val date: String, // YYYY-MM-DD
    val periodCount: Int = 1,
    val ownerId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(
    tableName = "attendance_records",
    indices = [
        Index(value = ["sessionId", "studentId", "period"], unique = true),
        Index(value = ["studentId"]),
        Index(value = ["status"])
    ]
)
data class AttendanceRecordEntity(
    @PrimaryKey val recordId: String,
    val sessionId: String,
    val studentId: String,
    val period: Int = 1,
    val status: String // "Present", "Absent", "Leave"
)

@Entity(
    tableName = "tests",
    indices = [
        Index(value = ["classId", "date"]),
        Index(value = ["ownerId"])
    ]
)
data class TestEntity(
    @PrimaryKey val testId: String,
    val classId: String,
    val subjectId: String,
    val testName: String,
    val date: String,
    val totalMarks: Double,
    val ownerId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "test_results",
    indices = [
        Index(value = ["testId", "studentId"], unique = true),
        Index(value = ["studentId"])
    ]
)
data class TestResultEntity(
    @PrimaryKey val resultId: String,
    val testId: String,
    val studentId: String,
    val obtainedMarks: Double,
    val percentage: Double,
    val status: String // "Passed", "Failed"
)

@Entity(
    tableName = "message_logs",
    indices = [
        Index(value = ["studentId"]),
        Index(value = ["ownerId"]),
        Index(value = ["timestamp"])
    ]
)
data class MessageLogEntity(
    @PrimaryKey val messageId: String,
    val studentId: String,
    val studentName: String,
    val rollNumber: String,
    val parentPhone: String,
    val messageText: String,
    val status: String, // "Draft", "Opened", "Logged"
    val timestamp: Long = System.currentTimeMillis(),
    val ownerId: String
)
