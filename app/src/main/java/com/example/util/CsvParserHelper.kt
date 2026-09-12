package com.example.util

import com.example.data.model.StudentEntity
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.UUID

data class ParsedCsvRow(
    val rowNumber: Int,
    val rollNumber: String,
    val name: String,
    val fatherName: String,
    val phone: String,
    val className: String, // "First Year" or "Second Year"
    val section: String,
    val groupName: String,
    val optionalSubjects: String,
    val isValid: Boolean,
    val validationError: String? = null,
    val isDuplicateInFile: Boolean = false,
    val existingStudent: StudentEntity? = null
) {
    val isDuplicateOfExisting: Boolean
        get() = existingStudent != null
}

data class CsvParseResult(
    val totalRows: Int,
    val validRows: List<ParsedCsvRow>,
    val invalidRows: List<ParsedCsvRow>,
    val duplicateInFileRows: List<ParsedCsvRow>,
    val existingMatchRows: List<ParsedCsvRow>, // Matches student in DB
    val allParsedRows: List<ParsedCsvRow>
) {
    val newRows: List<ParsedCsvRow>
        get() = validRows.filter { it.existingStudent == null && !it.isDuplicateInFile }
}

data class CsvImportSummary(
    val importedCount: Int,
    val updatedCount: Int,
    val skippedCount: Int,
    val failedCount: Int,
    val message: String
)

object CsvParserHelper {

