package com.george.viagemplanejada

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import android.widget.AutoCompleteTextView
import com.george.viagemplanejada.databinding.ActivityAddTripBinding

class AddTripActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTripBinding
    private lateinit var tripManager: TripManager

    // Variáveis para edição
    private var isEditMode = false
    private var originalTrip: Trip? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddTripBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tripManager = TripManager(this)

        // Verificar se é modo de edição
        checkEditMode()
        setupUI()
        setupUiEnhancements()
    }

    private fun checkEditMode() {
        val tripName = intent.getStringExtra("trip_name")
        val tripDestination = intent.getStringExtra("trip_destination")

        if (tripName != null && tripDestination != null) {
            // Modo edição
            isEditMode = true

            // Encontrar a viagem original
            val trips = tripManager.getAllTrips()
            originalTrip = trips.find { it.name == tripName && it.destination == tripDestination }

            originalTrip?.let { trip ->
                // Preencher campos com dados existentes
                binding.editTripName.setText(trip.name)
                binding.editDestination.setText(trip.destination)
                binding.editStartDate.setText(trip.startDate)
                binding.editEndDate.setText(trip.endDate)
                binding.editBudget.setText(trip.budget)
                binding.editNotes.setText(trip.notes)

                // Atualizar título
                binding.textTitle.text = "✏️ Editar Viagem"
                binding.buttonSave.text = "💾 Atualizar"
                binding.textStatus.text = "Edite os campos desejados"
            }
        }
    }

    private fun setupUI() {
        binding.buttonCancel.setOnClickListener {
            finish()
        }

        binding.buttonSave.setOnClickListener {
            if (isEditMode) {
                updateTrip()
            } else {
                saveTrip()
            }
        }
    }

    private fun setupUiEnhancements() {
        // Configurar AutoComplete para destino
        UiUtils.setupCityAutoComplete(this, binding.editDestination as AutoCompleteTextView)

        // Configurar seletores de data
        UiUtils.setupDatePicker(this, binding.editStartDate)
        UiUtils.setupDatePicker(this, binding.editEndDate)

        // Configurar formatação de moeda
        UiUtils.setupCurrencyEditText(binding.editBudget)

        // Adicionar ícones aos campos
        binding.editTripName.setCompoundDrawablesWithIntrinsicBounds(
            android.R.drawable.ic_dialog_info, 0, 0, 0
        )
        binding.editDestination.setCompoundDrawablesWithIntrinsicBounds(
            android.R.drawable.ic_dialog_map, 0, 0, 0
        )
        binding.editStartDate.setCompoundDrawablesWithIntrinsicBounds(
            android.R.drawable.ic_menu_today, 0, 0, 0
        )
        binding.editEndDate.setCompoundDrawablesWithIntrinsicBounds(
            android.R.drawable.ic_menu_today, 0, 0, 0
        )
        binding.editBudget.setCompoundDrawablesWithIntrinsicBounds(
            android.R.drawable.ic_dialog_info, 0, 0, 0
        )
        binding.editNotes.setCompoundDrawablesWithIntrinsicBounds(
            android.R.drawable.ic_menu_edit, 0, 0, 0
        )
    }

    private fun saveTrip() {
        val tripName = binding.editTripName.text.toString().trim()
        val destination = binding.editDestination.text.toString().trim()
        val startDate = binding.editStartDate.text.toString().trim()
        val endDate = binding.editEndDate.text.toString().trim()
        val budget = binding.editBudget.text.toString().trim()
        val notes = binding.editNotes.text.toString().trim()

        // Validações aprimoradas
        if (tripName.isEmpty()) {
            binding.textStatus.text = "❌ Nome da viagem é obrigatório"
            binding.editTripName.requestFocus()
            return
        }

        if (destination.isEmpty()) {
            binding.textStatus.text = "❌ Destino é obrigatório"
            binding.editDestination.requestFocus()
            return
        }

        if (startDate.isNotEmpty() && !UiUtils.isValidDate(startDate)) {
            binding.textStatus.text = "❌ Data de início inválida"
            binding.editStartDate.requestFocus()
            return
        }

        if (endDate.isNotEmpty() && !UiUtils.isValidDate(endDate)) {
            binding.textStatus.text = "❌ Data de fim inválida"
            binding.editEndDate.requestFocus()
            return
        }

        if (budget.isNotEmpty() && !UiUtils.isValidCurrency(budget)) {
            binding.textStatus.text = "❌ Orçamento inválido"
            binding.editBudget.requestFocus()
            return
        }

        // Criar objeto Trip
        val trip = Trip(
            name = tripName,
            destination = destination,
            startDate = startDate,
            endDate = endDate,
            budget = budget,
            notes = notes
        )

        // Salvar usando TripManager
        val success = tripManager.saveTrip(trip)

        if (success) {
            binding.textStatus.text = "✅ Viagem salva com sucesso!"
            Toast.makeText(this, "Viagem '$tripName' salva!", Toast.LENGTH_SHORT).show()

            // Voltar após 1 segundo
            binding.root.postDelayed({
                finish()
            }, 1000)
        } else {
            binding.textStatus.text = "❌ Erro ao salvar viagem"
            Toast.makeText(this, "Erro ao salvar. Tente novamente.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTrip() {
        val tripName = binding.editTripName.text.toString().trim()
        val destination = binding.editDestination.text.toString().trim()
        val startDate = binding.editStartDate.text.toString().trim()
        val endDate = binding.editEndDate.text.toString().trim()
        val budget = binding.editBudget.text.toString().trim()
        val notes = binding.editNotes.text.toString().trim()

        // Validações aprimoradas
        if (tripName.isEmpty()) {
            binding.textStatus.text = "❌ Nome da viagem é obrigatório"
            binding.editTripName.requestFocus()
            return
        }

        if (destination.isEmpty()) {
            binding.textStatus.text = "❌ Destino é obrigatório"
            binding.editDestination.requestFocus()
            return
        }

        if (startDate.isNotEmpty() && !UiUtils.isValidDate(startDate)) {
            binding.textStatus.text = "❌ Data de início inválida"
            binding.editStartDate.requestFocus()
            return
        }

        if (endDate.isNotEmpty() && !UiUtils.isValidDate(endDate)) {
            binding.textStatus.text = "❌ Data de fim inválida"
            binding.editEndDate.requestFocus()
            return
        }

        if (budget.isNotEmpty() && !UiUtils.isValidCurrency(budget)) {
            binding.textStatus.text = "❌ Orçamento inválido"
            binding.editBudget.requestFocus()
            return
        }

        // Criar nova versão da viagem
        val updatedTrip = Trip(
            name = tripName,
            destination = destination,
            startDate = startDate,
            endDate = endDate,
            budget = budget,
            notes = notes
        )

        // Atualizar usando TripManager
        originalTrip?.let { oldTrip ->
            val success = tripManager.editTrip(oldTrip, updatedTrip)

            if (success) {
                binding.textStatus.text = "✅ Viagem atualizada com sucesso!"
                Toast.makeText(this, "Viagem '$tripName' atualizada!", Toast.LENGTH_SHORT).show()

                // Voltar após 1 segundo
                binding.root.postDelayed({
                    finish()
                }, 1000)
            } else {
                binding.textStatus.text = "❌ Erro ao atualizar viagem"
                Toast.makeText(this, "Erro ao atualizar. Tente novamente.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}