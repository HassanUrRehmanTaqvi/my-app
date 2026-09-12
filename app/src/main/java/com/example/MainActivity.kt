package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.ClassesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.NominalRollScreen
import com.example.ui.screens.ParentCommunicationScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TestsScreen
import com.example.ui.theme.CollegeAttendanceTheme
import com.example.ui.viewmodel.CollegeViewModel

sealed class AppDestination(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    data object Dashboard : AppDestination("dashboard", "ڈیش بورڈ", Icons.Default.Dashboard)
    data object Classes : AppDestination("classes", "کلاسز", Icons.Default.Class)
    data object Attendance : AppDestination("attendance", "حاضری", Icons.Default.CheckCircle)
    data object NominalRoll : AppDestination("nominal_roll", "نامینل رول", Icons.Default.Group)
    data object Reports : AppDestination("reports", "رپورٹس", Icons.Default.Assessment)
    data object Settings : AppDestination("settings", "ترتیبات", Icons.Default.Settings)
    data object ParentMessaging : AppDestination("parent_messaging", "پیغامات", Icons.AutoMirrored.Filled.Chat)
    data object Tests : AppDestination("tests", "ٹیسٹ", Icons.Default.Assignment)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CollegeAttendanceTheme {
                CollegeAttendanceApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollegeAttendanceApp() {
    val viewModel: CollegeViewModel = viewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val validationIssues by viewModel.validationIssues.collectAsStateWithLifecycle()

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbarMessage()
        }
    }

    var currentDestination by remember { mutableStateOf<AppDestination>(AppDestination.Dashboard) }
    var attendancePreselectedClassId by remember { mutableStateOf<String?>(null) }
    var messagingClassId by remember { mutableStateOf<String?>(null) }
    var messagingDate by remember { mutableStateOf<String?>(null) }
    var messagingMissedPeriods by remember { mutableIntStateOf(1) }
    var testPreselectedClassId by remember { mutableStateOf<String?>(null) }

    val bottomNavItems = listOf(
        AppDestination.Dashboard,
        AppDestination.Classes,
        AppDestination.Attendance,
        AppDestination.NominalRoll,
        AppDestination.Reports,
        AppDestination.Settings
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentDestination.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    // Tests Quick Button
                    IconButton(
                        onClick = { currentDestination = AppDestination.Tests },
                        modifier = Modifier.testTag("top_action_tests")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = "ٹیسٹ اور نتائج",
                            tint = if (currentDestination == AppDestination.Tests) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Parent Messages Quick Action
                    IconButton(
                        onClick = { currentDestination = AppDestination.ParentMessaging },
                        modifier = Modifier.testTag("top_action_messaging")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "والدین سے رابطہ",
                            tint = if (currentDestination == AppDestination.ParentMessaging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Validation Warnings Badge on Nominal Roll
                    if (validationIssues.isNotEmpty()) {
                        IconButton(
                            onClick = { currentDestination = AppDestination.NominalRoll },
                            modifier = Modifier.testTag("top_action_warnings")
                        ) {
                            BadgedBox(
                                badge = {
                                    Badge { Text("${validationIssues.size}") }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = "ماسٹر نامینل رول",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                bottomNavItems.forEach { destination ->
                    val selected = currentDestination == destination
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(imageVector = destination.icon, contentDescription = destination.title)
                        },
                        label = {
                            Text(
                                text = destination.title,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.testTag("nav_item_${destination.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                AppDestination.Dashboard -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToTakeAttendance = { classId ->
                            attendancePreselectedClassId = classId
                            currentDestination = AppDestination.Attendance
                        },
                        onNavigateToCreateClass = {
                            currentDestination = AppDestination.Classes
                        },
                        onNavigateToClassDetail = { classId ->
                            attendancePreselectedClassId = classId
                            currentDestination = AppDestination.Classes
                        },
                        onNavigateToNominalRoll = {
                            currentDestination = AppDestination.NominalRoll
                        },
                        onNavigateToReports = {
                            currentDestination = AppDestination.Reports
                        }
                    )
                }
                AppDestination.Classes -> {
                    ClassesScreen(
                        viewModel = viewModel,
                        onTakeAttendance = { classId ->
                            attendancePreselectedClassId = classId
                            currentDestination = AppDestination.Attendance
                        },
                        onAddTest = { classId ->
                            testPreselectedClassId = classId
                            currentDestination = AppDestination.Tests
                        }
                    )
                }
                AppDestination.Attendance -> {
                    AttendanceScreen(
                        viewModel = viewModel,
                        preselectedClassId = attendancePreselectedClassId,
                        onNavigateToParentMessaging = { classId, date, missedCount ->
                            messagingClassId = classId
                            messagingDate = date
                            messagingMissedPeriods = missedCount
                            currentDestination = AppDestination.ParentMessaging
                        }
                    )
                }
                AppDestination.ParentMessaging -> {
                    ParentCommunicationScreen(
                        viewModel = viewModel,
                        targetClassId = messagingClassId,
                        targetDate = messagingDate,
                        missedPeriods = messagingMissedPeriods
                    )
                }
                AppDestination.Tests -> {
                    TestsScreen(
                        viewModel = viewModel,
                        preselectedClassId = testPreselectedClassId
                    )
                }
                AppDestination.Reports -> {
                    ReportsScreen(
                        viewModel = viewModel
                    )
                }
                AppDestination.NominalRoll -> {
                    NominalRollScreen(
                        viewModel = viewModel
                    )
                }
                AppDestination.Settings -> {
                    SettingsScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
