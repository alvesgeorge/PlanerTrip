// app/src/main/java/com/george/viagemplanejada/data/PlaceItem.kt
package com.george.viagemplanejada.data

data class PlaceItem(
    val id: String,
    val name: String,
    val address: String,
    val day: String,
    val category: String,
    val duration: Double, // ← DOUBLE, não String
    val preferredTime: String,
    val cost: Double,
    val description: String,
    val priority: String = "Média"
) {
    fun toSaveString(): String {
        return "$id|$name|$address|$day|$category|$duration|$preferredTime|$cost|$description|$priority"
    }

    companion object {
        fun fromSaveString(saveString: String): PlaceItem? {
            val parts = saveString.split("|")
            return if (parts.size >= 9) {
                PlaceItem(
                    id = parts[0],
                    name = parts[1],
                    address = parts[2],
                    day = parts[3],
                    category = parts[4],
                    duration = parts[5].toDoubleOrNull() ?: 2.0,
                    preferredTime = parts[6],
                    cost = parts[7].toDoubleOrNull() ?: 0.0,
                    description = parts[8],
                    priority = if (parts.size > 9) parts[9] else "Média"
                )
            } else null
        }
    }
}