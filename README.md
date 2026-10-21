# MySpend 💰

**MySpend** is a modern, polished, native Android personal expense tracker tailored for users in India. Built with **Kotlin** and **Jetpack Compose (Material 3)**, it is completely **offline-first**, requiring **no mandatory accounts, no bank logins, no ads, and no external tracking**.

---

## 🌟 Key Features

### 1. 🛡️ App Lock & Security
- **4-Digit PIN Protection**: Restrict app access behind a secure PIN gate.
- **Custom Keypad**: Smooth numeric keypad with visual dot indicators and error feedback for incorrect attempts.
- **PIN Management**: Set, change, or turn off your PIN anytime in Settings (protected by current PIN verification).
- **One-Tap Quick Lock**: Lock the app immediately via the lock icon in the top app bar.
- **On-Device Security**: PIN is hashed with SHA-256 and salt locally—never stored in plain text.

### 2. 💳 Monthly Spending Limit & Dynamic Gradient Bar
- **Adjustable Monthly Balance**: Set and adjust your monthly spending target (e.g., ₹3,000, ₹5,000, or any custom amount in INR).
- **Real-Time Gradient Progress Bar**:
  - Dynamically calculates spent vs. remaining balance in real time.
  - Smoothly transitions across a color spectrum: **Green (Safe) ➔ Yellow/Amber (Warning) ➔ Red (Near limit / Exceeded)**.
  - Displays percentage used (e.g., `70% of budget used`).
- **Over-Budget Alert Banner**:
  - Triggers an immediate alert on the Dashboard if expenses exceed the configured balance:  
    `⚠️ Alert: You have exceeded your balance! (₹X over limit)`

### 3. 📊 Dashboard
- **Today's & Current Month's Spend**: Prominent summary cards formatted in Indian Rupees (`₹`).
- **Category Donut Chart**: Animated Canvas donut chart representing category-wise spending proportions with legends and percentages.
- **Quick Actions**: One-tap access to *Add Expense*, *All Expenses*, *Reports*, *PIN Lock*, and *Data & Privacy*.
- **Clutter-Free**: Strictly omits distracting recent transaction lists from the dashboard.

### 4. ⚡ Fast Expense Entry
- **Minimal Taps**: Large amount entry with automatic decimal handling and quick increment chips (`+₹100`, `+₹200`, `+₹500`, `+₹1000`).
- **9 Predefined Categories**:
  - 🍕 Food
  - 🚗 Travel
  - 🛍️ Shopping
  - 🎓 Education
  - 🧾 Bills
  - 🎬 Entertainment
  - 🏥 Health
  - 👤 Personal
  - 📦 Other
- **4 Payment Methods**: Cash, UPI, Debit/Credit Card, and Bank Transfer.
- **Date Selector**: Defaults to Today with quick chips for *Today*, *Yesterday*, and a Material 3 Calendar Picker.
- **Edit & Delete**: Full editing capabilities with an alert confirmation dialog before deletion.

### 5. 🔍 All Expenses Screen
- **Real-Time Search**: Search expenses by note, category, payment method, or amount.
- **Multi-Filter Chips**: Filter by Date Range (*All Time*, *Today*, *This Week*, *This Month*, *Last Month*), Category, or Payment Method.
- **Custom Sorting**: Sort by *Newest First*, *Oldest First*, *Amount: High to Low*, or *Amount: Low to High*.
- **Live Filtered Total**: Sticky header showing total ₹ amount and count for the active search and filter criteria.

### 6. 📈 Reports & Trends
- **Summary Totals**: Daily, weekly, monthly, and all-time recorded expenses.
- **6-Month Comparison Bar Chart**: Historical monthly spending trends with the current month highlighted.
- **Category Breakdown**: Ranked list with spend amount, percentage share, transaction counts, and visual progress bars.
- **Payment Method Distribution**: Spend breakdown across UPI, Cash, Cards, and Net Banking.

