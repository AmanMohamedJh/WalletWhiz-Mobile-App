package com.example.walletwhiz.Adaptors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.walletwhiz.R
import com.example.walletwhiz.model.Transaction
import java.text.NumberFormat
import java.util.Locale

class EachCatogaryTransactionsAdapter(
    private val transactions: List<Transaction>,
    private val onItemClick: (Transaction) -> Unit
) : RecyclerView.Adapter<EachCatogaryTransactionsAdapter.TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.each_catogary_transection, parent, false)
        return TransactionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount() = transactions.size

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tvSubtitle)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val ivCategory: ImageView = itemView.findViewById(R.id.ivCategory)

        init {
            itemView.setOnClickListener {
                onItemClick(transactions[adapterPosition])
            }
        }

        fun bind(transaction: Transaction) {
            tvTitle.text = transaction.title
            tvSubtitle.text = transaction.date
            val formattedAmount = "Rs "+ String.format(Locale("en", "IN"), "%,.2f", Math.abs(transaction.amount))
            if (transaction.type == "EXPENSE") {
                tvAmount.text = "- $formattedAmount"
                tvAmount.setTextColor(itemView.context.getColor(R.color.debitRed))
            } else {
                tvAmount.text = formattedAmount
                tvAmount.setTextColor(itemView.context.getColor(R.color.black))
            }
            ivCategory.setImageResource(getCategoryIcon(transaction.category))
        }

        private fun getCategoryIcon(category: String): Int {
            return when (category.lowercase()) {
                "food" -> R.drawable.ic_food
                "salary" -> R.drawable.ic_salary
                "rent" -> R.drawable.ic_rent
                "transport" -> R.drawable.ic_transport
                "savings" -> R.drawable.ic_savings
                "medicine", "medicines" -> R.drawable.ic_medicine
                "groceries" -> R.drawable.ic_groceries
                "gift" -> R.drawable.ic_gift
                "entertainment" -> R.drawable.ic_entertainment
                else -> R.drawable.ic_misc
            }
        }
    }
}
