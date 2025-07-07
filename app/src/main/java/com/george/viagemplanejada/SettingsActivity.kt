package com.george.viagemplanejada

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.george.viagemplanejada.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        binding.textTitle.text = "⚙️ Configurações"
        binding.textContent.text = "🚧 Funcionalidade em desenvolvimento!\n\nEm breve você poderá:\n• Personalizar tema\n• Configurar notificações\n• Definir moeda padrão\n• Ajustar preferências"

        binding.buttonBack.setOnClickListener {
            finish()
        }
    }
}