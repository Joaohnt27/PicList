package com.example.listacompras.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.listacompras.ui.common.EspacamentoItens
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
        if (::email.isInitialized) {
            listaViewModel.buscarListas()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



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
                intent.putExtra("img_lista", item.imageUri)
                startActivity(intent)
            },
            onEdit = { lista ->
                val intent = Intent(this, AddListaActivity::class.java)
                intent.putExtra("nome_lista", lista.titulo)
                intent.putExtra("id_lista", lista.id)
                intent.putExtra("imageUri", lista.imageUri)
                editarListaLauncher.launch(intent)
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
                val texto = s.toString()
                listaViewModel.pesquisar(texto)
            }
        })
    }

    private fun normalizarTxt(s: String): String {
        val n = Normalizer.normalize(s.trim(), Normalizer.Form.NFD)
        return n.replace("\\p{M}+".toRegex(), "").lowercase()
    }

    private fun tituloExiste(titulo: String, idIgnorar: String? = null): Boolean {
        val alvo = normalizarTxt(titulo)

        // verifica na lista completa do Adapter
        return adapter.currentItems().any { itemLista ->
            // se o id for igual ao que está editando, pula a verificação
            if (itemLista.id == idIgnorar) return@any false

            // verifica se o nome bate
            normalizarTxt(itemLista.titulo) == alvo
        }
    }

    private val addListaLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val titulo = result.data?.getStringExtra("titulo") ?: return@registerForActivityResult
            val uriString = result.data?.getStringExtra("imageUri")

            val uri = if (!uriString.isNullOrEmpty()) Uri.parse(uriString) else null

            // chama sem id (pq é nova)
            if (tituloExiste(titulo)) {
                Toast.makeText(this, "Já existe uma lista com esse nome, jovem!", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            // Manda para o ViewModel salvar no Firebase
            listaViewModel.criarLista(titulo, uri)
        }
    }

    private val editarListaLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult

            val id = data.getStringExtra("id_lista")
            val novoNome = data.getStringExtra("titulo") ?: return@registerForActivityResult
            val novaUriString = data.getStringExtra("imageUri")

            // chamando com id pp/ permitir salvar o mesmo nome se for a própria lista
            if (tituloExiste(novoNome, idIgnorar = id)) {
                Toast.makeText(this, "Já existe outra lista com esse nome, jovem!", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            if (!id.isNullOrEmpty()) {
                val currentUserId = authViewModel.getCurrentUser()?.uid ?: ""

                val listaEditada = Lista(
                    id = id,
                    titulo = novoNome,
                    imageUri = novaUriString,
                    userId = currentUserId
                )

                val novaUri = if (!novaUriString.isNullOrEmpty()) Uri.parse(novaUriString) else null

                // salva no firebase
                listaViewModel.editarLista(listaEditada, novaUri)

                // limpa a busca para garantir que a lista apareça se tiver filtro
                binding.etBusca.text?.clear()

                Toast.makeText(this, "Lista atualizada!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}