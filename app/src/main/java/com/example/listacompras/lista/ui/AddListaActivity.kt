package com.example.listacompras.lista.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.listacompras.databinding.ActivityAddListaBinding

class AddListaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddListaBinding
    private var imageUri: Uri? = null

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            imageUri = uri
            binding.imgPreview.setImageURI(uri)

            try {
                // funciona sem firebase storage
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // configuração do ViewBinding
        binding = ActivityAddListaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recebe dados para edição (se houver)
        val nomeAtual = intent.getStringExtra("nome_lista")
        val idLista = intent.getStringExtra("id_lista")

        // Preenche os campos se for edição
        if (!nomeAtual.isNullOrEmpty()) {
            binding.etNome.setText(nomeAtual)
            binding.btnSalvar.text = "Atualizar Lista"
        }

        // Botão para escolher imagem
        binding.fabPick.setOnClickListener {
            pickImage.launch("image/*")
        }

        // Botão Salvar
        binding.btnSalvar.setOnClickListener {
            val novoNome = binding.etNome.text?.toString()?.trim().orEmpty()

            if (novoNome.isEmpty()) {
                binding.etNome.error = "Informe o nome"
                return@setOnClickListener
            }

            val data = Intent().apply {
                putExtra("titulo", novoNome)
                // Envia a URI como string para a MainActivity converter e fazer upload
                putExtra("imageUri", imageUri?.toString())

                // Dados para controle de edição
                putExtra("editar", !nomeAtual.isNullOrEmpty())
                putExtra("id_lista", idLista)
                putExtra("nome_antigo", nomeAtual)
            }

            Toast.makeText(this, "Salvando...", Toast.LENGTH_SHORT).show()

            setResult(RESULT_OK, data)
            finish()
        }

        // Botão Cancelar
        binding.btnCancelar.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }
}