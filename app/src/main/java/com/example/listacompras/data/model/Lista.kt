package com.example.listacompras.data.model

import android.R

data class Lista(
    var titulo: String,
    val imageRes: Int = R.drawable.ic_menu_gallery,
    val imageUri: String? = null,
    val itens: MutableList<Item> = mutableListOf()
)