package com.george.viagemplanejada

import com.google.gson.annotations.SerializedName

// Modelo para resposta da API GeoDB Cities
data class CityResponse(
    @SerializedName("data")
    val data: List<City>
)

data class City(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("country")
    val country: String,
    @SerializedName("countryCode")
    val countryCode: String,
    @SerializedName("region")
    val region: String? = null,
    @SerializedName("regionCode")
    val regionCode: String? = null,
    @SerializedName("population")
    val population: Int? = null
) {
    fun getDisplayName(): String {
        return if (regionCode != null && regionCode.isNotEmpty()) {
            "$name, $regionCode, $country"
        } else {
            "$name, $country"
        }
    }
}