package com.example.listacompras

object ListMemory {
    private val data = mutableMapOf<String, MutableList<Lista>>()

    fun get(email: String): MutableList<Lista> =
        data.getOrPut(email) { mutableListOf() }

    fun set(email: String, items: List<Lista>) {
        data[email] = items.toMutableList()
    }

    fun getByName(email: String, nome: String): Lista? {
        return data[email]?.find { it.titulo == nome }
    }

    fun rename(email: String, antigo: String, novo: String) {
        val lista = getByName(email, antigo) ?: return
        lista.titulo = novo
    }

    fun remove(email: String, nomeLista: String) {
        val listas = data[email] ?: return
        listas.removeIf { it.titulo == nomeLista }
        set(email, listas)
    }
}


