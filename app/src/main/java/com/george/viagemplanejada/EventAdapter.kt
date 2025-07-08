package com.george.viagemplanejada

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.*

class ExpenseAdapter(
    private var expenses: List<ExpenseItem>,
    private val onExpenseClick: (ExpenseItem, String) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textDescription: TextView = itemView.findViewById(R.id.textExpenseDescription)
        val textAmount: TextView = itemView.findViewById(R.id.textExpenseAmount)
        val textCategory: TextView = itemView.findViewById(R.id.textExpenseCategory)
        val textDate: TextView = itemView.findViewById(R.id.textExpenseDate)
        val textPaymentMethod: TextView = itemView.findViewById(R.id.textExpensePaymentMethod)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]

        holder.textDescription.text = expense.description
        holder.textAmount.text = formatCurrency(expense.amount)
        holder.textCategory.text = getCategoryIcon(expense.category) + " " + expense.category
        holder.textDate.text = "📅 " + expense.date
        holder.textPaymentMethod.text = "💳 " + expense.paymentMethod

        // Click listeners
        holder.itemView.setOnClickListener {
            onExpenseClick(expense, "DETAILS")
        }

        holder.itemView.setOnLongClickListener {
            onExpenseClick(expense, "DELETE")
            true
        }
    }

    override fun getItemCount(): Int = expenses.size

    fun updateExpenses(newExpenses: List<ExpenseItem>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }

    private fun formatCurrency(amount: Double): String {
        return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(amount)
    }

    private fun getCategoryIcon(category: String): String {
        return when (category) {
            "Transporte" -> "🚗"
            "Hospedagem" -> "🏨"
            "Alimentação" -> "🍽️"
            "Compras" -> "🛍️"
            "Atividades" -> "🎡"
            else -> "💰"
        }
    }
}