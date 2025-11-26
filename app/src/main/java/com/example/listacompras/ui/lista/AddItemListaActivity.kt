package com.example.listacompras.ui.lista

import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.listacompras.ItensAdapter
import com.example.listacompras.R
import com.example.listacompras.data.model.Item
import com.example.listacompras.databinding.ActivityAddItemListaBinding
import com.example.listacompras.ui.item.AddItemActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.listacompras.data.model.Lista
import com.example.listacompras.ui.auth.AuthViewModel


class AddItemListaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddItemListaBinding
    private val itemViewModel: ItemViewModel by viewModels()
    private val listaViewModel: ListaViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var adapter: ItensAdapter
    private var idLista: String = ""
    private var nomeLista: String = ""
    private var imagemLista: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddItemListaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idLista = intent.getStringExtra("id_lista") ?: ""
        nomeLista = intent.getStringExtra("nome_lista") ?: ""
        imagemLista = intent.getStringExtra("img_lista")

        binding.tvTitulo.text = nomeLista

        setupRecyclerView()
        setupObservers()
        setupListeners()

        // Busca os dados iniciais
        itemViewModel.carregarItens(idLista)
    }

    private fun setupRecyclerView() {
        adapter = ItensAdapter(
            mutableListOf(),

            onClick = { item ->
                val intent = Intent(this, AddItemActivity::class.java).apply {
                    putExtra("item_id", item.id)
                    putExtra("item_nome", item.nome)
                    putExtra("item_qtd", item.quantidade)
                    putExtra("item_un", item.unidade)
                    putExtra("item_cat", item.categoria)
                }
                editItemLauncher.launch(intent)
            },

            onCheck = { item ->
                itemViewModel.atualizarItem(item)
            }
        )

        binding.rvListas.layoutManager = LinearLayoutManager(this)
        binding.rvListas.adapter = adapter

        // Swipe-to-delete
        val swipeToDelete = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            override fun onMove(
                rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                val pos = vh.bindingAdapterPosition
                val itemParaRemover = adapter.getItemAt(pos)

                itemViewModel.deletarItem(itemParaRemover)

                Snackbar.make(binding.root, "Item removido da lista", Snackbar.LENGTH_LONG)
                    .setAction("Desfazer") {
                        // Recria o item no Firestore se o usuário desfazer
                        itemViewModel.adicionarItem(itemParaRemover)
                    }.show()
            }

            override fun onChildDraw(
                c: Canvas,
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(c, rv, vh, dX, dY, actionState, isCurrentlyActive)

                val itemView = vh.itemView
                val paint = Paint().apply { color = Color.parseColor("#F44336") }

                val icon = ContextCompat.getDrawable(rv.context, R.drawable.delete_forever_24dp_e3e3e3_fill0_wght400_grad0_opsz24)

                // Desenha o Fundo Vermelho
                if (dX < 0) {
                    c.drawRect(
                        itemView.right.toFloat(), itemView.top.toFloat(),
                        itemView.right + dX, itemView.bottom.toFloat(), paint
                    )
                }

                // Desenha e Centraliza o Ícone
                icon?.let {
                    val margin = (itemView.height - it.intrinsicHeight) / 2
                    val top = itemView.top + margin
                    val bottom = top + it.intrinsicHeight

                    if (dX > 0) { // Swipe para direita (se ativar no futuro)
                        val left = itemView.left + margin
                        val right = left + it.intrinsicWidth
                        it.setBounds(left, top, right, bottom)
                    } else if (dX < 0) { // Swipe para esquerda (Delete)
                        val right = itemView.right - margin
                        val left = right - it.intrinsicWidth
                        it.setBounds(left, top, right, bottom)
                    }
                    it.draw(c)
                }
            }
        }
        ItemTouchHelper(swipeToDelete).attachToRecyclerView(binding.rvListas)
    }

    private fun setupObservers() {
        itemViewModel.itens.observe(this) { listaAtualizada ->
            adapter.updateList(listaAtualizada)
        }
    }

    private fun setupListeners() {
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, AddItemActivity::class.java)
            addItemLauncher.launch(intent)
        }

        binding.btnVoltar.setOnClickListener { finish() }

        binding.btnMenu.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.option_menu, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.editarLista -> {
                        val intent = Intent(this, AddListaActivity::class.java)

                        intent.putExtra("nome_lista", nomeLista)
                        intent.putExtra("id_lista", idLista)
                        intent.putExtra("imageUri", imagemLista)

                        editarListaLauncher.launch(intent)

                        true
                    }
                    R.id.excluirLista -> {
                        MaterialAlertDialogBuilder(this)
                            .setTitle("Excluir lista?")
                            .setPositiveButton("Excluir") { _, _ ->
                                val listaFake = com.example.listacompras.data.model.Lista(id = idLista)
                                listaViewModel.excluirLista(listaFake)
                                finish()
                            }
                            .setNegativeButton("Cancelar", null)
                            .show()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        // Busca de Itens
        binding.etBusca.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val texto = s.toString()
                // Chama o ViewModel passando o ID da lista atual e o texto
                itemViewModel.pesquisar(idLista, texto)
            }
        })
    }

    // Launchers
    private val addItemLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == RESULT_OK) {
            val data = res.data ?: return@registerForActivityResult
            val nome = data.getStringExtra("nome") ?: ""
            val qtd = data.getIntExtra("quantidade", 1)
            val un = data.getStringExtra("unidade") ?: "un"
            val cat = data.getStringExtra("categoria") ?: "Outros"

            val novoItem = Item(
                listaId = idLista, // Vincula à lista pai!
                nome = nome,
                quantidade = qtd,
                unidade = un,
                categoria = cat
            )
            itemViewModel.adicionarItem(novoItem)
        }
    }

    private val editItemLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == RESULT_OK) {
            val data = res.data ?: return@registerForActivityResult

            val id = data.getStringExtra("item_id")
            val nome = data.getStringExtra("nome")
            val qtd = data.getIntExtra("quantidade", 1)
            val un = data.getStringExtra("unidade") ?: "un"
            val cat = data.getStringExtra("categoria") ?: "Outros"

            if (!id.isNullOrEmpty() && !nome.isNullOrEmpty()) {
                val itemEditado = Item(
                    id = id,
                    listaId = idLista,
                    nome = nome,
                    quantidade = qtd,
                    unidade = un,
                    categoria = cat,
                    marcado = false // Mantém desmarcado ao editar item
                )
                itemViewModel.atualizarItem(itemEditado)
                Toast.makeText(this, "Item atualizado, jovem!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val editarListaLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult

            val novoNome = data.getStringExtra("titulo")
            val novaUriString = data.getStringExtra("imageUri")

            if (!novoNome.isNullOrEmpty()) {
                val currentUserId = authViewModel.getCurrentUser()?.uid ?: ""

                // cria o objeto com o msm id para o firebase entender que é update
                val listaEditada = Lista(
                    id = idLista,
                    titulo = novoNome,
                    userId = currentUserId,
                )

                val novaUri = if (!novaUriString.isNullOrEmpty()) Uri.parse(novaUriString) else null

                // chama o ViewModel p/ salvar
                listaViewModel.editarLista(listaEditada, novaUri)

                // Atualiza o título na tela
                binding.tvTitulo.text = novoNome
                // Atualiza a variável local
                nomeLista = novoNome

                Toast.makeText(this, "Lista atualizada, jovem!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}