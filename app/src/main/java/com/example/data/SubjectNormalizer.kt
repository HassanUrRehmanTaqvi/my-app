package com.example.data

object SubjectNormalizer {

    data class NormalizedSubject(
        val standardId: String,
        val standardName: String,
        val urduName: String,
        val type: String, // "Compulsory" or "Elective"
        val aliases: List<String>
    )

    val DEFAULT_SUBJECTS = listOf(
        // Compulsory (Punjab HSSC Scheme)
        NormalizedSubject(
            standardId = "comp_islamiat",
            standardName = "Islamic Studies (Compulsory)",
            urduName = "اسلامیات لازمی",
            type = "Compulsory",
            aliases = listOf("islamiat", "islamic studies", "اسلامیات", "اسلامیات لازمی", "isl")
        ),
        NormalizedSubject(
            standardId = "comp_pak_studies",
            standardName = "Pakistan Studies",
            urduName = "مطالعہ پاکستان",
            type = "Compulsory",
            aliases = listOf("pak studies", "pakistan studies", "مطالعہ پاکستان", "ps")
        ),
        NormalizedSubject(
            standardId = "comp_urdu",
            standardName = "Urdu",
            urduName = "اردو لازمی",
            type = "Compulsory",
            aliases = listOf("urdu", "اردو", "اردو لازمی")
        ),
        NormalizedSubject(
            standardId = "comp_english",
            standardName = "English",
            urduName = "انگریزی لازمی",
            type = "Compulsory",
            aliases = listOf("english", "انگریزی", "eng")
        ),
        NormalizedSubject(
            standardId = "comp_quran",
            standardName = "Tarjuma-tul-Quran",
            urduName = "ترجمۃ القرآن المجید",
            type = "Compulsory",
            aliases = listOf("quran", "tarjuma tul quran", "ترجمۃ القرآن", "ترجمہ قرآن")
        ),

        // Electives
        NormalizedSubject(
            standardId = "elec_psychology",
            standardName = "Psychology",
            urduName = "نفسیات / سائیکالوجی",
            type = "Elective",
            aliases = listOf("psychology", "psych", "سائیکالوجی", "نفسیات")
        ),
        NormalizedSubject(
            standardId = "elec_economics",
            standardName = "Economics",
            urduName = "معاشیات / اکنامکس",
            type = "Elective",
            aliases = listOf("economics", "econ", "eco", "اکنامکس", "معاشیات")
        ),
        NormalizedSubject(
            standardId = "elec_computer_science",
            standardName = "Computer Science",
            urduName = "کمپیوٹر سائنس / سی ایس ٹی",
            type = "Elective",
            aliases = listOf("computer science", "cs", "cst", "کمپیوٹر سائنس", "سی ایس ٹی", "computer")
        ),
        NormalizedSubject(
            standardId = "elec_civics",
            standardName = "Civics",
            urduName = "شہریت / سیوکس",
            type = "Elective",
            aliases = listOf("civics", "civ", "شہریت", "سیوکس")
        ),
        NormalizedSubject(
            standardId = "elec_education",
            standardName = "Education",
            urduName = "تعلیم / ایجوکیشن",
            type = "Elective",
            aliases = listOf("education", "edu", "تعلیم", "ایجوکیشن")
        ),
        NormalizedSubject(
            standardId = "elec_sociology",
            standardName = "Sociology",
            urduName = "عمرانیات / سوشیالوجی",
            type = "Elective",
            aliases = listOf("sociology", "soc", "سوشیالوجی", "عمرانیات")
        ),
        NormalizedSubject(
            standardId = "elec_islamic_studies_elective",
            standardName = "Islamic Studies (Elective)",
            urduName = "اسلامیات اختیاری",
            type = "Elective",
            aliases = listOf("islamic studies elective", "islamiat elective", "اسلامیات اختیاری", "isl elec")
        ),
        NormalizedSubject(
            standardId = "elec_statistics",
            standardName = "Statistics",
            urduName = "شماریات / سٹیٹ",
            type = "Elective",
            aliases = listOf("statistics", "stats", "stat", "شماریات", "سٹیٹ")
        ),
        NormalizedSubject(
            standardId = "elec_mathematics",
            standardName = "Mathematics",
            urduName = "ریاضی / میتھ",
            type = "Elective",
            aliases = listOf("mathematics", "math", "maths", "ریاضی", "میتھ")
        ),
        NormalizedSubject(
            standardId = "elec_physics",
            standardName = "Physics",
            urduName = "طبیعیات / فزکس",
            type = "Elective",
            aliases = listOf("physics", "phy", "فزکس", "طبیعیات")
        ),
        NormalizedSubject(
            standardId = "elec_chemistry",
            standardName = "Chemistry",
            urduName = "کیمیا / کیمسٹری",
            type = "Elective",
            aliases = listOf("chemistry", "chem", "کیمسٹری", "کیمیا")
        ),
        NormalizedSubject(
            standardId = "elec_biology",
            standardName = "Biology",
            urduName = "حیاتیات / بائیولوجی",
            type = "Elective",
            aliases = listOf("biology", "bio", "بائیولوجی", "حیاتیات")
        ),
        NormalizedSubject(
            standardId = "elec_arabic",
            standardName = "Arabic",
            urduName = "عربی",
            type = "Elective",
            aliases = listOf("arabic", "arab", "عربی")
        ),
        NormalizedSubject(
            standardId = "elec_history",
            standardName = "History of Islam",
            urduName = "تاریخ اسلام",
            type = "Elective",
            aliases = listOf("history", "history of islam", "تاریخ", "تاریخ اسلام", "hist")
        )
    )

