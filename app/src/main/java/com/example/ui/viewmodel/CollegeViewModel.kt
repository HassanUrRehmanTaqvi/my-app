package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.InitialSeedData
import com.example.data.NominalRollValidator
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.AttendanceSessionEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.ClassEntity
import com.example.data.model.ClassMembershipEntity
import com.example.data.model.MessageLogEntity
import com.example.data.model.StudentEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.SyncQueueEntity
import com.example.data.model.TeacherEntity
import com.example.data.model.TestEntity
import com.example.data.model.TestResultEntity
import com.example.data.model.UserEntity
import com.example.repository.CollegeRepository
import com.example.util.CsvImportSummary
import com.example.util.GoogleCalendarHelper
import com.example.util.ParsedCsvRow
import com.example.util.SessionHelper
import kotlinx.coroutines.flow.Flow
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
import java.util.UUID

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
    val repository = CollegeRepository(db, application)

    val currentUser: StateFlow<UserEntity?> = repository.currentUserFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTeachers: StateFlow<List<TeacherEntity>> = repository.getAllTeachersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentOwnerId = MutableStateFlow(InitialSeedData.DEFAULT_USER_ID)
    val currentOwnerId: StateFlow<String> = _currentOwnerId.asStateFlow()

    private val _currentYearId = MutableStateFlow(InitialSeedData.DEFAULT_YEAR_ID)
    val currentYearId: StateFlow<String> = _currentYearId.asStateFlow()

    // Professor Setup Flow State
    private val _isSetupCompleted = MutableStateFlow(
        com.example.util.ProfessorSetupPreferences.isSetupCompleted(application)
    )
    val isSetupCompleted: StateFlow<Boolean> = _isSetupCompleted.asStateFlow()

    private val _showSetupFlow = MutableStateFlow(false)
    val showSetupFlow: StateFlow<Boolean> = _showSetupFlow.asStateFlow()

    private val _validationIssues = MutableStateFlow<List<NominalRollValidator.ValidationIssue>>(emptyList())
    val validationIssues: StateFlow<List<NominalRollValidator.ValidationIssue>> = _validationIssues.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Session Management (e.g. 2026–2028, 2025–2027)
    private val _activeSession = MutableStateFlow(SessionHelper.DEFAULT_ACTIVE_SESSION)
    val activeSession: StateFlow<String> = _activeSession.asStateFlow()

    // Offline-First Sync State
    private val _syncStatus = MutableStateFlow("ہم آہنگ (Synced)")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
    )
    val lastSyncTime: StateFlow<String> = _lastSyncTime.asStateFlow()

    val pendingSyncCount: Flow<Int>
        get() = repository.getPendingSyncCountFlow(_currentOwnerId.value)

    val auditLogs: Flow<List<AuditLogEntity>>
        get() = repository.getAuditLogs(_currentOwnerId.value)

    init {
        viewModelScope.launch {
            repository.initializeSeedDataIfNeeded(application)
            currentUser.collect { user ->
                if (user != null) {
                    _currentOwnerId.value = user.userId
                    refreshValidationIssues(user.userId)
                }
            }
        }
    }

    fun claimTeacherProfile(teacherId: String, email: String) {
        viewModelScope.launch {
            repository.claimTeacherProfile(teacherId, email)
            _snackbarMessage.value = "پروفائل کامیابی سے منسلک کر دی گئی!"
        }
    }

    fun updateTeacher(teacher: TeacherEntity) {
        viewModelScope.launch {
            repository.updateTeacher(teacher)
            _snackbarMessage.value = "استاد کا ریکارڈ اپ ڈیٹ ہو گیا!"
        }
    }

    fun getDistinctGroups(): Flow<List<String>> = repository.getDistinctGroups(_currentOwnerId.value)

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
            _snackbarMessage.value = "طالب علم آرکائیو ہو گیا (ڈیٹا اور حاضری محفوظ ہیں)"
        }
    }

    fun restoreStudent(studentId: String) {
        viewModelScope.launch {
            repository.restoreStudent(studentId)
            refreshValidationIssues(_currentOwnerId.value)
            _snackbarMessage.value = "طالب علم فعال لسٹ میں بحال ہو گیا!"
        }
    }

    suspend fun checkDuplicateRoll(rollNumber: String, className: String): StudentEntity? {
        return repository.findStudentByRollAndClass(_currentOwnerId.value, rollNumber.trim(), className.trim())
    }

    // Class Archiving & Safe Management
    fun getArchivedClasses(ownerId: String): Flow<List<ClassEntity>> = repository.getArchivedClasses(ownerId)

    fun archiveClass(classId: String) {
        viewModelScope.launch {
            repository.archiveClass(classId)
            _snackbarMessage.value = "کلاس کو آرکائیو کر دیا گیا! (تمام حاضری، ٹیسٹ اور ریکارڈ محفوظ ہیں)"
        }
    }

    fun restoreClass(classId: String) {
        viewModelScope.launch {
            repository.restoreClass(classId)
            _snackbarMessage.value = "کلاس کو دوبارہ فعال لسٹ میں بحال کر دیا گیا!"
        }
    }

    fun deleteClassPermanently(classId: String) {
        viewModelScope.launch {
            repository.deleteClassPermanently(classId)
            _snackbarMessage.value = "کلاس کو ڈیٹا بیس سے مکمل حذف کر دیا گیا"
        }
    }

    suspend fun getClassStats(classId: String): com.example.repository.ClassStats {
        return repository.getClassStats(classId)
    }

    // Session Selection
    fun setActiveSession(session: String) {
        _activeSession.value = session
        _snackbarMessage.value = "فعال تعلیمی سیشن تبدیل ہو گیا: $session"
    }

    // Offline Sync Action
    fun syncNow() {
        viewModelScope.launch {
            _syncStatus.value = "ہم آہنگ کیا جا رہا ہے (Syncing...)"
            kotlinx.coroutines.delay(800) // Visual feedback
            repository.markAllSynced(_currentOwnerId.value)
            _syncStatus.value = "ہم آہنگ (Synced)"
            _lastSyncTime.value = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            _snackbarMessage.value = "تمام ریکارڈز کامیابی سے کلاؤڈ/ڈرائیو کیو کے ساتھ ہم آہنگ ہو گئے!"
        }
    }

    fun logAudit(actionType: String, summary: String, details: String = "") {
        viewModelScope.launch {
            repository.logAudit(_currentOwnerId.value, actionType, summary, details)
        }
    }

    fun scheduleMonthEndReminder(context: Context) {
        try {
            val user = currentUser.value
            val college = user?.college ?: "گورنمنٹ ایسوسی ایٹ کالج مخدوم رشید ملتان"
            val teacher = user?.name ?: "پروفیسر حسن الرحمن تقویٰ"
            val intent = GoogleCalendarHelper.createMonthEndReminderIntent(college, teacher)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            _snackbarMessage.value = "کیلنڈر میں ماہانہ یاددہانی شیڈول کرنے کی درخواست کھولی جا رہی ہے"
        } catch (e: Exception) {
            _snackbarMessage.value = "کیلنڈر ایپلیکیشن دستیاب نہیں ہے"
        }
    }

    // CSV Bulk Import
    fun importCsvStudents(
        parsedRows: List<ParsedCsvRow>,
        updateExisting: Boolean,
        onComplete: (CsvImportSummary) -> Unit
    ) {
        viewModelScope.launch {
            val ownerId = _currentOwnerId.value
            val yearId = _currentYearId.value
            val summary = repository.importStudentsFromCsv(
                parsedRows = parsedRows,
                updateExisting = updateExisting,
                ownerId = ownerId,
                yearId = yearId
            )
            refreshValidationIssues(ownerId)
            _snackbarMessage.value = summary.message
            onComplete(summary)
        }
    }

    // Professor Setup Actions
    fun openSetupFlow() {
        _showSetupFlow.value = true
    }

    fun closeSetupFlow() {
        _showSetupFlow.value = false
    }

    fun completeProfessorSetup(
        teacher: TeacherEntity,
        className: String,
        section: String,
        groupName: String,
        selectedStudents: List<StudentEntity>,
        selectionMode: String,
        onComplete: (classId: String) -> Unit
    ) {
        viewModelScope.launch {
            val classId = repository.setupProfessorAccount(
                teacher = teacher,
                className = className,
                section = section,
                groupName = groupName,
                selectedStudents = selectedStudents,
                selectionMode = selectionMode
            )
            val ctx = getApplication<Application>()
            com.example.util.ProfessorSetupPreferences.setSetupCompleted(ctx, true)
            com.example.util.ProfessorSetupPreferences.setTeacherId(ctx, teacher.teacherId)
            com.example.util.ProfessorSetupPreferences.setTeacherName(ctx, teacher.name)
            com.example.util.ProfessorSetupPreferences.setClassId(ctx, classId)
            com.example.util.ProfessorSetupPreferences.setClassName(ctx, className)
            com.example.util.ProfessorSetupPreferences.setClassLevel(ctx, className)
            com.example.util.ProfessorSetupPreferences.setGroupName(ctx, groupName)
            com.example.util.ProfessorSetupPreferences.setSelectionMode(ctx, selectionMode)
            com.example.util.ProfessorSetupPreferences.setStudentCount(ctx, selectedStudents.size)

            _isSetupCompleted.value = true
            _showSetupFlow.value = false
            _snackbarMessage.value = "پروفیسر اور کلاس کا سیٹ اپ کامیابی سے مکمل ہو گیا!"
            onComplete(classId)
        }
    }

    fun updateClassStudentSelection(
        classId: String,
        selectedStudents: List<StudentEntity>,
        selectionMode: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.updateClassStudentSelection(classId, selectedStudents)
            val ctx = getApplication<Application>()
            com.example.util.ProfessorSetupPreferences.setSelectionMode(ctx, selectionMode)
            com.example.util.ProfessorSetupPreferences.setStudentCount(ctx, selectedStudents.size)
            _snackbarMessage.value = "طلبہ کی فہرست کامیابی سے تبدیل ہو گئی! (${selectedStudents.size} طلبہ)"
            onComplete()
        }
    }

    fun resetProfessorSetup() {
        val ctx = getApplication<Application>()
        com.example.util.ProfessorSetupPreferences.resetSetup(ctx)
        _isSetupCompleted.value = false
        _showSetupFlow.value = true
        _snackbarMessage.value = "سیٹ اپ ری سیٹ کر دیا گیا۔ نیا پروفیسر منتخب کریں۔"
    }

    suspend fun getAvailableStudentsForClass(
        className: String,
        groupName: String
    ): List<StudentEntity> {
        return repository.getAvailableStudentsForClass(className, groupName)
    }
}
