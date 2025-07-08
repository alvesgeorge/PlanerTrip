package com.george.viagemplanejada

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.george.viagemplanejada.databinding.ActivityCreateTripBinding
import com.george.viagemplanejada.data.DataManager
import com.george.viagemplanejada.data.TripItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class CreateTripActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateTripBinding
    private lateinit var dataManager: DataManager
    private var startDate = ""
    private var endDate = ""
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Lista de cidades para sugestões
    private val citySuggestions = mutableListOf<String>()
    private lateinit var cityAdapter: ArrayAdapter<String>
    private var isSelectingCity = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateTripBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataManager = DataManager.getInstance(this)

        setupUI()
        setupCitySearch()
    }

    private fun setupUI() {
        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.editStartDate.setOnClickListener {
            showDatePicker(true)
        }

        binding.editEndDate.setOnClickListener {
            showDatePicker(false)
        }

        binding.buttonCreateTrip.setOnClickListener {
            createTrip()
        }
    }

    private fun setupCitySearch() {
        // Configurar adapter para sugestões
        cityAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, citySuggestions)
        binding.autoCompleteDestination.setAdapter(cityAdapter)

        // Configurar busca em tempo real
        binding.autoCompleteDestination.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (!isSelectingCity && s != null && s.length >= 3) {
                    searchCities(s.toString())
                }
                isSelectingCity = false
            }
        })

        // Configurar seleção de item
        binding.autoCompleteDestination.setOnItemClickListener { _, _, position, _ ->
            isSelectingCity = true
            val selectedCity = citySuggestions[position]
            binding.autoCompleteDestination.setText(selectedCity)
            binding.autoCompleteDestination.dismissDropDown()
        }
    }

    private fun searchCities(query: String) {
        // Mostrar indicador de carregamento
        binding.progressBarCity.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val cities = withContext(Dispatchers.IO) {
                    searchCitiesOnline(query)
                }

                citySuggestions.clear()
                citySuggestions.addAll(cities)
                cityAdapter.notifyDataSetChanged()

                // Mostrar dropdown se há sugestões
                if (cities.isNotEmpty()) {
                    binding.autoCompleteDestination.showDropDown()
                }

            } catch (e: Exception) {
                // Fallback para cidades brasileiras populares
                val fallbackCities = getFallbackCities(query)
                citySuggestions.clear()
                citySuggestions.addAll(fallbackCities)
                cityAdapter.notifyDataSetChanged()

                if (fallbackCities.isNotEmpty()) {
                    binding.autoCompleteDestination.showDropDown()
                }
            } finally {
                binding.progressBarCity.visibility = View.GONE
            }
        }
    }

    private suspend fun searchCitiesOnline(query: String): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Usar API de geocoding simples (OpenStreetMap Nominatim)
                val url = "https://nominatim.openstreetmap.org/search?format=json&limit=5&countrycodes=br&city=${query}"
                val response = URL(url).readText()

                // Parse do JSON usando JSONArray
                val cities = mutableListOf<String>()
                val jsonArray = JSONArray(response)

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val displayName = jsonObject.getString("display_name")
                    val cityName = displayName.split(",")[0].trim()

                    if (!cities.contains(cityName) && cities.size < 5) {
                        cities.add(cityName)
                    }
                }

                cities
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    private fun getFallbackCities(query: String): List<String> {
        val brazilianCities = listOf(
            "São Paulo", "Rio de Janeiro", "Brasília", "Salvador", "Fortaleza",
            "Belo Horizonte", "Manaus", "Curitiba", "Recife", "Goiânia",
            "Belém", "Porto Alegre", "Guarulhos", "Campinas", "São Luís",
            "São Gonçalo", "Maceió", "Duque de Caxias", "Natal", "Teresina",
            "Campo Grande", "Nova Iguaçu", "São Bernardo do Campo", "João Pessoa", "Osasco",
            "Santo André", "Jaboatão dos Guararapes", "Contagem", "São José dos Campos", "Uberlândia",
            "Sorocaba", "Cuiabá", "Aparecida de Goiânia", "Aracaju", "Feira de Santana",
            "Londrina", "Juiz de Fora", "Joinville", "Niterói", "Ananindeua",
            "Florianópolis", "Santos", "Ribeirão Preto", "Vila Velha", "Serra",
            "Diadema", "Carapicuíba", "Mauá", "Olinda", "Betim",
            "Gramado", "Campos do Jordão", "Bonito", "Jericoacoara", "Fernando de Noronha",
            "Paraty", "Ouro Preto", "Tiradentes", "Búzios", "Arraial do Cabo",
            "Ilhabela", "Ubatuba", "Angra dos Reis", "Petrópolis", "Teresópolis"
        )

        return brazilianCities.filter {
            it.lowercase().contains(query.lowercase())
        }.take(5)
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val selectedDate = dateFormat.format(calendar.time)

                if (isStartDate) {
                    startDate = selectedDate
                    binding.editStartDate.setText(selectedDate)
                } else {
                    endDate = selectedDate
                    binding.editEndDate.setText(selectedDate)
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun createTrip() {
        val name = binding.editTripName.text.toString().trim()
        val destination = binding.autoCompleteDestination.text.toString().trim()
        val description = binding.editTripDescription?.text?.toString()?.trim() ?: ""

        // Validações
        if (name.isEmpty()) {
            binding.editTripName.error = "Nome da viagem é obrigatório"
            return
        }

        if (destination.isEmpty()) {
            binding.autoCompleteDestination.error = "Destino é obrigatório"
            return
        }

        if (startDate.isEmpty()) {
            Toast.makeText(this, "Selecione a data de início", Toast.LENGTH_SHORT).show()
            return
        }

        if (endDate.isEmpty()) {
            Toast.makeText(this, "Selecione a data de fim", Toast.LENGTH_SHORT).show()
            return
        }

        // Validar se data de fim é posterior à data de início
        try {
            val startDateParsed = dateFormat.parse(startDate)
            val endDateParsed = dateFormat.parse(endDate)

            if (endDateParsed != null && startDateParsed != null && endDateParsed.before(startDateParsed)) {
                Toast.makeText(this, "Data de fim deve ser posterior à data de início", Toast.LENGTH_SHORT).show()
                return
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao validar datas", Toast.LENGTH_SHORT).show()
            return
        }

        // Criar viagem
        val trip = TripItem(
            id = dataManager.generateId(),
            name = name,
            destination = destination,
            startDate = startDate,
            endDate = endDate,
            description = description
        )

        dataManager.saveTrip(trip)
        dataManager.setCurrentTrip(trip.id)

        Toast.makeText(this, "✅ Viagem '$name' criada com sucesso!", Toast.LENGTH_SHORT).show()
        finish()
    }
}