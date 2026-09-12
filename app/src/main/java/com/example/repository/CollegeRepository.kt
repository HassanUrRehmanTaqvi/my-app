package com.example.repository

import android.content.Context
import com.example.data.AppDatabase
import com.example.data.InitialSeedData
import com.example.data.NominalRollValidator
import com.example.data.SubjectNormalizer
import com.example.data.model.AcademicYearEntity
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
import com.example.util.CsvImportSummary
import com.example.util.ParsedCsvRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ClassStats(
    val attendanceSessionsCount: Int,
    val attendanceRecordsCount: Int,
    val testsCount: Int,
    val studentsCount: Int
)

class CollegeRepository(private val database: AppDatabase) {

    private val userDao = database.userDao()
    private val academicYearDao = database.academicYearDao()
    private val studentDao = database.studentDao()
    private val subjectDao = database.subjectDao()
    private val classDao = database.classDao()
    private val membershipDao = database.classMembershipDao()
    private val attendanceDao = database.attendanceDao()
    private val testDao = database.testDao()
    private val messageLogDao = database.messageLogDao()

    val currentUserFlow: Flow<UserEntity?> = userDao.getCurrentUserFlow()
    val allUsersFlow: Flow<List<UserEntity>> = userDao.getAllUsersFlow()

    suspend fun initializeSeedDataIfNeeded() = withContext(Dispatchers.IO) {
        val currentUser = userDao.getCurrentUser()
        if (currentUser == null) {
            // Seed User
            userDao.insertUser(InitialSeedData.defaultUser)
            val ownerId = InitialSeedData.DEFAULT_USER_ID

            // Seed Academic Year
            academicYearDao.insertYear(InitialSeedData.defaultAcademicYear)

            // Seed Subjects
            subjectDao.insertSubjects(InitialSeedData.getDefaultSubjects(ownerId))

            // Seed First Year & Second Year Nominal Rolls
            val fyStudents = InitialSeedData.getFirstYearNominalRoll(ownerId, InitialSeedData.DEFAULT_YEAR_ID)
            val syStudents = InitialSeedData.getSecondYearNominalRoll(ownerId, InitialSeedData.DEFAULT_YEAR_ID)
            studentDao.insertStudents(fyStudents + syStudents)

            // Seed Default Class
            classDao.insertClass(InitialSeedData.defaultClass)

            // Seed Default Memberships
            val memberships = InitialSeedData.getDefaultMemberships(fyStudents, InitialSeedData.defaultClassId)
            membershipDao.insertMemberships(memberships)

            // Seed initial attendance session for demonstration
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val sampleSessionId = "session_sample_01"
            val session = AttendanceSessionEntity(
                sessionId = sampleSessionId,
                classId = InitialSeedData.defaultClassId,
                academicYearId = InitialSeedData.DEFAULT_YEAR_ID,
                subjectId = "comp_islamiat",
                date = todayDate,
                periodCount = 1,
                ownerId = ownerId,
                notes = "Daily Lecture Attendance"
            )
            attendanceDao.insertSession(session)

            // Mark attendance: all present except 2 students absent
            val enrolledStudents = fyStudents.filter { it.section == "B" }
            val records = enrolledStudents.mapIndexed { idx, student ->
                val status = if (idx == 1 || idx == 3) "Absent" else "Present"
                AttendanceRecordEntity(
                    recordId = UUID.randomUUID().toString(),
                    sessionId = sampleSessionId,
                    studentId = student.studentId,
                    period = 1,
                    status = status
                )
            }
            attendanceDao.insertRecords(records)

            // Seed a sample test
            val sampleTestId = "test_sample_01"
            val test = TestEntity(
                testId = sampleTestId,
                classId = InitialSeedData.defaultClassId,
                subjectId = "comp_islamiat",
                testName = "Monthly Test — سیرت النبی ﷺ",
                date = todayDate,
                totalMarks = 25.0,
                ownerId = ownerId
            )
            testDao.insertTest(test)

            val testResults = enrolledStudents.mapIndexed { idx, student ->
                val marks = when (idx) {
                    0 -> 24.0
                    1 -> 18.0
                    2 -> 22.5
                    3 -> 12.0
                    4 -> 20.0
                    else -> 19.0
                }
                TestResultEntity(
                    resultId = UUID.randomUUID().toString(),
                    testId = sampleTestId,
                    studentId = student.studentId,
                    obtainedMarks = marks,
                    percentage = (marks / 25.0) * 100.0,
                    status = if (marks >= 10.0) "Passed" else "Failed"
                )
            }
            testDao.insertResults(testResults)
        }
    }

