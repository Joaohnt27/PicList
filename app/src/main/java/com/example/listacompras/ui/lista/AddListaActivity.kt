package com.example.listacompras.ui.lista

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.listacompras.databinding.ActivityAddListaBinding // Import do Binding

class AddListaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddListaBinding
    private var imageUri: Uri? = null

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            imageUri = uri
            binding.imgPreview.setImageURI(uri)

            // --- CORREÇÃO IMPORTANTE PARA O FIREBASE STORAGE ---
            // Isso garante que o app tenha permissão persistente para ler o arquivo
            // mesmo depois de fechar a galeria. Sem isso, o upload falha silenciosamente.
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Algumas galerias não suportam persistência, mas a maioria sim.
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. Configuração do ViewBinding
        binding = ActivityAddListaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recebe dados para edição (se houver)
        val nomeAtual = intent.getStringExtra("nome_lista")
        val idLista = intent.getStringExtra("id_lista") // Agora precisamos do ID

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
                putExtra("id_lista", idLista) // Devolvemos o ID para o ViewModel saber quem atualizar
                putExtra("nome_antigo", nomeAtual)
            }

            Toast.makeText(this, "Salvando...", Toast.LENGTH_SHORT).show()

            setResult(Activity.RESULT_OK, data)
            finish()
        }

        // Botão Cancelar
        binding.btnCancelar.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
}