package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ClassEntity
import com.example.ui.components.AcademicHeader
import com.example.ui.components.StatCard
import com.example.ui.theme.CrimsonAbsent
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPresent
import com.example.ui.viewmodel.CollegeViewModel
import com.example.util.SessionHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: CollegeViewModel,
    onNavigateToTakeAttendance: (classId: String?) -> Unit,
    onNavigateToCreateClass: () -> Unit,
    onNavigateToClassDetail: (classId: String) -> Unit,
    onNavigateToNominalRoll: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToTests: () -> Unit = {},
    onNavigateToMessaging: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val ownerId by viewModel.currentOwnerId.collectAsStateWithLifecycle()
    val classes by viewModel.repository.getClasses(ownerId).collectAsStateWithLifecycle(emptyList())
    val students by viewModel.repository.getActiveStudents(ownerId).collectAsStateWithLifecycle(emptyList())
    val validationIssues by viewModel.validationIssues.collectAsStateWithLifecycle()
    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsStateWithLifecycle(0)

    val todayFormatted = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("ur", "PK")).format(Date())
    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val todaySessions by viewModel.repository.getAllSessions(ownerId).collectAsStateWithLifecycle(emptyList())
    val todaysSession = todaySessions.find { it.date == todayDateStr }

    // Session aware filtering
    val activeSessionStudents = students.filter { it.session == activeSession || it.session.isBlank() }
    val firstYearCount = students.count { it.className == "First Year" }
    val secondYearCount = students.count { it.className == "Second Year" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 90.dp, top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Institutional Header
        item {
            AcademicHeader(
                collegeName = currentUser?.college ?: "گورنمنٹ ایسوسی ایٹ کالج مخدوم رشید ملتان",
                teacherName = currentUser?.name ?: "پروفیسر حسن الرحمن تقویٰ",
                designation = currentUser?.designation ?: "لیکچرار اسلامیات",
                academicYear = activeSession
            )
        }

        // 2. Academic Session Selector (e.g. 2026–2028, 2025–2027)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تعلیمی سیشن (Academic Session)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "خودکار جماعت تشخیص فعال",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(SessionHelper.DEFAULT_SESSIONS) { session ->
                            val isSelected = session == activeSession
                            val levelInfo = SessionHelper.determineLevelForSession(session)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setActiveSession(session) },
                                label = {
                                    Text(
                                        text = "$session (${levelInfo.urduLabel})",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }
            }
        }

        // 3. Offline-First Sync & Data Safety Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (pendingSyncCount == 0) Icons.Default.CloudDone else Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = if (pendingSyncCount == 0) EmeraldPresent else Color(0xFFD97706),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "آف لائن لوکل ڈیٹا بیس فعال",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF166534)
                            )
                            Text(
                                text = if (pendingSyncCount > 0) "زیر التواء ریکارڈز: $pendingSyncCount • آخری ہم آہنگی: $lastSyncTime"
                                else "$syncStatus • آخری ہم آہنگی: $lastSyncTime",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                    Button(
                        onClick = { viewModel.syncNow() },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPresent),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("sync_now_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ہم آہنگ کریں", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // 4. Validation Warning Banner (Nominal Roll Alerts)
        if (validationIssues.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToNominalRoll() }
                        .testTag("validation_warning_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ماسٹر نامینل رول میں ${validationIssues.size} انتباہات (Warnings)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF92400E)
                            )
                            Text(
                                text = "ڈپلیکیٹ رول نمبر یا فون نمبرز کی جانچ اور تصدیق کے لیے ٹیپ کریں۔",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB45309)
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 5. Quick Actions Section (Comprehensive Hub)
        item {
            Text(
                text = "فوری کارروائیاں (Quick Actions)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Action 1: Take Attendance
                    Button(
                        onClick = { onNavigateToTakeAttendance(null) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_take_attendance_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPresent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حاضری لگائیں")
                    }

                    // Action 2: Nominal Roll & Import
                    OutlinedButton(
                        onClick = onNavigateToNominalRoll,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_nominal_roll_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("امپورٹ رول")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Action 3: Tests & Results
                    OutlinedButton(
                        onClick = onNavigateToTests,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_tests_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ٹیسٹ و نتائج")
                    }

                    // Action 4: Parent SMS & Calls
                    OutlinedButton(
                        onClick = onNavigateToMessaging,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_messaging_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("والدین رابطہ")
                    }
                }
            }
        }

        // 6. Google Calendar Month-End Integration Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color(0xFF1D4ED8),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "گوگل کیلنڈر میں ماہانہ یاددہانی",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1E40AF)
                            )
                            Text(
                                text = "ماہ کے اختتام پر رجسٹر تکمیل اور رپورٹ نوٹیفکیشن شیڈول کریں۔",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF2563EB)
                            )
                        }
                    }
                    Button(
                        onClick = { viewModel.scheduleMonthEndReminder(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("شیڈول کریں", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // 7. Statistics Grid
        item {
            Text(
                text = "کالج کا خلاصہ (Overview)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "کل کلاسز",
                    value = "${classes.size}",
                    icon = Icons.Default.Class,
                    iconColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "کل طلبہ (Roll)",
                    value = "${students.size}",
                    icon = Icons.Default.Group,
                    iconColor = Color(0xFF0284C7),
                    containerColor = Color(0xFFE0F2FE),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "First Year طلبہ",
                    value = "$firstYearCount",
                    icon = Icons.Default.School,
                    iconColor = EmeraldPresent,
                    containerColor = EmeraldLight.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Second Year طلبہ",
                    value = "$secondYearCount",
                    icon = Icons.Default.School,
                    iconColor = Color(0xFF7C3AED),
                    containerColor = Color(0xFFEDE9FE),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 8. Teacher's Active Classes Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "میری کلاسز (Teacher Classes)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "سب دیکھیں (${classes.size})",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onNavigateToCreateClass() }
                )
            }
        }

        if (classes.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Class,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "ابھی کوئی کلاس نہیں بنی",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "نئی کلاس بنائیں تاکہ نامینل رول سے طلبہ منتخب ہو سکیں۔",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onNavigateToCreateClass) {
                            Text("نئی کلاس بنائیں")
                        }
                    }
                }
            }
        } else {
            items(classes) { cls ->
                ClassCardItem(
                    classEntity = cls,
                    onTakeAttendance = { onNavigateToTakeAttendance(cls.classId) },
                    onViewDetail = { onNavigateToClassDetail(cls.classId) }
                )
            }
        }
    }
}

@Composable
fun ClassCardItem(
    classEntity: ClassEntity,
    onTakeAttendance: () -> Unit,
    onViewDetail: () -> Unit
) {
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
                        text = classEntity.className,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${classEntity.level} • Section ${classEntity.section}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (classEntity.subjectType == "Compulsory") Color(0xFFEFF6FF) else Color(0xFFFDF2F8)
                ) {
                    Text(
                        text = if (classEntity.subjectType == "Compulsory") "لازمی (Compulsory)" else "اختیاری (Elective)",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
                Text(
                    text = "مضمون: ${classEntity.subjectName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onTakeAttendance,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPresent),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("حاضری", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

