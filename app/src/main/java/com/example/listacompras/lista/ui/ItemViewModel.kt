package com.example.listacompras.lista.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.listacompras.item.data.model.Item
import com.example.listacompras.item.data.repository.ItemRepository
import com.example.listacompras.item.data.repository.ItemRepositoryImpl
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.Locale

class ItemViewModel : ViewModel() {

    private val repository: ItemRepository = ItemRepositoryImpl()

    private val _itens = MutableLiveData<List<Item>>()
    val itens: LiveData<List<Item>> = _itens

    // Sua lista de categorias (copiei do seu código)
    private val ordemCategorias = listOf("Hortifrúti", "Padaria e Confeitaria", "Açougue e Peixaria", "Frios e Laticínios", "Congelados", "Mercearia Seca", "Doces e Snacks", "Bebidas", "Infantil", "Pet Shop", "Limpeza", "Higiene Pessoal e Beleza", "Saúde e Farmácia", "Utilidades Domésticas e Outros")
    private val collator = Collator.getInstance(Locale("pt", "BR")).apply { strength = Collator.PRIMARY }

    fun carregarItens(listaId: String) {
        viewModelScope.launch {
            repository.buscarItens(listaId).onSuccess { listaBruta ->
                _itens.value = ordenarLista(listaBruta)
            }
        }
    }

    fun adicionarItem(item: Item) {
        viewModelScope.launch {
            repository.salvarItem(item).onSuccess {
                carregarItens(item.listaId) // Recarrega
            }
        }
    }

    fun atualizarItem(item: Item) {
        viewModelScope.launch {
            repository.atualizarItem(item).onSuccess {
                carregarItens(item.listaId)
            }
        }
    }

    fun deletarItem(item: Item) {
        viewModelScope.launch {
            repository.deletarItem(item.id).onSuccess {
                carregarItens(item.listaId)
            }
        }
    }

    // Lógica de ordenação (mantida do seu código original)
    private fun ordenarLista(lista: List<Item>): List<Item> {
        val checked = lista.filter { it.marcado }
        val unchecked = lista.filterNot { it.marcado }

        val comparator = Comparator<Item> { a, b ->
            val idxA = ordemCategorias.indexOf(a.categoria).let { if (it >= 0) it else 999 }
            val idxB = ordemCategorias.indexOf(b.categoria).let { if (it >= 0) it else 999 }
            val catDiff = idxA.compareTo(idxB)
            if (catDiff != 0) catDiff else collator.compare(a.nome, b.nome)
        }

        return unchecked.sortedWith(comparator) + checked.sortedWith(comparator)
    }

    fun pesquisar(listaId: String, query: String) {
        if (query.isBlank()) {
            carregarItens(listaId)
            return
        }

        viewModelScope.launch {
            repository.pesquisarItens(listaId, query).onSuccess { listaBruta ->
                // Ainda aplicamos a ordenação visual (categorias) no resultado da busca
                _itens.value = ordenarLista(listaBruta)
            }
        }
    }
}