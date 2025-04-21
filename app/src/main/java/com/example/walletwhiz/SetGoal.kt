package com.example.walletwhiz

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.walletwhiz.model.Goal
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class SetGoal : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.set_goal)

        val tvDate = findViewById<TextView>(R.id.tvDate)
        val btnPickDate = findViewById<ImageButton>(R.id.btnPickDate)
        val tvCategory = findViewById<TextView>(R.id.tvCategory)
        val btnPickCategory = findViewById<ImageButton>(R.id.btnPickCategory)
        val etAmount = findViewById<EditText>(R.id.etAmount)
        val etTitle = findViewById<EditText>(R.id.etTitle)
        val btnSetGoal = findViewById<Button>(R.id.btnSetGoal)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        // Date Picker
        btnPickDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                tvDate.text = dateFormat.format(calendar.time)
                tvDate.setTextColor(resources.getColor(R.color.primaryGreen, null))
            }
            DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        tvDate.setOnClickListener { btnPickDate.performClick() }

        // Category Dropdown
        val categories = arrayOf("Entertainment","Gift","Groceries","Medicine","Rent","Salary","Savings","Transport")
        btnPickCategory.setOnClickListener {
            val popup = PopupMenu(this, btnPickCategory)
            categories.forEach { popup.menu.add(it) }
            popup.setOnMenuItemClickListener { item ->
                tvCategory.text = item.title
                tvCategory.setTextColor(resources.getColor(R.color.primaryGreen, null))
                true
            }
            popup.show()
        }
        tvCategory.setOnClickListener { btnPickCategory.performClick() }

        // Back button
        btnBack.setOnClickListener {
            finish()
        }

        // Set Goal logic to add goal to GoalsActivity
        btnSetGoal.setOnClickListener {
            val date = tvDate.text.toString()
            val category = tvCategory.text.toString()
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val title = etTitle.text.toString()

            if (date.isBlank() || category.isBlank() || amount <= 0.0 || title.isBlank()) {
                Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newGoal = Goal(
                date = date,
                category = category,
                amount = amount,
                title = title
            )
            saveGoalToStorage(newGoal)
            Toast.makeText(this, "Goal added!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun saveGoalToStorage(newGoal: Goal) {
        val fileName = "goals.json"
        val goalsList = loadGoalsFromStorage().toMutableList()
        goalsList.add(newGoal)
        val json = Gson().toJson(goalsList)
        openFileOutput(fileName, Context.MODE_PRIVATE).use { it.write(json.toByteArray()) }
    }

    private fun loadGoalsFromStorage(): List<Goal> {
        val fileName = "goals.json"
        return try {
            val json = openFileInput(fileName).bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Goal>>() {}.type
            Gson().fromJson(json, type) ?: listOf()
        } catch (e: Exception) {
            listOf()
        }
    }
}
