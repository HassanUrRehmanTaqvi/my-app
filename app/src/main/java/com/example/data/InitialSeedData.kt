package com.example.data

import com.example.data.model.AcademicYearEntity
import com.example.data.model.ClassEntity
import com.example.data.model.ClassMembershipEntity
import com.example.data.model.StudentEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.UserEntity
import java.util.UUID

object InitialSeedData {

    const val DEFAULT_USER_ID = "prof_taqvi_001"
    const val DEFAULT_YEAR_ID = "year_2026_27"

    val defaultUser = UserEntity(
        userId = DEFAULT_USER_ID,
        name = "پروفیسر حسن الرحمن تقوی",
        email = "ttaqvi@gmail.com",
        college = "گورنمنٹ ایسوسی ایٹ کالج مخدوم رشید ملتان",
        designation = "لیکچرار اسلامیات",
        defaultSubject = "اسلامیات لازمی",
        smsTemplate = "محترم والدِ گرامی!\n\nآپ کے بچے {student_name}، رول نمبر {roll_number} کی آج {date} کو کلاس میں غیر حاضری ریکارڈ کی گئی ہے۔\n\nآج کے {missed_periods} پیریڈز کا تعلیمی نقصان ہوا ہے۔ بچے کی مسلسل حاضری اس کی تعلیمی کامیابی کے لیے نہایت اہم ہے۔ براہِ کرم اس کی حاضری پر خصوصی توجہ فرمائیں تاکہ تعلیمی سلسلہ متاثر نہ ہو۔\n\nآپ کی توجہ اور تعاون آپ کے بچے کے بہتر مستقبل کی بنیاد ہے۔\n\nشکریہ\n\n{teacher_name}\n{designation}\n{college_name}",
        isCurrent = true
    )

    val defaultAcademicYear = AcademicYearEntity(
        yearId = DEFAULT_YEAR_ID,
        name = "2026–27",
        ownerId = DEFAULT_USER_ID,
        isDefault = true
    )

    fun getDefaultSubjects(ownerId: String = DEFAULT_USER_ID): List<SubjectEntity> {
        return SubjectNormalizer.DEFAULT_SUBJECTS.map { s ->
            SubjectEntity(
                subjectId = s.standardId,
                name = s.standardName,
                urduName = s.urduName,
                englishName = s.standardName,
                type = s.type,
                academicLevel = if (s.standardId == "comp_islamiat") "First Year" else if (s.standardId == "comp_pak_studies") "Second Year" else "Both",
                groupEligibility = "All",
                aliases = s.aliases.joinToString(", "),
                active = true,
                ownerId = ownerId
            )
        }
    }

