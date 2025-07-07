package com.george.viagemplanejada

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.george.viagemplanejada.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        updateStatistics()
    }

    private fun setupUI() {
        // Atualizar data atual
        updateCurrentDate()

        // Configurações
        binding.buttonSettings.setOnClickListener {
            Toast.makeText(this, "⚙️ Configurações em desenvolvimento", Toast.LENGTH_SHORT).show()
        }

        // Ações Rápidas
        binding.buttonNewTrip.setOnClickListener {
            startActivity(Intent(this, AddTripActivity::class.java))
        }

        binding.buttonViewTrips.setOnClickListener {
            startActivity(Intent(this, TripListActivity::class.java))
        }

        // Orçamento
        binding.buttonBudgetDetails.setOnClickListener {
            binding.buttonBudgetDetails.setOnClickListener {
                val intent = Intent(this, BudgetActivity::class.java)
                intent.putExtra("trip_name", "Minha Viagem")
                intent.putExtra("trip_id", "trip_001")
                startActivity(intent)
            }
        }

        // Menu Principal - Linha 1
        binding.buttonCalendar.setOnClickListener {
            binding.buttonCalendar.setOnClickListener {
                val intent = Intent(this, CalendarActivity::class.java)
                intent.putExtra("trip_name", "Minha Viagem")
                intent.putExtra("trip_id", "trip_001")
                startActivity(intent)
            }
        }

        binding.buttonBudget.setOnClickListener {
            binding.buttonBudget.setOnClickListener {
                val intent = Intent(this, BudgetActivity::class.java)
                intent.putExtra("trip_name", "Minha Viagem")
                intent.putExtra("trip_id", "trip_001")
                startActivity(intent)
            }
        }

        binding.buttonTasks.setOnClickListener {
            // Abrir tarefas genéricas por enquanto
            val intent = Intent(this, TasksActivity::class.java)
            intent.putExtra("trip_name", "Tarefas Gerais")
            intent.putExtra("trip_id", "general")
            startActivity(intent)
        }

        // Menu Principal - Linha 2
        binding.buttonItinerary.setOnClickListener {
            binding.buttonItinerary.setOnClickListener {
                val intent = Intent(this, ItineraryActivity::class.java)
                intent.putExtra("trip_name", "Minha Viagem")
                intent.putExtra("trip_id", "trip_001")
                startActivity(intent)
            }
        }

        binding.buttonReports.setOnClickListener {
            Toast.makeText(this, "📊 Relatórios em desenvolvimento", Toast.LENGTH_SHORT).show()
        }

        binding.buttonBackup.setOnClickListener {
            Toast.makeText(this, "💾 Backup em desenvolvimento", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCurrentDate() {
        val sdf = SimpleDateFormat("EEEE, dd 'de' MMMM", Locale("pt", "BR"))
        val currentDate = sdf.format(Date())
        binding.textCurrentDate.text = "Hoje, $currentDate"
    }

    private fun updateStatistics() {
        // Por enquanto, valores fixos
        // Depois vamos conectar com dados reais

        binding.textTotalTrips.text = "3"
        binding.textNextTrip.text = "Rio de Janeiro"
        binding.textTotalBudget.text = "R$ 2.500,00"

        // Adicionar viagens recentes (exemplo)
        updateRecentTrips()
    }

    private fun updateRecentTrips() {
        // Limpar container
        binding.recentTripsContainer.removeAllViews()

        // Adicionar viagens de exemplo
        val recentTrips = listOf(
            "🏖️ Rio de Janeiro - Jan 2025",
            "🏔️ Campos do Jordão - Dez 2024",
            "🌊 Florianópolis - Nov 2024"
        )

        for (trip in recentTrips) {
            val tripView = layoutInflater.inflate(android.R.layout.simple_list_item_1, binding.recentTripsContainer, false)
            val textView = tripView.findViewById<android.widget.TextView>(android.R.id.text1)
            textView.text = trip
            textView.textSize = 14f
            textView.setPadding(16, 12, 16, 12)

            tripView.setOnClickListener {
                Toast.makeText(this, "📋 Detalhes: $trip", Toast.LENGTH_SHORT).show()
            }

            binding.recentTripsContainer.addView(tripView)
        }
    }

    override fun onResume() {
        super.onResume()
        // Atualizar estatísticas quando voltar para a tela
        updateStatistics()
    }
}