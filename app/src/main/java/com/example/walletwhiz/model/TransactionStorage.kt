package com.example.walletwhiz.model

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object TransactionStorage {
    private const val PREFS_NAME = "walletwhiz_transactions"
    private const val KEY_TRANSACTIONS = "transactions_list"

    fun saveTransactions(context: Context, transactions: List<Transaction>) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val json = Gson().toJson(transactions)
        editor.putString(KEY_TRANSACTIONS, json)
        editor.apply()
    }

    fun loadTransactions(context: Context): List<Transaction> {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_TRANSACTIONS, null)
        return if (json.isNullOrEmpty()) {
            emptyList()
        } else {
            val type = object : TypeToken<List<Transaction>>() {}.type
            Gson().fromJson(json, type)
        }
    }
}
