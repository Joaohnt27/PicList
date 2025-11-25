package com.example.listacompras.data.model

import java.io.Serializable

data class Lista(
    // ID gerado automaticamente pelo Firestore (precisa ser var para podermos alterar)
    var id: String = "",

    // Título da lista
    val titulo: String = "",

    // Caminho da imagem (pode ser nulo se não tiver foto)
    val imageUri: String? = null,

    // ID do usuário dono da lista (para filtrar depois)
    val userId: String = ""
) : Serializable
// Serializable ajuda a passar o objeto de uma tela para outra via Intent, se precisar.