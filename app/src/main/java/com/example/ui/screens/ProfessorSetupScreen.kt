package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentEntity
import com.example.data.model.TeacherEntity
import com.example.ui.theme.EmeraldPresent
import com.example.ui.viewmodel.CollegeViewModel
import com.example.util.ParentNotificationHelper
import com.example.util.ProfessorSetupPreferences
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessorSetupScreen(
    viewModel: CollegeViewModel,
    isEditMode: Boolean = false,
    onSetupCompleted: (classId: String) -> Unit,
    onCancel: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val allTeachers by viewModel.allTeachers.collectAsState(initial = emptyList())

    // Current step: 1 = Professor Selection, 2 = Class & Group, 3 = Students Selection
    var currentStep by remember { mutableIntStateOf(if (isEditMode) 2 else 1) }

    // Selection States
    var selectedTeacher by remember { mutableStateOf<TeacherEntity?>(null) }
    var selectedClassLevel by remember {
        mutableStateOf(
            if (isEditMode) ProfessorSetupPreferences.getClassLevel(context) else "First Year"
        )
    }
    var selectedGroupName by remember {
        mutableStateOf(
            if (isEditMode) ProfessorSetupPreferences.getGroupName(context) else "تمام گروپس"
        )
    }
    var selectionMode by remember {
        mutableStateOf(
            if (isEditMode) ProfessorSetupPreferences.getSelectionMode(context) else "ALL"
        )
    }

    // Candidate students for the selected class/group
    var availableStudents by remember { mutableStateOf<List<StudentEntity>>(emptyList()) }
    val selectedStudentIds = remember { mutableStateListOf<String>() }

    // UI Dialogs
    var showTeacherSelectionDialog by remember { mutableStateOf(false) }
    var showAddTeacherDialog by remember { mutableStateOf(false) }

    // Pre-populate if in edit mode or previously saved
    LaunchedEffect(allTeachers) {
        val savedTeacherId = ProfessorSetupPreferences.getTeacherId(context)
        if (savedTeacherId != null && selectedTeacher == null) {
            selectedTeacher = allTeachers.find { it.teacherId == savedTeacherId }
        }
    }

    // Load available students whenever classLevel or groupName changes
    LaunchedEffect(selectedClassLevel, selectedGroupName) {
        val students = viewModel.getAvailableStudentsForClass(selectedClassLevel, selectedGroupName)
        availableStudents = students

        // Apply initial filter mode
        selectedStudentIds.clear()
        when (selectionMode) {
            "ALL" -> {
                selectedStudentIds.addAll(students.map { it.studentId })
            }
            "EVEN" -> {
                selectedStudentIds.addAll(students.filter {
                    val roll = it.rollNumber.toIntOrNull() ?: 0
                    roll % 2 == 0
                }.map { it.studentId })
            }
            "ODD" -> {
                selectedStudentIds.addAll(students.filter {
                    val roll = it.rollNumber.toIntOrNull() ?: 0
                    roll % 2 != 0
                }.map { it.studentId })
            }
            else -> {
                selectedStudentIds.addAll(students.map { it.studentId })
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isEditMode) "کلاس اور طلبہ کی تبدیلی" else "پروفیسر اور کلاس سیٹ اپ",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "گورنمنٹ ایسوسی ایٹ کالج مخدوم رشید ملتان",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    if (isEditMode && onCancel != null) {
                        IconButton(onClick = onCancel) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Stepper Indicator
            StepProgressBar(
                currentStep = currentStep,
                totalSteps = 3,
                stepTitles = listOf("پروفیسر", "کلاس", "طلبہ")
            )

            // Step Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentStep) {
                    1 -> {
                        Step1ProfessorSelection(
                            selectedTeacher = selectedTeacher,
                            onOpenTeacherDialog = { showTeacherSelectionDialog = true },
                            onNext = {
                                if (selectedTeacher != null) {
                                    currentStep = 2
                                }
                            }
                        )
                    }
                    2 -> {
                        Step2ClassSelection(
                            selectedTeacher = selectedTeacher,
                            selectedClassLevel = selectedClassLevel,
                            onClassLevelChanged = { selectedClassLevel = it },
                            selectedGroupName = selectedGroupName,
                            onGroupNameChanged = { selectedGroupName = it },
                            studentCount = availableStudents.size,
                            onBack = {
                                if (!isEditMode) currentStep = 1
                            },
                            onNext = {
                                currentStep = 3
                            }
                        )
                    }
                    3 -> {
                        Step3StudentSelection(
                            availableStudents = availableStudents,
                            selectedStudentIds = selectedStudentIds,
                            selectionMode = selectionMode,
                            onModeChanged = { mode ->
                                selectionMode = mode
                                selectedStudentIds.clear()
                                when (mode) {
                                    "ALL" -> {
                                        selectedStudentIds.addAll(availableStudents.map { it.studentId })
                                    }
                                    "EVEN" -> {
                                        selectedStudentIds.addAll(availableStudents.filter {
                                            val roll = it.rollNumber.toIntOrNull() ?: 0
                                            roll % 2 == 0
                                        }.map { it.studentId })
                                    }
                                    "ODD" -> {
                                        selectedStudentIds.addAll(availableStudents.filter {
                                            val roll = it.rollNumber.toIntOrNull() ?: 0
                                            roll % 2 != 0
                                        }.map { it.studentId })
                                    }
                                    "CUSTOM" -> {
                                        // Keep current or empty
                                    }
                                }
                            },
                            onSelectAll = {
                                selectionMode = "ALL"
                                selectedStudentIds.clear()
                                selectedStudentIds.addAll(availableStudents.map { it.studentId })
                            },
                            onDeselectAll = {
                                selectionMode = "CUSTOM"
                                selectedStudentIds.clear()
                            },
                            onToggleStudent = { studentId ->
                                selectionMode = "CUSTOM"
                                if (selectedStudentIds.contains(studentId)) {
                                    selectedStudentIds.remove(studentId)
                                } else {
                                    selectedStudentIds.add(studentId)
                                }
                            },
                            onBack = { currentStep = 2 },
                            onSave = {
                                val teacher = selectedTeacher ?: return@Step3StudentSelection
                                val chosenStudents = availableStudents.filter {
                                    selectedStudentIds.contains(it.studentId)
                                }

                                if (isEditMode) {
                                    val classId = ProfessorSetupPreferences.getClassId(context)
                                    if (classId != null) {
                                        viewModel.updateClassStudentSelection(
                                            classId = classId,
                                            selectedStudents = chosenStudents,
                                            selectionMode = selectionMode,
                                            onComplete = {
                                                onSetupCompleted(classId)
                                            }
                                        )
                                    }
                                } else {
                                    viewModel.completeProfessorSetup(
                                        teacher = teacher,
                                        className = selectedClassLevel,
                                        section = "A",
                                        groupName = selectedGroupName,
                                        selectedStudents = chosenStudents,
                                        selectionMode = selectionMode,
                                        onComplete = { newClassId ->
                                            onSetupCompleted(newClassId)
                                        }
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Teacher Selection Dialog / Sheet
    if (showTeacherSelectionDialog) {
        TeacherSelectionDialog(
            teachers = allTeachers,
            onDismiss = { showTeacherSelectionDialog = false },
            onSelectTeacher = { teacher ->
                selectedTeacher = teacher
                showTeacherSelectionDialog = false
            },
            onAddNewTeacher = {
                showTeacherSelectionDialog = false
                showAddTeacherDialog = true
            }
        )
    }

    // Add Custom Teacher Dialog
    if (showAddTeacherDialog) {
        AddCustomTeacherDialog(
            onDismiss = { showAddTeacherDialog = false },
            onTeacherCreated = { newTeacher ->
                coroutineScope.launch {
                    viewModel.repository.createTeacher(newTeacher)
                    selectedTeacher = newTeacher
                    showAddTeacherDialog = false
                }
            }
        )
    }
}

@Composable
fun StepProgressBar(
    currentStep: Int,
    totalSteps: Int,
    stepTitles: List<String>
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            stepTitles.forEachIndexed { index, title ->
                val stepNum = index + 1
                val isCompleted = stepNum < currentStep
                val isCurrent = stepNum == currentStep

                val circleBg = when {
                    isCompleted -> EmeraldPresent
                    isCurrent -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outlineVariant
                }
                val textColor = when {
                    isCurrent -> MaterialTheme.colorScheme.primary
                    isCompleted -> EmeraldPresent
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(circleBg),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Done",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = "$stepNum",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = textColor
                    )
                }

                if (index < stepTitles.size - 1) {
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(2.dp)
                            .background(
                                if (stepNum < currentStep) EmeraldPresent else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 1: Professor Selection
// -------------------------------------------------------------
@Composable
fun Step1ProfessorSelection(
    selectedTeacher: TeacherEntity?,
    onOpenTeacherDialog: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Professor Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "پروفیسر کا انتخاب",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "کالج فیکلٹی کی فہرست میں سے اپنا نام منتخب کریں تاکہ آپ کا لاگ ان اور کلاس ڈیٹا ترتیب دیا جا سکے",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Dropdown / Arrow List Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenTeacherDialog() }
                    .testTag("professor_dropdown_selector"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedTeacher != null)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    else
                        MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    color = if (selectedTeacher != null)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outline
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (selectedTeacher != null) EmeraldPresent else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (selectedTeacher != null) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "Select",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            if (selectedTeacher != null) {
                                val formattedName = ParentNotificationHelper.formatTeacherName(selectedTeacher.name)
                                Text(
                                    text = formattedName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${selectedTeacher.designation} • شعبہ ${selectedTeacher.department}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(
                                    text = "پروفیسر منتخب کرنے کے لیے یہاں دبائیں",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "کالج فیکلٹی کے تمام 25 اساتذہ کی فہرست",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dropdown Arrow",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            if (selectedTeacher != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldPresent.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = EmeraldPresent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "پروفیسر کا انتخاب مکمل ہو گیا ہے۔ اگلے مرحلے کے لیے بٹن دبائیں۔",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldPresent
                        )
                    }
                }
            }
        }

        // Bottom Next Button
        Button(
            onClick = onNext,
            enabled = selectedTeacher != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("step1_next_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = "اگلا مرحلہ: کلاس کا انتخاب",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
        }
    }
}

// -------------------------------------------------------------
// STEP 2: Class Selection (Professor کی اپنی Classes)
// -------------------------------------------------------------
@Composable
fun Step2ClassSelection(
    selectedTeacher: TeacherEntity?,
    selectedClassLevel: String,
    onClassLevelChanged: (String) -> Unit,
    selectedGroupName: String,
    onGroupNameChanged: (String) -> Unit,
    studentCount: Int,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "کلاس اور گروپ کا انتخاب",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            val teacherName = selectedTeacher?.let { ParentNotificationHelper.formatTeacherName(it.name) } ?: ""
            Text(
                text = "$teacherName کے لیے متعلقہ کلاس اور گروپ منتخب فرمائیں:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Academic Class Level (First Year vs Second Year)
            Text(
                text = "1. تعلیمی درجہ / سال:",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val isFirstYear = selectedClassLevel.contains("First", ignoreCase = true)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onClassLevelChanged("First Year") }
                        .testTag("select_first_year"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isFirstYear)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isFirstYear) 2.dp else 1.dp,
                        color = if (isFirstYear) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "فرسٹ ایئر (11th)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isFirstYear) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "سیشن 2026–2028",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isFirstYear) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val isSecondYear = selectedClassLevel.contains("Second", ignoreCase = true)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onClassLevelChanged("Second Year") }
                        .testTag("select_second_year"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSecondYear)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSecondYear) 2.dp else 1.dp,
                        color = if (isSecondYear) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "سیکنڈ ایئر (12th)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isSecondYear) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "سیشن 2025–2027",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSecondYear) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Group / Section Selection
            Text(
                text = "2. متعلقہ گروپ / سیکشن:",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            val groups = listOf(
                "تمام گروپس" to "مشترکہ کلاس (All)",
                "ICS" to "کمپیوٹر سائنس",
                "Pre-Medical" to "پری میڈیکل",
                "Pre-Engineering" to "پری انجینئرنگ",
                "General Science" to "جنرل سائنس",
                "Arts" to "ہیومینٹیز / آرٹس"
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Render as 2 columns of 3
                for (chunk in groups.chunked(2)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for ((groupKey, groupDesc) in chunk) {
                            val isSelected = selectedGroupName == groupKey
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onGroupNameChanged(groupKey) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                            )
                                            .border(
                                                1.5.dp,
                                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = groupKey,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = groupDesc,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Student Count Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "اس کلاس اور گروپ میں کل $studentCount طلبہ نامینل رول میں موجود ہیں۔",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Action Buttons (Back and Next)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                Spacer(modifier = Modifier.width(6.dp))
                Text("واپس", style = MaterialTheme.typography.titleSmall)
            }

            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(2f)
                    .height(52.dp)
                    .testTag("step2_next_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "اگلا مرحلہ: طلبہ کا انتخاب",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 3: Students Selection (All, Even, Odd, Custom)
// -------------------------------------------------------------
@Composable
fun Step3StudentSelection(
    availableStudents: List<StudentEntity>,
    selectedStudentIds: List<String>,
    selectionMode: String,
    onModeChanged: (String) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onToggleStudent: (String) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Header & Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "طلبہ کا انتخاب",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "منتخب کردہ طلبہ: ${selectedStudentIds.size} از ${availableStudents.size}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = onSelectAll) {
                        Text("سب منتخب", style = MaterialTheme.typography.labelSmall)
                    }
                    TextButton(onClick = onDeselectAll) {
                        Text("کوئی نہیں", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Modes: All, Even, Odd, Custom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val modes = listOf(
                    Triple("ALL", "تمام طلبا", "All"),
                    Triple("EVEN", "جفت رول نمبرز", "Even (2, 4...)"),
                    Triple("ODD", "طاق رول نمبرز", "Odd (1, 3...)"),
                    Triple("CUSTOM", "اپنی مرضی سے", "Manual")
                )

                modes.forEach { (modeKey, modeTitle, _) ->
                    val isSelected = selectionMode == modeKey
                    FilterChip(
                        selected = isSelected,
                        onClick = { onModeChanged(modeKey) },
                        label = {
                            Text(
                                text = modeTitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Student List with Checkboxes & Even/Odd indicator
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(availableStudents, key = { it.studentId }) { student ->
                    val isChecked = selectedStudentIds.contains(student.studentId)
                    val rollInt = student.rollNumber.toIntOrNull() ?: 0
                    val isEven = rollInt % 2 == 0

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleStudent(student.studentId) },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isChecked)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isChecked) 1.5.dp else 0.5.dp,
                            color = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { onToggleStudent(student.studentId) }
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                Surface(
                                    shape = CircleShape,
                                    color = if (isEven) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = student.rollNumber,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
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
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (isEven) Color(0xFFE0F2FE) else Color(0xFFF3E8FF)
                                        ) {
                                            Text(
                                                text = if (isEven) "جفت" else "طاق",
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isEven) Color(0xFF0369A1) else Color(0xFF7E22CE)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "ولدیت: ${student.fatherName} • ${student.groupName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                Spacer(modifier = Modifier.width(6.dp))
                Text("واپس", style = MaterialTheme.typography.titleSmall)
            }

            Button(
                onClick = onSave,
                enabled = selectedStudentIds.isNotEmpty(),
                modifier = Modifier
                    .weight(2.2f)
                    .height(52.dp)
                    .testTag("step3_save_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPresent)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Save")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "سیٹ اپ محفوظ کریں اور حاضری شروع کریں",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Dialog: Searchable Faculty Selection
// -------------------------------------------------------------
@Composable
fun TeacherSelectionDialog(
    teachers: List<TeacherEntity>,
    onDismiss: () -> Unit,
    onSelectTeacher: (TeacherEntity) -> Unit,
    onAddNewTeacher: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredTeachers = remember(teachers, searchQuery) {
        if (searchQuery.isBlank()) teachers
        else teachers.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.department.contains(searchQuery, ignoreCase = true) ||
            it.designation.contains(searchQuery, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "پروفیسرز اور اساتذہ کرام کی فہرست",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "اپنا نام منتخب فرمائیں (کل 25 اساتذہ):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("نام یا شعبہ تلاش کریں...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredTeachers, key = { it.teacherId }) { teacher ->
                        val formattedName = ParentNotificationHelper.formatTeacherName(teacher.name)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectTeacher(teacher) },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = teacher.name.take(1),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = formattedName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${teacher.designation} • شعبہ ${teacher.department}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Select",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onAddNewTeacher,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("فہرست میں نام نہیں ہے؟ نیا استاد شامل کریں", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("منسوخ کریں")
            }
        }
    )
}

// -------------------------------------------------------------
// Dialog: Add Custom Teacher
// -------------------------------------------------------------
@Composable
fun AddCustomTeacherDialog(
    onDismiss: () -> Unit,
    onTeacherCreated: (TeacherEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var designation by remember { mutableStateOf("پروفیسر") }
    var department by remember { mutableStateOf("اسلامیات") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "نیا پروفیسر / استاد شامل کریں",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("استاد کا نام *") },
                    placeholder = { Text("مثلاً: ڈاکٹر علی حسن") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = designation,
                    onValueChange = { designation = it },
                    label = { Text("عہدہ (Designation)") },
                    placeholder = { Text("لیکچرر / اسسٹنٹ پروفیسر") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("شعبہ / مضمون (Department)") },
                    placeholder = { Text("اسلامیات / کیمسٹری / کمپیوٹر سائنس") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val newTeacher = TeacherEntity(
                            teacherId = "custom_${System.currentTimeMillis()}",
                            name = name.trim(),
                            designation = designation.trim(),
                            department = department.trim(),
                            role = "teacher",
                            isRegistered = "Yes"
                        )
                        onTeacherCreated(newTeacher)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("شامل کریں")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("منسوخ")
            }
        }
    )
}
