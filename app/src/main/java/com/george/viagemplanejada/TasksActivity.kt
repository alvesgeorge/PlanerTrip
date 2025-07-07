package com.george.viagemplanejada

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TasksActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Criar layout programaticamente para evitar crash
        createSimpleLayout()
    }

    private fun createSimpleLayout() {
        val tripName = intent.getStringExtra("trip_name") ?: "Tarefas"

        // Layout principal
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        // Header
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
            setBackgroundColor(android.graphics.Color.parseColor("#9C27B0"))
        }

        // Botão voltar
        val backButton = android.widget.Button(this).apply {
            text = "← Voltar"
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setOnClickListener { finish() }
        }

        // Título
        val titleText = TextView(this).apply {
            text = "📝 $tripName"
            textSize = 20f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(32, 0, 0, 0)
        }

        headerLayout.addView(backButton)
        headerLayout.addView(titleText)

        // Conteúdo
        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 32, 16, 16)
        }

        // Ícone grande
        val iconText = TextView(this).apply {
            text = "📝"
            textSize = 64f
            gravity = android.view.Gravity.CENTER
            setPadding(0, 32, 0, 16)
        }

        // Título principal
        val mainTitle = TextView(this).apply {
            text = "Sistema de Tarefas"
            textSize = 24f
            setTextColor(android.graphics.Color.parseColor("#212121"))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 8)
        }

        // Subtítulo
        val subtitle = TextView(this).apply {
            text = "Organize suas tarefas de viagem"
            textSize = 16f
            setTextColor(android.graphics.Color.parseColor("#757575"))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 32)
        }

        // Lista de tarefas de exemplo
        val tasksList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 16, 0, 16)
        }

        // Tarefas de exemplo
        val sampleTasks = listOf(
            "✅ Reservar passagens aéreas",
            "📋 Fazer check-in online",
            "🏨 Confirmar reserva do hotel",
            "🍽️ Pesquisar restaurantes",
            "🛍️ Comprar protetor solar"
        )

        for (task in sampleTasks) {
            val taskView = TextView(this).apply {
                text = task
                textSize = 16f
                setPadding(16, 12, 16, 12)
                setBackgroundColor(android.graphics.Color.parseColor("#F5F5F5"))
                setTextColor(android.graphics.Color.parseColor("#212121"))

                // Margem entre itens
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 8)
                layoutParams = params

                setOnClickListener {
                    Toast.makeText(this@TasksActivity, "📋 $task", Toast.LENGTH_SHORT).show()
                }
            }
            tasksList.addView(taskView)
        }

        // Botão adicionar
        val addButton = android.widget.Button(this).apply {
            text = "➕ Adicionar Nova Tarefa"
            textSize = 16f
            setPadding(16, 16, 16, 16)
            setOnClickListener {
                showAddTaskDialog()
            }
        }

        // Montar layout
        contentLayout.addView(iconText)
        contentLayout.addView(mainTitle)
        contentLayout.addView(subtitle)
        contentLayout.addView(tasksList)
        contentLayout.addView(addButton)

        mainLayout.addView(headerLayout)
        mainLayout.addView(contentLayout)

        setContentView(mainLayout)
    }

    private fun showAddTaskDialog() {
        val options = arrayOf(
            "🚗 Transporte",
            "🏨 Hospedagem",
            "📋 Documentos",
            "🍽️ Alimentação",
            "🛍️ Compras",
            "🎭 Atividades",
            "📱 Outros"
        )

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("➕ Adicionar Tarefa")
            .setMessage("Escolha uma categoria:")
            .setItems(options) { _, which ->
                val category = options[which]
                Toast.makeText(this, "📝 Categoria selecionada: $category\n\n✨ Funcionalidade completa em desenvolvimento!", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}