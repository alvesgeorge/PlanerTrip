package com.george.viagemplanejada

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.george.viagemplanejada.databinding.ActivityTripListBinding

class TripListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTripListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTripListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.fabAddTrip.setOnClickListener {
            val intent = Intent(this, AddTripActivity::class.java)
            startActivity(intent)
        }

        Toast.makeText(this, "✅ Lista de viagens carregada", Toast.LENGTH_SHORT).show()
    }
}