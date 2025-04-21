package com.example.walletwhiz

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.walletwhiz.Adaptors.EachCatogaryTransactionsAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView

class EachCategoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.each_catogary)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        BottomNavHelper.setup(bottomNavigation, this)

        // Back button to go to Categories page
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack?.setOnClickListener {
            startActivity(Intent(this, CategoriesActivity::class.java))
            finish()
        }

        // Show selected category name in EachCategoryActivity
        val categoryName = intent.getStringExtra("category_name")
        val tvCategoryTitle = findViewById<TextView?>(R.id.tvCategoryTitle)
        tvCategoryTitle?.text = categoryName ?: "Category"

        val rvCategoryTransactions = findViewById<RecyclerView>(R.id.rvCategoryTransactions)
        val tvNoTransactions = findViewById<TextView>(R.id.tvNoTransactions)

        // Use a vertical linear layout for category transactions (clean and elegant)
        rvCategoryTransactions.layoutManager = LinearLayoutManager(this)

        // Load and filter transactions by category (case-insensitive, trimmed)
        val transactions = com.example.walletwhiz.model.TransactionStorage.loadTransactions(this)
        val filtered = transactions.filter {
            it.category.trim().equals(categoryName?.trim(), ignoreCase = true)
        }

        val adapter = EachCatogaryTransactionsAdapter(filtered) { transaction ->
            // On click, open EditTransectionActivity with transaction id
            val intent = Intent(this, EditTransectionActivity::class.java)
            intent.putExtra("transaction_id", transaction.id)
            startActivity(intent)
        }
        rvCategoryTransactions.adapter = adapter
        rvCategoryTransactions.setHasFixedSize(true)

        if (filtered.isEmpty()) {
            rvCategoryTransactions.visibility = View.GONE
            tvNoTransactions.visibility = View.VISIBLE
        } else {
            rvCategoryTransactions.visibility = View.VISIBLE
            tvNoTransactions.visibility = View.GONE
        }
    }
}
