package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.ui.components.StudentProfileDialog
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.NominalRollValidator
import com.example.data.model.StudentEntity
import com.example.data.model.TeacherEntity
import com.example.ui.components.CommunicationHelper
import com.example.ui.theme.CrimsonAbsent
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPresent
import com.example.ui.viewmodel.CollegeViewModel
import com.example.util.CsvImportSummary
import com.example.util.CsvParseResult
import com.example.util.CsvParserHelper
import com.example.util.ParsedCsvRow
import com.example.util.SessionHelper
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NominalRollScreen(
    viewModel: CollegeViewModel,
    modifier: Modifier = Modifier
) {
    val ownerId by viewModel.currentOwnerId.collectAsStateWithLifecycle()
    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
    val allStudents by viewModel.repository.getActiveStudents(ownerId).collectAsStateWithLifecycle(emptyList())
    val archivedStudents by viewModel.repository.getArchivedStudents(ownerId).collectAsStateWithLifecycle(emptyList())
    val allTeachers by viewModel.allTeachers.collectAsStateWithLifecycle()
    val validationIssues by viewModel.validationIssues.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedSessionFilter by remember { mutableStateOf("تمام سیشنز") }
    var selectedGroupFilter by remember { mutableStateOf("تمام گروپس") }
    var selectedTeacherDeptFilter by remember { mutableStateOf("تمام شعبہ جات") }
    var teacherToClaim by remember { mutableStateOf<TeacherEntity?>(null) }

    val tabTitles = listOf(
        "فرسٹ ایئر",
        "سیکنڈ ایئر",
        "تمام طلبہ (${allStudents.size})",
        "اساتذہ کرام (${allTeachers.size})",
        "آرکائیو شدہ (${archivedStudents.size})",
        "انتباہات (${validationIssues.size})"
    )

    var searchQuery by remember { mutableStateOf("") }
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var studentToEdit by remember { mutableStateOf<StudentEntity?>(null) }
    var showBulkImportDialog by remember { mutableStateOf(false) }
    var selectedStudentForProfile by remember { mutableStateOf<StudentEntity?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header & Action Bar
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ماسٹر نامینل رول (Master Nominal Roll)",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "گورنمنٹ کالج مخدوم رشید ملتان • پنجاب ہائر ایجوکیشن ڈیٹا",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showBulkImportDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("امپورٹ CSV")
                        }

                        Button(
                            onClick = { showAddStudentDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("طالب علم شامل")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("نام، ولدیت، رول نمبر یا شعبہ تلاش کریں...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Tabs
            PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (index == 5 && validationIssues.isNotEmpty()) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
            }

            // Academic Session Filter Chips Row for Student Tabs
            if (selectedTabIndex in 0..2) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val filterOptions = listOf("تمام سیشنز", activeSession) + SessionHelper.DEFAULT_SESSIONS.filter { it != activeSession }
                    items(filterOptions.distinct()) { opt ->
                        FilterChip(
                            selected = selectedSessionFilter == opt,
                            onClick = { selectedSessionFilter = opt },
                            label = {
                                Text(
                                    text = if (opt == activeSession) "$opt (فعال)" else opt,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                // Academic Groups Filter Row (میڈیکل، نان میڈیکل، آئی سی ایس، وغیرہ)
                val groupOptions = remember(allStudents) {
                    listOf("تمام گروپس") + allStudents.map { it.groupName }.filter { it.isNotBlank() }.distinct()
                }
                if (groupOptions.size > 1) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(groupOptions) { grp ->
                            FilterChip(
                                selected = selectedGroupFilter == grp,
                                onClick = { selectedGroupFilter = grp },
                                label = { Text(grp, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }
                }
            }

            // Tab 0, 1, 2: Active Students
            if (selectedTabIndex in 0..2) {
                val displayedStudents = allStudents.filter { s ->
                    val studentSession = s.session.ifBlank { activeSession }
                    val matchesSession = when (selectedSessionFilter) {
                        "تمام سیشنز" -> true
                        else -> s.session.isBlank() || s.session == selectedSessionFilter
                    }
                    val matchesGroup = when (selectedGroupFilter) {
                        "تمام گروپس" -> true
                        else -> s.groupName.contains(selectedGroupFilter, ignoreCase = true)
                    }
                    val matchesTab = when (selectedTabIndex) {
                        0 -> s.className == "First Year" || SessionHelper.determineLevelForSession(studentSession) == SessionHelper.AcademicLevel.FIRST_YEAR
                        1 -> s.className == "Second Year" || SessionHelper.determineLevelForSession(studentSession) == SessionHelper.AcademicLevel.SECOND_YEAR
                        else -> true
                    }
                    val matchesSearch = searchQuery.isBlank() ||
                            s.name.contains(searchQuery, ignoreCase = true) ||
                            s.rollNumber.contains(searchQuery) ||
                            s.fatherName.contains(searchQuery, ignoreCase = true) ||
                            s.phone.contains(searchQuery) ||
                            s.guardianPhone.contains(searchQuery)
                    matchesTab && matchesSearch && matchesSession && matchesGroup
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (displayedStudents.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("اس سیکشن میں کوئی طالب علم موجود نہیں ہے", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        items(displayedStudents) { student ->
                            StudentMasterCard(
                                student = student,
                                isArchived = false,
                                onEdit = { studentToEdit = student },
                                onArchive = { viewModel.archiveStudent(student.studentId) },
                                onRestore = {},
                                onViewProfile = { selectedStudentForProfile = student }
                            )
                        }
                    }
                }
            } else if (selectedTabIndex == 3) {
                // Tab 3: Teachers Directory (اساتذہ کرام)
                val allDepts = remember(allTeachers) {
                    listOf("تمام شعبہ جات") + allTeachers.map { it.department }.filter { it.isNotBlank() }.distinct()
                }
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allDepts) { dept ->
                        FilterChip(
                            selected = selectedTeacherDeptFilter == dept,
                            onClick = { selectedTeacherDeptFilter = dept },
                            label = { Text(dept, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                val displayedTeachers = allTeachers.filter { t ->
                    val matchesDept = selectedTeacherDeptFilter == "تمام شعبہ جات" || t.department == selectedTeacherDeptFilter
                    val matchesSearch = searchQuery.isBlank() ||
                            t.name.contains(searchQuery, ignoreCase = true) ||
                            t.department.contains(searchQuery, ignoreCase = true) ||
                            t.designation.contains(searchQuery, ignoreCase = true) ||
                            t.teacherId.contains(searchQuery, ignoreCase = true)
                    matchesDept && matchesSearch
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (displayedTeachers.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                                Text("کوئی استاد نہیں ملا", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        items(displayedTeachers) { teacher ->
                            TeacherMasterCard(
                                teacher = teacher,
                                onClaim = { teacherToClaim = teacher }
                            )
                        }
                    }
                }
            } else if (selectedTabIndex == 4) {
                // Tab 4: Archived Students
                val displayedArchived = archivedStudents.filter { s ->
                    searchQuery.isBlank() ||
                            s.name.contains(searchQuery, ignoreCase = true) ||
                            s.rollNumber.contains(searchQuery) ||
                            s.fatherName.contains(searchQuery, ignoreCase = true)
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (displayedArchived.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "کوئی آرکائیو شدہ طالب علم نہیں ہے",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "کسی طالب علم کو آرکائیو کرنے سے ان کی سابقہ حاضری اور رزلٹ ریکارڈ مکمل محفوظ رہتا ہے۔",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(displayedArchived) { student ->
                            StudentMasterCard(
                                student = student,
                                isArchived = true,
                                onEdit = { studentToEdit = student },
                                onArchive = {},
                                onRestore = { viewModel.restoreStudent(student.studentId) },
                                onViewProfile = { selectedStudentForProfile = student }
                            )
                        }
                    }
                }
            } else if (selectedTabIndex == 5) {
                // Tab 5: Validation Issues List
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (validationIssues.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = EmeraldLight),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPresent)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("بہترین! نامینل رول میں کوئی ڈپلیکیٹ یا غلط اندراج موجود نہیں ہے۔")
                                }
                            }
                        }
                    } else {
                        items(validationIssues) { issue ->
                            ValidationIssueCard(
                                issue = issue,
                                onFixStudent = { studentId ->
                                    val s = allStudents.find { it.studentId == studentId }
                                    studentToEdit = s
                                }
                            )
                        }
                    }
                }
            }
        }

        // Teacher Claim Profile Dialog
        if (teacherToClaim != null) {
            val teacher = teacherToClaim!!
            ClaimTeacherProfileDialog(
                teacher = teacher,
                onDismiss = { teacherToClaim = null },
                onClaim = { email ->
                    viewModel.claimTeacherProfile(teacher.teacherId, email)
                    teacherToClaim = null
                }
            )
        }

        // Add or Edit Student Dialog with DUPLICATE DETECTION
        if (showAddStudentDialog || studentToEdit != null) {
            AddEditStudentDialog(
                student = studentToEdit,
                ownerId = ownerId,
                allStudents = allStudents,
                onDismiss = {
                    showAddStudentDialog = false
                    studentToEdit = null
                },
                onSave = { savedStudent ->
                    if (studentToEdit != null) {
                        viewModel.updateStudent(savedStudent)
                    } else {
                        viewModel.addStudentToNominalRoll(savedStudent)
                    }
                    showAddStudentDialog = false
                    studentToEdit = null
                }
            )
        }

        // Bulk Import CSV Dialog with Android File Picker & Preview
        if (showBulkImportDialog) {
            BulkImportCsvDialog(
                ownerId = ownerId,
                allStudents = allStudents,
                viewModel = viewModel,
                onDismiss = { showBulkImportDialog = false }
            )
        }

        // Student Profile & Semester Attendance Trend Dialog (D3 / Recharts visualization)
        selectedStudentForProfile?.let { student ->
            StudentProfileDialog(
                student = student,
                viewModel = viewModel,
                onDismiss = { selectedStudentForProfile = null }
            )
        }
    }
}

@Composable
fun StudentMasterCard(
    student: StudentEntity,
    isArchived: Boolean,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onRestore: () -> Unit,
    onViewProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val contactPhone = student.guardianPhone.ifBlank { student.phone }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student_card_${student.rollNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isArchived) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = CircleShape,
                        color = if (isArchived) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = student.rollNumber,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isArchived) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(
                        modifier = Modifier.clickable { onViewProfile() }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = student.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isArchived) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = CrimsonLight
                                ) {
                                    Text(
                                        text = "آرکائیو شدہ",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CrimsonAbsent
                                    )
                                }
                            }
                        }
                        Text(
                            text = "ولدیت: ${student.fatherName} • فون: ${student.phone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (student.guardianPhone.isNotBlank()) {
                            Text(
                                text = "سرپرست / والد فون: ${student.guardianPhone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF0D9488)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isArchived) {
                        OutlinedButton(
                            onClick = onRestore,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = "Restore", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("بحال کریں", style = MaterialTheme.typography.labelSmall)
                        }
                    } else {
                        IconButton(onClick = onViewProfile, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.ShowChart, contentDescription = "حاضری رجحان", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onArchive, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Archive, contentDescription = "Archive", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "${student.className} • ${student.groupName}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (student.marks != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = "نمبر: ${student.marks}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFB45309)
                            )
                        }
                    }

                    if (student.session.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFEFF6FF)
                        ) {
                            Text(
                                text = student.session,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1D4ED8)
                            )
                        }
                    }
                }

                val subjectList = listOf(student.subject1, student.subject2, student.subject3).filter { it.isNotBlank() }
                val subjectsDisplay = if (subjectList.isNotEmpty()) subjectList.joinToString(" • ") else student.electiveSubjectsRaw
                if (subjectsDisplay.isNotBlank()) {
                    Text(
                        text = subjectsDisplay,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                }
            }

            // Quick Call, WhatsApp & SMS Bar for direct communication
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile & Trend View Button
                OutlinedButton(
                    onClick = onViewProfile,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.ShowChart,
                        contentDescription = "پروفائل و رجحان",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("پروفائل و رجحان", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }

                if (contactPhone.isNotBlank() && !isArchived) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // WhatsApp
                        Button(
                            onClick = {
                                val defaultMsg = "محترم والد/سرپرست، گورنمنٹ ایسوسی ایٹ کالج مخدوم رشید ملتان کی جانب سے آپ کے صاحبزادے ${student.name} (رول نمبر ${student.rollNumber}) کے حوالے سے اطلاع۔"
                                CommunicationHelper.openWhatsApp(context, contactPhone, defaultMsg)
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "WhatsApp", modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("واٹس ایپ", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }

                        // SMS
                        OutlinedButton(
                            onClick = {
                                val defaultMsg = "محترم والد/سرپرست، گورنمنٹ ایسوسی ایٹ کالج مخدوم رشید ملتان کی جانب سے آپ کے صاحبزادے ${student.name} (رول نمبر ${student.rollNumber}) کے حوالے سے اطلاع۔"
                                CommunicationHelper.openSmsApp(context, contactPhone, defaultMsg)
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "SMS", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ایس ایم ایس", style = MaterialTheme.typography.labelSmall)
                        }

                        // Call
                        Button(
                            onClick = { CommunicationHelper.makePhoneCall(context, contactPhone) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPresent),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("کال کریں", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherMasterCard(
    teacher: TeacherEntity,
    onClaim: () -> Unit
) {
    val isRegistered = teacher.isRegistered.equals("Yes", ignoreCase = true)
    val isAdmin = teacher.role.equals("admin", ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("teacher_card_${teacher.teacherId}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = CircleShape,
                        color = if (isAdmin) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = teacher.teacherId.replace("t_", "#"),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isAdmin) Color(0xFFB45309) else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = teacher.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isAdmin) Color(0xFFFEF3C7) else Color(0xFFEFF6FF)
                            ) {
                                Text(
                                    text = if (isAdmin) "ایڈمنسٹریٹر" else "استاد",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isAdmin) Color(0xFFB45309) else Color(0xFF1D4ED8)
                                )
                            }
                        }
                        Text(
                            text = "${teacher.designation} • شعبہ ${teacher.department}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isRegistered && teacher.registeredEmail.isNotBlank()) {
                            Text(
                                text = "ای میل: ${teacher.registeredEmail}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF059669)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isRegistered) EmeraldLight else Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = if (isRegistered) "منسلک / رجسٹرڈ" else "غیر رجسٹرڈ",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isRegistered) EmeraldPresent else Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "شناختی کوڈ: ${teacher.teacherId}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!isRegistered) {
                    Button(
                        onClick = onClaim,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("پروفائل منسلک کریں (Claim)", style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    OutlinedButton(
                        onClick = onClaim,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ای میل تبدیل کریں", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun ClaimTeacherProfileDialog(
    teacher: TeacherEntity,
    onDismiss: () -> Unit,
    onClaim: (String) -> Unit
) {
    var emailInput by remember { mutableStateOf(teacher.registeredEmail) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "پروفائل کلیم کریں (Claim Teacher Profile)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = teacher.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${teacher.designation} • شعبہ ${teacher.department}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "شناختی نمبر: ${teacher.teacherId} • کردار: ${if (teacher.role == "admin") "ایڈمنسٹریٹر" else "استاد"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = "براہ کرم اپنا کالج / دفتری ای میل درج کریں تاکہ آپ کی شناخت سسٹم سے منسلک ہو سکے:",
                    style = MaterialTheme.typography.bodySmall
                )

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("ای میل پتہ (Email Address)") },
                    placeholder = { Text("teacher@gacmr.edu.pk") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onClaim(emailInput) },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("منسلک کریں")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("منسوخ")
            }
        }
    )
}

@Composable
fun ValidationIssueCard(
    issue: NominalRollValidator.ValidationIssue,
    onFixStudent: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = issue.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF92400E)
                    )
                    Text(
                        text = issue.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB45309)
                    )
                }
            }

            issue.studentId?.let { sId ->
                TextButton(onClick = { onFixStudent(sId) }) {
                    Text("درست کریں")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditStudentDialog(
    student: StudentEntity?,
    ownerId: String,
    allStudents: List<StudentEntity>,
    onDismiss: () -> Unit,
    onSave: (StudentEntity) -> Unit
) {
    var rollNumber by remember { mutableStateOf(student?.rollNumber ?: "") }
    var name by remember { mutableStateOf(student?.name ?: "") }
    var fatherName by remember { mutableStateOf(student?.fatherName ?: "") }
    var phone by remember { mutableStateOf(student?.phone ?: "03") }
    var guardianPhone by remember { mutableStateOf(student?.guardianPhone ?: "") }
    var session by remember { mutableStateOf(student?.session?.ifBlank { "2026–2028" } ?: "2026–2028") }
    var className by remember { mutableStateOf(student?.className ?: "First Year") }
    var section by remember { mutableStateOf(student?.section ?: "B") }
    var groupName by remember { mutableStateOf(student?.groupName ?: "Pre-Medical") }
    var electiveSubjects by remember { mutableStateOf(student?.electiveSubjectsRaw ?: "") }

    var sessionExpanded by remember { mutableStateOf(false) }
    var classExpanded by remember { mutableStateOf(false) }
    var sectionExpanded by remember { mutableStateOf(false) }
    var groupExpanded by remember { mutableStateOf(false) }

    // REAL-TIME DUPLICATE DETECTION
    val duplicateStudent = remember(rollNumber, className, session) {
        val trimmedRoll = rollNumber.trim()
        if (trimmedRoll.isBlank()) null
        else allStudents.find {
            it.rollNumber.trim().equals(trimmedRoll, ignoreCase = true) &&
            (it.className.trim().equals(className.trim(), ignoreCase = true) || it.session.trim().equals(session.trim(), ignoreCase = true)) &&
            it.studentId != student?.studentId
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (student != null) "طالب علم کا ڈیٹا اپ ڈیٹ کریں" else "نیا طالب علم شامل کریں")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().height(480.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Duplicate warning banner if detected
                if (duplicateStudent != null) {
                    Surface(
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFF87171), RoundedCornerShape(8.dp))
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "انتباہ: رول نمبر ${duplicateStudent.rollNumber} پہلے ہی '${duplicateStudent.name}' کو الاٹ ہے۔ مختلف رول نمبر درج کریں۔",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF991B1B)
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = rollNumber,
                        onValueChange = { rollNumber = it },
                        label = { Text("رول نمبر") },
                        isError = duplicateStudent != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.8f)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("طالب علم فون") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )
                }

                OutlinedTextField(
                    value = guardianPhone,
                    onValueChange = { guardianPhone = it },
                    label = { Text("والد / سرپرست فون نمبر") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("طالب علم کا نام") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fatherName,
                    onValueChange = { fatherName = it },
                    label = { Text("والد کا نام") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Session & Class Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Session Dropdown
                    ExposedDropdownMenuBox(
                        expanded = sessionExpanded,
                        onExpandedChange = { sessionExpanded = !sessionExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = session,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("تعلیمی سیشن") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sessionExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(expanded = sessionExpanded, onDismissRequest = { sessionExpanded = false }) {
                            SessionHelper.DEFAULT_SESSIONS.forEach { ssn ->
                                DropdownMenuItem(
                                    text = { Text(ssn) },
                                    onClick = {
                                        session = ssn
                                        className = SessionHelper.determineLevelForSession(ssn).englishTitle
                                        sessionExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Class Dropdown
                    ExposedDropdownMenuBox(
                        expanded = classExpanded,
                        onExpandedChange = { classExpanded = !classExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = className,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("کلاس") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(expanded = classExpanded, onDismissRequest = { classExpanded = false }) {
                            DropdownMenuItem(text = { Text("First Year") }, onClick = { className = "First Year"; classExpanded = false })
                            DropdownMenuItem(text = { Text("Second Year") }, onClick = { className = "Second Year"; classExpanded = false })
                        }
                    }
                }

                // Section & Group Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Section Dropdown
                    ExposedDropdownMenuBox(
                        expanded = sectionExpanded,
                        onExpandedChange = { sectionExpanded = !sectionExpanded },
                        modifier = Modifier.weight(0.8f)
                    ) {
                        OutlinedTextField(
                            value = section,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("سیکشن") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectionExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(expanded = sectionExpanded, onDismissRequest = { sectionExpanded = false }) {
                            listOf("A", "B", "C").forEach { s ->
                                DropdownMenuItem(text = { Text("Section $s") }, onClick = { section = s; sectionExpanded = false })
                            }
                        }
                    }

                    // Group Dropdown
                    ExposedDropdownMenuBox(
                        expanded = groupExpanded,
                        onExpandedChange = { groupExpanded = !groupExpanded },
                        modifier = Modifier.weight(1.2f)
                    ) {
                        OutlinedTextField(
                            value = groupName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("گروپ") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = groupExpanded, onDismissRequest = { groupExpanded = false }) {
                            listOf("Pre-Medical", "Pre-Engineering", "ICS", "General Science", "Humanities/Arts", "Commerce").forEach { grp ->
                                DropdownMenuItem(text = { Text(grp) }, onClick = { groupName = grp; groupExpanded = false })
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = electiveSubjects,
                    onValueChange = { electiveSubjects = it },
                    label = { Text("اختیاری مضامین (کوما سے الگ کریں)") },
                    placeholder = { Text("Physics, Chemistry, Biology") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (rollNumber.isNotBlank() && name.isNotBlank() && duplicateStudent == null) {
                        val finalStudent = (student ?: StudentEntity(
                            studentId = UUID.randomUUID().toString(),
                            rollNumber = rollNumber.trim(),
                            name = name.trim(),
                            fatherName = fatherName.trim(),
                            phone = phone.trim(),
                            guardianPhone = guardianPhone.trim(),
                            session = session.trim(),
                            className = className,
                            section = section,
                            groupName = groupName,
                            electiveSubjectsRaw = electiveSubjects.trim(),
                            academicYearId = "year_2026_27",
                            ownerId = ownerId
                        )).copy(
                            rollNumber = rollNumber.trim(),
                            name = name.trim(),
                            fatherName = fatherName.trim(),
                            phone = phone.trim(),
                            guardianPhone = guardianPhone.trim(),
                            session = session.trim(),
                            className = className,
                            section = section,
                            groupName = groupName,
                            electiveSubjectsRaw = electiveSubjects.trim()
                        )
                        onSave(finalStudent)
                    }
                },
                enabled = rollNumber.isNotBlank() && name.isNotBlank() && duplicateStudent == null
            ) {
                Text("محفوظ کریں")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("منسوخ")
            }
        }
    )
}

/**
 * Android File-Picker Based CSV Import with Preview & Duplicate Protection
 */
@Composable
fun BulkImportCsvDialog(
    ownerId: String,
    allStudents: List<StudentEntity>,
    viewModel: CollegeViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var parseResult by remember { mutableStateOf<CsvParseResult?>(null) }
    var updateExisting by remember { mutableStateOf(true) }
    var importStatusMessage by remember { mutableStateOf<String?>(null) }
    var isImporting by remember { mutableStateOf(false) }
    var activeMode by remember { mutableStateOf("picker") } // "picker" or "paste"
    var rawPasteText by remember {
        mutableStateOf(
            "Roll Number,Student Name,Father Name,Phone,Class,Section,Group,Option\n" +
            "701,Muhammad Arham,Tariq Mahmood,03001234567,First Year,B,Pre-Medical,Biology\n" +
            "702,Abdullah Khan,Muhammad Aslam,03019876543,First Year,B,Pre-Engineering,Mathematics\n" +
            "703,Ali Hassan,Raza Ahmad,03025556677,First Year,B,ICS,Computer Science"
        )
    }
    var parseErrorMessage by remember { mutableStateOf<String?>(null) }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val fileName = getFileNameFromUri(context, uri)
                selectedFileName = fileName
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val lines = CsvParserHelper.readStreamToLines(stream)
                    val result = CsvParserHelper.parseCsvData(lines, allStudents)
                    parseResult = result
                    parseErrorMessage = null
                }
            } catch (e: Exception) {
                parseErrorMessage = "فائل پڑھنے میں مسئلہ پیش آیا: ${e.localizedMessage}"
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isImporting) onDismiss() },
        title = {
            Text("بلک نامینل رول امپورٹ (Real CSV Import)")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().height(460.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // If an import just finished, show summary
                if (importStatusMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EmeraldLight),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPresent)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "امپورٹ مکمل ہو گئی!",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldPresent
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = importStatusMessage ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else if (parseResult == null) {
                    // STEP 1: Selection Mode (File Picker or Text Paste)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { activeMode = "picker" },
                            modifier = Modifier.weight(1f),
                            colors = if (activeMode == "picker") ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text("فائل منتخب کریں")
                        }
                        Button(
                            onClick = { activeMode = "paste" },
                            modifier = Modifier.weight(1f),
                            colors = if (activeMode == "paste") ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text("ٹیکسٹ پیسٹ")
                        }
                    }

                    if (activeMode == "picker") {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "ڈیوائس سے .csv فائل منتخب کریں",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "لازمی فیلڈز: Roll Number, Student Name, Father Name, Phone, Class, Section, Group, Option",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = {
                                        filePickerLauncher.launch(
                                            arrayOf("text/*", "text/comma-separated-values", "text/csv", "application/csv", "*/*")
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("CSV فائل کا انتخاب کریں")
                                }
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "CSV مواد یہاں پیسٹ کریں (پہلی لائن ہیڈر ہو سکتی ہے):",
                                style = MaterialTheme.typography.bodySmall
                            )
                            OutlinedTextField(
                                value = rawPasteText,
                                onValueChange = { rawPasteText = it },
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                maxLines = 10
                            )
                            Button(
                                onClick = {
                                    val lines = rawPasteText.lines()
                                    val res = CsvParserHelper.parseCsvData(lines, allStudents)
                                    selectedFileName = "Pasted_CSV.csv"
                                    parseResult = res
                                    parseErrorMessage = null
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("ڈیٹا چیک اور پریویو کریں")
                            }
                        }
                    }

                    if (parseErrorMessage != null) {
                        Surface(
                            color = Color(0xFFFEF2F2),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text(
                                text = parseErrorMessage ?: "",
                                color = Color(0xFFDC2626),
                                modifier = Modifier.padding(10.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                } else {
                    // STEP 2: PREVIEW & DUPLICATE RESOLUTION
                    val result = parseResult!!
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "فائل: ${selectedFileName ?: "CSV"}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            TextButton(
                                onClick = {
                                    parseResult = null
                                    selectedFileName = null
                                }
                            ) {
                                Text("دوسری فائل")
                            }
                        }

                        // Stats Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFDCFCE7),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("نئے طلبہ", style = MaterialTheme.typography.labelSmall, color = Color(0xFF166534))
                                    Text("${result.newRows.size}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF166534))
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFEF3C7),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("موجودہ طلبہ", style = MaterialTheme.typography.labelSmall, color = Color(0xFF92400E))
                                    Text("${result.existingMatchRows.size}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF92400E))
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFEE2E2),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("ناقص قطاریں", style = MaterialTheme.typography.labelSmall, color = Color(0xFF991B1B))
                                    Text("${result.invalidRows.size}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF991B1B))
                                }
                            }
                        }

                        // Duplicate Handling Policy
                        if (result.existingMatchRows.isNotEmpty()) {
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = "ڈپلیکیٹ / موجودہ طلبہ کا طریقہ کار:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable { updateExisting = true },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(selected = updateExisting, onClick = { updateExisting = true })
                                        Text("معلومات اپ ڈیٹ کریں (سابقہ حاضری و ریکارڈ محفوظ رہیں گے)", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable { updateExisting = false },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(selected = !updateExisting, onClick = { updateExisting = false })
                                        Text("ڈپلیکیٹ طلبہ کو چھوڑ دیں (Skip Duplicates)", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }

                        // Preview Table
                        Text(
                            text = "امپورٹ پریویو (${result.validRows.size} درست قطاریں):",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                .padding(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(result.allParsedRows) { row ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when {
                                        !row.isValid -> Color(0xFFFEF2F2)
                                        row.isDuplicateOfExisting -> Color(0xFFFFFBEB)
                                        else -> MaterialTheme.colorScheme.surface
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = when {
                                                    !row.isValid -> Color(0xFFFCA5A5)
                                                    row.isDuplicateOfExisting -> Color(0xFFFDE68A)
                                                    else -> Color(0xFFBBF7D0)
                                                }
                                            ) {
                                                Text(
                                                    text = when {
                                                        !row.isValid -> "ناقص"
                                                        row.isDuplicateOfExisting -> "موجودہ"
                                                        else -> "نیا"
                                                    },
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    text = "${row.rollNumber}. ${row.name}",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                                )
                                                Text(
                                                    text = "${row.className} • Sec ${row.section} • ${row.groupName}${if (row.optionalSubjects.isNotBlank()) " • اختیاری: " + row.optionalSubjects else ""}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                if (row.validationError != null) {
                                                    Text(
                                                        text = "خرابی: ${row.validationError}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color(0xFFDC2626)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (importStatusMessage != null) {
                Button(onClick = onDismiss) {
                    Text("مکمل (Done)")
                }
            } else if (parseResult != null) {
                val validCount = parseResult!!.validRows.size
                Button(
                    onClick = {
                        val result = parseResult ?: return@Button
                        isImporting = true
                        viewModel.importCsvStudents(
                            parsedRows = result.validRows,
                            updateExisting = updateExisting,
                            onComplete = { summary ->
                                isImporting = false
                                importStatusMessage = "نتیجہ: ${summary.importedCount} نئے شامل کیے گئے، ${summary.updatedCount} اپ ڈیٹ کیے گئے، ${summary.skippedCount} چھوڑے گئے، اور ${summary.failedCount} ناقص پائے گئے۔"
                            }
                        )
                    },
                    enabled = validCount > 0 && !isImporting
                ) {
                    Text(if (isImporting) "امپورٹ ہو رہا ہے..." else "امپورٹ شروع کریں ($validCount طلبہ)")
                }
            }
        },
        dismissButton = {
            if (importStatusMessage == null) {
                TextButton(onClick = onDismiss, enabled = !isImporting) {
                    Text("منسوخ")
                }
            }
        }
    )
}

private fun getFileNameFromUri(context: Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx != -1) {
                        result = it.getString(idx)
                    }
                }
            }
        } catch (e: Exception) {
            // fallback
        }
    }
    if (result == null) {
        result = uri.path?.substringAfterLast('/')
    }
    return result ?: "students.csv"
}

