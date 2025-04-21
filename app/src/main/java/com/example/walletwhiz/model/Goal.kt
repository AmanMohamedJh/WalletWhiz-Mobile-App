package com.example.walletwhiz.model

import java.util.UUID

data class Goal(
    val id: String = UUID.randomUUID().toString(),
    val date: String,
    val category: String,
    val amount: Double,
    val title: String
)
