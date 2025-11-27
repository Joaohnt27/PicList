package com.example.listacompras.ui.item

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.listacompras.databinding.ActivityAddItemBinding

class AddItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddItemBinding

    // ID agora é String (padrão firestore)
    private var itemId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val options = arrayOf("un", "kg", "L", "g") // add novas opções
        val adapterUnidade = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapterUnidade.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerUnKg.adapter = adapterUnidade

        val categorias = arrayOf(
            "Hortifrúti", "Padaria e Confeitaria", "Açougue e Peixaria",
            "Frios e Laticínios", "Congelados", "Mercearia Seca", "Doces e Snacks",
            "Bebidas", "Infantil", "Pet Shop", "Limpeza", "Higiene Pessoal e Beleza",
            "Saúde e Farmácia", "Utilidades Domésticas e Outros"
        )
        val adapterCategoria = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategoria.adapter = adapterCategoria

        // Recebendo dados da Intent (AddItemListaActivity)
        val itemNome = intent.getStringExtra("item_nome")
        val itemQuantidade = intent.getIntExtra("item_qtd", -1)
        val itemUnidade = intent.getStringExtra("item_un")
        val itemCategoria = intent.getStringExtra("item_cat")
        itemId = intent.getStringExtra("item_id")

        val isEdit = itemId != null // Se tem ID (String), é edição

        binding.tvTitulo.text = if (isEdit) "Editar item" else "Adicionar item"

        // Preenche os campos se for Edição
        if (isEdit) {
            binding.etNome.setText(itemNome.orEmpty())
            // Só preenche se for válido
            binding.etQuantidade.setText(if (itemQuantidade > 0) itemQuantidade.toString() else "")

            // Seleciona a Unidade correta no Spinner
            val idxUn = options.indexOf(itemUnidade)
            if (idxUn >= 0) binding.spinnerUnKg.setSelection(idxUn)

            // Seleciona a Categoria correta no Spinner
            val idxCat = categorias.indexOf(itemCategoria)
            if (idxCat >= 0) binding.spinnerCategoria.setSelection(idxCat)
        }

        // Botão Salvar
        binding.btnSalvar.setOnClickListener {
            val nome = binding.etNome.text?.toString()?.trim().orEmpty()
            val quantidade = binding.etQuantidade.text?.toString()?.toIntOrNull()
            val unidade = binding.spinnerUnKg.selectedItem?.toString() ?: "un"
            val categoria = binding.spinnerCategoria.selectedItem?.toString() ?: "Utilidades Domésticas e Outros"

            if (nome.isEmpty() || quantidade == null) {
                Toast.makeText(this, "Jovem, preencha os campos de nome e quantidade!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Retorna os dados para a tela anterior
            val data = Intent().apply {
                putExtra("item_id", itemId)
                putExtra("nome", nome)
                putExtra("nome_lower", nome.lowercase())
                putExtra("quantidade", quantidade)
                putExtra("unidade", unidade)
                putExtra("categoria", categoria)
            }
            setResult(RESULT_OK, data)
            finish()
        }

        // Botão Cancelar
        binding.btnCancelar.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }
}