    // User Profile
    suspend fun updateCurrentUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
    }

    suspend fun switchUser(userId: String) = withContext(Dispatchers.IO) {
        userDao.switchActiveUser(userId)
    }

    suspend fun createNewUser(name: String, email: String, college: String, designation: String, subject: String) = withContext(Dispatchers.IO) {
        val newUserId = UUID.randomUUID().toString()
        val user = UserEntity(
            userId = newUserId,
            name = name,
            email = email,
            college = college,
            designation = designation,
            defaultSubject = subject,
            smsTemplate = InitialSeedData.defaultUser.smsTemplate,
            isCurrent = true
        )
        userDao.switchActiveUser("NONE")
        userDao.insertUser(user)

        // Seed default academic year & subjects for new user
        val yearId = UUID.randomUUID().toString()
        academicYearDao.insertYear(
            AcademicYearEntity(
                yearId = yearId,
                name = "2026–27",
                ownerId = newUserId,
                isDefault = true
            )
        )
        subjectDao.insertSubjects(InitialSeedData.getDefaultSubjects(newUserId))
    }

    // Academic Years
    fun getAcademicYears(ownerId: String): Flow<List<AcademicYearEntity>> = academicYearDao.getYearsForOwner(ownerId)

    suspend fun addAcademicYear(name: String, ownerId: String) = withContext(Dispatchers.IO) {
        val year = AcademicYearEntity(
            yearId = UUID.randomUUID().toString(),
            name = name,
            ownerId = ownerId,
            isDefault = false
        )
        academicYearDao.insertYear(year)
    }

    // Students & Nominal Rolls
    fun getActiveStudents(ownerId: String): Flow<List<StudentEntity>> = studentDao.getAllActiveStudentsFlow(ownerId)

    fun getStudentsByClass(ownerId: String, className: String): Flow<List<StudentEntity>> =
        studentDao.getStudentsByClassFlow(ownerId, className)

    suspend fun getStudentsForClassCreation(
        ownerId: String,
        className: String,
        section: String?,
        subjectName: String,
        subjectType: String,
        academicYearId: String
    ): List<StudentEntity> = withContext(Dispatchers.IO) {
        val allClassStudents = studentDao.getStudentsByClassAndYear(ownerId, className, academicYearId)

        val sectionFiltered = if (!section.isNullOrBlank() && section != "All") {
            allClassStudents.filter { it.section.equals(section, ignoreCase = true) }
        } else {
            allClassStudents
        }

        // COMPULSORY SUBJECT: All students eligible
        if (subjectType.equals("Compulsory", ignoreCase = true)) {
            return@withContext sectionFiltered
        }

        // ELECTIVE SUBJECT: Filter by chosen elective or group
        return@withContext sectionFiltered.filter { student ->
            SubjectNormalizer.isStudentEligible(
                targetSubjectName = subjectName,
                targetSubjectType = subjectType,
                studentGroup = student.groupName,
                studentElectivesRaw = student.electiveSubjectsRaw
            )
        }
    }

    suspend fun getStudentById(studentId: String): StudentEntity? = withContext(Dispatchers.IO) {
        studentDao.getStudentById(studentId)
    }

    suspend fun getStudentsByIds(ids: List<String>): List<StudentEntity> = withContext(Dispatchers.IO) {
        studentDao.getStudentsByIds(ids)
    }

    fun getStudentsByIdsFlow(ids: List<String>): Flow<List<StudentEntity>> =
        studentDao.getStudentsByIdsFlow(ids)

    suspend fun insertStudent(student: StudentEntity) = withContext(Dispatchers.IO) {
        studentDao.insertStudent(student)
    }

    suspend fun updateStudent(student: StudentEntity) = withContext(Dispatchers.IO) {
        studentDao.updateStudent(student)
    }

    fun getArchivedStudents(ownerId: String): Flow<List<StudentEntity>> =
        studentDao.getArchivedStudentsFlow(ownerId)

    suspend fun findStudentByRollAndClass(ownerId: String, rollNumber: String, className: String): StudentEntity? = withContext(Dispatchers.IO) {
        studentDao.findStudentByRollAndClass(ownerId, rollNumber, className)
    }

    suspend fun archiveStudent(studentId: String) = withContext(Dispatchers.IO) {
        studentDao.archiveStudent(studentId)
    }

    suspend fun restoreStudent(studentId: String) = withContext(Dispatchers.IO) {
        studentDao.restoreStudent(studentId)
    }

    suspend fun validateStudents(ownerId: String): List<NominalRollValidator.ValidationIssue> = withContext(Dispatchers.IO) {
        val students = studentDao.getAllActiveStudentsFlow(ownerId).firstOrNull() ?: emptyList()
        NominalRollValidator.validateNominalRoll(students)
    }

    suspend fun importStudentsFromCsv(
        parsedRows: List<ParsedCsvRow>,
        updateExisting: Boolean,
        ownerId: String,
        yearId: String
    ): CsvImportSummary = withContext(Dispatchers.IO) {
        var imported = 0
        var updated = 0
        var skipped = 0
        var failed = 0

        for (row in parsedRows) {
            if (!row.isValid) {
                failed++
                continue
            }
            // Check if student already exists by roll number and class
            val existing = studentDao.findStudentByRollAndClass(ownerId, row.rollNumber, row.className)
            if (existing != null) {
                if (updateExisting) {
                    // Update details while strictly preserving studentId, academicYearId, ownerId, status, and createdAt.
                    // This guarantees that all attendance records and test scores remain linked and intact.
                    val updatedStudent = existing.copy(
                        name = row.name.ifBlank { existing.name },
                        fatherName = row.fatherName.ifBlank { existing.fatherName },
                        phone = row.phone.ifBlank { existing.phone },
                        section = row.section.ifBlank { existing.section },
                        groupName = row.groupName.ifBlank { existing.groupName },
                        electiveSubjectsRaw = if (row.optionalSubjects.isNotBlank()) row.optionalSubjects else existing.electiveSubjectsRaw
                    )
                    studentDao.updateStudent(updatedStudent)
                    updated++
                } else {
                    skipped++
                }
            } else {
                // Insert new student with unique UUID
                val newStudent = StudentEntity(
                    studentId = UUID.randomUUID().toString(),
                    rollNumber = row.rollNumber,
                    name = row.name,
                    fatherName = row.fatherName,
                    phone = row.phone,
                    className = row.className,
                    section = row.section,
                    groupName = row.groupName,
                    electiveSubjectsRaw = row.optionalSubjects,
                    academicYearId = yearId,
                    ownerId = ownerId,
                    status = "Active"
                )
                studentDao.insertStudent(newStudent)
                imported++
            }
        }

        val message = "امپورٹ رپورٹ: $imported نئے شامل، $updated اپ ڈیٹ، $skipped چھوڑے گئے، $failed ناکام"
        CsvImportSummary(
            importedCount = imported,
            updatedCount = updated,
            skippedCount = skipped,
            failedCount = failed,
            message = message
        )
    }

    // Subjects
    fun getSubjects(ownerId: String): Flow<List<SubjectEntity>> = subjectDao.getSubjectsFlow(ownerId)

    suspend fun addSubject(
        name: String,
        urduName: String,
        type: String,
        level: String,
        aliases: String,
        ownerId: String
    ) = withContext(Dispatchers.IO) {
        val subject = SubjectEntity(
            subjectId = UUID.randomUUID().toString(),
            name = name,
            urduName = urduName,
            englishName = name,
            type = type,
            academicLevel = level,
            aliases = aliases,
            active = true,
            ownerId = ownerId
        )
        subjectDao.insertSubject(subject)
    }

    // Classes & Memberships
    fun getClasses(ownerId: String): Flow<List<ClassEntity>> = classDao.getClassesFlow(ownerId)

    fun getArchivedClasses(ownerId: String): Flow<List<ClassEntity>> = classDao.getArchivedClassesFlow(ownerId)

    fun getAllClasses(ownerId: String): Flow<List<ClassEntity>> = classDao.getAllClassesFlow(ownerId)

    fun getClass(classId: String): Flow<ClassEntity?> = classDao.getClassFlow(classId)

    suspend fun archiveClass(classId: String) = withContext(Dispatchers.IO) {
        classDao.archiveClass(classId)
    }

    suspend fun restoreClass(classId: String) = withContext(Dispatchers.IO) {
        classDao.restoreClass(classId)
    }

    suspend fun deleteClassPermanently(classId: String) = withContext(Dispatchers.IO) {
        classDao.deleteClass(classId)
    }

    suspend fun getClassStats(classId: String): ClassStats = withContext(Dispatchers.IO) {
        val sessions = attendanceDao.getSessionCountForClass(classId)
        val records = attendanceDao.getRecordCountForClass(classId)
        val tests = testDao.getTestCountForClass(classId)
        val students = membershipDao.getActiveMembershipCount(classId)
        ClassStats(
            attendanceSessionsCount = sessions,
            attendanceRecordsCount = records,
            testsCount = tests,
            studentsCount = students
        )
    }

    suspend fun createClass(
        className: String,
        academicYearId: String,
        level: String,
        section: String,
        subjectId: String,
        subjectName: String,
        subjectType: String,
        teacherName: String,
        ownerId: String,
        studentIds: List<String>
    ): String = withContext(Dispatchers.IO) {
        val classId = UUID.randomUUID().toString()
        val classEntity = ClassEntity(
            classId = classId,
            className = className,
            academicYearId = academicYearId,
            level = level,
            section = section,
            subjectId = subjectId,
            subjectName = subjectName,
            subjectType = subjectType,
            teacherName = teacherName,
            ownerId = ownerId
        )
        classDao.insertClass(classEntity)

        val memberships = studentIds.map { sId ->
            ClassMembershipEntity(
                membershipId = UUID.randomUUID().toString(),
                classId = classId,
                studentId = sId,
                startDate = System.currentTimeMillis(),
                status = "Active"
            )
        }
        membershipDao.insertMemberships(memberships)
        classId
    }

    suspend fun updateClassName(classId: String, newName: String) = withContext(Dispatchers.IO) {
        val cls = classDao.getClassById(classId) ?: return@withContext
        classDao.updateClass(cls.copy(className = newName))
    }

    fun getActiveMemberships(classId: String): Flow<List<ClassMembershipEntity>> =
        membershipDao.getActiveMembershipsFlow(classId)

    suspend fun getActiveMembershipsDirect(classId: String): List<ClassMembershipEntity> = withContext(Dispatchers.IO) {
        membershipDao.getActiveMemberships(classId)
    }

    suspend fun addStudentToClass(classId: String, studentId: String) = withContext(Dispatchers.IO) {
        val membership = ClassMembershipEntity(
            membershipId = UUID.randomUUID().toString(),
            classId = classId,
            studentId = studentId,
            startDate = System.currentTimeMillis(),
            status = "Active"
        )
        membershipDao.insertMembership(membership)
    }

    suspend fun removeStudentFromClass(classId: String, studentId: String, reason: String = "Removed from class") = withContext(Dispatchers.IO) {
        membershipDao.removeStudentFromClass(
            classId = classId,
            studentId = studentId,
            endDate = System.currentTimeMillis(),
            reason = reason
        )
    }

    suspend fun transferStudent(
        studentId: String,
        fromClassId: String,
        toClassId: String,
        newSection: String
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        // 1. Soft-close old membership
        membershipDao.removeStudentFromClass(
            classId = fromClassId,
            studentId = studentId,
            endDate = now,
            reason = "Transferred to another section/class"
        )
        // 2. Add new membership in destination class
        membershipDao.insertMembership(
            ClassMembershipEntity(
                membershipId = UUID.randomUUID().toString(),
                classId = toClassId,
                studentId = studentId,
                startDate = now,
                status = "Active"
            )
        )
        // 3. Update student's section
        val student = studentDao.getStudentById(studentId)
        if (student != null) {
            studentDao.updateStudent(student.copy(section = newSection))
        }
    }

    // Attendance Engine
    fun getSessionsForClass(classId: String): Flow<List<AttendanceSessionEntity>> =
        attendanceDao.getSessionsForClassFlow(classId)

    suspend fun getSessionsForClassDirect(classId: String): List<AttendanceSessionEntity> = withContext(Dispatchers.IO) {
        attendanceDao.getSessionsForClass(classId)
    }

    fun getAllSessions(ownerId: String): Flow<List<AttendanceSessionEntity>> =
        attendanceDao.getAllSessionsFlow(ownerId)

    suspend fun findExistingSession(classId: String, date: String): AttendanceSessionEntity? = withContext(Dispatchers.IO) {
        attendanceDao.findExistingSession(classId, date)
    }

    suspend fun getSessionById(sessionId: String): AttendanceSessionEntity? = withContext(Dispatchers.IO) {
        attendanceDao.getSessionById(sessionId)
    }

    fun getRecordsForSession(sessionId: String): Flow<List<AttendanceRecordEntity>> =
        attendanceDao.getRecordsForSessionFlow(sessionId)

    suspend fun getRecordsForSessionDirect(sessionId: String): List<AttendanceRecordEntity> = withContext(Dispatchers.IO) {
        attendanceDao.getRecordsForSession(sessionId)
    }

    suspend fun saveAttendanceSession(
        classId: String,
        academicYearId: String,
        subjectId: String,
        date: String,
        periodCount: Int,
        ownerId: String,
        notes: String,
        studentStatuses: Map<String, String>, // studentId -> "Present", "Absent", "Leave"
        existingSessionId: String? = null
    ): String = withContext(Dispatchers.IO) {
        val sessionId = existingSessionId ?: UUID.randomUUID().toString()

        val session = AttendanceSessionEntity(
            sessionId = sessionId,
            classId = classId,
            academicYearId = academicYearId,
            subjectId = subjectId,
            date = date,
            periodCount = periodCount,
            ownerId = ownerId,
            timestamp = System.currentTimeMillis(),
            notes = notes
        )
        attendanceDao.insertSession(session)

        if (existingSessionId != null) {
            attendanceDao.deleteRecordsForSession(sessionId)
        }

        val records = mutableListOf<AttendanceRecordEntity>()
        for (period in 1..periodCount) {
            studentStatuses.forEach { (studentId, status) ->
                records.add(
                    AttendanceRecordEntity(
                        recordId = UUID.randomUUID().toString(),
                        sessionId = sessionId,
                        studentId = studentId,
                        period = period,
                        status = status
                    )
                )
            }
        }
        attendanceDao.insertRecords(records)
        sessionId
    }

    suspend fun getRecordsForClass(classId: String): List<AttendanceRecordEntity> = withContext(Dispatchers.IO) {
        attendanceDao.getRecordsForClass(classId)
    }

    suspend fun getSessionsForDate(ownerId: String, date: String): List<AttendanceSessionEntity> = withContext(Dispatchers.IO) {
        attendanceDao.getSessionsForDate(ownerId, date)
    }

    // Parent Messaging
    fun formatSmsMessage(
        template: String,
        student: StudentEntity,
        date: String,
        missedPeriods: Int,
        className: String,
        section: String,
        subject: String,
        teacherName: String,
        designation: String,
        collegeName: String
    ): String {
        return template
            .replace("{student_name}", student.name)
            .replace("{roll_number}", student.rollNumber)
            .replace("{father_name}", student.fatherName)
            .replace("{date}", date)
            .replace("{missed_periods}", missedPeriods.toString())
            .replace("{class_name}", className)
            .replace("{section}", section)
            .replace("{subject}", subject)
            .replace("{teacher_name}", teacherName)
            .replace("{designation}", designation)
            .replace("{college_name}", collegeName)
    }

    suspend fun logMessage(
        student: StudentEntity,
        messageText: String,
        status: String,
        ownerId: String
    ) = withContext(Dispatchers.IO) {
        val log = MessageLogEntity(
            messageId = UUID.randomUUID().toString(),
            studentId = student.studentId,
            studentName = student.name,
            rollNumber = student.rollNumber,
            parentPhone = student.phone,
            messageText = messageText,
            status = status,
            ownerId = ownerId
        )
        messageLogDao.insertLog(log)
    }

    fun getMessageLogs(ownerId: String): Flow<List<MessageLogEntity>> = messageLogDao.getLogsFlow(ownerId)

    // Tests & Results
    fun getTestsForClass(classId: String): Flow<List<TestEntity>> = testDao.getTestsForClassFlow(classId)

    fun getAllTests(ownerId: String): Flow<List<TestEntity>> = testDao.getAllTestsFlow(ownerId)

    suspend fun getTestById(testId: String): TestEntity? = withContext(Dispatchers.IO) {
        testDao.getTestById(testId)
    }

    fun getResultsForTest(testId: String): Flow<List<TestResultEntity>> = testDao.getResultsForTestFlow(testId)

    suspend fun getResultsForTestDirect(testId: String): List<TestResultEntity> = withContext(Dispatchers.IO) {
        testDao.getResultsForTest(testId)
    }

    suspend fun createTestWithResults(
        classId: String,
        subjectId: String,
        testName: String,
        date: String,
        totalMarks: Double,
        ownerId: String,
        studentMarks: Map<String, Double> // studentId -> obtainedMarks
    ): String = withContext(Dispatchers.IO) {
        val testId = UUID.randomUUID().toString()
        val test = TestEntity(
            testId = testId,
            classId = classId,
            subjectId = subjectId,
            testName = testName,
            date = date,
            totalMarks = totalMarks,
            ownerId = ownerId
        )
        testDao.insertTest(test)

        val results = studentMarks.map { (studentId, obtained) ->
            val percentage = if (totalMarks > 0) (obtained / totalMarks) * 100.0 else 0.0
            val status = if (percentage >= 33.0) "Passed" else "Failed"
            TestResultEntity(
                resultId = UUID.randomUUID().toString(),
                testId = testId,
                studentId = studentId,
                obtainedMarks = obtained,
                percentage = percentage,
                status = status
            )
        }
        testDao.insertResults(results)
        testId
    }

    // Nominal Roll Import from Raw Data (CSV/TSV/Lines)
    suspend fun parseAndValidateImport(
        rawText: String,
        targetClass: String, // "First Year" or "Second Year"
        ownerId: String,
        academicYearId: String
    ): Pair<List<StudentEntity>, List<NominalRollValidator.ValidationIssue>> = withContext(Dispatchers.Default) {
        val students = mutableListOf<StudentEntity>()
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

        for (line in lines) {
            // Support CSV, TSV, or pipe separated
            val tokens = when {
                line.contains("\t") -> line.split("\t")
                line.contains(",") -> line.split(",")
                line.contains("|") -> line.split("|")
                else -> line.split(Regex("\\s{2,}"))
            }.map { it.trim() }

            if (tokens.size >= 2) {
                // Ignore header line
                val firstTokenLower = tokens[0].lowercase()
                if (firstTokenLower.contains("roll") || firstTokenLower.contains("name")) continue

                val roll = tokens.getOrNull(0) ?: ""
                val name = tokens.getOrNull(1) ?: ""
                val father = tokens.getOrNull(2) ?: ""
                val group = tokens.getOrNull(3) ?: "Arts"
                val electives = tokens.getOrNull(4) ?: ""
                val phone = tokens.getOrNull(5) ?: ""

                students.add(
                    StudentEntity(
                        studentId = UUID.randomUUID().toString(),
                        rollNumber = roll,
                        name = name,
                        fatherName = father,
                        phone = phone,
                        className = targetClass,
                        section = "A",
                        groupName = group,
                        electiveSubjectsRaw = electives,
                        academicYearId = academicYearId,
                        ownerId = ownerId
                    )
                )
            }
        }

        val issues = NominalRollValidator.validateNominalRoll(students)
        Pair(students, issues)
    }

    suspend fun importStudents(students: List<StudentEntity>) = withContext(Dispatchers.IO) {
        studentDao.insertStudents(students)
    }

    // Full JSON Backup & Restore
    suspend fun exportDatabaseToJson(ownerId: String): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("ownerId", ownerId)

        val user = userDao.getCurrentUser()
        if (user != null) {
            val userObj = JSONObject().apply {
                put("userId", user.userId)
                put("name", user.name)
                put("email", user.email)
                put("college", user.college)
                put("designation", user.designation)
                put("defaultSubject", user.defaultSubject)
                put("smsTemplate", user.smsTemplate)
            }
            root.put("user", userObj)
        }

        val students = studentDao.getAllActiveStudentsFlow(ownerId).firstOrNull() ?: emptyList()
        val studentsArray = JSONArray()
        for (s in students) {
            studentsArray.put(JSONObject().apply {
                put("studentId", s.studentId)
                put("rollNumber", s.rollNumber)
                put("name", s.name)
                put("fatherName", s.fatherName)
                put("phone", s.phone)
                put("className", s.className)
                put("section", s.section)
                put("groupName", s.groupName)
                put("electiveSubjectsRaw", s.electiveSubjectsRaw)
                put("academicYearId", s.academicYearId)
                put("status", s.status)
            })
        }
        root.put("students", studentsArray)

        val classes = classDao.getClassesFlow(ownerId).firstOrNull() ?: emptyList()
        val classesArray = JSONArray()
        for (c in classes) {
            classesArray.put(JSONObject().apply {
                put("classId", c.classId)
                put("className", c.className)
                put("academicYearId", c.academicYearId)
                put("level", c.level)
                put("section", c.section)
                put("subjectId", c.subjectId)
                put("subjectName", c.subjectName)
                put("subjectType", c.subjectType)
                put("teacherName", c.teacherName)
            })
        }
        root.put("classes", classesArray)

        root.toString(2)
    }

    suspend fun restoreDatabaseFromJson(jsonString: String, ownerId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (root.has("students")) {
                val array = root.getJSONArray("students")
                val students = mutableListOf<StudentEntity>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    students.add(
                        StudentEntity(
                            studentId = obj.getString("studentId"),
                            rollNumber = obj.getString("rollNumber"),
                            name = obj.getString("name"),
                            fatherName = obj.getString("fatherName"),
                            phone = obj.getString("phone"),
                            className = obj.getString("className"),
                            section = obj.optString("section", "A"),
                            groupName = obj.optString("groupName", "Arts"),
                            electiveSubjectsRaw = obj.optString("electiveSubjectsRaw", ""),
                            academicYearId = obj.optString("academicYearId", InitialSeedData.DEFAULT_YEAR_ID),
                            ownerId = ownerId,
                            status = obj.optString("status", "Active")
                        )
                    )
                }
                studentDao.insertStudents(students)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
