package com.example.listacompras.main.ui

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
import com.example.listacompras.auth.ui.AuthViewModel
import com.example.listacompras.auth.ui.LoginActivity
import com.example.listacompras.common.EspacamentoItens
import com.example.listacompras.lista.data.model.Lista
import com.example.listacompras.databinding.ActivityMainBinding
import com.example.listacompras.lista.ui.AddItemListaActivity
import com.example.listacompras.lista.ui.AddListaActivity
import com.example.listacompras.lista.ui.ListaViewModel
import com.example.listacompras.lista.ui.ListasAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.Normalizer

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ListasAdapter

    // ViewModels
    private val authViewModel: AuthViewModel by viewModels()
    private val listaViewModel: ListaViewModel by viewModels()

    // Sempre que a tela aparecer (voltar da exclusão/edição), busca os dados
    override fun onResume() {
        super.onResume()
        // Se o usuário estiver logado, atualiza a lista
        if (authViewModel.getCurrentUser() != null) {
            listaViewModel.buscarListas()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentUser = authViewModel.getCurrentUser()
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val rv = binding.rvListas
        rv.layoutManager = GridLayoutManager(this, 2)
        rv.addItemDecoration(EspacamentoItens(12))

        adapter = ListasAdapter(
            mutableListOf(),
            onClick = { item ->
                val intent = Intent(this, AddItemListaActivity::class.java)
                intent.putExtra("id_lista", item.id)
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

        listaViewModel.listas.observe(this) { listas ->
            adapter.setItems(listas.toMutableList())
            adapter.notifyDataSetChanged()
        }

        listaViewModel.isLoading.observe(this) { isLoading ->
            binding.rvListas.alpha = if (isLoading) 0.5f else 1.0f
        }

        binding.btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Sair da conta?")
                .setMessage("Você deseja encerrar a sessão?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Sair") { _, _ ->
                    authViewModel.logout()
                    startActivity(Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
                .show()
        }

        binding.fabAdd.setOnClickListener {
            addListaLauncher.launch(Intent(this, AddListaActivity::class.java))
        }

        binding.etBusca.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                listaViewModel.pesquisar(s.toString())
            }
        })
    }

    private fun normalizarTxt(s: String): String {
        val n = Normalizer.normalize(s.trim(), Normalizer.Form.NFD)
        return n.replace("\\p{M}+".toRegex(), "").lowercase()
    }

    private fun tituloExiste(titulo: String, idIgnorar: String? = null): Boolean {
        val alvo = normalizarTxt(titulo)
        return adapter.currentItems().any { itemLista ->
            if (itemLista.id == idIgnorar) return@any false
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

            if (tituloExiste(titulo)) {
                Toast.makeText(this, "Já existe uma lista com esse nome, jovem!", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

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

            if (tituloExiste(novoNome, idIgnorar = id)) {
                Toast.makeText(this, "Já existe outra lista com esse nome, jovem!", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            if (!id.isNullOrEmpty()) {
                // Recupera o ID do usuário direto do Auth (Segurança)
                val currentUserId = authViewModel.getCurrentUser()?.uid ?: ""

                val listaEditada = Lista(
                    id = id,
                    titulo = novoNome,
                    imageUri = novaUriString,
                    userId = currentUserId
                )

                val novaUri = if (!novaUriString.isNullOrEmpty()) Uri.parse(novaUriString) else null

                listaViewModel.editarLista(listaEditada, novaUri)
                binding.etBusca.text?.clear() // Limpa busca p/ ver a lista editada

                Toast.makeText(this, "Lista atualizada!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}