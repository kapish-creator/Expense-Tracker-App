package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseEntity
import com.example.data.model.PaymentMethod
import com.example.util.CurrencyUtils
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read string from context verify app_name is MySpend`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("MySpend", appName)
    }

    @Test
    fun `test expense database operations insert update delete`() = runBlocking {
        val dao = database.expenseDao()

        // 1. Insert
        val id = dao.insertExpense(
            ExpenseEntity(
                amount = 250.0,
                category = ExpenseCategory.FOOD.title,
                dateMillis = System.currentTimeMillis(),
                paymentMethod = PaymentMethod.UPI.title,
                note = "Chai and samosa"
            )
        )
        assertTrue(id > 0)

        // 2. Query
        val expense = dao.getExpenseById(id)
        assertNotNull(expense)
        assertEquals(250.0, expense!!.amount, 0.001)
        assertEquals(ExpenseCategory.FOOD.title, expense.category)
        assertEquals("Chai and samosa", expense.note)

        // 3. Update
        val updated = expense.copy(amount = 300.0, note = "Chai, samosa and sweets")
        dao.updateExpense(updated)
        val refreshed = dao.getExpenseById(id)
        assertEquals(300.0, refreshed!!.amount, 0.001)
        assertEquals("Chai, samosa and sweets", refreshed.note)

        // 4. Delete
        dao.deleteExpense(refreshed)
        val all = dao.getAllExpensesList()
        assertTrue(all.isEmpty())
    }

    @Test
    fun `test currency formatting with INR symbol`() {
        val formatted1 = CurrencyUtils.formatInr(1500.0)
        assertTrue(formatted1.contains("₹"))
        assertTrue(formatted1.contains("1,500"))

        val formatted2 = CurrencyUtils.formatInr(250.50, alwaysShowDecimals = true)
        assertTrue(formatted2.contains("₹"))
        assertTrue(formatted2.contains("250.50"))

        val parsed = CurrencyUtils.parseAmount("₹ 1,250.00")
        assertEquals(1250.0, parsed ?: 0.0, 0.001)
    }
}
