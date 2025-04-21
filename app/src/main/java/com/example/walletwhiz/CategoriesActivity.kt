package com.example.walletwhiz

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.components.Description

class CategoriesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.catogaries)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        BottomNavHelper.setup(bottomNavigation, this)
        bottomNavigation.selectedItemId = R.id.menu_cards

        // Category click listeners
        val categoryFood = findViewById<LinearLayout>(R.id.categoryFood)
        val categoryTransport = findViewById<LinearLayout>(R.id.categoryTransport)
        val categoryMedicine = findViewById<LinearLayout>(R.id.categoryMedicine)
        val categoryGroceries = findViewById<LinearLayout>(R.id.categoryGroceries)
        val categoryRent = findViewById<LinearLayout>(R.id.categoryRent)
        val categoryGift = findViewById<LinearLayout>(R.id.categoryGift)
        val categorySavings = findViewById<LinearLayout>(R.id.categorySavings)
        val categoryEntertainment = findViewById<LinearLayout>(R.id.categoryEntertainment)
        val categorySalary = findViewById<LinearLayout>(R.id.categorySalary)

        val categoryMap = mapOf(
            categoryFood to "Food",
            categoryTransport to "Transport",
            categoryMedicine to "Medicine",
            categoryGroceries to "Groceries",
            categoryRent to "Rent",
            categoryGift to "Gift",
            categorySavings to "Savings",
            categoryEntertainment to "Entertainment",
            categorySalary to "Salary"
        )

        for ((view, name) in categoryMap) {
            view.setOnClickListener {
                val intent = Intent(this, EachCategoryActivity::class.java)
                intent.putExtra("category_name", name)
                startActivity(intent)
            }
        }

        // --- Category Summary Logic ---
        val transactions = com.example.walletwhiz.model.TransactionStorage.loadTransactions(this)
        val categories = listOf(
            "Food", "Transport", "Medicine", "Groceries", "Rent", "Gift", "Savings", "Entertainment", "Salary"
        )

        // Prepare data for charts
        val incomeMap = mutableMapOf<String, Double>()
        val expenseMap = mutableMapOf<String, Double>()
        for (cat in categories) {
            val income = transactions.filter { it.category.equals(cat, true) && it.type == "INCOME" }.sumOf { it.amount }
            val expense = transactions.filter { it.category.equals(cat, true) && it.type == "EXPENSE" }.sumOf { it.amount }
            incomeMap[cat] = income
            expenseMap[cat] = expense
        }

        // Setup chart views
        val pieChartIncome = findViewById<com.github.mikephil.charting.charts.PieChart>(R.id.pieChartIncome)
        val pieChartExpense = findViewById<com.github.mikephil.charting.charts.PieChart>(R.id.pieChartExpense)
        val barChartCategory = findViewById<com.github.mikephil.charting.charts.BarChart>(R.id.barChartCategory)

        // Pie Chart for Income
        val incomeEntries = incomeMap.map { com.github.mikephil.charting.data.PieEntry(it.value.toFloat(), it.key) }
        val incomeDataSet = com.github.mikephil.charting.data.PieDataSet(incomeEntries, "Income by Category")
        incomeDataSet.colors = com.github.mikephil.charting.utils.ColorTemplate.MATERIAL_COLORS.toList()
        incomeDataSet.valueFormatter = com.github.mikephil.charting.formatter.PercentFormatter(pieChartIncome)
        incomeDataSet.valueTextSize = 14f
        val incomeData = com.github.mikephil.charting.data.PieData(incomeDataSet)
        pieChartIncome.data = incomeData
        pieChartIncome.setUsePercentValues(true)
        pieChartIncome.setEntryLabelTextSize(12f)
        pieChartIncome.setDrawEntryLabels(true)
        pieChartIncome.centerText = "Income"
        pieChartIncome.description = com.github.mikephil.charting.components.Description().apply { text = "" }
        pieChartIncome.invalidate()

        // Pie Chart for Expenses
        val expenseEntries = expenseMap.map { com.github.mikephil.charting.data.PieEntry(it.value.toFloat(), it.key) }
        val expenseDataSet = com.github.mikephil.charting.data.PieDataSet(expenseEntries, "Expense by Category")
        expenseDataSet.colors = com.github.mikephil.charting.utils.ColorTemplate.COLORFUL_COLORS.toList()
        expenseDataSet.valueFormatter = com.github.mikephil.charting.formatter.PercentFormatter(pieChartExpense)
        expenseDataSet.valueTextSize = 14f
        val expenseData = com.github.mikephil.charting.data.PieData(expenseDataSet)
        pieChartExpense.data = expenseData
        pieChartExpense.setUsePercentValues(true)
        pieChartExpense.setEntryLabelTextSize(12f)
        pieChartExpense.setDrawEntryLabels(true)
        pieChartExpense.centerText = "Expense"
        pieChartExpense.description = com.github.mikephil.charting.components.Description().apply { text = "" }
        pieChartExpense.invalidate()

        // Bar Chart for Income vs Expense by Category
        val barIncomeEntries = categories.mapIndexed { idx, cat -> com.github.mikephil.charting.data.BarEntry(idx.toFloat(), incomeMap[cat]?.toFloat() ?: 0f) }
        val barExpenseEntries = categories.mapIndexed { idx, cat -> com.github.mikephil.charting.data.BarEntry(idx.toFloat(), expenseMap[cat]?.toFloat() ?: 0f) }
        val barIncomeSet = com.github.mikephil.charting.data.BarDataSet(barIncomeEntries, "Income")
        barIncomeSet.color = com.github.mikephil.charting.utils.ColorTemplate.MATERIAL_COLORS[0]
        val barExpenseSet = com.github.mikephil.charting.data.BarDataSet(barExpenseEntries, "Expense")
        barExpenseSet.color = com.github.mikephil.charting.utils.ColorTemplate.COLORFUL_COLORS[1]
        val barData = com.github.mikephil.charting.data.BarData(barIncomeSet, barExpenseSet)
        barData.barWidth = 0.4f
        barChartCategory.data = barData
        barChartCategory.xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(categories)
        barChartCategory.xAxis.granularity = 1f
        barChartCategory.xAxis.setDrawGridLines(false)
        barChartCategory.axisLeft.axisMinimum = 0f
        barChartCategory.axisRight.isEnabled = false
        barChartCategory.description = com.github.mikephil.charting.components.Description().apply { text = "" }
        barChartCategory.setFitBars(true)
        barChartCategory.invalidate()

        // Back button: redirect to HomePageActivity
        findViewById<android.widget.ImageButton>(R.id.btnBack).setOnClickListener {
            val intent = Intent(this, HomePageActivity::class.java)
            startActivity(intent)
            finish()
        }

        // FAB button: redirect to AddTransactionActivity
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)
        fabAdd?.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }
    }
}
