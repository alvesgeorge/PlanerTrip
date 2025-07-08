package com.george.viagemplanejada

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.george.viagemplanejada.databinding.ActivityTripsListBinding
import com.george.viagemplanejada.data.DataManager
import com.george.viagemplanejada.data.TripItem

class TripsListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTripsListBinding
    private lateinit var dataManager: DataManager
    private lateinit var tripAdapter: TripHomeAdapter
    private var trips = mutableListOf<TripItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTripsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataManager = DataManager.getInstance(this)

        setupUI()
        setupRecyclerView()
        loadTrips()
    }

    override fun onResume() {
        super.onResume()
        loadTrips()
    }

    private fun setupUI() {
        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.fabNewTrip.setOnClickListener {
            val intent = Intent(this, CreateTripActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        tripAdapter = TripHomeAdapter(trips) { trip ->
            val intent = Intent(this, ItineraryActivity::class.java)
            intent.putExtra("trip_id", trip.id)
            intent.putExtra("trip_name", trip.name)
            startActivity(intent)
        }

        binding.recyclerViewTrips.apply {
            layoutManager = LinearLayoutManager(this@TripsListActivity)
            adapter = tripAdapter
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
        } else {
            binding.recyclerViewTrips.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
        }

        binding.textTotalTrips.text = "${trips.size} viagens"
    }
}