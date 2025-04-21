package com.example.walletwhiz

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.walletwhiz.Adaptors.BudgetAdapter
import com.example.walletwhiz.Adaptors.NotBudgetedAdapter
import com.example.walletwhiz.model.Budget
import com.example.walletwhiz.model.BudgetStorage
import com.example.walletwhiz.model.TransactionStorage
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.widget.EditText
import android.widget.TextView
import android.view.View
import com.example.walletwhiz.R

class BudgetActivity : AppCompatActivity() {
    private val allCategories = listOf(
        "Food", "Transport", "Medicine", "Groceries", "Rent", "Gift", "Savings", "Entertainment", "Salary"
    )

    private lateinit var rvBudgeted: RecyclerView
    private lateinit var rvNotBudgeted: RecyclerView
    private lateinit var budgetedAdapter: BudgetAdapter
    private lateinit var notBudgetedAdapter: NotBudgetedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.budget_page)

        // Setup Top AppBar back button
        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.topAppBar).setNavigationOnClickListener {
            // Always go to HomePageActivity
            startActivity(android.content.Intent(this, HomePageActivity::class.java))
            finish()
        }

        // Setup bottom navigation
        val bottomNavigation = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)
        BottomNavHelper.setup(bottomNavigation, this)
        bottomNavigation.selectedItemId = R.id.menu_stats

        // Budgeted
        rvBudgeted = RecyclerView(this)
        rvBudgeted.layoutManager = LinearLayoutManager(this)
        findViewById<android.widget.LinearLayout>(R.id.layoutBudgetedList).addView(rvBudgeted)

        // Not Budgeted
        rvNotBudgeted = RecyclerView(this)
        rvNotBudgeted.layoutManager = LinearLayoutManager(this)
        findViewById<android.widget.LinearLayout>(R.id.layoutNotBudgetedList).addView(rvNotBudgeted)

        refreshBudgets()
    }

    // Helper to get category icon resource
    private fun getCategoryIconRes(category: String): Int {
        return when (category) {
            "Food" -> R.drawable.ic_food
            "Transport" -> R.drawable.ic_transport
            "Medicine" -> R.drawable.ic_medicine
            "Groceries" -> R.drawable.ic_groceries
            "Rent" -> R.drawable.ic_rent
            "Gift" -> R.drawable.ic_gift
            "Savings" -> R.drawable.ic_savings
            "Entertainment" -> R.drawable.ic_entertainment
            "Salary" -> R.drawable.ic_salary
            else -> R.drawable.ic_misc
        }
    }

    private fun refreshBudgets() {
        val budgetsRaw = BudgetStorage.loadBudgets(this)
        val transactions = TransactionStorage.loadTransactions(this)
        // Always recalculate 'spent' for each budgeted category
        val budgets = budgetsRaw.map { budget ->
            val spent = transactions.filter { it.category == budget.category && it.type == "EXPENSE" }.sumOf { it.amount }
            budget.copy(spent = spent)
        }
        val budgetedCategories = budgets.map { it.category }
        val notBudgetedCategories = allCategories.filter { !budgetedCategories.contains(it) }

        // Show popup warning if any category is near or over budget
        val prefs = getSharedPreferences("settings_prefs", MODE_PRIVATE)
        val budgetAlertsEnabled = prefs.getBoolean("budget_alerts_enabled", true)
        if (budgetAlertsEnabled) {
            val overBudget = budgets.filter { it.spent >= it.limit }
            val nearBudget = budgets.filter { it.spent in (0.8 * it.limit)..(it.limit - 0.01) }
            if (overBudget.isNotEmpty()) {
                Toast.makeText(this, "Warning: You have exceeded your budget for: " + overBudget.joinToString { it.category }, Toast.LENGTH_LONG).show()
            } else if (nearBudget.isNotEmpty()) {
                Toast.makeText(this, "Warning: You are nearing your budget limit for: " + nearBudget.joinToString { it.category }, Toast.LENGTH_LONG).show()
            }
        }

        budgetedAdapter = BudgetAdapter(budgets,
            onDelete = { budget ->
                BudgetStorage.deleteBudget(this, budget.category)
                refreshBudgets()
                Toast.makeText(this, "Budget deleted", Toast.LENGTH_SHORT).show()
            },
            onEdit = { budget ->
                showSetBudgetDialog(budget)
            },
            getIconRes = { category -> getCategoryIconRes(category) }
        )
        rvBudgeted.adapter = budgetedAdapter

        notBudgetedAdapter = NotBudgetedAdapter(notBudgetedCategories, { category ->
            showSetBudgetDialog(Budget(category, 0.0))
        }, getIconRes = { category -> getCategoryIconRes(category) })
        rvNotBudgeted.adapter = notBudgetedAdapter

        findViewById<TextView>(R.id.tvNoBudgets).visibility = if (budgets.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showSetBudgetDialog(budget: Budget) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_set_budget, null)
        val etLimit = dialogView.findViewById<EditText>(R.id.etLimit)
        val tvCategoryName = dialogView.findViewById<TextView>(R.id.tvCategoryName)
        tvCategoryName.text = budget.category
        etLimit.setText(if (budget.limit > 0) budget.limit.toString() else "")
        val tvMonthInfo = dialogView.findViewById<TextView>(R.id.tvMonthInfo)
        tvMonthInfo.text = "Month: " + java.text.SimpleDateFormat("MMMM, yyyy").format(java.util.Date())

        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(true)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Set") { _, _ ->
                val limit = etLimit.text.toString().toDoubleOrNull()
                if (limit == null || limit <= 0) {
                    Toast.makeText(this, "Please enter a valid limit", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val spent = TransactionStorage.loadTransactions(this)
                    .filter { it.category == budget.category && it.type == "EXPENSE" }
                    .sumOf { it.amount }
                val updatedBudget = budget.copy(limit = limit, spent = spent,
                    date = java.text.SimpleDateFormat("MMM, yyyy").format(java.util.Date()))
                BudgetStorage.addOrUpdateBudget(this, updatedBudget)
                refreshBudgets()
                Toast.makeText(this, "Budget set!", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
}
