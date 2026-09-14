package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.InitialSeedData
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.AttendanceSessionEntity
import com.example.data.model.ClassEntity
import com.example.data.model.ClassMembershipEntity
import com.example.data.model.StudentEntity
import com.example.data.model.UserEntity
import com.example.repository.CollegeRepository
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AttendanceAndBackupTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: CollegeRepository
    private val testOwnerId = "test_teacher_01"

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = CollegeRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testSaveAttendanceSessionAndEdit() = runBlocking {
        // 1. Seed user, students, and class
        val user = UserEntity(
            userId = testOwnerId,
            name = "Test Professor",
            email = "prof@college.edu",
            college = "Govt Associate College Makhdoom Rashid",
            designation = "Assistant Professor",
            defaultSubject = "Biology",
            smsTemplate = "Dear Parent, your child is absent.",
            role = "teacher"
        )
        db.userDao().insertUser(user)

        val student1 = StudentEntity(
            studentId = "s1",
            rollNumber = "101",
            name = "Ali Ahmad",
            fatherName = "Ahmad Khan",
            phone = "03001111111",
            className = "First Year",
            section = "A",
            groupName = "Pre-Medical",
            electiveSubjectsRaw = "Biology, Chemistry, Physics",
            academicYearId = InitialSeedData.DEFAULT_YEAR_ID,
            ownerId = testOwnerId
        )
        val student2 = StudentEntity(
            studentId = "s2",
            rollNumber = "102",
            name = "Bilal Raza",
            fatherName = "Raza Ali",
            phone = "03002222222",
            className = "First Year",
            section = "A",
            groupName = "Pre-Medical",
            electiveSubjectsRaw = "Biology, Chemistry, Physics",
            academicYearId = InitialSeedData.DEFAULT_YEAR_ID,
            ownerId = testOwnerId
        )
        db.studentDao().insertStudents(listOf(student1, student2))

        val classId = "class_bio_1"
        val classEntity = ClassEntity(
            classId = classId,
            className = "First Year Biology Sec A",
            academicYearId = InitialSeedData.DEFAULT_YEAR_ID,
            level = "First Year",
            section = "A",
            subjectId = "sub_bio",
            subjectName = "Biology",
            subjectType = "Compulsory",
            teacherName = "Test Professor",
            ownerId = testOwnerId
        )
        db.classDao().insertClass(classEntity)

        // 2. Save initial attendance: Student 1 Present, Student 2 Absent (1 period)
        val initialStatuses = mapOf("s1" to "Present", "s2" to "Absent")
        val sessionId = repository.saveAttendanceSession(
            classId = classId,
            academicYearId = InitialSeedData.DEFAULT_YEAR_ID,
            subjectId = "sub_bio",
            date = "2026-10-01",
            periodCount = 1,
            ownerId = testOwnerId,
            notes = "First lecture",
            studentStatuses = initialStatuses
        )

        assertNotNull(sessionId)

        // Verify session saved
        val existing = repository.findExistingSession(classId, "2026-10-01")
        assertNotNull(existing)
        assertEquals(sessionId, existing!!.sessionId)
        assertEquals(1, existing.periodCount)
        assertEquals("First lecture", existing.notes)

        // Verify records
        val records = repository.getRecordsForSessionDirect(sessionId)
        assertEquals(2, records.size)
        val s1Rec = records.find { it.studentId == "s1" }
        val s2Rec = records.find { it.studentId == "s2" }
        assertEquals("Present", s1Rec?.status)
        assertEquals("Absent", s2Rec?.status)

        // 3. Edit attendance on the same date: Student 2 marked Present, periodCount changed to 2
        val editedStatuses = mapOf("s1" to "Present", "s2" to "Present")
        val updatedSessionId = repository.saveAttendanceSession(
            classId = classId,
            academicYearId = InitialSeedData.DEFAULT_YEAR_ID,
            subjectId = "sub_bio",
            date = "2026-10-01",
            periodCount = 2,
            ownerId = testOwnerId,
            notes = "Double lecture updated",
            studentStatuses = editedStatuses,
            existingSessionId = sessionId
        )

        assertEquals(sessionId, updatedSessionId)

        // Verify no duplicate session was created
        val allSessions = repository.getSessionsForClassDirect(classId)
        assertEquals(1, allSessions.size)
        assertEquals(2, allSessions.first().periodCount)
        assertEquals("Double lecture updated", allSessions.first().notes)

        // Verify records: each student has 2 records (one for period 1, one for period 2)
        val updatedRecords = repository.getRecordsForSessionDirect(sessionId)
        assertEquals(4, updatedRecords.size)
        assertTrue(updatedRecords.all { it.status == "Present" })
    }

    @Test
    fun testExportAndRestoreDatabase() = runBlocking {
        // 1. Prepare sample data across entities
        val user = UserEntity(
            userId = testOwnerId,
            name = "Test Teacher",
            email = "teacher@test.pk",
            college = "Govt Associate College Makhdoom Rashid",
            designation = "Lecturer",
            defaultSubject = "Computer Science",
            smsTemplate = "Dear Parent, student was absent.",
            role = "teacher"
        )
        db.userDao().insertUser(user)

        val student = StudentEntity(
            studentId = "s_100",
            rollNumber = "500",
            name = "Hamza Farooq",
            fatherName = "Farooq Ahmad",
            phone = "03003333333",
            className = "Second Year",
            section = "B",
            groupName = "ICS",
            electiveSubjectsRaw = "Computer Science, Physics, Math",
            marks = 950,
            academicYearId = InitialSeedData.DEFAULT_YEAR_ID,
            ownerId = testOwnerId
        )
        db.studentDao().insertStudent(student)

        val classId = "class_cs_2"
        val classEntity = ClassEntity(
            classId = classId,
            className = "Second Year Computer Science",
            academicYearId = InitialSeedData.DEFAULT_YEAR_ID,
            level = "Second Year",
            section = "B",
            subjectId = "sub_cs",
            subjectName = "Computer Science",
            subjectType = "Elective",
            teacherName = "Test Teacher",
            ownerId = testOwnerId
        )
        db.classDao().insertClass(classEntity)

        val membership = ClassMembershipEntity(
            membershipId = "mem_1",
            classId = classId,
            studentId = "s_100",
            startDate = System.currentTimeMillis(),
            status = "Active"
        )
        db.classMembershipDao().insertMembership(membership)

        val session = AttendanceSessionEntity(
            sessionId = "sess_1",
            classId = classId,
            academicYearId = InitialSeedData.DEFAULT_YEAR_ID,
            subjectId = "sub_cs",
            date = "2026-10-15",
            periodCount = 2,
            notes = "Lab class",
            ownerId = testOwnerId
        )
        db.attendanceDao().insertSession(session)

        val record1 = AttendanceRecordEntity(
            recordId = "rec_1",
            sessionId = "sess_1",
            studentId = "s_100",
            status = "Present",
            period = 1
        )
        val record2 = AttendanceRecordEntity(
            recordId = "rec_2",
            sessionId = "sess_1",
            studentId = "s_100",
            status = "Present",
            period = 2
        )
        db.attendanceDao().insertRecords(listOf(record1, record2))

        // 2. Export database to JSON
        val exportedJson = repository.exportDatabaseToJson(testOwnerId)
        assertTrue(exportedJson.isNotEmpty())

        val jsonObj = JSONObject(exportedJson)
        assertTrue(jsonObj.has("metadata"))
        assertTrue(jsonObj.has("students"))
        assertTrue(jsonObj.has("classes"))
        assertTrue(jsonObj.has("attendanceSessions"))
        assertTrue(jsonObj.has("attendanceRecords"))

        val studentsArray = jsonObj.getJSONArray("students")
        assertEquals(1, studentsArray.length())
        assertEquals("Hamza Farooq", studentsArray.getJSONObject(0).getString("name"))

        // 3. Clear database by deleting class (cascades memberships, sessions, records)
        db.classDao().deleteClass(classId)
        db.studentDao().archiveStudent("s_100")

        // Confirm database has no active students or sessions
        val emptyStudents = db.studentDao().getAllStudents().filter { it.status == "Active" }
        val emptySessions = db.attendanceDao().getAllSessions()
        assertEquals(0, emptyStudents.size)
        assertEquals(0, emptySessions.size)

        // 4. Restore database from JSON
        val restoreSuccess = repository.restoreDatabaseFromJson(exportedJson, testOwnerId)
        assertTrue(restoreSuccess)

        // 5. Verify restored data matches original state exactly
        val restoredStudents = db.studentDao().getAllStudents().filter { it.ownerId == testOwnerId }
        assertEquals(1, restoredStudents.size)
        val restoredStudent = restoredStudents.first()
        assertEquals("s_100", restoredStudent.studentId)
        assertEquals("500", restoredStudent.rollNumber)
        assertEquals("Hamza Farooq", restoredStudent.name)
        assertEquals(950, restoredStudent.marks)

        val restoredClasses = db.classDao().getAllClasses(testOwnerId)
        assertEquals(1, restoredClasses.size)
        assertEquals("Second Year Computer Science", restoredClasses.first().className)

        val restoredSessions = db.attendanceDao().getAllSessions()
        assertEquals(1, restoredSessions.size)
        assertEquals(2, restoredSessions.first().periodCount)
        assertEquals("Lab class", restoredSessions.first().notes)

        val restoredRecords = db.attendanceDao().getRecordsForSession("sess_1")
        assertEquals(2, restoredRecords.size)
        assertTrue(restoredRecords.all { it.status == "Present" })
    }

    @Test
    fun testSessionLevelCalculation() {
        val active = "2026–2028"
        assertEquals("First Year", com.example.util.SessionHelper.calculateLevelForSession("2026–2028", active))
        assertEquals("Second Year", com.example.util.SessionHelper.calculateLevelForSession("2025–2027", active))
        assertEquals("Completed", com.example.util.SessionHelper.calculateLevelForSession("2024–2026", active))
        assertEquals("Completed", com.example.util.SessionHelper.calculateLevelForSession("2023–2025", active))
        assertEquals("First Year", com.example.util.SessionHelper.calculateLevelForSession("2027–2029", active))

        // Hyphen vs en-dash tolerance
        assertEquals("First Year", com.example.util.SessionHelper.calculateLevelForSession("2026-2028", "2026-2028"))
        assertEquals("Second Year", com.example.util.SessionHelper.calculateLevelForSession("2025-2027", "2026-2028"))

        // Urdu level translation
        assertEquals("فرسٹ ایئر (سال اول)", com.example.util.SessionHelper.getLevelUrdu("First Year"))
        assertEquals("سیکنڈ ایئر (سال دوم)", com.example.util.SessionHelper.getLevelUrdu("Second Year"))
    }

    @Test
    fun testNominalRollDuplicateAndValidation() {
        val studentA = StudentEntity(
            studentId = "s_dup1",
            rollNumber = "105",
            name = "Usman Tariq",
            fatherName = "Tariq Mahmood",
            phone = "03001234567",
            className = "First Year",
            section = "A",
            groupName = "Pre-Medical",
            electiveSubjectsRaw = "Biology, Chemistry, Physics",
            academicYearId = InitialSeedData.DEFAULT_YEAR_ID,
            ownerId = testOwnerId
        )
        val studentB = StudentEntity(
            studentId = "s_dup2",
            rollNumber = "105", // Duplicate roll number!
            name = "Usman Tariq",
            fatherName = "Tariq Mahmood",
            phone = "03001234567",
            className = "First Year",
            section = "A",
            groupName = "Pre-Medical",
            electiveSubjectsRaw = "Biology, Chemistry, Physics",
            academicYearId = InitialSeedData.DEFAULT_YEAR_ID,
            ownerId = testOwnerId
        )

        val issues = com.example.data.NominalRollValidator.validateNominalRoll(listOf(studentA, studentB))
        assertTrue(issues.isNotEmpty())
        assertTrue(issues.any { it.type == com.example.data.NominalRollValidator.IssueType.DUPLICATE_ROLL_NUMBER })
    }

    @Test
    fun testCsvParserHelper() {
        val csvData = listOf(
            "Roll No,Student Name,Father Name,Session,Group,Optional Subjects,Phone",
            "201,محمد بلال,بلال حسین,2026–2028,ICS,Computer Science,03001122334",
            "202,علی حمزہ,حمزہ خان,2025–2027,Pre-Medical,Biology,03005566778"
        )
        val result = com.example.util.CsvParserHelper.parseCsvData(
            lines = csvData,
            existingStudents = emptyList(),
            activeSession = "2026–2028"
        )
        assertEquals(2, result.totalRows)
        assertEquals(2, result.validRows.size)
        assertEquals("First Year", result.validRows[0].className)
        assertEquals("Second Year", result.validRows[1].className)
    }

    @Test
    fun testParentAbsenceNotificationUrduFormatting() {
        val teacherRaw = "حسن الرحمن تقوی"
        val formattedTeacher = com.example.util.ParentNotificationHelper.formatTeacherName(teacherRaw)
        assertEquals("پروفیسر حسن الرحمن تقوی", formattedTeacher)

        val notification = com.example.util.ParentNotificationHelper.generateAbsenceNotification(
            studentName = "احمد رضا",
            rollNumber = "105",
            date = "2026-10-15",
            missedPeriodsToday = 2,
            monthlyPresent = 18,
            monthlyAbsent = 4,
            attendancePercentage = 81.8,
            consecutiveAbsentDays = 3,
            teacherName = "حسن الرحمن تقوی",
            department = "اسلامیات",
            collegeName = "گورنمنٹ ایسوسی ایٹ کالج مخدوم رشید ملتان"
        )

        assertTrue(notification.contains("احمد رضا"))
        assertTrue(notification.contains("105"))
        assertTrue(notification.contains("2026-10-15"))
        assertTrue(notification.contains("آج 2 پیریڈ میں غیر حاضری ہوئی ہے۔"))
        assertTrue(notification.contains("حاضری: 18 پیریڈ"))
        assertTrue(notification.contains("غیر حاضری: 4 پیریڈ"))
        assertTrue(notification.contains("81.8٪"))
        assertTrue(notification.contains("طالب علم گزشتہ 3 دن سے مسلسل غیر حاضر ہے۔"))
        assertTrue(notification.contains("پروفیسر حسن الرحمن تقوی"))
        assertTrue(notification.contains("شعبہ: اسلامیات"))
        assertTrue(notification.contains("گورنمنٹ ایسوسی ایٹ کالج مخدوم رشید ملتان"))
    }
}
