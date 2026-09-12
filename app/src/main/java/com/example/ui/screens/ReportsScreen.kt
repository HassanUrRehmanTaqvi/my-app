package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.InitialSeedData
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.AttendanceSessionEntity
import com.example.data.model.ClassEntity
import com.example.data.model.StudentEntity
import com.example.ui.components.PrintHelper
import com.example.ui.theme.CrimsonAbsent
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPresent
import com.example.ui.viewmodel.CollegeViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: CollegeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val ownerId by viewModel.currentOwnerId.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val classes by viewModel.repository.getClasses(ownerId).collectAsStateWithLifecycle(emptyList())

    // Selected Class
    var selectedClass by remember { mutableStateOf<ClassEntity?>(null) }
    var classDropdownExpanded by remember { mutableStateOf(false) }

    // Report Type: "Monthly Register", "Annual Register", "Low Attendance (<75%)", "Test Results"
    var selectedReportType by remember { mutableStateOf("Monthly Register") }
    var reportTypeExpanded by remember { mutableStateOf(false) }

    // Month & Year selection
    val currentMonth = remember { Calendar.getInstance().get(Calendar.MONTH) + 1 }
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    var selectedMonth by remember { mutableIntStateOf(currentMonth) }
    var selectedYear by remember { mutableIntStateOf(currentYear) }
    var monthDropdownExpanded by remember { mutableStateOf(false) }

    var isGeneratingReport by remember { mutableStateOf(false) }
    var reportSummaryText by remember { mutableStateOf("") }

    val monthsList = listOf(
        1 to "January (جنوری)", 2 to "February (فروری)", 3 to "March (مارچ)",
        4 to "April (اپریل)", 5 to "May (مئی)", 6 to "June (جون)",
        7 to "July (جولائی)", 8 to "August (اگست)", 9 to "September (ستمبر)",
        10 to "October (اکتوبر)", 11 to "November (نومبر)", 12 to "December (دسمبر)"
    )

    LaunchedEffect(classes) {
        if (selectedClass == null && classes.isNotEmpty()) {
            selectedClass = classes.first()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "تعلیمی اور حاضری رپورٹس (Academic Registers)",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "پنجاب ہائیر ایجوکیشن فارمیٹ پر ماہانہ رجسٹر، سالانہ ریکارڈ اور پرنٹ ایبل پی ڈی ایف",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Controls Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Class Selector
                        ExposedDropdownMenuBox(
                            expanded = classDropdownExpanded,
                            onExpandedChange = { classDropdownExpanded = !classDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedClass?.className ?: "کلاس منتخب کریں",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("کلاس") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classDropdownExpanded) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = classDropdownExpanded,
                                onDismissRequest = { classDropdownExpanded = false }
                            ) {
                                classes.forEach { cls ->
                                    DropdownMenuItem(
                                        text = { Text(cls.className) },
                                        onClick = {
                                            selectedClass = cls
                                            classDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Report Type Selector
                        ExposedDropdownMenuBox(
                            expanded = reportTypeExpanded,
                            onExpandedChange = { reportTypeExpanded = !reportTypeExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedReportType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("رپورٹ کی قسم") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reportTypeExpanded) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = reportTypeExpanded,
                                onDismissRequest = { reportTypeExpanded = false }
                            ) {
                                listOf(
                                    "Monthly Register" to "ماہانہ حاضری رجسٹر (Monthly Register)",
                                    "Annual Register" to "سالانہ حاضری شیٹ (Annual Sheet)",
                                    "Low Attendance" to "غیر حاضر طلبہ رپورٹ (Below 75% Attendance)"
                                ).forEach { (type, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            selectedReportType = type
                                            reportTypeExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Month Selector (if Monthly Register selected)
                        if (selectedReportType == "Monthly Register") {
                            ExposedDropdownMenuBox(
                                expanded = monthDropdownExpanded,
                                onExpandedChange = { monthDropdownExpanded = !monthDropdownExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val monthName = monthsList.find { it.first == selectedMonth }?.second ?: "Month"
                                OutlinedTextField(
                                    value = "$monthName $selectedYear",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("مہینہ اور سال") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthDropdownExpanded) },
                                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = monthDropdownExpanded,
                                    onDismissRequest = { monthDropdownExpanded = false }
                                ) {
                                    monthsList.forEach { (m, name) ->
                                        DropdownMenuItem(
                                            text = { Text(name) },
                                            onClick = {
                                                selectedMonth = m
                                                monthDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Action Buttons: Print PDF & Export CSV
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val cls = selectedClass ?: return@Button
                                    val user = currentUser ?: InitialSeedData.defaultUser
                                    isGeneratingReport = true
                                    coroutineScope.launch {
                                        generateAndPrintReport(
                                            context = context,
                                            viewModel = viewModel,
                                            cls = cls,
                                            user = user,
                                            reportType = selectedReportType,
                                            month = selectedMonth,
                                            year = selectedYear
                                        )
                                        isGeneratingReport = false
                                    }
                                },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("print_report_pdf_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("پرنٹ یا PDF بنائیں")
                            }

                            OutlinedButton(
                                onClick = {
                                    val cls = selectedClass
                                    if (cls != null) {
                                        coroutineScope.launch {
                                            val csvData = generateCsvReport(viewModel, cls, selectedReportType, selectedMonth, selectedYear)
                                            PrintHelper.shareCsv(context, "${cls.className}_Report.csv", csvData)
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ایکسل / CSV")
                            }
                        }
                    }
                }
            }

            // Report Preview & Guidelines Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "سرکاری فارمیٹ کے تقاضے (Punjab Academic Standards)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Text(
                            text = "• ہیڈر: کالج کا نام، تعلیمی سال، کلاس، سیکشن، مضمون، استاد کا نام و عہدہ۔\n" +
                                    "• لینڈ اسکیپ اورینٹیشن (Landscape A4) جس میں تمام تواریخ اور دستخط کے خانے شامل ہیں۔\n" +
                                    "• حاضری 75% سے کم ہونے کی صورت میں طالب علم کی نشان دہی بورڈ داخلہ کے لیے ضروری ہے۔\n" +
                                    "• پی ڈی ایف میں اردو فونٹس اور دستخطی خانے خودکار پرنٹ ہوتے ہیں۔",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (isGeneratingReport) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("رپورٹ تیار ہو رہی ہے...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

private suspend fun generateAndPrintReport(
    context: android.content.Context,
    viewModel: CollegeViewModel,
    cls: ClassEntity,
    user: com.example.data.model.UserEntity,
    reportType: String,
    month: Int,
    year: Int
) {
    val ownerId = user.userId
    val students = viewModel.repository.getStudentsForClassCreation(
        ownerId = ownerId,
        className = cls.level,
        section = cls.section,
        subjectName = cls.subjectName,
        subjectType = cls.subjectType,
        academicYearId = cls.academicYearId
    )

    val monthPadded = if (month < 10) "0$month" else "$month"
    val prefix = "$year-$monthPadded"
    val sessions = viewModel.repository.getSessionsForClassDirect(cls.classId)
        .filter { it.date.startsWith(prefix) }
        .sortedBy { it.date }

    val sessionIds = sessions.map { it.sessionId }
    val allRecords = mutableListOf<AttendanceRecordEntity>()
    sessions.forEach { s ->
        val recs = viewModel.repository.getRecordsForSessionDirect(s.sessionId)
        allRecords.addAll(recs)
    }

    // Build Landscape HTML report
    val html = buildLandscapeHtmlReport(cls, user, students, sessions, allRecords, month, year, reportType)
    PrintHelper.printHtmlReport(context, "${cls.className}_Register_${month}_$year", html)
}

private fun buildLandscapeHtmlReport(
    cls: ClassEntity,
    user: com.example.data.model.UserEntity,
    students: List<StudentEntity>,
    sessions: List<AttendanceSessionEntity>,
    records: List<AttendanceRecordEntity>,
    month: Int,
    year: Int,
    reportType: String
): String {
    val monthNames = arrayOf("", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val monthName = if (month in 1..12) monthNames[month] else "Month"

    val dateHeaders = sessions.joinToString("") { s ->
        val day = s.date.substringAfterLast("-")
        "<th style='padding:4px 2px; font-size:10px; border:1px solid #333; width:22px; text-align:center;'>$day</th>"
    }

    val rowsHtml = StringBuilder()
    students.forEachIndexed { index, student ->
        val studentRecords = records.filter { it.studentId == student.studentId }
        val presentCount = studentRecords.count { it.status == "Present" }
        val absentCount = studentRecords.count { it.status == "Absent" }
        val leaveCount = studentRecords.count { it.status == "Leave" }
        val totalSessions = sessions.size
        val pct = if (totalSessions > 0) (presentCount.toDouble() / totalSessions) * 100.0 else 0.0

        val daysCells = sessions.joinToString("") { session ->
            val rec = studentRecords.find { it.sessionId == session.sessionId }
            val symbol = when (rec?.status) {
                "Present" -> "<span style='color:#059669; font-weight:bold;'>P</span>"
                "Absent" -> "<span style='color:#DC2626; font-weight:bold;'>A</span>"
                "Leave" -> "<span style='color:#D97706; font-weight:bold;'>L</span>"
                else -> "—"
            }
            "<td style='padding:4px 2px; font-size:10px; border:1px solid #333; text-align:center;'>$symbol</td>"
        }

        val pctColor = if (pct < 75.0 && totalSessions > 0) "#DC2626" else "#111"

        rowsHtml.append("""
            <tr>
                <td style='padding:4px; font-size:11px; border:1px solid #333; text-align:center;'>${index + 1}</td>
                <td style='padding:4px; font-size:11px; border:1px solid #333; text-align:center; font-weight:bold;'>${student.rollNumber}</td>
                <td style='padding:4px; font-size:11px; border:1px solid #333;'>${student.name}</td>
                <td style='padding:4px; font-size:11px; border:1px solid #333;'>${student.fatherName}</td>
                $daysCells
                <td style='padding:4px; font-size:11px; border:1px solid #333; text-align:center; font-weight:bold; color:#059669;'>$presentCount</td>
                <td style='padding:4px; font-size:11px; border:1px solid #333; text-align:center; font-weight:bold; color:#DC2626;'>$absentCount</td>
                <td style='padding:4px; font-size:11px; border:1px solid #333; text-align:center; font-weight:bold; color:$pctColor;'>${String.format(Locale.US, "%.1f", pct)}%</td>
            </tr>
        """.trimIndent())
    }

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Attendance Register</title>
            <style>
                @page { size: A4 landscape; margin: 10mm; }
                body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; direction: ltr; margin: 0; padding: 0; color: #111; }
                .header { text-align: center; border-bottom: 2px solid #002B49; padding-bottom: 8px; margin-bottom: 12px; }
                .college-name { font-size: 18px; font-weight: bold; color: #002B49; margin: 0; text-transform: uppercase; }
                .urdu-name { font-size: 14px; font-weight: bold; color: #333; margin: 2px 0; }
                .sub-header { font-size: 13px; font-weight: bold; margin-top: 4px; }
                .meta-table { width: 100%; font-size: 12px; margin-bottom: 10px; }
                .meta-table td { padding: 3px 0; }
                table.data { width: 100%; border-collapse: collapse; margin-top: 6px; }
                table.data th { background-color: #F1F5F9; color: #002B49; border: 1px solid #333; font-weight: bold; }
                .footer-table { width: 100%; margin-top: 30px; font-size: 12px; }
                .footer-table td { padding-top: 25px; border-top: 1px dashed #666; text-align: center; width: 33%; }
            </style>
        </head>
        <body>
            <div class="header">
                <div class="college-name">${user.college}</div>
                <div class="urdu-name">گورنمنٹ ایسوسی ایٹ کالج مخدوم رشید ملتان • تعلیمی سال 2026–27</div>
                <div class="sub-header">MONTHLY ATTENDANCE REGISTER • $monthName $year</div>
            </div>

            <table class="meta-table">
                <tr>
                    <td><strong>Class:</strong> ${cls.className} (${cls.level} - Sec ${cls.section})</td>
                    <td><strong>Subject:</strong> ${cls.subjectName} (${cls.subjectType})</td>
                    <td><strong>Teacher:</strong> ${user.name} (${user.designation})</td>
                    <td><strong>Total Lectures:</strong> ${sessions.size}</td>
                </tr>
            </table>

            <table class="data">
                <thead>
                    <tr>
                        <th style="padding:4px; font-size:11px; width:30px;">Sr</th>
                        <th style="padding:4px; font-size:11px; width:45px;">Roll</th>
                        <th style="padding:4px; font-size:11px; width:150px; text-align:left;">Student Name</th>
                        <th style="padding:4px; font-size:11px; width:140px; text-align:left;">Father Name</th>
                        $dateHeaders
                        <th style="padding:4px; font-size:11px; width:35px;">P</th>
                        <th style="padding:4px; font-size:11px; width:35px;">A</th>
                        <th style="padding:4px; font-size:11px; width:45px;">%</th>
                    </tr>
                </thead>
                <tbody>
                    $rowsHtml
                </tbody>
            </table>

            <table class="footer-table">
                <tr>
                    <td><strong>Lecturer / Subject Teacher</strong><br>${user.name}</td>
                    <td><strong>Head of Department</strong><br>Checked & Verified</td>
                    <td><strong>Principal</strong><br>Govt Associate College Makhdoom Rashid</td>
                </tr>
            </table>
        </body>
        </html>
    """.trimIndent()
}

private suspend fun generateCsvReport(
    viewModel: CollegeViewModel,
    cls: ClassEntity,
    reportType: String,
    month: Int,
    year: Int
): String {
    val ownerId = cls.ownerId
    val students = viewModel.repository.getStudentsForClassCreation(
        ownerId = ownerId,
        className = cls.level,
        section = cls.section,
        subjectName = cls.subjectName,
        subjectType = cls.subjectType,
        academicYearId = cls.academicYearId
    )

    val monthPadded = if (month < 10) "0$month" else "$month"
    val prefix = "$year-$monthPadded"
    val sessions = viewModel.repository.getSessionsForClassDirect(cls.classId)
        .filter { it.date.startsWith(prefix) }
        .sortedBy { it.date }

    val csv = StringBuilder()
    csv.append("Sr,Roll No,Student Name,Father Name,Class,Section,Subject,Total Lectures,Present,Absent,Percentage\n")

    students.forEachIndexed { idx, s ->
        var present = 0
        var absent = 0
        sessions.forEach { sess ->
            val recs = viewModel.repository.getRecordsForSessionDirect(sess.sessionId)
            val r = recs.find { it.studentId == s.studentId }
            if (r?.status == "Present") present++
            else if (r?.status == "Absent") absent++
        }
        val pct = if (sessions.isNotEmpty()) (present.toDouble() / sessions.size) * 100.0 else 0.0
        csv.append("${idx + 1},${s.rollNumber},\"${s.name}\",\"${s.fatherName}\",\"${cls.level}\",\"${cls.section}\",\"${cls.subjectName}\",${sessions.size},$present,$absent,${String.format(Locale.US, "%.1f", pct)}%\n")
    }

    return csv.toString()
}
