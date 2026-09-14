package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AcademicYearEntity
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
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE isCurrent = 1 LIMIT 1")
    fun getCurrentUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isCurrent = 1 LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isCurrent = CASE WHEN userId = :userId THEN 1 ELSE 0 END")
    suspend fun switchActiveUser(userId: String)
}

@Dao
interface AcademicYearDao {
    @Query("SELECT * FROM academic_years WHERE ownerId = :ownerId ORDER BY name DESC")
    fun getYearsForOwner(ownerId: String): Flow<List<AcademicYearEntity>>

    @Query("SELECT * FROM academic_years")
    suspend fun getAllYears(): List<AcademicYearEntity>

    @Query("SELECT * FROM academic_years WHERE ownerId = :ownerId AND isDefault = 1 LIMIT 1")
    suspend fun getDefaultYear(ownerId: String): AcademicYearEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertYear(year: AcademicYearEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertYears(years: List<AcademicYearEntity>)
}

@Dao
interface StudentDao {
    @Query("SELECT * FROM students WHERE ownerId = :ownerId AND status != 'Archived' ORDER BY CAST(rollNumber AS INTEGER) ASC, rollNumber ASC")
    fun getAllActiveStudentsFlow(ownerId: String): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE ownerId = :ownerId AND status = 'Archived' ORDER BY CAST(rollNumber AS INTEGER) ASC, rollNumber ASC")
    fun getArchivedStudentsFlow(ownerId: String): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE ownerId = :ownerId AND className = :className AND status != 'Archived' ORDER BY CAST(rollNumber AS INTEGER) ASC, rollNumber ASC")
    fun getStudentsByClassFlow(ownerId: String, className: String): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE ownerId = :ownerId AND className = :className AND academicYearId = :academicYearId AND status != 'Archived' ORDER BY CAST(rollNumber AS INTEGER) ASC, rollNumber ASC")
    suspend fun getStudentsByClassAndYear(ownerId: String, className: String, academicYearId: String): List<StudentEntity>

    @Query("SELECT * FROM students WHERE ownerId = :ownerId AND rollNumber = :rollNumber AND className = :className LIMIT 1")
    suspend fun findStudentByRollAndClass(ownerId: String, rollNumber: String, className: String): StudentEntity?

    @Query("SELECT * FROM students WHERE studentId = :studentId")
    suspend fun getStudentById(studentId: String): StudentEntity?

    @Query("SELECT * FROM students WHERE studentId IN (:studentIds)")
    suspend fun getStudentsByIds(studentIds: List<String>): List<StudentEntity>

    @Query("SELECT * FROM students WHERE studentId IN (:studentIds)")
    fun getStudentsByIdsFlow(studentIds: List<String>): Flow<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Query("UPDATE students SET status = 'Archived' WHERE studentId = :studentId")
    suspend fun archiveStudent(studentId: String)

    @Query("UPDATE students SET status = 'Active' WHERE studentId = :studentId")
    suspend fun restoreStudent(studentId: String)

    @Query("SELECT * FROM students WHERE ownerId = :ownerId AND session = :session AND status != 'Archived' ORDER BY CAST(rollNumber AS INTEGER) ASC, rollNumber ASC")
    fun getStudentsBySessionFlow(ownerId: String, session: String): Flow<List<StudentEntity>>

    @Query("SELECT DISTINCT session FROM students WHERE ownerId = :ownerId AND session != '' ORDER BY session DESC")
    fun getDistinctSessionsFlow(ownerId: String): Flow<List<String>>

    @Query("SELECT DISTINCT groupName FROM students WHERE ownerId = :ownerId AND groupName != '' ORDER BY groupName ASC")
    fun getDistinctGroupsFlow(ownerId: String): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM students")
    suspend fun getStudentCount(): Int

    @Query("SELECT * FROM students")
    suspend fun getAllStudents(): List<StudentEntity>

