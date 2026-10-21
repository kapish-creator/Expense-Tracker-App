package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class ExpenseCategory(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val darkColor: Color
) {
    FOOD(
        title = "Food",
        icon = Icons.Rounded.Fastfood,
        color = Color(0xFFE65100),
        darkColor = Color(0xFFFF9800)
    ),
    TRAVEL(
        title = "Travel",
        icon = Icons.Rounded.DirectionsCar,
        color = Color(0xFF0277BD),
        darkColor = Color(0xFF29B6F6)
    ),
    SHOPPING(
        title = "Shopping",
        icon = Icons.Rounded.ShoppingBag,
        color = Color(0xFF8E24AA),
        darkColor = Color(0xFFBA68C8)
    ),
    EDUCATION(
        title = "Education",
        icon = Icons.Rounded.School,
        color = Color(0xFF1565C0),
        darkColor = Color(0xFF64B5F6)
    ),
    BILLS(
        title = "Bills",
        icon = Icons.Rounded.ReceiptLong,
        color = Color(0xFFC2185B),
        darkColor = Color(0xFFF06292)
    ),
    ENTERTAINMENT(
        title = "Entertainment",
        icon = Icons.Rounded.Movie,
        color = Color(0xFF5E35B1),
        darkColor = Color(0xFF9575CD)
    ),
    HEALTH(
        title = "Health",
        icon = Icons.Rounded.LocalHospital,
        color = Color(0xFFD32F2F),
        darkColor = Color(0xFFE57373)
    ),
    PERSONAL(
        title = "Personal",
        icon = Icons.Rounded.Person,
        color = Color(0xFF00796B),
        darkColor = Color(0xFF4DB6AC)
    ),
    OTHER(
        title = "Other",
        icon = Icons.Rounded.Category,
        color = Color(0xFF455A64),
        darkColor = Color(0xFF90A4AE)
    );

    companion object {
        fun fromTitle(title: String?): ExpenseCategory {
            return entries.firstOrNull { it.title.equals(title, ignoreCase = true) } ?: OTHER
        }
    }
}
