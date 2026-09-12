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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.School
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ClassEntity
import com.example.data.model.StudentEntity
import com.example.data.model.TestEntity
import com.example.data.model.TestResultEntity
import com.example.ui.theme.CrimsonAbsent
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPresent
import com.example.ui.viewmodel.CollegeViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestsScreen(
    viewModel: CollegeViewModel,
    preselectedClassId: String?,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val ownerId by viewModel.currentOwnerId.collectAsStateWithLifecycle()
    val classes by viewModel.repository.getClasses(ownerId).collectAsStateWithLifecycle(emptyList())
    val allTests by viewModel.repository.getAllTests(ownerId).collectAsStateWithLifecycle(emptyList())

    var showAddTestDialog by remember { mutableStateOf(false) }
    var selectedTestDetail by remember { mutableStateOf<TestEntity?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "کلاس ٹیسٹ اور نتائج (Tests & Results)",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "ماہانہ ٹیسٹ، نمبرات کا اندراج اور درجہ بندی (Ranking)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { showAddTestDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("نیا ٹیسٹ")
                    }
                }
            }

            if (allTests.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "ابھی تک کوئی ٹیسٹ درج نہیں ہوا",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "کلاس ٹیسٹ کے نمبر درج کریں، فی صد اور پوزیشن خودکار معلوم ہوگی۔",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { showAddTestDialog = true }) {
                                Text("پہلا ٹیسٹ درج کریں")
                            }
                        }
                    }
                }
            } else {
                items(allTests) { test ->
                    TestCardItem(
                        test = test,
                        classes = classes,
                        onViewDetail = { selectedTestDetail = test }
                    )
                }
            }
        }

        // Add Test & Marks Entry Dialog
        if (showAddTestDialog) {
            AddTestDialog(
                viewModel = viewModel,
                classes = classes,
                preselectedClassId = preselectedClassId,
                onDismiss = { showAddTestDialog = false },
                onSuccess = { showAddTestDialog = false }
            )
        }

        // View Test Results Details Modal
        selectedTestDetail?.let { test ->
            TestDetailDialog(
                test = test,
                viewModel = viewModel,
                onDismiss = { selectedTestDetail = null }
            )
        }
    }
}

