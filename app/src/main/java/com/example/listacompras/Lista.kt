package com.example.listacompras
data class Lista(
    var titulo: String,
    val imageRes: Int = android.R.drawable.ic_menu_gallery,
    val imageUri: String? = null,
    val itens: MutableList<Item> = mutableListOf() // adiciona itens
)
