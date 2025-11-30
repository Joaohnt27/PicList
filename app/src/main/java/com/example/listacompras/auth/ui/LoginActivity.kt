package com.example.listacompras.auth.ui

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.listacompras.auth.ui.CadastroActivity
import com.example.listacompras.databinding.ActivityLoginBinding
import com.example.listacompras.main.ui.MainActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    // Implementação do ViewModel
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verifica se já existe usuário logado no Firebase ao abrir o app1
        checkAutoLogin()

        setupListeners()
        setupObservers()
        setupRecoveryObserver()
    }

    // Função p/ verificar se já existe usuário logado no Firebase
    private fun checkAutoLogin() {
        // Se tiver logado no Firebase, pula o login
        val currentUser = viewModel.getCurrentUser()
        if (currentUser != null) {
            goToHome(currentUser.email ?: "")
        }
    }

    private fun setupListeners() {
        binding.btnAcessar.setOnClickListener {
            val email = binding.etEmail.text?.toString().orEmpty()
            val senha = binding.etSenha.text?.toString().orEmpty()

            if (!email.contains("@")) {
                binding.tilEmail.error = "Informe um e-mail válido"
                return@setOnClickListener
            } else binding.tilEmail.error = null

            if (senha.isBlank()) {
                binding.tilSenha.error = "Informe a senha"
                return@setOnClickListener
            } else binding.tilSenha.error = null

            // chamada do ViewModel
            viewModel.login(email, senha)
        }

        binding.btnCriarConta.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }

        binding.tvEsqueceuSenha.setOnClickListener {
            showRecoverDialog()
        }
    }

    private fun showRecoverDialog() {
        val inputEmail = EditText(this)
        inputEmail.hint = "Digite seu e-mail"

        // Tenta preencher automaticamente se o usuário já digitou no campo de login
        inputEmail.setText(binding.etEmail.text.toString())

        MaterialAlertDialogBuilder(this)
            .setTitle("Recuperar Senha")
            .setMessage("Informe seu e-mail para receber o link de redefinição, jovem.")
            .setView(inputEmail)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Enviar") { _, _ ->
                val email = inputEmail.text.toString().trim()
                if (email.isNotEmpty()) {
                    viewModel.recuperarSenha(email)
                } else {
                    Toast.makeText(this, "Informe um e-mail válido", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun setupRecoveryObserver() {
        viewModel.recoveryResult.observe(this) { result ->
            result.onSuccess {
                MaterialAlertDialogBuilder(this)
                    .setTitle("E-mail enviado, jovem!")
                    .setMessage("Verifique sua caixa de entrada (e spam) para redefinir sua senha.")
                    .setPositiveButton("OK", null)
                    .show()
            }
            result.onFailure { e ->
                val msg = when {
                    e.message?.contains("user-not-found") == true -> "Usuário não encontrado."
                    e.message?.contains("badly formatted") == true -> "E-mail inválido."
                    else -> "Erro: ${e.message}"
                }
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    // Observa a resposta do Firebase (padrão Observer)
    private fun setupObservers() {
        viewModel.authResult.observe(this) { result ->
            result.onSuccess { user ->
                // Login com sucesso no Firebase
                goToHome(user?.email ?: "")
            }
            result.onFailure { exception ->
                // Erro no Login
                val mensagemErro = when(exception.message) {
                    // Tratar mensagens de erro do firebae (A FAZER) !!!!!!
                    else -> "Erro ao acessar: ${exception.localizedMessage}"
                }
                Snackbar.make(binding.root, mensagemErro, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            // Desabilita o botão de acessar para evitar vários cliques enquanto carrega
            binding.btnAcessar.isEnabled = !isLoading
            binding.btnAcessar.text = if (isLoading) "Carregando..." else "Acessar"
        }
    }

    private fun goToHome(email: String) {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}