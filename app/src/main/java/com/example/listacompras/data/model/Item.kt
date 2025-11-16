package com.example.listacompras.data.model

data class Item(
    val id: Int,
    var nome: String,
    var quantidade: Int,
    var unidade: String,
    var categoria: String,
    var marcado: Boolean
)