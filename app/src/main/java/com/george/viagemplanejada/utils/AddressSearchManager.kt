// ADICIONAR este método ao AddressSearchManager existente:

fun searchAddressesInCity(
    query: String,
    city: City,
    callback: (List<AddressSuggestion>) -> Unit
) {
    if (query.length < 3) {
        callback(emptyList())
        return
    }

    scope.launch {
        val suggestions = mutableListOf<AddressSuggestion>()

        try {
            // 1. Buscar pontos turísticos da cidade específica
            val localResults = searchPlacesInSpecificCity(query, city)
            suggestions.addAll(localResults)

            // 2. CEP brasileiro (se for Brasil)
            if (city.countryCode == "BR" && isBrazilianCEP(query)) {
                val cepResults = searchViaCEP(query)
                suggestions.addAll(cepResults)
            }

            // 3. Busca no Nominatim FILTRADA por cidade
            if (suggestions.size < 3) {
                val nominatimResults = searchNominatimInSpecificCity(query, city)
                suggestions.addAll(nominatimResults)
            }

            val uniqueSuggestions = suggestions
                .distinctBy { it.address }
                .take(8)

            withContext(Dispatchers.Main) {
                callback(uniqueSuggestions)
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                callback(emptyList())
            }
        }
    }
}

private fun searchPlacesInSpecificCity(query: String, city: City): List<AddressSuggestion> {
    return famousPlaces
        .filter { place ->
            place.contains(query, ignoreCase = true) &&
                    (place.contains(city.name, ignoreCase = true) ||
                            place.contains(city.country, ignoreCase = true))
        }
        .map { place ->
            val parts = place.split(" - ")
            AddressSuggestion(
                name = parts[0],
                address = place,
                category = detectGlobalCategory(parts[0]),
                isLocal = true,
                country = "${getCityFlag(city.countryCode)} ${city.country}"
            )
        }
}

private suspend fun searchNominatimInSpecificCity(query: String, city: City): List<AddressSuggestion> {
    return withContext(Dispatchers.IO) {
        try {
            delay(1000)

            // Busca FILTRADA por cidade
            val searchQuery = "$query, ${city.name}, ${city.country}"
            val encodedQuery = URLEncoder.encode(searchQuery, "UTF-8")
            val url = URL("https://nominatim.openstreetmap.org/search?format=json&q=$encodedQuery&limit=5&addressdetails=1")

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "ViagemPlanejada/1.0")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val response = connection.inputStream.bufferedReader().readText()
            val jsonArray = JSONArray(response)

            val results = mutableListOf<AddressSuggestion>()

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val displayName = item.getString("display_name")
                val name = item.optString("name", "Local")

                // Verificar se realmente está na cidade
                if (displayName.contains(city.name, ignoreCase = true)) {
                    results.add(
                        AddressSuggestion(
                            name = name,
                            address = displayName,
                            category = detectCategoryFromOSM(item),
                            isLocal = false,
                            country = "${getCityFlag(city.countryCode)} ${city.country}"
                        )
                    )
                }
            }

            results
        } catch (e: Exception) {
            emptyList()
        }
    }
}

private fun getCityFlag(countryCode: String): String {
    return when (countryCode.lowercase()) {
        "br" -> "🇧🇷"
        "us" -> "🇺🇸"
        "gb" -> "🇬🇧"
        "fr" -> "��🇷"
        "it" -> "🇮🇹"
        "es" -> "🇪🇸"
        "de" -> "🇩🇪"
        "ca" -> "🇨🇦"
        "au" -> "��🇺"
        "jp" -> "🇯🇵"
        else -> "🌍"
    }
}