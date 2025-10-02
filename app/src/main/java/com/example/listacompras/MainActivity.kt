package com.example.listacompras

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: ListasAdapter
    private lateinit var email: String

    override fun onResume() {
        super.onResume()
        val listasOrdenadas = ListMemory.get(email).sortedBy { it.titulo.lowercase() }
        adapter.setItems(listasOrdenadas.toMutableList()) // Atualiza o adapter com as listas mais recentes
        adapter.notifyDataSetChanged() // Notifica o adapter que a lista foi alterada
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        email = Session.userEmail ?: run {
            finish()
            return
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnLogout)
            .setOnClickListener {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Sair da conta?")
                    .setMessage("Você deseja encerrar a sessão e voltar para a tela de login?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Sair") { _, _ ->
                        Session.userEmail = null  // Limpa apenas a sessão
                        startActivity(Intent(this, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                    }
                    .show()
            }

        val rv = findViewById<RecyclerView>(R.id.rvListas)
        rv.layoutManager = GridLayoutManager(this, 2)
        rv.addItemDecoration(EspacamentoItens(12))

        val listasOrdenadas = ListMemory.get(email).sortedBy { it.titulo.lowercase() } // ordem alfabéitca
        adapter = ListasAdapter(
            listasOrdenadas.toMutableList(),
            onClick = { item ->
                val intent = Intent(this, AddItemListaActivity::class.java)
                intent.putExtra("nome_lista", item.titulo)
                startActivity(intent)
            }
        )
        rv.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            addListaLauncher.launch(Intent(this, AddListaActivity::class.java))
        }

        val etBusca = findViewById<TextInputEditText>(R.id.etBusca)
        etBusca.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s?.toString() ?: "")
            }
        })
    }

    private fun persistirDados() {
        ListMemory.set(email, adapter.currentItems())  // -> Salva a lista no ListMemory
    }

    // Normaliza para comparar (sem acento e minúsculas)
    private fun normalizarTxt(s: String): String {
        val n = java.text.Normalizer.normalize(s.trim(), java.text.Normalizer.Form.NFD)
        return n.replace("\\p{M}+".toRegex(), "").lowercase()
    }

    // Verifica se já existe lista com esse título
    private fun tituloExiste(titulo: String): Boolean {
        val alvo = normalizarTxt(titulo)
        return ListMemory.get(email).any { normalizarTxt(it.titulo) == alvo }
    }

    private val addListaLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val titulo = result.data?.getStringExtra("titulo") ?: return@registerForActivityResult
            val uri = result.data?.getStringExtra("imageUri")

            if (tituloExiste(titulo)) {
                Toast.makeText(this, "Já existe uma lista com esse título, jovem!", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            // Adicionando a lista e salvando em memória
            adapter.addItem(Lista(titulo = titulo, imageUri = uri))
            persistirDados()  // Garante o salvamento das listas em memória
        }
    }

    private val editarListaLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            val novoNome = data.getStringExtra("titulo") ?: return@registerForActivityResult
            val nomeAntigo = data.getStringExtra("nome_antigo") ?: return@registerForActivityResult
            val editar = data.getBooleanExtra("editar", false)
            val novaUri = data.getStringExtra("imageUri")

            Toast.makeText(this, "Renomeando: $nomeAntigo -> $novoNome", Toast.LENGTH_SHORT).show()

            if (editar) {
                // Atualiza o adapter imediatamente
                adapter.renameByTitle(nomeAntigo, novoNome, novaUri)

                val ordenadas = adapter.currentItems().sortedBy { it.titulo.lowercase() }
                adapter.setItems(ordenadas)
                ListMemory.rename(email, nomeAntigo, novoNome)
                persistirDados()
            }
        }
    }
}
