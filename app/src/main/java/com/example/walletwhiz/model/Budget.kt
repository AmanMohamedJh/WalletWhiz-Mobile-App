package com.example.walletwhiz.model

data class Budget(
    val category: String,
    var limit: Double,
    var spent: Double = 0.0,
    var date: String = ""
)
