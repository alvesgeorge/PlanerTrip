package com.george.viagemplanejada

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.george.viagemplanejada.databinding.ActivityAddEventBinding
import com.george.viagemplanejada.data.DataManager
import java.text.SimpleDateFormat
import java.util.*


class AddEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEventBinding
    private var tripId: String = ""
    private var tripName: String = ""
    private var selectedDate: String = ""
    private lateinit var dataManager: DataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddEventBinding.inflate(layoutInflater)
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
        selectedDate = intent.getStringExtra("selected_date") ?: getCurrentDate()

        // Set initial date
        binding.editEventDate.setText(selectedDate)
    }

    private fun setupUI() {
        binding.buttonBack.setOnClickListener { finish() }
        binding.buttonSaveEvent.setOnClickListener { saveEvent() }
    }

    private fun setupSpinners() {
        // Categorias
        val categories = arrayOf("Turismo", "Hospedagem", "Alimentação", "Transporte", "Atividade", "Compras", "Outros")
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerEventCategory.adapter = categoryAdapter
    }

    private fun setupDateTimePickers() {
        // Date picker
        binding.editEventDate.setOnClickListener {
            showDatePicker()
        }

        // Time pickers
        binding.editEventStartTime.setOnClickListener {
            showTimePicker(true)
        }

        binding.editEventEndTime.setOnClickListener {
            showTimePicker(false)
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        // Parse current date if available
        try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val currentDate = dateFormat.parse(binding.editEventDate.text.toString())
            if (currentDate != null) {
                calendar.time = currentDate
            }
        } catch (e: Exception) {
            // Use current date if parsing fails
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(selectedYear, selectedMonth, selectedDay)

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            binding.editEventDate.setText(dateFormat.format(selectedDate.time))
        }, year, month, day).show()
    }

    private fun showTimePicker(isStartTime: Boolean) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val timeFormat = String.format("%02d:%02d", selectedHour, selectedMinute)

            if (isStartTime) {
                binding.editEventStartTime.setText(timeFormat)
            } else {
                binding.editEventEndTime.setText(timeFormat)
            }
        }, hour, minute, true).show()
    }

    private fun saveEvent() {
        val title = binding.editEventTitle.text.toString().trim()
        val description = binding.editEventDescription.text.toString().trim()
        val date = binding.editEventDate.text.toString().trim()
        val startTime = binding.editEventStartTime.text.toString().trim()
        val endTime = binding.editEventEndTime.text.toString().trim()
        val category = binding.spinnerEventCategory.selectedItem.toString()
        val location = binding.editEventLocation.text.toString().trim()
        val priority = getSelectedPriority()

        // Validações
        if (title.isEmpty()) {
            Toast.makeText(this, "⚠️ Digite o título do evento", Toast.LENGTH_SHORT).show()
            return
        }

        if (date.isEmpty()) {
            Toast.makeText(this, "⚠️ Selecione a data do evento", Toast.LENGTH_SHORT).show()
            return
        }

        if (startTime.isEmpty()) {
            Toast.makeText(this, "⚠️ Selecione o horário de início", Toast.LENGTH_SHORT).show()
            return
        }

        // Validar se horário de fim é posterior ao de início
        if (endTime.isNotEmpty() && !isValidTimeRange(startTime, endTime)) {
            Toast.makeText(this, "⚠️ Horário de fim deve ser posterior ao de início", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Criar e salvar o evento

            val event = EventItem(
                id = dataManager.generateId(),
                title = title,
                description = description,
                date = selectedDate,
                time = selectedTime,  // ← Usar 'time' em vez de 'startTime'
                location = location,
                type = type,  // ← Usar 'type' em vez de 'category'
                isCompleted = false
            )

            dataManager.saveEvent(tripId, event)

            val eventSummary = buildString {
                appendLine("📅 $title")
                appendLine("🕐 $date às $startTime")
                if (endTime.isNotEmpty()) {
                    appendLine("⏰ Até $endTime")
                }
                appendLine("🏷️ $category")
                if (location.isNotEmpty()) {
                    appendLine("📍 $location")
                }
                append("⭐ Prioridade: $priority")
            }

            Toast.makeText(this, "✅ Evento salvo com sucesso!\n\n$eventSummary", Toast.LENGTH_LONG).show()

            setResult(RESULT_OK)
            finish()

        } catch (e: Exception) {
            Toast.makeText(this, "⚠️ Erro ao salvar evento: ${e.message}", Toast.LENGTH_SHORT).show()
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

    private fun isValidTimeRange(startTime: String, endTime: String): Boolean {
        return try {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val start = timeFormat.parse(startTime)
            val end = timeFormat.parse(endTime)

            start != null && end != null && end.after(start)
        } catch (e: Exception) {
            false
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date())
    }
}