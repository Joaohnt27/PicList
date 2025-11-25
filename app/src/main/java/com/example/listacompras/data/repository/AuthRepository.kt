package com.example.listacompras.data.repository

import com.example.listacompras.data.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// O contrato (O QUE fazer)
interface AuthRepository {
    suspend fun login(email: String, pass: String): Result<FirebaseUser?>
    suspend fun cadastro(nome: String, email: String, pass: String): Result<FirebaseUser?>
    fun logout()
    fun getCurrentUser(): FirebaseUser?
    suspend fun recuperarSenha(email: String): Result<Boolean>
}

// O COMO fazer
class AuthRepositoryImpl : AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override suspend fun cadastro(nome: String, email: String, pass: String): Result<FirebaseUser?> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                val novoUsuario = Usuario(
                    id = firebaseUser.uid,
                    nome = nome,
                    email = email
                )

                db.collection("usuarios")
                    .document(firebaseUser.uid)
                    .set(novoUsuario)
                    .await()
            }

            Result.success(firebaseUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, pass: String): Result<FirebaseUser?> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            Result.success(result.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun logout() {
        auth.signOut()
    }

    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
    override suspend fun recuperarSenha(email: String): Result<Boolean> {
        return try {
            // O Firebase envia o email automaticamente
            auth.sendPasswordResetEmail(email).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}