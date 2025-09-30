package com.example.listacompras

data class Item(
    val nome: String,
    val quantidade: Int,
    val unidade: String,
    val categoria: String,
    var marcado: Boolean = false
)
