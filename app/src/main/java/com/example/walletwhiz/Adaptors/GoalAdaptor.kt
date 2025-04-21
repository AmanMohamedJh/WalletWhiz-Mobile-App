package com.example.walletwhiz.Adaptors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.walletwhiz.R
import com.example.walletwhiz.model.Goal
import android.util.Log
import android.widget.ImageView
import java.util.Locale

class GoalAdaptor(
    private val goals: MutableList<Goal>,
    private val onDelete: (Goal) -> Unit
) : RecyclerView.Adapter<GoalAdaptor.GoalViewHolder>() {

    class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvGoalTitle: TextView = itemView.findViewById(R.id.tvBudgetTitle)
        val tvGoalAmount: TextView = itemView.findViewById(R.id.tvBudgetAmount)
        val tvGoalCategory: TextView = itemView.findViewById(R.id.tvBudgetCategory)
        val tvGoalDate: TextView = itemView.findViewById(R.id.tvBudgetDate)
        val ivDeleteGoal: ImageView = itemView.findViewById(R.id.ivDeleteBudget)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_goal, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        try {
            if (position < 0 || position >= goals.size) {
                Log.e("GoalAdaptor", "Invalid position $position for goals size ${goals.size}")
                return
            }
            val goal = goals[position]
            Log.d("GoalAdaptor", "Binding position $position: id=${goal.id}, title=${goal.title}, amount=${goal.amount}, category=${goal.category}, date=${goal.date}")
            holder.tvGoalTitle.text = goal.title ?: ""
            holder.tvGoalAmount.text = String.format(Locale.getDefault(), "Rs%,.2f", goal.amount)
            holder.tvGoalCategory.text = goal.category ?: ""
            holder.tvGoalDate.text = goal.date ?: ""
            holder.ivDeleteGoal.setOnClickListener {
                val pos = holder.adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onDelete(goal)
                }
            }
        } catch (e: Exception) {
            Log.e("GoalAdaptor", "Error binding goal at position $position: ${e.message}", e)
        }
    }

    override fun getItemCount(): Int = goals.size

    fun removeAt(position: Int) {
        if (position >= 0 && position < goals.size) {
            goals.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
