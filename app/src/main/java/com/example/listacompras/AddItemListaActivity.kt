package com.example.listacompras

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class AddItemListaActivity : AppCompatActivity() {

    private lateinit var email: String
    private lateinit var adapter: ItensAdapter
    private lateinit var itemAtual: Lista
    private lateinit var rvItens: RecyclerView

    private val addItemLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        android.util.Log.d("ITENS", "launcher code=${result.resultCode}")
        if (result.resultCode == RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            val nome = data.getStringExtra("nome") ?: return@registerForActivityResult
            val quantidade = data.getIntExtra("quantidade", 1)
            val unidade = data.getStringExtra("unidade") ?: "un"
            val categoria = data.getStringExtra("categoria") ?: "Utilidades Domésticas e Outros"

            val novo = Item(nome, quantidade, unidade, categoria, false)

            val pos = itemAtual.itens.size
            itemAtual.itens.add(novo)
            adapter.notifyItemInserted(pos)
            rvItens.scrollToPosition(pos)

            android.widget.Toast.makeText(this, "Item adicionado: $nome", android.widget.Toast.LENGTH_SHORT).show()
            android.util.Log.d("ITENS", "inserido pos=$pos total=${itemAtual.itens.size}")
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

                findViewById<MaterialButton>(R.id.btnEditar).text = novoNome
                findViewById<TextView>(R.id.tvTitulo).text = novoNome
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

        findViewById<TextView>(R.id.tvTitulo).text = itemAtual.titulo

        rvItens = findViewById(R.id.rvListas)
        rvItens.layoutManager = LinearLayoutManager(this)
        adapter = ItensAdapter(itemAtual.itens) { /* clique no item */ }
        rvItens.adapter = adapter

        // Arrastar paraa deletar (Swipe-to-delete) - TESTANDO !!!
        val swipeToDelete = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
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
                        itemAtual.itens.add(pos, removido)
                        adapter.notifyItemInserted(pos)
                        rvItens.scrollToPosition(pos)
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
                if (dX > 0) { // direita
                    c.drawRect(
                        itemView.left.toFloat(), itemView.top.toFloat(),
                        itemView.left + dX, itemView.bottom.toFloat(), paint
                    )
                } else if (dX < 0) { // esquerda
                    c.drawRect(
                        itemView.right + dX, itemView.top.toFloat(),
                        itemView.right.toFloat(), itemView.bottom.toFloat(), paint
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

        findViewById<MaterialButton>(R.id.btnEditar).setOnClickListener {
            val intent = Intent(this, AddListaActivity::class.java)
            intent.putExtra("nome_lista", itemAtual.titulo)
            editarListaLauncher.launch(intent)
        }

        findViewById<MaterialButton>(R.id.btnVoltar).setOnClickListener { finish() }
    }
}
