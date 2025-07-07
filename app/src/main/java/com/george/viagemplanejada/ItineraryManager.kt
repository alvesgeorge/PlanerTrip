package com.george.viagemplanejada

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

class ItineraryManager(private val context: Context) {

    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("itinerary", Context.MODE_PRIVATE)

    // Salvar item do roteiro
    fun saveItinerary(itinerary: Itinerary): Boolean {
        return try {
            val existingItems = getAllItineraries().toMutableList()
            existingItems.add(itinerary)

            val itinerariesString = existingItems.joinToString(";") { it.toSaveString() }

            sharedPref.edit()
                .putString("itinerary_list", itinerariesString)
                .apply()

            true
        } catch (e: Exception) {
            false
        }
    }

    // Criar novo item do roteiro
    fun createItinerary(
        tripName: String,
        day: Int,
        date: String,
        title: String,
        description: String,
        startTime: String,
        endTime: String,
        location: String,
        category: ItineraryCategory,
        estimatedCost: String,
        notes: String
    ): Boolean {
        val itinerary = Itinerary(
            id = UUID.randomUUID().toString(),
            tripName = tripName,
            day = day,
            date = date,
            title = title,
            description = description,
            startTime = startTime,
            endTime = endTime,
            location = location,
            category = category,
            estimatedCost = estimatedCost,
            notes = notes,
            isCompleted = false
        )

        return saveItinerary(itinerary)
    }

    // Obter todos os itinerários
    fun getAllItineraries(): List<Itinerary> {
        val itinerariesString = sharedPref.getString("itinerary_list", "") ?: ""

        return if (itinerariesString.isEmpty()) {
            emptyList()
        } else {
            itinerariesString.split(";")
                .mapNotNull { Itinerary.fromSaveString(it) }
        }
    }

    // Obter itinerários por viagem
    fun getItinerariesByTrip(tripName: String): List<Itinerary> {
        return getAllItineraries()
            .filter { it.tripName == tripName }
            .sortedWith(compareBy<Itinerary> { it.day }.thenBy { it.startTime })
    }

    // Obter itinerários por viagem e dia
    fun getItinerariesByTripAndDay(tripName: String, day: Int): List<Itinerary> {
        return getAllItineraries()
            .filter { it.tripName == tripName && it.day == day }
            .sortedBy { it.startTime }
    }

    // Marcar item como concluído
    fun toggleItineraryCompletion(itineraryId: String): Boolean {
        return try {
            val itineraries = getAllItineraries().toMutableList()
            val itineraryIndex = itineraries.indexOfFirst { it.id == itineraryId }

            if (itineraryIndex != -1) {
                val itinerary = itineraries[itineraryIndex]
                itineraries[itineraryIndex] = itinerary.copy(isCompleted = !itinerary.isCompleted)

                val itinerariesString = itineraries.joinToString(";") { it.toSaveString() }

                sharedPref.edit()
                    .putString("itinerary_list", itinerariesString)
                    .apply()

                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    // Deletar item do roteiro
    fun deleteItinerary(itineraryId: String): Boolean {
        return try {
            val itineraries = getAllItineraries().toMutableList()
            itineraries.removeAll { it.id == itineraryId }

            val itinerariesString = itineraries.joinToString(";") { it.toSaveString() }

            sharedPref.edit()
                .putString("itinerary_list", itinerariesString)
                .apply()

            true
        } catch (e: Exception) {
            false
        }
    }

    // Obter estatísticas do roteiro
    fun getItineraryStats(tripName: String): ItineraryStats {
        val itineraries = getItinerariesByTrip(tripName)
        val totalDays = itineraries.maxOfOrNull { it.day } ?: 0
        val totalItems = itineraries.size
        val completedItems = itineraries.count { it.isCompleted }

        val totalCost = itineraries.sumOf { itinerary ->
            itinerary.estimatedCost.replace("R$", "").replace(".", "").replace(",", ".")
                .replace(" ", "").toDoubleOrNull() ?: 0.0
        }

        return ItineraryStats(
            totalDays = totalDays,
            totalItems = totalItems,
            completedItems = completedItems,
            totalCost = totalCost
        )
    }

    // Obter próximo número de dia para uma viagem
    fun getNextDayNumber(tripName: String): Int {
        val itineraries = getItinerariesByTrip(tripName)
        return (itineraries.maxOfOrNull { it.day } ?: 0) + 1
    }
}

data class ItineraryStats(
    val totalDays: Int,
    val totalItems: Int,
    val completedItems: Int,
    val totalCost: Double
)