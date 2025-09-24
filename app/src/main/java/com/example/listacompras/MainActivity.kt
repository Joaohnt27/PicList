package com.example.listacompras

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        email = Session.userEmail ?: run {
            finish()
            return
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnLogout) // -> TESTANDO!!!
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

        adapter = ListasAdapter(
            ListMemory.get(email).toMutableList()
        ) { item ->
            Toast.makeText(this, "Abrindo lista: ${item.titulo}", Toast.LENGTH_SHORT).show()
        }
        rv.adapter = adapter

        // TESTANDO !!
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

        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAdd)
            .setOnClickListener {
                Toast.makeText(this, "Nova lista criada! ;)", Toast.LENGTH_SHORT).show()
                addListaLauncher.launch(Intent(this, AddListaActivity::class.java))
            }
    }

    private fun persistirDados() {
        ListMemory.set(email, adapter.currentItems())  // -> Salva a lista no ListMemory
    }

    private val addListaLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val titulo = result.data?.getStringExtra("titulo") ?: return@registerForActivityResult
            val uri = result.data?.getStringExtra("imageUri")

            // Adicionando a lista e salvando em memória
            adapter.addItem(Lista(titulo = titulo, imageUri = uri))
            persistirDados()  // Garante o salvamento das listas em memória
        }
    }
}
