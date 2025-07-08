package com.george.viagemplanejada.data

data class EventItem(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    val location: String = "",
    val type: String = "Evento",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)