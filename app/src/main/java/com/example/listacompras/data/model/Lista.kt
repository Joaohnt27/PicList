package com.example.listacompras.data.model

import java.io.Serializable

data class Lista(
    // ID gerado automaticamente pelo Firestore (precisa ser var para podermos alterar)
    var id: String = "",

    // Título da lista
    val titulo: String = "",

    // Titulo minusculo para normalizar a busca no Firestore (Ajustando pois o Firestore é case-insensitive)
    val titulo_lower: String = "",

    // Caminho da imagem (pode ser nulo se não tiver foto)
    val imageUri: String? = null,

    // ID do usuário dono da lista (para filtrar depois)
    val userId: String = ""
) : Serializable