    // First Year (11th) Nominal Roll — Govt Associate College Makhdoom Rashid Multan
    fun getFirstYearNominalRoll(ownerId: String = DEFAULT_USER_ID, yearId: String = DEFAULT_YEAR_ID): List<StudentEntity> {
        return listOf(
            StudentEntity(
                studentId = "fy_std_001",
                rollNumber = "1",
                name = "محمد علی رضا",
                fatherName = "محمد اختر رضوی",
                phone = "0300-7382910",
                className = "First Year",
                section = "A",
                groupName = "Arts",
                electiveSubjectsRaw = "Psychology, Civics, Islamic Studies Elective",
                academicYearId = yearId,
                ownerId = ownerId
            ),
            StudentEntity(
                studentId = "fy_std_002",
                rollNumber = "2",
                name = "محمد حمزہ",
                fatherName = "عبدالرشید خان",
                phone = "0301-6492019",
                className = "First Year",
                section = "A",
                groupName = "ICS Physics",
                electiveSubjectsRaw = "Computer Science, Physics, Mathematics",
                academicYearId = yearId,
                ownerId = ownerId
            ),
            StudentEntity(
                studentId = "fy_std_003",
                rollNumber = "3",
                name = "عثمان غنی",
                fatherName = "محمد صدیق",
                phone = "0321-8291024",
                className = "First Year",
                section = "A",
                groupName = "Arts Psychology",
                electiveSubjectsRaw = "Psychology, Economics, Arabic",
                academicYearId = yearId,
                ownerId = ownerId
            ),
            StudentEntity(
                studentId = "fy_std_004",
                rollNumber = "4",
                name = "بلال احمد",
                fatherName = "طاہر محمود",
                phone = "0333-6172839",
                className = "First Year",
                section = "A",
                groupName = "ICS Economics",
                electiveSubjectsRaw = "Computer Science, Economics, Mathematics",
                academicYearId = yearId,
                ownerId = ownerId
            ),
            StudentEntity(
                studentId = "fy_std_005",
                rollNumber = "5",
                name = "احمد حسن",
                fatherName = "غلام شبیر",
                phone = "0306-7281920",
                className = "First Year",
                section = "B",
                groupName = "Arts",
                electiveSubjectsRaw = "Sociology, Civics, History of Islam",
                academicYearId = yearId,
                ownerId = ownerId
            ),
            StudentEntity(
                studentId = "fy_std_006",
                rollNumber = "6",
                name = "سعد بن طارق",
                fatherName = "طارق عزیز",
                phone = "0302-8392018",
                className = "First Year",
                section = "B",
                groupName = "Arts Psychology",
                electiveSubjectsRaw = "Psychology, Education, Islamic Studies Elective",
                academicYearId = yearId,
                ownerId = ownerId
            ),
            StudentEntity(
                studentId = "fy_std_007",
                rollNumber = "7",
                name = "حذیفہ رحمن",
                fatherName = "عبدالرحمن تقوی",
                phone = "0307-8492011",
                className = "First Year",
                section = "B",
                groupName = "ICS Statistics",
                electiveSubjectsRaw = "Computer Science, Statistics, Mathematics",
                academicYearId = yearId,
                ownerId = ownerId
            ),
            StudentEntity(
                studentId = "fy_std_008",
                rollNumber = "8",
                name = "فیصل جاوید",
                fatherName = "محمد جاوید اقبال",
                phone = "0345-7281923",
                className = "First Year",
                section = "B",
                groupName = "Arts",
                electiveSubjectsRaw = "Economics, Civics, Education",
                academicYearId = yearId,
                ownerId = ownerId
            ),
            StudentEntity(
                studentId = "fy_std_009",
                rollNumber = "9",
                name = "عبداللہ ارشد",
                fatherName = "محمد ارشد بھٹہ",
                phone = "0308-6172834",
                className = "First Year",
                section = "B",
                groupName = "Arts Psychology",
                electiveSubjectsRaw = "Psychology, Economics, Sociology",
                academicYearId = yearId,
                ownerId = ownerId
            ),
            StudentEntity(
                studentId = "fy_std_010",
                rollNumber = "10",
                name = "حسن مصطفیٰ",
                fatherName = "غلام مصطفیٰ ملک",
                phone = "0312-7382915",
                className = "First Year",
                section = "B",
                groupName = "Pre-Medical",
                electiveSubjectsRaw = "Biology, Physics, Chemistry",
                academicYearId = yearId,
                ownerId = ownerId
            ),
            StudentEntity(
                studentId = "fy_std_011",
                rollNumber = "11",
                name = "طاہر رشید",
                fatherName = "عبدالرشید انصاری",
                phone = "0303-9182736",
                className = "First Year",
                section = "B",
                groupName = "IT Arts",
                electiveSubjectsRaw = "Computer Science, Education, Arabic",
                academicYearId = yearId,
                ownerId = ownerId
            ),
            StudentEntity(
                studentId = "fy_std_012",
                rollNumber = "12",
                name = "محمد زین",
                fatherName = "محمد سلیم راجپوت",
                phone = "0300-8291047",
                className = "First Year",
                section = "B",
                groupName = "Arts",
                electiveSubjectsRaw = "Psychology, Sociology, Civics",
                academicYearId = yearId,
                ownerId = ownerId
            ),
            StudentEntity(
                studentId = "fy_std_014",
                rollNumber = "14",
                name = "اویس قرنی",
                fatherName = "نور احمد",
                phone = "0322-7182930",
                className = "First Year",
                section = "B",
                groupName = "Arts",
                electiveSubjectsRaw = "Economics, Islamic Studies Elective, Civics",
                academicYearId = yearId,
                ownerId = ownerId
            ),
            StudentEntity(
                studentId = "fy_std_015",
                rollNumber = "15",
                name = "طلحہ بلال",
                fatherName = "بلال حسن قریشی",
                phone = "0334-6281928",
                className = "First Year",
                section = "B",
                groupName = "Pre-Engineering",
                electiveSubjectsRaw = "Mathematics, Physics, Chemistry",
                academicYearId = yearId,
                ownerId = ownerId
            ),
            StudentEntity(
                studentId = "fy_std_026",
                rollNumber = "26",
                name = "حماد اسلم",
                fatherName = "محمد اسلم گجر",
                phone = "0305-8392014",
                className = "First Year",
                section = "B",
                groupName = "Arts Psychology",
                electiveSubjectsRaw = "Psychology, Education, Arabic",
                academicYearId = yearId,
                ownerId = ownerId
            ),
            StudentEntity(
                studentId = "fy_std_038",
                rollNumber = "38",
                name = "رضوان حیدر",
                fatherName = "سید امداد حسین",
                phone = "0300-9281726",
                className = "First Year",
                section = "B",
                groupName = "Arts Psychology",
                electiveSubjectsRaw = "Psychology, Economics, Civics",
                academicYearId = yearId,
                ownerId = ownerId
            ),
            StudentEntity(
                studentId = "fy_std_040",
                rollNumber = "40",
                name = "شعیب اختر",
                fatherName = "محمد اختر کھوکھر",
                phone = "0346-6182937",
                className = "First Year",
                section = "B",
                groupName = "ICS Physics",
                electiveSubjectsRaw = "Computer Science, Physics, Mathematics",
                academicYearId = yearId,
                ownerId = ownerId
            ),
            StudentEntity(
                studentId = "fy_std_046",
                rollNumber = "46",
                name = "دانیال وسیم",
                fatherName = "وسیم اکرم شیخ",
                phone = "0301-8392017",
                className = "First Year",
                section = "B",
                groupName = "Arts",
                electiveSubjectsRaw = "Sociology, Education, History of Islam",
                academicYearId = yearId,
                ownerId = ownerId
            ),
            StudentEntity(
                studentId = "fy_std_049",
                rollNumber = "49",
                name = "یاسر نواز",
                fatherName = "محمد نواز سیال",
                phone = "0307-9281729",
                className = "First Year",
                section = "B",
                groupName = "Arts Psychology",
                electiveSubjectsRaw = "Psychology, Civics, Islamic Studies Elective",
                academicYearId = yearId,
                ownerId = ownerId
            ),
            StudentEntity(
                studentId = "fy_std_064",
                rollNumber = "64",
                name = "اسامہ بن خالد",
                fatherName = "خالد محمود ڈوگر",
                phone = "0331-7281928",
                className = "First Year",
                section = "B",
                groupName = "General Science",
                electiveSubjectsRaw = "Statistics, Economics, Mathematics",
                academicYearId = yearId,
                ownerId = ownerId
            )
        )
    }

