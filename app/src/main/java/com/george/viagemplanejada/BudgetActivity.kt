package com.george.viagemplanejada

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.george.viagemplanejada.databinding.ActivityBudgetBinding
import com.george.viagemplanejada.data.DataManager
import com.george.viagemplanejada.data.ExpenseItem
import java.text.NumberFormat
import java.util.*

class BudgetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBudgetBinding
    private var expenses = mutableListOf<ExpenseItem>()
    private lateinit var expenseAdapter: ExpenseAdapter
    private var totalBudget = 5000.0
    private var tripId: String = ""
    private var tripName: String = ""
    private lateinit var dataManager: DataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBudgetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataManager = DataManager.getInstance(this)

        getTripData()
        setupUI()
        setupRecyclerView()
        loadExpenses()
        updateBudgetInfo()
    }

    override fun onResume() {
        super.onResume()
        loadExpenses()
        updateBudgetInfo()
    }

    private fun getTripData() {
        tripId = intent.getStringExtra("trip_id") ?: dataManager.getCurrentTripId() ?: "default_trip"
        tripName = intent.getStringExtra("trip_name") ?: "Minha Viagem"
        binding.textTripName.text = "💰 Orçamento: $tripName"

        totalBudget = dataManager.getBudget(tripId)
    }

    private fun setupUI() {
        binding.buttonBack.setOnClickListener { finish() }
        binding.buttonSetBudget.setOnClickListener { showSetBudgetDialog() }
        binding.fabAddExpense.setOnClickListener { showAddExpenseDialog() }

        // Filter chips
        binding.chipAll.setOnClickListener { filterExpenses("Todos") }
        binding.chipTransport.setOnClickListener { filterExpenses("Transporte") }
        binding.chipAccommodation.setOnClickListener { filterExpenses("Hospedagem") }
        binding.chipFood.setOnClickListener { filterExpenses("Alimentação") }
        binding.chipShopping.setOnClickListener { filterExpenses("Compras") }
        binding.chipActivities.setOnClickListener { filterExpenses("Atividades") }
    }

    private fun setupRecyclerView() {
        expenseAdapter = ExpenseAdapter(expenses) { expense, action ->
            when (action) {
                "DETAILS" -> showExpenseDetails(expense)
                "DELETE" -> deleteExpense(expense)
            }
        }

        binding.recyclerViewExpenses.apply {
            layoutManager = LinearLayoutManager(this@BudgetActivity)
            adapter = expenseAdapter
        }
    }

    private fun loadExpenses() {
        expenses.clear()
        expenses.addAll(dataManager.getExpenses(tripId))
        updateEmptyState()
        expenseAdapter.updateExpenses(expenses)
    }

    private fun updateEmptyState() {
        if (expenses.isEmpty()) {
            binding.recyclerViewExpenses.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
        } else {
            binding.recyclerViewExpenses.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
        }
    }

    private fun updateBudgetInfo() {
        val totalSpent = expenses.sumOf { it.amount }
        val remaining = totalBudget - totalSpent
        val progress = if (totalBudget > 0) ((totalSpent / totalBudget) * 100).toInt() else 0

        binding.textTotalBudget.text = "💰 Orçamento: ${formatCurrency(totalBudget)}"
        binding.textTotalSpent.text = "💸 Gasto: ${formatCurrency(totalSpent)}"
        binding.textRemaining.text = "💵 Restante: ${formatCurrency(remaining)}"
        binding.textProgressPercentage.text = "$progress% do orçamento utilizado"

        binding.progressBudget.progress = progress

        // Cor do progresso
        val color = when {
            progress < 50 -> android.graphics.Color.GREEN
            progress < 80 -> android.graphics.Color.parseColor("#FF9800")
            else -> android.graphics.Color.RED
        }
        binding.progressBudget.progressTintList = android.content.res.ColorStateList.valueOf(color)

        // Alerta se passou do orçamento
        if (totalSpent > totalBudget) {
            binding.textRemaining.text = "⚠️ Excedeu: ${formatCurrency(totalSpent - totalBudget)}"
            binding.textRemaining.setTextColor(android.graphics.Color.RED)
        }
    }

    private fun showSetBudgetDialog() {
        val editText = android.widget.EditText(this).apply {
            hint = "Digite o orçamento total"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(totalBudget.toString())
        }

        AlertDialog.Builder(this)
            .setTitle("💰 Definir Orçamento")
            .setView(editText)
            .setPositiveButton("Salvar") { _, _ ->
                val newBudget = editText.text.toString().toDoubleOrNull()
                if (newBudget != null && newBudget > 0) {
                    totalBudget = newBudget
                    dataManager.saveBudget(tripId, totalBudget)
                    updateBudgetInfo()
                    Toast.makeText(this, "✅ Orçamento salvo!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "❌ Valor inválido", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showAddExpenseDialog() {
        val intent = Intent(this, AddExpenseActivity::class.java)
        intent.putExtra("trip_id", tripId)
        intent.putExtra("trip_name", tripName)
        startActivity(intent)
    }

    private fun filterExpenses(category: String) {
        val filteredExpenses = if (category == "Todos") {
            expenses
        } else {
            expenses.filter { it.category == category }
        }
        expenseAdapter.updateExpenses(filteredExpenses)
        Toast.makeText(this, "🔍 Filtro: $category", Toast.LENGTH_SHORT).show()
    }

    private fun deleteExpense(expense: ExpenseItem) {
        val message = "Deseja excluir ${expense.title}?"

        AlertDialog.Builder(this)
            .setTitle("⚠️ Confirmar Exclusão")
            .setMessage(message)
            .setPositiveButton("🗑️ Excluir") { _, _ ->
                dataManager.deleteExpense(tripId, expense.id)
                loadExpenses()
                updateBudgetInfo()
                Toast.makeText(this, "✅ Gasto excluído", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showExpenseDetails(expense: ExpenseItem) {
        val details = buildString {
            appendLine("💰 Valor: ${formatCurrency(expense.amount)}")
            appendLine("🏷️ Categoria: ${expense.category}")
            appendLine("💳 Pagamento: ${expense.paymentMethod}")
            appendLine("📅 Data: ${expense.date}")
            append("📝 Observações: ${expense.description}")
        }

        AlertDialog.Builder(this)
            .setTitle(expense.title)
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun formatCurrency(amount: Double): String {
        return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(amount)
    }
}