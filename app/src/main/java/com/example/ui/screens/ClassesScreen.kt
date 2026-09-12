package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ClassEntity
import com.example.data.model.StudentEntity
import com.example.data.model.SubjectEntity
import com.example.repository.ClassStats
import com.example.ui.theme.CrimsonAbsent
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.EmeraldPresent
import com.example.ui.viewmodel.CollegeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassesScreen(
    viewModel: CollegeViewModel,
    onTakeAttendance: (classId: String) -> Unit,
    onAddTest: (classId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val ownerId by viewModel.currentOwnerId.collectAsStateWithLifecycle()
    val classes by viewModel.repository.getClasses(ownerId).collectAsStateWithLifecycle(emptyList<ClassEntity>())
    val archivedClasses by viewModel.getArchivedClasses(ownerId).collectAsStateWithLifecycle(emptyList<ClassEntity>())
    val subjects by viewModel.repository.getSubjects(ownerId).collectAsStateWithLifecycle(emptyList())
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("فعال کلاسز (${classes.size})", "آرکائیو شدہ کلاسز (${archivedClasses.size})")

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedClassForManage by remember { mutableStateOf<ClassEntity?>(null) }
    var classToRename by remember { mutableStateOf<ClassEntity?>(null) }
    var classToArchive by remember { mutableStateOf<ClassEntity?>(null) }
    var classToDelete by remember { mutableStateOf<ClassEntity?>(null) }
    var classStatsForDeletion by remember { mutableStateOf<ClassStats?>(null) }
    var isLoadingStats by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "کلاسز کی فہرست (Classes)",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "کل کلاسز: ${classes.size} • پنجاب ایچ ایس ایس سی سکیم",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("create_class_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("نئی کلاس")
                }
            }

            // Tabs for Active vs Archived
            PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    )
                }
            }

            if (selectedTabIndex == 0) {
                // Active Classes List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (classes.isEmpty()) {
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
                                    Text(
                                        text = "ابھی تک کوئی کلاس رجسٹر نہیں ہوئی",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "مضمون منتخب کریں، نامینل رول سے لازمی یا اختیاری طلبہ خودکار فلٹر ہوں گے!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(onClick = { showCreateDialog = true }) {
                                        Text("پہلی کلاس بنائیں")
                                    }
                                }
                            }
                        }
                    } else {
                        items(classes) { cls ->
                            ClassManagementCard(
                                classEntity = cls,
                                isArchived = false,
                                viewModel = viewModel,
                                onTakeAttendance = { onTakeAttendance(cls.classId) },
                                onAddTest = { onAddTest(cls.classId) },
                                onManageStudents = { selectedClassForManage = cls },
                                onRename = { classToRename = cls },
                                onArchive = { classToArchive = cls },
                                onRestore = {},
                                onDelete = {}
                            )
                        }
                    }
                }
            } else {
                // Archived Classes List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (archivedClasses.isEmpty()) {
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
                                    Text(
                                        text = "کوئی آرکائیو شدہ کلاس موجود نہیں ہے",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "جب کوئی تعلیمی سیشن مکمل ہو یا کلاس کو وقتی طور پر چھپانا ہو، تو آپ اسے آرکائیو کر سکتے ہیں۔ اس سے سابقہ حاضری مکمل محفوظ رہتی ہے۔",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(archivedClasses) { cls ->
                            ClassManagementCard(
                                classEntity = cls,
                                isArchived = true,
                                viewModel = viewModel,
                                onTakeAttendance = { onTakeAttendance(cls.classId) },
                                onAddTest = { onAddTest(cls.classId) },
                                onManageStudents = { selectedClassForManage = cls },
                                onRename = { classToRename = cls },
                                onArchive = {},
                                onRestore = { viewModel.restoreClass(cls.classId) },
                                onDelete = {
                                    classToDelete = cls
                                    isLoadingStats = true
                                    coroutineScope.launch {
                                        classStatsForDeletion = viewModel.getClassStats(cls.classId)
                                        isLoadingStats = false
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Create Class Dialog
        if (showCreateDialog) {
            CreateClassDialog(
                viewModel = viewModel,
                subjects = subjects,
                defaultTeacherName = currentUser?.name ?: "پروفیسر حسن الرحمن تقوی",
                onDismiss = { showCreateDialog = false },
                onClassCreated = { classId ->
                    showCreateDialog = false
                }
            )
        }

        // Manage Students in Class Dialog
        selectedClassForManage?.let { cls ->
            ManageClassStudentsDialog(
                classEntity = cls,
                viewModel = viewModel,
                allClasses = classes,
                onDismiss = { selectedClassForManage = null }
            )
        }

        // Rename Class Dialog
        classToRename?.let { cls ->
            var newName by remember { mutableStateOf(cls.className) }
            AlertDialog(
                onDismissRequest = { classToRename = null },
                title = { Text("کلاس کا نام تبدیل کریں") },
                text = {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("کلاس کا نیا نام") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newName.isNotBlank()) {
                                viewModel.renameClass(cls.classId, newName.trim())
                                classToRename = null
                            }
                        }
                    ) {
                        Text("محفوظ کریں")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { classToRename = null }) {
                        Text("منسوخ")
                    }
                }
            )
        }

        // Archive Class Confirmation Dialog
        classToArchive?.let { cls ->
            AlertDialog(
                onDismissRequest = { classToArchive = null },
                icon = { Icon(Icons.Default.Archive, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                title = { Text("کلاس آرکائیو کریں") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "کیا آپ واقعی کلاس '${cls.className}' کو آرکائیو کرنا چاہتے ہیں؟",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "آرکائیو کرنے سے یہ کلاس مین لسٹ سے ہٹ جائے گی، تاہم اس کی تمام سابقہ حاضری، طلبہ کی ممبرشپ اور ٹیسٹ رزلٹس مکمل محفوظ رہیں گے اور کسی بھی وقت واپس بحال کیے جا سکتے ہیں۔",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.archiveClass(cls.classId)
                            classToArchive = null
                        }
                    ) {
                        Text("آرکائیو کریں")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { classToArchive = null }) {
                        Text("منسوخ")
                    }
                }
            )
        }

        // Delete Class Safety Check Dialog
        classToDelete?.let { cls ->
            AlertDialog(
                onDismissRequest = {
                    classToDelete = null
                    classStatsForDeletion = null
                },
                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626)) },
                title = { Text("کلاس حذف کرنے کی تصدیق") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isLoadingStats) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("کلاس کے تاریخی ڈیٹا کی جانچ ہو رہی ہے...")
                            }
                        } else {
                            val stats = classStatsForDeletion
                            if (stats != null && (stats.attendanceSessionsCount > 0 || stats.testsCount > 0)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFEF2F2),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "⚠️ تاریخی ڈیٹا کا تحفظ (Data Protection):",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF991B1B)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "اس کلاس میں ${stats.attendanceSessionsCount} حاضری کے سیشنز (${stats.attendanceRecordsCount} حاضری ریکارڈز) اور ${stats.testsCount} ٹیسٹ رزلٹس موجود ہیں۔",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF991B1B)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "سرکاری کالج ریکارڈ کی حفاظت کے تحت یہ کلاس مکمل ڈیلیٹ نہیں کی جا سکتی۔ یہ کلاس آرکائیو رہے گی تاکہ رپورٹس میں اس کا تاریخی ریکارڈ دستیاب رہے۔",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF7F1D1D)
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "اس کلاس میں کوئی حاضری یا ٹیسٹ ریکارڈ موجود نہیں ہے۔ کیا آپ واقعی اسے ہمیشہ کے لیے حذف کرنا چاہتے ہیں؟",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    val stats = classStatsForDeletion
                    val canDeletePermanently = stats != null && stats.attendanceSessionsCount == 0 && stats.testsCount == 0
                    if (canDeletePermanently) {
                        Button(
                            onClick = {
                                viewModel.deleteClassPermanently(cls.classId)
                                classToDelete = null
                                classStatsForDeletion = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("مکمل حذف کریں")
                        }
                    } else {
                        Button(
                            onClick = {
                                classToDelete = null
                                classStatsForDeletion = null
                            }
                        ) {
                            Text("سمجھ گیا (Ok)")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        classToDelete = null
                        classStatsForDeletion = null
                    }) {
                        Text("بند کریں")
                    }
                }
            )
        }
    }
}

