package com.example.listacompras.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.listacompras.EspacamentoItens
import com.example.listacompras.Session
import com.example.listacompras.data.model.Lista
import com.example.listacompras.databinding.ActivityMainBinding
import com.example.listacompras.ui.auth.AuthViewModel
import com.example.listacompras.ui.lista.AddItemListaActivity
import com.example.listacompras.ui.lista.AddListaActivity
import com.example.listacompras.ui.lista.ListaViewModel
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
    private val listaViewModel: ListaViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        // Recarrega do Firebase para garantir atualização ao voltar de outras telas
        if (::email.isInitialized) {
            listaViewModel.buscarListas()
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

        val rv = binding.rvListas
        rv.layoutManager = GridLayoutManager(this, 2)
        rv.addItemDecoration(EspacamentoItens(12))

        // Inicializa o adapter vazio
        adapter = ListasAdapter(
            mutableListOf(),
            onClick = { item ->
                val intent = Intent(this, AddItemListaActivity::class.java)
                intent.putExtra("id_lista", item.id)    // ID do Firestore
                intent.putExtra("nome_lista", item.titulo)
                startActivity(intent)
            }
        )
        rv.adapter = adapter

        // quando o Firebase devolver dados, atualiza a tela
        listaViewModel.listas.observe(this) { listas ->
            adapter.setItems(listas.toMutableList())
            adapter.notifyDataSetChanged()
        }

        // Feedback visual de carregamento
        listaViewModel.isLoading.observe(this) { isLoading ->
            binding.rvListas.alpha = if (isLoading) 0.5f else 1.0f
        }

        binding.btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Sair da conta?")
                .setMessage("Você deseja encerrar a sessão e voltar para a tela de login?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Sair") { _, _ ->
                    authViewModel.logout()
                    Session.userEmail = null
                    startActivity(Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
                .show()
        }

        // Botão adicionar nova lista
        binding.fabAdd.setOnClickListener {
            addListaLauncher.launch(Intent(this, AddListaActivity::class.java))
        }

        // Campo de busca
        binding.etBusca.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Filtra localmente o que já tá no adapter
                adapter.filter(s?.toString() ?: "")
            }
        })
    }

    private fun normalizarTxt(s: String): String {
        val n = Normalizer.normalize(s.trim(), Normalizer.Form.NFD)
        return n.replace("\\p{M}+".toRegex(), "").lowercase()
    }

    private fun tituloExiste(titulo: String): Boolean {
        val alvo = normalizarTxt(titulo)
        return adapter.currentItems().any { normalizarTxt(it.titulo) == alvo }
    }

    private val addListaLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val titulo = result.data?.getStringExtra("titulo") ?: return@registerForActivityResult
            val uriString = result.data?.getStringExtra("imageUri")

            val uri = if (!uriString.isNullOrEmpty()) Uri.parse(uriString) else null

            // Manda para o ViewModel salvar no Firebase
            listaViewModel.criarLista(titulo, uri)
        }
    }

    private val editarListaLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            val novoNome = data.getStringExtra("titulo") ?: return@registerForActivityResult
            val novaUriString = data.getStringExtra("imageUri")

            // Tenta recuperar o objeto Lista
            val listaOriginal = data.getSerializableExtra("lista_original") as? Lista

            if (listaOriginal != null) {
                val listaEditada = listaOriginal.copy(titulo = novoNome)
                val novaUri = if (novaUriString != null) Uri.parse(novaUriString) else null

                listaViewModel.editarLista(listaEditada, novaUri)
            }
        }
    }
}