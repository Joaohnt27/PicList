package com.example.listacompras.ui.lista

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.listacompras.ui.item.AddItemActivity
import com.example.listacompras.ItensAdapter
import com.example.listacompras.R
import com.example.listacompras.Session
import com.example.listacompras.data.memory.ListMemory
import com.example.listacompras.data.model.Item
import com.example.listacompras.data.model.Lista
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import java.text.Collator
import java.util.Locale

class AddItemListaActivity : AppCompatActivity() {

    private lateinit var email: String
    private lateinit var adapter: ItensAdapter
    private lateinit var itemAtual: Lista
    private lateinit var rvItens: RecyclerView

    // Função para comparar strings sem considerar acentos e case
    private val collator: Collator = Collator.getInstance(Locale("pt", "BR")).apply {
        strength = Collator.PRIMARY
    }

    // Função para ordenar categorias conforme a lista predefinida
    private val ordemCategorias = listOf("Hortifrúti", "Padaria e Confeitaria", "Açougue e Peixaria", "Frios e Laticínios", "Congelados", "Mercearia Seca", "Doces e Snacks", "Bebidas", "Infantil", "Pet Shop", "Limpeza", "Higiene Pessoal e Beleza", "Saúde e Farmácia", "Utilidades Domésticas e Outros")

    private fun ordenarItens(notify: Boolean = true) {
        // Filtra os itens marcados e não marcados
        val checkedItems = itemAtual.itens.filter { it.marcado }
        val uncheckedItems = itemAtual.itens.filterNot { it.marcado }

        // Ordena os itens não marcados por categoria (de acordo com ordemCategorias) e nome
        val sortedUncheckedItems = uncheckedItems.sortedWith(Comparator { a, b ->
            val idxA = ordemCategorias.indexOf(a.categoria).let { if (it >= 0) it else Int.MAX_VALUE }
            val idxB = ordemCategorias.indexOf(b.categoria).let { if (it >= 0) it else Int.MAX_VALUE }
            val catComparison = idxA.compareTo(idxB) // Compara as categorias pela ordem em ordemCategorias

            if (catComparison != 0) catComparison
            else collator.compare(a.nome, b.nome) // Ordena por nome dentro da categoria
        })

        // Ordena os itens marcados da mesma forma
        val sortedCheckedItems = checkedItems.sortedWith(Comparator { a, b ->
            val idxA = ordemCategorias.indexOf(a.categoria).let { if (it >= 0) it else Int.MAX_VALUE }
            val idxB = ordemCategorias.indexOf(b.categoria).let { if (it >= 0) it else Int.MAX_VALUE }
            val catComparison = idxA.compareTo(idxB) // Compara as categorias pela ordem em ordemCategorias

            if (catComparison != 0) catComparison
            else collator.compare(a.nome, b.nome) // Ordena por nome dentro da categoria
        })

        // Recria a lista final, com os itens não marcados primeiro, seguidos pelos itens marcados
        val newList = sortedUncheckedItems + sortedCheckedItems

        // Atualiza a lista original
        itemAtual.itens.clear()
        itemAtual.itens.addAll(newList)

        if (notify) adapter.notifyDataSetChanged()
    }