@Composable
fun ClassManagementCard(
    classEntity: ClassEntity,
    isArchived: Boolean,
    viewModel: CollegeViewModel,
    onTakeAttendance: () -> Unit,
    onAddTest: () -> Unit,
    onManageStudents: () -> Unit,
    onRename: () -> Unit,
    onArchive: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    val activeMemberships by viewModel.repository.getActiveMemberships(classEntity.classId).collectAsStateWithLifecycle(emptyList())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("class_card_${classEntity.classId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isArchived) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = classEntity.className,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!isArchived) {
                            IconButton(
                                onClick = onRename,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Rename Class",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
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
                        text = "${classEntity.level} • Section ${classEntity.section} • استاد: ${classEntity.teacherName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (classEntity.subjectType == "Compulsory") Color(0xFFDBEAFE) else Color(0xFFFCE7F3)
                ) {
                    Text(
                        text = if (classEntity.subjectType == "Compulsory") "لازمی (Compulsory)" else "اختیاری (Elective)",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (classEntity.subjectType == "Compulsory") Color(0xFF1D4ED8) else Color(0xFFBE185D)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "کل طلبہ: ${activeMemberships.size}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isArchived) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onRestore,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("بحال کریں", style = MaterialTheme.typography.labelMedium)
                        }

                        Button(
                            onClick = onDelete,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حذف کریں", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = onArchive,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Archive,
                                contentDescription = "Archive Class",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        OutlinedButton(
                            onClick = onManageStudents,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text("طلبہ", style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = onAddTest,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text("ٹیسٹ", style = MaterialTheme.typography.labelMedium)
                        }

                        Button(
                            onClick = onTakeAttendance,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPresent),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("حاضری", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Full implementation of Teacher Class Creation workflow (Sections 7, 8, 9, 10, 11, 48)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClassDialog(
    viewModel: CollegeViewModel,
    subjects: List<SubjectEntity>,
    defaultTeacherName: String,
    onDismiss: () -> Unit,
    onClassCreated: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val ownerId by viewModel.currentOwnerId.collectAsStateWithLifecycle()
    val yearId by viewModel.currentYearId.collectAsStateWithLifecycle()

    var level by remember { mutableStateOf("First Year") }
    var section by remember { mutableStateOf("B") }
    var selectedSubject by remember { mutableStateOf(subjects.firstOrNull() ?: SubjectEntity("comp_islamiat", "Islamic Studies (Compulsory)", "اسلامیات لازمی", "Islamic Studies (Compulsory)", "Compulsory", "First Year", "All", "", true, ownerId)) }
    var teacherName by remember { mutableStateOf(defaultTeacherName) }
    var customClassName by remember { mutableStateOf("") }

    // Dropdown expanded states
    var levelExpanded by remember { mutableStateOf(false) }
    var sectionExpanded by remember { mutableStateOf(false) }
    var subjectExpanded by remember { mutableStateOf(false) }

    // Filtered eligible students from nominal roll
    var eligibleStudents by remember { mutableStateOf<List<StudentEntity>>(emptyList()) }
    var isLoadingEligible by remember { mutableStateOf(false) }

    // Selected students map (studentId -> Boolean)
    val selectedStudentsMap = remember { mutableStateMapOf<String, Boolean>() }

    // Search query within eligible students
    var searchQuery by remember { mutableStateOf("") }

    // Specific roll numbers input state
    var specificRollsInput by remember { mutableStateOf("") }
    var showSpecificRollsDialog by remember { mutableStateOf(false) }

    // Update custom class name default whenever level, section or subject changes
    LaunchedEffect(level, section, selectedSubject) {
        customClassName = "$level $section – ${selectedSubject.urduName}"
    }

    // Load eligible students dynamically from nominal roll
    fun refreshEligibleStudents() {
        coroutineScope.launch {
            isLoadingEligible = true
            val list = viewModel.repository.getStudentsForClassCreation(
                ownerId = ownerId,
                className = level,
                section = section,
                subjectName = selectedSubject.name,
                subjectType = selectedSubject.type,
                academicYearId = yearId
            )
            eligibleStudents = list
            selectedStudentsMap.clear()
            // Default select all eligible students
            list.forEach { selectedStudentsMap[it.studentId] = true }
            isLoadingEligible = false
        }
    }

    LaunchedEffect(level, section, selectedSubject) {
        refreshEligibleStudents()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "نئی کلاس بنائیں (Create Class)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "نامینل رول سے خودکار اہلیت فلٹر ہوگی",
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
                // Class / Level Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = levelExpanded,
                        onExpandedChange = { levelExpanded = !levelExpanded },
                        modifier = Modifier.weight(1.2f)
                    ) {
                        OutlinedTextField(
                            value = level,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("کلاس / سال") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = levelExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = levelExpanded,
                            onDismissRequest = { levelExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("First Year (11th)") },
                                onClick = { level = "First Year"; levelExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Second Year (12th)") },
                                onClick = { level = "Second Year"; levelExpanded = false }
                            )
                        }
                    }

                    // Section Selector
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
                        ExposedDropdownMenu(
                            expanded = sectionExpanded,
                            onDismissRequest = { sectionExpanded = false }
                        ) {
                            listOf("A", "B", "C", "All").forEach { sec ->
                                DropdownMenuItem(
                                    text = { Text("Section $sec") },
                                    onClick = { section = sec; sectionExpanded = false }
                                )
                            }
                        }
                    }
                }

                // Subject Dropdown
                ExposedDropdownMenuBox(
                    expanded = subjectExpanded,
                    onExpandedChange = { subjectExpanded = !subjectExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = "${selectedSubject.urduName} (${selectedSubject.name})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("مضمون (Subject)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = subjectExpanded,
                        onDismissRequest = { subjectExpanded = false }
                    ) {
                        subjects.forEach { sub ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("${sub.urduName} • ${sub.name}")
                                        Text(
                                            text = if (sub.type == "Compulsory") "لازمی مضمون (Compulsory)" else "اختیاری مضمون (Elective)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (sub.type == "Compulsory") Color(0xFF1D4ED8) else Color(0xFFBE185D)
                                        )
                                    }
                                },
                                onClick = {
                                    selectedSubject = sub
                                    subjectExpanded = false
                                }
                            )
                        }
                    }
                }

                // Subject Type Indicator
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedSubject.type == "Compulsory") Color(0xFFEFF6FF) else Color(0xFFFDF2F8),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedSubject.type == "Compulsory") "Subject Type: Compulsory (لازمی - تمام طلبہ)" else "Subject Type: Elective (اختیاری - صرف منتخب طلبہ)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (selectedSubject.type == "Compulsory") Color(0xFF1D4ED8) else Color(0xFFBE185D)
                        )
                        Text(
                            text = "اہل طلبہ: ${eligibleStudents.size}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Custom Class Name field
                OutlinedTextField(
                    value = customClassName,
                    onValueChange = { customClassName = it },
                    label = { Text("کلاس کا نام (Class Label)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Selection Options Bar (Sections 8, 10)
                Text(
                    text = "طلبہ کا انتخاب (Selection Methods):",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            eligibleStudents.forEach { selectedStudentsMap[it.studentId] = true }
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("سب (All)", style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedButton(
                        onClick = {
                            eligibleStudents.forEach {
                                val rollInt = it.rollNumber.toIntOrNull()
                                selectedStudentsMap[it.studentId] = (rollInt != null && rollInt % 2 == 0)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Even (جفت)", style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedButton(
                        onClick = {
                            eligibleStudents.forEach {
                                val rollInt = it.rollNumber.toIntOrNull()
                                selectedStudentsMap[it.studentId] = (rollInt != null && rollInt % 2 != 0)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Odd (طاق)", style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedButton(
                        onClick = { showSpecificRollsDialog = true },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("خاص رولز", style = MaterialTheme.typography.labelSmall)
                    }
                }

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("نام یا رول نمبر تلاش کریں...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Filtered Students Checklist
                val displayedStudents = eligibleStudents.filter {
                    searchQuery.isBlank() ||
                            it.name.contains(searchQuery, ignoreCase = true) ||
                            it.rollNumber.contains(searchQuery) ||
                            it.fatherName.contains(searchQuery, ignoreCase = true)
                }

                if (isLoadingEligible) {
                    Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                } else if (displayedStudents.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                        Text("اس معیار کے مطابق کوئی طالب علم دستیاب نہیں", style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        items(displayedStudents) { student ->
                            val isSelected = selectedStudentsMap[student.studentId] == true
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedStudentsMap[student.studentId] = !isSelected }
                                    .padding(vertical = 4.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { selectedStudentsMap[student.studentId] = it }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = student.rollNumber,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = student.name,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "ولدیت: ${student.fatherName} • ${student.groupName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        },
        confirmButton = {
            val selectedCount = selectedStudentsMap.count { it.value }
            Button(
                onClick = {
                    val finalSelectedIds = selectedStudentsMap.filter { it.value }.keys.toList()
                    viewModel.createClass(
                        className = customClassName.ifBlank { "$level $section – ${selectedSubject.urduName}" },
                        level = level,
                        section = section,
                        subjectId = selectedSubject.subjectId,
                        subjectName = selectedSubject.name,
                        subjectType = selectedSubject.type,
                        teacherName = teacherName,
                        selectedStudentIds = finalSelectedIds,
                        onSuccess = { classId ->
                            onClassCreated(classId)
                        }
                    )
                },
                enabled = selectedCount > 0,
                modifier = Modifier.testTag("confirm_create_class_button")
            ) {
                Text("کلاس بنائیں ($selectedCount طلبہ)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("منسوخ")
            }
        }
    )

    // Dialog for Specific Roll Numbers (Option E: 2, 8, 14, 26, 40, 64)
    if (showSpecificRollsDialog) {
        AlertDialog(
            onDismissRequest = { showSpecificRollsDialog = false },
            title = { Text("خاص رول نمبرز درج کریں") },
            text = {
                Column {
                    Text(
                        text = "رول نمبرز کو کوما (comma) کے ذریعے الگ کریں: مثلاً 2, 8, 14, 26, 40",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = specificRollsInput,
                        onValueChange = { specificRollsInput = it },
                        placeholder = { Text("2, 8, 14, 26, 40") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val requestedRolls = specificRollsInput.split(",", " ", "\n")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                            .toSet()

                        eligibleStudents.forEach { s ->
                            selectedStudentsMap[s.studentId] = requestedRolls.contains(s.rollNumber)
                        }
                        showSpecificRollsDialog = false
                    }
                ) {
                    Text("منتخب کریں")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSpecificRollsDialog = false }) {
                    Text("منسوخ")
                }
            }
        )
    }
}

/**
 * Manage Students Dialog: Add, Remove, and Transfer with historical preservation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageClassStudentsDialog(
    classEntity: ClassEntity,
    viewModel: CollegeViewModel,
    allClasses: List<ClassEntity>,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val ownerId by viewModel.currentOwnerId.collectAsStateWithLifecycle()
    val activeMemberships by viewModel.repository.getActiveMemberships(classEntity.classId).collectAsStateWithLifecycle(emptyList())

    var enrolledStudents by remember { mutableStateOf<List<StudentEntity>>(emptyList()) }
    var availableStudentsToAdd by remember { mutableStateOf<List<StudentEntity>>(emptyList()) }
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var studentToTransfer by remember { mutableStateOf<StudentEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(activeMemberships) {
        val ids = activeMemberships.map { it.studentId }
        if (ids.isNotEmpty()) {
            enrolledStudents = viewModel.repository.getStudentsByIds(ids)
        } else {
            enrolledStudents = emptyList()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "کلاس طلبہ مینجمنٹ",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${classEntity.className} (${enrolledStudents.size} طلبہ فعال)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("تلاش کریں...") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val allStudents = viewModel.repository.getActiveStudents(ownerId)
                                // Filter candidates not currently active in this class
                                val currentIds = enrolledStudents.map { it.studentId }.toSet()
                                val candidates = viewModel.repository.getStudentsForClassCreation(
                                    ownerId = ownerId,
                                    className = classEntity.level,
                                    section = null, // any section
                                    subjectName = classEntity.subjectName,
                                    subjectType = classEntity.subjectType,
                                    academicYearId = classEntity.academicYearId
                                ).filter { !currentIds.contains(it.studentId) }
                                availableStudentsToAdd = candidates
                                showAddStudentDialog = true
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("طالب علم شامل کریں")
                    }
                }

                val filtered = enrolledStudents.filter {
                    searchQuery.isBlank() ||
                            it.name.contains(searchQuery, ignoreCase = true) ||
                            it.rollNumber.contains(searchQuery)
                }

                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("کوئی طالب علم موجود نہیں", style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filtered) { student ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
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
                                            modifier = Modifier.size(30.dp)
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
                                            Text(
                                                text = student.name,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "ولدیت: ${student.fatherName} • گروپ: ${student.groupName}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row {
                                        // Transfer Button
                                        IconButton(
                                            onClick = { studentToTransfer = student },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.SwapHoriz,
                                                contentDescription = "Transfer Section",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        // Remove Button
                                        IconButton(
                                            onClick = {
                                                viewModel.removeStudentFromClass(classEntity.classId, student.studentId)
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PersonRemove,
                                                contentDescription = "Remove Student",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
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
            Button(onClick = onDismiss) {
                Text("مکمل")
            }
        }
    )

    // Add Student to Class Sub-dialog
    if (showAddStudentDialog) {
        AlertDialog(
            onDismissRequest = { showAddStudentDialog = false },
            title = { Text("نامینل رول سے طالب علم شامل کریں") },
            text = {
                if (availableStudentsToAdd.isEmpty()) {
                    Text("کوئی نیا طالب علم دستیاب نہیں ہے یا تمام طلبہ پہلے سے شامل ہیں۔")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        items(availableStudentsToAdd) { candidate ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.addStudentToClass(classEntity.classId, candidate.studentId)
                                        showAddStudentDialog = false
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "${candidate.rollNumber}. ${candidate.name}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "ولدیت: ${candidate.fatherName} • سیکشن ${candidate.section} • ${candidate.groupName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Button(
                                    onClick = {
                                        viewModel.addStudentToClass(classEntity.classId, candidate.studentId)
                                        showAddStudentDialog = false
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("شامل کریں")
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddStudentDialog = false }) {
                    Text("بند کریں")
                }
            }
        )
    }

    // Transfer Student Sub-dialog (Section Change / Class Transfer)
    studentToTransfer?.let { student ->
        val otherClasses = allClasses.filter { it.classId != classEntity.classId }
        var targetClass by remember { mutableStateOf(otherClasses.firstOrNull()) }
        var targetSection by remember { mutableStateOf("A") }

        AlertDialog(
            onDismissRequest = { studentToTransfer = null },
            title = { Text("طالب علم کو دوسری کلاس میں منتقل کریں") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "طالب علم: ${student.name} (رول نمبر ${student.rollNumber})",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "طالب علم کا سابقہ ریکارڈ، سابقہ کلاس کی حاضری اور نتائج محفوظ رہیں گے اور نئی کلاس میں آج کی تاریخ سے حاضری شروع ہوگی۔",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (otherClasses.isEmpty()) {
                        Text(
                            text = "منتقلی کے لیے کوئی دوسری کلاس موجود نہیں ہے۔ پہلے نئی کلاس بنائیں۔",
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text("منزل کلاس منتخب کریں:")
                        otherClasses.forEach { oc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { targetClass = oc }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = targetClass?.classId == oc.classId,
                                    onCheckedChange = { if (it) targetClass = oc }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${oc.className} (سیکشن ${oc.section})")
                            }
                        }

                        OutlinedTextField(
                            value = targetSection,
                            onValueChange = { targetSection = it },
                            label = { Text("نیا سیکشن") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        targetClass?.let { tc ->
                            viewModel.transferStudent(
                                studentId = student.studentId,
                                fromClassId = classEntity.classId,
                                toClassId = tc.classId,
                                newSection = targetSection.trim()
                            )
                            studentToTransfer = null
                        }
                    },
                    enabled = targetClass != null
                ) {
                    Text("منتقلی مکمل کریں")
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToTransfer = null }) {
                    Text("منسوخ")
                }
            }
        )
    }
}
