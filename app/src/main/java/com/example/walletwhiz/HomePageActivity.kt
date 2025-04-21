package com.example.walletwhiz

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.walletwhiz.Adaptors.TransactionsAdapter
import com.example.walletwhiz.model.Transaction
import com.example.walletwhiz.model.TransactionStorage
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.Locale

class HomePageActivity : AppCompatActivity() {
    private lateinit var transactionsAdapter: TransactionsAdapter
    private lateinit var rvTransactions: RecyclerView
    private lateinit var tvTotalBalance: TextView
    private lateinit var tvIncome: TextView
    private lateinit var tvExpense: TextView
    private lateinit var lineChartTrend: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        rvTransactions = findViewById(R.id.rvTransactions)
        rvTransactions.setHasFixedSize(true)
        rvTransactions.itemAnimator = null
        rvTransactions.layoutManager = LinearLayoutManager(this)

        tvTotalBalance = findViewById(R.id.tvTotalBalance)
        tvIncome = findViewById(R.id.tvIncome)
        tvExpense = findViewById(R.id.tvExpense)

        lineChartTrend = findViewById(R.id.lineChartTrend)
        setupTrendChart()

        // Initial setup
        updateBalanceCards()
        loadAndShowTransactions()

        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)
        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.menu_home
        BottomNavHelper.setup(bottomNavigation, this)

        val btnSetGoal = findViewById<TextView>(R.id.btnSetGoal)
        btnSetGoal.setOnClickListener {
            startActivity(Intent(this, GoalActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        updateBalanceCards()
        loadAndShowTransactions()
        setupTrendChart()
    }

    private fun loadAndShowTransactions() {
        val transactions = TransactionStorage.loadTransactions(this)
        transactionsAdapter = TransactionsAdapter(transactions) { transaction ->
            // Pass transaction ID to EditTransectionActivity
            val intent = Intent(this, EditTransectionActivity::class.java)
            intent.putExtra("transaction_id", transaction.id)
            startActivity(intent)
        }
        rvTransactions.adapter = transactionsAdapter
    }

    private fun updateBalanceCards() {
        val transactions = TransactionStorage.loadTransactions(this)
        var totalIncome = 0.0
        var totalExpense = 0.0
        for (t in transactions) {
            if (t.type == "INCOME") {
                totalIncome += t.amount
            } else if (t.type == "EXPENSE") {
                totalExpense += t.amount
            }
        }
        val balance = totalIncome - totalExpense
        tvTotalBalance.text = "Rs" + String.format("%,.2f", balance)
        tvIncome.text = "Rs" + String.format("%,.2f", totalIncome)
        tvExpense.text = "Rs" + String.format("%,.2f", totalExpense)
    }

    private fun setupTrendChart() {
        val transactions = TransactionStorage.loadTransactions(this)
        // We'll plot by order of grouped entries, not by date label
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val grouped = transactions
            .filter { it.date != "Select Date" && runCatching { dateFormat.parse(it.date) }.isSuccess }
            .groupBy { it.date }
        val sortedGroups = grouped.keys.sortedBy { dateFormat.parse(it) }
        val incomeEntries = mutableListOf<Entry>()
        val expenseEntries = mutableListOf<Entry>()
        var idx = 0f
        for (date in sortedGroups) {
            val daily = grouped[date] ?: continue
            val income = daily.filter { it.type == "INCOME" }.sumOf { it.amount }
            val expense = daily.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            incomeEntries.add(Entry(idx, income.toFloat()))
            expenseEntries.add(Entry(idx, expense.toFloat()))
            idx += 1f
        }
        val incomeSet = LineDataSet(incomeEntries, "Income").apply {
            color = ColorTemplate.MATERIAL_COLORS[0]
            setCircleColor(ColorTemplate.MATERIAL_COLORS[0])
            lineWidth = 2f
            circleRadius = 4f
            valueTextSize = 10f
            setDrawFilled(true)
            fillColor = ColorTemplate.MATERIAL_COLORS[0]
        }
        val expenseSet = LineDataSet(expenseEntries, "Expense").apply {
            color = ColorTemplate.COLORFUL_COLORS[1]
            setCircleColor(ColorTemplate.COLORFUL_COLORS[1])
            lineWidth = 2f
            circleRadius = 4f
            valueTextSize = 10f
            setDrawFilled(true)
            fillColor = ColorTemplate.COLORFUL_COLORS[1]
        }
        val lineData = LineData(incomeSet, expenseSet)
        lineChartTrend.data = lineData
        // X-axis: just show generic numbers (1, 2, 3, ...), or hide labels
        lineChartTrend.xAxis.valueFormatter = null
        lineChartTrend.xAxis.setDrawLabels(false)
        lineChartTrend.xAxis.granularity = 1f
        lineChartTrend.xAxis.setDrawGridLines(false)
        lineChartTrend.axisLeft.axisMinimum = 0f
        lineChartTrend.axisRight.isEnabled = false
        lineChartTrend.description = Description().apply { text = "" }
        lineChartTrend.legend.isEnabled = true
        lineChartTrend.invalidate()
    }
}
