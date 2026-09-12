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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import com.example.ui.theme.CrimsonAbsent
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPresent
import com.example.ui.viewmodel.CollegeViewModel
import com.example.util.CsvImportSummary
import com.example.util.CsvParseResult
import com.example.util.CsvParserHelper
import com.example.util.ParsedCsvRow
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NominalRollScreen(
    viewModel: CollegeViewModel,
    modifier: Modifier = Modifier
) {
    val ownerId by viewModel.currentOwnerId.collectAsStateWithLifecycle()
    val allStudents by viewModel.repository.getActiveStudents(ownerId).collectAsStateWithLifecycle(emptyList())
    val archivedStudents by viewModel.repository.getArchivedStudents(ownerId).collectAsStateWithLifecycle(emptyList())
    val validationIssues by viewModel.validationIssues.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf(
        "First Year",
        "Second Year",
        "تمام طلبہ (${allStudents.size})",
        "آرکائیو شدہ (${archivedStudents.size})",
        "انتباہات (${validationIssues.size})"
    )

    var searchQuery by remember { mutableStateOf("") }
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var studentToEdit by remember { mutableStateOf<StudentEntity?>(null) }
    var showBulkImportDialog by remember { mutableStateOf(false) }

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
                    placeholder = { Text("نام، ولدیت یا رول نمبر تلاش کریں...") },
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
                                color = if (index == 4 && validationIssues.isNotEmpty()) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
            }

            // Tab 0, 1, 2: Active Students
            if (selectedTabIndex in 0..2) {
                val displayedStudents = allStudents.filter { s ->
                    val matchesTab = when (selectedTabIndex) {
                        0 -> s.className == "First Year"
                        1 -> s.className == "Second Year"
                        else -> true
                    }
                    val matchesSearch = searchQuery.isBlank() ||
                            s.name.contains(searchQuery, ignoreCase = true) ||
                            s.rollNumber.contains(searchQuery) ||
                            s.fatherName.contains(searchQuery, ignoreCase = true) ||
                            s.phone.contains(searchQuery)
                    matchesTab && matchesSearch
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
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
                                onRestore = {}
                            )
                        }
                    }
                }
            } else if (selectedTabIndex == 3) {
                // Tab 3: Archived Students
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
                                onRestore = { viewModel.restoreStudent(student.studentId) }
                            )
                        }
                    }
                }
            } else if (selectedTabIndex == 4) {
                // Tab 4: Validation Issues List
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
    }
}

@Composable
fun StudentMasterCard(
    student: StudentEntity,
    isArchived: Boolean,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onRestore: () -> Unit
) {
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
                    Column {
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
                    }
                }

                Row {
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
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${student.className} • Section ${student.section} • ${student.groupName}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (student.electiveSubjectsRaw.isNotBlank()) {
                    Text(
                        text = "اختیاری: ${student.electiveSubjectsRaw}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
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
    var className by remember { mutableStateOf(student?.className ?: "First Year") }
    var section by remember { mutableStateOf(student?.section ?: "B") }
    var groupName by remember { mutableStateOf(student?.groupName ?: "Pre-Medical") }
    var electiveSubjects by remember { mutableStateOf(student?.electiveSubjectsRaw ?: "") }

    var classExpanded by remember { mutableStateOf(false) }
    var sectionExpanded by remember { mutableStateOf(false) }
    var groupExpanded by remember { mutableStateOf(false) }

    // REAL-TIME DUPLICATE DETECTION
    val duplicateStudent = remember(rollNumber, className) {
        val trimmedRoll = rollNumber.trim()
        if (trimmedRoll.isBlank()) null
        else allStudents.find {
            it.rollNumber.trim().equals(trimmedRoll, ignoreCase = true) &&
            it.className.trim().equals(className.trim(), ignoreCase = true) &&
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
                modifier = Modifier.fillMaxWidth().height(440.dp),
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
                                text = "انتباہ: رول نمبر ${duplicateStudent.rollNumber} پہلے ہی '${duplicateStudent.name}' (سیکشن ${duplicateStudent.section}) کو الاٹ ہے۔ مختلف رول نمبر درج کریں۔",
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
                        label = { Text("فون نمبر (03...)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )
                }

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

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Class Dropdown
                    ExposedDropdownMenuBox(
                        expanded = classExpanded,
                        onExpandedChange = { classExpanded = !classExpanded },
                        modifier = Modifier.weight(1.2f)
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
                }

                // Group Dropdown
                ExposedDropdownMenuBox(
                    expanded = groupExpanded,
                    onExpandedChange = { groupExpanded = !groupExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("گروپ / شعبہ (Group)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = groupExpanded, onDismissRequest = { groupExpanded = false }) {
                        listOf("Pre-Medical", "Pre-Engineering", "ICS", "General Science", "Humanities/Arts", "Commerce").forEach { grp ->
                            DropdownMenuItem(text = { Text(grp) }, onClick = { groupName = grp; groupExpanded = false })
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

