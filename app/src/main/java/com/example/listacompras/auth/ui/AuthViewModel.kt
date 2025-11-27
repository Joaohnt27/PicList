package com.example.listacompras.auth.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.listacompras.auth.data.model.Usuario
import com.example.listacompras.auth.data.repository.AuthRepository
import com.example.listacompras.auth.data.repository.AuthRepositoryImpl
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel(
    // Instanciando o Repository aqui
    private val repository: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    // LiveData p/ observar o estado do Login
    private val _authResult = MutableLiveData<Result<Usuario?>>()
    val authResult: LiveData<Result<Usuario?>> = _authResult

    // LiveData p/ carregamento (mostrar barra de progresso)
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Obsevar o resultado da rec de senha
    private val _recoveryResult = MutableLiveData<Result<Boolean>>()
    val recoveryResult: LiveData<Result<Boolean>> = _recoveryResult

    fun login(email: String, pass: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.login(email, pass)
            _authResult.value = result
            _isLoading.value = false
        }
    }

    fun cadastro(nome: String, email: String, pass: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.cadastro(nome, email, pass)
            _authResult.value = result
            _isLoading.value = false
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return repository.getCurrentUser()
    }

    fun logout() {
        repository.logout()
    }

    fun recuperarSenha(email: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.recuperarSenha(email)
            _recoveryResult.value = result
            _isLoading.value = false
        }
    }
}