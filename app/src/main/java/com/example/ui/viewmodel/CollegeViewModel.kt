package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.InitialSeedData
import com.example.data.NominalRollValidator
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.AttendanceSessionEntity
import com.example.data.model.ClassEntity
import com.example.data.model.ClassMembershipEntity
import com.example.data.model.MessageLogEntity
import com.example.data.model.StudentEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.TestEntity
import com.example.data.model.TestResultEntity
import com.example.data.model.UserEntity
import com.example.repository.CollegeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AttendanceSummaryData(
    val totalStudents: Int = 0,
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val leaveCount: Int = 0,
    val percentage: Double = 0.0,
    val periodCount: Int = 1
)

class CollegeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = CollegeRepository(db)

    val currentUser: StateFlow<UserEntity?> = repository.currentUserFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentOwnerId = MutableStateFlow(InitialSeedData.DEFAULT_USER_ID)
    val currentOwnerId: StateFlow<String> = _currentOwnerId.asStateFlow()

    private val _currentYearId = MutableStateFlow(InitialSeedData.DEFAULT_YEAR_ID)
    val currentYearId: StateFlow<String> = _currentYearId.asStateFlow()

    private val _validationIssues = MutableStateFlow<List<NominalRollValidator.ValidationIssue>>(emptyList())
    val validationIssues: StateFlow<List<NominalRollValidator.ValidationIssue>> = _validationIssues.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeSeedDataIfNeeded()
            currentUser.collect { user ->
                if (user != null) {
                    _currentOwnerId.value = user.userId
                    refreshValidationIssues(user.userId)
                }
            }
        }
    }

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun refreshValidationIssues(ownerId: String) {
        viewModelScope.launch {
            val issues = repository.validateStudents(ownerId)
            _validationIssues.value = issues
        }
    }

    fun updateProfile(
        name: String,
        email: String,
        college: String,
        designation: String,
        defaultSubject: String,
        smsTemplate: String
    ) {
        viewModelScope.launch {
            val curr = currentUser.value ?: return@launch
            val updated = curr.copy(
                name = name,
                email = email,
                college = college,
                designation = designation,
                defaultSubject = defaultSubject,
                smsTemplate = smsTemplate
            )
            repository.updateCurrentUser(updated)
            _snackbarMessage.value = "پروفائل کامیابی سے اپ ڈیٹ ہو گئی!"
        }
    }

    fun switchUser(userId: String) {
        viewModelScope.launch {
            repository.switchUser(userId)
            _currentOwnerId.value = userId
            _snackbarMessage.value = "استاد کا پروفائل تبدیل ہو گیا!"
        }
    }

    fun createNewTeacherProfile(
        name: String,
        email: String,
        college: String,
        designation: String,
        subject: String
    ) {
        viewModelScope.launch {
            repository.createNewUser(name, email, college, designation, subject)
            _snackbarMessage.value = "نیا اکاؤنٹ کامیابی سے بن گیا!"
        }
    }

    // Class Creation
    fun createClass(
        className: String,
        level: String,
        section: String,
        subjectId: String,
        subjectName: String,
        subjectType: String,
        teacherName: String,
        selectedStudentIds: List<String>,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            val ownerId = _currentOwnerId.value
            val classId = repository.createClass(
                className = className,
                academicYearId = _currentYearId.value,
                level = level,
                section = section,
                subjectId = subjectId,
                subjectName = subjectName,
                subjectType = subjectType,
                teacherName = teacherName,
                ownerId = ownerId,
                studentIds = selectedStudentIds
            )
            _snackbarMessage.value = "کلاس '$className' کامیابی سے تیار ہو گئی! (${selectedStudentIds.size} طلبہ شامل)"
            onSuccess(classId)
        }
    }

    fun renameClass(classId: String, newName: String) {
        viewModelScope.launch {
            repository.updateClassName(classId, newName)
            _snackbarMessage.value = "کلاس کا نام تبدیل کر دیا گیا!"
        }
    }

    fun removeStudentFromClass(classId: String, studentId: String) {
        viewModelScope.launch {
            repository.removeStudentFromClass(classId, studentId)
            _snackbarMessage.value = "طالب علم کو کلاس سے خارج کر دیا گیا (سابقہ ریکارڈ محفوظ ہے)"
        }
    }

    fun addStudentToClass(classId: String, studentId: String) {
        viewModelScope.launch {
            repository.addStudentToClass(classId, studentId)
            _snackbarMessage.value = "طالب علم کلاس میں شامل ہو گیا"
        }
    }

    fun transferStudent(
        studentId: String,
        fromClassId: String,
        toClassId: String,
        newSection: String
    ) {
        viewModelScope.launch {
            repository.transferStudent(studentId, fromClassId, toClassId, newSection)
            _snackbarMessage.value = "طالب علم دوسری کلاس میں منتقل ہو گیا! (سابقہ حاضری و نتائج محفوظ ہیں)"
        }
    }

    // Attendance Operations
    fun saveAttendance(
        classId: String,
        subjectId: String,
        date: String,
        periodCount: Int,
        notes: String,
        studentStatuses: Map<String, String>,
        existingSessionId: String? = null,
        onComplete: (AttendanceSummaryData) -> Unit
    ) {
        viewModelScope.launch {
            val ownerId = _currentOwnerId.value
            repository.saveAttendanceSession(
                classId = classId,
                academicYearId = _currentYearId.value,
                subjectId = subjectId,
                date = date,
                periodCount = periodCount,
                ownerId = ownerId,
                notes = notes,
                studentStatuses = studentStatuses,
                existingSessionId = existingSessionId
            )

            val total = studentStatuses.size
            val present = studentStatuses.count { it.value == "Present" }
            val absent = studentStatuses.count { it.value == "Absent" }
            val leave = studentStatuses.count { it.value == "Leave" }
            val percentage = if (total > 0) (present.toDouble() / total) * 100.0 else 0.0

            val summary = AttendanceSummaryData(
                totalStudents = total,
                presentCount = present,
                absentCount = absent,
                leaveCount = leave,
                percentage = percentage,
                periodCount = periodCount
            )
            _snackbarMessage.value = "حاضری کامیابی سے محفوظ ہو گئی! (حاضر: $present، غیر حاضر: $absent)"
            onComplete(summary)
        }
    }

    // Test Operations
    fun saveTest(
        classId: String,
        subjectId: String,
        testName: String,
        date: String,
        totalMarks: Double,
        studentMarks: Map<String, Double>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val ownerId = _currentOwnerId.value
            repository.createTestWithResults(
                classId = classId,
                subjectId = subjectId,
                testName = testName,
                date = date,
                totalMarks = totalMarks,
                ownerId = ownerId,
                studentMarks = studentMarks
            )
            _snackbarMessage.value = "ٹیسٹ اور نتائج کامیابی سے محفوظ ہو گئے!"
            onSuccess()
        }
    }

    fun logSentMessage(student: StudentEntity, messageText: String, status: String) {
        viewModelScope.launch {
            repository.logMessage(student, messageText, status, _currentOwnerId.value)
        }
    }

    fun addSubject(name: String, urduName: String, type: String, level: String, aliases: String) {
        viewModelScope.launch {
            repository.addSubject(name, urduName, type, level, aliases, _currentOwnerId.value)
            _snackbarMessage.value = "نیا مضمون شامل کر دیا گیا!"
        }
    }

    fun addStudentToNominalRoll(student: StudentEntity) {
        viewModelScope.launch {
            repository.insertStudent(student)
            refreshValidationIssues(_currentOwnerId.value)
            _snackbarMessage.value = "طالب علم ماسٹر رول میں شامل ہو گیا!"
        }
    }

    fun updateStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.updateStudent(student)
            refreshValidationIssues(_currentOwnerId.value)
            _snackbarMessage.value = "طالب علم کا ڈیٹا اپ ڈیٹ ہو گیا!"
        }
    }

    fun archiveStudent(studentId: String) {
        viewModelScope.launch {
            repository.archiveStudent(studentId)
            refreshValidationIssues(_currentOwnerId.value)
            _snackbarMessage.value = "طالب علم آرکائیو ہو گیا (ڈیٹا محفوظ ہے)"
        }
    }
}
