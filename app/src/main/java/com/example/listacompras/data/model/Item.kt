package com.example.listacompras.data.model

data class Item(
    var id: String = "",        // ID do Firestore (String)
    val listaId: String = "",   // ID da Lista pai
    val nome: String = "",
    val nome_lower: String = "",
    val quantidade: Int = 1,
    val unidade: String = "un",
    val categoria: String = "Outros",
    var marcado: Boolean = false
)