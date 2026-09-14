package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AcademicYearDao
import com.example.data.dao.AttendanceDao
import com.example.data.dao.AuditLogDao
import com.example.data.dao.ClassDao
import com.example.data.dao.ClassMembershipDao
import com.example.data.dao.MessageLogDao
import com.example.data.dao.StudentDao
import com.example.data.dao.SubjectDao
import com.example.data.dao.SyncQueueDao
import com.example.data.dao.TeacherDao
import com.example.data.dao.TestDao
import com.example.data.dao.UserDao
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

@Database(
    entities = [
        UserEntity::class,
        AcademicYearEntity::class,
        StudentEntity::class,
        SubjectEntity::class,
        ClassEntity::class,
        ClassMembershipEntity::class,
        AttendanceSessionEntity::class,
        AttendanceRecordEntity::class,
        TestEntity::class,
        TestResultEntity::class,
        MessageLogEntity::class,
        AuditLogEntity::class,
        SyncQueueEntity::class,
        TeacherEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun academicYearDao(): AcademicYearDao
    abstract fun studentDao(): StudentDao
    abstract fun subjectDao(): SubjectDao
    abstract fun classDao(): ClassDao
    abstract fun classMembershipDao(): ClassMembershipDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun testDao(): TestDao
    abstract fun messageLogDao(): MessageLogDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun teacherDao(): TeacherDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE classes ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `teachers` (`teacherId` TEXT NOT NULL, `name` TEXT NOT NULL, `designation` TEXT NOT NULL, `department` TEXT NOT NULL, `role` TEXT NOT NULL, `isRegistered` TEXT NOT NULL, `registeredEmail` TEXT NOT NULL, PRIMARY KEY(`teacherId`))")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_teachers_teacherId` ON `teachers` (`teacherId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_teachers_role` ON `teachers` (`role`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_teachers_department` ON `teachers` (`department`)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `teachers` (`teacherId` TEXT NOT NULL, `name` TEXT NOT NULL, `designation` TEXT NOT NULL, `department` TEXT NOT NULL, `role` TEXT NOT NULL, `isRegistered` TEXT NOT NULL, `registeredEmail` TEXT NOT NULL, PRIMARY KEY(`teacherId`))")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_teachers_teacherId` ON `teachers` (`teacherId`)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Migrate class_memberships
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `class_memberships_new` (
                        `membershipId` TEXT NOT NULL,
                        `classId` TEXT NOT NULL,
                        `studentId` TEXT NOT NULL,
                        `startDate` INTEGER NOT NULL,
                        `endDate` INTEGER,
                        `status` TEXT NOT NULL,
                        `transferReason` TEXT,
                        PRIMARY KEY(`membershipId`),
                        FOREIGN KEY(`classId`) REFERENCES `classes`(`classId`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`studentId`) REFERENCES `students`(`studentId`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO `class_memberships_new` (`membershipId`, `classId`, `studentId`, `startDate`, `endDate`, `status`, `transferReason`)
                    SELECT `membershipId`, `classId`, `studentId`, `startDate`, `endDate`, `status`, `transferReason`
                    FROM `class_memberships`
                """.trimIndent())
                db.execSQL("DROP TABLE `class_memberships`")
                db.execSQL("ALTER TABLE `class_memberships_new` RENAME TO `class_memberships`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_class_memberships_classId_studentId` ON `class_memberships` (`classId`, `studentId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_class_memberships_classId` ON `class_memberships` (`classId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_class_memberships_studentId` ON `class_memberships` (`studentId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_class_memberships_status` ON `class_memberships` (`status`)")

                // 2. Migrate attendance_sessions
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `attendance_sessions_new` (
                        `sessionId` TEXT NOT NULL,
                        `classId` TEXT NOT NULL,
                        `academicYearId` TEXT NOT NULL,
                        `subjectId` TEXT NOT NULL,
                        `date` TEXT NOT NULL,
                        `periodCount` INTEGER NOT NULL,
                        `ownerId` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `notes` TEXT NOT NULL,
                        PRIMARY KEY(`sessionId`),
                        FOREIGN KEY(`classId`) REFERENCES `classes`(`classId`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT OR REPLACE INTO `attendance_sessions_new` (`sessionId`, `classId`, `academicYearId`, `subjectId`, `date`, `periodCount`, `ownerId`, `timestamp`, `notes`)
                    SELECT `sessionId`, `classId`, `academicYearId`, `subjectId`, `date`, `periodCount`, `ownerId`, `timestamp`, `notes`
                    FROM `attendance_sessions`
                    GROUP BY `classId`, `date`
                """.trimIndent())
                db.execSQL("DROP TABLE `attendance_sessions`")
                db.execSQL("ALTER TABLE `attendance_sessions_new` RENAME TO `attendance_sessions`")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_attendance_sessions_classId_date` ON `attendance_sessions` (`classId`, `date`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_attendance_sessions_classId` ON `attendance_sessions` (`classId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_attendance_sessions_ownerId` ON `attendance_sessions` (`ownerId`)")

                // 3. Migrate attendance_records
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `attendance_records_new` (
                        `recordId` TEXT NOT NULL,
                        `sessionId` TEXT NOT NULL,
                        `studentId` TEXT NOT NULL,
                        `period` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        PRIMARY KEY(`recordId`),
                        FOREIGN KEY(`sessionId`) REFERENCES `attendance_sessions`(`sessionId`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`studentId`) REFERENCES `students`(`studentId`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT OR REPLACE INTO `attendance_records_new` (`recordId`, `sessionId`, `studentId`, `period`, `status`)
                    SELECT `recordId`, `sessionId`, `studentId`, `period`, `status`
                    FROM `attendance_records`
                    WHERE `sessionId` IN (SELECT `sessionId` FROM `attendance_sessions`)
                """.trimIndent())
                db.execSQL("DROP TABLE `attendance_records`")
                db.execSQL("ALTER TABLE `attendance_records_new` RENAME TO `attendance_records`")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_attendance_records_sessionId_studentId_period` ON `attendance_records` (`sessionId`, `studentId`, `period`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_attendance_records_sessionId` ON `attendance_records` (`sessionId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_attendance_records_studentId` ON `attendance_records` (`studentId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_attendance_records_status` ON `attendance_records` (`status`)")

                // 4. Migrate tests
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `tests_new` (
                        `testId` TEXT NOT NULL,
                        `classId` TEXT NOT NULL,
                        `subjectId` TEXT NOT NULL,
                        `testName` TEXT NOT NULL,
                        `date` TEXT NOT NULL,
                        `totalMarks` REAL NOT NULL,
                        `ownerId` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`testId`),
                        FOREIGN KEY(`classId`) REFERENCES `classes`(`classId`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO `tests_new` (`testId`, `classId`, `subjectId`, `testName`, `date`, `totalMarks`, `ownerId`, `createdAt`)
                    SELECT `testId`, `classId`, `subjectId`, `testName`, `date`, `totalMarks`, `ownerId`, `createdAt`
                    FROM `tests`
                """.trimIndent())
                db.execSQL("DROP TABLE `tests`")
                db.execSQL("ALTER TABLE `tests_new` RENAME TO `tests`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tests_classId_date` ON `tests` (`classId`, `date`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tests_classId` ON `tests` (`classId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tests_ownerId` ON `tests` (`ownerId`)")

                // 5. Migrate test_results
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `test_results_new` (
                        `resultId` TEXT NOT NULL,
                        `testId` TEXT NOT NULL,
                        `studentId` TEXT NOT NULL,
                        `obtainedMarks` REAL NOT NULL,
                        `percentage` REAL NOT NULL,
                        `status` TEXT NOT NULL,
                        `grade` TEXT NOT NULL,
                        `rank` INTEGER NOT NULL,
                        PRIMARY KEY(`resultId`),
                        FOREIGN KEY(`testId`) REFERENCES `tests`(`testId`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`studentId`) REFERENCES `students`(`studentId`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT OR REPLACE INTO `test_results_new` (`resultId`, `testId`, `studentId`, `obtainedMarks`, `percentage`, `status`, `grade`, `rank`)
                    SELECT `resultId`, `testId`, `studentId`, `obtainedMarks`, `percentage`, `status`, `grade`, `rank`
                    FROM `test_results`
                    WHERE `testId` IN (SELECT `testId` FROM `tests`)
                """.trimIndent())
                db.execSQL("DROP TABLE `test_results`")
                db.execSQL("ALTER TABLE `test_results_new` RENAME TO `test_results`")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_test_results_testId_studentId` ON `test_results` (`testId`, `studentId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_test_results_testId` ON `test_results` (`testId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_test_results_studentId` ON `test_results` (`studentId`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "college_academic_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
