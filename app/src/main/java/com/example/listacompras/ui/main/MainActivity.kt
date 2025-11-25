package com.example.listacompras.ui.main

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.listacompras.EspacamentoItens
import com.example.listacompras.Session
import com.example.listacompras.data.memory.ListMemory
import com.example.listacompras.data.model.Lista
import com.example.listacompras.databinding.ActivityMainBinding // Import do Binding
import com.example.listacompras.ui.auth.AuthViewModel
import com.example.listacompras.ui.lista.AddItemListaActivity
import com.example.listacompras.ui.lista.AddListaActivity
import com.example.listacompras.ui.lista.ListasAdapter
import com.example.listacompras.ui.login.LoginActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.Normalizer

class MainActivity : AppCompatActivity() {

    // Configuração do ViewBinding
    private lateinit var binding: ActivityMainBinding

    private lateinit var adapter: ListasAdapter
    private lateinit var email: String

    private val authViewModel: AuthViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        // Verifica se o email foi inicializado
        if (::email.isInitialized) {
            val listasOrdenadas = ListMemory.get(email).sortedBy { it.titulo.lowercase() }
            adapter.setItems(listasOrdenadas.toMutableList())
            adapter.notifyDataSetChanged()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        email = Session.userEmail ?: run {
            finish()
            return
        }

        binding.btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Sair da conta?")
                .setMessage("Você deseja encerrar a sessão e voltar para a tela de login?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Sair") { _, _ ->

                    // desloga do Firebase
                    authViewModel.logout()

                    // limpa a sessão local
                    Session.userEmail = null

                    // volta p/ o Login e limpa o histórico de telas
                    startActivity(Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
                .show()
        }

        // Configuração da RecyclerView
        val rv = binding.rvListas
        rv.layoutManager = GridLayoutManager(this, 2)
        rv.addItemDecoration(EspacamentoItens(12))

        val listasOrdenadas = ListMemory.get(email).sortedBy { it.titulo.lowercase() }
        adapter = ListasAdapter(
            listasOrdenadas.toMutableList(),
            onClick = { item ->
                val intent = Intent(this, AddItemListaActivity::class.java)
                intent.putExtra("nome_lista", item.titulo)
                startActivity(intent)
            }
        )
        rv.adapter = adapter

        // Botão adicionar nova lista
        binding.fabAdd.setOnClickListener {
            addListaLauncher.launch(Intent(this, AddListaActivity::class.java))
        }

        // Campo de busca
        binding.etBusca.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s?.toString() ?: "")
            }
        })
    }

    private fun persistirDados() {
        if (::email.isInitialized) {
            ListMemory.set(email, adapter.currentItems())
        }
    }

    private fun normalizarTxt(s: String): String {
        val n = Normalizer.normalize(s.trim(), Normalizer.Form.NFD)
        return n.replace("\\p{M}+".toRegex(), "").lowercase()
    }

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

            adapter.addItem(Lista(titulo = titulo, imageUri = uri))
            persistirDados()
        }
    }

    private val editarListaLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            val novoNome = data.getStringExtra("titulo") ?: return@registerForActivityResult
            val nomeAntigo = data.getStringExtra("nome_antigo") ?: return@registerForActivityResult
            val editar = data.getBooleanExtra("editar", false)
            val novaUri = data.getStringExtra("imageUri")

            Toast.makeText(this, "Renomeando: $nomeAntigo -> $novoNome", Toast.LENGTH_SHORT).show()

            if (editar) {
                adapter.renameByTitle(nomeAntigo, novoNome, novaUri)
                val ordenadas = adapter.currentItems().sortedBy { it.titulo.lowercase() }
                adapter.setItems(ordenadas)
                ListMemory.rename(email, nomeAntigo, novoNome)
                persistirDados()
            }
        }
    }
}