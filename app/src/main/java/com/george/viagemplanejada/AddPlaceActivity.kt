package com.george.viagemplanejada

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.george.viagemplanejada.data.DataManager
import com.george.viagemplanejada.data.PlaceItem // ← IMPORT CORRETO
import java.text.SimpleDateFormat
import java.util.*

class AddPlaceActivity : AppCompatActivity() {

    private lateinit var dataManager: DataManager
    private lateinit var editPlaceName: EditText
    private lateinit var editPlaceAddress: AutoCompleteTextView
    private lateinit var spinnerDay: Spinner
    private lateinit var spinnerCategory: Spinner
    private lateinit var editVisitDuration: EditText
    private lateinit var editPreferredTime: EditText
    private lateinit var editPlaceCost: EditText
    private lateinit var editPlaceDescription: EditText
    private lateinit var buttonScanNote: Button
    private lateinit var buttonSavePlace: Button
    private lateinit var buttonBack: Button

    // RadioButtons para prioridade
    private lateinit var radioHighPriority: RadioButton
    private lateinit var radioMediumPriority: RadioButton
    private lateinit var radioLowPriority: RadioButton

    private var tripId: String = ""
    private var tripName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_place)

        dataManager = DataManager.getInstance(this)

        initViews()
        getTripData()
        setupSpinners()
        setupListeners()
    }

    private fun initViews() {
        editPlaceName = findViewById(R.id.editPlaceName)
        editPlaceAddress = findViewById(R.id.editPlaceAddress)
        spinnerDay = findViewById(R.id.spinnerDay)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        editVisitDuration = findViewById(R.id.editVisitDuration)
        editPreferredTime = findViewById(R.id.editPreferredTime)
        editPlaceCost = findViewById(R.id.editPlaceCost)
        editPlaceDescription = findViewById(R.id.editPlaceDescription)
        buttonScanNote = findViewById(R.id.buttonScanNote)
        buttonSavePlace = findViewById(R.id.buttonSavePlace)
        buttonBack = findViewById(R.id.buttonBack)

        radioHighPriority = findViewById(R.id.radioHighPriority)
        radioMediumPriority = findViewById(R.id.radioMediumPriority)
        radioLowPriority = findViewById(R.id.radioLowPriority)
    }

    private fun getTripData() {
        tripId = intent.getStringExtra("trip_id") ?: ""
        tripName = intent.getStringExtra("trip_name") ?: ""

        // Se não temos tripId, tentar obter da viagem atual
        if (tripId.isEmpty()) {
            val currentTrip = dataManager.getCurrentTrip()
            if (currentTrip != null) {
                tripId = currentTrip.id
                tripName = currentTrip.name
            }
        }

        supportActionBar?.title = "Adicionar Local - $tripName"
    }

    private fun setupSpinners() {
        // Setup do Spinner de Dias - usando dados da viagem real
        val trip = dataManager.getTripById(tripId)
        val daysList = if (trip != null) {
            generateDaysList(trip.startDate, trip.endDate)
        } else {
            // Fallback para dias genéricos
            listOf(
                "📅 Dia 1",
                "📅 Dia 2",
                "📅 Dia 3",
                "📅 Dia 4",
                "📅 Dia 5"
            )
        }

        val daysAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, daysList)
        daysAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDay.adapter = daysAdapter

        // Setup do Spinner de Categorias
        val categories = listOf(
            "🏛️ Turismo",
            "🎭 Cultura",
            "🌳 Natureza",
            "🎡 Lazer",
            "🍽️ Gastronomia",
            "🛍️ Compras",
            "🏨 Hospedagem",
            "🚗 Transporte",
            "📍 Outros"
        )

        val categoriesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoriesAdapter
    }

    private fun generateDaysList(startDate: String, endDate: String): List<String> {
        val daysList = mutableListOf<String>()

        if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val start = sdf.parse(startDate)
                val end = sdf.parse(endDate)

                if (start != null && end != null) {
                    val calendar = Calendar.getInstance()
                    calendar.time = start
                    var dayCount = 1

                    while (calendar.time <= end) {
                        val dayString = "📅 Dia $dayCount (${sdf.format(calendar.time)})"
                        daysList.add(dayString)
                        calendar.add(Calendar.DAY_OF_MONTH, 1)
                        dayCount++
                    }
                }
            } catch (e: Exception) {
                // Se houver erro, usar dias genéricos
                for (i in 1..5) {
                    daysList.add("�� Dia $i")
                }
            }
        } else {
            // Dias genéricos se não houver datas
            for (i in 1..5) {
                daysList.add("📅 Dia $i")
            }
        }

        return daysList
    }

    private fun setupListeners() {
        buttonBack.setOnClickListener {
            finish()
        }

        // Configurar seletor de horário
        editPreferredTime.setOnClickListener {
            showTimePicker()
        }

        // Configurar scanner de nota (placeholder)
        buttonScanNote.setOnClickListener {
            Toast.makeText(this, "📸 Funcionalidade de scanner em desenvolvimento!", Toast.LENGTH_SHORT).show()
        }

        // Configurar AutoComplete para endereços
        setupAddressAutoComplete()

        buttonSavePlace.setOnClickListener {
            savePlace()
        }
    }

    private fun setupAddressAutoComplete() {
        val famousPlaces = arrayOf(
            "Cristo Redentor - Rio de Janeiro, RJ",
            "Pão de Açúcar - Rio de Janeiro, RJ",
            "Copacabana - Rio de Janeiro, RJ",
            "Ipanema - Rio de Janeiro, RJ",
            "Avenida Paulista - São Paulo, SP",
            "Marco Zero - Recife, PE",
            "Pelourinho - Salvador, BA",
            "Centro Histórico - Ouro Preto, MG",
            "Cataratas do Iguaçu - Foz do Iguaçu, PR",
            "Teatro Amazonas - Manaus, AM",
            "Bonito - Mato Grosso do Sul",
            "Fernando de Noronha - Pernambuco",
            "Gramado - Rio Grande do Sul",
            "Campos do Jordão - São Paulo",
            "Búzios - Rio de Janeiro"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, famousPlaces)
        editPlaceAddress.setAdapter(adapter)
        editPlaceAddress.threshold = 2
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val timeString = String.format("%02d:%02d", selectedHour, selectedMinute)
            editPreferredTime.setText(timeString)
        }, hour, minute, true).show()
    }

    private fun savePlace() {
        val name = editPlaceName.text.toString().trim()
        val address = editPlaceAddress.text.toString().trim()
        val day = spinnerDay.selectedItem.toString()
        val category = spinnerCategory.selectedItem.toString()
        val durationText = editVisitDuration.text.toString().trim()
        val preferredTime = editPreferredTime.text.toString().trim()
        val costText = editPlaceCost.text.toString().trim()
        val description = editPlaceDescription.text.toString().trim()

        // Validações básicas
        if (name.isEmpty()) {
            editPlaceName.error = "Nome é obrigatório"
            editPlaceName.requestFocus()
            return
        }

        if (address.isEmpty()) {
            editPlaceAddress.error = "Endereço é obrigatório"
            editPlaceAddress.requestFocus()
            return
        }

        if (tripId.isEmpty()) {
            Toast.makeText(this, "❌ Erro: ID da viagem não encontrado", Toast.LENGTH_LONG).show()
            return
        }

        // Determinar prioridade
        val priority = when {
            radioHighPriority.isChecked -> "Alta"
            radioMediumPriority.isChecked -> "Média"
            radioLowPriority.isChecked -> "Baixa"
            else -> "Média"
        }

        // Converter duração para Double
        val duration = if (durationText.isNotEmpty()) {
            durationText.toDoubleOrNull() ?: 2.0
        } else {
            2.0
        }

        // Converter custo para Double
        val cost = if (costText.isNotEmpty()) {
            costText.toDoubleOrNull() ?: 0.0
        } else {
            0.0
        }

        // Criar objeto PlaceItem usando o tipo correto
        val place = PlaceItem(
            id = dataManager.generateId(), // ← USAR assim
            name = name,
            address = address,
            day = day,
            preferredTime = preferredTime,
            duration = duration,
            category = category,
            priority = priority,
            cost = cost,
            description = description
        )

        // Salvar usando o método savePlace do DataManager
        try {
            dataManager.savePlace(tripId, place)

            Toast.makeText(this, "✅ Local '$name' adicionado com sucesso!", Toast.LENGTH_SHORT).show()

            // Retornar para a tela anterior
            val resultIntent = Intent()
            resultIntent.putExtra("place_added", true)
            resultIntent.putExtra("place_name", name)
            setResult(RESULT_OK, resultIntent)
            finish()

        } catch (e: Exception) {
            Toast.makeText(this, "❌ Erro ao salvar local: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}