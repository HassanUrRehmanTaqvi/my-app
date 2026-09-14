package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.InitialSeedData
import com.example.data.model.ClassEntity
import com.example.data.model.StudentEntity
import com.example.ui.components.NotificationType
import com.example.ui.components.ParentNotificationDialog
import com.example.util.CommunicationHelper
import com.example.util.ParentNotificationHelper
import com.example.util.StudentMonthlyAttendanceStats
import com.example.ui.theme.AmberLeave
import com.example.ui.theme.AmberLight
import com.example.ui.theme.CrimsonAbsent
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPresent
import com.example.ui.viewmodel.CollegeViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ParentCommunicationScreen(
    viewModel: CollegeViewModel,
    targetClassId: String?,
    targetDate: String?,
    missedPeriods: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val ownerId by viewModel.currentOwnerId.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val classes by viewModel.repository.getClasses(ownerId).collectAsStateWithLifecycle(emptyList())

    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val effectiveDate = targetDate ?: todayDate

    // Selected Class
    var selectedClass by remember { mutableStateOf<ClassEntity?>(null) }
    var absentStudents by remember { mutableStateOf<List<StudentEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // Status map: studentId -> status ("Draft", "Opened in Composer", "Logged")
    val messageStatuses = remember { mutableStateMapOf<String, String>() }

    // Template editing
    var showTemplateDialog by remember { mutableStateOf(false) }
    var editableTemplate by remember { mutableStateOf(currentUser?.smsTemplate ?: InitialSeedData.defaultUser.smsTemplate) }

    // Show Message Preview Modal
    var studentForNotificationDialog by remember { mutableStateOf<StudentEntity?>(null) }
    var generatedNotificationMessage by remember { mutableStateOf("") }
    var isGeneratingMessage by remember { mutableStateOf(false) }

    // Bulk Review Dialog
    var showBulkReviewDialog by remember { mutableStateOf(false) }
    var bulkPreparedMessages by remember { mutableStateOf<List<Pair<StudentEntity, String>>>(emptyList()) }
    var studentStatsMap by remember { mutableStateOf<Map<String, StudentMonthlyAttendanceStats>>(emptyMap()) }

    LaunchedEffect(classes, targetClassId) {
        if (classes.isNotEmpty()) {
            selectedClass = if (targetClassId != null) {
                classes.find { it.classId == targetClassId } ?: classes.first()
            } else {
                classes.first()
            }
        }
    }

    LaunchedEffect(selectedClass, effectiveDate) {
        val cls = selectedClass ?: return@LaunchedEffect
        isLoading = true
        coroutineScope.launch {
            // Find existing session for this class & date
            val session = viewModel.repository.findExistingSession(cls.classId, effectiveDate)
            val students = if (session != null) {
                val records = viewModel.repository.getRecordsForSessionDirect(session.sessionId)
                val absentRecordIds = records.filter { it.status == "Absent" }.map { it.studentId }.toSet()
                viewModel.repository.getStudentsByIds(absentRecordIds.toList())
            } else {
                // If no session found yet, check all class students
                val allStudents = viewModel.repository.getStudentsForClassCreation(
                    ownerId = ownerId,
                    className = cls.level,
                    section = cls.section,
                    subjectName = cls.subjectName,
                    subjectType = cls.subjectType,
                    academicYearId = cls.academicYearId
                )
                allStudents.take(2) // Sample demonstration
            }
            absentStudents = students

            // Compute performance & attendance stats for each absent student
            val stats = mutableMapOf<String, StudentMonthlyAttendanceStats>()
            students.forEach { s ->
                stats[s.studentId] = viewModel.repository.getStudentAttendanceStats(
                    studentId = s.studentId,
                    classId = cls.classId,
                    date = effectiveDate,
                    missedPeriodsToday = missedPeriods
                )
            }
            studentStatsMap = stats
            isLoading = false
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
            // Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "والدین سے رابطہ (Parent Communication)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "کلاس: ${selectedClass?.className ?: "—"} • تاریخ: $effectiveDate",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { showTemplateDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Urdu Template",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CrimsonLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "غیر حاضر طلبہ: ${absentStudents.size}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = CrimsonAbsent
                                )
                                Text(
                                    text = "ضائع شدہ پیریڈز: $missedPeriods",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CrimsonAbsent
                                )
                            }
                        }

                        // Bulk Action: Message All Absent Parents (Section 26)
                        if (absentStudents.isNotEmpty()) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        isLoading = true
                                        val user = currentUser ?: InitialSeedData.defaultUser
                                        val dept = ParentNotificationHelper.resolveDepartment(user.department, selectedClass?.subjectName, user.defaultSubject)
                                        val clg = user.college.ifBlank { ParentNotificationHelper.DEFAULT_COLLEGE_NAME }

                                        val prepared = absentStudents.map { s ->
                                            val stats = viewModel.repository.getStudentAttendanceStats(
                                                studentId = s.studentId,
                                                classId = selectedClass?.classId ?: "",
                                                date = effectiveDate,
                                                missedPeriodsToday = missedPeriods
                                            )
                                            val msg = ParentNotificationHelper.generateAbsenceNotification(
                                                studentName = s.name,
                                                rollNumber = s.rollNumber,
                                                date = effectiveDate,
                                                missedPeriodsToday = stats.missedPeriodsToday,
                                                monthlyPresent = stats.presentPeriods,
                                                monthlyAbsent = stats.absentPeriods,
                                                attendancePercentage = stats.attendancePercentage,
                                                consecutiveAbsentDays = stats.consecutiveAbsentDays,
                                                teacherName = user.name,
                                                department = dept,
                                                collegeName = clg
                                            )
                                            Pair(s, msg)
                                        }
                                        bulkPreparedMessages = prepared
                                        showBulkReviewDialog = true
                                        isLoading = false
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("message_all_absent_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("تمام غیر حاضر طلبہ کے والدین کو میسج کریں (${absentStudents.size})")
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (absentStudents.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = EmeraldLight.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPresent, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "ماشاءاللہ! آج کوئی طالب علم غیر حاضر نہیں ہے",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldPresent
                            )
                            Text(
                                text = "کلاس کی 100% حاضری ریکارڈ کی گئی ہے۔",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(absentStudents) { student ->
                    val status = messageStatuses[student.studentId] ?: "Draft"
                    val stats = studentStatsMap[student.studentId]

                    AbsentStudentCommunicationCard(
                        student = student,
                        status = status,
                        stats = stats,
                        onOpenMessageDialog = {
                            coroutineScope.launch {
                                isGeneratingMessage = true
                                val stats = viewModel.repository.getStudentAttendanceStats(
                                    studentId = student.studentId,
                                    classId = selectedClass?.classId ?: "",
                                    date = effectiveDate,
                                    missedPeriodsToday = missedPeriods
                                )
                                val user = currentUser ?: InitialSeedData.defaultUser
                                val dept = ParentNotificationHelper.resolveDepartment(user.department, selectedClass?.subjectName, user.defaultSubject)
                                val clg = user.college.ifBlank { ParentNotificationHelper.DEFAULT_COLLEGE_NAME }

                                generatedNotificationMessage = ParentNotificationHelper.generateAbsenceNotification(
                                    studentName = student.name,
                                    rollNumber = student.rollNumber,
                                    date = effectiveDate,
                                    missedPeriodsToday = stats.missedPeriodsToday,
                                    monthlyPresent = stats.presentPeriods,
                                    monthlyAbsent = stats.absentPeriods,
                                    attendancePercentage = stats.attendancePercentage,
                                    consecutiveAbsentDays = stats.consecutiveAbsentDays,
                                    teacherName = user.name,
                                    department = dept,
                                    collegeName = clg
                                )
                                studentForNotificationDialog = student
                                isGeneratingMessage = false
                            }
                        },
                        onCallParent = {
                            CommunicationHelper.openDialer(context, student.phone)
                        },
                        onSendWhatsApp = {
                            coroutineScope.launch {
                                val stats = viewModel.repository.getStudentAttendanceStats(
                                    studentId = student.studentId,
                                    classId = selectedClass?.classId ?: "",
                                    date = effectiveDate,
                                    missedPeriodsToday = missedPeriods
                                )
                                val user = currentUser ?: InitialSeedData.defaultUser
                                val dept = ParentNotificationHelper.resolveDepartment(user.department, selectedClass?.subjectName, user.defaultSubject)
                                val clg = user.college.ifBlank { ParentNotificationHelper.DEFAULT_COLLEGE_NAME }

                                val msg = ParentNotificationHelper.generateAbsenceNotification(
                                    studentName = student.name,
                                    rollNumber = student.rollNumber,
                                    date = effectiveDate,
                                    missedPeriodsToday = stats.missedPeriodsToday,
                                    monthlyPresent = stats.presentPeriods,
                                    monthlyAbsent = stats.absentPeriods,
                                    attendancePercentage = stats.attendancePercentage,
                                    consecutiveAbsentDays = stats.consecutiveAbsentDays,
                                    teacherName = user.name,
                                    department = dept,
                                    collegeName = clg
                                )
                                CommunicationHelper.openWhatsApp(context, student.phone, msg)
                                messageStatuses[student.studentId] = "WhatsApp Opened"
                                viewModel.logSentMessage(student, msg, "WhatsApp Opened")
                            }
                        },
                        onCopyText = {
                            coroutineScope.launch {
                                val stats = viewModel.repository.getStudentAttendanceStats(
                                    studentId = student.studentId,
                                    classId = selectedClass?.classId ?: "",
                                    date = effectiveDate,
                                    missedPeriodsToday = missedPeriods
                                )
                                val user = currentUser ?: InitialSeedData.defaultUser
                                val dept = ParentNotificationHelper.resolveDepartment(user.department, selectedClass?.subjectName, user.defaultSubject)
                                val clg = user.college.ifBlank { ParentNotificationHelper.DEFAULT_COLLEGE_NAME }

                                val msg = ParentNotificationHelper.generateAbsenceNotification(
                                    studentName = student.name,
                                    rollNumber = student.rollNumber,
                                    date = effectiveDate,
                                    missedPeriodsToday = stats.missedPeriodsToday,
                                    monthlyPresent = stats.presentPeriods,
                                    monthlyAbsent = stats.absentPeriods,
                                    attendancePercentage = stats.attendancePercentage,
                                    consecutiveAbsentDays = stats.consecutiveAbsentDays,
                                    teacherName = user.name,
                                    department = dept,
                                    collegeName = clg
                                )
                                CommunicationHelper.copyToClipboard(context, msg, "Parent Urdu SMS")
                            }
                        }
                    )
                }
            }
        }

        // Urdu Template Editor Dialog (Section 22)
        if (showTemplateDialog) {
            AlertDialog(
                onDismissRequest = { showTemplateDialog = false },
                title = { Text("اردو ایس ایم ایس ٹیمپلیٹ ایڈٹ کریں") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "دستیاب متغیرات (Placeholders): {student_name}، {roll_number}، {father_name}، {date}، {missed_periods}، {class_name}، {teacher_name}، {college_name}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        OutlinedTextField(
                            value = editableTemplate,
                            onValueChange = { editableTemplate = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            maxLines = 10
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            currentUser?.let { user ->
                                viewModel.updateProfile(
                                    name = user.name,
                                    email = user.email,
                                    college = user.college,
                                    designation = user.designation,
                                    defaultSubject = user.defaultSubject,
                                    smsTemplate = editableTemplate
                                )
                            }
                            showTemplateDialog = false
                        }
                    ) {
                        Text("محفوظ کریں")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTemplateDialog = false }) {
                        Text("منسوخ")
                    }
                }
            )
        }

        // Preview / Send Student Message Modal via ParentNotificationDialog
        studentForNotificationDialog?.let { s ->
            val user = currentUser ?: InitialSeedData.defaultUser
            val dept = ParentNotificationHelper.resolveDepartment(user.department, selectedClass?.subjectName, user.defaultSubject)
            ParentNotificationDialog(
                student = s,
                notificationType = NotificationType.ABSENCE,
                initialMessage = generatedNotificationMessage,
                parentPhone = s.phone,
                teacherName = user.name,
                teacherDepartment = dept,
                collegeName = user.college,
                onDismiss = { studentForNotificationDialog = null },
                onMessageSent = { status ->
                    messageStatuses[s.studentId] = status
                    viewModel.logSentMessage(s, generatedNotificationMessage, status)
                }
            )
        }

        // Bulk Review Dialog
        if (showBulkReviewDialog) {
            AlertDialog(
                onDismissRequest = { showBulkReviewDialog = false },
                title = {
                    Text(
                        text = "تمام غیر حاضر طلبہ کے پیغامات کا جائزہ (${bulkPreparedMessages.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "ہر طالب علم کا تیار کردہ پیغام نیچے دیا گیا ہے۔ آپ جائزہ لے کر انفرادی طور پر ارسال یا کاپی کر سکتے ہیں:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(bulkPreparedMessages) { (std, msg) ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${std.name} (رول نمبر: ${std.rollNumber})",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = std.phone,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        // Performance & Absence visual indicator
                                        val stdStats = studentStatsMap[std.studentId]
                                        if (stdStats != null) {
                                            val (pillBg, pillFg, pillIcon, pillLabel) = when {
                                                stdStats.attendancePercentage >= 75.0 && stdStats.consecutiveAbsentDays < 3 ->
                                                    listOf(
                                                        EmeraldLight,
                                                        EmeraldPresent,
                                                        Icons.Default.CheckCircle,
                                                        "حاضری تسلی بخش (${stdStats.attendancePercentage.toInt()}%)"
                                                    )
                                                stdStats.attendancePercentage >= 60.0 && stdStats.consecutiveAbsentDays < 3 ->
                                                    listOf(
                                                        AmberLight,
                                                        AmberLeave,
                                                        Icons.Default.Warning,
                                                        "حاضری توجہ طلب (${stdStats.attendancePercentage.toInt()}%)"
                                                    )
                                                else ->
                                                    listOf(
                                                        CrimsonLight,
                                                        CrimsonAbsent,
                                                        Icons.Default.Warning,
                                                        if (stdStats.consecutiveAbsentDays >= 3)
                                                            "مسلسل ${stdStats.consecutiveAbsentDays} دن غیر حاضر • سنگین (${stdStats.attendancePercentage.toInt()}%)"
                                                        else
                                                            "سنگین غیر حاضری (${stdStats.attendancePercentage.toInt()}%)"
                                                    )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = pillBg as Color
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        pillIcon as androidx.compose.ui.graphics.vector.ImageVector,
                                                        contentDescription = null,
                                                        tint = pillFg as Color,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = pillLabel as String,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = pillFg
                                                    )
                                                }
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = msg,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    CommunicationHelper.copyToClipboard(context, msg, "Parent Notification")
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("کاپی", style = MaterialTheme.typography.labelSmall)
                                            }
                                            Button(
                                                onClick = {
                                                    CommunicationHelper.sendSms(context, std.phone, msg)
                                                    messageStatuses[std.studentId] = "SMS Opened"
                                                    viewModel.logSentMessage(std, msg, "SMS Opened")
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("ایس ایم ایس", style = MaterialTheme.typography.labelSmall)
                                            }
                                            Button(
                                                onClick = {
                                                    CommunicationHelper.openWhatsApp(context, std.phone, msg)
                                                    messageStatuses[std.studentId] = "WhatsApp Opened"
                                                    viewModel.logSentMessage(std, msg, "WhatsApp Opened")
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("واٹس ایپ", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showBulkReviewDialog = false }) {
                        Text("مکمل / بند کریں")
                    }
                }
            )
        }
    }
}

@Composable
fun AbsentStudentCommunicationCard(
    student: StudentEntity,
    status: String,
    stats: StudentMonthlyAttendanceStats? = null,
    onOpenMessageDialog: () -> Unit,
    onCallParent: () -> Unit,
    onSendWhatsApp: () -> Unit,
    onCopyText: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = CrimsonLight,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = student.rollNumber,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = CrimsonAbsent
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = student.name,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "والد: ${student.fatherName} • فون: ${student.phone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (status) {
                        "Opened in SMS Composer", "SMS Opened", "WhatsApp Opened" -> EmeraldLight
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (status) {
                            "Opened in SMS Composer", "SMS Opened", "WhatsApp Opened" -> EmeraldPresent
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            // Performance & Absence Visual Indicator (Green for good performance, Red for high absences)
            if (stats != null) {
                val (badgeBg, badgeFg, badgeIcon, badgeLabel) = when {
                    stats.attendancePercentage >= 75.0 && stats.consecutiveAbsentDays < 3 ->
                        listOf(
                            EmeraldLight,
                            EmeraldPresent,
                            Icons.Default.CheckCircle,
                            "تسلی بخش حاضری (${stats.attendancePercentage.toInt()}%)"
                        )
                    stats.attendancePercentage >= 60.0 && stats.consecutiveAbsentDays < 3 ->
                        listOf(
                            AmberLight,
                            AmberLeave,
                            Icons.Default.Warning,
                            "توجہ طلب حاضری (${stats.attendancePercentage.toInt()}%)"
                        )
                    else ->
                        listOf(
                            CrimsonLight,
                            CrimsonAbsent,
                            Icons.Default.Warning,
                            if (stats.consecutiveAbsentDays >= 3)
                                "مسلسل ${stats.consecutiveAbsentDays} دن غیر حاضر • سنگین خطرہ (${stats.attendancePercentage.toInt()}%)"
                            else
                                "زیادہ غیر حاضری (${stats.attendancePercentage.toInt()}%)"
                        )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeBg as Color,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            badgeIcon as androidx.compose.ui.graphics.vector.ImageVector,
                            contentDescription = null,
                            tint = badgeFg as Color,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = badgeLabel as String,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = badgeFg
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "کل حاضر: ${stats.presentPeriods} • کل غیر حاضر: ${stats.absentPeriods}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: والدین کو پیغام بھیجیں (Primary), WhatsApp, Call, Copy
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onOpenMessageDialog,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1.4f),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("والدین کو پیغام بھیجیں", style = MaterialTheme.typography.labelSmall)
                }

                Button(
                    onClick = onSendWhatsApp,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "WhatsApp", modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("واٹس ایپ", style = MaterialTheme.typography.labelSmall, color = Color.White)
                }

                OutlinedButton(
                    onClick = onCallParent,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmeraldPresent)
                }

                IconButton(
                    onClick = onCopyText,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy message", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
