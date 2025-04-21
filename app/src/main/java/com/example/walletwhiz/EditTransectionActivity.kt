package com.example.walletwhiz

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class EditTransectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_transection)

        // Find views
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val tvIncome = findViewById<TextView>(R.id.tvIncome)
        val tvExpense = findViewById<TextView>(R.id.tvExpense)
        val editDate = findViewById<EditText>(R.id.editDate)
        val btnDatePicker = findViewById<ImageButton>(R.id.btnDatePicker)
        val tvCategory = findViewById<TextView>(R.id.tvCategory)
        val btnPickCategory = findViewById<ImageButton>(R.id.btnPickCategory)
        val spinnerCategory = findViewById<Spinner?>(R.id.spinnerCategory) // Defensive: in case not present
        val editAmount = findViewById<EditText>(R.id.editAmount)
        val editTitle = findViewById<EditText>(R.id.editTitle)
        val editNote = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editNote)
        val btnUpdate = findViewById<Button>(R.id.btnUpdate)
        val btnDelete = findViewById<Button>(R.id.btnDelete)
        val bottomNavigation = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)

        // --- Bottom Navigation Setup ---
        com.example.walletwhiz.BottomNavHelper.setup(bottomNavigation, this)
        // Optionally highlight none (since this is not a main tab)
        bottomNavigation.menu.setGroupCheckable(0, false, true)

        // --- Back Button ---
        btnBack.setOnClickListener {
            startActivity(Intent(this, HomePageActivity::class.java))
            finish()
        }

        // --- Income/Expense Toggle Logic ---
        var selectedType = "INCOME" // default or load from transaction if available
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

        // --- Date Picker (fix for EditText) ---
        fun showDatePicker(editDate: EditText) {
            val calendar = Calendar.getInstance()
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val selectedDate = dateFormat.format(calendar.time)
                editDate.setText(selectedDate)
                editDate.setTextColor(ContextCompat.getColor(this, R.color.primaryGreen))
            }
            DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        btnDatePicker.setOnClickListener { showDatePicker(editDate) }
        editDate.setOnClickListener { showDatePicker(editDate) }
        // Prevent keyboard on focus
        editDate.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                showDatePicker(editDate)
            }
        }
        editDate.inputType = 0 // TYPE_NULL disables keyboard

        // Category Spinner (consistent categories)
        val categories = arrayOf(
            "Entertainment", "Salary", "Rent", "Transport", "Savings", "Medicines", "Groceries", "Gift"
        )
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spinnerCategory.adapter = spinnerAdapter

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // The spinner will automatically show the selected value.
                // If you have a custom TextView (tvCategory), update it as well:
                tvCategory.text = categories[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // --- Load Transaction by ID ---
        val transactionId = intent.getStringExtra("transaction_id")
        val allTransactions = com.example.walletwhiz.model.TransactionStorage.loadTransactions(this).toMutableList()
        val transaction = allTransactions.find { it.id == transactionId }
        if (transaction == null) {
            Toast.makeText(this, "Transaction not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Populate fields
        selectedType = transaction.type
        updateToggleVisuals()
        editDate.setText(transaction.date)
        editTitle.setText(transaction.title)
        editAmount.setText(transaction.amount.toString())
        editNote.setText(transaction.message ?: "")
        // Set spinner selection (case-insensitive, robust)
        val catIndex = categories.indexOfFirst { it.equals(transaction.category, ignoreCase = true) }
        if (catIndex >= 0) spinnerCategory.setSelection(catIndex)

        // --- UI fix: clicking text or icon opens spinner ---
        tvCategory.setOnClickListener { spinnerCategory.performClick() }
        btnPickCategory.setOnClickListener { spinnerCategory.performClick() }

        // --- Update Button ---
        btnUpdate.setOnClickListener {
            val newType = selectedType
            val newDate = editDate.text.toString().trim()
            val newCategory = spinnerCategory.selectedItem.toString()
            val newAmount = editAmount.text.toString().toDoubleOrNull() ?: 0.0
            val newTitle = editTitle.text.toString().trim()
            val newNote = editNote.text.toString().trim()

            if (newTitle.isEmpty() || newAmount == 0.0 || newDate.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Update transaction in list
            val idx = allTransactions.indexOfFirst { it.id == transactionId }
            if (idx >= 0) {
                allTransactions[idx] = transaction.copy(
                    type = newType,
                    date = newDate,
                    category = newCategory,
                    amount = newAmount,
                    title = newTitle,
                    message = newNote
                )
                com.example.walletwhiz.model.TransactionStorage.saveTransactions(this, allTransactions)
                Toast.makeText(this, "Transaction updated!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
        }

        // --- Delete Button ---
        btnDelete.setOnClickListener {
            val idx = allTransactions.indexOfFirst { it.id == transactionId }
            if (idx >= 0) {
                allTransactions.removeAt(idx)
                com.example.walletwhiz.model.TransactionStorage.saveTransactions(this, allTransactions)
                Toast.makeText(this, "Transaction deleted!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
        }
    }
}
