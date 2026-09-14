package com.example.data

import android.content.Context
import com.example.data.model.StudentEntity
import com.example.data.model.TeacherEntity
import org.json.JSONObject

data class CollegeMasterData(
    val institution: String,
    val teachers: List<TeacherEntity>,
    val firstYearStudents: List<StudentEntity>,
    val secondYearStudents: List<StudentEntity>,
    val allStudents: List<StudentEntity>
)

object CollegeDataLoader {

    private const val MASTER_JSON_FILE = "college_master_data.json"

    fun loadFromAssets(
        context: Context,
        ownerId: String = InitialSeedData.DEFAULT_USER_ID,
        yearId: String = InitialSeedData.DEFAULT_YEAR_ID
    ): CollegeMasterData {
        val jsonString = try {
            context.assets.open(MASTER_JSON_FILE).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            return fallbackData(ownerId, yearId)
        }

        return try {
            val root = JSONObject(jsonString)
            val institution = root.optString("institution", "گورنمنٹ ایسوسی ایٹ کالج مخدوم رشید ملتان")

            // Parse Teachers (25)
            val teachersList = mutableListOf<TeacherEntity>()
            val teachersArray = root.optJSONArray("teachers")
            if (teachersArray != null) {
                for (i in 0 until teachersArray.length()) {
                    val obj = teachersArray.getJSONObject(i)
                    teachersList.add(
                        TeacherEntity(
                            teacherId = obj.optString("teacher_id", "t_${i + 1}"),
                            name = obj.optString("name"),
                            designation = obj.optString("designation"),
                            department = obj.optString("department"),
                            role = obj.optString("role", "teacher"),
                            isRegistered = obj.optString("is_registered", "No"),
                            registeredEmail = obj.optString("registered_email", "")
                        )
                    )
                }
            }

            // Parse Students
            val firstYearList = mutableListOf<StudentEntity>()
            val secondYearList = mutableListOf<StudentEntity>()

            val studentsObj = root.optJSONObject("students")
            if (studentsObj != null) {
                // Second Year 2025-27
                val syObj = studentsObj.optJSONObject("second_year_2025_2027")
                if (syObj != null) {
                    val sySession = syObj.optString("session", "2025-2027").replace("-", "–")
                    val records = syObj.optJSONArray("records")
                    if (records != null) {
                        for (i in 0 until records.length()) {
                            val r = records.getJSONObject(i)
                            val roll = r.optInt("roll_no", i + 1)
                            val s1 = r.optString("subject_1", "")
                            val s2 = r.optString("subject_2", "")
                            val s3 = r.optString("subject_3", "")
                            val marks = if (r.has("marks") && !r.isNull("marks")) r.getInt("marks") else null
                            val electives = listOf(s1, s2, s3).filter { it.isNotBlank() }.joinToString(", ")

                            secondYearList.add(
                                StudentEntity(
                                    studentId = "sy_${roll}",
                                    rollNumber = roll.toString(),
                                    name = r.optString("name"),
                                    fatherName = r.optString("father_name"),
                                    phone = r.optString("contact_no"),
                                    guardianPhone = r.optString("contact_no"),
                                    session = sySession,
                                    className = "Second Year",
                                    section = "A",
                                    groupName = r.optString("group", "General"),
                                    electiveSubjectsRaw = electives,
                                    subject1 = s1,
                                    subject2 = s2,
                                    subject3 = s3,
                                    marks = marks,
                                    academicYearId = yearId,
                                    ownerId = ownerId
                                )
                            )
                        }
                    }
                }

                // First Year 2026-28
                val fyObj = studentsObj.optJSONObject("first_year_2026_2028")
                if (fyObj != null) {
                    val fySession = fyObj.optString("session", "2026-2028").replace("-", "–")
                    val records = fyObj.optJSONArray("records")
                    if (records != null) {
                        for (i in 0 until records.length()) {
                            val r = records.getJSONObject(i)
                            val roll = r.optInt("roll_no", i + 1)
                            val s1 = r.optString("subject_1", "")
                            val s2 = r.optString("subject_2", "")
                            val s3 = r.optString("subject_3", "")
                            val marks = if (r.has("marks") && !r.isNull("marks")) r.getInt("marks") else null
                            val electives = listOf(s1, s2, s3).filter { it.isNotBlank() }.joinToString(", ")

                            firstYearList.add(
                                StudentEntity(
                                    studentId = "fy_${roll}",
                                    rollNumber = roll.toString(),
                                    name = r.optString("name"),
                                    fatherName = r.optString("father_name"),
                                    phone = r.optString("contact_no"),
                                    guardianPhone = r.optString("contact_no"),
                                    session = fySession,
                                    className = "First Year",
                                    section = "A",
                                    groupName = r.optString("group", "General"),
                                    electiveSubjectsRaw = electives,
                                    subject1 = s1,
                                    subject2 = s2,
                                    subject3 = s3,
                                    marks = marks,
                                    academicYearId = yearId,
                                    ownerId = ownerId
                                )
                            )
                        }
                    }
                }
            }

            val allStudents = secondYearList + firstYearList
            CollegeMasterData(
                institution = institution,
                teachers = teachersList,
                firstYearStudents = firstYearList,
                secondYearStudents = secondYearList,
                allStudents = allStudents
            )
        } catch (e: Exception) {
            e.printStackTrace()
            fallbackData(ownerId, yearId)
        }
    }

    private fun fallbackData(ownerId: String, yearId: String): CollegeMasterData {
        val fy = InitialSeedData.getFirstYearNominalRoll(ownerId, yearId)
        val sy = InitialSeedData.getSecondYearNominalRoll(ownerId, yearId)
        return CollegeMasterData(
            institution = "گورنمنٹ ایسوسی ایٹ کالج مخدوم رشید ملتان",
            teachers = emptyList(),
            firstYearStudents = fy,
            secondYearStudents = sy,
            allStudents = fy + sy
        )
    }
}