### 7. 🔒 Data Privacy, Backup & Offline Storage
- **100% Offline**: Powered by an on-device Android Room SQLite database (`myspend_database`).
- **Data Persistence**: Data survives app restarts, background killing, and phone reboots.
- **CSV Spreadsheet Export**: Export your complete transaction history to standard `.csv` format via Android Storage Access Framework.
- **JSON Backup & Restore**: Create full data backups in `.json` format and restore them anytime.
- **Zero Cloud Dependence**: No network calls, no telemetry, no tracking.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.2
- **UI Toolkit**: Jetpack Compose with Material Design 3 (M3)
- **Database**: Android Jetpack Room (2.7.0) with KSP
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
- **State Management**: Kotlin Coroutines & `StateFlow` / `SharedFlow`
- **Charts & Graphics**: Jetpack Compose Canvas (custom animated donut & bar charts)
- **Local Storage**: Encrypted/Hashed SharedPreferences via `PreferenceManager`
- **Testing**: Robolectric & JUnit 4 JVM unit testing

---

## 📂 Project Architecture

```
app/src/main/java/com/example/
├── MainActivity.kt                  # App root, M3 Scaffold, Tab navigation & PIN gate
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt           # Room Database definition & singleton builder
│   │   ├── ExpenseDao.kt            # Room DAO (Reactive Flow queries, CRUD)
│   │   └── PreferenceManager.kt     # Hashed PIN & Monthly Balance persistence
│   ├── model/
│   │   ├── Category.kt              # ExpenseCategory enum (icons, colors, titles)
│   │   ├── ExpenseEntity.kt         # Room @Entity data class
│   │   └── PaymentMethod.kt         # PaymentMethod enum (Cash, UPI, Card, Bank)
│   └── repository/
│       └── ExpenseRepository.kt     # Data layer, CSV export & JSON backup/restore
├── ui/
│   ├── ExpenseViewModel.kt          # StateFlow management, filtering & totals
│   ├── components/
│   │   ├── AddEditExpenseSheet.kt   # Fast entry modal bottom sheet with validation
│   │   ├── BackupRestoreDialog.kt   # CSV export, JSON backup & restore dialog
│   │   ├── BalanceProgressBar.kt    # Dynamic gradient progress bar & over-budget alert
│   │   ├── CategoryChart.kt         # Custom Canvas donut chart & legend
│   │   ├── ManagePinDialog.kt       # Set, change, or turn off PIN dialog
│   │   ├── MonthlyBarChart.kt       # 6-month comparison Canvas bar chart
│   │   ├── PinLockScreen.kt         # Full-screen PIN security keypad
│   │   └── SetBalanceDialog.kt      # Monthly spending limit configuration dialog
│   ├── screens/
│   │   ├── AllExpensesScreen.kt     # Filterable, searchable expense list
│   │   ├── DashboardScreen.kt       # Overview cards, balance bar & category chart
│   │   └── ReportsScreen.kt         # Totals, comparison charts & breakdowns
│   └── theme/
│       ├── Color.kt                 # Financial indigo, emerald, amber & dark palette
│       ├── Theme.kt                 # Material 3 light/dark color schemes
│       └── Type.kt                  # Typography definitions
└── util/
    ├── CurrencyUtils.kt             # Indian Rupee (₹) formatting & parsing
    └── DateUtils.kt                 # Date range calculations & Indian date formatting
```

---

## 🚀 Building & Running the Project

### Prerequisites
- Android Studio Ladybug / Meerkat or later
- Android SDK 36 (compileSdk) / Min SDK 24 (Android 7.0+)
- JDK 17 or higher
- Gradle 9.x

### Build Commands

```bash
# Build Debug APK
gradle :app:assembleDebug

# Run Unit and Robolectric Tests
gradle :app:testDebugUnitTest

# Check Code Quality
gradle :app:lint
```

---

## 📱 Installing on Your Android Device

1. Download the generated **`MySpend.apk`** file from the root directory or from `app/build/outputs/apk/debug/app-debug.apk`.
2. Transfer `MySpend.apk` to your phone via USB, Google Drive, or messaging.
3. Tap on the `.apk` file on your device.
4. When prompted by Android with *"Install unknown apps"*, tap **Settings** and enable **"Allow from this source"**.
5. Tap **Install** and enjoy managing your expenses securely with **MySpend**!
