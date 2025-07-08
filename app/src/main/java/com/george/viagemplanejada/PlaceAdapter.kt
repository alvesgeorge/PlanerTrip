package com.george.viagemplanejada

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.george.viagemplanejada.data.PlaceItem

class PlaceAdapter(
    private var places: List<PlaceItem>,
    private val onPlaceClick: (PlaceItem, String) -> Unit
) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textCategoryIcon: TextView = itemView.findViewById(R.id.textCategoryIcon)
        val textPlaceName: TextView = itemView.findViewById(R.id.textPlaceName)
        val textPlaceAddress: TextView = itemView.findViewById(R.id.textPlaceAddress)
        val textPlaceDay: TextView = itemView.findViewById(R.id.textPlaceDay)
        val textPlaceTime: TextView = itemView.findViewById(R.id.textPlaceTime)
        val textPlaceDuration: TextView = itemView.findViewById(R.id.textPlaceDuration)
        val textPlaceCategory: TextView = itemView.findViewById(R.id.textPlaceCategory)
        val textPlacePriority: TextView = itemView.findViewById(R.id.textPlacePriority)
        val textPlaceCost: TextView = itemView.findViewById(R.id.textPlaceCost)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        val context = holder.itemView.context

        // Nome e endereço
        holder.textPlaceName.text = place.name
        holder.textPlaceAddress.text = place.address

        // Informações com emojis
        holder.textPlaceDay.text = "📅 ${place.day}"
        holder.textPlaceTime.text = "🕐 ${place.preferredTime}"
        holder.textPlaceDuration.text = "⏱️ ${place.duration.toInt()}h"

        // Categoria com ícone e cor
        val (categoryIcon, categoryColor) = getCategoryIconAndColor(place.category)
        holder.textCategoryIcon.text = categoryIcon
        holder.textPlaceCategory.text = "$categoryIcon ${place.category.removePrefix("🏛️ ").removePrefix("🎭 ").removePrefix("🌳 ").removePrefix("🎡 ").removePrefix("🍽️ ").removePrefix("🛍️ ").removePrefix("🏨 ").removePrefix("🚗 ").removePrefix("📍 ")}"
        holder.textPlaceCategory.setTextColor(ContextCompat.getColor(context, categoryColor))

        // Prioridade com cor e background
        holder.textPlacePriority.text = place.priority.uppercase()
        val (priorityColor, priorityBg) = getPriorityColorAndBackground(place.priority)
        holder.textPlacePriority.setTextColor(ContextCompat.getColor(context, android.R.color.white))
        holder.textPlacePriority.setBackgroundColor(ContextCompat.getColor(context, priorityColor))

        // Custo com formatação
        if (place.cost > 0) {
            holder.textPlaceCost.text = "💰 R\$ ${String.format("%.2f", place.cost)}"
            holder.textPlaceCost.visibility = View.VISIBLE
        } else {
            holder.textPlaceCost.visibility = View.GONE
        }

        // Animação de entrada
        animateItemEntry(holder.itemView, position)

        // Click listeners com feedback visual
        holder.itemView.setOnClickListener {
            animateClick(holder.itemView) {
                onPlaceClick(place, "DETAILS")
            }
        }

        holder.itemView.setOnLongClickListener {
            animateClick(holder.itemView) {
                onPlaceClick(place, "EDIT")
            }
            true
        }
    }

    override fun getItemCount(): Int = places.size

    fun updatePlaces(newPlaces: List<PlaceItem>) {
        val oldSize = places.size
        places = newPlaces

        when {
            oldSize == 0 && newPlaces.isNotEmpty() -> {
                notifyItemRangeInserted(0, newPlaces.size)
            }
            oldSize > 0 && newPlaces.isEmpty() -> {
                notifyItemRangeRemoved(0, oldSize)
            }
            else -> {
                notifyDataSetChanged()
            }
        }
    }

    private fun getCategoryIconAndColor(category: String): Pair<String, Int> {
        return when {
            category.contains("Turismo") -> "🏛️" to R.color.category_tourism
            category.contains("Cultura") -> "🎭" to R.color.category_culture
            category.contains("Natureza") -> "🌳" to R.color.category_nature
            category.contains("Lazer") -> "🎡" to R.color.category_leisure
            category.contains("Gastronomia") -> "🍽️" to R.color.category_food
            category.contains("Compras") -> "��️" to R.color.category_shopping
            category.contains("Hospedagem") -> "🏨" to R.color.category_hotel
            category.contains("Transporte") -> "��" to R.color.category_transport
            else -> "��" to R.color.category_other
        }
    }

    private fun getPriorityColorAndBackground(priority: String): Pair<Int, Int> {
        return when (priority) {
            "Alta" -> R.color.priority_high to R.drawable.priority_badge
            "Média" -> R.color.priority_medium to R.drawable.priority_badge
            "Baixa" -> R.color.priority_low to R.drawable.priority_badge
            else -> R.color.text_secondary to R.drawable.priority_badge
        }
    }

    private fun animateItemEntry(view: View, position: Int) {
        view.alpha = 0f
        view.translationY = 100f

        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(300)
            .setStartDelay((position * 50).toLong())
            .start()
    }

    private fun animateClick(view: View, action: () -> Unit) {
        val scaleDown = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f)
        val scaleUp = ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1f)

        scaleDown.duration = 100
        scaleUp.duration = 100

        scaleDown.start()
        scaleDown.doOnEnd {
            scaleUp.start()
            action()
        }
    }
}

// Extensão para ObjectAnimator
private fun ObjectAnimator.doOnEnd(action: () -> Unit) {
    addListener(object : android.animation.Animator.AnimatorListener {
        override fun onAnimationStart(animation: android.animation.Animator) {}
        override fun onAnimationEnd(animation: android.animation.Animator) { action() }
        override fun onAnimationCancel(animation: android.animation.Animator) {}
        override fun onAnimationRepeat(animation: android.animation.Animator) {}
    })
}