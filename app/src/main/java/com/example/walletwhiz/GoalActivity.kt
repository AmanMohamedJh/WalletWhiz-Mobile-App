package com.example.walletwhiz

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.walletwhiz.Adaptors.GoalAdaptor
import com.example.walletwhiz.model.Goal
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*

class GoalActivity : AppCompatActivity() {
    private lateinit var rvGoals: RecyclerView
    private lateinit var tvNoGoals: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: GoalAdaptor
    private lateinit var goalsList: MutableList<Goal>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.goals)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        BottomNavHelper.setup(bottomNavigation, this)
        bottomNavigation.selectedItemId = R.id.menu_stats

        val btnAddGoal = findViewById<Button>(R.id.btnAddBudget)
        btnAddGoal.setOnClickListener {
            startActivity(Intent(this, SetGoal::class.java))
        }

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            startActivity(Intent(this, HomePageActivity::class.java))
            finish()
        }

        rvGoals = findViewById(R.id.rvBudgets)
        tvNoGoals = findViewById(R.id.tvNoBudgets)
        progressBar = findViewById(R.id.progressBarBudgets)
        rvGoals.setHasFixedSize(true)
        rvGoals.itemAnimator = null
        rvGoals.layoutManager = LinearLayoutManager(this)

        goalsList = mutableListOf()
        adapter = GoalAdaptor(goalsList) { goal ->
            deleteGoal(goal)
        }
        rvGoals.adapter = adapter

        rvGoals.visibility = View.GONE
        tvNoGoals.visibility = View.GONE

        CoroutineScope(Dispatchers.Main).launch {
            progressBar.visibility = View.VISIBLE
            val loadedGoals = withContext(Dispatchers.IO) {
                loadGoalsFromStorage()
            }
            goalsList.clear()
            goalsList.addAll(loadedGoals)
            adapter.notifyDataSetChanged()
            progressBar.visibility = View.GONE
            if (goalsList.isEmpty()) {
                rvGoals.visibility = View.GONE
                tvNoGoals.visibility = View.VISIBLE
            } else {
                rvGoals.visibility = View.VISIBLE
                tvNoGoals.visibility = View.GONE
            }
            Log.d("GoalActivity", "Loaded goals: ${goalsList.size}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("GoalActivity", "onDestroy called")
    }

    override fun onPause() {
        super.onPause()
        Log.d("GoalActivity", "onPause called")
    }

    override fun onStop() {
        super.onStop()
        Log.d("GoalActivity", "onStop called")
    }

    private fun loadGoalsFromStorage(): List<Goal> {
        val fileName = "goals.json"
        return try {
            val json = openFileInput(fileName).bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Goal>>() {}.type
            Gson().fromJson<List<Goal>>(json, type) ?: listOf()
        } catch (e: Exception) {
            Log.e("GoalActivity", "Error loading goals: ${e.message}", e)
            listOf()
        }
    }

    private fun deleteGoal(goal: Goal) {
        val index = goalsList.indexOfFirst { it.id == goal.id }
        if (index != -1) {
            adapter.removeAt(index)
            CoroutineScope(Dispatchers.IO).launch {
                saveGoalsToStorage(goalsList)
            }
            if (goalsList.isEmpty()) {
                rvGoals.visibility = View.GONE
                tvNoGoals.visibility = View.VISIBLE
            }
        }
    }

    private fun saveGoalsToStorage(goals: List<Goal>) {
        val fileName = "goals.json"
        try {
            val json = Gson().toJson(goals)
            openFileOutput(fileName, MODE_PRIVATE).use { it.write(json.toByteArray()) }
        } catch (e: Exception) {
            Log.e("GoalActivity", "Error saving goals", e)
        }
    }
}
