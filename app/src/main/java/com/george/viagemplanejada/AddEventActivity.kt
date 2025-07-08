package com.george.viagemplanejada

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.george.viagemplanejada.databinding.ActivityAddEventBinding
import com.george.viagemplanejada.data.DataManager
import com.george.viagemplanejada.data.EventItem
import java.text.SimpleDateFormat
import java.util.*

class AddEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEventBinding
    private lateinit var dataManager: DataManager
    private var selectedDate = ""
    private var selectedTime = ""
    private var selectedType = "Evento"
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataManager = DataManager.getInstance(this)

        setupUI()
        setupSpinners()
    }

    private fun setupUI() {
        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.editEventDate.setOnClickListener {
            showDatePicker()
        }

        binding.editEventTime.setOnClickListener {
            showTimePicker()
        }

        binding.buttonSaveEvent.setOnClickListener {
            saveEvent()
        }
    }

    private fun setupSpinners() {
        val eventTypes = arrayOf("Evento", "Reunião", "Atividade", "Transporte", "Refeição", "Lazer")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, eventTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerEventType.adapter = adapter

        binding.spinnerEventType.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedType = eventTypes[position]
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
                binding.editEventDate.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()

        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                selectedTime = timeFormat.format(calendar.time)
                binding.editEventTime.setText(selectedTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun saveEvent() {
        val title = binding.editEventTitle.text.toString().trim()
        val description = binding.editEventDescription.text.toString().trim()
        val location = binding.editEventLocation.text.toString().trim()

        if (title.isEmpty()) {
            binding.editEventTitle.error = "Título é obrigatório"
            return
        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Selecione uma data", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedTime.isEmpty()) {
            Toast.makeText(this, "Selecione um horário", Toast.LENGTH_SHORT).show()
            return
        }

        val event = EventItem(
            id = dataManager.generateId(),
            title = title,
            description = description,
            date = selectedDate,
            time = selectedTime,
            location = location,
            type = selectedType
        )

        dataManager.saveEvent(event)
        Toast.makeText(this, "✅ Evento '$title' salvo com sucesso!", Toast.LENGTH_SHORT).show()
        finish()
    }
}