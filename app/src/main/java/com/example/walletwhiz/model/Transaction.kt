package com.example.walletwhiz.model

import java.util.UUID

/**
 * Data class representing a Transaction (Income or Expense).
 */
data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val type: String, // "INCOME" or "EXPENSE"
    val date: String,
    val category: String,
    val amount: Double,
    val title: String,
    val message: String? = null
)
