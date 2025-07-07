package com.george.viagemplanejada

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventAdapter(
    private var events: List<EventItem>,
    private val onEventAction: (EventItem, String) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val priorityIndicator: LinearLayout = itemView.findViewById(R.id.priorityIndicator)
        val timeText: TextView = itemView.findViewById(R.id.textEventTime)
        val categoryText: TextView = itemView.findViewById(R.id.textEventCategory)
        val titleText: TextView = itemView.findViewById(R.id.textEventTitle)
        val descriptionText: TextView = itemView.findViewById(R.id.textEventDescription)
        val locationText: TextView = itemView.findViewById(R.id.textEventLocation)
        val optionsButton: Button = itemView.findViewById(R.id.buttonEventOptions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]

        // Priority color
        val priorityColor = when (event.priority) {
            "Alta" -> Color.parseColor("#F44336")
            "Média" -> Color.parseColor("#FF9800")
            "Baixa" -> Color.parseColor("#4CAF50")
            else -> Color.parseColor("#9E9E9E")
        }
        holder.priorityIndicator.setBackgroundColor(priorityColor)

        // Time
        val timeDisplay = if (event.endTime.isNotEmpty()) {
            "${event.startTime} - ${event.endTime}"
        } else {
            event.startTime
        }
        holder.timeText.text = timeDisplay

        // Category with icon
        val categoryIcon = when (event.category) {
            "Turismo" -> "🗺️"
            "Hospedagem" -> "🏨"
            "Alimentação" -> "🍽️"
            "Transporte" -> "🚗"
            "Atividade" -> "🎭"
            "Compras" -> "🛍️"
            else -> "📅"
        }
        holder.categoryText.text = "$categoryIcon ${event.category}"

        // Event info
        holder.titleText.text = event.title

        if (event.description.isNotEmpty()) {
            holder.descriptionText.text = event.description
            holder.descriptionText.visibility = View.VISIBLE
        } else {
            holder.descriptionText.visibility = View.GONE
        }

        if (event.location.isNotEmpty()) {
            holder.locationText.text = "📍 ${event.location}"
            holder.locationText.visibility = View.VISIBLE
        } else {
            holder.locationText.visibility = View.GONE
        }

        // Click listeners
        holder.itemView.setOnClickListener {
            onEventAction(event, "DETAILS")
        }

        holder.optionsButton.setOnClickListener {
            showOptionsMenu(holder.itemView, event)
        }
    }

    private fun showOptionsMenu(view: View, event: EventItem) {
        val popup = androidx.appcompat.widget.PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.event_options_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_details -> {
                    onEventAction(event, "DETAILS")
                    true
                }
                R.id.action_edit -> {
                    onEventAction(event, "EDIT")
                    true
                }
                R.id.action_delete -> {
                    onEventAction(event, "DELETE")
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    override fun getItemCount(): Int = events.size

    fun updateEvents(newEvents: List<EventItem>) {
        events = newEvents
        notifyDataSetChanged()
    }
}

// Data class para eventos
data class EventItem(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val category: String,
    val location: String,
    val priority: String
)