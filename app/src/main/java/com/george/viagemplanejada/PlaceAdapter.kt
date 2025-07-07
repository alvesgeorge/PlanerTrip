package com.george.viagemplanejada

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.*

class PlaceAdapter(
    private var places: List<PlaceItem>,
    private val onPlaceAction: (PlaceItem, String) -> Unit
) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val priorityIndicator: LinearLayout = itemView.findViewById(R.id.priorityIndicator)
        val dayText: TextView = itemView.findViewById(R.id.textPlaceDay)
        val timeText: TextView = itemView.findViewById(R.id.textPlaceTime)
        val categoryText: TextView = itemView.findViewById(R.id.textPlaceCategory)
        val nameText: TextView = itemView.findViewById(R.id.textPlaceName)
        val addressText: TextView = itemView.findViewById(R.id.textPlaceAddress)
        val durationText: TextView = itemView.findViewById(R.id.textPlaceDuration)
        val costText: TextView = itemView.findViewById(R.id.textPlaceCost)
        val optionsButton: Button = itemView.findViewById(R.id.buttonPlaceOptions)
        val directionsButton: Button = itemView.findViewById(R.id.buttonDirections)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]

        // Priority color
        val priorityColor = when (place.priority) {
            "Alta" -> Color.parseColor("#F44336")
            "Média" -> Color.parseColor("#FF9800")
            "Baixa" -> Color.parseColor("#4CAF50")
            else -> Color.parseColor("#9E9E9E")
        }
        holder.priorityIndicator.setBackgroundColor(priorityColor)

        // Basic info
        holder.dayText.text = place.day
        holder.timeText.text = place.preferredTime
        holder.nameText.text = place.name
        holder.addressText.text = "📍 ${place.address}"
        holder.durationText.text = "⏱️ ${place.duration}h"

        // Category with icon
        val categoryIcon = when (place.category) {
            "Turismo" -> "🏛️"
            "Cultura" -> "🎭"
            "Natureza" -> "��"
            "Lazer" -> "🏖️"
            "Gastronomia" -> "🍽️"
            "Compras" -> "🛍️"
            else -> "📍"
        }
        holder.categoryText.text = "$categoryIcon ${place.category}"

        // Cost (show only if > 0)
        if (place.cost > 0) {
            holder.costText.text = "💰 ${NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(place.cost)}"
            holder.costText.visibility = View.VISIBLE
        } else {
            holder.costText.visibility = View.GONE
        }

        // Click listeners
        holder.itemView.setOnClickListener {
            onPlaceAction(place, "DETAILS")
        }

        holder.optionsButton.setOnClickListener {
            showOptionsMenu(holder.itemView, place)
        }

        holder.directionsButton.setOnClickListener {
            onPlaceAction(place, "DIRECTIONS")
        }
    }

    private fun showOptionsMenu(view: View, place: PlaceItem) {
        val popup = androidx.appcompat.widget.PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.place_options_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_details -> {
                    onPlaceAction(place, "DETAILS")
                    true
                }
                R.id.action_edit -> {
                    onPlaceAction(place, "EDIT")
                    true
                }
                R.id.action_delete -> {
                    onPlaceAction(place, "DELETE")
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    override fun getItemCount(): Int = places.size

    fun updatePlaces(newPlaces: List<PlaceItem>) {
        places = newPlaces
        notifyDataSetChanged()
    }
}

// Data class para locais
data class PlaceItem(
    val id: String,
    val name: String,
    val address: String,
    val description: String,
    val day: String,
    val category: String,
    val duration: Double,
    val preferredTime: String,
    val priority: String,
    val cost: Double
)