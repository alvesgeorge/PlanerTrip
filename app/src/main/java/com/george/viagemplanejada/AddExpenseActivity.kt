package com.george.viagemplanejada

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.george.viagemplanejada.databinding.ActivityAddExpenseBinding
import com.george.viagemplanejada.data.DataManager
import com.george.viagemplanejada.data.ExpenseItem
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private lateinit var dataManager: DataManager
    private var selectedDate = ""
    private var selectedCategory = "Alimentação"
    private var selectedPaymentMethod = "Dinheiro"
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataManager = DataManager.getInstance(this)

        setupUI()
        setupSpinners()

        // Definir data atual como padrão
        selectedDate = dateFormat.format(Date())
        binding.editExpenseDate.setText(selectedDate)
    }

    private fun setupUI() {
        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.editExpenseDate.setOnClickListener {
            showDatePicker()
        }

        binding.buttonSaveExpense.setOnClickListener {
            saveExpense()
        }
    }

    private fun setupSpinners() {
        // Spinner de categorias
        val categories = arrayOf("Alimentação", "Transporte", "Hospedagem", "Compras", "Atividades", "Outros")
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerExpenseCategory.adapter = categoryAdapter

        binding.spinnerExpenseCategory.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedCategory = categories[position]
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })

        // Spinner de métodos de pagamento
        val paymentMethods = arrayOf("Dinheiro", "Cartão de Crédito", "Cartão de Débito", "PIX", "Transferência")
        val paymentAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, paymentMethods)
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPaymentMethod.adapter = paymentAdapter

        binding.spinnerPaymentMethod.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedPaymentMethod = paymentMethods[position]
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = dateFormat.format(calendar.time)
                binding.editExpenseDate.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveExpense() {
        val title = binding.editExpenseTitle.text.toString().trim()
        val amountText = binding.editExpenseAmount.text.toString().trim()
        val description = binding.editExpenseDescription.text.toString().trim()

        if (title.isEmpty()) {
            binding.editExpenseTitle.error = "Título é obrigatório"
            return
        }

        val amount = try {
            amountText.toDouble()
        } catch (e: NumberFormatException) {
            binding.editExpenseAmount.error = "Valor inválido"
            return
        }

        if (amount <= 0) {
            binding.editExpenseAmount.error = "Valor deve ser maior que zero"
            return
        }

        val expense = ExpenseItem(
            id = dataManager.generateId(),
            title = title,
            amount = amount,
            category = selectedCategory,
            date = selectedDate,
            description = description,
            paymentMethod = selectedPaymentMethod
        )

        dataManager.saveExpense(expense)
        Toast.makeText(this, "✅ Despesa '$title' salva com sucesso!", Toast.LENGTH_SHORT).show()
        finish()
    }
}