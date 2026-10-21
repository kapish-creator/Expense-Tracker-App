package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val fullDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    private val shortDateFormat = SimpleDateFormat("dd MMM", Locale.ENGLISH)
    private val monthYearFormat = SimpleDateFormat("MMM yyyy", Locale.ENGLISH)
    private val monthAbbrFormat = SimpleDateFormat("MMM", Locale.ENGLISH)
    private val csvDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)

    fun formatDate(millis: Long): String {
        val calendar = Calendar.getInstance()
        val todayYear = calendar.get(Calendar.YEAR)
        val todayDay = calendar.get(Calendar.DAY_OF_YEAR)

        calendar.timeInMillis = millis
        val expenseYear = calendar.get(Calendar.YEAR)
        val expenseDay = calendar.get(Calendar.DAY_OF_YEAR)

        return when {
            todayYear == expenseYear && todayDay == expenseDay -> "Today"
            todayYear == expenseYear && todayDay - expenseDay == 1 -> "Yesterday"
            todayYear == expenseYear -> shortDateFormat.format(Date(millis))
            else -> fullDateFormat.format(Date(millis))
        }
    }

    fun formatFullDate(millis: Long): String {
        return fullDateFormat.format(Date(millis))
    }

    fun formatMonthYear(millis: Long): String {
        return monthYearFormat.format(Date(millis))
    }

    fun formatMonthAbbr(millis: Long): String {
        return monthAbbrFormat.format(Date(millis))
    }

    fun formatCsvDate(millis: Long): String {
        return csvDateFormat.format(Date(millis))
    }

    fun parseCsvDate(str: String): Long? {
        return try {
            csvDateFormat.parse(str)?.time
        } catch (_: Exception) {
            str.toLongOrNull()
        }
    }

    fun getStartOfDay(calendar: Calendar = Calendar.getInstance()): Long {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getEndOfDay(calendar: Calendar = Calendar.getInstance()): Long {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun getStartOfMonth(calendar: Calendar = Calendar.getInstance()): Long {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return getStartOfDay(cal)
    }

    fun getEndOfMonth(calendar: Calendar = Calendar.getInstance()): Long {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        return getEndOfDay(cal)
    }

    fun getStartOfWeek(calendar: Calendar = Calendar.getInstance()): Long {
        val cal = calendar.clone() as Calendar
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        return getStartOfDay(cal)
    }

    fun getEndOfWeek(calendar: Calendar = Calendar.getInstance()): Long {
        val cal = calendar.clone() as Calendar
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        return getEndOfDay(cal)
    }

    fun getLastMonthStartAndEnd(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -1)
        return Pair(getStartOfMonth(cal), getEndOfMonth(cal))
    }
}
