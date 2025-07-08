// app/src/main/java/com/george/viagemplanejada/data/TripItem.kt
package com.george.viagemplanejada.data

data class TripItem(
    val id: String,
    val name: String,
    val destination: String,
    val startDate: String,
    val endDate: String,
    val description: String = "",
    val budget: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)