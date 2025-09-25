package com.example.listacompras

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import android.widget.ImageView
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.button.MaterialButton

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

        findViewById<FloatingActionButton>(R.id.fabPick).setOnClickListener {
            pickImage.launch("image/*")
        }

        findViewById<MaterialButton>(R.id.btnSalvar).setOnClickListener {
            val nome = findViewById<TextInputEditText>(R.id.etNome).text?.toString()?.trim().orEmpty()
            if (nome.isEmpty()) {
                findViewById<TextInputEditText>(R.id.etNome).error = "Informe o nome"
                return@setOnClickListener
            }
            // Envia os daados para a MainActivity
            val data = Intent().apply {
                putExtra("titulo", nome)
                putExtra("imageUri", imageUri?.toString())
            }
            Toast.makeText(this, "Nova lista criada! ;)", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK, data)
            finish()
        }

        findViewById<MaterialButton>(R.id.btnCancelar).setOnClickListener {
            Toast.makeText(this, "Criação de lista cancelada.", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
}