    @Query("SELECT * FROM students WHERE className = :className AND status != 'Archived' ORDER BY CAST(rollNumber AS INTEGER) ASC, rollNumber ASC")
    suspend fun getStudentsByClassName(className: String): List<StudentEntity>

    @Query("SELECT * FROM students WHERE className = :className AND groupName = :groupName AND status != 'Archived' ORDER BY CAST(rollNumber AS INTEGER) ASC, rollNumber ASC")
    suspend fun getStudentsByClassAndGroup(className: String, groupName: String): List<StudentEntity>
}

@Dao
interface TeacherDao {
    @Query("SELECT * FROM teachers ORDER BY CAST(SUBSTR(teacherId, 3) AS INTEGER) ASC, teacherId ASC")
    fun getAllTeachersFlow(): Flow<List<TeacherEntity>>

    @Query("SELECT * FROM teachers ORDER BY CAST(SUBSTR(teacherId, 3) AS INTEGER) ASC, teacherId ASC")
    suspend fun getAllTeachers(): List<TeacherEntity>

    @Query("SELECT * FROM teachers WHERE role = 'admin' ORDER BY teacherId ASC")
    fun getAdminTeachersFlow(): Flow<List<TeacherEntity>>

    @Query("SELECT * FROM teachers WHERE teacherId = :id LIMIT 1")
    suspend fun getTeacherById(id: String): TeacherEntity?

    @Query("SELECT * FROM teachers WHERE department = :dept ORDER BY CAST(SUBSTR(teacherId, 3) AS INTEGER) ASC")
    fun getTeachersByDepartmentFlow(dept: String): Flow<List<TeacherEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeachers(teachers: List<TeacherEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: TeacherEntity)

    @Update
    suspend fun updateTeacher(teacher: TeacherEntity)

    @Query("UPDATE teachers SET isRegistered = :isReg, registeredEmail = :email WHERE teacherId = :teacherId")
    suspend fun updateTeacherClaim(teacherId: String, isReg: String, email: String)

    @Query("SELECT COUNT(*) FROM teachers")
    suspend fun getTeacherCount(): Int
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects WHERE active = 1 AND ownerId = :ownerId ORDER BY type DESC, name ASC")
    fun getSubjectsFlow(ownerId: String): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE ownerId = :ownerId")
    suspend fun getAllSubjects(ownerId: String): List<SubjectEntity>

    @Query("SELECT * FROM subjects")
    suspend fun getAllSubjectsDirect(): List<SubjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Update
    suspend fun updateSubject(subject: SubjectEntity)
}

@Dao
interface ClassDao {
    @Query("SELECT * FROM classes WHERE ownerId = :ownerId AND isArchived = 0 ORDER BY createdAt DESC")
    fun getClassesFlow(ownerId: String): Flow<List<ClassEntity>>

    @Query("SELECT * FROM classes WHERE ownerId = :ownerId AND isArchived = 1 ORDER BY createdAt DESC")
    fun getArchivedClassesFlow(ownerId: String): Flow<List<ClassEntity>>

