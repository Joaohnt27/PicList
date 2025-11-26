package com.example.listacompras.data.repository

import com.example.listacompras.data.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// O contrato (O QUE fazer)
interface AuthRepository {
    suspend fun login(email: String, pass: String): Result<Usuario?>
    suspend fun cadastro(nome: String, email: String, pass: String): Result<Usuario?>
    fun logout()
    fun getCurrentUser(): FirebaseUser?
    suspend fun recuperarSenha(email: String): Result<Boolean>
}

// O COMO fazer
class AuthRepositoryImpl : AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override suspend fun cadastro(nome: String, email: String, pass: String): Result<Usuario?> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = authResult.user

            val usuario = firebaseUser?.let { user ->
                val novoUsuario = Usuario(
                    id = user.uid,
                    nome = nome,
                    email = email
                )

                db.collection("usuarios")
                    .document(user.uid)
                    .set(novoUsuario)
                    .await()

                novoUsuario
            }

            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, pass: String): Result<Usuario?> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val firebaseUser = result.user ?: return Result.success(null)

            // tenta buscar dados no Firestore
            val doc = db.collection("usuarios").document(firebaseUser.uid).get().await()

            val usuario = if (doc.exists()) {
                doc.toObject(Usuario::class.java)
            } else {
                // fallback se não tiver documento (só do Auth)
                Usuario(id = firebaseUser.uid, nome = firebaseUser.displayName ?: "", email = firebaseUser.email ?: email)
            }

            Result.success(usuario)
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