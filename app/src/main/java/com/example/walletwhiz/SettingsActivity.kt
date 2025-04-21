package com.example.walletwhiz

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.*
import android.view.View
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SettingsActivity : AppCompatActivity() {
    private val EXPORT_FILE_NAME = "walletwhiz_backup.json"
    private val IMPORT_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        val prefs = getSharedPreferences("settings_prefs", MODE_PRIVATE)
        val budgetAlertsEnabled = prefs.getBoolean("budget_alerts_enabled", true)
        val switchBudgetAlerts = findViewById<Switch>(R.id.switchBudgetAlerts)
        switchBudgetAlerts.isChecked = budgetAlertsEnabled

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        BottomNavHelper.setup(bottomNavigation, this)
        bottomNavigation.selectedItemId = R.id.menu_settings

        // Back button: redirect to HomePageActivity
        findViewById<android.widget.ImageButton>(R.id.btnBack).setOnClickListener {
            val intent = Intent(this, HomePageActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Export Data
        findViewById<Button>(R.id.btnExportData).setOnClickListener {
            exportData()
        }
        // Import Data
        findViewById<Button>(R.id.btnImportData).setOnClickListener {
            importData()
        }
        // Budget Alert Switch
        switchBudgetAlerts.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("budget_alerts_enabled", isChecked).apply()
            Toast.makeText(this, if (isChecked) "Budget alerts enabled" else "Budget alerts disabled", Toast.LENGTH_SHORT).show()
        }
        // Daily Reminder Switch
        findViewById<Switch>(R.id.switchDailyReminder).setOnCheckedChangeListener { _, isChecked ->
            // TODO: Enable/disable daily reminders
            Toast.makeText(this, if (isChecked) "Daily reminders enabled" else "Daily reminders disabled", Toast.LENGTH_SHORT).show()
        }
        // Notification Settings Row: open app notification settings
        findViewById<View>(R.id.rowNotificationSettings).setOnClickListener {
            val intent = Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, packageName)
                // For some devices/OS versions, the following may help:
                putExtra("app_package", packageName)
                putExtra("app_uid", applicationInfo.uid)
            }
            startActivity(intent)
        }
    }

    private fun exportData() {
        try {
            // Gather all data
            val transactions = com.example.walletwhiz.model.TransactionStorage.loadTransactions(this)
            val budgets = com.example.walletwhiz.model.BudgetStorage.loadBudgets(this)
            val gson = Gson()
            val backup = mapOf(
                "transactions" to transactions,
                "budgets" to budgets
            )
            val json = gson.toJson(backup)
            // Write to external files directory (user-accessible)
            val file = java.io.File(getExternalFilesDir(null), EXPORT_FILE_NAME)
            file.writeText(json)
            Toast.makeText(this, "Backup exported to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun importData() {
        // Open file picker for JSON
        val intent = android.content.Intent(android.content.Intent.ACTION_GET_CONTENT)
        intent.type = "application/json"
        startActivityForResult(android.content.Intent.createChooser(intent, "Select Backup File"), IMPORT_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMPORT_REQUEST_CODE && resultCode == android.app.Activity.RESULT_OK) {
            data?.data?.let { uri ->
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val importedJson = inputStream?.bufferedReader().use { it?.readText() } ?: ""
                    val gson = Gson()
                    val mapType = object : com.google.gson.reflect.TypeToken<Map<String, Any>>() {}.type
                    val backupMap: Map<String, Any> = gson.fromJson(importedJson, mapType)
                    // Restore transactions
                    val txJson = gson.toJson(backupMap["transactions"])
                    val txType = object : com.google.gson.reflect.TypeToken<List<com.example.walletwhiz.model.Transaction>>() {}.type
                    val transactions: List<com.example.walletwhiz.model.Transaction> = gson.fromJson(txJson, txType)
                    com.example.walletwhiz.model.TransactionStorage.saveTransactions(this, transactions)
                    // Restore budgets
                    val budJson = gson.toJson(backupMap["budgets"])
                    val budType = object : com.google.gson.reflect.TypeToken<List<com.example.walletwhiz.model.Budget>>() {}.type
                    val budgets: List<com.example.walletwhiz.model.Budget> = gson.fromJson(budJson, budType)
                    com.example.walletwhiz.model.BudgetStorage.saveBudgets(this, budgets)
                    Toast.makeText(this, "Data restored!", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Restore failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
