package com.example.listacompras

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class AddItemActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        val spinner = findViewById<Spinner>(R.id.spinnerUnKg)
        val options = arrayOf("un", "kg")

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, // Layout padrão para spinner
            options
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // Layout para o dropdown
        }

        spinner.adapter = adapter // Atribui o adapter ao Spinner

        findViewById<MaterialButton>(R.id.btnSalvar).setOnClickListener {
            val nome = findViewById<TextInputEditText>(R.id.etNome).text?.toString()?.trim().orEmpty()
            val quantidade = findViewById<TextInputEditText>(R.id.etQuantidade).text?.toString()?.toIntOrNull()
            val unidade = spinner.selectedItem.toString()

            if (nome.isEmpty() || quantidade == null) {  // Se o nome ou quantidade estiverem vazios, não salva
                return@setOnClickListener
            }
        }
    }
}
