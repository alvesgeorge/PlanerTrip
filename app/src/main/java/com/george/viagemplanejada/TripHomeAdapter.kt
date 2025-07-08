package com.george.viagemplanejada

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.george.viagemplanejada.data.TripItem

class TripHomeAdapter(
    private var trips: List<TripItem>,
    private val onTripClick: (TripItem) -> Unit
) : RecyclerView.Adapter<TripHomeAdapter.TripViewHolder>() {

    class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textTripName: TextView = itemView.findViewById(R.id.textTripName)
        val textTripDates: TextView = itemView.findViewById(R.id.textTripDates)
        val textTripDestination: TextView = itemView.findViewById(R.id.textTripDestination)
        val textTripStatus: TextView = itemView.findViewById(R.id.textTripStatus)
        val textTripDuration: TextView = itemView.findViewById(R.id.textTripDuration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trip_home, parent, false)
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = trips[position]
        val context = holder.itemView.context

        holder.textTripName.text = trip.name
        holder.textTripDestination.text = "📍 ${trip.destination}"

        // Formatação das datas
        val datesText = if (trip.startDate.isNotEmpty() && trip.endDate.isNotEmpty()) {
            "${trip.startDate} - ${trip.endDate}"
        } else {
            "Datas não definidas"
        }
        holder.textTripDates.text = datesText

        // Status da viagem
        val statusText = when {
            trip.startDate.isEmpty() -> "📝 Planejando"
            else -> "✅ Programada"
        }
        holder.textTripStatus.text = statusText

        // Duração estimada
        holder.textTripDuration.text = "⏱️ ${calculateDuration(trip)} dias"

        // Click listener
        holder.itemView.setOnClickListener {
            onTripClick(trip)
        }
    }

    override fun getItemCount(): Int = trips.size

    fun updateTrips(newTrips: List<TripItem>) {
        trips = newTrips
        notifyDataSetChanged()
    }

    private fun calculateDuration(trip: TripItem): Int {
        return if (trip.startDate.isNotEmpty() && trip.endDate.isNotEmpty()) {
            try {
                // Cálculo simples - você pode melhorar isso
                val start = trip.startDate.split("/")
                val end = trip.endDate.split("/")
                if (start.size == 3 && end.size == 3) {
                    val startDay = start[0].toInt()
                    val endDay = end[0].toInt()
                    maxOf(1, endDay - startDay + 1)
                } else 1
            } catch (e: Exception) {
                1
            }
        } else 1
    }
}