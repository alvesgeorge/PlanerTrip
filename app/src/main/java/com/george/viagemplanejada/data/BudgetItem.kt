package com.george.viagemplanejada.data

data class BudgetItem(
    val totalBudget: Double,
    val categories: Map<String, Double> = emptyMap(),
    val notes: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)