    // Second Year (12th) Nominal Roll — Govt Associate College Makhdoom Rashid Multan
    fun getSecondYearNominalRoll(ownerId: String = DEFAULT_USER_ID, yearId: String = DEFAULT_YEAR_ID): List<StudentEntity> {
        return listOf(
            StudentEntity(
                studentId = "sy_std_101",
                rollNumber = "101",
                name = "محمد وقاص",
                fatherName = "عبدالغفور چوہدری",
                phone = "0300-8291038",
                className = "Second Year",
                section = "A",
                groupName = "Arts",
                electiveSubjectsRaw = "Psychology, Civics, Islamic Studies Elective",
                academicYearId = yearId,
                ownerId = ownerId
            ),
            StudentEntity(
                studentId = "sy_std_102",
                rollNumber = "102",
                name = "شہزاد اکرم",
                fatherName = "محمد اکرم آرائیں",
                phone = "0302-6172834",
                className = "Second Year",
                section = "A",
                groupName = "ICS Physics",
                electiveSubjectsRaw = "Computer Science, Physics, Mathematics",
                academicYearId = yearId,
                ownerId = ownerId
            ),
            StudentEntity(
                studentId = "sy_std_103",
                rollNumber = "103",
                name = "عاطف نواز",
                fatherName = "محمد نواز گل",
                phone = "0321-7281938",
                className = "Second Year",
                section = "A",
                groupName = "Arts",
                electiveSubjectsRaw = "Economics, Sociology, Arabic",
                academicYearId = yearId,
                ownerId = ownerId
            ),
            StudentEntity(
                studentId = "sy_std_104",
                rollNumber = "104",
                name = "ندیم اسلم",
                fatherName = "محمد اسلم رانا",
                phone = "0333-8291048",
                className = "Second Year",
                section = "A",
                groupName = "Pre-Medical",
                electiveSubjectsRaw = "Biology, Physics, Chemistry",
                academicYearId = yearId,
                ownerId = ownerId
            ),
            StudentEntity(
                studentId = "sy_std_105",
                rollNumber = "105",
                name = "کاشف جمیل",
                fatherName = "جمیل احمد ہاشمی",
                phone = "0345-6281938",
                className = "Second Year",
                section = "B",
                groupName = "Arts Psychology",
                electiveSubjectsRaw = "Psychology, Economics, Education",
                academicYearId = yearId,
                ownerId = ownerId
            ),
            StudentEntity(
                studentId = "sy_std_106",
                rollNumber = "106",
                name = "وقار یونس",
                fatherName = "محمد یونس قریشی",
                phone = "0306-8291047",
                className = "Second Year",
                section = "B",
                groupName = "ICS Statistics",
                electiveSubjectsRaw = "Computer Science, Statistics, Mathematics",
                academicYearId = yearId,
                ownerId = ownerId
            ),
            StudentEntity(
                studentId = "sy_std_107",
                rollNumber = "107",
                name = "فرحان علی",
                fatherName = "علی احمد ملک",
                phone = "0307-7182938",
                className = "Second Year",
                section = "B",
                groupName = "Arts",
                electiveSubjectsRaw = "Economics, Civics, Sociology",
                academicYearId = yearId,
                ownerId = ownerId
            ),
            StudentEntity(
                studentId = "sy_std_108",
                rollNumber = "108",
                name = "عمر فاروق",
                fatherName = "فاروق احمد خان",
                phone = "0313-8291048",
                className = "Second Year",
                section = "B",
                groupName = "Arts Psychology",
                electiveSubjectsRaw = "Psychology, Civics, Islamic Studies Elective",
                academicYearId = yearId,
                ownerId = ownerId
            ),
            StudentEntity(
                studentId = "sy_std_109",
                rollNumber = "109",
                name = "عدنان رشید",
                fatherName = "عبدالرشید گوندل",
                phone = "0300-6182947",
                className = "Second Year",
                section = "B",
                groupName = "Arts Psychology",
                electiveSubjectsRaw = "Psychology, Education, Arabic",
                academicYearId = yearId,
                ownerId = ownerId
            ),
            StudentEntity(
                studentId = "sy_std_110",
                rollNumber = "110",
                name = "شاہد محمود",
                fatherName = "محمود الحسن",
                phone = "0323-7281948",
                className = "Second Year",
                section = "B",
                groupName = "Pre-Engineering",
                electiveSubjectsRaw = "Mathematics, Physics, Chemistry",
                academicYearId = yearId,
                ownerId = ownerId
            )
        ).map { it.copy(session = "2025–2027") }
    }

    // Default Class created for teacher: "First Year B – اسلامیات لازمی"
    val defaultClassId = "cls_fy_b_islamiat"
    val defaultClass = ClassEntity(
        classId = defaultClassId,
        className = "First Year B – اسلامیات لازمی",
        academicYearId = DEFAULT_YEAR_ID,
        level = "First Year",
        section = "B",
        subjectId = "comp_islamiat",
        subjectName = "Islamic Studies (Compulsory)",
        subjectType = "Compulsory",
        teacherName = "پروفیسر حسن الرحمن تقوی",
        ownerId = DEFAULT_USER_ID
    )

    fun getDefaultMemberships(students: List<StudentEntity>, classId: String = defaultClassId): List<ClassMembershipEntity> {
        return students.filter { it.section == "B" && it.className == "First Year" }.map { student ->
            ClassMembershipEntity(
                membershipId = UUID.randomUUID().toString(),
                classId = classId,
                studentId = student.studentId,
                startDate = System.currentTimeMillis() - (15L * 24 * 60 * 60 * 1000), // 15 days ago
                status = "Active"
            )
        }
    }
}
