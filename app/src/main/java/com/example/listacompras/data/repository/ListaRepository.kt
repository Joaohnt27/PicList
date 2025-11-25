package com.example.listacompras.data.repository

import android.net.Uri
import com.example.listacompras.data.model.Lista
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

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
    private val storage = FirebaseStorage.getInstance()
    private val collectionName = "listas"

    // Função auxiliar para fazer Upload da Imagem
    private suspend fun uploadImage(uri: Uri): String? {
        return try {
            val user = auth.currentUser ?: return null
            // Cria um nome único para a imagem: imagens/UID_DO_USER/ID_ALEATORIO.jpg
            val ref = storage.reference.child("imagens/${user.uid}/${UUID.randomUUID()}.jpg")

            // Faz o upload
            ref.putFile(uri).await()

            // Pega a URL pública (downloadUrl)
            val url = ref.downloadUrl.await()
            url.toString()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun salvarLista(lista: Lista, imageUriLocal: Uri?): Result<Boolean> {
        return try {
            val user = auth.currentUser ?: throw Exception("Usuário Off")
            val docRef = db.collection(collectionName).document() // Gera ID novo

            // 1. Se tiver imagem, faz upload primeiro
            var urlImagemRemota = ""
            if (imageUriLocal != null) {
                urlImagemRemota = uploadImage(imageUriLocal) ?: ""
            }

            // 2. Cria o objeto final com ID e URL
            val novaLista = lista.copy(
                id = docRef.id,
                userId = user.uid,
                imageUri = urlImagemRemota // Salva a URL do Storage no Firestore
            )

            // 3. Salva no banco
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

            // Ordenação Alfabética (Requisito RF003)
            val listas = snapshot.toObjects(Lista::class.java)
                .sortedBy { it.titulo.lowercase() }

            Result.success(listas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun excluirLista(lista: Lista): Result<Boolean> {
        return try {
            // 1. Apagar imagem do Storage (se houver)
            if (lista.imageUri?.isNotEmpty() == true) {
                try {
                    val ref = storage.getReferenceFromUrl(lista.imageUri)
                    ref.delete().await()
                } catch (e: Exception) {
                    // Se falhar ao apagar imagem (ex: não existe), segue o baile
                }
            }

            // 2. Apagar TODOS os itens dessa lista (Cascade Delete)
            // Assumindo que você terá uma coleção "itens" onde cada item tem "listaId"
            val itensRef = db.collection("itens")
                .whereEqualTo("listaId", lista.id)
                .get()
                .await()

            // Apaga cada item encontrado em lote (Batch)
            val batch = db.batch()
            for (document in itensRef) {
                batch.delete(document.reference)
            }
            batch.commit().await()

            // 3. Finalmente, apaga a Lista
            db.collection(collectionName).document(lista.id).delete().await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun editarLista(lista: Lista, novaImageUri: Uri?): Result<Boolean> {
        return try {
            var listaAtualizada = lista

            // Se o usuário escolheu uma NOVA imagem
            if (novaImageUri != null) {
                // 1. Tenta apagar a antiga para não deixar lixo
                if (!lista.imageUri.isNullOrEmpty()) {
                    try { storage.getReferenceFromUrl(lista.imageUri).delete().await() } catch (_: Exception){}
                }
                // 2. Sobe a nova
                val novaUrl = uploadImage(novaImageUri)
                listaAtualizada = lista.copy(imageUri = novaUrl)
            }

            // 3. Atualiza no Firestore
            db.collection(collectionName).document(lista.id).set(listaAtualizada).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun pesquisarListas(query: String): Result<List<Lista>> {
        return try {
            val user = auth.currentUser ?: throw Exception("Usuário Off")

            // TRUQUE DO FIRESTORE PARA BUSCA DE TEXTO ("COMEÇA COM...")
            // Ex: titulo >= "Arr" E titulo <= "Arr" + "uf8ff" (último caractere possível)
            val snapshot = db.collection("listas")
                .whereEqualTo("userId", user.uid)
                .whereGreaterThanOrEqualTo("titulo", query)
                .whereLessThanOrEqualTo("titulo", query + "\uf8ff")
                .get()
                .await()

            val listas = snapshot.toObjects(Lista::class.java)
            Result.success(listas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}