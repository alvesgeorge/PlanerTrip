package com.george.viagemplanejada

data class Trip(
    val name: String,
    val destination: String,
    val startDate: String,
    val endDate: String,
    val budget: String,
    val notes: String,
    // APENAS UM CAMPO NOVO para cidade selecionada
    val selectedCity: City? = null
) {
    // Função para converter em string para salvar - ATUALIZADA
    fun toSaveString(): String {
        val cityData = if (selectedCity != null) {
            "${selectedCity.name}|${selectedCity.country}|${selectedCity.countryCode}"
        } else {
            "||"
        }
        return "$name|$destination|$startDate|$endDate|$budget|$notes|$cityData"
    }

    // Função para criar Trip a partir de string salva - ATUALIZADA
    companion object {
        fun fromSaveString(saveString: String): Trip? {
            val parts = saveString.split("|")
            return when (parts.size) {
                // Compatibilidade com formato antigo (6 campos)
                6 -> Trip(
                    name = parts[0],
                    destination = parts[1],
                    startDate = parts[2],
                    endDate = parts[3],
                    budget = parts[4],
                    notes = parts[5]
                )
                // Novo formato com cidade (9 campos)
                9 -> {
                    val city = if (parts[6].isNotEmpty() && parts[7].isNotEmpty()) {
                        City(
                            id = 0,
                            name = parts[6],
                            country = parts[7],
                            countryCode = parts[8],
                            region = "",
                            regionCode = "",
                            population = 0
                        )
                    } else null

                    Trip(
                        name = parts[0],
                        destination = parts[1],
                        startDate = parts[2],
                        endDate = parts[3],
                        budget = parts[4],
                        notes = parts[5],
                        selectedCity = city
                    )
                }
                else -> null
            }
        }
    }

    // NOVA função para verificar se tem cidade
    fun hasSelectedCity(): Boolean {
        return selectedCity != null
    }

    // NOVA função para obter flag do país
    fun getCityFlag(): String {
        return when (selectedCity?.countryCode?.lowercase()) {
            "br" -> "🇧🇷"
            "us" -> "🇺🇸"
            "gb" -> "🇬🇧"
            "fr" -> "🇫🇷"
            "it" -> "🇮🇹"
            "es" -> "🇪🇸"
            "de" -> "��🇪"
            "ca" -> "🇨🇦"
            "au" -> "��🇺"
            "jp" -> "🇯🇵"
            "kr" -> "��🇷"
            "th" -> "🇹🇭"
            "ae" -> "🇦🇪"
            "sg" -> "🇸🇬"
            "hk" -> "🇭🇰"
            "nl" -> "🇳🇱"
            "at" -> "🇦🇹"
            "cz" -> "🇨🇿"
            "hu" -> "🇭🇺"
            else -> "🌍"
        }
    }

    // NOVA função para obter localização formatada
    fun getFormattedLocation(): String {
        return if (hasSelectedCity()) {
            "${getCityFlag()} ${selectedCity!!.name}, ${selectedCity!!.country}"
        } else {
            "📍 $destination"
        }
    }

    // Funções existentes - mantidas
    fun getIcon(): String {
        return when {
            destination.contains("praia", ignoreCase = true) ||
                    destination.contains("rio", ignoreCase = true) ||
                    destination.contains("salvador", ignoreCase = true) -> "🏖️"

            destination.contains("montanha", ignoreCase = true) ||
                    destination.contains("serra", ignoreCase = true) ||
                    destination.contains("gramado", ignoreCase = true) -> "🏔️"

            destination.contains("históric", ignoreCase = true) ||
                    destination.contains("ouro preto", ignoreCase = true) ||
                    destination.contains("tiradentes", ignoreCase = true) -> "🏛️"

            destination.contains("campo", ignoreCase = true) ||
                    destination.contains("fazenda", ignoreCase = true) -> "🌾"

            destination.contains("cidade", ignoreCase = true) ||
                    destination.contains("são paulo", ignoreCase = true) -> "🏙️"

            else -> "✈️"
        }
    }

    fun getDuration(): String {
        return if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
            "De $startDate até $endDate"
        } else if (startDate.isNotEmpty()) {
            "Início: $startDate"
        } else {
            "Datas a definir"
        }
    }
}