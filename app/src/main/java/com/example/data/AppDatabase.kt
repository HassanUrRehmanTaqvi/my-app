package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AcademicYearDao
import com.example.data.dao.AttendanceDao
import com.example.data.dao.ClassDao
import com.example.data.dao.ClassMembershipDao
import com.example.data.dao.MessageLogDao
import com.example.data.dao.StudentDao
import com.example.data.dao.SubjectDao
import com.example.data.dao.TestDao
import com.example.data.dao.UserDao
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
        MessageLogEntity::class
    ],
    version = 2,
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

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE classes ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "college_academic_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
