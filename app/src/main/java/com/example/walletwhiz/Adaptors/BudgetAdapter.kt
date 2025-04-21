package com.example.walletwhiz.Adaptors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.walletwhiz.R
import com.example.walletwhiz.model.Budget
import java.util.Locale

class BudgetAdapter(
    private val budgets: List<Budget>,
    private val onDelete: (Budget) -> Unit,
    private val onEdit: (Budget) -> Unit,
    private val getIconRes: (String) -> Int
) : RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgCategoryIcon: ImageView = itemView.findViewById(R.id.imgCategoryIcon)
        val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        val tvBudgetDate: TextView = itemView.findViewById(R.id.tvBudgetDate)
        val tvBudgetLimit: TextView = itemView.findViewById(R.id.tvBudgetLimit)
        val tvBudgetSpent: TextView = itemView.findViewById(R.id.tvBudgetSpent)
        val tvBudgetRemaining: TextView = itemView.findViewById(R.id.tvBudgetRemaining)
        val progressBudget: ProgressBar = itemView.findViewById(R.id.progressBudget)
        val btnDeleteBudget: ImageView = itemView.findViewById(R.id.btnDeleteBudget)
        val warningIcon: ImageView = itemView.findViewById(R.id.imgWarning)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_budgeted, parent, false)
        return BudgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        val budget = budgets[position]
        holder.tvCategoryName.text = budget.category
        holder.tvBudgetDate.text = budget.date
        holder.tvBudgetLimit.text = "Limit: Rs" + String.format(Locale.getDefault(), "%,.2f", budget.limit)
        holder.tvBudgetSpent.text = "Spent: Rs" + String.format(Locale.getDefault(), "%,.2f", budget.spent)
        val remaining = budget.limit - budget.spent
        holder.tvBudgetRemaining.text = "Remaining: Rs" + String.format(Locale.getDefault(), "%,.2f", remaining)
        holder.progressBudget.max = budget.limit.toInt()
        holder.progressBudget.progress = budget.spent.toInt().coerceAtMost(budget.limit.toInt())
        // Set icon based on category
        holder.imgCategoryIcon.setImageResource(getIconRes(budget.category))

        // Warning logic
        if (budget.spent >= budget.limit) {
            holder.tvBudgetSpent.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.expenseRed))
            holder.tvBudgetRemaining.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.expenseRed))
            holder.warningIcon.visibility = View.VISIBLE
            holder.warningIcon.setImageResource(R.drawable.ic_warning)
            holder.warningIcon.contentDescription = "Budget Exceeded"
        } else if (budget.spent >= 0.8 * budget.limit) {
            holder.tvBudgetSpent.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.expenseRed))
            holder.tvBudgetRemaining.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.expenseRed))
            holder.warningIcon.visibility = View.VISIBLE
            holder.warningIcon.setImageResource(R.drawable.ic_warning_yellow)
            holder.warningIcon.contentDescription = "Budget Nearing Limit"
        } else {
            holder.tvBudgetSpent.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.primaryPink))
            holder.tvBudgetRemaining.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.primaryGreen))
            holder.warningIcon.visibility = View.GONE
        }

        holder.btnDeleteBudget.setOnClickListener { onDelete(budget) }
        holder.itemView.setOnClickListener { onEdit(budget) }
    }

    override fun getItemCount(): Int = budgets.size
}
