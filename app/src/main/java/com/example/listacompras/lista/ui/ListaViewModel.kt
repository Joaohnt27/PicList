package com.example.listacompras.lista.ui

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.listacompras.lista.data.model.Lista
import com.example.listacompras.lista.data.repository.ListaRepository
import com.example.listacompras.lista.data.repository.ListaRepositoryImpl
import kotlinx.coroutines.launch

class ListaViewModel : ViewModel() {

    private val repository: ListaRepository = ListaRepositoryImpl()

    private val _listas = MutableLiveData<List<Lista>>()
    val listas: LiveData<List<Lista>> = _listas

    private val _status = MutableLiveData<Result<Boolean>>()
    val status: LiveData<Result<Boolean>> = _status

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        buscarListas()
    }

    fun buscarListas() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.buscarListas()
            result.onSuccess { listaDeListas ->
                _listas.value = listaDeListas
            }
            _isLoading.value = false
        }
    }

    fun criarLista(titulo: String, uriImagem: Uri?) {
        _isLoading.value = true
        viewModelScope.launch {
            val novaLista = Lista(titulo = titulo)
            // Passamos a Uri local para o repository fazer o upload
            val result = repository.salvarLista(novaLista, uriImagem)

            _status.value = result
            if (result.isSuccess) buscarListas() // Atualiza a lista na tela
            _isLoading.value = false
        }
    }

    fun editarLista(lista: Lista, novaUri: Uri?) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.editarLista(lista, novaUri)
            _status.value = result

            if (result.isSuccess) {
                buscarListas()
            }
            _isLoading.value = false
        }
    }

    fun excluirLista(lista: Lista) {
        viewModelScope.launch {
            val result = repository.excluirLista(lista)
            if (result.isSuccess) {
                buscarListas() // Atualiza a tela removendo o item
            }
        }
    }

    fun pesquisar(query: String) {
        // Se a busca for vazia, traz tudo normal
        if (query.isBlank()) {
            buscarListas()
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            // Chama a nova query do repository
            val result = repository.pesquisarListas(query)
            result.onSuccess { listaFiltrada ->
                _listas.value = listaFiltrada
            }
            _isLoading.value = false
        }
    }
}