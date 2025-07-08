package com.george.viagemplanejada

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.george.viagemplanejada.data.EventItem

class EventAdapter(
    private var events: List<EventItem>,
    private val onEventClick: (EventItem, String) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textTitle: TextView = itemView.findViewById(R.id.textEventTitle)
        val textTime: TextView = itemView.findViewById(R.id.textEventTime)
        val textLocation: TextView = itemView.findViewById(R.id.textEventLocation)
        val textType: TextView = itemView.findViewById(R.id.textEventType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]

        holder.textTitle.text = event.title
        holder.textTime.text = "🕐 ${event.time}"
        holder.textLocation.text = if (event.location.isNotEmpty()) "📍 ${event.location}" else "📍 Local não definido"
        holder.textType.text = "🏷️ ${event.type}"

        holder.itemView.setOnClickListener {
            onEventClick(event, "DETAILS")
        }

        holder.itemView.setOnLongClickListener {
            onEventClick(event, "DELETE")
            true
        }
    }

    override fun getItemCount(): Int = events.size

    fun updateEvents(newEvents: List<EventItem>) {
        events = newEvents
        notifyDataSetChanged()
    }
}