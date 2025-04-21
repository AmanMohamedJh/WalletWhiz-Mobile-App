package com.example.walletwhiz.model

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object BudgetStorage {
    private const val PREFS_NAME = "budgets_prefs"
    private const val BUDGETS_KEY = "budgets_list"
    private val gson = Gson()

    fun loadBudgets(context: Context): MutableList<Budget> {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(BUDGETS_KEY, null)
        return if (json.isNullOrEmpty()) mutableListOf() else gson.fromJson(json, object : TypeToken<MutableList<Budget>>(){}.type)
    }

    fun saveBudgets(context: Context, budgets: List<Budget>) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(BUDGETS_KEY, gson.toJson(budgets)).apply()
    }

    fun addOrUpdateBudget(context: Context, budget: Budget) {
        val budgets = loadBudgets(context)
        val idx = budgets.indexOfFirst { it.category == budget.category }
        if (idx >= 0) budgets[idx] = budget else budgets.add(budget)
        saveBudgets(context, budgets)
    }

    fun deleteBudget(context: Context, category: String) {
        val budgets = loadBudgets(context)
        budgets.removeAll { it.category == category }
        saveBudgets(context, budgets)
    }
}
