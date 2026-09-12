package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE isCurrent = 1 LIMIT 1")
    fun getCurrentUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isCurrent = 1 LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isCurrent = CASE WHEN userId = :userId THEN 1 ELSE 0 END")
    suspend fun switchActiveUser(userId: String)
}

@Dao
interface AcademicYearDao {
    @Query("SELECT * FROM academic_years WHERE ownerId = :ownerId ORDER BY name DESC")
    fun getYearsForOwner(ownerId: String): Flow<List<AcademicYearEntity>>

    @Query("SELECT * FROM academic_years WHERE ownerId = :ownerId AND isDefault = 1 LIMIT 1")
    suspend fun getDefaultYear(ownerId: String): AcademicYearEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertYear(year: AcademicYearEntity)
}

@Dao
interface StudentDao {
    @Query("SELECT * FROM students WHERE ownerId = :ownerId AND status != 'Archived' ORDER BY CAST(rollNumber AS INTEGER) ASC, rollNumber ASC")
    fun getAllActiveStudentsFlow(ownerId: String): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE ownerId = :ownerId AND className = :className AND status != 'Archived' ORDER BY CAST(rollNumber AS INTEGER) ASC, rollNumber ASC")
    fun getStudentsByClassFlow(ownerId: String, className: String): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE ownerId = :ownerId AND className = :className AND academicYearId = :academicYearId AND status != 'Archived' ORDER BY CAST(rollNumber AS INTEGER) ASC, rollNumber ASC")
    suspend fun getStudentsByClassAndYear(ownerId: String, className: String, academicYearId: String): List<StudentEntity>

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
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects WHERE active = 1 AND ownerId = :ownerId ORDER BY type DESC, name ASC")
    fun getSubjectsFlow(ownerId: String): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE ownerId = :ownerId")
    suspend fun getAllSubjects(ownerId: String): List<SubjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Update
    suspend fun updateSubject(subject: SubjectEntity)
}

@Dao
interface ClassDao {
    @Query("SELECT * FROM classes WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    fun getClassesFlow(ownerId: String): Flow<List<ClassEntity>>

    @Query("SELECT * FROM classes WHERE classId = :classId")
    suspend fun getClassById(classId: String): ClassEntity?

    @Query("SELECT * FROM classes WHERE classId = :classId")
    fun getClassFlow(classId: String): Flow<ClassEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(classEntity: ClassEntity)

    @Update
    suspend fun updateClass(classEntity: ClassEntity)

    @Query("DELETE FROM classes WHERE classId = :classId")
    suspend fun deleteClass(classId: String)
}

@Dao
interface ClassMembershipDao {
    @Query("SELECT * FROM class_memberships WHERE classId = :classId AND status = 'Active'")
    fun getActiveMembershipsFlow(classId: String): Flow<List<ClassMembershipEntity>>

    @Query("SELECT * FROM class_memberships WHERE classId = :classId AND status = 'Active'")
    suspend fun getActiveMemberships(classId: String): List<ClassMembershipEntity>

    @Query("SELECT * FROM class_memberships WHERE classId = :classId")
    suspend fun getAllMembershipsForClass(classId: String): List<ClassMembershipEntity>

    @Query("SELECT * FROM class_memberships WHERE studentId = :studentId")
    suspend fun getMembershipsForStudent(studentId: String): List<ClassMembershipEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemberships(memberships: List<ClassMembershipEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembership(membership: ClassMembershipEntity)

    @Query("UPDATE class_memberships SET status = :status, endDate = :endDate, transferReason = :reason WHERE membershipId = :membershipId")
    suspend fun updateMembershipStatus(membershipId: String, status: String, endDate: Long, reason: String?)

    @Query("UPDATE class_memberships SET status = 'Removed', endDate = :endDate, transferReason = :reason WHERE classId = :classId AND studentId = :studentId AND status = 'Active'")
    suspend fun removeStudentFromClass(classId: String, studentId: String, endDate: Long, reason: String?)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_sessions WHERE classId = :classId ORDER BY date DESC, timestamp DESC")
    fun getSessionsForClassFlow(classId: String): Flow<List<AttendanceSessionEntity>>

    @Query("SELECT * FROM attendance_sessions WHERE classId = :classId ORDER BY date DESC, timestamp DESC")
    suspend fun getSessionsForClass(classId: String): List<AttendanceSessionEntity>

    @Query("SELECT * FROM attendance_sessions WHERE ownerId = :ownerId ORDER BY date DESC, timestamp DESC")
    fun getAllSessionsFlow(ownerId: String): Flow<List<AttendanceSessionEntity>>

    @Query("SELECT * FROM attendance_sessions WHERE classId = :classId AND date = :date LIMIT 1")
    suspend fun findExistingSession(classId: String, date: String): AttendanceSessionEntity?

    @Query("SELECT * FROM attendance_sessions WHERE sessionId = :sessionId")
    suspend fun getSessionById(sessionId: String): AttendanceSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: AttendanceSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<AttendanceRecordEntity>)

    @Query("SELECT * FROM attendance_records WHERE sessionId = :sessionId")
    fun getRecordsForSessionFlow(sessionId: String): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE sessionId = :sessionId")
    suspend fun getRecordsForSession(sessionId: String): List<AttendanceRecordEntity>

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

    @Query("SELECT * FROM tests WHERE ownerId = :ownerId ORDER BY date DESC")
    fun getAllTestsFlow(ownerId: String): Flow<List<TestEntity>>

    @Query("SELECT * FROM tests WHERE testId = :testId")
    suspend fun getTestById(testId: String): TestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: TestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<TestResultEntity>)

    @Query("SELECT * FROM test_results WHERE testId = :testId")
    fun getResultsForTestFlow(testId: String): Flow<List<TestResultEntity>>

    @Query("SELECT * FROM test_results WHERE testId = :testId")
    suspend fun getResultsForTest(testId: String): List<TestResultEntity>

    @Query("SELECT * FROM test_results WHERE studentId = :studentId")
    suspend fun getResultsForStudent(studentId: String): List<TestResultEntity>
}

@Dao
interface MessageLogDao {
    @Query("SELECT * FROM message_logs WHERE ownerId = :ownerId ORDER BY timestamp DESC")
    fun getLogsFlow(ownerId: String): Flow<List<MessageLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MessageLogEntity)
}
