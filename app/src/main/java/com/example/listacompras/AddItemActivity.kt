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

        // Configuração do Spinner de Unidades (un ou kg)
        val spinnerUnKg = findViewById<Spinner>(R.id.spinnerUnKg)
        val options = arrayOf("un", "kg")

        val adapterUnKg = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, // Layout para o Spinner de unidades
            options
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // Layout para o dropdown
        }

        spinnerUnKg.adapter = adapterUnKg // Atribui o adapter ao Spinner de unidades

        // Configuração do Spinner de categorias
        val spinnerCategoria = findViewById<Spinner>(R.id.spinnerCategoria)
        val categorias = arrayOf("Hortifrúti", "Padaria e Confeitaria", "Açougue e Peixaria", "Frios e Laticínios", "Congelados", "Mercearia Seca", "Doces e Snacks", "Bebidas", "Infantil", "Pet Shop", "Limpeza", "Higiene Pessoal e Beleza", "Saúde e Farmácia", "Utilidades Domésticas e Outros") // Categorias predefinidas

        val adapterCategoria = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categorias
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // Layout para o dropdown
        }
        spinnerCategoria.adapter = adapterCategoria // Atribui o adapter ao Spinner de categorias

        // Botão de salvar
        findViewById<MaterialButton>(R.id.btnSalvar).setOnClickListener {
            val nome = findViewById<TextInputEditText>(R.id.etNome).text?.toString()?.trim().orEmpty()
            val quantidade = findViewById<TextInputEditText>(R.id.etQuantidade).text?.toString()?.toIntOrNull()
            val unidade = spinnerUnKg.selectedItem.toString()
            val categoria = spinnerCategoria.selectedItem.toString()

            // Verifica se o nome e a qtd são válidos
            if (nome.isEmpty() || quantidade == null) {
                // Caso os dados sejam inválidos, exibe um aviso e não salva
                return@setOnClickListener
            }

            // Cria o item e salva em memória
            val item = Item(nome, quantidade, unidade, categoria)
            ItensMemory.itens.add(item)  // Adiciona o item à lista de itens na memória

            // Limpar os campos para adicionar outro item
            findViewById<TextInputEditText>(R.id.etNome).text?.clear()
            findViewById<TextInputEditText>(R.id.etQuantidade).text?.clear()
            spinnerCategoria.setSelection(0) // Resetando o Spinner de categorias para o primeiro item
            spinnerUnKg.setSelection(0) // Resetando o Spinner de unidades para o primeiro item
        }

        // Configuração do botão de cancelar
        findViewById<MaterialButton>(R.id.btnCancelar).setOnClickListener {
            // Ao clicar em "Cancelar", volta para a tela anterior
            onBackPressed()
        }
    }
}