    @Query("SELECT * FROM classes WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    fun getAllClassesFlow(ownerId: String): Flow<List<ClassEntity>>

    @Query("SELECT * FROM classes WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    suspend fun getAllClasses(ownerId: String): List<ClassEntity>

    @Query("SELECT * FROM classes ORDER BY createdAt DESC")
    suspend fun getAllClassesDirect(): List<ClassEntity>

    @Query("SELECT * FROM classes WHERE classId = :classId")
    suspend fun getClassById(classId: String): ClassEntity?

    @Query("SELECT * FROM classes WHERE classId = :classId")
    fun getClassFlow(classId: String): Flow<ClassEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(classEntity: ClassEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClasses(classes: List<ClassEntity>)

    @Update
    suspend fun updateClass(classEntity: ClassEntity)

    @Query("UPDATE classes SET isArchived = 1 WHERE classId = :classId")
    suspend fun archiveClass(classId: String)

    @Query("UPDATE classes SET isArchived = 0 WHERE classId = :classId")
    suspend fun restoreClass(classId: String)

    @Query("DELETE FROM classes WHERE classId = :classId")
    suspend fun deleteClass(classId: String)
}

@Dao
interface ClassMembershipDao {
    @Query("SELECT * FROM class_memberships WHERE classId = :classId AND status = 'Active'")
    fun getActiveMembershipsFlow(classId: String): Flow<List<ClassMembershipEntity>>

    @Query("SELECT * FROM class_memberships WHERE classId = :classId AND status = 'Active'")
    suspend fun getActiveMemberships(classId: String): List<ClassMembershipEntity>

    @Query("SELECT COUNT(*) FROM class_memberships WHERE classId = :classId AND status = 'Active'")
    suspend fun getActiveMembershipCount(classId: String): Int

    @Query("SELECT * FROM class_memberships WHERE classId = :classId")
    suspend fun getAllMembershipsForClass(classId: String): List<ClassMembershipEntity>

    @Query("SELECT * FROM class_memberships WHERE studentId = :studentId")
    suspend fun getMembershipsForStudent(studentId: String): List<ClassMembershipEntity>

    @Query("SELECT * FROM class_memberships")
    suspend fun getAllMemberships(): List<ClassMembershipEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemberships(memberships: List<ClassMembershipEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembership(membership: ClassMembershipEntity)

    @Query("UPDATE class_memberships SET status = :status, endDate = :endDate, transferReason = :reason WHERE membershipId = :membershipId")
    suspend fun updateMembershipStatus(membershipId: String, status: String, endDate: Long, reason: String?)

    @Query("UPDATE class_memberships SET status = 'Removed', endDate = :endDate, transferReason = :reason WHERE classId = :classId AND studentId = :studentId AND status = 'Active'")
    suspend fun removeStudentFromClass(classId: String, studentId: String, endDate: Long, reason: String?)

    @Query("DELETE FROM class_memberships WHERE classId = :classId")
    suspend fun deleteMembershipsForClass(classId: String)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_sessions WHERE classId = :classId ORDER BY date DESC, timestamp DESC")
    fun getSessionsForClassFlow(classId: String): Flow<List<AttendanceSessionEntity>>

    @Query("SELECT * FROM attendance_sessions WHERE classId = :classId ORDER BY date DESC, timestamp DESC")
    suspend fun getSessionsForClass(classId: String): List<AttendanceSessionEntity>

    @Query("SELECT COUNT(*) FROM attendance_sessions WHERE classId = :classId")
    suspend fun getSessionCountForClass(classId: String): Int

    @Query("SELECT COUNT(*) FROM attendance_records ar INNER JOIN attendance_sessions s ON ar.sessionId = s.sessionId WHERE s.classId = :classId")
    suspend fun getRecordCountForClass(classId: String): Int

    @Query("SELECT * FROM attendance_sessions WHERE ownerId = :ownerId ORDER BY date DESC, timestamp DESC")
    fun getAllSessionsFlow(ownerId: String): Flow<List<AttendanceSessionEntity>>

    @Query("SELECT * FROM attendance_sessions")
    suspend fun getAllSessions(): List<AttendanceSessionEntity>

    @Query("SELECT * FROM attendance_sessions WHERE classId = :classId AND date = :date LIMIT 1")
    suspend fun findExistingSession(classId: String, date: String): AttendanceSessionEntity?

    @Query("SELECT * FROM attendance_sessions WHERE sessionId = :sessionId")
    suspend fun getSessionById(sessionId: String): AttendanceSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: AttendanceSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<AttendanceSessionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<AttendanceRecordEntity>)

    @Query("SELECT * FROM attendance_records WHERE sessionId = :sessionId")
    fun getRecordsForSessionFlow(sessionId: String): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE sessionId = :sessionId")
    suspend fun getRecordsForSession(sessionId: String): List<AttendanceRecordEntity>

    @Query("SELECT * FROM attendance_records")
    suspend fun getAllRecords(): List<AttendanceRecordEntity>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId")
    suspend fun getRecordsForStudent(studentId: String): List<AttendanceRecordEntity>

    @Query("SELECT ar.* FROM attendance_records ar INNER JOIN attendance_sessions s ON ar.sessionId = s.sessionId WHERE s.classId = :classId")
    suspend fun getRecordsForClass(classId: String): List<AttendanceRecordEntity>

    @Query("SELECT * FROM attendance_sessions WHERE ownerId = :ownerId AND date = :date")
    suspend fun getSessionsForDate(ownerId: String, date: String): List<AttendanceSessionEntity>

    @Query("DELETE FROM attendance_records WHERE sessionId = :sessionId")
    suspend fun deleteRecordsForSession(sessionId: String)
}

@Dao
interface TestDao {
    @Query("SELECT * FROM tests WHERE classId = :classId ORDER BY date DESC")
    fun getTestsForClassFlow(classId: String): Flow<List<TestEntity>>

    @Query("SELECT COUNT(*) FROM tests WHERE classId = :classId")
    suspend fun getTestCountForClass(classId: String): Int

    @Query("SELECT * FROM tests WHERE ownerId = :ownerId ORDER BY date DESC")
    fun getAllTestsFlow(ownerId: String): Flow<List<TestEntity>>

    @Query("SELECT * FROM tests")
    suspend fun getAllTests(): List<TestEntity>

    @Query("SELECT * FROM tests WHERE testId = :testId")
    suspend fun getTestById(testId: String): TestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: TestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTests(tests: List<TestEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<TestResultEntity>)

    @Query("SELECT * FROM test_results WHERE testId = :testId")
    fun getResultsForTestFlow(testId: String): Flow<List<TestResultEntity>>

    @Query("SELECT * FROM test_results WHERE testId = :testId")
    suspend fun getResultsForTest(testId: String): List<TestResultEntity>

    @Query("SELECT * FROM test_results")
    suspend fun getAllResults(): List<TestResultEntity>

    @Query("SELECT * FROM test_results WHERE studentId = :studentId")
    suspend fun getResultsForStudent(studentId: String): List<TestResultEntity>
}

@Dao
interface MessageLogDao {
    @Query("SELECT * FROM message_logs WHERE ownerId = :ownerId ORDER BY timestamp DESC")
    fun getLogsFlow(ownerId: String): Flow<List<MessageLogEntity>>

    @Query("SELECT * FROM message_logs")
    suspend fun getAllLogs(): List<MessageLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MessageLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<MessageLogEntity>)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs WHERE ownerId = :ownerId ORDER BY timestamp DESC LIMIT 100")
    fun getAuditLogsFlow(ownerId: String): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs")
    suspend fun getAllAuditLogs(): List<AuditLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLogs(logs: List<AuditLogEntity>)

    @Query("DELETE FROM audit_logs WHERE ownerId = :ownerId")
    suspend fun clearAuditLogs(ownerId: String)
}

@Dao
interface SyncQueueDao {
    @Query("SELECT COUNT(*) FROM sync_queue WHERE ownerId = :ownerId AND status = 'Pending'")
    fun getPendingCountFlow(ownerId: String): Flow<Int>

    @Query("SELECT * FROM sync_queue WHERE ownerId = :ownerId AND status = 'Pending' ORDER BY timestamp ASC")
    suspend fun getPendingItems(ownerId: String): List<SyncQueueEntity>

    @Query("SELECT * FROM sync_queue")
    suspend fun getAllSyncQueue(): List<SyncQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncItem(item: SyncQueueEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncQueue(items: List<SyncQueueEntity>)

    @Query("UPDATE sync_queue SET status = 'Synced' WHERE ownerId = :ownerId AND status = 'Pending'")
    suspend fun markAllSynced(ownerId: String)

    @Query("DELETE FROM sync_queue WHERE ownerId = :ownerId AND status = 'Synced'")
    suspend fun clearSyncedItems(ownerId: String)
}

