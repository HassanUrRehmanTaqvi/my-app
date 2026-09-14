package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShowChart
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentEntity
import com.example.repository.StudentProfileAttendanceStats
import com.example.repository.StudentSemesterTrendPoint
import com.example.ui.theme.AmberLeave
import com.example.ui.theme.AmberLight
import com.example.ui.theme.CrimsonAbsent
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPresent
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.CollegeViewModel
import java.util.Locale

@Composable
fun StudentProfileDialog(
    student: StudentEntity,
    viewModel: CollegeViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var stats by remember { mutableStateOf<StudentProfileAttendanceStats?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(student.studentId) {
        isLoading = true
        stats = viewModel.repository.getStudentProfileAttendanceStats(student.studentId)
        isLoading = false
    }

    val contactPhone = student.guardianPhone.ifBlank { student.phone }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("بند کریں")
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "طالب علم کا مکمل پروفائل",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "بند کریں")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Card with Student Info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = student.rollNumber,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = student.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "ولدیت: ${student.fatherName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${student.className} • ${student.groupName} • سیشن: ${student.session}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Overall Attendance Summary Row
                if (stats != null) {
                    val s = stats!!
                    val isGoodStanding = s.overallPercentage >= 75.0
                    val isWarning = s.overallPercentage in 60.0..74.9

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isGoodStanding -> EmeraldLight.copy(alpha = 0.5f)
                                isWarning -> AmberLight.copy(alpha = 0.5f)
                                else -> CrimsonLight.copy(alpha = 0.5f)
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isGoodStanding) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (isGoodStanding) EmeraldPresent else if (isWarning) AmberLeave else CrimsonAbsent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when {
                                            isGoodStanding -> "حاضری تسلی بخش (اہل برائے امتحانات)"
                                            isWarning -> "حاضری توجہ طلب (75% شرط ضروری ہے)"
                                            else -> "حاضری سنگین طور پر کم (شارٹ اٹینڈنس)"
                                        },
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isGoodStanding) EmeraldPresent else if (isWarning) AmberLeave else CrimsonAbsent
                                    )
                                }

                                Text(
                                    text = "${String.format(Locale.US, "%.1f", s.overallPercentage)}%",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isGoodStanding) EmeraldPresent else if (isWarning) AmberLeave else CrimsonAbsent
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(color = Color.Black.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("کل سیشنز", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${s.totalSessions}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("حاضر", style = MaterialTheme.typography.labelSmall, color = EmeraldPresent)
                                    Text("${s.presentCount}", fontWeight = FontWeight.Bold, color = EmeraldPresent, style = MaterialTheme.typography.bodyMedium)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("غیر حاضر", style = MaterialTheme.typography.labelSmall, color = CrimsonAbsent)
                                    Text("${s.absentCount}", fontWeight = FontWeight.Bold, color = CrimsonAbsent, style = MaterialTheme.typography.bodyMedium)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("رخصت", style = MaterialTheme.typography.labelSmall, color = AmberLeave)
                                    Text("${s.leaveCount}", fontWeight = FontWeight.Bold, color = AmberLeave, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }

                // D3 / Recharts-style Attendance Percentage Trend Visualization
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ShowChart,
                                    contentDescription = null,
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "سمسٹر میں حاضری کا رجحان (Attendance Trend)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = NavyPrimary.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "ماہ بہ ماہ رجحان",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NavyPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(28.dp))
                            }
                        } else {
                            val trendData = stats?.trendPoints ?: emptyList()
                            SemesterAttendanceTrendChart(
                                trendPoints = trendData,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(190.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp, 2.dp)
                                        .background(CrimsonAbsent)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "75% سرکاری معیار",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CrimsonAbsent
                                )
                            }
                            Text(
                                text = "حاضری کم از کم 75 فیصد ہونا لازمی ہے",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Additional Academic Info (Elective Subjects, Phone)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "تعلیمی و رابطہ کی معلومات:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        val electives = listOf(student.subject1, student.subject2, student.subject3).filter { it.isNotBlank() }
                        if (electives.isNotEmpty()) {
                            Text(
                                text = "اختیاری مضامین: ${electives.joinToString(" • ")}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (student.marks != null) {
                            Text(
                                text = "میٹرک نمبرات: ${student.marks}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            text = "طالب علم فون: ${student.phone}",
                            style = MaterialTheme.typography.bodySmall
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

                // Direct Parent / Student Communication Actions
                if (contactPhone.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val msg = "محترم والد/سرپرست، گورنمنٹ ایسوسی ایٹ کالج مخدوم رشید ملتان کی جانب سے آپ کے صاحبزادے ${student.name} (رول نمبر ${student.rollNumber}) کا حاضری ریکارڈ: مجموعی حاضری ${String.format(Locale.US, "%.1f", stats?.overallPercentage ?: 85.0)} فیصد ہے۔"
                                CommunicationHelper.openWhatsApp(context, contactPhone, msg)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("واٹس ایپ", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = {
                                CommunicationHelper.openDialer(context, contactPhone)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmeraldPresent)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("کال کریں", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    )
}

/**
 * Recharts / D3 inspired Canvas line chart visualizing attendance percentage trend over the semester.
 */
@Composable
fun SemesterAttendanceTrendChart(
    trendPoints: List<StudentSemesterTrendPoint>,
    modifier: Modifier = Modifier
) {
    if (trendPoints.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("کوئی رجحان ڈیٹا موجود نہیں ہے", style = MaterialTheme.typography.bodySmall)
        }
        return
    }

    var selectedIndex by remember { mutableIntStateOf(-1) }

    Column(modifier = modifier) {
        // Active point tooltip (if selected)
        if (selectedIndex in trendPoints.indices) {
            val pt = trendPoints[selectedIndex]
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = NavyPrimary,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 4.dp)
            ) {
                Text(
                    text = "${pt.monthLabel}: ${String.format(Locale.US, "%.1f", pt.percentage)}% (حاضر: ${pt.presentSessions}/${pt.totalSessions})",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val width = size.width
                val height = size.height
                val paddingLeft = 45f
                val paddingRight = 30f
                val paddingTop = 20f
                val paddingBottom = 30f

                val chartWidth = width - paddingLeft - paddingRight
                val chartHeight = height - paddingTop - paddingBottom

                // Draw Horizontal Grid lines and percentage levels (100%, 75%, 50%, 25%, 0%)
                val levels = listOf(100, 75, 50, 25, 0)
                levels.forEach { level ->
                    val y = paddingTop + chartHeight * (1f - level / 100f)

                    if (level == 75) {
                        // 75% Benchmark threshold line
                        drawLine(
                            color = CrimsonAbsent.copy(alpha = 0.8f),
                            start = Offset(paddingLeft, y),
                            end = Offset(width - paddingRight, y),
                            strokeWidth = 2.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    } else {
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.5f),
                            start = Offset(paddingLeft, y),
                            end = Offset(width - paddingRight, y),
                            strokeWidth = 1f
                        )
                    }
                }

                if (trendPoints.size < 2) return@Canvas

                val stepX = chartWidth / (trendPoints.size - 1)
                val coordinates = trendPoints.mapIndexed { index, pt ->
                    val clampedPct = pt.percentage.coerceIn(0.0, 100.0).toFloat()
                    val x = paddingLeft + index * stepX
                    val y = paddingTop + chartHeight * (1f - clampedPct / 100f)
                    Offset(x, y)
                }

                // 1. Draw Gradient Fill under the curve (Area Chart styling)
                val fillPath = Path().apply {
                    moveTo(coordinates.first().x, coordinates.first().y)
                    for (i in 1 until coordinates.size) {
                        val prev = coordinates[i - 1]
                        val curr = coordinates[i]
                        val cX = (prev.x + curr.x) / 2f
                        cubicTo(cX, prev.y, cX, curr.y, curr.x, curr.y)
                    }
                    lineTo(coordinates.last().x, paddingTop + chartHeight)
                    lineTo(coordinates.first().x, paddingTop + chartHeight)
                    close()
                }

                val areaGradient = Brush.verticalGradient(
                    colors = listOf(
                        NavyPrimary.copy(alpha = 0.35f),
                        NavyPrimary.copy(alpha = 0.02f)
                    ),
                    startY = paddingTop,
                    endY = paddingTop + chartHeight
                )
                drawPath(fillPath, brush = areaGradient)

                // 2. Draw Smooth Bezier Trend Line
                val linePath = Path().apply {
                    moveTo(coordinates.first().x, coordinates.first().y)
                    for (i in 1 until coordinates.size) {
                        val prev = coordinates[i - 1]
                        val curr = coordinates[i]
                        val cX = (prev.x + curr.x) / 2f
                        cubicTo(cX, prev.y, cX, curr.y, curr.x, curr.y)
                    }
                }

                drawPath(
                    linePath,
                    color = NavyPrimary,
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )

                // 3. Draw Data Point Dots & Benchmark Highlighting
                coordinates.forEachIndexed { index, offset ->
                    val pct = trendPoints[index].percentage
                    val dotColor = when {
                        pct >= 75.0 -> EmeraldPresent
                        pct >= 60.0 -> AmberLeave
                        else -> CrimsonAbsent
                    }

                    val isSelected = (index == selectedIndex)
                    val radius = if (isSelected) 8f else 5.5f

                    // Outer halo
                    drawCircle(
                        color = dotColor.copy(alpha = 0.3f),
                        radius = radius + 4f,
                        center = offset
                    )
                    // Inner dot
                    drawCircle(
                        color = dotColor,
                        radius = radius,
                        center = offset
                    )
                    // Center core
                    drawCircle(
                        color = Color.White,
                        radius = radius / 2f,
                        center = offset
                    )
                }
            }
        }

        // X-Axis Month Labels Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp, end = 20.dp, top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            trendPoints.forEachIndexed { index, pt ->
                Text(
                    text = pt.monthLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = if (selectedIndex == index) NavyPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.clickable {
                        selectedIndex = if (selectedIndex == index) -1 else index
                    }
                )
            }
        }
    }
}
