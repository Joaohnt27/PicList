package com.example.listacompras

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class AddListaActivity : AppCompatActivity() {

    private var imageUri: Uri? = null

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            imageUri = uri
            findViewById<ImageView>(R.id.imgPreview).setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_lista)

        val etNome = findViewById<TextInputEditText>(R.id.etNome)
        // Recebe o nome da lista atual, se houver permite edição
        val nomeAtual = intent.getStringExtra("nome_lista")
        if (!nomeAtual.isNullOrEmpty()) {
            etNome.setText(nomeAtual)
        }

        findViewById<FloatingActionButton>(R.id.fabPick).setOnClickListener {
            pickImage.launch("image/*")
        }

        // Botão salvar (criação ou edição)
        findViewById<MaterialButton>(R.id.btnSalvar).setOnClickListener {
            val novoNome = etNome.text?.toString()?.trim().orEmpty()
            if (novoNome.isEmpty()) {
                etNome.error = "Informe o nome"
                return@setOnClickListener
            }

            val data = Intent().apply {
                putExtra("titulo", novoNome)
                putExtra("imageUri", imageUri?.toString())
                putExtra("editar", !nomeAtual.isNullOrEmpty()) // indica se é edição
                putExtra("nome_antigo", nomeAtual) // para atualizar memória se for edição
            }

            Toast.makeText(
                this,
                if (nomeAtual != null) "Lista atualizada, jovem! ;)" else "Nova lista criada! ;)",
                Toast.LENGTH_SHORT
            ).show()

            setResult(Activity.RESULT_OK, data)
            finish()
        }

        // Botão cancelar
        findViewById<MaterialButton>(R.id.btnCancelar).setOnClickListener {
            Toast.makeText(
                this,
                if (nomeAtual != null) "Atualização de lista cancelada, jovem. :(" else "Criação de lista cancelada, jovem. :(",
                Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
}
