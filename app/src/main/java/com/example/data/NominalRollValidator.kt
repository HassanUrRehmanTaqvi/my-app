package com.example.data

import com.example.data.model.StudentEntity

object NominalRollValidator {

    enum class IssueSeverity {
        WARNING,
        ERROR
    }

    enum class IssueType {
        DUPLICATE_ROLL_NUMBER,
        MISSING_ROLL_NUMBER,
        DUPLICATE_STUDENT_NAME,
        SAME_FATHER_NAME,
        DUPLICATE_PHONE,
        INVALID_PAKISTANI_PHONE,
        MISSING_FATHER_NAME,
        MISSING_PHONE,
        INCOMPLETE_SUBJECT_INFO
    }

    data class ValidationIssue(
        val type: IssueType,
        val severity: IssueSeverity,
        val title: String,
        val message: String,
        val studentRoll: String,
        val studentName: String,
        val studentId: String
    )

    fun validateNominalRoll(students: List<StudentEntity>): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()

        val rollMap = mutableMapOf<String, MutableList<StudentEntity>>()
        val nameMap = mutableMapOf<String, MutableList<StudentEntity>>()
        val fatherMap = mutableMapOf<String, MutableList<StudentEntity>>()
        val phoneMap = mutableMapOf<String, MutableList<StudentEntity>>()

        for (s in students) {
            val roll = s.rollNumber.trim()
            val name = s.name.trim().lowercase()
            val father = s.fatherName.trim().lowercase()
            val cleanPhone = s.phone.replace(Regex("[^0-9+]"), "")

            if (roll.isNotBlank()) {
                rollMap.getOrPut(roll) { mutableListOf() }.add(s)
            } else {
                issues.add(
                    ValidationIssue(
                        type = IssueType.MISSING_ROLL_NUMBER,
                        severity = IssueSeverity.ERROR,
                        title = "Missing Roll Number",
                        message = "Student '${s.name}' has no assigned roll number.",
                        studentRoll = "—",
                        studentName = s.name,
                        studentId = s.studentId
                    )
                )
            }

            if (name.isNotBlank()) {
                nameMap.getOrPut(name) { mutableListOf() }.add(s)
            }

            if (father.isNotBlank()) {
                fatherMap.getOrPut(father) { mutableListOf() }.add(s)
            } else {
                issues.add(
                    ValidationIssue(
                        type = IssueType.MISSING_FATHER_NAME,
                        severity = IssueSeverity.WARNING,
                        title = "Missing Father Name",
                        message = "Father name is missing for roll number ${s.rollNumber}.",
                        studentRoll = s.rollNumber,
                        studentName = s.name,
                        studentId = s.studentId
                    )
                )
            }

            if (cleanPhone.isNotBlank()) {
                phoneMap.getOrPut(cleanPhone) { mutableListOf() }.add(s)

                // Validate Pakistani mobile format: 03XX XXXXXXX or +923XX XXXXXXX
                val isPakMobile = cleanPhone.matches(Regex("^(03[0-9]{9}|\\+923[0-9]{9}|923[0-9]{9})$"))
                if (!isPakMobile && cleanPhone.length < 10) {
                    issues.add(
                        ValidationIssue(
                            type = IssueType.INVALID_PAKISTANI_PHONE,
                            severity = IssueSeverity.WARNING,
                            title = "Invalid Phone Number Format",
                            message = "Phone '${s.phone}' does not match standard Pakistani format (03XX-XXXXXXX).",
                            studentRoll = s.rollNumber,
                            studentName = s.name,
                            studentId = s.studentId
                        )
                    )
                }
            } else {
                issues.add(
                    ValidationIssue(
                        type = IssueType.MISSING_PHONE,
                        severity = IssueSeverity.WARNING,
                        title = "Missing Parent Phone",
                        message = "Parent contact number is not provided for ${s.name} (Roll ${s.rollNumber}).",
                        studentRoll = s.rollNumber,
                        studentName = s.name,
                        studentId = s.studentId
                    )
                )
            }

            if (s.electiveSubjectsRaw.isBlank() && s.groupName.isBlank()) {
                issues.add(
                    ValidationIssue(
                        type = IssueType.INCOMPLETE_SUBJECT_INFO,
                        severity = IssueSeverity.WARNING,
                        title = "Incomplete Subject/Group Info",
                        message = "No elective subjects or academic group specified for ${s.name} (Roll ${s.rollNumber}).",
                        studentRoll = s.rollNumber,
                        studentName = s.name,
                        studentId = s.studentId
                    )
                )
            }
        }

        // Duplicate Roll Numbers Check
        rollMap.forEach { (roll, studentList) ->
            if (studentList.size > 1) {
                studentList.forEach { s ->
                    issues.add(
                        ValidationIssue(
                            type = IssueType.DUPLICATE_ROLL_NUMBER,
                            severity = IssueSeverity.ERROR,
                            title = "Duplicate Roll Number Detected — Review Required",
                            message = "Roll Number $roll is assigned to multiple students (${studentList.joinToString { it.name }}).",
                            studentRoll = roll,
                            studentName = s.name,
                            studentId = s.studentId
                        )
                    )
                }
            }
        }

        // Duplicate Student Names Warning
        nameMap.forEach { (name, studentList) ->
            if (studentList.size > 1) {
                studentList.forEach { s ->
                    issues.add(
                        ValidationIssue(
                            type = IssueType.DUPLICATE_STUDENT_NAME,
                            severity = IssueSeverity.WARNING,
                            title = "Duplicate Student Name",
                            message = "Name '${s.name}' appears ${studentList.size} times with different father names or roll numbers.",
                            studentRoll = s.rollNumber,
                            studentName = s.name,
                            studentId = s.studentId
                        )
                    )
                }
            }
        }

        // Duplicate Parent Phone Warning
        phoneMap.forEach { (phone, studentList) ->
            if (studentList.size > 1) {
                studentList.forEach { s ->
                    issues.add(
                        ValidationIssue(
                            type = IssueType.DUPLICATE_PHONE,
                            severity = IssueSeverity.WARNING,
                            title = "Shared Parent Phone",
                            message = "Phone number $phone is shared by ${studentList.size} students (${studentList.joinToString { "${it.name} [Roll ${it.rollNumber}]" }}).",
                            studentRoll = s.rollNumber,
                            studentName = s.name,
                            studentId = s.studentId
                        )
                    )
                }
            }
        }

        return issues
    }
}
