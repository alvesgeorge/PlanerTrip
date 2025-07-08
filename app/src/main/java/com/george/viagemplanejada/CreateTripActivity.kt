package com.george.viagemplanejada

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.george.viagemplanejada.databinding.ActivityCreateTripBinding
import com.george.viagemplanejada.data.DataManager
import com.george.viagemplanejada.data.TripItem
import java.text.SimpleDateFormat
import java.util.*

class CreateTripActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateTripBinding
    private lateinit var dataManager: DataManager
    private var startDate = ""
    private var endDate = ""
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateTripBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataManager = DataManager.getInstance(this)

        setupUI()
    }

    private fun setupUI() {
        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.editStartDate.setOnClickListener {
            showDatePicker { date ->
                startDate = date
                binding.editStartDate.setText(date)
            }
        }

        binding.editEndDate.setOnClickListener {
            showDatePicker { date ->
                endDate = date
                binding.editEndDate.setText(date)
            }
        }

        binding.buttonCreateTrip.setOnClickListener {
            createTrip()
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val formattedDate = dateFormat.format(calendar.time)
                onDateSelected(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun createTrip() {
        val name = binding.editTripName.text.toString().trim()
        val destination = binding.editDestination.text.toString().trim()
        val description = binding.editDescription.text.toString().trim()
        val budgetText = binding.editBudget.text.toString().trim()

        // Validações
        if (name.isEmpty()) {
            binding.editTripName.error = "Nome da viagem é obrigatório"
            return
        }

        if (destination.isEmpty()) {
            binding.editDestination.error = "Destino é obrigatório"
            return
        }

        val budget = try {
            if (budgetText.isEmpty()) 0.0 else budgetText.toDouble()
        } catch (e: NumberFormatException) {
            binding.editBudget.error = "Valor inválido"
            return
        }

        // Criar trip
        val trip = TripItem(
            id = UUID.randomUUID().toString(),
            name = name,
            destination = destination,
            startDate = startDate,
            endDate = endDate,
            description = description,
            budget = budget
        )

        // Salvar
        dataManager.saveTrip(trip)
        dataManager.setCurrentTrip(trip.id)

        Toast.makeText(this, "✅ Viagem '$name' criada com sucesso!", Toast.LENGTH_SHORT).show()

        finish()
    }
}