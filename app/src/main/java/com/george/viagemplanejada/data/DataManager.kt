package com.george.viagemplanejada.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class DataManager private constructor(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("viagem_planejada_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        @Volatile
        private var INSTANCE: DataManager? = null

        fun getInstance(context: Context): DataManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DataManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // ========== MÉTODOS UTILITÁRIOS ==========

    fun generateId(): String {
        return UUID.randomUUID().toString()
    }

    fun getCurrentTripId(): String? {
        val currentTripId = sharedPreferences.getString("current_trip_id", null)
        return if (currentTripId != null) {
            currentTripId
        } else {
            val firstTrip = getAllTrips().firstOrNull()
            if (firstTrip != null) {
                setCurrentTrip(firstTrip.id)
                firstTrip.id
            } else {
                null
            }
        }
    }

    // ========== MÉTODOS PARA TRIPS ==========

    fun saveTrip(trip: TripItem) {
        val trips = getAllTrips().toMutableList()
        val existingIndex = trips.indexOfFirst { it.id == trip.id }

        if (existingIndex != -1) {
            trips[existingIndex] = trip
        } else {
            trips.add(trip)
        }

        val json = gson.toJson(trips)
        sharedPreferences.edit().putString("trips", json).apply()
    }

    fun getAllTrips(): List<TripItem> {
        val json = sharedPreferences.getString("trips", null) ?: return emptyList()
        val type = object : TypeToken<List<TripItem>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun getTripById(tripId: String): TripItem? {
        return getAllTrips().find { it.id == tripId }
    }

    fun deleteTrip(tripId: String) {
        val trips = getAllTrips().toMutableList()
        trips.removeAll { it.id == tripId }

        val json = gson.toJson(trips)
        sharedPreferences.edit().putString("trips", json).apply()

        deletePlacesForTrip(tripId)
        deleteEventsForTrip(tripId)
        deleteExpensesForTrip(tripId)
        deleteBudgetForTrip(tripId)
    }

    fun getCurrentTrip(): TripItem? {
        val currentTripId = getCurrentTripId()
        return if (currentTripId != null) {
            getTripById(currentTripId)
        } else {
            getAllTrips().firstOrNull()
        }
    }

    fun setCurrentTrip(tripId: String) {
        sharedPreferences.edit().putString("current_trip_id", tripId).apply()
    }

    // ========== MÉTODOS PARA PLACES ==========

    fun savePlace(tripId: String, place: PlaceItem) {
        val key = "places_$tripId"
        val places = getPlaces(tripId).toMutableList()
        val existingIndex = places.indexOfFirst { it.id == place.id }

        if (existingIndex != -1) {
            places[existingIndex] = place
        } else {
            places.add(place)
        }

        val json = gson.toJson(places)
        sharedPreferences.edit().putString(key, json).apply()
    }

    fun getPlaces(tripId: String): List<PlaceItem> {
        val key = "places_$tripId"
        val json = sharedPreferences.getString(key, null) ?: return emptyList()
        val type = object : TypeToken<List<PlaceItem>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun deletePlace(tripId: String, placeId: String) {
        val key = "places_$tripId"
        val places = getPlaces(tripId).toMutableList()
        places.removeAll { it.id == placeId }

        val json = gson.toJson(places)
        sharedPreferences.edit().putString(key, json).apply()
    }

    private fun deletePlacesForTrip(tripId: String) {
        val key = "places_$tripId"
        sharedPreferences.edit().remove(key).apply()
    }

    // ========== MÉTODOS PARA EVENTS (COMPATIBILIDADE) ==========

    fun saveEvent(tripId: String, event: com.george.viagemplanejada.EventItem) {
        val key = "events_$tripId"
        val events = getEventsCompat(tripId).toMutableList()
        val existingIndex = events.indexOfFirst { it.id == event.id }

        if (existingIndex != -1) {
            events[existingIndex] = event
        } else {
            events.add(event)
        }

        val json = gson.toJson(events)
        sharedPreferences.edit().putString(key, json).apply()
    }

    fun getEvents(tripId: String): List<com.george.viagemplanejada.EventItem> {
        return getEventsCompat(tripId)
    }

    private fun getEventsCompat(tripId: String): List<com.george.viagemplanejada.EventItem> {
        val key = "events_$tripId"
        val json = sharedPreferences.getString(key, null) ?: return emptyList()
        val type = object : TypeToken<List<com.george.viagemplanejada.EventItem>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun deleteEvent(tripId: String, eventId: String) {
        val key = "events_$tripId"
        val events = getEventsCompat(tripId).toMutableList()
        events.removeAll { it.id == eventId }

        val json = gson.toJson(events)
        sharedPreferences.edit().putString(key, json).apply()
    }

    private fun deleteEventsForTrip(tripId: String) {
        val key = "events_$tripId"
        sharedPreferences.edit().remove(key).apply()
    }

    // ========== MÉTODOS PARA EXPENSES (COMPATIBILIDADE) ==========

    fun saveExpense(tripId: String, expense: com.george.viagemplanejada.ExpenseItem) {
        val key = "expenses_$tripId"
        val expenses = getExpensesCompat(tripId).toMutableList()
        val existingIndex = expenses.indexOfFirst { it.id == expense.id }

        if (existingIndex != -1) {
            expenses[existingIndex] = expense
        } else {
            expenses.add(expense)
        }

        val json = gson.toJson(expenses)
        sharedPreferences.edit().putString(key, json).apply()
    }

    fun getExpenses(tripId: String): List<com.george.viagemplanejada.ExpenseItem> {
        return getExpensesCompat(tripId)
    }

    private fun getExpensesCompat(tripId: String): List<com.george.viagemplanejada.ExpenseItem> {
        val key = "expenses_$tripId"
        val json = sharedPreferences.getString(key, null) ?: return emptyList()
        val type = object : TypeToken<List<com.george.viagemplanejada.ExpenseItem>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun deleteExpense(tripId: String, expenseId: String) {
        val key = "expenses_$tripId"
        val expenses = getExpensesCompat(tripId).toMutableList()
        expenses.removeAll { it.id == expenseId }

        val json = gson.toJson(expenses)
        sharedPreferences.edit().putString(key, json).apply()
    }

    private fun deleteExpensesForTrip(tripId: String) {
        val key = "expenses_$tripId"
        sharedPreferences.edit().remove(key).apply()
    }

    // ========== MÉTODOS PARA BUDGET ==========

    fun getBudget(tripId: String): Double {
        val key = "budget_$tripId"
        return sharedPreferences.getFloat(key, 0f).toDouble()
    }

    fun saveBudget(tripId: String, budget: Double) {
        val key = "budget_$tripId"
        sharedPreferences.edit().putFloat(key, budget.toFloat()).apply()
    }

    fun saveBudgetItem(tripId: String, budget: BudgetItem) {
        val key = "budget_item_$tripId"
        val json = gson.toJson(budget)
        sharedPreferences.edit().putString(key, json).apply()
        saveBudget(tripId, budget.totalBudget)
    }

    fun getBudgetItem(tripId: String): BudgetItem? {
        val key = "budget_item_$tripId"
        val json = sharedPreferences.getString(key, null) ?: return null
        return gson.fromJson(json, BudgetItem::class.java)
    }

    private fun deleteBudgetForTrip(tripId: String) {
        val key = "budget_$tripId"
        val keyItem = "budget_item_$tripId"
        sharedPreferences.edit()
            .remove(key)
            .remove(keyItem)
            .apply()
    }

    // ========== MÉTODOS TEMPORÁRIOS PARA COMPATIBILIDADE ==========

    fun getPlaceById(tripId: String, placeId: String): PlaceItem? {
        return getPlaces(tripId).find { it.id == placeId }
    }

    fun saveEvent(event: com.george.viagemplanejada.EventItem) {
        val tripId = getCurrentTripId() ?: return
        saveEvent(tripId, event)
    }

    fun saveExpense(expense: com.george.viagemplanejada.ExpenseItem) {
        val tripId = getCurrentTripId() ?: return
        saveExpense(tripId, expense)
    }

    // ========== MÉTODOS GERAIS ==========

    fun clearAllData() {
        sharedPreferences.edit().clear().apply()
    }

    fun exportData(): String {
        val allData = mapOf(
            "trips" to getAllTrips(),
            "places" to getAllTrips().associate { trip ->
                trip.id to getPlaces(trip.id)
            },
            "events" to getAllTrips().associate { trip ->
                trip.id to getEvents(trip.id)
            },
            "expenses" to getAllTrips().associate { trip ->
                trip.id to getExpenses(trip.id)
            },
            "budgets" to getAllTrips().associate { trip ->
                trip.id to getBudget(trip.id)
            }
        )
        return gson.toJson(allData)
    }
}