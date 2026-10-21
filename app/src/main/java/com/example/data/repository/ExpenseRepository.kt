package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.ExpenseDao
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseEntity
import com.example.data.model.PaymentMethod
import com.example.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val context: Context
) {
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    suspend fun addExpense(expense: ExpenseEntity): Long = withContext(Dispatchers.IO) {
        expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: ExpenseEntity) = withContext(Dispatchers.IO) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) = withContext(Dispatchers.IO) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun deleteExpenseById(id: Long) = withContext(Dispatchers.IO) {
        expenseDao.deleteById(id)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        expenseDao.deleteAllExpenses()
    }

    suspend fun exportCsv(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val expenses = expenseDao.getAllExpensesList()
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                    // Header
                    writer.write("ID,Amount (INR),Category,Date,Payment Method,Note,Date Millis,Created At\n")
                    for (e in expenses) {
                        val escapedNote = "\"" + e.note.replace("\"", "\"\"") + "\""
                        val dateFormatted = DateUtils.formatCsvDate(e.dateMillis)
                        val createdAtFormatted = DateUtils.formatCsvDate(e.createdAt)
                        writer.write("${e.id},${e.amount},\"${e.category}\",\"$dateFormatted\",\"${e.paymentMethod}\",$escapedNote,${e.dateMillis},\"$createdAtFormatted\"\n")
                    }
                    writer.flush()
                }
            } ?: error("Unable to open output stream")
            expenses.size
        }
    }

    suspend fun exportBackupJson(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val expenses = expenseDao.getAllExpensesList()
            val jsonArray = JSONArray()
            for (e in expenses) {
                val obj = JSONObject()
                obj.put("id", e.id)
                obj.put("amount", e.amount)
                obj.put("category", e.category)
                obj.put("dateMillis", e.dateMillis)
                obj.put("paymentMethod", e.paymentMethod)
                obj.put("note", e.note)
                obj.put("createdAt", e.createdAt)
                jsonArray.put(obj)
            }
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(jsonArray.toString(2))
                    writer.flush()
                }
            } ?: error("Unable to open output stream")
            expenses.size
        }
    }

    suspend fun importData(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            } ?: error("Unable to open input stream")

            val trimmed = content.trim()
            val importedList = mutableListOf<ExpenseEntity>()

            if (trimmed.startsWith("[") || trimmed.startsWith("{")) {
                // Parse JSON
                val array = if (trimmed.startsWith("[")) JSONArray(trimmed) else JSONArray().put(JSONObject(trimmed))
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val amount = obj.optDouble("amount", 0.0)
                    if (amount > 0) {
                        val category = obj.optString("category", ExpenseCategory.OTHER.title)
                        val dateMillis = obj.optLong("dateMillis", System.currentTimeMillis())
                        val paymentMethod = obj.optString("paymentMethod", PaymentMethod.UPI.title)
                        val note = obj.optString("note", "")
                        val createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        importedList.add(
                            ExpenseEntity(
                                id = 0, // Auto-generate new IDs on import
                                amount = amount,
                                category = category,
                                dateMillis = dateMillis,
                                paymentMethod = paymentMethod,
                                note = note,
                                createdAt = createdAt
                            )
                        )
                    }
                }
            } else {
                // Parse CSV
                val lines = trimmed.lines()
                for (index in lines.indices) {
                    if (index == 0) continue // Skip header
                    val line = lines[index].trim()
                    if (line.isEmpty()) continue
                    val parts = parseCsvLine(line)
                    if (parts.size >= 5) {
                        val amount = parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            val category = parts.getOrNull(2) ?: ExpenseCategory.OTHER.title
                            val dateStr = parts.getOrNull(3) ?: ""
                            val paymentMethod = parts.getOrNull(4) ?: PaymentMethod.UPI.title
                            val note = parts.getOrNull(5) ?: ""
                            val dateMillisFromCol = parts.getOrNull(6)?.toLongOrNull()
                            val dateMillis = dateMillisFromCol ?: (DateUtils.parseCsvDate(dateStr) ?: System.currentTimeMillis())

                            importedList.add(
                                ExpenseEntity(
                                    id = 0,
                                    amount = amount,
                                    category = category,
                                    dateMillis = dateMillis,
                                    paymentMethod = paymentMethod,
                                    note = note,
                                    createdAt = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                }
            }

            if (importedList.isNotEmpty()) {
                expenseDao.insertExpenses(importedList)
            }
            importedList.size
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var inQuotes = false
        val sb = StringBuilder()
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    sb.append('\"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString().trim())
                sb.clear()
            } else {
                sb.append(c)
            }
            i++
        }
        result.add(sb.toString().trim())
        return result
    }
}
