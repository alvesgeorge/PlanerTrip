package com.george.viagemplanejada

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.*

class ExpenseAdapter(
    private var expenses: List<ExpenseItem>,
    private val onExpenseAction: (ExpenseItem, String) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconText: TextView = itemView.findViewById(R.id.textExpenseIcon)
        val descriptionText: TextView = itemView.findViewById(R.id.textExpenseDescription)
        val detailsText: TextView = itemView.findViewById(R.id.textExpenseDetails)
        val amountText: TextView = itemView.findViewById(R.id.textExpenseAmount)
        val optionsButton: Button = itemView.findViewById(R.id.buttonExpenseOptions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]

        // Ícone baseado na categoria
        val categoryIcon = when (expense.category) {
            "Transporte" -> "🚗"
            "Hospedagem" -> "🏨"
            "Alimentação" -> "🍽️"
            "Compras" -> "🛍️"
            "Atividades" -> "🎭"
            else -> "💰"
        }

        holder.iconText.text = categoryIcon
        holder.descriptionText.text = expense.description
        holder.detailsText.text = "${expense.category} • ${expense.date} • ${expense.paymentMethod}"
        holder.amountText.text = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(expense.amount)

        // Click listeners
        holder.itemView.setOnClickListener {
            onExpenseAction(expense, "DETAILS")
        }

        holder.optionsButton.setOnClickListener {
            showOptionsMenu(holder.itemView, expense)
        }
    }

    private fun showOptionsMenu(view: View, expense: ExpenseItem) {
        val popup = androidx.appcompat.widget.PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.expense_options_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_details -> {
                    onExpenseAction(expense, "DETAILS")
                    true
                }
                R.id.action_edit -> {
                    onExpenseAction(expense, "EDIT")
                    true
                }
                R.id.action_delete -> {
                    onExpenseAction(expense, "DELETE")
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    override fun getItemCount(): Int = expenses.size

    fun updateExpenses(newExpenses: List<ExpenseItem>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }
}

// Data class para gastos
data class ExpenseItem(
    val id: String,
    val description: String,
    val amount: Double,
    val category: String,
    val paymentMethod: String,
    val date: String,
    val notes: String
)