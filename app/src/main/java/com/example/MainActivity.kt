package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ExpenseViewModel
import com.example.ui.components.AddEditExpenseSheet
import com.example.ui.components.BackupRestoreDialog
import com.example.ui.components.ManagePinDialog
import com.example.ui.components.PinLockScreen
import com.example.ui.components.SetBalanceDialog
import com.example.ui.screens.AllExpensesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class AppScreen(val title: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Dashboard", Icons.Rounded.Dashboard, "nav_dashboard"),
    ALL_EXPENSES("All Expenses", Icons.Rounded.ReceiptLong, "nav_all_expenses"),
    REPORTS("Reports", Icons.Rounded.BarChart, "nav_reports")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MySpendApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MySpendApp(viewModel: ExpenseViewModel = viewModel()) {
    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }
    val snackbarHostState = remember { SnackbarHostState() }

    val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()
    val isPinEnabled by viewModel.isPinEnabled.collectAsStateWithLifecycle()
    val monthlyBalance by viewModel.monthlyBalance.collectAsStateWithLifecycle()

    val showAddEditSheet by viewModel.showAddEditSheet.collectAsStateWithLifecycle()
    val editingExpense by viewModel.editingExpense.collectAsStateWithLifecycle()
    val showBackupDialog by viewModel.showBackupDialog.collectAsStateWithLifecycle()
    val showSetBalanceDialog by viewModel.showSetBalanceDialog.collectAsStateWithLifecycle()
    val showManagePinDialog by viewModel.showManagePinDialog.collectAsStateWithLifecycle()

    // Collect snackbar messages
    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // App Lock Gate: If locked, show full-screen PinLockScreen
    if (isAppLocked) {
        PinLockScreen(
            onVerifyPin = { pin -> viewModel.unlockApp(pin) },
            onSuccess = { /* state handled in unlockApp */ }
        )
        return
    }

    // Back button handling
    BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
        currentScreen = AppScreen.DASHBOARD
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentScreen) {
                            AppScreen.DASHBOARD -> "MySpend"
                            AppScreen.ALL_EXPENSES -> "All Expenses"
                            AppScreen.REPORTS -> "Reports & Trends"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    if (isPinEnabled) {
                        IconButton(
                            onClick = { viewModel.lockApp() },
                            modifier = Modifier.testTag("top_bar_lock_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = "Lock App Now",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.openBackupDialog() },
                        modifier = Modifier.testTag("top_bar_backup_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = "Data & Privacy Backup",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                AppScreen.entries.forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag(screen.tag)
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddExpense() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                modifier = Modifier.testTag("add_expense_fab")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add Expense",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    AppScreen.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToAllExpenses = { currentScreen = AppScreen.ALL_EXPENSES },
                        onNavigateToReports = { currentScreen = AppScreen.REPORTS }
                    )

                    AppScreen.ALL_EXPENSES -> AllExpensesScreen(
                        viewModel = viewModel
                    )

                    AppScreen.REPORTS -> ReportsScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    // Add / Edit Expense Modal Bottom Sheet
    if (showAddEditSheet) {
        AddEditExpenseSheet(
            expenseToEdit = editingExpense,
            onDismiss = { viewModel.closeAddEditSheet() },
            onSave = { amount, category, dateMillis, paymentMethod, note ->
                viewModel.saveExpense(
                    amount = amount,
                    category = category,
                    dateMillis = dateMillis,
                    paymentMethod = paymentMethod,
                    note = note
                )
            },
            onDelete = { expense ->
                viewModel.deleteExpense(expense)
            }
        )
    }

    // Set Monthly Balance Limit Dialog
    if (showSetBalanceDialog) {
        SetBalanceDialog(
            currentBalance = monthlyBalance,
            onDismiss = { viewModel.closeSetBalanceDialog() },
            onConfirm = { amount ->
                viewModel.saveMonthlyBalance(amount)
            }
        )
    }

    // Manage App PIN Lock Dialog
    if (showManagePinDialog) {
        ManagePinDialog(
            isPinCurrentlyEnabled = isPinEnabled,
            onVerifyCurrentPin = { pin -> viewModel.verifyPin(pin) },
            onSetNewPin = { pin -> viewModel.setNewPin(pin) },
            onDisablePin = { viewModel.disablePin() },
            onDismiss = { viewModel.closeManagePinDialog() }
        )
    }

    // Data & Privacy / Settings Dialog
    if (showBackupDialog) {
        BackupRestoreDialog(
            onDismiss = { viewModel.closeBackupDialog() },
            onExportCsv = { uri -> viewModel.exportToCsv(uri) },
            onExportBackup = { uri -> viewModel.exportBackupJson(uri) },
            onRestore = { uri -> viewModel.restoreBackup(uri) },
            onClearAll = { viewModel.clearAllData() },
            onManagePin = { viewModel.openManagePinDialog() },
            onSetBalance = { viewModel.openSetBalanceDialog() }
        )
    }
}
