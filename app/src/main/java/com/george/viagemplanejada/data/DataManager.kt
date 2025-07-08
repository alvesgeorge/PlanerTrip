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

    // ========== UTILITÁRIOS ==========

    fun generateId(): String = UUID.randomUUID().toString()

    fun getCurrentTripId(): String? {
        val currentTripId = sharedPreferences.getString("current_trip_id", null)
        return if (currentTripId != null) {
            currentTripId
        } else {
            val firstTrip = getAllTrips().firstOrNull()
            if (firstTrip != null) {
                setCurrentTrip(firstTrip.id)
                firstTrip.id
            } else null
        }
    }

    // ========== TRIPS ==========

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

        // Limpar dados relacionados
        sharedPreferences.edit()
            .remove("places_$tripId")
            .remove("events_$tripId")
            .remove("expenses_$tripId")
            .remove("budget_$tripId")
            .apply()
    }

    fun getCurrentTrip(): TripItem? {
        val currentTripId = getCurrentTripId()
        return if (currentTripId != null) getTripById(currentTripId) else null
    }

    fun setCurrentTrip(tripId: String) {
        sharedPreferences.edit().putString("current_trip_id", tripId).apply()
    }

    // ========== PLACES ==========

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

    // ========== EVENTS ==========

    fun saveEvent(tripId: String, event: EventItem) {
        val key = "events_$tripId"
        val events = getEvents(tripId).toMutableList()
        val existingIndex = events.indexOfFirst { it.id == event.id }

        if (existingIndex != -1) {
            events[existingIndex] = event
        } else {
            events.add(event)
        }

        val json = gson.toJson(events)
        sharedPreferences.edit().putString(key, json).apply()
    }

    fun getEvents(tripId: String): List<EventItem> {
        val key = "events_$tripId"
        val json = sharedPreferences.getString(key, null) ?: return emptyList()
        val type = object : TypeToken<List<EventItem>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun deleteEvent(tripId: String, eventId: String) {
        val key = "events_$tripId"
        val events = getEvents(tripId).toMutableList()
        events.removeAll { it.id == eventId }

        val json = gson.toJson(events)
        sharedPreferences.edit().putString(key, json).apply()
    }

    // ========== EXPENSES ==========

    fun saveExpense(tripId: String, expense: ExpenseItem) {
        val key = "expenses_$tripId"
        val expenses = getExpenses(tripId).toMutableList()
        val existingIndex = expenses.indexOfFirst { it.id == expense.id }

        if (existingIndex != -1) {
            expenses[existingIndex] = expense
        } else {
            expenses.add(expense)
        }

        val json = gson.toJson(expenses)
        sharedPreferences.edit().putString(key, json).apply()
    }

    fun getExpenses(tripId: String): List<ExpenseItem> {
        val key = "expenses_$tripId"
        val json = sharedPreferences.getString(key, null) ?: return emptyList()
        val type = object : TypeToken<List<ExpenseItem>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun deleteExpense(tripId: String, expenseId: String) {
        val key = "expenses_$tripId"
        val expenses = getExpenses(tripId).toMutableList()
        expenses.removeAll { it.id == expenseId }

        val json = gson.toJson(expenses)
        sharedPreferences.edit().putString(key, json).apply()
    }

    // ========== BUDGET ==========

    fun getBudget(tripId: String): Double {
        val key = "budget_$tripId"
        return sharedPreferences.getFloat(key, 0f).toDouble()
    }

    fun saveBudget(tripId: String, budget: Double) {
        val key = "budget_$tripId"
        sharedPreferences.edit().putFloat(key, budget.toFloat()).apply()
    }

    fun getBudgetItem(tripId: String): BudgetItem? {
        val key = "budget_item_$tripId"
        val json = sharedPreferences.getString(key, null) ?: return null
        return gson.fromJson(json, BudgetItem::class.java)
    }

    fun saveBudgetItem(tripId: String, budget: BudgetItem) {
        val key = "budget_item_$tripId"
        val json = gson.toJson(budget)
        sharedPreferences.edit().putString(key, json).apply()
        saveBudget(tripId, budget.totalBudget)
    }

    // ========== COMPATIBILIDADE ==========

    fun saveEvent(event: EventItem) {
        val tripId = getCurrentTripId() ?: return
        saveEvent(tripId, event)
    }

    fun saveExpense(expense: ExpenseItem) {
        val tripId = getCurrentTripId() ?: return
        saveExpense(tripId, expense)
    }

    // ========== UTILIDADES ==========

    fun clearAllData() {
        sharedPreferences.edit().clear().apply()
    }

    fun exportData(): String {
        val allData = mapOf(
            "trips" to getAllTrips(),
            "places" to getAllTrips().associate { it.id to getPlaces(it.id) },
            "events" to getAllTrips().associate { it.id to getEvents(it.id) },
            "expenses" to getAllTrips().associate { it.id to getExpenses(it.id) },
            "budgets" to getAllTrips().associate { it.id to getBudget(it.id) }
        )
        return gson.toJson(allData)
    }
}