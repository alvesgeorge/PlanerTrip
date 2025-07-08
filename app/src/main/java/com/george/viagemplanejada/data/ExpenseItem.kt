package com.george.viagemplanejada.data

data class ExpenseItem(
    val id: String,
    val title: String,
    val amount: Double,
    val category: String,
    val date: String,
    val description: String = "",
    val paymentMethod: String = "Dinheiro",
    val createdAt: Long = System.currentTimeMillis()
)