    /**
     * Parses standard CSV line respecting double quotes and commas within quotes (RFC 4180)
     */
    fun parseCsvLine(line: String, delimiter: Char = ','): List<String> {
        val result = mutableListOf<String>()
        val sb = java.lang.StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                    // Escaped quote
                    sb.append('"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == delimiter && !inQuotes) {
                result.add(sb.toString().trim())
                sb.setLength(0)
            } else {
                sb.append(c)
            }
            i++
        }
        result.add(sb.toString().trim())
        return result
    }

    /**
     * Read stream with UTF-8 support and strip BOM if present
     */
    fun readStreamToLines(inputStream: InputStream): List<String> {
        val reader = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
        val lines = mutableListOf<String>()
        var isFirst = true
        reader.forEachLine { line ->
            var cleaned = line
            if (isFirst) {
                // Strip UTF-8 BOM if present
                if (cleaned.startsWith("\uFEFF")) {
                    cleaned = cleaned.substring(1)
                }
                isFirst = false
            }
            if (cleaned.isNotBlank()) {
                lines.add(cleaned)
            }
        }
        return lines
    }

    /**
     * Parse raw CSV text or lines into structured preview objects
     */
    fun parseCsvData(
        lines: List<String>,
        existingStudents: List<StudentEntity>
    ): CsvParseResult {
        if (lines.isEmpty()) {
            return CsvParseResult(0, emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        }

        // Detect delimiter (comma or semicolon or tab)
        val firstLine = lines.first()
        val delimiter = when {
            firstLine.count { it == ';' } > firstLine.count { it == ',' } -> ';'
            firstLine.count { it == '\t' } > firstLine.count { it == ',' } -> '\t'
            else -> ','
        }

        val headerTokens = parseCsvLine(firstLine, delimiter).map { it.lowercase().trim() }
        val hasHeaders = isHeaderRow(headerTokens)

        val colIndexMap = if (hasHeaders) {
            detectColumnIndices(headerTokens)
        } else {
            // Default expected order: Roll, Name, FatherName, Phone, Class, Section, Group, Optional Subjects
            mapOf(
                "roll" to 0,
                "name" to 1,
                "father" to 2,
                "phone" to 3,
                "class" to 4,
                "section" to 5,
                "group" to 6,
                "optional" to 7
            )
        }

        val dataLines = if (hasHeaders) lines.drop(1) else lines
        val parsedRows = mutableListOf<ParsedCsvRow>()
        val seenRollAndClass = mutableSetOf<String>()

        dataLines.forEachIndexed { index, rawLine ->
            val rowNum = (if (hasHeaders) index + 2 else index + 1)
            val tokens = parseCsvLine(rawLine, delimiter)

            val rawRoll = getColValue(tokens, colIndexMap["roll"])
            val rawName = getColValue(tokens, colIndexMap["name"])
            val rawFather = getColValue(tokens, colIndexMap["father"])
            val rawPhone = getColValue(tokens, colIndexMap["phone"])
            val rawClass = getColValue(tokens, colIndexMap["class"])
            val rawSection = getColValue(tokens, colIndexMap["section"])
            val rawGroup = getColValue(tokens, colIndexMap["group"])
            val rawOptional = getColValue(tokens, colIndexMap["optional"])

            val normalizedClass = normalizeClassName(rawClass)
            val normalizedRoll = rawRoll.trim()
            val normalizedSection = if (rawSection.isNotBlank()) rawSection.trim().uppercase() else "A"
            val normalizedGroup = if (rawGroup.isNotBlank()) rawGroup.trim() else "Pre-Medical"

            // Validation checks
            val errors = mutableListOf<String>()
            if (normalizedRoll.isBlank()) {
                errors.add("رول نمبر موجود نہیں ہے (Missing Roll Number)")
            }
            if (rawName.isBlank()) {
                errors.add("طالب علم کا نام موجود نہیں ہے (Missing Name)")
            }

            val key = "${normalizedClass.lowercase()}_${normalizedRoll}"
            val isDuplicateInFile = if (normalizedRoll.isNotBlank()) {
                if (seenRollAndClass.contains(key)) {
                    errors.add("فائل میں اسی کلاس کا یہ رول نمبر دہرایا گیا ہے (Duplicate Roll in File)")
                    true
                } else {
                    seenRollAndClass.add(key)
                    false
                }
            } else false

            // Check match with existing DB students
            val existingStudent = if (normalizedRoll.isNotBlank()) {
                existingStudents.find {
                    it.rollNumber.equals(normalizedRoll, ignoreCase = true) &&
                            it.className.equals(normalizedClass, ignoreCase = true)
                }
            } else null

            val isValid = errors.isEmpty()
            val errorMsg = if (errors.isNotEmpty()) errors.joinToString(" • ") else null

            parsedRows.add(
                ParsedCsvRow(
                    rowNumber = rowNum,
                    rollNumber = normalizedRoll,
                    name = rawName.trim(),
                    fatherName = rawFather.trim(),
                    phone = if (rawPhone.isNotBlank()) rawPhone.trim() else "03000000000",
                    className = normalizedClass,
                    section = normalizedSection,
                    groupName = normalizedGroup,
                    optionalSubjects = rawOptional.trim(),
                    isValid = isValid,
                    validationError = errorMsg,
                    isDuplicateInFile = isDuplicateInFile,
                    existingStudent = existingStudent
                )
            )
        }

        val validRows = parsedRows.filter { it.isValid && it.existingStudent == null }
        val existingMatchRows = parsedRows.filter { it.isValid && it.existingStudent != null }
        val invalidRows = parsedRows.filter { !it.isValid }
        val duplicateInFileRows = parsedRows.filter { it.isDuplicateInFile }

        return CsvParseResult(
            totalRows = parsedRows.size,
            validRows = validRows,
            invalidRows = invalidRows,
            duplicateInFileRows = duplicateInFileRows,
            existingMatchRows = existingMatchRows,
            allParsedRows = parsedRows
        )
    }

    private fun getColValue(tokens: List<String>, idx: Int?): String {
        return if (idx != null && idx in tokens.indices) tokens[idx] else ""
    }

    private fun isHeaderRow(tokens: List<String>): Boolean {
        val keywords = listOf(
            "roll", "name", "father", "phone", "class", "section", "group", "optional",
            "رول", "نام", "ولدیت", "فون", "سیکشن", "گروپ"
        )
        return tokens.any { token ->
            keywords.any { k -> token.contains(k, ignoreCase = true) }
        }
    }

    private fun detectColumnIndices(headers: List<String>): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        headers.forEachIndexed { i, h ->
            val header = h.trim().lowercase()
            when {
                header.contains("roll") || header.contains("رول") -> map["roll"] = i
                header.contains("father") || header.contains("ولدیت") || header.contains("والد") -> map["father"] = i
                header.contains("name") || header.contains("نام") || header.contains("student") || header.contains("طالب") -> {
                    if (!map.containsKey("name") && !header.contains("father") && !header.contains("والد")) {
                        map["name"] = i
                    }
                }
                header.contains("phone") || header.contains("mobile") || header.contains("cell") || header.contains("contact") || header.contains("فون") || header.contains("موبائل") -> map["phone"] = i
                header.contains("class") || header.contains("grade") || header.contains("year") || header.contains("کلاس") || header.contains("سال") -> map["class"] = i
                header.contains("section") || header.contains("sec") || header.contains("سیکشن") -> map["section"] = i
                header.contains("group") || header.contains("discipline") || header.contains("گروپ") || header.contains("شعبہ") -> map["group"] = i
                header.contains("optional") || header.contains("elective") || header.contains("subject") || header.contains("اختیاری") || header.contains("مضامین") -> map["optional"] = i
            }
        }
        return map
    }

    fun normalizeClassName(raw: String): String {
        val lower = raw.trim().lowercase()
        return when {
            lower.contains("12") || lower.contains("second") || lower.contains("2nd") || lower.contains("بارہویں") || lower.contains("12th") -> "Second Year"
            else -> "First Year"
        }
    }
}
