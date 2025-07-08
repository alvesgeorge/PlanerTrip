package com.george.viagemplanejada

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.george.viagemplanejada.databinding.ActivityMainBinding
import com.george.viagemplanejada.data.DataManager
import com.george.viagemplanejada.data.TripItem
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dataManager: DataManager
    private lateinit var tripAdapter: TripHomeAdapter
    private var trips = mutableListOf<TripItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar status bar transparente (CORRIGIDO)
        setupStatusBar()

        // Inicializar DataManager
        dataManager = DataManager.getInstance(this)

        // Configurar UI
        setupUI()
        setupRecyclerView()

        // Carregar dados
        loadTrips()

        // Animações de entrada
        startEntryAnimations()
    }

    override fun onResume() {
        super.onResume()
        loadTrips()
    }

    // ✅ MÉTODO CORRIGIDO - Remove warnings de APIs deprecated
    private fun setupStatusBar() {
        // API moderna para configurar status bar
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // API 30+ (Android 11+)
            window.insetsController?.let { controller ->
                controller.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            }
        } else {
            // API 23-29 (Android 6-10) - Manter compatibilidade
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }

        // Cor da status bar
        window.statusBarColor = android.graphics.Color.TRANSPARENT
    }

    private fun setupUI() {
        // Cards de ação rápida
        binding.cardNewTrip.setOnClickListener {
            animateCardClick(binding.cardNewTrip) {
                openNewTripActivity()
            }
        }

        binding.cardSearchDestination.setOnClickListener {
            animateCardClick(binding.cardSearchDestination) {
                showSearchDestination()
            }
        }

        // Botões
        binding.buttonViewAllTrips.setOnClickListener {
            openTripsListActivity()
        }

        binding.buttonCreateFirstTrip.setOnClickListener {
            openNewTripActivity()
        }

        binding.fabNewTrip.setOnClickListener {
            animateFabClick {
                openNewTripActivity()
            }
        }
    }

    private fun setupRecyclerView() {
        tripAdapter = TripHomeAdapter(trips) { trip ->
            openTripDetails(trip)
        }

        binding.recyclerViewTrips.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = tripAdapter
            // Adicionar animação de entrada para itens
            layoutAnimation = AnimationUtils.loadLayoutAnimation(
                this@MainActivity, R.anim.layout_animation_slide_from_bottom
            )
        }
    }

    private fun loadTrips() {
        trips.clear()
        trips.addAll(dataManager.getAllTrips())

        updateUI()
        tripAdapter.updateTrips(trips)
    }

    private fun updateUI() {
        if (trips.isEmpty()) {
            binding.recyclerViewTrips.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.buttonViewAllTrips.visibility = View.GONE
        } else {
            binding.recyclerViewTrips.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
            binding.buttonViewAllTrips.visibility = View.VISIBLE

            // Mostrar apenas as 3 viagens mais recentes na home
            val recentTrips = trips.take(3)
            tripAdapter.updateTrips(recentTrips)
        }
    }

    // ✅ MÉTODO CORRIGIDO - Remove warning de overridePendingTransition
    private fun startActivityWithTransition(intent: Intent) {
        startActivity(intent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // API 34+ - usar transições modernas
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN,
                R.anim.slide_in_right, R.anim.slide_out_left)
        } else {
            // Fallback para APIs antigas
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun openNewTripActivity() {
        val intent = Intent(this, CreateTripActivity::class.java)
        startActivityWithTransition(intent) // ✅ USANDO MÉTODO CORRIGIDO
    }

    private fun openTripDetails(trip: TripItem) {
        val intent = Intent(this, ItineraryActivity::class.java)
        intent.putExtra("trip_id", trip.id)
        intent.putExtra("trip_name", trip.name)
        startActivityWithTransition(intent) // ✅ USANDO MÉTODO CORRIGIDO
    }

    private fun openTripsListActivity() {
        val intent = Intent(this, TripsListActivity::class.java)
        startActivityWithTransition(intent) // ✅ USANDO MÉTODO CORRIGIDO
    }

    private fun showSearchDestination() {
        Toast.makeText(this, "🔍 Funcionalidade de busca em desenvolvimento", Toast.LENGTH_SHORT).show()

        // TODO: Implementar busca de destinos
        // Pode abrir uma activity de busca ou um bottom sheet
    }

    private fun startEntryAnimations() {
        // Animação do header
        val slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down)
        binding.root.findViewById<View>(R.id.cardNewTrip).parent?.let { header ->
            (header as View).startAnimation(slideDown)
        }

        // Animação dos cards com delay
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)

        binding.cardNewTrip.startAnimation(slideUp)

        binding.cardSearchDestination.postDelayed({
            binding.cardSearchDestination.startAnimation(slideUp)
        }, 100)

        // Animação do FAB
        binding.fabNewTrip.postDelayed({
            val scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in)
            binding.fabNewTrip.startAnimation(scaleIn)
        }, 300)
    }

    private fun animateCardClick(view: View, action: () -> Unit) {
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .withEndAction {
                        action()
                    }
                    .start()
            }
            .start()
    }

    private fun animateFabClick(action: () -> Unit) {
        binding.fabNewTrip.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(100)
            .withEndAction {
                binding.fabNewTrip.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .withEndAction {
                        action()
                    }
                    .start()
            }
            .start()
    }
}