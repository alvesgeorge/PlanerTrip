package com.george.viagemplanejada

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface CityApiService {

    @GET("cities")
    suspend fun searchCities(
        @Query("namePrefix") namePrefix: String,
        @Query("limit") limit: Int = 10,
        @Query("minPopulation") minPopulation: Int = 10000,
        @Query("sort") sort: String = "-population",
        @Header("X-RapidAPI-Key") apiKey: String,
        @Header("X-RapidAPI-Host") host: String = "wft-geo-db.p.rapidapi.com"
    ): Response<CityResponse>
}