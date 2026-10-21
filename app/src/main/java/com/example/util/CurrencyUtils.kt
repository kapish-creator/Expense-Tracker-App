package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyUtils {
    private val symbols = DecimalFormatSymbols(Locale("en", "IN")).apply {
        currencySymbol = "₹"
    }

    private val inrFormatWithDecimals = DecimalFormat("₹#,##,##0.00", symbols)
    private val inrFormatNoDecimals = DecimalFormat("₹#,##,##0", symbols)

    fun formatInr(amount: Double, alwaysShowDecimals: Boolean = false): String {
        return if (alwaysShowDecimals || (amount % 1.0 != 0.0)) {
            inrFormatWithDecimals.format(amount)
        } else {
            inrFormatNoDecimals.format(amount)
        }
    }

    fun parseAmount(input: String): Double? {
        val clean = input.replace("₹", "")
            .replace(",", "")
            .trim()
        return clean.toDoubleOrNull()?.takeIf { it > 0.0 }
    }
}
