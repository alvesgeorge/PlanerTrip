package com.george.viagemplanejada.data

data class Trip(
    val id: String,
    val name: String,
    val destination: String,
    val startDate: String,
    val endDate: String,
    val description: String,
    val imageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class TaskItem(
    val id: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val priority: String,
    val dueDate: String,
    val category: String
)