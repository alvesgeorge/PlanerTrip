package com.george.viagemplanejada

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.george.viagemplanejada.databinding.ActivityItineraryBinding
import com.george.viagemplanejada.data.DataManager
import com.george.viagemplanejada.data.PlaceItem

class ItineraryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItineraryBinding
    private var places = mutableListOf<PlaceItem>()
    private lateinit var placeAdapter: PlaceAdapter
    private lateinit var dataManager: DataManager
    private var tripId: String = ""
    private var tripName: String = ""
    private var selectedDay: String = "Todos"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityItineraryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataManager = DataManager.getInstance(this)

        getTripData()
        setupUI()
        setupRecyclerView()
        loadPlaces()
        updateStats()
        filterPlacesByDay(selectedDay)
    }

    override fun onResume() {
        super.onResume()
        loadPlaces()
        updateStats()
        filterPlacesByDay(selectedDay)
    }

    private fun getTripData() {
        tripId = intent.getStringExtra("trip_id") ?: ""
        tripName = intent.getStringExtra("trip_name") ?: "Viagem"
        binding.textTripName.text = "🗺️ Roteiro: $tripName"
    }

    private fun setupUI() {
        binding.buttonBack.setOnClickListener { finish() }
        binding.buttonMapView.setOnClickListener { openMapView() }
        binding.fabAddPlace.setOnClickListener { showAddPlaceDialog() }
        binding.fabOptimizeRoute.setOnClickListener { optimizeRoute() }

        // Day filter chips
        binding.chipAllDays.setOnClickListener { filterPlacesByDay("Todos") }
        binding.chipDay1.setOnClickListener { filterPlacesByDay("Dia 1") }
        binding.chipDay2.setOnClickListener { filterPlacesByDay("Dia 2") }
        binding.chipDay3.setOnClickListener { filterPlacesByDay("Dia 3") }
    }

    private fun setupRecyclerView() {
        placeAdapter = PlaceAdapter(places) { place, action ->
            when (action) {
                "DETAILS" -> showPlaceDetails(place)
                "EDIT" -> editPlace(place)
                "DELETE" -> deletePlace(place)
                "DIRECTIONS" -> openDirections(place)
            }
        }

        binding.recyclerViewPlaces.apply {
            layoutManager = LinearLayoutManager(this@ItineraryActivity)
            adapter = placeAdapter
        }
    }

    private fun loadPlaces() {
        places.clear()
        places.addAll(dataManager.getPlaces(tripId))
        updateEmptyState()
    }

    private fun updateStats() {
        val totalPlaces = places.size
        val totalDays = places.map { it.day }.distinct().filter { it.isNotEmpty() }.size
        val totalTime = places.sumOf { it.duration }

        binding.textTotalPlaces.text = totalPlaces.toString()
        binding.textTotalDays.text = if (totalDays > 0) totalDays.toString() else "0"
        binding.textEstimatedTime.text = "${totalTime.toInt()}h"
    }

    private fun filterPlacesByDay(day: String) {
        selectedDay = day

        val filteredPlaces: List<PlaceItem> = if (day == "Todos") {
            places
        } else {
            places.filter { it.day.contains(day) }
        }

        placeAdapter.updatePlaces(filteredPlaces)
        updateEmptyState(filteredPlaces)

        if (places.isNotEmpty()) {
            Toast.makeText(this, "🔍 Filtro: $day (${filteredPlaces.size} locais)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateEmptyState(filteredPlaces: List<PlaceItem> = places) {
        if (filteredPlaces.isEmpty()) {
            binding.recyclerViewPlaces.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
        } else {
            binding.recyclerViewPlaces.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
        }
    }

    private fun showAddPlaceDialog() {
        val intent = Intent(this, AddPlaceActivity::class.java)
        intent.putExtra("trip_id", tripId)
        intent.putExtra("trip_name", tripName)
        startActivity(intent)
    }

    private fun openMapView() {
        if (places.isEmpty()) {
            Toast.makeText(this, "⚠️ Adicione locais ao roteiro primeiro", Toast.LENGTH_SHORT).show()
            return
        }

        val baseUrl = "https://www.google.com/maps/dir/"
        val waypoints = places.joinToString("/") { place ->
            place.address.replace(" ", "+").replace(",", "")
        }

        val mapsUrl = "$baseUrl$waypoints"

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl))
            intent.setPackage("com.google.android.apps.maps")
            startActivity(intent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl))
            startActivity(webIntent)
        }
    }

    private fun optimizeRoute() {
        if (places.size < 2) {
            Toast.makeText(this, "⚠️ Adicione pelo menos 2 locais para otimizar", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("🧭 Otimizar Roteiro")
            .setMessage("Deseja reorganizar os locais para otimizar o tempo de deslocamento?")
            .setPositiveButton("✅ Otimizar") { _, _ ->
                places.sortWith(compareBy<PlaceItem> { it.day }.thenByDescending {
                    when (it.priority) {
                        "Alta" -> 3
                        "Média" -> 2
                        "Baixa" -> 1
                        else -> 0
                    }
                })

                places.forEachIndexed { index, place ->
                    dataManager.savePlace(tripId, place)
                }

                placeAdapter.updatePlaces(places)
                Toast.makeText(this, "✅ Roteiro otimizado e salvo!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showPlaceDetails(place: PlaceItem) {
        val costInfo = if (place.cost > 0) {
            "\n💰 Custo: R\$ ${String.format("%.2f", place.cost)}"
        } else {
            ""
        }

        val details = buildString {
            appendLine("📍 Endereço: ${place.address}")
            appendLine("📅 Dia: ${place.day}")
            appendLine("🕐 Horário: ${place.preferredTime}")
            appendLine("⏱️ Duração: ${place.duration}h")
            appendLine("🏷️ Categoria: ${place.category}")
            appendLine("⭐ Prioridade: ${place.priority}")
            if (costInfo.isNotEmpty()) {
                append(costInfo)
            }
            if (place.description.isNotEmpty()) {
                appendLine("\n📝 Descrição:")
                append(place.description)
            }
        }

        AlertDialog.Builder(this)
            .setTitle(place.name)
            .setMessage(details)
            .setPositiveButton("OK", null)
            .setNeutralButton("🧭 Direções") { _, _ ->
                openDirections(place)
            }
            .setNegativeButton("🗑️ Excluir") { _, _ ->
                deletePlace(place)
            }
            .show()
    }

    private fun editPlace(place: PlaceItem) {
        val intent = Intent(this, AddPlaceActivity::class.java)
        intent.putExtra("trip_id", tripId)
        intent.putExtra("trip_name", tripName)
        intent.putExtra("edit_place_id", place.id)
        startActivity(intent)
    }

    private fun deletePlace(place: PlaceItem) {
        val message = "Deseja remover ${place.name} do roteiro?"

        AlertDialog.Builder(this)
            .setTitle("⚠️ Confirmar Remoção")
            .setMessage(message)
            .setPositiveButton("🗑️ Remover") { _, _ ->
                dataManager.deletePlace(tripId, place.id)
                places.remove(place)
                filterPlacesByDay(selectedDay)
                updateStats()
                Toast.makeText(this, "✅ Local removido do roteiro", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun openDirections(place: PlaceItem) {
        val address = place.address.replace(" ", "+")
        val mapsUrl = "https://www.google.com/maps/dir/?api=1&destination=$address"

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl))
            intent.setPackage("com.google.android.apps.maps")
            startActivity(intent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl))
            startActivity(webIntent)
        }
    }
}