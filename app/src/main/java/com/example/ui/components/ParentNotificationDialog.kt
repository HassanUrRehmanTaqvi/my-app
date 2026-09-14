package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentEntity
import com.example.ui.theme.CrimsonAbsent
import com.example.ui.theme.EmeraldPresent
import com.example.util.CommunicationHelper
import com.example.util.NotificationPreferences
import com.example.util.ParentNotificationHelper

enum class NotificationType {
    ABSENCE,
    TEST_RESULT
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ParentNotificationDialog(
    student: StudentEntity,
    notificationType: NotificationType,
    initialMessage: String,
    parentPhone: String,
    onDismiss: () -> Unit,
    // Optional parameters for Test Result dynamic threshold updates
    testDetails: TestResultData? = null,
    teacherName: String = "",
    teacherDepartment: String = "",
    collegeName: String = ParentNotificationHelper.DEFAULT_COLLEGE_NAME,
    onMessageSent: (status: String) -> Unit = {}
) {
    val context = LocalContext.current
    var messageText by remember { mutableStateOf(initialMessage) }
    var isEditing by remember { mutableStateOf(false) }

    // Configurable thresholds for test performance evaluation
    var goodThreshold by remember {
        mutableDoubleStateOf(NotificationPreferences.getGoodThreshold(context))
    }
    var lowThreshold by remember {
        mutableDoubleStateOf(NotificationPreferences.getLowThreshold(context))
    }

    // If this is a test report and testDetails is provided, regenerate when threshold changes
    LaunchedEffect(goodThreshold, lowThreshold) {
        if (notificationType == NotificationType.TEST_RESULT && testDetails != null && !isEditing) {
            messageText = ParentNotificationHelper.generateTestResultNotification(
                studentName = student.name,
                rollNumber = student.rollNumber,
                testName = testDetails.testName,
                subject = testDetails.subjectName,
                date = testDetails.date,
                obtainedMarks = testDetails.obtainedMarks,
                totalMarks = testDetails.totalMarks,
                percentage = testDetails.percentage,
                teacherName = teacherName,
                department = teacherDepartment,
                collegeName = collegeName,
                goodThreshold = goodThreshold,
                lowThreshold = lowThreshold
            )
        }
    }

    val contactNumber = parentPhone.ifBlank { student.phone }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("parent_notification_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (notificationType == NotificationType.ABSENCE)
                            CrimsonAbsent.copy(alpha = 0.15f)
                        else
                            MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = if (notificationType == NotificationType.ABSENCE)
                                    CrimsonAbsent
                                else
                                    MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (notificationType == NotificationType.ABSENCE)
                                "والدین کو غیر حاضری کا پیغام"
                            else
                                "والدین کو ٹیسٹ رپورٹ بھیجیں",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${student.name} (رول نمبر: ${student.rollNumber})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = { isEditing = !isEditing },
                    modifier = Modifier.testTag("toggle_edit_message_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "پیغام ترمیم کریں",
                        tint = if (isEditing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                // Student & Guardian Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "طالب علم: ${student.name}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "رول نمبر: ${student.rollNumber}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "والد کا نام: ${student.fatherName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "رابطہ نمبر: ${contactNumber.ifBlank { "دستیاب نہیں" }}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (contactNumber.isNotBlank()) MaterialTheme.colorScheme.primary else CrimsonAbsent
                            )
                        }
                    }
                }

                // If Test Result: Show configurable threshold control
                if (notificationType == NotificationType.TEST_RESULT && testDetails != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "مثبت کارکردگی کا معیار (Good Threshold):",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Text(
                                    text = "${goodThreshold.toInt()}٪",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldPresent
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(50.0, 60.0, 70.0, 75.0, 80.0).forEach { th ->
                                    FilterChip(
                                        selected = goodThreshold == th,
                                        onClick = {
                                            goodThreshold = th
                                            NotificationPreferences.setGoodThreshold(context, th)
                                        },
                                        label = { Text("${th.toInt()}%") }
                                    )
                                }
                            }
                        }
                    }
                }

                // Message Preview / Editor
                Text(
                    text = if (isEditing) "پیغام میں ترمیم کریں (Edit Message):" else "پیغام کا پیش منظر (Message Preview):",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                if (isEditing) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .testTag("parent_message_editor_input"),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp)
                    )
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Box(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = messageText,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 24.sp,
                                    letterSpacing = 0.2.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Action Buttons Row (Copy, SMS, WhatsApp, Share)
                Text(
                    text = "ارسال کرنے کے اختیارات (Send Options):",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Copy Message
                    OutlinedButton(
                        onClick = {
                            CommunicationHelper.copyToClipboard(context, messageText, "Parent Notification")
                            onMessageSent("Copied")
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("copy_parent_message_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("کاپی کریں")
                    }

                    // 2. Send SMS
                    Button(
                        onClick = {
                            CommunicationHelper.sendSms(context, contactNumber, messageText)
                            onMessageSent("SMS Opened")
                        },
                        shape = RoundedCornerShape(8.dp),
                        enabled = contactNumber.isNotBlank(),
                        modifier = Modifier.testTag("send_sms_parent_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ایس ایم ایس")
                    }

                    // 3. WhatsApp
                    Button(
                        onClick = {
                            CommunicationHelper.openWhatsApp(context, contactNumber, messageText)
                            onMessageSent("WhatsApp Opened")
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        enabled = contactNumber.isNotBlank(),
                        modifier = Modifier.testTag("send_whatsapp_parent_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("واٹس ایپ", color = Color.White)
                    }

                    // 4. Share Sheet
                    OutlinedButton(
                        onClick = {
                            CommunicationHelper.shareText(
                                context,
                                messageText,
                                "والدین کو پیغام بھیجیں (${student.name})"
                            )
                            onMessageSent("Shared")
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("share_parent_message_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("شیئر کریں")
                    }

                    // 5. Direct Phone Call
                    if (contactNumber.isNotBlank()) {
                        OutlinedButton(
                            onClick = { CommunicationHelper.makePhoneCall(context, contactNumber) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("call_guardian_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = EmeraldPresent
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("کال کریں", color = EmeraldPresent)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("close_parent_notification_btn")
            ) {
                Text("بند کریں")
            }
        }
    )
}

data class TestResultData(
    val testName: String,
    val subjectName: String,
    val date: String,
    val obtainedMarks: Double,
    val totalMarks: Double,
    val percentage: Double
)
