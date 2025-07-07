package com.george.viagemplanejada

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.Toast
import com.george.viagemplanejada.databinding.ActivityAddItineraryBinding
import com.george.viagemplanejada.data.DataManager

class AddItineraryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddItineraryBinding
    private lateinit var itineraryManager: ItineraryManager
    private var tripName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddItineraryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        itineraryManager = ItineraryManager(this)
        tripName = intent.getStringExtra("trip_name")

        setupUI()
    }

    private fun setupUI() {
        binding.textTitle.text = "➕ Nova Atividade"

        // Mostrar viagem selecionada
        tripName?.let {
            binding.textTripName.text = "Viagem: $it"

            // Sugerir próximo dia
            val nextDay = itineraryManager.getNextDayNumber(it)
            binding.editDay.setText(nextDay.toString())
        }

        setupCategorySpinner()

        binding.buttonCancel.setOnClickListener {
            finish()
        }

        binding.buttonSave.setOnClickListener {
            saveItinerary()
        }
    }

    private fun setupCategorySpinner() {
        val categories = listOf(
            "Transporte", "Hospedagem", "Alimentação", "Atração",
            "Compras", "Entretenimento", "Relaxamento", "Outros"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun saveItinerary() {
        val title = binding.editTitle.text.toString().trim()
        val description = binding.editDescription.text.toString().trim()
        val day = binding.editDay.text.toString().trim().toIntOrNull() ?: 1
        val date = binding.editDate.text.toString().trim()
        val startTime = binding.editStartTime.text.toString().trim()
        val endTime = binding.editEndTime.text.toString().trim()
        val location = binding.editLocation.text.toString().trim()
        val estimatedCost = binding.editCost.text.toString().trim()
        val notes = binding.editNotes.text.toString().trim()

        // Validações
        if (title.isEmpty()) {
            binding.textStatus.text = "❌ Título da atividade é obrigatório"
            binding.editTitle.requestFocus()
            return
        }

        if (description.isEmpty()) {
            binding.textStatus.text = "❌ Descrição da atividade é obrigatória"
            binding.editDescription.requestFocus()
            return
        }

        if (tripName == null) {
            binding.textStatus.text = "❌ Erro: viagem não identificada"
            return
        }

        // Obter categoria selecionada
        val categoryIndex = binding.spinnerCategory.selectedItemPosition
        val category = when (categoryIndex) {
            0 -> ItineraryCategory.TRANSPORT
            1 -> ItineraryCategory.ACCOMMODATION
            2 -> ItineraryCategory.FOOD
            3 -> ItineraryCategory.ATTRACTION
            4 -> ItineraryCategory.SHOPPING
            5 -> ItineraryCategory.ENTERTAINMENT
            6 -> ItineraryCategory.RELAXATION
            else -> ItineraryCategory.OTHERS
        }

        // Criar item do roteiro
        val success = itineraryManager.createItinerary(
            tripName = tripName!!,
            day = day,
            date = date,
            title = title,
            description = description,
            startTime = startTime,
            endTime = endTime,
            location = location,
            category = category,
            estimatedCost = estimatedCost,
            notes = notes
        )

        if (success) {
            binding.textStatus.text = "✅ Atividade adicionada com sucesso!"
            Toast.makeText(this, "Atividade '$title' adicionada ao roteiro!", Toast.LENGTH_SHORT).show()

            // Voltar após 1 segundo
            binding.root.postDelayed({
                finish()
            }, 1000)
        } else {
            binding.textStatus.text = "❌ Erro ao adicionar atividade"
            Toast.makeText(this, "Erro ao adicionar atividade. Tente novamente.", Toast.LENGTH_SHORT).show()
        }
    }
}