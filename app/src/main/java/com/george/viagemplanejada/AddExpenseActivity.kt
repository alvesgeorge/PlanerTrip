package com.george.viagemplanejada

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.george.viagemplanejada.databinding.ActivityAddExpenseBinding
import com.george.viagemplanejada.data.DataManager
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private var tripId: String = ""
    private var tripName: String = ""
    private lateinit var dataManager: DataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataManager = DataManager.getInstance(this)

        getTripData()
        setupUI()
        setupSpinners()
        setupDateTimePickers()
    }

    private fun getTripData() {
        tripId = intent.getStringExtra("trip_id") ?: dataManager.getCurrentTripId() ?: "default_trip"
        tripName = intent.getStringExtra("trip_name") ?: "Viagem"
    }

    private fun setupUI() {
        binding.buttonBack.setOnClickListener { finish() }
        binding.buttonSaveExpense.setOnClickListener { saveExpense() }

        // Data atual
        binding.editExpenseDate.setText(getCurrentDate())

        // Date picker
        binding.editExpenseDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun setupSpinners() {
        // Categorias
        val categories = arrayOf("Transporte", "Hospedagem", "Alimentação", "Compras", "Atividades", "Outros")
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = categoryAdapter

        // Formas de pagamento
        val paymentMethods = arrayOf("Dinheiro", "Cartão de Crédito", "Cartão de Débito", "PIX", "Transferência", "Outros")
        val paymentAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, paymentMethods)
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPaymentMethod.adapter = paymentAdapter
    }

    private fun setupDateTimePickers() {
        binding.editExpenseDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(selectedYear, selectedMonth, selectedDay)

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            binding.editExpenseDate.setText(dateFormat.format(selectedDate.time))
        }, year, month, day).show()
    }

    private fun saveExpense() {
        val description = binding.editExpenseDescription.text.toString().trim()
        val amountText = binding.editExpenseAmount.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString()
        val paymentMethod = binding.spinnerPaymentMethod.selectedItem.toString()
        val date = binding.editExpenseDate.text.toString()
        val notes = binding.editExpenseNotes.text.toString().trim()

        // Validações
        if (description.isEmpty()) {
            Toast.makeText(this, "⚠️ Digite a descrição do gasto", Toast.LENGTH_SHORT).show()
            return
        }

        if (amountText.isEmpty()) {
            Toast.makeText(this, "⚠️ Digite o valor do gasto", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val amount = amountText.replace(",", ".").toDouble()

            if (amount <= 0) {
                Toast.makeText(this, "⚠️ O valor deve ser maior que zero", Toast.LENGTH_SHORT).show()
                return
            }

            // Criar e salvar o gasto
            val expense = ExpenseItem(
                id = dataManager.generateId(),
                description = description,
                amount = amount,
                category = category,
                paymentMethod = paymentMethod,
                date = date,
                notes = notes
            )

            dataManager.saveExpense(tripId, expense)

            Toast.makeText(this, "✅ Gasto salvo com sucesso!\n💰 $description: R\$ ${String.format("%.2f", amount)}", Toast.LENGTH_LONG).show()

            setResult(RESULT_OK)
            finish()

        } catch (e: NumberFormatException) {
            Toast.makeText(this, "⚠️ Valor inválido", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date())
    }
}