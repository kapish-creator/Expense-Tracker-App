package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class PaymentMethod(
    val title: String,
    val icon: ImageVector,
    val color: Color
) {
    CASH(
        title = "Cash",
        icon = Icons.Rounded.Payments,
        color = Color(0xFF2E7D32)
    ),
    UPI(
        title = "UPI",
        icon = Icons.Rounded.QrCodeScanner,
        color = Color(0xFF0288D1)
    ),
    CARD(
        title = "Debit/Credit Card",
        icon = Icons.Rounded.CreditCard,
        color = Color(0xFF6A1B9A)
    ),
    BANK_TRANSFER(
        title = "Bank Transfer",
        icon = Icons.Rounded.AccountBalance,
        color = Color(0xFFD84315)
    );

    companion object {
        fun fromTitle(title: String?): PaymentMethod {
            return entries.firstOrNull { it.title.equals(title, ignoreCase = true) } ?: UPI
        }
    }
}
