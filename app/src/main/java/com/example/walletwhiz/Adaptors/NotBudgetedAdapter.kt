package com.example.walletwhiz.Adaptors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.walletwhiz.R

class NotBudgetedAdapter(
    private val categories: List<String>,
    private val onSetBudget: (String) -> Unit,
    private val getIconRes: (String) -> Int
) : RecyclerView.Adapter<NotBudgetedAdapter.NotBudgetedViewHolder>() {

    class NotBudgetedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgCategoryIcon: ImageView = itemView.findViewById(R.id.imgCategoryIcon)
        val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        val btnSetBudget: Button = itemView.findViewById(R.id.btnSetBudget)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotBudgetedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_not_budgeted, parent, false)
        return NotBudgetedViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotBudgetedViewHolder, position: Int) {
        val category = categories[position]
        holder.tvCategoryName.text = category
        // Set icon based on category
        holder.imgCategoryIcon.setImageResource(getIconRes(category))
        holder.btnSetBudget.setOnClickListener { onSetBudget(category) }
    }

    override fun getItemCount(): Int = categories.size
}
