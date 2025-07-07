package com.george.viagemplanejada

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object UiUtils {

    // Formatador de moeda (adapta-se à região do dispositivo)
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())

    // Formatador de data
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Formatador de hora
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    /**
     * Configura um EditText para formatação automática de moeda
     */
    fun setupCurrencyEditText(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return

                isUpdating = true

                // Usar replace simples ao invés de regex
                val str = s.toString()
                    .replace("R$", "")
                    .replace("€", "")
                    .replace("£", "")
                    .replace("¥", "")
                    .replace("₹", "")
                    .replace("¢", "")
                    .replace("$", "")
                    .replace(" ", "")
                    .replace(".", "")
                    .replace(",", "")

                if (str.isNotEmpty()) {
                    val value = str.toDoubleOrNull() ?: 0.0
                    val formatted = currencyFormat.format(value / 100)
                    editText.setText(formatted)
                    editText.setSelection(formatted.length)
                }

                isUpdating = false
            }
        })
    }

    /**
     * Configura um EditText para seleção de data
     */
    fun setupDatePicker(context: Context, editText: EditText) {
        editText.isFocusable = false
        editText.isClickable = true

        editText.setOnClickListener {
            val calendar = Calendar.getInstance()

            // Se já tem data, usar ela como padrão
            if (editText.text.isNotEmpty()) {
                try {
                    val date = dateFormat.parse(editText.text.toString())
                    date?.let { calendar.time = it }
                } catch (e: Exception) {
                    // Usar data atual se não conseguir parsear
                }
            }

            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    editText.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    /**
     * Configura um EditText para seleção de horário
     */
    fun setupTimePicker(context: Context, editText: EditText) {
        editText.isFocusable = false
        editText.isClickable = true

        editText.setOnClickListener {
            val calendar = Calendar.getInstance()

            // Se já tem horário, usar ele como padrão
            if (editText.text.isNotEmpty()) {
                try {
                    val time = timeFormat.parse(editText.text.toString())
                    time?.let { calendar.time = it }
                } catch (e: Exception) {
                    // Usar horário atual se não conseguir parsear
                }
            }

            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    editText.setText(timeFormat.format(calendar.time))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }
    }

    /**
     * Configura AutoComplete para cidades mundiais com controle otimizado
     */
    fun setupCityAutoComplete(context: Context, autoCompleteTextView: AutoCompleteTextView) {
        val cityApiManager = CityApiManager(context)
        var searchJob: Job? = null
        var lastQuery = ""

        autoCompleteTextView.threshold = 2

        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()

                // Evitar buscas duplicadas
                if (query == lastQuery) return
                lastQuery = query

                if (query.length >= 2) {
                    // Cancelar busca anterior apenas se necessário
                    searchJob?.cancel()

                    // Nova busca com delay maior para evitar cancelamentos
                    searchJob = CoroutineScope(Dispatchers.Main).launch {
                        delay(800) // Aumentar delay para 800ms

                        try {
                            val cities = cityApiManager.searchCities(query)
                            val cityNames = cities.map { it.getDisplayName() }

                            // Verificar se ainda é a query atual
                            if (query == autoCompleteTextView.text.toString().trim()) {
                                val adapter = ArrayAdapter(
                                    context,
                                    android.R.layout.simple_dropdown_item_1line,
                                    cityNames
                                )

                                autoCompleteTextView.setAdapter(adapter)

                                if (cityNames.isNotEmpty()) {
                                    autoCompleteTextView.showDropDown()
                                }
                            }
                        } catch (e: Exception) {
                            // Em caso de erro, usar fallback offline
                            if (query == autoCompleteTextView.text.toString().trim()) {
                                setupOfflineCityAutoComplete(context, autoCompleteTextView, query)
                            }
                        }
                    }
                } else if (query.isEmpty()) {
                    // Limpar sugestões se campo estiver vazio
                    searchJob?.cancel()
                    autoCompleteTextView.dismissDropDown()
                }
            }
        })

        // Configurar sugestões iniciais offline
        setupOfflineCityAutoComplete(context, autoCompleteTextView, "")
    }

    /**
     * Fallback para AutoComplete offline com busca
     */
    private fun setupOfflineCityAutoComplete(context: Context, autoCompleteTextView: AutoCompleteTextView, query: String) {
        val cities = getPopularCities()
        val filteredCities = if (query.isNotEmpty()) {
            cities.filter { it.contains(query, ignoreCase = true) }
        } else {
            cities.take(20) // Mostrar apenas as 20 primeiras inicialmente
        }

        val adapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, filteredCities)
        autoCompleteTextView.setAdapter(adapter)

        if (filteredCities.isNotEmpty() && query.isNotEmpty()) {
            autoCompleteTextView.showDropDown()
        }
    }

    /**
     * Lista expandida de cidades populares (fallback offline)
     */
    private fun getPopularCities(): List<String> {
        return listOf(
            // Principais destinos mundiais
            "New York, USA", "Los Angeles, USA", "Chicago, USA", "San Francisco, USA",
            "London, UK", "Paris, France", "Berlin, Germany", "Madrid, Spain",
            "Rome, Italy", "Barcelona, Spain", "Amsterdam, Netherlands", "Vienna, Austria",
            "Prague, Czech Republic", "Budapest, Hungary", "Stockholm, Sweden", "Copenhagen, Denmark",
            "Tokyo, Japan", "Seoul, South Korea", "Bangkok, Thailand", "Singapore",
            "Hong Kong", "Mumbai, India", "Delhi, India", "Shanghai, China", "Beijing, China",
            "Dubai, UAE", "Sydney, Australia", "Melbourne, Australia", "Toronto, Canada",
            "Montreal, Canada", "Vancouver, Canada",

            // Brasil - Principais cidades
            "São Paulo, Brasil", "Rio de Janeiro, Brasil", "Brasília, Brasil",
            "Salvador, Brasil", "Fortaleza, Brasil", "Belo Horizonte, Brasil",
            "Manaus, Brasil", "Curitiba, Brasil", "Recife, Brasil", "Goiânia, Brasil",
            "Belém, Brasil", "Porto Alegre, Brasil", "Guarulhos, Brasil",
            "Campinas, Brasil", "São Luís, Brasil", "Florianópolis, Brasil",

            // Brasil - Destinos turísticos
            "Gramado, Brasil", "Canela, Brasil", "Campos do Jordão, Brasil",
            "Monte Verde, Brasil", "Búzios, Brasil", "Angra dos Reis, Brasil",
            "Paraty, Brasil", "Petrópolis, Brasil", "Ouro Preto, Brasil",
            "Tiradentes, Brasil", "Bonito, Brasil", "Fernando de Noronha, Brasil",
            "Jericoacoara, Brasil", "Morro de São Paulo, Brasil", "Porto de Galinhas, Brasil",
            "Maragogi, Brasil", "Caldas Novas, Brasil", "Foz do Iguaçu, Brasil",
            "Balneário Camboriú, Brasil", "Blumenau, Brasil",

            // América Latina
            "Mexico City, Mexico", "Buenos Aires, Argentina", "Lima, Peru",
            "Bogotá, Colombia", "Santiago, Chile", "Caracas, Venezuela",
            "Quito, Ecuador", "La Paz, Bolivia", "Montevideo, Uruguay",
            "Asunción, Paraguay", "Guatemala City, Guatemala", "San José, Costa Rica",
            "Panama City, Panama", "Havana, Cuba",

            // Outros destinos importantes
            "Cairo, Egypt", "Istanbul, Turkey", "Moscow, Russia", "Athens, Greece",
            "Lisbon, Portugal", "Zurich, Switzerland", "Geneva, Switzerland",
            "Brussels, Belgium", "Dublin, Ireland", "Oslo, Norway", "Helsinki, Finland",
            "Tel Aviv, Israel", "Cape Town, South Africa", "Marrakech, Morocco",
            "Casablanca, Morocco", "Nairobi, Kenya", "Lagos, Nigeria"
        ).sorted()
    }

    /**
     * Valida se uma string é um valor monetário válido
     */
    fun isValidCurrency(value: String): Boolean {
        // Usar replace simples ao invés de regex
        val cleanValue = value
            .replace("R$", "")
            .replace("€", "")
            .replace("£", "")
            .replace("¥", "")
            .replace("₹", "")
            .replace("¢", "")
            .replace("$", "")
            .replace(" ", "")
            .replace(".", "")
            .replace(",", "")

        return cleanValue.isNotEmpty() && cleanValue.toDoubleOrNull() != null && cleanValue.toDouble() > 0
    }

    /**
     * Extrai valor numérico de uma string formatada como moeda
     */
    fun extractCurrencyValue(formattedValue: String): Double {
        // Usar replace simples ao invés de regex
        val cleanValue = formattedValue
            .replace("R$", "")
            .replace("€", "")
            .replace("£", "")
            .replace("¥", "")
            .replace("₹", "")
            .replace("¢", "")
            .replace("$", "")
            .replace(" ", "")
            .replace(".", "")
            .replace(",", ".")

        return cleanValue.toDoubleOrNull() ?: 0.0
    }

    /**
     * Formata um valor para moeda local
     */
    fun formatCurrency(value: Double): String {
        return currencyFormat.format(value)
    }

    /**
     * Valida se uma data está no formato correto
     */
    fun isValidDate(date: String): Boolean {
        return try {
            dateFormat.parse(date)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Valida se um horário está no formato correto
     */
    fun isValidTime(time: String): Boolean {
        return try {
            timeFormat.parse(time)
            true
        } catch (e: Exception) {
            false
        }
    }
}