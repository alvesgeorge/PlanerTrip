package com.george.viagemplanejada

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.george.viagemplanejada.databinding.ActivityAddPlaceBinding
import com.george.viagemplanejada.data.DataManager
import com.george.viagemplanejada.data.PlaceItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class AddPlaceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPlaceBinding
    private lateinit var dataManager: DataManager
    private var selectedDate = ""
    private var selectedTime = ""
    private var tripId = ""
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    // Para busca de endereços
    private val addressSuggestions = mutableListOf<String>()
    private lateinit var addressAdapter: ArrayAdapter<String>
    private var isSelectingAddress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataManager = DataManager.getInstance(this)
        tripId = intent.getStringExtra("trip_id") ?: dataManager.getCurrentTripId() ?: ""

        initViews()
        setupAddressSearch()

        // Definir data e hora atuais como padrão
        val now = Calendar.getInstance()
        selectedDate = dateFormat.format(now.time)
        selectedTime = timeFormat.format(now.time)
        binding.editPlaceDate.setText(selectedDate)
        binding.editPlaceTime.setText(selectedTime)
    }

    private fun initViews() {
        // ✅ CORREÇÃO: Usar ImageButton em vez de Button
        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.editPlaceDate.setOnClickListener {
            showDatePicker()
        }

        binding.editPlaceTime.setOnClickListener {
            showTimePicker()
        }

        binding.buttonSavePlace.setOnClickListener {
            savePlace()
        }
    }

    private fun setupAddressSearch() {
        // Configurar adapter para sugestões de endereço
        addressAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, addressSuggestions)
        binding.autoCompleteAddress.setAdapter(addressAdapter)

        // Configurar busca em tempo real
        binding.autoCompleteAddress.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (!isSelectingAddress && s != null && s.length >= 5) {
                    searchAddresses(s.toString())
                }
                isSelectingAddress = false
            }
        })

        // Configurar seleção de item
        binding.autoCompleteAddress.setOnItemClickListener { _, _, position, _ ->
            isSelectingAddress = true
            val selectedAddress = addressSuggestions[position]
            binding.autoCompleteAddress.setText(selectedAddress)
            binding.autoCompleteAddress.dismissDropDown()
        }
    }

    private fun searchAddresses(query: String) {
        // Mostrar indicador de carregamento
        binding.progressBarAddress.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val addresses = withContext(Dispatchers.IO) {
                    searchAddressesOnline(query)
                }

                addressSuggestions.clear()
                addressSuggestions.addAll(addresses)
                addressAdapter.notifyDataSetChanged()

                // Mostrar dropdown se há sugestões
                if (addresses.isNotEmpty()) {
                    binding.autoCompleteAddress.showDropDown()
                }

            } catch (e: Exception) {
                // Fallback para endereços genéricos
                val fallbackAddresses = getFallbackAddresses(query)
                addressSuggestions.clear()
                addressSuggestions.addAll(fallbackAddresses)
                addressAdapter.notifyDataSetChanged()

                if (fallbackAddresses.isNotEmpty()) {
                    binding.autoCompleteAddress.showDropDown()
                }
            } finally {
                binding.progressBarAddress.visibility = View.GONE
            }
        }
    }

    private suspend fun searchAddressesOnline(query: String): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Usar API de geocoding (OpenStreetMap Nominatim)
                val url = "https://nominatim.openstreetmap.org/search?format=json&limit=5&countrycodes=br&q=${query}"
                val response = URL(url).readText()

                // Parse do JSON
                val addresses = mutableListOf<String>()
                val jsonArray = JSONArray(response)

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val displayName = jsonObject.getString("display_name")

                    if (!addresses.contains(displayName) && addresses.size < 5) {
                        addresses.add(displayName)
                    }
                }

                addresses
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    private fun getFallbackAddresses(query: String): List<String> {
        val commonPlaces = listOf(
            "Centro, ${query}",
            "Aeroporto de ${query}",
            "Rodoviária de ${query}",
            "Centro Histórico, ${query}",
            "Praia de ${query}",
            "Shopping ${query}",
            "Estação ${query}",
            "Hotel em ${query}",
            "Restaurante em ${query}",
            "Ponto Turístico, ${query}"
        )

        return commonPlaces.take(5)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = dateFormat.format(calendar.time)
                binding.editPlaceDate.setText(selectedDate)
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
                binding.editPlaceTime.setText(selectedTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }
    private fun savePlace() {
        val name = binding.editPlaceName.text.toString().trim()
        val address = binding.autoCompleteAddress.text.toString().trim()
        val description = binding.editPlaceDescription.text.toString().trim()
        val durationText = binding.editPlaceDuration.text.toString().trim().ifEmpty { "2" }
        val costText = binding.editPlaceCost.text.toString().trim().ifEmpty { "0.00" }

        // Validações
        if (name.isEmpty()) {
            binding.editPlaceName.error = "Nome do local é obrigatório"
            return
        }

        if (address.isEmpty()) {
            binding.autoCompleteAddress.error = "Endereço é obrigatório"
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

        // ✅ CONVERSÕES CORRETAS para Double
        val duration = try {
            // Extrair apenas números da duração (ex: "2 horas" -> 2.0)
            durationText.replace(Regex("[^0-9.,]"), "").replace(",", ".").toDoubleOrNull() ?: 2.0
        } catch (e: Exception) {
            2.0
        }

        val cost = try {
            costText.replace(",", ".").toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            0.0
        }

        // ✅ CRIAR PLACE COM TIPOS CORRETOS
        val place = PlaceItem(
            id = dataManager.generateId(),
            name = name,
            address = address,
            day = selectedDate,
            category = "Geral",
            duration = duration,           // ✅ Double
            preferredTime = selectedTime,
            cost = cost,                   // ✅ Double
            description = description
            // priority tem valor padrão "Média"
        )

        dataManager.savePlace(tripId, place)
        Toast.makeText(this, "✅ Local '$name' adicionado com sucesso!", Toast.LENGTH_SHORT).show()
        finish()
    }
}