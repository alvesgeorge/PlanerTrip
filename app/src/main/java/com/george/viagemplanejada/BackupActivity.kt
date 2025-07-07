package com.george.viagemplanejada

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.george.viagemplanejada.databinding.ActivityBackupBinding

class BackupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBackupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBackupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        binding.textTitle.text = "💾 Backup e Sincronização"
        binding.textContent.text = "🚧 Funcionalidade em desenvolvimento!\n\nEm breve você poderá:\n• Fazer backup dos dados\n• Sincronizar na nuvem\n• Restaurar informações\n• Exportar/importar dados"

        binding.buttonBack.setOnClickListener {
            finish()
        }
    }
}