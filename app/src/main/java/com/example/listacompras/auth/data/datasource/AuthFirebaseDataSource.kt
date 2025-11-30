package com.example.listacompras.auth.data.datasource

import com.example.listacompras.auth.data.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

//COMO deve fazer
class AuthFirebaseDataSource : AuthDataSource {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override suspend fun createAccount(nome: String, email: String, pass: String): Result<FirebaseUser?> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            Result.success(authResult.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveUserToFirestore(uid: String, nome: String, email: String): Result<Unit> {
        return try {
            val novoUsuario = Usuario(id = uid, nome = nome, email = email)
            db.collection("usuarios")
                .document(uid)
                .set(novoUsuario)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signIn(email: String, pass: String): Result<FirebaseUser?> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            Result.success(result.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserFromFirestore(uid: String): Result<Usuario?> {
        return try {
            val doc = db.collection("usuarios").document(uid).get().await()
            val usuario = doc.toObject(Usuario::class.java)
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun signOut() = auth.signOut()

    override fun getCurrentUser(): FirebaseUser? = auth.currentUser

    override suspend fun sendPasswordResetEmail(email: String): Result<Boolean> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