@Composable
fun TestCardItem(
    test: TestEntity,
    classes: List<ClassEntity>,
    onViewDetail: () -> Unit
) {
    val matchingClass = classes.find { it.classId == test.classId }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetail() },
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = test.testName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "کلاس: ${matchingClass?.className ?: "—"} • تاریخ: ${test.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "کل نمبر: ${test.totalMarks.toInt()}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onViewDetail,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Leaderboard, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("مکمل رزلٹ شیٹ اور رینکنگ")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTestDialog(
    viewModel: CollegeViewModel,
    classes: List<ClassEntity>,
    preselectedClassId: String?,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val ownerId by viewModel.currentOwnerId.collectAsStateWithLifecycle()

    var selectedClass by remember {
        mutableStateOf(classes.find { it.classId == preselectedClassId } ?: classes.firstOrNull())
    }
    var classDropdownExpanded by remember { mutableStateOf(false) }

    var testName by remember { mutableStateOf("ماہانہ ٹیسٹ (Monthly Assessment)") }
    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var testDate by remember { mutableStateOf(todayDate) }
    var totalMarksInput by remember { mutableStateOf("25") }

    var enrolledStudents by remember { mutableStateOf<List<StudentEntity>>(emptyList()) }
    val marksMap = remember { mutableStateMapOf<String, String>() }
    var isLoadingStudents by remember { mutableStateOf(false) }

    LaunchedEffect(selectedClass) {
        val cls = selectedClass ?: return@LaunchedEffect
        isLoadingStudents = true
        coroutineScope.launch {
            val list = viewModel.repository.getStudentsForClassCreation(
                ownerId = ownerId,
                className = cls.level,
                section = cls.section,
                subjectName = cls.subjectName,
                subjectType = cls.subjectType,
                academicYearId = cls.academicYearId
            )
            enrolledStudents = list
            marksMap.clear()
            list.forEach { s -> marksMap[s.studentId] = "0" }
            isLoadingStudents = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "نیا ٹیسٹ درج کریں (Add Test)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "حاصل کردہ نمبرات درج کریں، فی صد اور پاس/فیل خودکار معلوم ہوگا",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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

                // Test Name
                OutlinedTextField(
                    value = testName,
                    onValueChange = { testName = it },
                    label = { Text("ٹیسٹ کا نام / عنوان") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Date & Total Marks
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = testDate,
                        onValueChange = { testDate = it },
                        label = { Text("تاریخ") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )
                    OutlinedTextField(
                        value = totalMarksInput,
                        onValueChange = { totalMarksInput = it },
                        label = { Text("کل نمبر (Total)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.8f)
                    )
                }

                Text(
                    text = "حاصل کردہ نمبر (طلبہ کی فہرست - کل ${enrolledStudents.size}):",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val totalMarks = totalMarksInput.toDoubleOrNull() ?: 25.0

                if (isLoadingStudents) {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(2.dp)
                    ) {
                        items(enrolledStudents) { student ->
                            val markStr = marksMap[student.studentId] ?: "0"
                            val markVal = markStr.toDoubleOrNull() ?: 0.0
                            val pct = if (totalMarks > 0) (markVal / totalMarks) * 100.0 else 0.0

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = student.rollNumber,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = student.name, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                        Text(
                                            text = "فی صد: ${String.format(Locale.US, "%.1f", pct)}% • ${if (pct >= 33.0) "پاس" else "فیل"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (pct >= 33.0) EmeraldPresent else CrimsonAbsent
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = markStr,
                                    onValueChange = { marksMap[student.studentId] = it },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.width(80.dp)
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cls = selectedClass ?: return@Button
                    val total = totalMarksInput.toDoubleOrNull() ?: 25.0
                    val numericMarks = marksMap.mapValues { it.value.toDoubleOrNull() ?: 0.0 }

                    viewModel.saveTest(
                        classId = cls.classId,
                        subjectId = cls.subjectId,
                        testName = testName.ifBlank { "Monthly Test" },
                        date = testDate,
                        totalMarks = total,
                        studentMarks = numericMarks,
                        onSuccess = onSuccess
                    )
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
fun TestDetailDialog(
    test: TestEntity,
    viewModel: CollegeViewModel,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var results by remember { mutableStateOf<List<TestResultEntity>>(emptyList()) }
    var studentsMap by remember { mutableStateOf<Map<String, StudentEntity>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(test) {
        isLoading = true
        coroutineScope.launch {
            val resList = viewModel.repository.getResultsForTestDirect(test.testId)
            val sIds = resList.map { it.studentId }
            val sList = viewModel.repository.getStudentsByIds(sIds)
            studentsMap = sList.associateBy { it.studentId }
            // Sort by obtainedMarks descending to show rankings (Section 29)
            results = resList.sortedByDescending { it.obtainedMarks }
            isLoading = false
        }
    }

    val totalCount = results.size
    val averageMarks = if (totalCount > 0) results.map { it.obtainedMarks }.average() else 0.0
    val highestMarks = results.maxOfOrNull { it.obtainedMarks } ?: 0.0
    val lowestMarks = results.minOfOrNull { it.obtainedMarks } ?: 0.0
    val passCount = results.count { it.status == "Passed" }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = test.testName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(text = "تاریخ: ${test.date} • کل نمبر: ${test.totalMarks.toInt()}", style = MaterialTheme.typography.bodySmall)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Class Statistics Summary (Section 29)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("کلاس اوسط (Average):", style = MaterialTheme.typography.bodySmall)
                            Text("${String.format(Locale.US, "%.1f", averageMarks)} / ${test.totalMarks.toInt()}", fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("سب سے زیادہ (Highest):", style = MaterialTheme.typography.bodySmall)
                            Text("$highestMarks", fontWeight = FontWeight.Bold, color = EmeraldPresent)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("کامیاب طلبہ (Passed):", style = MaterialTheme.typography.bodySmall)
                            Text("$passCount / $totalCount", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text(
                    text = "طلبہ کی پوزیشن اور نمبرات (Student Rankings):",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(results.mapIndexed { idx, res -> Pair(idx + 1, res) }) { (rank, res) ->
                            val student = studentsMap[res.studentId]
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (rank <= 3) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (rank <= 3) Color(0xFFD97706) else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "#$rank",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = Color.White
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "${student?.name ?: "طالب علم"} (رول ${student?.rollNumber ?: "—"})",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "ولدیت: ${student?.fatherName ?: "—"}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${res.obtainedMarks} / ${test.totalMarks.toInt()}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${String.format(Locale.US, "%.1f", res.percentage)}% • ${res.status}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (res.status == "Passed") EmeraldPresent else CrimsonAbsent
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("بند کریں")
            }
        }
    )
}
