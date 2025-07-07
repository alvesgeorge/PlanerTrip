package com.george.viagemplanejada

data class Itinerary(
    val id: String,
    val tripName: String,
    val day: Int,
    val date: String,
    val title: String,
    val description: String,
    val startTime: String,
    val endTime: String,
    val location: String,
    val category: ItineraryCategory,
    val estimatedCost: String,
    val notes: String,
    val isCompleted: Boolean
) {
    fun toSaveString(): String {
        return "$id|$tripName|$day|$date|$title|$description|$startTime|$endTime|$location|${category.name}|$estimatedCost|$notes|$isCompleted"
    }

    companion object {
        fun fromSaveString(saveString: String): Itinerary? {
            val parts = saveString.split("|")
            return if (parts.size == 13) {
                Itinerary(
                    id = parts[0],
                    tripName = parts[1],
                    day = parts[2].toIntOrNull() ?: 1,
                    date = parts[3],
                    title = parts[4],
                    description = parts[5],
                    startTime = parts[6],
                    endTime = parts[7],
                    location = parts[8],
                    category = ItineraryCategory.valueOf(parts[9]),
                    estimatedCost = parts[10],
                    notes = parts[11],
                    isCompleted = parts[12].toBoolean()
                )
            } else null
        }
    }

    fun getCategoryIcon(): String {
        return when (category) {
            ItineraryCategory.TRANSPORT -> "🚗"
            ItineraryCategory.ACCOMMODATION -> "🏨"
            ItineraryCategory.FOOD -> "🍽️"
            ItineraryCategory.ATTRACTION -> "🎯"
            ItineraryCategory.SHOPPING -> "🛍️"
            ItineraryCategory.ENTERTAINMENT -> "🎭"
            ItineraryCategory.RELAXATION -> "🧘"
            ItineraryCategory.OTHERS -> "📝"
        }
    }

    fun getTimeRange(): String {
        return if (startTime.isNotEmpty() && endTime.isNotEmpty()) {
            "$startTime - $endTime"
        } else if (startTime.isNotEmpty()) {
            "A partir de $startTime"
        } else {
            "Horário livre"
        }
    }

    fun getDayTitle(): String {
        return if (date.isNotEmpty()) {
            "Dia $day - $date"
        } else {
            "Dia $day"
        }
    }
}

enum class ItineraryCategory {
    TRANSPORT, ACCOMMODATION, FOOD, ATTRACTION, SHOPPING, ENTERTAINMENT, RELAXATION, OTHERS
}