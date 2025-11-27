package com.example.listacompras.data.repository

import com.example.listacompras.data.model.Item
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

interface ItemRepository {
    suspend fun buscarItens(listaId: String): Result<List<Item>>
    suspend fun salvarItem(item: Item): Result<Boolean>
    suspend fun atualizarItem(item: Item): Result<Boolean>
    suspend fun deletarItem(itemId: String): Result<Boolean>
    suspend fun pesquisarItens(listaId: String, query: String): Result<List<Item>>
}

class ItemRepositoryImpl : ItemRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = "itens"

    override suspend fun buscarItens(listaId: String): Result<List<Item>> {
        return try {
            val snapshot = db.collection(collection)
                .whereEqualTo("listaId", listaId)
                .get()
                .await()
            val itens = snapshot.toObjects(Item::class.java)
            Result.success(itens)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun salvarItem(item: Item): Result<Boolean> {
        return try {
            val docRef = db.collection("itens").document()

            // Preenche o nome_lower
            val novoItem = item.copy(
                id = docRef.id,
                nome_lower = normalizar(item.nome)
            )

            docRef.set(novoItem).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun atualizarItem(item: Item): Result<Boolean> {
        return try {
            // Atualiza o nome_lower caso o nome tenha mudado
            val itemAtualizado = item.copy(
                nome_lower = normalizar(item.nome)
            )

            db.collection("itens").document(item.id).set(itemAtualizado).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletarItem(itemId: String): Result<Boolean> {
        return try {
            db.collection(collection).document(itemId).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun pesquisarItens(listaId: String, query: String): Result<List<Item>> {
        return try {
            val queryNormalizada = normalizar(query)

            val snapshot = db.collection("itens")
                .whereEqualTo("listaId", listaId)
                .whereGreaterThanOrEqualTo("nome_lower", queryNormalizada)
                .whereLessThanOrEqualTo("nome_lower", queryNormalizada + "\uf8ff")
                .get()
                .await()

            val itens = snapshot.toObjects(Item::class.java)
            Result.success(itens)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun normalizar(texto: String): String {
        val n = java.text.Normalizer.normalize(texto.trim(), java.text.Normalizer.Form.NFD)
        return n.replace("\\p{M}+".toRegex(), "").lowercase()
    }
}