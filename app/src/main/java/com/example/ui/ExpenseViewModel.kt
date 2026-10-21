package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseEntity
import com.example.data.model.PaymentMethod
import com.example.data.repository.ExpenseRepository
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class DateFilterType(val label: String) {
    ALL("All Time"),
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    CUSTOM("Custom Range")
}

enum class SortOrder(val label: String) {
    NEWEST("Newest First"),
    OLDEST("Oldest First"),
    AMOUNT_HIGH("Amount: High to Low"),
    AMOUNT_LOW("Amount: Low to High")
}

data class CategorySpend(
    val category: ExpenseCategory,
    val totalAmount: Double,
    val count: Int,
    val percentage: Float
)

data class PaymentMethodSpend(
    val method: PaymentMethod,
    val totalAmount: Double,
    val count: Int,
    val percentage: Float
)

data class MonthSpend(
    val monthLabel: String,
    val year: Int,
    val monthIndex: Int,
    val totalAmount: Double,
    val isCurrentMonth: Boolean
)

data class FilterState(
    val searchQuery: String = "",
    val category: String? = null,
    val paymentMethod: String? = null,
    val dateFilter: DateFilterType = DateFilterType.ALL,
    val customDateRange: Pair<Long, Long>? = null,
    val sortOrder: SortOrder = SortOrder.NEWEST
)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository = ExpenseRepository(
        AppDatabase.getDatabase(application).expenseDao(),
        application
    )

    val allExpenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val _editingExpense = MutableStateFlow<ExpenseEntity?>(null)
    val editingExpense: StateFlow<ExpenseEntity?> = _editingExpense.asStateFlow()

    private val _showAddEditSheet = MutableStateFlow(false)
    val showAddEditSheet: StateFlow<Boolean> = _showAddEditSheet.asStateFlow()

    private val preferenceManager = com.example.data.local.PreferenceManager(application)

    private val _isPinEnabled = MutableStateFlow(preferenceManager.isPinEnabled)
    val isPinEnabled: StateFlow<Boolean> = _isPinEnabled.asStateFlow()

    private val _isAppLocked = MutableStateFlow(preferenceManager.isPinEnabled)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _monthlyBalance = MutableStateFlow(preferenceManager.monthlyBalance)
    val monthlyBalance: StateFlow<Double> = _monthlyBalance.asStateFlow()

    private val _showSetBalanceDialog = MutableStateFlow(false)
    val showSetBalanceDialog: StateFlow<Boolean> = _showSetBalanceDialog.asStateFlow()

    private val _showManagePinDialog = MutableStateFlow(false)
    val showManagePinDialog: StateFlow<Boolean> = _showManagePinDialog.asStateFlow()

    private val _showBackupDialog = MutableStateFlow(false)
    val showBackupDialog: StateFlow<Boolean> = _showBackupDialog.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Filtered expenses for "All Expenses" screen
    val filteredExpenses: StateFlow<List<ExpenseEntity>> = combine(allExpenses, _filterState) { list, filter ->
        var result = list

        // Search Query (notes, category, payment method, amount)
        if (filter.searchQuery.isNotBlank()) {
            val q = filter.searchQuery.trim().lowercase()
            result = result.filter {
                it.note.lowercase().contains(q) ||
                it.category.lowercase().contains(q) ||
                it.paymentMethod.lowercase().contains(q) ||
                it.amount.toString().contains(q)
            }
        }

        // Category filter
        if (filter.category != null) {
            result = result.filter { it.category.equals(filter.category, ignoreCase = true) }
        }

        // Payment Method filter
        if (filter.paymentMethod != null) {
            result = result.filter { it.paymentMethod.equals(filter.paymentMethod, ignoreCase = true) }
        }

        // Date range filter
        result = when (filter.dateFilter) {
            DateFilterType.ALL -> result
            DateFilterType.TODAY -> {
                val start = DateUtils.getStartOfDay()
                val end = DateUtils.getEndOfDay()
                result.filter { it.dateMillis in start..end }
            }
            DateFilterType.THIS_WEEK -> {
                val start = DateUtils.getStartOfWeek()
                val end = DateUtils.getEndOfWeek()
                result.filter { it.dateMillis in start..end }
            }
            DateFilterType.THIS_MONTH -> {
                val start = DateUtils.getStartOfMonth()
                val end = DateUtils.getEndOfMonth()
                result.filter { it.dateMillis in start..end }
            }
            DateFilterType.LAST_MONTH -> {
                val (start, end) = DateUtils.getLastMonthStartAndEnd()
                result.filter { it.dateMillis in start..end }
            }
            DateFilterType.CUSTOM -> {
                filter.customDateRange?.let { (start, end) ->
                    result.filter { it.dateMillis in start..end }
                } ?: result
            }
        }

        // Sort order
        when (filter.sortOrder) {
            SortOrder.NEWEST -> result.sortedWith(compareByDescending<ExpenseEntity> { it.dateMillis }.thenByDescending { it.id })
            SortOrder.OLDEST -> result.sortedWith(compareBy<ExpenseEntity> { it.dateMillis }.thenBy { it.id })
            SortOrder.AMOUNT_HIGH -> result.sortedWith(compareByDescending<ExpenseEntity> { it.amount }.thenByDescending { it.dateMillis })
            SortOrder.AMOUNT_LOW -> result.sortedWith(compareBy<ExpenseEntity> { it.amount }.thenByDescending { it.dateMillis })
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered total
    val filteredTotal: StateFlow<Double> = combine(filteredExpenses) { list ->
        list[0].sumOf { it.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    // Dashboard: Today's Total
    val todayTotal: StateFlow<Double> = combine(allExpenses) { list ->
        val start = DateUtils.getStartOfDay()
        val end = DateUtils.getEndOfDay()
        list[0].filter { it.dateMillis in start..end }.sumOf { it.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    // Dashboard: Current Month Total
    val currentMonthTotal: StateFlow<Double> = combine(allExpenses) { list ->
        val start = DateUtils.getStartOfMonth()
        val end = DateUtils.getEndOfMonth()
        list[0].filter { it.dateMillis in start..end }.sumOf { it.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    // Dashboard: Category-wise spending for current month
    val currentMonthCategorySpend: StateFlow<List<CategorySpend>> = combine(allExpenses) { list ->
        val start = DateUtils.getStartOfMonth()
        val end = DateUtils.getEndOfMonth()
        val monthExpenses = list[0].filter { it.dateMillis in start..end }
        val total = monthExpenses.sumOf { it.amount }

        if (total == 0.0) {
            emptyList()
        } else {
            monthExpenses
                .groupBy { ExpenseCategory.fromTitle(it.category) }
                .map { (cat, items) ->
                    val sum = items.sumOf { it.amount }
                    CategorySpend(
                        category = cat,
                        totalAmount = sum,
                        count = items.size,
                        percentage = (sum / total).toFloat()
                    )
                }
                .sortedByDescending { it.totalAmount }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Reports: All-time category spend
    val allTimeCategorySpend: StateFlow<List<CategorySpend>> = combine(allExpenses) { list ->
        val total = list[0].sumOf { it.amount }
        if (total == 0.0) {
            emptyList()
        } else {
            list[0]
                .groupBy { ExpenseCategory.fromTitle(it.category) }
                .map { (cat, items) ->
                    val sum = items.sumOf { it.amount }
                    CategorySpend(
                        category = cat,
                        totalAmount = sum,
                        count = items.size,
                        percentage = (sum / total).toFloat()
                    )
                }
                .sortedByDescending { it.totalAmount }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Reports: This Week Total
    val thisWeekTotal: StateFlow<Double> = combine(allExpenses) { list ->
        val start = DateUtils.getStartOfWeek()
        val end = DateUtils.getEndOfWeek()
        list[0].filter { it.dateMillis in start..end }.sumOf { it.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    // Reports: All-time Total
    val allTimeTotal: StateFlow<Double> = combine(allExpenses) { list ->
        list[0].sumOf { it.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    // Reports: Payment Method Distribution
    val paymentMethodSpend: StateFlow<List<PaymentMethodSpend>> = combine(allExpenses) { list ->
        val total = list[0].sumOf { it.amount }
        if (total == 0.0) {
            emptyList()
        } else {
            list[0]
                .groupBy { PaymentMethod.fromTitle(it.paymentMethod) }
                .map { (method, items) ->
                    val sum = items.sumOf { it.amount }
                    PaymentMethodSpend(
                        method = method,
                        totalAmount = sum,
                        count = items.size,
                        percentage = (sum / total).toFloat()
                    )
                }
                .sortedByDescending { it.totalAmount }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Reports: Monthly comparison for the last 6 calendar months
    val monthlyComparison: StateFlow<List<MonthSpend>> = combine(allExpenses) { list ->
        val expenses = list[0]
        val result = mutableListOf<MonthSpend>()
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        for (i in 5 downTo 0) {
            val monthCal = Calendar.getInstance().apply {
                add(Calendar.MONTH, -i)
            }
            val start = DateUtils.getStartOfMonth(monthCal)
            val end = DateUtils.getEndOfMonth(monthCal)
            val monthSum = expenses.filter { it.dateMillis in start..end }.sumOf { it.amount }
            val isCurrent = monthCal.get(Calendar.MONTH) == currentMonth && monthCal.get(Calendar.YEAR) == currentYear

            result.add(
                MonthSpend(
                    monthLabel = DateUtils.formatMonthAbbr(start),
                    year = monthCal.get(Calendar.YEAR),
                    monthIndex = monthCal.get(Calendar.MONTH),
                    totalAmount = monthSum,
                    isCurrentMonth = isCurrent
                )
            )
        }
        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filter mutations
    fun setSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(searchQuery = query)
    }

    fun setCategoryFilter(category: String?) {
        _filterState.value = _filterState.value.copy(category = category)
    }

    fun setPaymentMethodFilter(method: String?) {
        _filterState.value = _filterState.value.copy(paymentMethod = method)
    }

    fun setDateFilter(dateFilter: DateFilterType, customRange: Pair<Long, Long>? = null) {
        _filterState.value = _filterState.value.copy(
            dateFilter = dateFilter,
            customDateRange = customRange
        )
    }

    fun setSortOrder(order: SortOrder) {
        _filterState.value = _filterState.value.copy(sortOrder = order)
    }

    fun clearAllFilters() {
        _filterState.value = FilterState()
    }

    // Sheet / Dialog controls
    fun openAddExpense() {
        _editingExpense.value = null
        _showAddEditSheet.value = true
    }

    fun openEditExpense(expense: ExpenseEntity) {
        _editingExpense.value = expense
        _showAddEditSheet.value = true
    }

    fun closeAddEditSheet() {
        _showAddEditSheet.value = false
        _editingExpense.value = null
    }

    fun openBackupDialog() {
        _showBackupDialog.value = true
    }

    fun closeBackupDialog() {
        _showBackupDialog.value = false
    }

    // Monthly Balance Controls
    fun openSetBalanceDialog() {
        _showSetBalanceDialog.value = true
    }

    fun closeSetBalanceDialog() {
        _showSetBalanceDialog.value = false
    }

    fun saveMonthlyBalance(amount: Double) {
        preferenceManager.monthlyBalance = amount
        _monthlyBalance.value = amount
        closeSetBalanceDialog()
        viewModelScope.launch {
            _userMessage.emit("Monthly balance limit set to ${com.example.util.CurrencyUtils.formatInr(amount)}")
        }
    }

    // PIN Lock Controls
    fun openManagePinDialog() {
        _showManagePinDialog.value = true
    }

    fun closeManagePinDialog() {
        _showManagePinDialog.value = false
    }

    fun verifyPin(pin: String): Boolean {
        return preferenceManager.verifyPin(pin)
    }

    fun unlockApp(pin: String): Boolean {
        val valid = preferenceManager.verifyPin(pin)
        if (valid) {
            _isAppLocked.value = false
        }
        return valid
    }

    fun lockApp() {
        if (_isPinEnabled.value) {
            _isAppLocked.value = true
        }
    }

    fun setNewPin(pin: String) {
        preferenceManager.setPin(pin)
        _isPinEnabled.value = true
        _isAppLocked.value = false
        viewModelScope.launch {
            _userMessage.emit("App PIN lock enabled successfully")
        }
    }

    fun disablePin() {
        preferenceManager.disablePin()
        _isPinEnabled.value = false
        _isAppLocked.value = false
        viewModelScope.launch {
            _userMessage.emit("App PIN lock turned off")
        }
    }

    // CRUD Actions
    fun saveExpense(
        amount: Double,
        category: String,
        dateMillis: Long,
        paymentMethod: String,
        note: String
    ) {
        viewModelScope.launch {
            val current = _editingExpense.value
            if (current == null) {
                // Add new
                repository.addExpense(
                    ExpenseEntity(
                        amount = amount,
                        category = category,
                        dateMillis = dateMillis,
                        paymentMethod = paymentMethod,
                        note = note.trim()
                    )
                )
                _userMessage.emit("Expense added successfully")
            } else {
                // Update existing
                repository.updateExpense(
                    current.copy(
                        amount = amount,
                        category = category,
                        dateMillis = dateMillis,
                        paymentMethod = paymentMethod,
                        note = note.trim()
                    )
                )
                _userMessage.emit("Expense updated")
            }
            closeAddEditSheet()
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            _userMessage.emit("Expense deleted")
            if (_editingExpense.value?.id == expense.id) {
                closeAddEditSheet()
            }
        }
    }

    fun exportToCsv(uri: Uri) {
        viewModelScope.launch {
            repository.exportCsv(uri)
                .onSuccess { count ->
                    _userMessage.emit("Successfully exported $count expenses to CSV")
                }
                .onFailure {
                    _userMessage.emit("CSV Export failed: ${it.localizedMessage}")
                }
        }
    }

    fun exportBackupJson(uri: Uri) {
        viewModelScope.launch {
            repository.exportBackupJson(uri)
                .onSuccess { count ->
                    _userMessage.emit("Backup created ($count expenses saved)")
                }
                .onFailure {
                    _userMessage.emit("Backup failed: ${it.localizedMessage}")
                }
        }
    }

    fun restoreBackup(uri: Uri) {
        viewModelScope.launch {
            repository.importData(uri)
                .onSuccess { count ->
                    _userMessage.emit("Successfully imported $count expenses")
                }
                .onFailure {
                    _userMessage.emit("Import failed: ${it.localizedMessage}")
                }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAll()
            _userMessage.emit("All expense records cleared")
        }
    }

    fun loadSampleExpenses() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val dayMillis = 24 * 3600 * 1000L
            repository.addExpense(
                ExpenseEntity(
                    amount = 80.0,
                    category = ExpenseCategory.FOOD.title,
                    dateMillis = now,
                    paymentMethod = PaymentMethod.UPI.title,
                    note = "Masala Chai & Samosa"
                )
            )
            repository.addExpense(
                ExpenseEntity(
                    amount = 250.0,
                    category = ExpenseCategory.TRAVEL.title,
                    dateMillis = now,
                    paymentMethod = PaymentMethod.CASH.title,
                    note = "Auto Rickshaw"
                )
            )
            repository.addExpense(
                ExpenseEntity(
                    amount = 850.0,
                    category = ExpenseCategory.SHOPPING.title,
                    dateMillis = now,
                    paymentMethod = PaymentMethod.UPI.title,
                    note = "Groceries & Vegetables"
                )
            )
            repository.addExpense(
                ExpenseEntity(
                    amount = 1499.0,
                    category = ExpenseCategory.BILLS.title,
                    dateMillis = now - (2 * dayMillis),
                    paymentMethod = PaymentMethod.BANK_TRANSFER.title,
                    note = "Broadband WiFi bill"
                )
            )
            _userMessage.emit("Sample expenses loaded! You can edit or delete them anytime.")
        }
    }
}
