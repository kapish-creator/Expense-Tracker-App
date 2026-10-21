package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val category: String,
    val dateMillis: Long,
    val paymentMethod: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
