package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.InitialSeedData
import com.example.data.model.AttendanceSessionEntity
import com.example.data.model.ClassEntity
import com.example.data.model.StudentEntity
import com.example.ui.components.NotificationType
import com.example.ui.components.ParentNotificationDialog
import com.example.util.CommunicationHelper
import com.example.util.ParentNotificationHelper
import com.example.ui.theme.AmberLeave
import com.example.ui.theme.AmberLight
import com.example.ui.theme.CrimsonAbsent
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPresent
import com.example.ui.viewmodel.AttendanceSummaryData
import com.example.ui.viewmodel.CollegeViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: CollegeViewModel,
    preselectedClassId: String?,
    onNavigateToParentMessaging: (classId: String, date: String, missedPeriods: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val ownerId by viewModel.currentOwnerId.collectAsStateWithLifecycle()
    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val classes by viewModel.repository.getClasses(ownerId).collectAsStateWithLifecycle(emptyList())

    // Selected Class
    var selectedClass by remember { mutableStateOf<ClassEntity?>(null) }
    var classDropdownExpanded by remember { mutableStateOf(false) }

    // Date & Periods
    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var selectedDate by remember { mutableStateOf(todayDate) }
    var periodCount by remember { mutableIntStateOf(1) }
    var notes by remember { mutableStateOf("") }

    // Attendance state: studentId -> "Present", "Absent", "Leave"
    val studentStatuses = remember { mutableStateMapOf<String, String>() }
    var enrolledStudents by remember { mutableStateOf<List<StudentEntity>>(emptyList()) }
    var isLoadingStudents by remember { mutableStateOf(false) }

    // Undo stack: Pair(studentId, previousStatus)
    val undoStack = remember { mutableListOf<Pair<String, String>>() }

    // Filter tab: "All", "Absent Only", "Present Only"
    var filterMode by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    // Duplicate detection state
    var existingSession by remember { mutableStateOf<AttendanceSessionEntity?>(null) }
    var showDuplicateWarningDialog by remember { mutableStateOf(false) }

    // Summary dialog state
    var attendanceSummary by remember { mutableStateOf<AttendanceSummaryData?>(null) }
    var showSummaryDialog by remember { mutableStateOf(false) }

    // Parent notification dialog state
    var studentForNotification by remember { mutableStateOf<StudentEntity?>(null) }
    var generatedNotificationMsg by remember { mutableStateOf("") }
    var showClassSetupDialog by remember { mutableStateOf(false) }

    // Initialize selected class
    LaunchedEffect(classes, preselectedClassId) {
        if (classes.isNotEmpty()) {
            val savedClassId = com.example.util.ProfessorSetupPreferences.getClassId(context)
            selectedClass = if (preselectedClassId != null) {
                classes.find { it.classId == preselectedClassId } ?: classes.first()
            } else if (savedClassId != null) {
                classes.find { it.classId == savedClassId } ?: classes.first()
            } else {
                classes.first()
            }
        }
    }

    // Load active students for selected class and check duplicates
    fun loadStudentsAndCheckExisting() {
        val currentClass = selectedClass ?: return
        coroutineScope.launch {
            isLoadingStudents = true
            val allActive = viewModel.repository.getStudentsForClass(currentClass.classId)
            enrolledStudents = allActive

            // Check if existing session exists for this class & date
            val existing = viewModel.repository.findExistingSession(currentClass.classId, selectedDate)
            existingSession = existing

            studentStatuses.clear()
            undoStack.clear()

            if (existing != null) {
                // Prepopulate with existing records
                val records = viewModel.repository.getRecordsForSessionDirect(existing.sessionId)
                periodCount = existing.periodCount
                allActive.forEach { student ->
                    val rec = records.find { it.studentId == student.studentId }
                    studentStatuses[student.studentId] = rec?.status ?: "Present"
                }
            } else {
                // DEFAULT = PRESENT for all students! (Rule #16)
                allActive.forEach { student ->
                    studentStatuses[student.studentId] = "Present"
                }
            }

            isLoadingStudents = false
        }
    }

    LaunchedEffect(selectedClass, selectedDate) {
        loadStudentsAndCheckExisting()
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header / Class Selector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "روزانہ حاضری (Take Attendance)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFEFF6FF)
                            ) {
                                Text(
                                    text = "سیشن: $activeSession",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF1D4ED8)
                                )
                            }
                        }

                        // Class Dropdown
                        ExposedDropdownMenuBox(
                            expanded = classDropdownExpanded,
                            onExpandedChange = { classDropdownExpanded = !classDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedClass?.className ?: "کلاس منتخب کریں...",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("کلاس منتخب کریں") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = classDropdownExpanded,
                                onDismissRequest = { classDropdownExpanded = false }
                            ) {
                                classes.forEach { cls ->
                                    DropdownMenuItem(
                                        text = { Text("${cls.className} (${cls.level} • Sec ${cls.section})") },
                                        onClick = {
                                            selectedClass = cls
                                            classDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Class Details & Quick Change Action
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "کل شامل طلبہ: ${enrolledStudents.size}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.clickable { showClassSetupDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.School,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "کلاس / طلبہ تبدیل کریں",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Date & Period row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = selectedDate,
                                onValueChange = { selectedDate = it },
                                label = { Text("تاریخ (YYYY-MM-DD)") },
                                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.weight(1.3f)
                            )

                            // Number of Periods (1, 2, 3)
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("پیریڈز:", style = MaterialTheme.typography.bodySmall)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf(1, 2, 3).forEach { p ->
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (periodCount == p) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            modifier = Modifier
                                                .clickable { periodCount = p }
                                                .padding(2.dp)
                                        ) {
                                            Text(
                                                text = "$p",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (periodCount == p) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Duplicate Session Warning Banner (Rule #19)
            if (existingSession != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "اس تاریخ کی حاضری پہلے سے موجود ہے!",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = "آپ موجودہ سیشن کو ایڈٹ کر سکتے ہیں یا نئی حاضری محفوظ کر سکتے ہیں۔",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                    }
                }
            }

            // Fast Control Bar (Present All, Absent All, Undo, Status Count)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val presentCount = studentStatuses.count { it.value == "Present" }
                            val absentCount = studentStatuses.count { it.value == "Absent" }
                            val leaveCount = studentStatuses.count { it.value == "Leave" }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(shape = RoundedCornerShape(6.dp), color = EmeraldLight) {
                                    Text(
                                        text = "حاضر: $presentCount",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = EmeraldPresent
                                    )
                                }
                                Surface(shape = RoundedCornerShape(6.dp), color = CrimsonLight) {
                                    Text(
                                        text = "غیر حاضر: $absentCount",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = CrimsonAbsent
                                    )
                                }
                                if (leaveCount > 0) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = AmberLight) {
                                        Text(
                                            text = "رخصت: $leaveCount",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = AmberLeave
                                        )
                                    }
                                }
                            }

                            // Undo button
                            OutlinedButton(
                                onClick = {
                                    if (undoStack.isNotEmpty()) {
                                        val last = undoStack.removeAt(undoStack.lastIndex)
                                        studentStatuses[last.first] = last.second
                                    }
                                },
                                enabled = undoStack.isNotEmpty(),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Undo", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick action buttons: Present All / Absent All
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    enrolledStudents.forEach { s ->
                                        undoStack.add(Pair(s.studentId, studentStatuses[s.studentId] ?: "Present"))
                                        studentStatuses[s.studentId] = "Present"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPresent),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("سب حاضر (Present All)", style = MaterialTheme.typography.labelSmall)
                            }

                            OutlinedButton(
                                onClick = {
                                    enrolledStudents.forEach { s ->
                                        undoStack.add(Pair(s.studentId, studentStatuses[s.studentId] ?: "Present"))
                                        studentStatuses[s.studentId] = "Absent"
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = CrimsonAbsent)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("سب غیر حاضر", style = MaterialTheme.typography.labelSmall, color = CrimsonAbsent)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Filters & Search
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("All" to "تمام طلبہ", "Absent" to "صرف غیر حاضر", "Present" to "صرف حاضر").forEach { (key, label) ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (filterMode == key) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .clickable { filterMode = key }
                                        .padding(2.dp)
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (filterMode == key) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("نام یا رول نمبر تلاش کریں...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Student Attendance List (1-Tap Toggle between Present / Absent / Leave)
            val filteredList = enrolledStudents.filter { s ->
                val status = studentStatuses[s.studentId] ?: "Present"
                val matchesFilter = when (filterMode) {
                    "Absent" -> status == "Absent"
                    "Present" -> status == "Present"
                    else -> true
                }
                val matchesSearch = searchQuery.isBlank() ||
                        s.name.contains(searchQuery, ignoreCase = true) ||
                        s.rollNumber.contains(searchQuery) ||
                        s.fatherName.contains(searchQuery, ignoreCase = true)
                matchesFilter && matchesSearch
            }

            if (isLoadingStudents) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (filteredList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("کوئی طالب علم موجود نہیں ہے", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            } else {
                items(filteredList) { student ->
                    val currentStatus = studentStatuses[student.studentId] ?: "Present"

                    StudentAttendanceToggleRow(
                        student = student,
                        status = currentStatus,
                        onToggle = {
                            val nextStatus = when (currentStatus) {
                                "Present" -> "Absent"
                                "Absent" -> "Leave"
                                else -> "Present"
                            }
                            undoStack.add(Pair(student.studentId, currentStatus))
                            studentStatuses[student.studentId] = nextStatus
                        },
                        onSetStatus = { newStatus ->
                            undoStack.add(Pair(student.studentId, currentStatus))
                            studentStatuses[student.studentId] = newStatus
                        },
                        onNotifyParent = {
                            coroutineScope.launch {
                                val stats = viewModel.repository.getStudentAttendanceStats(
                                    studentId = student.studentId,
                                    classId = selectedClass?.classId ?: "",
                                    date = selectedDate,
                                    missedPeriodsToday = periodCount
                                )
                                val user = currentUser ?: InitialSeedData.defaultUser
                                val dept = ParentNotificationHelper.resolveDepartment(user.department, selectedClass?.subjectName, user.defaultSubject)
                                val clg = user.college.ifBlank { ParentNotificationHelper.DEFAULT_COLLEGE_NAME }

                                generatedNotificationMsg = ParentNotificationHelper.generateAbsenceNotification(
                                    studentName = student.name,
                                    rollNumber = student.rollNumber,
                                    date = selectedDate,
                                    missedPeriodsToday = stats.missedPeriodsToday,
                                    monthlyPresent = stats.presentPeriods,
                                    monthlyAbsent = stats.absentPeriods,
                                    attendancePercentage = stats.attendancePercentage,
                                    consecutiveAbsentDays = stats.consecutiveAbsentDays,
                                    teacherName = user.name,
                                    department = dept,
                                    collegeName = clg
                                )
                                studentForNotification = student
                            }
                        }
                    )
                }
            }
        }

        // Bottom Sticky Action Bar: Save Attendance
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val presentCount = studentStatuses.count { it.value == "Present" }
                val absentCount = studentStatuses.count { it.value == "Absent" }
                val totalCount = studentStatuses.size
                val percentage = if (totalCount > 0) (presentCount.toDouble() / totalCount) * 100.0 else 0.0

                Column {
                    Text(
                        text = "کل: $totalCount | حاضر: $presentCount | غیر حاضر: $absentCount",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "حاضری شرح: ${String.format(Locale.US, "%.1f", percentage)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (percentage >= 75.0) EmeraldPresent else CrimsonAbsent
                    )
                }

                Button(
                    onClick = {
                        val cls = selectedClass ?: return@Button
                        viewModel.saveAttendance(
                            classId = cls.classId,
                            subjectId = cls.subjectId,
                            date = selectedDate,
                            periodCount = periodCount,
                            notes = notes,
                            studentStatuses = studentStatuses.toMap(),
                            existingSessionId = existingSession?.sessionId,
                            onComplete = { summary ->
                                attendanceSummary = summary
                                showSummaryDialog = true
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPresent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("save_attendance_button")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("حاضری محفوظ کریں")
                }
            }
        }

        // Attendance Summary Dialog (Section 20)
        if (showSummaryDialog && attendanceSummary != null) {
            val sum = attendanceSummary!!
            AlertDialog(
                onDismissRequest = { showSummaryDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPresent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حاضری کا خلاصہ (Summary)")
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "کلاس: ${selectedClass?.className}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(text = "تاریخ: $selectedDate • کل پیریڈز: ${sum.periodCount}")
                        HorizontalDivider()
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("کل طلبہ (Total):")
                            Text("${sum.totalStudents}", fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("حاضر طلبہ (Present):")
                            Text("${sum.presentCount}", fontWeight = FontWeight.Bold, color = EmeraldPresent)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("غیر حاضر طلبہ (Absent):")
                            Text("${sum.absentCount}", fontWeight = FontWeight.Bold, color = CrimsonAbsent)
                        }
                        if (sum.leaveCount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("رخصت پر (Leave):")
                                Text("${sum.leaveCount}", fontWeight = FontWeight.Bold, color = AmberLeave)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("حاضری کا تناسب (Percentage):")
                            Text("${String.format(Locale.US, "%.1f", sum.percentage)}%", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                },
                confirmButton = {
                    if (sum.absentCount > 0) {
                        Button(
                            onClick = {
                                showSummaryDialog = false
                                selectedClass?.let { cls ->
                                    onNavigateToParentMessaging(cls.classId, selectedDate, sum.periodCount)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("والدین کو میسج بھیجیں (${sum.absentCount})")
                        }
                    } else {
                        Button(onClick = { showSummaryDialog = false }) {
                            Text("مکمل (Done)")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSummaryDialog = false }) {
                        Text("بند کریں")
                    }
                }
            )
        }

        // Parent Notification Dialog
        studentForNotification?.let { s ->
            val user = currentUser ?: InitialSeedData.defaultUser
            val dept = ParentNotificationHelper.resolveDepartment(user.department, selectedClass?.subjectName, user.defaultSubject)
            ParentNotificationDialog(
                student = s,
                notificationType = NotificationType.ABSENCE,
                initialMessage = generatedNotificationMsg,
                parentPhone = s.phone,
                teacherName = user.name,
                teacherDepartment = dept,
                collegeName = user.college,
                onDismiss = { studentForNotification = null }
            )
        }

        // Change Class & Student Selection Dialog
        if (showClassSetupDialog) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showClassSetupDialog = false },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ProfessorSetupScreen(
                        viewModel = viewModel,
                        isEditMode = true,
                        onSetupCompleted = { newClassId ->
                            showClassSetupDialog = false
                            loadStudentsAndCheckExisting()
                        },
                        onCancel = { showClassSetupDialog = false }
                    )
                }
            }
        }
    }
}

@Composable
fun StudentAttendanceToggleRow(
    student: StudentEntity,
    status: String,
    onToggle: () -> Unit,
    onSetStatus: (String) -> Unit,
    onNotifyParent: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val (bgColor, textColor, label) = when (status) {
        "Present" -> Triple(EmeraldLight, EmeraldPresent, "حاضر (P)")
        "Absent" -> Triple(CrimsonLight, CrimsonAbsent, "غیر حاضر (A)")
        else -> Triple(AmberLight, Color(0xFFB45309), "رخصت (L)")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .testTag("student_row_${student.rollNumber}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (status) {
                "Absent" -> Color(0xFFFFF1F2)
                "Leave" -> Color(0xFFFFFBEB)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = student.rollNumber,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = student.name,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (student.session.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = student.session,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                    Text(
                        text = "ولدیت: ${student.fatherName} • ${student.groupName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick Call / SMS & Direct Present/Absent Easy Controls
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val phoneToContact = student.guardianPhone.ifBlank { student.phone }
                if (status == "Absent" && phoneToContact.isNotBlank()) {
                    IconButton(
                        onClick = { CommunicationHelper.makePhoneCall(context, phoneToContact) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call Guardian", tint = CrimsonAbsent, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = {
                            if (onNotifyParent != null) {
                                onNotifyParent()
                            } else {
                                val msg = "محترم والدین! آپ کے فرزند ${student.name} (رول نمبر ${student.rollNumber}) آج کالج سے غیر حاضر ہیں۔ گورنمنٹ ایسوسی ایٹ کالج مخدوم رشید ملتان"
                                CommunicationHelper.sendSms(context, phoneToContact, msg)
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "SMS Guardian", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                }

                // Direct Present Button (حاضر)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (status == "Present") EmeraldPresent else Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (status == "Present") EmeraldPresent else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.clickable { onSetStatus("Present") }
                ) {
                    Text(
                        text = "حاضر",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (status == "Present") Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Direct Absent Button (غیر حاضر)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (status == "Absent") CrimsonAbsent else Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (status == "Absent") CrimsonAbsent else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.clickable { onSetStatus("Absent") }
                ) {
                    Text(
                        text = "غیر حاضر",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (status == "Absent") Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
