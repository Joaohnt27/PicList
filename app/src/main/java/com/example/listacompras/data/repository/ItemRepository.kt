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
            val docRef = db.collection(collection).document() // Novo ID
            val novoItem = item.copy(id = docRef.id)
            docRef.set(novoItem).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun atualizarItem(item: Item): Result<Boolean> {
        return try {
            db.collection(collection).document(item.id).set(item).await()
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
            val snapshot = db.collection("itens")
                .whereEqualTo("listaId", listaId)
                // Busca pelo nome do produto
                .whereGreaterThanOrEqualTo("nome", query)
                .whereLessThanOrEqualTo("nome", query + "\uf8ff")
                .get()
                .await()

            val itens = snapshot.toObjects(Item::class.java)
            Result.success(itens)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}