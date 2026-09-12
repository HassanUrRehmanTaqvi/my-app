package com.example.ui.screens

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
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NominalRollScreen(
    viewModel: CollegeViewModel,
    modifier: Modifier = Modifier
) {
    val ownerId by viewModel.currentOwnerId.collectAsStateWithLifecycle()
    val allStudents by viewModel.repository.getActiveStudents(ownerId).collectAsStateWithLifecycle(emptyList())
    val validationIssues by viewModel.validationIssues.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("First Year (11th)", "Second Year (12th)", "تمام طلبہ (${allStudents.size})", "انتباہات (${validationIssues.size})")

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
                                color = if (index == 3 && validationIssues.isNotEmpty()) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
            }

            // Filter Students
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

            if (selectedTabIndex == 3) {
                // Validation Issues List
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
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayedStudents) { student ->
                        StudentMasterCard(
                            student = student,
                            onEdit = { studentToEdit = student },
                            onArchive = { viewModel.archiveStudent(student.studentId) }
                        )
                    }
                }
            }
        }

        // Add or Edit Student Dialog
        if (showAddStudentDialog || studentToEdit != null) {
            AddEditStudentDialog(
                student = studentToEdit,
                ownerId = ownerId,
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

        // Bulk Import CSV Dialog (Section 47)
        if (showBulkImportDialog) {
            BulkImportCsvDialog(
                ownerId = ownerId,
                viewModel = viewModel,
                onDismiss = { showBulkImportDialog = false }
            )
        }
    }
}

@Composable
fun StudentMasterCard(
    student: StudentEntity,
    onEdit: () -> Unit,
    onArchive: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student_card_${student.rollNumber}"),
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
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
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
                        Text(
                            text = student.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "ولدیت: ${student.fatherName} • فون: ${student.phone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onArchive, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Archive, contentDescription = "Archive", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (student != null) "طالب علم کا ڈیٹا اپ ڈیٹ کریں" else "نیا طالب علم شامل کریں")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().height(420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = rollNumber,
                        onValueChange = { rollNumber = it },
                        label = { Text("رول نمبر") },
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
                    if (rollNumber.isNotBlank() && name.isNotBlank()) {
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
                }
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

@Composable
fun BulkImportCsvDialog(
    ownerId: String,
    viewModel: CollegeViewModel,
    onDismiss: () -> Unit
) {
    var rawText by remember {
        mutableStateOf(
            "Roll,Name,FatherName,Phone,Class,Section,Group\n" +
            "701,Muhammad Arham,Tariq Mahmood,03001234567,First Year,B,Pre-Medical\n" +
            "702,Abdullah Khan,Muhammad Aslam,03019876543,First Year,B,Pre-Engineering\n" +
            "703,Ali Hassan,Raza Ahmad,03025556677,First Year,B,ICS"
        )
    }
    var importStatus by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("بلک نامینل رول امپورٹ (CSV / Text)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "CSV ڈیٹا پیسٹ کریں (Roll, Name, FatherName, Phone, Class, Section, Group):",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    maxLines = 10
                )
                if (importStatus.isNotBlank()) {
                    Text(text = importStatus, color = EmeraldPresent, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lines = rawText.lines().filter { it.isNotBlank() }
                    var importedCount = 0
                    lines.drop(1).forEach { line ->
                        val parts = line.split(",").map { it.trim() }
                        if (parts.size >= 4) {
                            val roll = parts.getOrNull(0) ?: ""
                            val name = parts.getOrNull(1) ?: ""
                            val father = parts.getOrNull(2) ?: ""
                            val phone = parts.getOrNull(3) ?: ""
                            val cls = parts.getOrNull(4) ?: "First Year"
                            val sec = parts.getOrNull(5) ?: "B"
                            val grp = parts.getOrNull(6) ?: "Pre-Medical"

                            if (roll.isNotBlank() && name.isNotBlank()) {
                                val s = StudentEntity(
                                    studentId = UUID.randomUUID().toString(),
                                    rollNumber = roll,
                                    name = name,
                                    fatherName = father,
                                    phone = phone,
                                    className = cls,
                                    section = sec,
                                    groupName = grp,
                                    electiveSubjectsRaw = "",
                                    academicYearId = "year_2026_27",
                                    ownerId = ownerId
                                )
                                viewModel.addStudentToNominalRoll(s)
                                importedCount++
                            }
                        }
                    }
                    importStatus = "$importedCount طلبہ کامیابی سے شامل کر لیے گئے!"
                }
            ) {
                Text("امپورٹ کریں")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("بند کریں")
            }
        }
    )
}
