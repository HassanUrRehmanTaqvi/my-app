package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SwitchAccount
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.InitialSeedData
import com.example.data.model.SubjectEntity
import com.example.data.model.UserEntity
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPresent
import com.example.ui.viewmodel.CollegeViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: CollegeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val ownerId by viewModel.currentOwnerId.collectAsStateWithLifecycle()
    val subjects by viewModel.repository.getSubjects(ownerId).collectAsStateWithLifecycle(emptyList())

    // Profile form state
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var college by remember { mutableStateOf("") }
    var designation by remember { mutableStateOf("") }
    var defaultSubject by remember { mutableStateOf("") }
    var smsTemplate by remember { mutableStateOf("") }

    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var showCreateTeacherDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            name = it.name
            email = it.email
            college = it.college
            designation = it.designation
            defaultSubject = it.defaultSubject
            smsTemplate = it.smsTemplate
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "ترتیبات اور پروفائل (Settings & Master Config)",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "استاد کا پروفائل، مضامین کی ڈکشنری، گوگل ڈرائیو بیک اپ اور اردو میسج ٹیمپلیٹ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 1. Teacher Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "استاد کا پروفائل (Teacher Profile)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("استاد کا نام") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = designation,
                            onValueChange = { designation = it },
                            label = { Text("عہدہ (Designation)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = college,
                            onValueChange = { college = it },
                            label = { Text("کالج کا نام (College Name)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = defaultSubject,
                            onValueChange = { defaultSubject = it },
                            label = { Text("مخصوص مضمون (Default Subject)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                viewModel.updateProfile(
                                    name = name.trim(),
                                    email = email.trim(),
                                    college = college.trim(),
                                    designation = designation.trim(),
                                    defaultSubject = defaultSubject.trim(),
                                    smsTemplate = smsTemplate
                                )
                                Toast.makeText(context, "پروفائل کامیابی سے محفوظ ہو گئی!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("محفوظ کریں")
                        }
                    }
                }
            }

            // 2. Multi-Teacher Account Switcher (Sections 53, 54, 55, 56)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.SwitchAccount, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "اساتذہ کے پروفائلز (Multi-Teacher Profiles)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            OutlinedButton(
                                onClick = { showCreateTeacherDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("نیا استاد")
                            }
                        }

                        Text(
                            text = "ایک ہی ایپ میں مختلف مضامین کے اساتذہ کا ڈیٹا الگ الگ محفوظ رہتا ہے۔",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        allUsers.forEach { user ->
                            val isCurrent = user.userId == currentUser?.userId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${user.name} (${user.designation})",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "مضمون: ${user.defaultSubject} • ${user.college}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (isCurrent) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = EmeraldLight) {
                                        Text(
                                            text = "فعال (Active)",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = EmeraldPresent
                                        )
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { viewModel.switchUser(user.userId) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("سوئچ کریں", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }

            // 3. Subject Master & Aliases (Section 5)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "مضامین کی ڈکشنری (Subject Master)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            OutlinedButton(
                                onClick = { showAddSubjectDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("نیا مضمون")
                            }
                        }

                        Text(
                            text = "پنجاب بورڈ کے تحت لازمی اور اختیاری مضامین، اور ان کے متبادل نام (Aliases):",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        subjects.forEach { sub ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${sub.urduName} • ${sub.name}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "متبادل نام: ${sub.aliases.ifBlank { "کوئی نہیں" }}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (sub.type == "Compulsory") Color(0xFFEFF6FF) else Color(0xFFFDF2F8)
                                ) {
                                    Text(
                                        text = if (sub.type == "Compulsory") "لازمی" else "اختیاری",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (sub.type == "Compulsory") Color(0xFF1D4ED8) else Color(0xFFBE185D)
                                    )
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }

            // 4. Backup & Google Drive (Sections 39, 40, 41)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ڈیٹا بیک اپ اور بحالی (Backup & Restore)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Text(
                            text = "آپ کا تمام تعلیمی ڈیٹا، حاضری، ٹیسٹ نتائج اور اسٹوڈنٹ رول آپ کے ڈیوائس پر محفوظ ہیں۔ گوگل ڈرائیو میں فولڈر `College_Attendance_Backups/2026-27/` میں تاریخ کے ساتھ مکمل بیک اپ محفوظ کیا جا سکتا ہے۔",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val backupJson = viewModel.repository.exportDatabaseToJson(ownerId)
                                        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, backupJson)
                                            putExtra(Intent.EXTRA_SUBJECT, "College_Attendance_Backup_$timeStamp.json")
                                            type = "application/json"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Save Backup to Google Drive / Files"))
                                    }
                                },
                                modifier = Modifier.weight(1.2f).testTag("backup_now_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("بیک اپ محفوظ کریں")
                            }

                            OutlinedButton(
                                onClick = { showRestoreDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("بحال کریں (Restore)")
                            }
                        }
                    }
                }
            }
        }

        // Add Subject Dialog
        if (showAddSubjectDialog) {
            var subName by remember { mutableStateOf("") }
            var subUrdu by remember { mutableStateOf("") }
            var subType by remember { mutableStateOf("Compulsory") }
            var subLevel by remember { mutableStateOf("First Year") }
            var subAliases by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showAddSubjectDialog = false },
                title = { Text("نیا مضمون شامل کریں") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = subUrdu,
                            onValueChange = { subUrdu = it },
                            label = { Text("اردو نام (مثلاً اسلامیات لازمی)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = subName,
                            onValueChange = { subName = it },
                            label = { Text("انگریزی نام (مثلاً Islamic Studies)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = subAliases,
                            onValueChange = { subAliases = it },
                            label = { Text("متبادل نام / Aliases (کوما سے الگ)") },
                            placeholder = { Text("Islamic Education, اسلامیات, Ethics") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { subType = "Compulsory" },
                                modifier = Modifier.weight(1f),
                                colors = if (subType == "Compulsory") ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else ButtonDefaults.outlinedButtonColors()
                            ) {
                                Text("لازمی")
                            }
                            OutlinedButton(
                                onClick = { subType = "Elective" },
                                modifier = Modifier.weight(1f),
                                colors = if (subType == "Elective") ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else ButtonDefaults.outlinedButtonColors()
                            ) {
                                Text("اختیاری")
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (subName.isNotBlank() && subUrdu.isNotBlank()) {
                                viewModel.addSubject(subName.trim(), subUrdu.trim(), subType, subLevel, subAliases.trim())
                                showAddSubjectDialog = false
                            }
                        }
                    ) {
                        Text("محفوظ کریں")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddSubjectDialog = false }) {
                        Text("منسوخ")
                    }
                }
            )
        }

        // Create New Teacher Profile Dialog
        if (showCreateTeacherDialog) {
            var tName by remember { mutableStateOf("") }
            var tEmail by remember { mutableStateOf("") }
            var tCollege by remember { mutableStateOf("گورنمنٹ ایسوسی ایٹ کالج مخدوم رشید ملتان") }
            var tDesig by remember { mutableStateOf("لیکچرار") }
            var tSub by remember { mutableStateOf("فزکس") }

            AlertDialog(
                onDismissRequest = { showCreateTeacherDialog = false },
                title = { Text("نیا استاد پروفائل بنائیں") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = tName,
                            onValueChange = { tName = it },
                            label = { Text("استاد کا نام") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = tDesig,
                            onValueChange = { tDesig = it },
                            label = { Text("عہدہ") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = tSub,
                            onValueChange = { tSub = it },
                            label = { Text("مضمون") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = tCollege,
                            onValueChange = { tCollege = it },
                            label = { Text("کالج کا نام") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (tName.isNotBlank()) {
                                viewModel.createNewTeacherProfile(tName.trim(), tEmail.trim(), tCollege.trim(), tDesig.trim(), tSub.trim())
                                showCreateTeacherDialog = false
                            }
                        }
                    ) {
                        Text("پروفائل بنائیں")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateTeacherDialog = false }) {
                        Text("منسوخ")
                    }
                }
            )
        }

        // Restore Backup Dialog
        if (showRestoreDialog) {
            var jsonInput by remember { mutableStateOf("") }
            var restoreSuccess by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showRestoreDialog = false },
                title = { Text("بیک اپ بحال کریں (Restore Backup)") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("بیک اپ JSON کا مواد یہاں پیسٹ کریں:")
                        OutlinedTextField(
                            value = jsonInput,
                            onValueChange = { jsonInput = it },
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                            maxLines = 10
                        )
                        if (restoreSuccess) {
                            Text("بیک اپ کامیابی سے بحال ہو گیا!", color = EmeraldPresent, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val ok = viewModel.repository.restoreDatabaseFromJson(jsonInput, ownerId)
                                if (ok) {
                                    restoreSuccess = true
                                    Toast.makeText(context, "ڈیٹا کامیابی سے بحال ہو گیا!", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "بیک اپ بحال کرنے میں خامی آئی", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    ) {
                        Text("بحال کریں")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRestoreDialog = false }) {
                        Text("بند کریں")
                    }
                }
            )
        }
    }
}
