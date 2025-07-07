package com.george.viagemplanejada

import android.content.Context
import android.content.SharedPreferences

class TripManager(private val context: Context) {

    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("trips", Context.MODE_PRIVATE)

    // Salvar viagem
    fun saveTrip(trip: Trip): Boolean {
        return try {
            val existingTrips = getAllTrips().toMutableList()
            existingTrips.add(trip)

            val tripsString = existingTrips.joinToString(";") { it.toSaveString() }

            sharedPref.edit()
                .putString("trip_list", tripsString)
                .apply()

            true
        } catch (e: Exception) {
            false
        }
    }

    // Editar viagem existente
    fun editTrip(oldTrip: Trip, newTrip: Trip): Boolean {
        return try {
            val trips = getAllTrips().toMutableList()
            val index = trips.indexOfFirst {
                it.name == oldTrip.name && it.destination == oldTrip.destination
            }

            if (index != -1) {
                trips[index] = newTrip

                val tripsString = trips.joinToString(";") { it.toSaveString() }

                sharedPref.edit()
                    .putString("trip_list", tripsString)
                    .apply()

                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    // Obter todas as viagens
    fun getAllTrips(): List<Trip> {
        val tripsString = sharedPref.getString("trip_list", "") ?: ""

        return if (tripsString.isEmpty()) {
            emptyList()
        } else {
            tripsString.split(";")
                .mapNotNull { Trip.fromSaveString(it) }
        }
    }

    // Buscar viagens por termo
    fun searchTrips(searchTerm: String): List<Trip> {
        val allTrips = getAllTrips()

        return if (searchTerm.isEmpty()) {
            allTrips
        } else {
            allTrips.filter { trip ->
                trip.name.contains(searchTerm, ignoreCase = true) ||
                        trip.destination.contains(searchTerm, ignoreCase = true) ||
                        trip.notes.contains(searchTerm, ignoreCase = true)
            }
        }
    }

    // Deletar viagem
    fun deleteTrip(tripToDelete: Trip): Boolean {
        return try {
            val trips = getAllTrips().toMutableList()
            trips.removeAll { it.name == tripToDelete.name && it.destination == tripToDelete.destination }

            val tripsString = trips.joinToString(";") { it.toSaveString() }

            sharedPref.edit()
                .putString("trip_list", tripsString)
                .apply()

            true
        } catch (e: Exception) {
            false
        }
    }

    // Obter estatísticas
    fun getStats(): TripStats {
        val trips = getAllTrips()

        val totalBudget = trips.sumOf { trip ->
            trip.budget.replace("R$", "").replace(".", "").replace(",", ".")
                .replace(" ", "").toDoubleOrNull() ?: 0.0
        }

        return TripStats(
            totalTrips = trips.size,
            totalBudget = totalBudget,
            destinations = trips.map { it.destination }.distinct().size
        )
    }
}

// Classe para estatísticas
data class TripStats(
    val totalTrips: Int,
    val totalBudget: Double,
    val destinations: Int
)