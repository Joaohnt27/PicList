package com.example.listacompras

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AddItemListaActivity : AppCompatActivity() {

    private lateinit var email: String
    private lateinit var adapter: ItensAdapter
    private lateinit var itemAtual: Lista
    private lateinit var rvItens: RecyclerView

    // Launcher para editar lista
    private val editarListaLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            val novoNome = data.getStringExtra("titulo") ?: return@registerForActivityResult
            val nomeAntigo = data.getStringExtra("nome_antigo")
            val editar = data.getBooleanExtra("editar", false)

            if (editar && nomeAntigo != null) {
                // Atualiza memória apenas da conta logada
                val lista = ListMemory.getByName(email, nomeAntigo)
                lista?.titulo = novoNome
                itemAtual = lista!!

                // Atualiza título na tela
                findViewById<MaterialButton>(R.id.btnEditar).text = novoNome
            }

            adapter.notifyDataSetChanged()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item_lista)

        email = Session.userEmail ?: return
        val nomeLista = intent.getStringExtra("nome_lista") ?: ""
        itemAtual = ListMemory.getByName(email, nomeLista) ?: Lista(nomeLista)

        rvItens = findViewById(R.id.rvListas)
        rvItens.layoutManager = GridLayoutManager(this, 2)
        adapter = ItensAdapter(itemAtual.itens) { /* clique no item */ }
        rvItens.adapter = adapter

        // Botão adicionar item
        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            val intent = Intent(this, AddItemActivity::class.java)
            startActivity(intent)
        }

        // Botão editar lista
        findViewById<MaterialButton>(R.id.btnEditar).setOnClickListener {
            val intent = Intent(this, AddListaActivity::class.java)
            intent.putExtra("nome_lista", itemAtual.titulo)
            editarListaLauncher.launch(intent)
        }

        // Botão voltar
        findViewById<MaterialButton>(R.id.btnVoltar).setOnClickListener {
            finish()
        }
    }
}
