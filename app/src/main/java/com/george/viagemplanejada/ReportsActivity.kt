package com.george.viagemplanejada

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.george.viagemplanejada.databinding.ActivityReportsBinding

class ReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        binding.textTitle.text = "📊 Relatórios"
        binding.textContent.text = "🚧 Funcionalidade em desenvolvimento!\n\nEm breve você poderá:\n• Gerar relatórios de gastos\n• Análise de destinos favoritos\n• Estatísticas de viagens\n• Exportar dados"

        binding.buttonBack.setOnClickListener {
            finish()
        }
    }
}