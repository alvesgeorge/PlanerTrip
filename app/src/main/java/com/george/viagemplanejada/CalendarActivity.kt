package com.george.viagemplanejada

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.george.viagemplanejada.databinding.ActivityCalendarBinding
import com.george.viagemplanejada.data.DataManager
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarBinding
    private var events = mutableListOf<EventItem>()
    private lateinit var eventAdapter: EventAdapter
    private var selectedDate: String = ""
    private var tripId: String = ""
    private var tripName: String = ""
    private lateinit var dataManager: DataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataManager = DataManager.getInstance(this)

        getTripData()
        setupUI()
        setupCalendar()
        setupRecyclerView()
        loadEvents()
        updateSelectedDate(getCurrentDate())
    }

    override fun onResume() {
        super.onResume()
        loadEvents() // Recarregar quando voltar de AddEventActivity
    }

    private fun getTripData() {
        tripId = intent.getStringExtra("trip_id") ?: dataManager.getCurrentTripId() ?: "default_trip"
        tripName = intent.getStringExtra("trip_name") ?: "Minha Viagem"
        binding.textTripName.text = "📅 Calendário: $tripName"
    }

    private fun setupUI() {
        binding.buttonBack.setOnClickListener { finish() }
        binding.buttonToday.setOnClickListener { goToToday() }
        binding.fabAddEvent.setOnClickListener { showAddEventDialog() }
    }

    private fun setupCalendar() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            selectedDate = dateFormat.format(calendar.time)

            updateSelectedDate(selectedDate)
            filterEventsByDate(selectedDate)
        }

        // Set initial date
        selectedDate = getCurrentDate()
    }

    private fun setupRecyclerView() {
        eventAdapter = EventAdapter(events) { event, action ->
            when (action) {
                "DETAILS" -> showEventDetails(event)
                "EDIT" -> editEvent(event)
                "DELETE" -> deleteEvent(event)
            }
        }

        binding.recyclerViewEvents.apply {
            layoutManager = LinearLayoutManager(this@CalendarActivity)
            adapter = eventAdapter
        }
    }

    private fun loadEvents() {
        events.clear()
        events.addAll(dataManager.getEvents(tripId))
        filterEventsByDate(selectedDate)
    }

    private fun updateSelectedDate(date: String) {
        selectedDate = date

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val displayFormat = SimpleDateFormat("EEEE, dd 'de' MMMM", Locale("pt", "BR"))

        try {
            val parsedDate = dateFormat.parse(date)
            calendar.time = parsedDate ?: Date()

            val displayDate = displayFormat.format(calendar.time)
            binding.textSelectedDate.text = "📅 $displayDate"

        } catch (e: Exception) {
            binding.textSelectedDate.text = "📅 $date"
        }

        filterEventsByDate(date)
    }

    private fun filterEventsByDate(date: String) {
        val dayEvents = events.filter { it.date == date }
        eventAdapter.updateEvents(dayEvents)
        updateEmptyState(dayEvents)
        updateEventCount(dayEvents.size)
    }

    private fun updateEmptyState(dayEvents: List<EventItem>) {
        if (dayEvents.isEmpty()) {
            binding.recyclerViewEvents.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
        } else {
            binding.recyclerViewEvents.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
        }
    }

    private fun updateEventCount(count: Int) {
        val text = when (count) {
            0 -> "🎯 Nenhum evento programado"
            1 -> "🎯 1 evento programado"
            else -> "🎯 $count eventos programados"
        }
        binding.textEventCount.text = text
    }

    private fun goToToday() {
        val today = getCurrentDate()
        updateSelectedDate(today)

        // Update calendar view to today
        val calendar = Calendar.getInstance()
        binding.calendarView.date = calendar.timeInMillis

        Toast.makeText(this, "📅 Voltando para hoje", Toast.LENGTH_SHORT).show()
    }

    private fun showAddEventDialog() {
        val intent = Intent(this, AddEventActivity::class.java)
        intent.putExtra("trip_id", tripId)
        intent.putExtra("trip_name", tripName)
        intent.putExtra("selected_date", selectedDate)
        startActivity(intent)
    }

    private fun showEventDetails(event: EventItem) {
        val timeInfo = if (event.endTime.isNotEmpty()) {
            "${event.startTime} - ${event.endTime}"
        } else {
            event.startTime
        }

        val locationInfo = if (event.location.isNotEmpty()) {
            "\n📍 Local: ${event.location}"
        } else {
            ""
        }

        val details = buildString {
            appendLine("🕐 Horário: $timeInfo")
            appendLine("🏷️ Categoria: ${event.category}")
            appendLine("⭐ Prioridade: ${event.priority}")
            if (locationInfo.isNotEmpty()) {
                append(locationInfo)
            }
            if (event.description.isNotEmpty()) {
                appendLine("\n📝 Descrição:")
                append(event.description)
            }
        }

        AlertDialog.Builder(this)
            .setTitle(event.title)
            .setMessage(details)
            .setPositiveButton("OK", null)
            .setNeutralButton("✏️ Editar") { _, _ ->
                editEvent(event)
            }
            .setNegativeButton("🗑️ Excluir") { _, _ ->
                deleteEvent(event)
            }
            .show()
    }

    private fun editEvent(event: EventItem) {
        Toast.makeText(this, "✏️ Editar: ${event.title}", Toast.LENGTH_SHORT).show()
        // TODO: Implementar edição
    }

    private fun deleteEvent(event: EventItem) {
        val message = "Deseja excluir o evento ${event.title}?"

        AlertDialog.Builder(this)
            .setTitle("⚠️ Confirmar Exclusão")
            .setMessage(message)
            .setPositiveButton("🗑️ Excluir") { _, _ ->
                dataManager.deleteEvent(tripId, event.id)
                loadEvents()
                Toast.makeText(this, "✅ Evento excluído", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date())
    }
}