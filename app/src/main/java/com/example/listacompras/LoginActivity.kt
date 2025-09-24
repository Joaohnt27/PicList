package com.example.listacompras

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.listacompras.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {

    private lateinit var b: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnAcessar.setOnClickListener {
            val email = b.etEmail.text?.toString().orEmpty()
            val senha = b.etSenha.text?.toString().orEmpty()

            if (!email.contains("@")) {
                b.tilEmail.error = "Informe um e-mail válido"
                return@setOnClickListener
            } else b.tilEmail.error = null

            if (senha.isBlank()) {
                b.tilSenha.error = "Informe a senha"
                return@setOnClickListener
            } else b.tilSenha.error = null

            if (AuthMemory.isValid(email, senha)) {
                Session.userEmail = email          // guarda usuário atual em memória
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Snackbar.make(b.root, "Credenciais inválidas", Snackbar.LENGTH_SHORT).show()
            }
        }

       b.btnCriarConta.setOnClickListener {
         startActivity(Intent(this, CadastroActivity::class.java))
        }
    }
}
