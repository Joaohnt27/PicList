package com.example.listacompras

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.listacompras.databinding.ActivityCadastroBinding
import com.google.android.material.snackbar.Snackbar

class CadastroActivity : AppCompatActivity() {

    private lateinit var b: ActivityCadastroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnCriarConta.setOnClickListener {
            val nome  = b.etNome.text?.toString().orEmpty()
            val email = b.etEmail.text?.toString().orEmpty()
            val senha = b.etSenha.text?.toString().orEmpty()
            val conf  = b.etConfirmar.text?.toString().orEmpty()

            var ok = true
            if (nome.isBlank()) {
                b.tilNome.error = "Informe o nome";
                ok = false
            } else b.tilNome.error = null
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                b.tilEmail.error = "E-mail inválido";
                ok = false
            } else b.tilEmail.error = null
            if (senha.length < 8) {
                b.tilSenha.error = "Mínimo de 8 caracteres";
                ok = false
            } else b.tilSenha.error = null
            if (conf != senha) {
                b.tilConfirmar.error = "As senhas não coincidem";
                ok = false
            } else b.tilConfirmar.error = null
            if (!ok) return@setOnClickListener

            // Tenta cadastrar o usuário
            if (!AuthMemory.register(Usuario(nome, email, senha))) {
                Snackbar.make(b.root, "E-mail já cadastrado", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(this, "Conta criada com sucesso, jovem! ;)", Toast.LENGTH_SHORT).show()

            // Volta para a tela de Login já preenchendo o e-mail que foi criado
            val i = Intent(this, LoginActivity::class.java).apply {
                putExtra("prefill_email", email)
            }
            startActivity(i)
            finish()
        }

        b.btnVoltarLogin.setOnClickListener {
            finish() // Fecha a tela de cadastro e volta pro login
        }
    }
}
