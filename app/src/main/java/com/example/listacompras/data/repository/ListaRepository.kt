package com.example.listacompras.data.repository

import android.net.Uri
import com.example.listacompras.data.model.Lista
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

interface ListaRepository {
    suspend fun salvarLista(lista: Lista, imageUriLocal: Uri?): Result<Boolean>
    suspend fun buscarListas(): Result<List<Lista>>
    suspend fun excluirLista(lista: Lista): Result<Boolean>
    suspend fun editarLista(lista: Lista, novaImageUri: Uri?): Result<Boolean>
    suspend fun pesquisarListas(query: String): Result<List<Lista>>
}

class ListaRepositoryImpl : ListaRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val collectionName = "listas"

    override suspend fun salvarLista(lista: Lista, imageUriLocal: Uri?): Result<Boolean> {
        return try {
            val user = auth.currentUser ?: throw Exception("Usuário Off")
            val docRef = db.collection(collectionName).document()

            // O Firestore vai guardar o caminho dentro do celular
            val caminhoLocalImagem = imageUriLocal?.toString() ?: ""

            val novaLista = lista.copy(
                id = docRef.id,
                userId = user.uid,
                imageUri = caminhoLocalImagem, // Salva o caminho local
                titulo_lower = lista.titulo.lowercase()
            )

            docRef.set(novaLista).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun buscarListas(): Result<List<Lista>> {
        return try {
            val user = auth.currentUser ?: throw Exception("Usuário Off")

            val snapshot = db.collection(collectionName)
                .whereEqualTo("userId", user.uid)
                .get()
                .await()

            val listas = snapshot.toObjects(Lista::class.java)
                .sortedBy { it.titulo.lowercase() }

            Result.success(listas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun excluirLista(lista: Lista): Result<Boolean> {
        return try {

            val itensRef = db.collection("itens")
                .whereEqualTo("listaId", lista.id)
                .get()
                .await()

            val batch = db.batch()
            for (document in itensRef) {
                batch.delete(document.reference)
            }
            batch.commit().await()

            // Apagar Lista
            db.collection(collectionName).document(lista.id).delete().await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun editarLista(lista: Lista, novaImageUri: Uri?): Result<Boolean> {
        return try {
            // 1. Cria uma cópia garantindo que o titulo_lower acompanhe o titulo novo
            var listaAtualizada = lista.copy(
                titulo_lower = lista.titulo.lowercase() // <--- A CORREÇÃO MÁGICA É ESSA LINHA
            )

            // 2. Se tiver imagem nova, atualiza também
            if (novaImageUri != null) {
                listaAtualizada = listaAtualizada.copy(imageUri = novaImageUri.toString())
            }

            // 3. Salva no banco
            db.collection(collectionName).document(lista.id).set(listaAtualizada).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun pesquisarListas(query: String): Result<List<Lista>> {
        return try {
            val user = auth.currentUser ?: throw Exception("Usuário Off")

            val queryNormalizada = query.lowercase()

            val snapshot = db.collection(collectionName)
                .whereEqualTo("userId", user.uid)
                .whereGreaterThanOrEqualTo("titulo_lower", queryNormalizada)
                .whereLessThanOrEqualTo("titulo_lower", queryNormalizada + "\uf8ff")
                .get()
                .await()

            val listas = snapshot.toObjects(Lista::class.java)
            Result.success(listas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}