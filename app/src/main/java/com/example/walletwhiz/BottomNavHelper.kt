package com.example.walletwhiz

import android.app.Activity
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView

object BottomNavHelper {
    fun setup(bottomNavigation: BottomNavigationView, activity: Activity) {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    if (activity !is HomePageActivity) {
                        activity.startActivity(Intent(activity, HomePageActivity::class.java))
                        activity.finish()
                    }
                    true
                }
                R.id.menu_stats -> {
                    if (activity !is BudgetActivity) {
                        activity.startActivity(Intent(activity, BudgetActivity::class.java))
                        activity.finish()
                    }
                    true
                }
                R.id.menu_add -> {
                    activity.startActivity(Intent(activity, AddTransactionActivity::class.java))
                    true
                }
                R.id.menu_cards -> {
                    if (activity !is CategoriesActivity) {
                        activity.startActivity(Intent(activity, CategoriesActivity::class.java))
                        activity.finish()
                    }
                    true
                }
                R.id.menu_settings -> {
                    if (activity !is SettingsActivity) {
                        activity.startActivity(Intent(activity, SettingsActivity::class.java))
                        activity.finish()
                    }
                    true
                }
                else -> false
            }
        }
    }
}
