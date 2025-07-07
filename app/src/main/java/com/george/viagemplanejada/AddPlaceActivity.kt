package com.george.viagemplanejada

import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.george.viagemplanejada.databinding.ActivityAddPlaceBinding
import com.george.viagemplanejada.data.DataManager
import com.george.viagemplanejada.utils.AddressSearchManager
import com.george.viagemplanejada.utils.AddressSuggestion
import java.util.Calendar

class AddPlaceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPlaceBinding
    private lateinit var dataManager: DataManager
    private lateinit var addressSearchManager: AddressSearchManager
    private var tripId: String = ""
    private var tripName: String = ""
    private var editPlaceId: String = ""
    private var isEditMode = false
    private var addressSuggestions = mutableListOf<AddressSuggestion>()

    // Flag para evitar loop infinito
    private var isSelectingAddress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar managers
        dataManager = DataManager.getInstance(this)
        addressSearchManager = AddressSearchManager(this)

        getTripData()
        checkEditMode()
        setupUI()
        setupSpinners()
        setupTimePicker()
        setupAddressSearch()

        if (isEditMode) {
            loadPlaceData()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        addressSearchManager.cleanup()
    }

    private fun setupAddressSearch() {
        // Configurar busca de endereços com proteção contra loop
        binding.editPlaceAddress.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Evitar busca durante seleção automática
                if (isSelectingAddress) return

                val query = s.toString().trim()
                if (query.length >= 3) {
                    searchAddresses(query)
                } else {
                    clearAddressSuggestions()
                }
            }
        })

        // Configurar dropdown de sugestões
        setupAddressDropdown()
    }


    private fun searchAddresses(query: String) {
        // Mostrar indicador de carregamento
        binding.editPlaceAddress.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.ic_popup_sync, 0)

        addressSearchManager.searchAddresses(query) { suggestions ->
            addressSuggestions.clear()
            addressSuggestions.addAll(suggestions)
            updateAddressDropdown()

            // Remover indicador de carregamento
            binding.editPlaceAddress.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }
    }

    private fun setupAddressDropdown() {
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line)
        binding.editPlaceAddress.setAdapter(adapter)

        binding.editPlaceAddress.setOnItemClickListener { parent, view, position, id ->
            if (position < addressSuggestions.size) {
                val suggestion = addressSuggestions[position]
                selectAddress(suggestion)
            }
        }
    }

    private fun updateAddressDropdown() {
        val suggestions = addressSuggestions.map { suggestion ->
            val icon = if (suggestion.isLocal) "⭐" else "📍"
            "$icon ${suggestion.name} - ${suggestion.country}"
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, suggestions)
        binding.editPlaceAddress.setAdapter(adapter)

        if (suggestions.isNotEmpty()) {
            binding.editPlaceAddress.showDropDown()
            Toast.makeText(this, "🌍 ${suggestions.size} sugestões encontradas", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearAddressSuggestions() {
        addressSuggestions.clear()
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, emptyList<String>())
        binding.editPlaceAddress.setAdapter(adapter)
    }

    private fun selectAddress(suggestion: AddressSuggestion) {
        // ATIVAR FLAG para evitar loop
        isSelectingAddress = true

        // Preencher automaticamente o nome se estiver vazio
        if (binding.editPlaceName.text.toString().trim().isEmpty()) {
            binding.editPlaceName.setText(suggestion.name)
        }

        // Definir endereço completo
        binding.editPlaceAddress.setText(suggestion.address)

        // Auto-detectar categoria
        autoSelectCategory(suggestion.category)

        // Limpar sugestões
        clearAddressSuggestions()

        // Feedback visual com país
        val icon = if (suggestion.isLocal) "⭐" else "📍"
        Toast.makeText(this, "$icon ${suggestion.name} (${suggestion.country}) selecionado!", Toast.LENGTH_SHORT).show()

        // DESATIVAR FLAG após um pequeno delay
        binding.editPlaceAddress.postDelayed({
            isSelectingAddress = false
        }, 500)
    }

    private fun autoSelectCategory(detectedCategory: String) {
        val categories = arrayOf("Turismo", "Cultura", "Natureza", "Lazer", "Gastronomia", "Compras", "Outros")
        val position = categories.indexOf(detectedCategory)

        if (position >= 0) {
            binding.spinnerCategory.setSelection(position)
            Toast.makeText(this, "🏷️ Categoria detectada: $detectedCategory", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getTripData() {
        tripId = intent.getStringExtra("trip_id") ?: ""
        tripName = intent.getStringExtra("trip_name") ?: "Viagem"
        editPlaceId = intent.getStringExtra("edit_place_id") ?: ""
        isEditMode = editPlaceId.isNotEmpty()
    }

    private fun checkEditMode() {
        if (isEditMode) {
            supportActionBar?.title = "✏️ Editar Local"
            binding.buttonSavePlace.text = "✅ Atualizar Local"
        } else {
            supportActionBar?.title = "📍 Adicionar Local"
            binding.buttonSavePlace.text = "✅ Salvar Local"
        }
    }

    private fun setupUI() {
        binding.buttonBack.setOnClickListener { finish() }
        binding.buttonSavePlace.setOnClickListener { savePlace() }
    }

    private fun setupSpinners() {
        val days = arrayOf("Dia 1", "Dia 2", "Dia 3", "Dia 4", "Dia 5")
        val dayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, days)
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDay.adapter = dayAdapter

        val categories = arrayOf("Turismo", "Cultura", "Natureza", "Lazer", "Gastronomia", "Compras", "Outros")
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = categoryAdapter
    }

    private fun setupTimePicker() {
        binding.editPreferredTime.setOnClickListener {
            showTimePicker()
        }
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val timeFormat = String.format("%02d:%02d", selectedHour, selectedMinute)
            binding.editPreferredTime.setText(timeFormat)
        }, hour, minute, true).show()
    }

    private fun loadPlaceData() {
        // ATIVAR FLAG durante carregamento
        isSelectingAddress = true

        val places = dataManager.getPlaces(tripId)
        val place = places.find { it.id == editPlaceId }

        if (place != null) {
            binding.editPlaceName.setText(place.name)
            binding.editPlaceAddress.setText(place.address)
            binding.editPlaceDescription.setText(place.description)
            binding.editVisitDuration.setText(place.duration.toString())
            binding.editPreferredTime.setText(place.preferredTime)

            if (place.cost > 0) {
                binding.editPlaceCost.setText(place.cost.toString())
            }

            val dayAdapter = binding.spinnerDay.adapter as ArrayAdapter<String>
            val dayPosition = dayAdapter.getPosition(place.day)
            if (dayPosition >= 0) {
                binding.spinnerDay.setSelection(dayPosition)
            }

            val categoryAdapter = binding.spinnerCategory.adapter as ArrayAdapter<String>
            val categoryPosition = categoryAdapter.getPosition(place.category)
            if (categoryPosition >= 0) {
                binding.spinnerCategory.setSelection(categoryPosition)
            }

            when (place.priority) {
                "Alta" -> binding.radioHighPriority.isChecked = true
                "Média" -> binding.radioMediumPriority.isChecked = true
                "Baixa" -> binding.radioLowPriority.isChecked = true
            }

            Toast.makeText(this, "📝 Dados carregados para edição", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "❌ Erro ao carregar dados do local", Toast.LENGTH_SHORT).show()
            finish()
        }

        // DESATIVAR FLAG após carregamento
        binding.editPlaceAddress.postDelayed({
            isSelectingAddress = false
        }, 1000)
    }


    private fun savePlace() {
        val name = binding.editPlaceName.text.toString().trim()
        val address = binding.editPlaceAddress.text.toString().trim()
        val description = binding.editPlaceDescription.text.toString().trim()
        val day = binding.spinnerDay.selectedItem.toString()
        val category = binding.spinnerCategory.selectedItem.toString()
        val durationText = binding.editVisitDuration.text.toString().trim()
        val preferredTime = binding.editPreferredTime.text.toString().trim()
        val priority = getSelectedPriority()
        val costText = binding.editPlaceCost.text.toString().trim()

        if (name.isEmpty()) {
            binding.editPlaceName.error = "Campo obrigatório"
            binding.editPlaceName.requestFocus()
            Toast.makeText(this, "⚠️ Digite o nome do local", Toast.LENGTH_SHORT).show()
            return
        }

        if (address.isEmpty()) {
            binding.editPlaceAddress.error = "Campo obrigatório"
            binding.editPlaceAddress.requestFocus()
            Toast.makeText(this, "⚠️ Digite o endereço", Toast.LENGTH_SHORT).show()
            return
        }

        if (durationText.isEmpty()) {
            binding.editVisitDuration.error = "Campo obrigatório"
            binding.editVisitDuration.requestFocus()
            Toast.makeText(this, "⚠️ Digite o tempo de visita", Toast.LENGTH_SHORT).show()
            return
        }

        if (preferredTime.isEmpty()) {
            binding.editPreferredTime.error = "Campo obrigatório"
            binding.editPreferredTime.requestFocus()
            Toast.makeText(this, "⚠️ Selecione o horário preferido", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val duration = durationText.toDouble()
            val cost = if (costText.isNotEmpty()) costText.toDouble() else 0.0

            if (duration <= 0) {
                binding.editVisitDuration.error = "Deve ser maior que zero"
                binding.editVisitDuration.requestFocus()
                Toast.makeText(this, "⚠️ O tempo de visita deve ser maior que zero", Toast.LENGTH_SHORT).show()
                return
            }

            if (cost < 0) {
                binding.editPlaceCost.error = "Não pode ser negativo"
                binding.editPlaceCost.requestFocus()
                Toast.makeText(this, "⚠️ O custo não pode ser negativo", Toast.LENGTH_SHORT).show()
                return
            }

            val placeItem = PlaceItem(
                id = if (isEditMode) editPlaceId else dataManager.generateId(),
                name = name,
                address = address,
                description = description,
                day = day,
                category = category,
                duration = duration,
                preferredTime = preferredTime,
                priority = priority,
                cost = cost
            )

            dataManager.savePlace(tripId, placeItem)

            val actionText = if (isEditMode) "atualizado" else "adicionado"
            Toast.makeText(this, "✅ Local $actionText com sucesso!", Toast.LENGTH_SHORT).show()

            setResult(RESULT_OK)
            finish()

        } catch (e: NumberFormatException) {
            Toast.makeText(this, "⚠️ Valores numéricos inválidos", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "❌ Erro ao salvar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getSelectedPriority(): String {
        return when {
            binding.radioHighPriority.isChecked -> "Alta"
            binding.radioMediumPriority.isChecked -> "Média"
            binding.radioLowPriority.isChecked -> "Baixa"
            else -> "Média"
        }
    }
}