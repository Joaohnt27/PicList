package com.example.listacompras.ui.item

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.listacompras.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class AddItemActivity : AppCompatActivity() {

    private var itemId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        val titleTv = findViewById<TextView>(R.id.tvTitulo)
        val etNome = findViewById<TextInputEditText>(R.id.etNome)
        val etQuantidade = findViewById<TextInputEditText>(R.id.etQuantidade)
        val spinnerUnKg = findViewById<Spinner>(R.id.spinnerUnKg)
        val spinnerCategoria = findViewById<Spinner>(R.id.spinnerCategoria)

        val options = arrayOf("un", "kg")
        spinnerUnKg.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, options
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        val categorias = arrayOf("Hortifrúti", "Padaria e Confeitaria", "Açougue e Peixaria", "Frios e Laticínios", "Congelados", "Mercearia Seca", "Doces e Snacks", "Bebidas", "Infantil", "Pet Shop", "Limpeza", "Higiene Pessoal e Beleza", "Saúde e Farmácia", "Utilidades Domésticas e Outros")
        spinnerCategoria.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, categorias
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        val itemNome = intent.getStringExtra("item_nome")
        val itemQuantidade = intent.getIntExtra("item_quantidade", -1)
        val itemUnidade = intent.getStringExtra("item_unidade")
        val itemCategoria = intent.getStringExtra("item_categoria")
        itemId = intent.getIntExtra("item_id", -1)

        val isEdit = itemId != -1

        titleTv.text = if (isEdit) "Editar item" else "Adicionar item"

        if (isEdit) {
            etNome.setText(itemNome.orEmpty())
            etQuantidade.setText(itemQuantidade.takeIf { it >= 0 }?.toString().orEmpty())

            val idxUn = options.indexOf(itemUnidade).takeIf { it >= 0 } ?: 0
            spinnerUnKg.setSelection(idxUn)

            val idxCat = categorias.indexOf(itemCategoria).takeIf { it >= 0 } ?: 0
            spinnerCategoria.setSelection(idxCat)
        }

        findViewById<MaterialButton>(R.id.btnSalvar).setOnClickListener {
            val nome = etNome.text?.toString()?.trim().orEmpty()
            val quantidade = etQuantidade.text?.toString()?.toIntOrNull()
            val unidade = spinnerUnKg.selectedItem?.toString() ?: "un"
            val categoria = spinnerCategoria.selectedItem?.toString() ?: "Utilidades Domésticas e Outros"

            if (nome.isEmpty() || quantidade == null) {
                Toast.makeText(this, "Jovem, preencha os campos de nome e quantidade!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val data = Intent().apply {
                putExtra("item_id", itemId)
                putExtra("nome", nome)
                putExtra("quantidade", quantidade)
                putExtra("unidade", unidade)
                putExtra("categoria", categoria)
            }
            setResult(RESULT_OK, data)
            finish()
        }

        findViewById<MaterialButton>(R.id.btnCancelar).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}