    private val addItemLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("ITENS", "launcher code=${result.resultCode}")
        if (result.resultCode == RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            val nome = data.getStringExtra("nome") ?: return@registerForActivityResult
            val quantidade = data.getIntExtra("quantidade", 1)
            val unidade = data.getStringExtra("unidade") ?: "un"
            val categoria = data.getStringExtra("categoria") ?: "Utilidades Domésticas e Outros"

            val novoId = itemAtual.itens.size + 1
            val novo = Item(novoId, nome, quantidade, unidade, categoria, false)

            val pos = itemAtual.itens.size
            itemAtual.itens.add(novo)
            adapter.notifyItemInserted(pos)
            rvItens.scrollToPosition(pos)

            Toast.makeText(this, "Item adicionado: $nome", Toast.LENGTH_SHORT).show()

            // Ela reordena a lista e atualiza o adapter
            ordenarItens()
            val newIndex = itemAtual.itens.indexOf(novo)
            if (newIndex >= 0) rvItens.scrollToPosition(newIndex)
        }
    }

    private val deleteListaLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val intent = Intent()
            setResult(RESULT_OK, intent) // Envia um resultado de sucesso
            finish() // Finaliza e retorna para a pág. anterior
        }
    }

    private val editItemLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            val id = data.getIntExtra("item_id", -1)
            val nome = data.getStringExtra("nome") ?: return@registerForActivityResult
            val quantidade = data.getIntExtra("quantidade", 1)
            val unidade = data.getStringExtra("unidade") ?: "un"
            val categoria = data.getStringExtra("categoria") ?: "Utilidades Domésticas e Outros"

            // Atualiza o item pelo ID
            val idx = itemAtual.itens.indexOfFirst { it.id == id }
            if (idx != -1) {
                val it = itemAtual.itens[idx]
                it.nome = nome
                it.quantidade = quantidade
                it.unidade = unidade
                it.categoria = categoria

                // Reordena e atualiza a UI
                ordenarItens(notify = false)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private val editarListaLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            val novoNome = data.getStringExtra("titulo") ?: return@registerForActivityResult
            val nomeAntigo = data.getStringExtra("nome_antigo")
            val editar = data.getBooleanExtra("editar", false)

            if (editar && nomeAntigo != null) {
                val lista = ListMemory.getByName(email, nomeAntigo)
                lista?.titulo = novoNome
                itemAtual = lista!!

               // findViewById<MaterialButton>(R.id.btnEditar).text = novoNome
                findViewById<TextView>(R.id.tvTitulo).text = novoNome
            }
            adapter.notifyDataSetChanged()
        }
    }

    // Func p/ excluir a lista
    private fun excluirLista() {
        ListMemory.remove(email, itemAtual.titulo)

        // Retorna para a MainActivity com um resultado indicando que a lista foi excluída
        val intent = Intent()
        setResult(RESULT_OK, intent) // Passando o resultado de sucesso caso ela tenha sido excluida com sucesso
        finish() // Finaliza e volta p/ MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item_lista)

        email = Session.userEmail ?: return
        val nomeLista = intent.getStringExtra("nome_lista") ?: ""
        itemAtual = ListMemory.getByName(email, nomeLista) ?: Lista(nomeLista)

        findViewById<TextView>(R.id.tvTitulo).text = itemAtual.titulo

        // ordena antes de renderizar
        ordenarItens(notify = false)
        rvItens = findViewById(R.id.rvListas)
        rvItens.layoutManager = LinearLayoutManager(this)
        adapter = ItensAdapter(itemAtual.itens) { item ->
            val intent = Intent(this, AddItemActivity::class.java).apply {
                putExtra("item_id", item.id)
                putExtra("item_nome", item.nome)
                putExtra("item_quantidade", item.quantidade)
                putExtra("item_unidade", item.unidade)
                putExtra("item_categoria", item.categoria)
            }
            editItemLauncher.launch(intent)
        }
        rvItens.adapter = adapter

        val etBusca = findViewById<TextInputEditText>(R.id.etBusca)
        etBusca.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s?.toString() ?: "")
            }
        })

        // Arrastar paraa deletar (Swipe-to-delete) - TESTANDO !!!
        val swipeToDelete = object : ItemTouchHelper.SimpleCallback(
            0,  ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                val pos = vh.bindingAdapterPosition
                val removido = adapter.getItemAt(pos)

                adapter.removeAt(pos)

                // Snackbar para desfazer
                Snackbar.make(rvItens, "Item removido da lista", Snackbar.LENGTH_LONG)
                    .setAction("Desfazer") {
                        itemAtual.itens.add(removido) // Adiciona o item de volta
                        ordenarItens()                // Reordena e atualiza tudo
                        val idx = itemAtual.itens.indexOf(removido)
                        if (idx >= 0) rvItens.scrollToPosition(idx)
                    }.show()
            }
            // Quando arrastar, cria o fundo vermelho e aparece o ícone da lixira
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

                // Fundo
                if (dX < 0) {
                    c.drawRect(
                        itemView.right.toFloat(), itemView.top.toFloat(),
                        itemView.right + dX, itemView.bottom.toFloat(), paint
                    )
                }

                // Centralização do ícone
                icon?.let {
                    val margin = (itemView.height - it.intrinsicHeight) / 2
                    val top = itemView.top + margin
                    val bottom = top + it.intrinsicHeight
                    if (dX > 0) {
                        val left = itemView.left + margin
                        val right = left + it.intrinsicWidth
                        it.setBounds(left, top, right, bottom)
                    } else if (dX < 0) {
                        val right = itemView.right - margin
                        val left = right - it.intrinsicWidth
                        it.setBounds(left, top, right, bottom)
                    }
                    it.draw(c)
                }
            }
        }
        ItemTouchHelper(swipeToDelete).attachToRecyclerView(rvItens) // Responsável por fazer o RecycleView entender o movimento de deslizar

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            val intent = Intent(this, AddItemActivity::class.java)
            addItemLauncher.launch(intent)
        }

        val btnMenu = findViewById<MaterialButton>(R.id.btnMenu)

        btnMenu.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.option_menu, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.editarLista -> {
                        val intent = Intent(this, AddListaActivity::class.java)
                        intent.putExtra("nome_lista", itemAtual.titulo)
                        editarListaLauncher.launch(intent)
                        true
                    }
                    R.id.excluirLista -> {
                        // Confirmação antes de excluir
                        MaterialAlertDialogBuilder(this)
                            .setTitle("Excluir a lista?")
                            .setMessage("Tem certeza que deseja excluir \"${itemAtual.titulo}\" , jovem?")
                            .setNegativeButton("Cancelar", null)
                            .setPositiveButton("Excluir") { _, _ ->
                                excluirLista() // Método para excluir a lista
                            }
                            .show()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        findViewById<MaterialButton>(R.id.btnVoltar).setOnClickListener { finish() }
    }
}