package com.example.listacompras.ui.cadastro

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.listacompras.databinding.ActivityCadastroBinding
import com.example.listacompras.ui.auth.AuthViewModel
import com.example.listacompras.ui.main.MainActivity
import com.google.android.material.snackbar.Snackbar

class CadastroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroBinding

    // Injeção de dependência do ViewModel
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.btnCriarConta.setOnClickListener {
            val nome  = binding.etNome.text?.toString().orEmpty()
            val email = binding.etEmail.text?.toString().orEmpty()
            val senha = binding.etSenha.text?.toString().orEmpty()
            val conf  = binding.etConfirmar.text?.toString().orEmpty()

            var ok = true
            if (nome.isBlank()) {
                binding.tilNome.error = "Informe o nome"
                ok = false
            } else binding.tilNome.error = null

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.error = "E-mail inválido"
                ok = false
            } else binding.tilEmail.error = null

            if (senha.length < 8) {
                binding.tilSenha.error = "Mínimo de 8 caracteres"
                ok = false
            } else binding.tilSenha.error = null

            if (conf != senha) {
                binding.tilConfirmar.error = "As senhas não coincidem"
                ok = false
            } else binding.tilConfirmar.error = null

            if (!ok) return@setOnClickListener

            viewModel.cadastro(nome, email, senha)
        }

        binding.btnVoltarLogin.setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        // Observa o resultado do cadastro
        viewModel.authResult.observe(this) { result ->
            result.onSuccess { user ->
                Toast.makeText(this, "Conta criada com sucesso, jovem! ;)", Toast.LENGTH_SHORT).show()

                // No Firebase, criar conta já faz o login automático (vai direto p/ main).
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finishAffinity() // fecha Login e Cadastro para o usuário não voltar com o botão "Voltar"
            }

            result.onFailure { exception ->
                // Tratamento de erros comuns do Firebase
                val msg = when {
                    exception.message?.contains("email address is already in use") == true -> "E-mail já cadastrado"
                    exception.message?.contains("network") == true -> "Sem conexão com a internet"
                    else -> "Erro: ${exception.localizedMessage}"
                }
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
            }
        }

        // Carregamento visual
        viewModel.isLoading.observe(this) { isLoading ->
            binding.btnCriarConta.isEnabled = !isLoading
            binding.btnCriarConta.text = if (isLoading) "Criando..." else "Criar Conta"
        }
    }
}