    fun normalizeSubject(input: String): String {
        val cleanInput = input.trim().lowercase()
        for (sub in DEFAULT_SUBJECTS) {
            if (cleanInput.equals(sub.standardName, ignoreCase = true) ||
                cleanInput.equals(sub.urduName, ignoreCase = true) ||
                sub.aliases.any { alias -> cleanInput.contains(alias.lowercase()) || alias.lowercase().contains(cleanInput) }
            ) {
                return sub.standardName
            }
        }
        return input.trim()
    }

    /**
     * Checks if a student is eligible for a given subject.
     * Compulsory: all students of class/section are eligible.
     * Elective: student must have chosen this elective, or student's group inherently contains it.
     */
    fun isStudentEligible(
        targetSubjectName: String,
        targetSubjectType: String,
        studentGroup: String,
        studentElectivesRaw: String
    ): Boolean {
        if (targetSubjectType.equals("Compulsory", ignoreCase = true)) {
            return true
        }

        val normTarget = normalizeSubject(targetSubjectName).lowercase()
        val normElectives = studentElectivesRaw.lowercase()
        val normGroup = studentGroup.lowercase()

        // Direct match in electives raw
        if (normElectives.contains(normTarget)) {
            return true
        }

        // Match via aliases
        val targetDef = DEFAULT_SUBJECTS.find { it.standardName.equals(targetSubjectName, ignoreCase = true) }
        val aliases = targetDef?.aliases ?: listOf(targetSubjectName.lowercase())

        for (alias in aliases) {
            if (normElectives.contains(alias.lowercase())) {
                return true
            }
        }

        // Group-based implicit subject inclusion
        when {
            normTarget.contains("psychology") -> {
                if (normGroup.contains("psychology") || normElectives.contains("psych") || normElectives.contains("نفسیات") || normElectives.contains("سائیکالوجی")) return true
            }
            normTarget.contains("economics") -> {
                if (normGroup.contains("economics") || normGroup.contains("eco") || normElectives.contains("معاشیات") || normElectives.contains("اکنامکس")) return true
            }
            normTarget.contains("computer") || normTarget.contains("cs") -> {
                if (normGroup.contains("ics") || normGroup.contains("cst") || normGroup.contains("it") || normElectives.contains("کمپیوٹر")) return true
            }
            normTarget.contains("physics") -> {
                if (normGroup.contains("physics") || normGroup.contains("pre-eng") || normGroup.contains("medical") || normGroup.contains("non-medical")) return true
            }
            normTarget.contains("chemistry") -> {
                if (normGroup.contains("medical") || normGroup.contains("pre-eng") || normGroup.contains("non-medical")) return true
            }
            normTarget.contains("biology") -> {
                if (normGroup.contains("medical") || normElectives.contains("bio") || normElectives.contains("حیاتیات")) return true
            }
            normTarget.contains("statistics") -> {
                if (normGroup.contains("stats") || normGroup.contains("statistics") || normElectives.contains("شماریات") || normElectives.contains("سٹیٹ")) return true
            }
            normTarget.contains("mathematics") -> {
                if (normGroup.contains("pre-eng") || normGroup.contains("ics") || normGroup.contains("general science") || normElectives.contains("ریاضی")) return true
            }
            normTarget.contains("arabic") -> {
                if (normGroup.contains("arabic") || normElectives.contains("عربی")) return true
            }
            normTarget.contains("education") -> {
                if (normGroup.contains("education") || normElectives.contains("تعلیم") || normElectives.contains("ایجوکیشن")) return true
            }
            normTarget.contains("sociology") -> {
                if (normGroup.contains("sociology") || normElectives.contains("عمرانیات") || normElectives.contains("سوشیالوجی")) return true
            }
            normTarget.contains("civics") -> {
                if (normGroup.contains("civics") || normElectives.contains("شہریت") || normElectives.contains("سیوکس")) return true
            }
        }

        return false
    }
}
