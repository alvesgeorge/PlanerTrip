package com.george.viagemplanejada.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.george.viagemplanejada.*

class DataManager private constructor(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "viagem_planejada_data"
        private const val KEY_TRIPS = "trips"
        private const val KEY_CURRENT_TRIP = "current_trip_id"
        private const val KEY_EXPENSES = "expenses_"
        private const val KEY_EVENTS = "events_"
        private const val KEY_PLACES = "places_"
        private const val KEY_BUDGET = "budget_"
        private const val KEY_TASKS = "tasks_"

        @Volatile
        private var INSTANCE: DataManager? = null

        fun getInstance(context: Context): DataManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DataManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // ==================== TRIPS ====================

    fun saveTrip(trip: Trip) {
        val trips = getAllTrips().toMutableList()
        val existingIndex = trips.indexOfFirst { it.id == trip.id }

        if (existingIndex >= 0) {
            trips[existingIndex] = trip
        } else {
            trips.add(trip)
        }

        val json = gson.toJson(trips)
        sharedPreferences.edit().putString(KEY_TRIPS, json).apply()
    }

    fun getAllTrips(): List<Trip> {
        val json = sharedPreferences.getString(KEY_TRIPS, null)
        return if (json != null) {
            val type = object : TypeToken<List<Trip>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun getTripById(tripId: String): Trip? {
        return getAllTrips().find { it.id == tripId }
    }

    fun deleteTrip(tripId: String) {
        val trips = getAllTrips().toMutableList()
        trips.removeAll { it.id == tripId }

        val json = gson.toJson(trips)
        sharedPreferences.edit().putString(KEY_TRIPS, json).apply()

        // Limpar dados relacionados
        clearTripData(tripId)
    }

    fun setCurrentTrip(tripId: String) {
        sharedPreferences.edit().putString(KEY_CURRENT_TRIP, tripId).apply()
    }

    fun getCurrentTripId(): String? {
        return sharedPreferences.getString(KEY_CURRENT_TRIP, null)
    }

    fun getCurrentTrip(): Trip? {
        val tripId = getCurrentTripId()
        return if (tripId != null) getTripById(tripId) else null
    }

    // ==================== EXPENSES ====================

    fun saveExpense(tripId: String, expense: ExpenseItem) {
        val expenses = getExpenses(tripId).toMutableList()
        val existingIndex = expenses.indexOfFirst { it.id == expense.id }

        if (existingIndex >= 0) {
            expenses[existingIndex] = expense
        } else {
            expenses.add(expense)
        }

        val json = gson.toJson(expenses)
        sharedPreferences.edit().putString(KEY_EXPENSES + tripId, json).apply()
    }

    fun getExpenses(tripId: String): List<ExpenseItem> {
        val json = sharedPreferences.getString(KEY_EXPENSES + tripId, null)
        return if (json != null) {
            val type = object : TypeToken<List<ExpenseItem>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun deleteExpense(tripId: String, expenseId: String) {
        val expenses = getExpenses(tripId).toMutableList()
        expenses.removeAll { it.id == expenseId }

        val json = gson.toJson(expenses)
        sharedPreferences.edit().putString(KEY_EXPENSES + tripId, json).apply()
    }

    fun saveBudget(tripId: String, budget: Double) {
        sharedPreferences.edit().putFloat(KEY_BUDGET + tripId, budget.toFloat()).apply()
    }

    fun getBudget(tripId: String): Double {
        return sharedPreferences.getFloat(KEY_BUDGET + tripId, 5000f).toDouble()
    }

    // ==================== EVENTS ====================

    fun saveEvent(tripId: String, event: EventItem) {
        val events = getEvents(tripId).toMutableList()
        val existingIndex = events.indexOfFirst { it.id == event.id }

        if (existingIndex >= 0) {
            events[existingIndex] = event
        } else {
            events.add(event)
        }

        val json = gson.toJson(events)
        sharedPreferences.edit().putString(KEY_EVENTS + tripId, json).apply()
    }

    fun getEvents(tripId: String): List<EventItem> {
        val json = sharedPreferences.getString(KEY_EVENTS + tripId, null)
        return if (json != null) {
            val type = object : TypeToken<List<EventItem>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun deleteEvent(tripId: String, eventId: String) {
        val events = getEvents(tripId).toMutableList()
        events.removeAll { it.id == eventId }

        val json = gson.toJson(events)
        sharedPreferences.edit().putString(KEY_EVENTS + tripId, json).apply()
    }

    // ==================== PLACES ====================

    fun savePlace(tripId: String, place: PlaceItem) {
        val places = getPlaces(tripId).toMutableList()
        val existingIndex = places.indexOfFirst { it.id == place.id }

        if (existingIndex >= 0) {
            places[existingIndex] = place
        } else {
            places.add(place)
        }

        val json = gson.toJson(places)
        sharedPreferences.edit().putString(KEY_PLACES + tripId, json).apply()
    }

    fun getPlaces(tripId: String): List<PlaceItem> {
        val json = sharedPreferences.getString(KEY_PLACES + tripId, null)
        return if (json != null) {
            val type = object : TypeToken<List<PlaceItem>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun deletePlace(tripId: String, placeId: String) {
        val places = getPlaces(tripId).toMutableList()
        places.removeAll { it.id == placeId }

        val json = gson.toJson(places)
        sharedPreferences.edit().putString(KEY_PLACES + tripId, json).apply()
    }

    // ==================== TASKS ====================

    fun saveTasks(tripId: String, tasks: List<TaskItem>) {
        val json = gson.toJson(tasks)
        sharedPreferences.edit().putString(KEY_TASKS + tripId, json).apply()
    }

    fun getTasks(tripId: String): List<TaskItem> {
        val json = sharedPreferences.getString(KEY_TASKS + tripId, null)
        return if (json != null) {
            val type = object : TypeToken<List<TaskItem>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    // ==================== UTILITIES ====================

    private fun clearTripData(tripId: String) {
        sharedPreferences.edit()
            .remove(KEY_EXPENSES + tripId)
            .remove(KEY_EVENTS + tripId)
            .remove(KEY_PLACES + tripId)
            .remove(KEY_BUDGET + tripId)
            .remove(KEY_TASKS + tripId)
            .apply()
    }

    fun exportAllData(): String {
        val allData = mutableMapOf<String, Any>()

        // Export trips
        allData["trips"] = getAllTrips()

        // Export all trip data
        getAllTrips().forEach { trip ->
            allData["expenses_${trip.id}"] = getExpenses(trip.id)
            allData["events_${trip.id}"] = getEvents(trip.id)
            allData["places_${trip.id}"] = getPlaces(trip.id)
            allData["budget_${trip.id}"] = getBudget(trip.id)
            allData["tasks_${trip.id}"] = getTasks(trip.id)
        }

        return gson.toJson(allData)
    }

    fun clearAllData() {
        sharedPreferences.edit().clear().apply()
    }

    fun generateId(): String {
        return "id_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}