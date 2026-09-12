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
import com.example.ui.components.CommunicationHelper
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
    var previewStudent by remember { mutableStateOf<StudentEntity?>(null) }

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
            if (session != null) {
                val records = viewModel.repository.getRecordsForSessionDirect(session.sessionId)
                val absentRecordIds = records.filter { it.status == "Absent" }.map { it.studentId }.toSet()
                val students = viewModel.repository.getStudentsByIds(absentRecordIds.toList())
                absentStudents = students
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
                absentStudents = allStudents.take(2) // Sample demonstration
            }
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
                                    // Sequentially open SMS or send batch
                                    val user = currentUser ?: InitialSeedData.defaultUser
                                    val cls = selectedClass
                                    absentStudents.forEachIndexed { idx, s ->
                                        val text = viewModel.repository.formatSmsMessage(
                                            template = user.smsTemplate,
                                            student = s,
                                            date = effectiveDate,
                                            missedPeriods = missedPeriods,
                                            className = cls?.level ?: "First Year",
                                            section = cls?.section ?: "B",
                                            subject = cls?.subjectName ?: "Islamic Studies",
                                            teacherName = user.name,
                                            designation = user.designation,
                                            collegeName = user.college
                                        )
                                        viewModel.logSentMessage(s, text, "Draft/Prepared")
                                        messageStatuses[s.studentId] = "Prepared"
                                    }
                                    Toast.makeText(context, "${absentStudents.size} پیغامات تیار ہو گئے ہیں!", Toast.LENGTH_SHORT).show()
                                    // Open first absent student in composer
                                    val firstStudent = absentStudents.first()
                                    val firstText = viewModel.repository.formatSmsMessage(
                                        template = user.smsTemplate,
                                        student = firstStudent,
                                        date = effectiveDate,
                                        missedPeriods = missedPeriods,
                                        className = cls?.level ?: "First Year",
                                        section = cls?.section ?: "B",
                                        subject = cls?.subjectName ?: "Islamic Studies",
                                        teacherName = user.name,
                                        designation = user.designation,
                                        collegeName = user.college
                                    )
                                    CommunicationHelper.openSmsComposer(context, firstStudent.phone, firstText) { status ->
                                        messageStatuses[firstStudent.studentId] = status
                                        viewModel.logSentMessage(firstStudent, firstText, status)
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
                    val user = currentUser ?: InitialSeedData.defaultUser
                    val cls = selectedClass

                    val preparedText = remember(student, user, cls, effectiveDate, missedPeriods) {
                        viewModel.repository.formatSmsMessage(
                            template = user.smsTemplate,
                            student = student,
                            date = effectiveDate,
                            missedPeriods = missedPeriods,
                            className = cls?.level ?: "First Year",
                            section = cls?.section ?: "B",
                            subject = cls?.subjectName ?: "Islamic Studies",
                            teacherName = user.name,
                            designation = user.designation,
                            collegeName = user.college
                        )
                    }

                    AbsentStudentCommunicationCard(
                        student = student,
                        preparedMessage = preparedText,
                        status = status,
                        onSendSms = {
                            CommunicationHelper.openSmsComposer(context, student.phone, preparedText) { updateStatus ->
                                messageStatuses[student.studentId] = updateStatus
                                viewModel.logSentMessage(student, preparedText, updateStatus)
                            }
                        },
                        onCallParent = {
                            CommunicationHelper.openDialer(context, student.phone)
                        },
                        onPreview = { previewStudent = student },
                        onCopyText = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Parent Urdu SMS", preparedText))
                            Toast.makeText(context, "اردو میسج کاپی ہو گیا!", Toast.LENGTH_SHORT).show()
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

        // Preview Student Message Modal
        previewStudent?.let { s ->
            val user = currentUser ?: InitialSeedData.defaultUser
            val cls = selectedClass
            val text = viewModel.repository.formatSmsMessage(
                template = user.smsTemplate,
                student = s,
                date = effectiveDate,
                missedPeriods = missedPeriods,
                className = cls?.level ?: "First Year",
                section = cls?.section ?: "B",
                subject = cls?.subjectName ?: "Islamic Studies",
                teacherName = user.name,
                designation = user.designation,
                collegeName = user.college
            )

            AlertDialog(
                onDismissRequest = { previewStudent = null },
                title = { Text("میسج کا پیش منظر (Preview)") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("طالب علم: ${s.name} (رول نمبر ${s.rollNumber})", fontWeight = FontWeight.Bold)
                        Text("فون نمبر: ${s.phone}")
                        HorizontalDivider()
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = text,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            CommunicationHelper.openSmsComposer(context, s.phone, text) { st ->
                                messageStatuses[s.studentId] = st
                                viewModel.logSentMessage(s, text, st)
                            }
                            previewStudent = null
                        }
                    ) {
                        Text("ایس ایم ایس بھیجیں")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { previewStudent = null }) {
                        Text("بند کریں")
                    }
                }
            )
        }
    }
}

@Composable
fun AbsentStudentCommunicationCard(
    student: StudentEntity,
    preparedMessage: String,
    status: String,
    onSendSms: () -> Unit,
    onCallParent: () -> Unit,
    onPreview: () -> Unit,
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
                        "Opened in SMS Composer" -> EmeraldLight
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (status) {
                            "Opened in SMS Composer" -> EmeraldPresent
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Message Parent, Call Parent, Preview, Copy
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSendSms,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1.3f),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("میسج والد", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onCallParent,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmeraldPresent)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("کال کریں", style = MaterialTheme.typography.labelMedium)
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
