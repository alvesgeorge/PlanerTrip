package com.george.viagemplanejada

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class CityApiManager(private val context: Context) {

    // COLE SUA API KEY AQUI
    private val API_KEY = "7d656ce512mshc5071404ad21403p1d8efbjsn221175571f9e"

    // URL base da API
    private val BASE_URL = "https://wft-geo-db.p.rapidapi.com/v1/geo/"

    // Controle simples de rate limiting
    private var lastRequestTime = 0L
    private val MIN_REQUEST_INTERVAL = 2000L // 2 segundos entre requisições

    private val retrofit: Retrofit by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val apiService: CityApiService by lazy {
        retrofit.create(CityApiService::class.java)
    }

    /**
     * Busca cidades com controle de rate limiting simplificado
     */
    suspend fun searchCities(query: String): List<City> {
        return withContext(Dispatchers.IO) {
            try {
                // Se query muito pequena, usar dados offline
                if (query.length < 2) {
                    return@withContext getOfflineCities(query)
                }

                // Se não tem API key configurada, usar dados offline
                if (API_KEY == "SUA_API_KEY_AQUI") {
                    Log.w("CityApiManager", "API Key não configurada, usando dados offline")
                    return@withContext getOfflineCities(query)
                }

                // Controle simples de rate limiting
                val currentTime = System.currentTimeMillis()
                val timeSinceLastRequest = currentTime - lastRequestTime

                if (timeSinceLastRequest < MIN_REQUEST_INTERVAL) {
                    Log.d("CityApiManager", "Rate limit ativo, usando dados offline")
                    return@withContext getOfflineCities(query)
                }

                Log.d("CityApiManager", "Buscando cidades para: $query")
                lastRequestTime = currentTime

                val response = apiService.searchCities(
                    namePrefix = query,
                    limit = 8, // Reduzir ainda mais o limite
                    minPopulation = 100000, // Aumentar população mínima
                    apiKey = API_KEY
                )

                when (response.code()) {
                    200 -> {
                        val cities = response.body()?.data ?: emptyList()
                        Log.d("CityApiManager", "Encontradas ${cities.size} cidades via API")

                        // Combinar resultados da API com dados offline
                        val offlineCities = getOfflineCities(query)
                        val combinedCities = (cities + offlineCities).distinctBy { it.name + it.country }.take(15)

                        combinedCities
                    }
                    429 -> {
                        Log.w("CityApiManager", "Rate limit da API atingido (429)")
                        getOfflineCities(query)
                    }
                    403 -> {
                        Log.e("CityApiManager", "API key inválida ou expirada (403)")
                        getOfflineCities(query)
                    }
                    else -> {
                        Log.e("CityApiManager", "Erro na API: ${response.code()}")
                        getOfflineCities(query)
                    }
                }
            } catch (e: Exception) {
                Log.e("CityApiManager", "Exceção ao buscar cidades: ${e.message}")
                getOfflineCities(query)
            }
        }
    }

    /**
     * Dados offline otimizados
     */
    private fun getOfflineCities(query: String): List<City> {
        val offlineCities = listOf(
            // Top 50 cidades mundiais mais buscadas
            City(1, "New York", "United States", "US", "New York", "NY", 8175133),
            City(2, "Los Angeles", "United States", "US", "California", "CA", 3971883),
            City(3, "London", "United Kingdom", "GB", "England", "ENG", 8982000),
            City(4, "Paris", "France", "FR", "Île-de-France", "IDF", 2161000),
            City(5, "Tokyo", "Japan", "JP", "Tokyo", "13", 13929286),
            City(6, "Sydney", "Australia", "AU", "New South Wales", "NSW", 5312163),
            City(7, "Toronto", "Canada", "CA", "Ontario", "ON", 2731571),
            City(8, "Berlin", "Germany", "DE", "Berlin", "BE", 3669491),
            City(9, "Rome", "Italy", "IT", "Lazio", "62", 2873000),
            City(10, "Madrid", "Spain", "ES", "Madrid", "MD", 3223000),
            City(11, "Barcelona", "Spain", "ES", "Catalonia", "CT", 1620343),
            City(12, "Amsterdam", "Netherlands", "NL", "North Holland", "NH", 821752),
            City(13, "Vienna", "Austria", "AT", "Vienna", "9", 1911000),
            City(14, "Prague", "Czech Republic", "CZ", "Prague", "10", 1280000),
            City(15, "Budapest", "Hungary", "HU", "Budapest", "BU", 1752000),
            City(16, "Dubai", "United Arab Emirates", "AE", "Dubai", "DU", 3331420),
            City(17, "Singapore", "Singapore", "SG", "", "", 5685807),
            City(18, "Hong Kong", "Hong Kong", "HK", "", "", 7496981),
            City(19, "Seoul", "South Korea", "KR", "Seoul", "11", 9720846),
            City(20, "Bangkok", "Thailand", "TH", "Bangkok", "10", 8305218),

            // Brasil - Top 30
            City(21, "São Paulo", "Brazil", "BR", "São Paulo", "SP", 12325232),
            City(22, "Rio de Janeiro", "Brazil", "BR", "Rio de Janeiro", "RJ", 6748000),
            City(23, "Brasília", "Brazil", "BR", "Federal District", "DF", 3055149),
            City(24, "Salvador", "Brazil", "BR", "Bahia", "BA", 2886698),
            City(25, "Fortaleza", "Brazil", "BR", "Ceará", "CE", 2669342),
            City(26, "Belo Horizonte", "Brazil", "BR", "Minas Gerais", "MG", 2521564),
            City(27, "Manaus", "Brazil", "BR", "Amazonas", "AM", 2219580),
            City(28, "Curitiba", "Brazil", "BR", "Paraná", "PR", 1948626),
            City(29, "Recife", "Brazil", "BR", "Pernambuco", "PE", 1653461),
            City(30, "Goiânia", "Brazil", "BR", "Goiás", "GO", 1536097),
            City(31, "Belém", "Brazil", "BR", "Pará", "PA", 1499641),
            City(32, "Porto Alegre", "Brazil", "BR", "Rio Grande do Sul", "RS", 1488252),
            City(33, "Florianópolis", "Brazil", "BR", "Santa Catarina", "SC", 508826),
            City(34, "Gramado", "Brazil", "BR", "Rio Grande do Sul", "RS", 36000),
            City(35, "Campos do Jordão", "Brazil", "BR", "São Paulo", "SP", 52000),
            City(36, "Búzios", "Brazil", "BR", "Rio de Janeiro", "RJ", 33000),
            City(37, "Paraty", "Brazil", "BR", "Rio de Janeiro", "RJ", 43000),
            City(38, "Ouro Preto", "Brazil", "BR", "Minas Gerais", "MG", 74000),
            City(39, "Bonito", "Brazil", "BR", "Mato Grosso do Sul", "MS", 22000),
            City(40, "Fernando de Noronha", "Brazil", "BR", "Pernambuco", "PE", 3000),
            City(41, "Jericoacoara", "Brazil", "BR", "Ceará", "CE", 2000),
            City(42, "Foz do Iguaçu", "Brazil", "BR", "Paraná", "PR", 258823),
            City(43, "Balneário Camboriú", "Brazil", "BR", "Santa Catarina", "SC", 138732),
            City(44, "Blumenau", "Brazil", "BR", "Santa Catarina", "SC", 361855),
            City(45, "Canela", "Brazil", "BR", "Rio Grande do Sul", "RS", 42000),
            City(46, "Monte Verde", "Brazil", "BR", "Minas Gerais", "MG", 8000),
            City(47, "Angra dos Reis", "Brazil", "BR", "Rio de Janeiro", "RJ", 200000),
            City(48, "Petrópolis", "Brazil", "BR", "Rio de Janeiro", "RJ", 306678),
            City(49, "Tiradentes", "Brazil", "BR", "Minas Gerais", "MG", 7000),
            City(50, "Caldas Novas", "Brazil", "BR", "Goiás", "GO", 81000)
        )

        return offlineCities.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.country.contains(query, ignoreCase = true) ||
                    it.regionCode?.contains(query, ignoreCase = true) == true
        }.take(10)
    }
}