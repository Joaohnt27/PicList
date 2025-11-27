package com.example.listacompras.auth.data.model

data class Usuario(
    val id: String = "", // UID do Firebase Auth
    val nome: String = "",
    val email: String = ""
)