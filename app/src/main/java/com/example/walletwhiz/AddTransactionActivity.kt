package com.example.walletwhiz

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.walletwhiz.model.Transaction
import com.example.walletwhiz.model.TransactionStorage
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {
    private var selectedType = "INCOME" // or "EXPENSE"
    private var selectedDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_transaction)

        // Setup Bottom Navigation
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        BottomNavHelper.setup(bottomNavigation, this)

        // Setup Toggle
        setToggle(selectedType)
        val tvIncome = findViewById<TextView>(R.id.tvIncome)
        val tvExpense = findViewById<TextView>(R.id.tvExpense)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnDatePicker = findViewById<ImageView>(R.id.btnDatePicker)
        val tvDate = findViewById<TextView>(R.id.tvDate)
        val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)
        val editTitle = findViewById<EditText>(R.id.editTitle)
        val editAmount = findViewById<EditText>(R.id.editAmount)
        val editNote = findViewById<EditText>(R.id.editNote)

        // Back button to go to HomePage
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack?.setOnClickListener {
            startActivity(Intent(this, HomePageActivity::class.java))
            finish()
        }

        // Toggle visual feedback
        fun updateToggleVisuals() {
            val incomeSelected = selectedType == "INCOME"
            tvIncome.setBackgroundResource(if (incomeSelected) R.drawable.toggle_text_selected else R.drawable.toggle_text_outlined)
            tvExpense.setBackgroundResource(if (!incomeSelected) R.drawable.toggle_text_selected else R.drawable.toggle_text_outlined)
            tvIncome.setTextColor(ContextCompat.getColor(this, if (incomeSelected) R.color.incomeBlue else R.color.inactiveGray))
            tvExpense.setTextColor(ContextCompat.getColor(this, if (!incomeSelected) R.color.expenseRed else R.color.inactiveGray))
        }
        updateToggleVisuals()

        tvIncome.setOnClickListener {
            selectedType = "INCOME"
            updateToggleVisuals()
        }
        tvExpense.setOnClickListener {
            selectedType = "EXPENSE"
            updateToggleVisuals()
        }

        btnDatePicker.setOnClickListener {
            showDatePicker(tvDate)
        }
        tvDate.setOnClickListener {
            showDatePicker(tvDate)
        }

        // Category Spinner
        val categories = arrayOf(
            "Food", "Entertainment", "Gift", "Groceries", "Medicine", "Rent", "Salary", "Savings", "Transport"
        )
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spinnerCategory.adapter = spinnerAdapter

        // Save Button
        btnSave.setOnClickListener {
            val type = selectedType
            val title = editTitle.text.toString().trim()
            val amount = editAmount.text.toString().toDoubleOrNull() ?: 0.0
            val category = spinnerCategory.selectedItem.toString()
            val date = tvDate.text.toString().trim()
            val note = editNote.text.toString().trim()

            // Input validation
            if (title.isEmpty() || amount == 0.0 || date.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create transaction object
            val transaction = Transaction(
                type = type,
                date = date,
                category = category,
                amount = amount,
                title = title,
                message = note
            )

            // Load, append, and save
            val existing = TransactionStorage.loadTransactions(this).toMutableList()
            existing.add(0, transaction) // Add to top
            TransactionStorage.saveTransactions(this, existing)

            Toast.makeText(this, "Transaction added!", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun setToggle(type: String) {
        val tvIncome = findViewById<TextView>(R.id.tvIncome)
        val tvExpense = findViewById<TextView>(R.id.tvExpense)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val incomeSelected = type == "INCOME"
        tvIncome.isSelected = incomeSelected
        tvExpense.isSelected = !incomeSelected
        btnSave.isSelected = !incomeSelected // Save button: green for income, red for expense
        btnSave.text = "Save Transaction"
        btnSave.setTextColor(ContextCompat.getColor(this, android.R.color.white))
    }

    private fun showDatePicker(tvDate: TextView) {
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            selectedDate = dateFormat.format(calendar.time)
            tvDate.text = selectedDate
            tvDate.setTextColor(ContextCompat.getColor(this, R.color.primaryGreen))
        }
        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}
